'''
Created on Jan 16, 2012

@author: mariusz
'''



from django.template import RequestContext
from django.shortcuts import render_to_response

from globalink.observer.models import RegisteredObserver
from globalink.observation.models import Observation

from globalink.observation.forms import FeedbackForm

import logging, math



Z_CONFIDENCE_VALUE = 2.5759 # 99% confidence interval



# Get an instance of a logger
logger = logging.getLogger("globalink.custom")


def CI_interval(prob, samples):
    '''
    Calculates the confidence interval based on the sample size
    '''
    return (Z_CONFIDENCE_VALUE * math.sqrt((prob * (1.0 - prob)) / samples)) * 100.0
    

def get_cars_and_nonsmokers(obs):
    total_cars = 0
    total_non_smokers = 0
    for o in obs:
        total_non_smokers += o.no_smoking
        total_cars += o.no_smoking + o.lone_adult + o.child + o.other_adults
    return (total_cars, total_non_smokers)


def get_second_hand_exposure(obs):
    total_smokers = 0
    total_second_hand = 0
    for o in obs:
        total_smokers += o.lone_adult + o.other_adults + o.child
        total_second_hand += o.other_adults + o.child
    if total_smokers > 1:
        return ((float(total_second_hand) / float(total_smokers)), total_smokers)
    else: 
        return (0, 1)
    
def get_child_exposure(obs):
    total_smokers = 0
    total_child = 0
    for o in obs:
        total_smokers += o.lone_adult + o.other_adults + o.child
        total_child += o.child
    if total_smokers > 1:
        return (float(total_child) / float(total_smokers)), total_smokers
    else:
        return 0, 1


def prepare_stats_for_observations(obs):
    total_cars, total_non_smokers = get_cars_and_nonsmokers(obs)
    area = {}
    area['num_cars'] = total_cars
    prob_non_smoke = float(float(total_non_smokers) / float(total_cars))
    area['ratio_of_smokers'] = (1.0 - prob_non_smoke) * 100.0
    area['ratio_of_smokers_interval'] = CI_interval(prob_non_smoke, total_cars)
    prob_second_hand, total_smokers = get_second_hand_exposure(obs)
    area['ratio_of_second_hand'] = prob_second_hand * 100.0
    area['ratio_of_second_hand_interval'] = CI_interval(prob_second_hand, total_smokers) 
    prob_child, total_smokers = get_child_exposure(obs)
    area['ratio_of_child'] = prob_child * 100.0
    area['ratio_of_child_interval'] = CI_interval(prob_child, total_smokers)
    area['num_observations'] = obs.count()
    return area

def prepare_stats_per_country():
    countrynames =  Observation.objects.distinct('loc_country')
    countries = []
    for c in countrynames:
        obs = Observation.objects(loc_country = c)
        country = prepare_stats_for_observations(obs)
        country['country_name'] = c
        countries.append(country)
    return countries

def prepare_stats_per_city():
    citynames =  Observation.objects.distinct('loc_city')
    cities = []
    for c in citynames:
        obs = Observation.objects(loc_city = c)
        city = prepare_stats_for_observations(obs)
        city['city_name'] = c
        cities.append(city)
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
    prob_non_smoke = float(float(total_non_smokers) / float(total_cars))
    b['ratio_of_smokers'] = (1.0 - prob_non_smoke) * 100.0
    b['ratio_of_smokers_interval'] = CI_interval(prob_non_smoke, total_cars)
    
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