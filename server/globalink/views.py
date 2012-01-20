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




def prepareStatistics():
    b = {}
    b['number_of_registered_observers'] = RegisteredObserver.objects.count()
    b['number_of_observations'] = Observation.objects.count()
    total_cars = 0
    total_non_smokers = 0
    for o in Observation.objects:
        total_non_smokers += o.no_smoking
        total_cars += o.no_smoking + o.lone_adult + o.child + o.other_adults
    
    b['number_of_cars'] = total_cars
    b['ratio_of_smokers'] = (1.0 - float(float(total_non_smokers) / float(total_cars))) * 100.0
    return b



def redirect_home_with_message(request, message):
    params = prepareStatistics()
    form = FeedbackForm()
    params['form'] = form
    form.message = message
    return render_to_response('observation/index.html', 
                              params,
                              context_instance=RequestContext(request))