'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''

from django.utils import simplejson as json
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse, HttpResponseForbidden
from datetime import datetime
import logging

from globalink.observation.models import GlobalinkUser, Observation

# Get an instance of a logger
logger = logging.getLogger("globalink.custom")



def home(request):
    return HttpResponse("Place holder for the HOME PAGE.")



@csrf_exempt
def add(request):
    '''
    Processes a POST request with Observation data, and puts it into a DB.
    '''
    logger.debug("Got a request.POST: {0}".format(request.POST))
    json_str = request.POST.get('Observation')
    logger.debug("Got a request.POST.Observation: {0}".format(json_str))
    
    if request.method == "POST":
        new_ob = json.loads(json_str)
        email_str = new_ob.get('user_email')
        u = GlobalinkUser.objects.get(email=email_str)
        data_pass_hash = new_ob.get('pass_hash')
        if u.password_hash == data_pass_hash:
            o = Observation(observation_hash = new_ob.get('hash'),
                        latitude = float(new_ob.get('latitude')),
                        longitude = float(new_ob.get('longitude')),
                        start = datetime.fromtimestamp(float(new_ob.get('start') / 1000.0)),
                        finish = datetime.fromtimestamp(float(new_ob.get('finish') / 1000.0)),
                        no_smoking = int(new_ob.get('no_smoking')),
                        other_adults = int(new_ob.get('other_adults')),
                        lone_adult = int(new_ob.get('lone_adult')),
                        child = int(new_ob.get('child')),
                        device_id = new_ob.get('device'),
                        upload_timestamp = datetime.now(),
                        user = u)
            o.save()
            logger.debug("New instance of an Observation has been saved!")
            return HttpResponse("Observation was added. Success.")
        else:
            logger.debug("User provided wrong password")
            return HttpResponseForbidden("Bad credentials.")
    else:
        logger.debug("got GET instead of POST")
        return HttpResponse('Go back <a href="/">home</a>.')
