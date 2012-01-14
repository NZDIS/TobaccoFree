'''
Created on Jan 12, 2012

@author: mariusz
'''


from django.db import models
from django.template.loader import render_to_string
from django.contrib.sites.models import Site
import random, hashlib, settings

import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")


class RegistrationManager(models.Manager):

    def confirm_observer(self, activation_key):
        '''
        Validates an activation key and confirms/activates the Observer.
        
        If the key is valid returns the Observer after activating.
        If the key is not valid returns ``False``.
        '''
        # Make sure the key we're trying conforms to the pattern of a
        # SHA1 hash; if it doesn't, no point trying to look it up in
        # the database.
        try:                                                               
            profile = self.get(activation_key=activation_key) 
        except self.model.DoesNotExist:                                    
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
        return new_observer
    
        



class RegisteredObserver(models.Model):
    email = models.EmailField(help_text="Primary email address, used as user identifier", unique=True)
    name = models.CharField(max_length=128, help_text="User real name")
    surname = models.CharField(max_length=256, help_text="User real surname")
    affiliation = models.TextField(help_text="Affiliation, university, department, etc.")
    password_hash = models.CharField(max_length=64, help_text='Enter password here. It will be hashed automatically.')
    approved = models.BooleanField(help_text="Is this user approved?", default=False)
    registration_confirmed = models.BooleanField(help_text="Has this user confirmed her/his registration?", default=False)
    registration_date = models.DateTimeField(auto_now_add=True, blank=True, help_text="Timestamp of user registration")
    activation_key = models.CharField(max_length=256)
    
    objects = RegistrationManager()
    
    def __unicode__(self):
        return u"{0} [{1} {2}]".format(self.email, self.name, self.surname)




class Observation(models.Model):
    observation_hash = models.CharField(max_length=64, unique=True)
    latitude = models.FloatField()
    longitude = models.FloatField()
    start = models.DateTimeField()
    finish = models.DateTimeField()
    no_smoking = models.IntegerField()
    other_adults = models.IntegerField()
    lone_adult = models.IntegerField()
    child = models.IntegerField()
    device_id = models.CharField(max_length=128)
    upload_timestamp = models.DateTimeField()
    user = models.ForeignKey(RegisteredObserver)

    def __unicode__(self):
        return u"Start: {0} Finish: {1} By: {2}".format(self.start, self.finish, self.user.email)

