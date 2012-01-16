'''
Created on Jan 12, 2012

@author: mariusz
'''


from mongoengine import Document, StringField,\
        IntField, FloatField, DateTimeField, ReferenceField, GeoPointField
        
from globalink.observer.models import RegisteredObserver

import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")


   


class Observation(Document):
    observation_hash = StringField(max_length=64, unique=True)
    latitude = FloatField()
    longitude = FloatField()
    loc = GeoPointField()
    start = DateTimeField()
    finish = DateTimeField()
    duration = IntField() #duration in milliseconds, finish-start
    no_smoking = IntField()
    other_adults = IntField()
    lone_adult = IntField()
    child = IntField()
    device_id = StringField(max_length=128)
    upload_timestamp = DateTimeField()
    user = ReferenceField(RegisteredObserver)

    def __unicode__(self):
        return u"Start: {0} Finish: {1} By: {2}".format(self.start, self.finish, self.user)

