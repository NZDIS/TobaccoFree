'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''

from django.contrib.auth.decorators import login_required
from django.core.mail import send_mail
from django.utils import simplejson as json
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse, HttpResponseForbidden
from django.template import RequestContext
from django.shortcuts import render_to_response
from datetime import datetime

from globalink.observation.models import RegisteredObserver, Observation, ObservationJSONEncoder
    
from mongoengine.django.auth import User
import mongoengine

from globalink.views import redirect_home_with_message
from globalink.observation.forms import FeedbackForm

from globalink import settings

import urllib
import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")



GEOCODE_BASE_URL = 'http://maps.googleapis.com/maps/geo?'

def geocode(lat, lng, **geo_args):
    geo_args.update({
        'key': settings.GOOGLE_KEY,
        'output': 'json',
        'q': str(lat) + ',' + str(lng),
        'sensor': False  
    })

    url = GEOCODE_BASE_URL + urllib.urlencode(geo_args)
    result = json.load(urllib.urlopen(url))
    
    try:
        city = result['Placemark'][0]['AddressDetails']['Country']['AdministrativeArea']['Locality']['LocalityName']
        country = result['Placemark'][0]['AddressDetails']['Country']['CountryName']
        return (city, country)
    except:
        return ('','')
    
    
def do_geocode_data(request):
    obj = Observation.objects
    for o in obj:
        if o.get('loc_city') == None or o.get('loc_city') == '':
            (city, country) = geocode(o.latitude, o.longitude)
            o.loc_city = city
            o.loc_country = country
            o.save()
    return redirect_home_with_message(request, "Geocoding done")



def home(request):
    return redirect_home_with_message(request, None)


def feedback(request):
    '''
    Process the feedback form. Send email if form OK.
    '''
    if request.method == 'POST':
        form = FeedbackForm(request.POST)
        if form.is_valid():
            cd = form.cleaned_data
            form = FeedbackForm()
            subject = cd['subject']
            subject = "[TobaccoFree Feedback]" + subject
            try:
                send_mail(subject,
                          cd['description'],
                          cd.get('email', settings.DEFAULT_FROM_EMAIL),
                            [settings.DEFAULT_FEEDBACK_EMAIL, ],)
                
                form.message = "Thank you for your feedback"
            except:
                form.message = "Email server is not working here" 
        
    return render_to_response('observation/index.html',
                                        {'form': form},
                                        context_instance=RequestContext(request))
    
    

   
@csrf_exempt
def add(request):
    '''
    Processes a POST request with Observation data, and puts new record into a DB.
    '''
#    logger.debug("Got a request.POST: {0}".format(request.POST))
    json_str = request.POST.get('Observation')
    logger.debug("Got a request.POST.Observation: {0}".format(json_str))
    
    if request.method == "POST":
        new_ob = json.loads(json_str)
        email_str = new_ob.get('user_email')
        try:
            userId = User.objects.get(username=email_str)
            u = RegisteredObserver.objects.get(user=userId)
        except:
            logger.debug("User doesn't exist")
            return HttpResponseForbidden("No user with this email address.")
        data_pass_hash = new_ob.get('pass_hash')
        alongitude = float(new_ob.get('longitude'))
        alatitude = float(new_ob.get('latitude'))
        astart = new_ob.get('start')
        afinish = new_ob.get('finish')
        version = int(new_ob.get('version', '1'))
        if (u.password_hash == data_pass_hash) and u.registration_confirmed:
            if version == 1:
                o = Observation(
                        observation_hash = new_ob.get('hash'),
                        latitude = alatitude,
                        longitude = alongitude,
                        loc = [alongitude, alatitude],
                        start = datetime.fromtimestamp(astart / 1000.0),
                        finish = datetime.fromtimestamp(afinish / 1000.0),
                        duration = (afinish - astart),
                        no_smoking = int(new_ob.get('lone_adult')),
                        other_adults = int(new_ob.get('child')),
                        lone_adult = int(new_ob.get('other_adults')),
                        child = 0,
                        device_id = new_ob.get('device'),
                        upload_timestamp = datetime.now(),
                        user = u)
            else:
                o = Observation(
                        observation_hash = new_ob.get('hash'),
                        latitude = alatitude,
                        longitude = alongitude,
                        loc = [alongitude, alatitude],
                        start = datetime.fromtimestamp(astart / 1000.0),
                        finish = datetime.fromtimestamp(afinish / 1000.0),
                        duration = (afinish - astart),
                        no_smoking = int(new_ob.get('no_smoking')),
                        other_adults = int(new_ob.get('other_adults')),
                        lone_adult = int(new_ob.get('lone_adult')),
                        child = int(new_ob.get('child')),
                        device_id = new_ob.get('device'),
                        upload_timestamp = datetime.now(),
                        user = u)
            (city, country) = geocode(o.latitude, o.longitude)
            o.loc_city = city
            o.loc_country = country
            o.save()
            logger.debug("New instance of an Observation has been saved!")
            return HttpResponse("Observation was added. Success.")
        else:
            logger.debug("User provided wrong password")
            return HttpResponseForbidden("Bad credentials.")
    else:
        logger.debug("got GET instead of POST")
        return HttpResponse('Go back <a href="/">home</a>.')




@login_required
def do_list(request):
    print Observation.objects.distinct('loc_city')
    
    observer = RegisteredObserver.objects.get(user=request.user)
    obs = Observation.objects(user=observer)
    obs_conv = []
    for o in obs:
        sec = o.duration / 1000
        minute = sec / 60
        sec = sec - (minute * 60)
        o.duration = '%dmin %dsec' % (minute, sec)
        obs_conv.append(o) 
    return render_to_response('observation/list.html',
                                        {'observer': observer,
                                         'observations': obs_conv},
                                        context_instance=RequestContext(request))
    
    

def all_latlng(request):
    '''
    Returns a JSON object with an array of all observations coordinates and description.
    '''
    obs = Observation.objects
    return HttpResponse(json.dumps(obs, cls=ObservationJSONEncoder), mimetype='application/json')

