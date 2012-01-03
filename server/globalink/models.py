'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''

from django.db import models

class GlobalinkUser(models.Model):
    name = models.CharField(max_length=100)
    surname = models.CharField(max_length=100)
    affiliation = models.TextField(required=True)
    email = models.EmailField(required=True)
    password = models.TextField(required=True)
    
class Observation(models.Model):
    id = models.IntegerField()
    latitude = models.FloatField()
    longitude = models.FloatField()
    start = models.DateTimeField()
    finish = models.DateTimeField()
    user = models.ForeignKey(GlobalinkUser)
    
class Choice(models.Model):
    observation_id = models.ForeignKey(Observation)
    event_type = models.CharField(max_length=200)
    timestamp = models.DateTimeField()