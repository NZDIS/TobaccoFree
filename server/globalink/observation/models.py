'''
Created on Jan 12, 2012

@author: mariusz
'''


from mongoengine import *

from django.template.loader import render_to_string
import random, hashlib
from datetime import datetime
from globalink import settings

import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")


   



class RegisteredObserver(Document):
    email = EmailField(help_text="Primary email address, used as user identifier", unique=True)
    name = StringField(max_length=128, help_text="User real name")
    surname = StringField(max_length=256, help_text="User real surname")
    affiliation = StringField(help_text="Affiliation, university, department, etc.")
    password_hash = StringField(max_length=64, help_text='Enter password here. It will be hashed automatically.')
    approved = BooleanField(default=False, help_text="Is this user approved?")
    registration_confirmed = BooleanField(default=False, help_text="Has this user confirmed her/his registration?")
    registration_date = DateTimeField(default=datetime.now, help_text="Timestamp of user registration")
    activation_key = StringField(max_length=256)
    
    def __unicode__(self):
        return u"{0} [{1} {2}]".format(self.email, self.name, self.surname)




class Observation(Document):
    observation_hash = StringField(max_length=64, unique=True)
    latitude = FloatField()
    longitude = FloatField()
    start = DateTimeField()
    finish = DateTimeField()
    no_smoking = IntField()
    other_adults = IntField()
    lone_adult = IntField()
    child = IntField()
    device_id = StringField(max_length=128)
    upload_timestamp = DateTimeField()
    user = ReferenceField(RegisteredObserver)

    def __unicode__(self):
        return u"Start: {0} Finish: {1} By: {2}".format(self.start, self.finish, self.user.email)



class RegistrationManager():

    def confirm_observer(self, activation_key):
        '''
        Validates an activation key and confirms/activates the Observer.
        
        If the key is valid returns the Observer after activating.
        If the key is not valid returns ``False``.
        '''
        profile = RegisteredObserver.objects.get(activation_key=activation_key) 
        if not profile:                                    
            return False                                                   
                                                                           
        profile.registration_confirmed = True;                             
        profile.activation_key = "ALREADY_ACTIVATED"                       
        profile.save()                                                     
        return profile                                                     

    
    def create_new_observer(self, email, name, surname, affiliation, password_hash):
        """
        Create a new, inactive ``Observer``, generates a
        ``profile`` and emails its activation key,
        returning the new ``Observer``.
        
        """
        new_observer = RegisteredObserver()
        new_observer.email = email
        new_observer.name = name
        new_observer.surname = surname
        new_observer.affiliation = affiliation
        new_observer.password_hash = password_hash
        new_observer.approved = False
        new_observer.registration_confirmed = False
        sha1 = hashlib.sha1()
        sha1.update(str(random.random()))
        salt = sha1.hexdigest()[:5]
        sha1 = hashlib.sha1()
        sha1.update(salt + email + name + affiliation)
        new_observer.activation_key = sha1.hexdigest()
        new_observer.save()
        
        from django.core.mail import send_mail
        subject = "Registration activation request for NZDIS: Tobacco Free"
        
        # Email subject *must not* contain newlines
        subject = ''.join(subject.splitlines())
            
        message = render_to_string('activation_email.txt',
                                   { 'activation_key': new_observer.activation_key,
                                     'name': new_observer.name, 
                                    })
        logger.debug("Trying to email this:\n" + message)
        try:
            send_mail(subject, message, settings.DEFAULT_FROM_EMAIL, [new_observer.email])
        except:
            pass
            # TODO what should we do if the email connection fails?
            # should never happen on the server
        return new_observer
    
    
    