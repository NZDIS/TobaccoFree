'''
Created on Jan 16, 2012

@author: mariusz
'''



from django.template import RequestContext
from django.shortcuts import render_to_response

from globalink.observer.models import RegisteredObserver
from globalink.observation.models import Observation


from globalink.observation.forms import FeedbackForm


import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")


def get_cars_and_nonsmokers(obs):
    total_cars = 0
    total_non_smokers = 0
    for o in obs:
        total_non_smokers += o.no_smoking
        total_cars += o.no_smoking + o.lone_adult + o.child + o.other_adults
    return (total_cars, total_non_smokers)

def prepare_stats_per_country():
    countrynames =  Observation.objects.distinct('loc_country')
    countries = []
    for c in countrynames:
        obs = Observation.objects(loc_country = c)
        total_cars, total_non_smokers = get_cars_and_nonsmokers(obs)
        country = {}
        country['country_name'] = c
        country['num_cars'] = total_cars
        country['ratio_of_smokers'] = (1.0 - float(float(total_non_smokers) / float(total_cars))) * 100.0
        country['num_observations'] = obs.count()
        countries.append(country)
    return countries

def prepare_stats_per_city():
    citynames =  Observation.objects.distinct('loc_city')
    cities = []
    for c in citynames:
        obs = Observation.objects(loc_city = c)
        total_cars, total_non_smokers = get_cars_and_nonsmokers(obs)
        country = {}
        country['city_name'] = c
        country['num_cars'] = total_cars
        country['ratio_of_smokers'] = (1.0 - float(float(total_non_smokers) / float(total_cars))) * 100.0
        country['num_observations'] = obs.count()
        cities.append(country)
    return cities


def prepareStatistics():
    b = {}
    b['number_of_registered_observers'] = RegisteredObserver.objects.count()
    b['number_of_active_observers'] = len(Observation.objects().distinct("user"))
    
    number_of_observations = Observation.objects.count()
    b['number_of_observations'] = number_of_observations
    number_of_iphone_observations = Observation.objects(device_type__icontains='iphone').count()
    b['number_of_iphone_observations'] = number_of_iphone_observations
    b['number_of_android_observations'] = number_of_observations - number_of_iphone_observations
    
    
    (total_cars, total_non_smokers) = get_cars_and_nonsmokers(Observation.objects)
    
    b['number_of_cars'] = total_cars
    b['ratio_of_smokers'] = (1.0 - float(float(total_non_smokers) / float(total_cars))) * 100.0
    
    b['countries'] = prepare_stats_per_country()
    b['cities'] = prepare_stats_per_city()
    return b



def redirect_home_with_message(request, message):
    params = prepareStatistics()
    form = FeedbackForm()
    params['form'] = form
    form.message = message
    return render_to_response('observation/index.html', 
                              params,
                              context_instance=RequestContext(request))