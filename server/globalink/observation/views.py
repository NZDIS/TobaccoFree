'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''

from django.contrib.auth.decorators import login_required
from django.core.mail import send_mail
from django.utils import simplejson as json
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse, HttpResponseForbidden, \
                    HttpResponseRedirect, HttpResponseBadRequest
from django.template import RequestContext
from django.shortcuts import render_to_response
from datetime import datetime

from globalink.observation.models import RegisteredObserver, Observation,\
                        Detail, ObservationJSONEncoder
    
from mongoengine.django.auth import User

from globalink.observation.forms import FeedbackForm

from globalink import settings
from globalink.views import redirect_home_with_message, prepare_stats_per_country

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
    except:
        try:
            city = result['Placemark'][0]['AddressDetails']['Country']['AdministrativeArea']['SubAdministrativeArea']['Locality']['LocalityName']
        except:
            city = '' # problems reading the city
        
    try:
        country = result['Placemark'][0]['AddressDetails']['Country']['CountryName']
        return (city, country)
    except:
        country = ''  # problems reading the country
    return (city, country)
    
    
def do_geocode_data(request):
    obj = Observation.objects
    for o in obj:
        if o.loc_city == None or o.loc_city == '':
            (city, country) = geocode(o.latitude, o.longitude)
            o.loc_city = city
            o.loc_country = country
            o.save()
    return redirect_home_with_message(request, "Geocoding done")



   
def home(request):
    return redirect_home_with_message(request, None)



def _populate_row(v, c, base_label, base_label_interval):
    '''
    Populate vector 'v' with data from country 'c' based on 
    the base_label and base_label_interval data
    '''
    v.append({'v': c[base_label], 'f': "%.2f" % c[base_label]})
    interval_1 = c[base_label] - c[base_label_interval]
    if interval_1 < 0:
        interval_1 = 0.0
    v.append({'v': interval_1, 'f': "%.2f" % interval_1})
    interval_2 = c[base_label] + c[base_label_interval]
    v.append({'v': interval_2, 'f': "%.2f" % interval_2})


def prepare_stats_data(request):
    """
    Returns a json array of stats for google charts.
    """  
    countries = prepare_stats_per_country()
    
    cols = [{"label":"Country", "type":"string"}, 
            {"label":"General", "type":"number"},
            {"type":"number", "p":{"role":"interval"}},
            {"type":"number", "p":{"role":"interval"}},
 
            {"label":"Second-hand", "type":"number"},
            {"type":"number", "p":{"role":"interval"}},
            {"type":"number", "p":{"role":"interval"}},

            {"label":"Child occurrences", "type":"number"},
            {"type":"number", "p":{"role":"interval"}},
            {"type":"number", "p":{"role":"interval"}}]
    data = {}
    rows = []
    
    for c in countries:
        if c['num_cars'] > 30:
            v = []
            v.append({'v': c['country_name']})
            _populate_row(v, c, 'ratio_of_smokers', 'ratio_of_smokers_interval')
            _populate_row(v, c, 'ratio_of_second_hand', 'ratio_of_second_hand_interval')
            _populate_row(v, c, 'ratio_of_child', 'ratio_of_child_interval')
            rows.append({'c': v})
        
    data.update({"cols":cols, "rows":rows})
    return HttpResponse(json.dumps(data), mimetype='application/json')


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
#   logger.debug("Got a request.POST: {0}".format(request.POST))
    json_str = request.POST.get('Observation')
#   logger.debug(u"Got a request.POST.Observation string: {0}".format(json_str))
    json
    if request.method == "POST":
#       logger.debug('parsing...')
        try:
            new_ob = json.loads(json_str)
        except:
            try:
#               logger.debug('Workaround missing closing } in json stream')
                new_ob = json.loads(json_str + '}')
            except:
#               logger.debug("Json malformed")
                return HttpResponseBadRequest("Data malformed, cannot complete the upload.")
        logger.debug(u"new_ob after parsing: {0}".format(new_ob))
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
            elif version == 2:
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
            else:
                #version 3, we have details! 
                #version 4, we have phone type!
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
                details = new_ob.get('details')
                for d in details :
                    tmp_detail = Detail()
                    tmp_detail.timestamp = datetime.fromtimestamp(d.get('timestamp') / 1000.0)
                    tmp_detail.smoking_id = int(d.get('smoking_id'))
                    o.details.append(tmp_detail)
                # version 4, device type check:
                device_type = new_ob.get('device_type')
                if (device_type != None and device_type != ''):
                    o.device_type = device_type
                
            (city, country) = geocode(o.latitude, o.longitude)
            o.loc_city = city
            o.loc_country = country
            o.save(safe=True)
            print "Adding observation", o
            logger.debug("New instance of an Observation has been saved!")
            return HttpResponse("Observation was added. Success.")
        else:
            logger.debug("User provided wrong password")
            return HttpResponseForbidden("Bad credentials.")
    else:
        logger.debug("got GET instead of POST")
        return HttpResponse('Go back <a href="/">to TobaccoFree home</a>.')




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



def translators(request):
    return render_to_response('translators.html',
                                       context_instance=RequestContext(request))

def contributors(request):
    return render_to_response('contributors.html',
                                       context_instance=RequestContext(request))
    
def api_info(request):
    return render_to_response('api_info.html',
                                       context_instance=RequestContext(request))