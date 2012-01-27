'''
Created on Jan 12, 2012

@author: mariusz
'''


from mongoengine import Document, StringField,\
        IntField, FloatField, DateTimeField, ReferenceField, GeoPointField
        
from globalink.observer.models import RegisteredObserver

from django.utils import simplejson as json

import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")


   


class Observation(Document):
    observation_hash = StringField(max_length=64, unique=True)
    latitude = FloatField()
    longitude = FloatField()
    loc = GeoPointField()
    loc_city = StringField(max_length=64)
    loc_country = StringField(max_length=64)
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



class ObservationJSONEncoder(json.JSONEncoder):
    def encode_object(self, obj):
        sec = obj.duration / 1000
        minute = sec / 60
        sec = sec - (minute * 60)
        d = '%dmin %dsec' % (minute, sec)

        return {'id':unicode(obj.id), 
                'latitude': obj.latitude,
                'longitude': obj.longitude,
                'date': obj.start,
                'duration': d,
            'description': '''<table>
                                <thead>
                                  <td><img src="/static/images/nosmoking.png" width="20" height="20"></img></td>
                                  <td><img src="/static/images/lone_adult.png" width="25" height="25"></img></td>
                                  <td><img src="/static/images/other_adults.png" width="25" height="25"></img></td>
                                  <td><img src="/static/images/child.png" width="25" height="25"></img></td>
                                </thead>
                                <tr>
                                  <td> <big><b>%d</b></big> </td>
                                  <td> <big><b>%d</b></big> </td>
                                  <td> <big><b>%d</b></big> </td>
                                  <td> <big><b>%d</b></big> </td>
                                </tr>
                              </table>
                            ''' % (obj.no_smoking, obj.lone_adult, obj.other_adults, obj.child) 
                }

    def default(self, obj):
        if hasattr(obj, 'isoformat'):
            return obj.isoformat()
        elif hasattr(obj, '__iter__'):
            return [ self.encode_object(x) for x in obj ]
        else:
            return self.encode_object(obj)