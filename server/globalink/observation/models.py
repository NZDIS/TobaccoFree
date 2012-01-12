'''
Created on Jan 12, 2012

@author: mariusz
'''

from django.db import models


class GlobalinkUser(models.Model):
    email = models.EmailField(help_text="Primary email address, used as user identifier", unique=True)
    name = models.CharField(max_length=128, help_text="User real name")
    surname = models.CharField(max_length=256, help_text="User real surname")
    affiliation = models.TextField(help_text="Affiliation, university, department, etc.")
    password_hash = models.CharField(max_length=64, help_text='Enter password here. It will be hashed automatically.')

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
    user = models.ForeignKey(GlobalinkUser)

    def __unicode__(self):
        return u"Start: {0} Finish: {1} By: {2}".format(self.start, self.finish, self.user.email)

