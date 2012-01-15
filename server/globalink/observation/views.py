'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''

from django.core.mail import send_mail
from django.core.exceptions import ObjectDoesNotExist
from django.utils import simplejson as json
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse, HttpResponseForbidden
from django.template import RequestContext
from django.shortcuts import render_to_response
from datetime import datetime

from globalink.observation.models import RegisteredObserver, Observation,\
    RegistrationManager
from mongoengine.django.auth import User

from globalink.observation.forms import FeedbackForm, RegistrationForm,\
    RegistrationConfirmationForm

import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")



def prepareStatistics():
    b = {}
    b['number_of_registered_observers'] = RegisteredObserver.objects.count()
    b['number_of_observations'] = Observation.objects.count()
    return b



def home(request):
    params = prepareStatistics()
    form = FeedbackForm(initial={'subject': 'I love what you have done!'})
    params['form'] = form
    return render_to_response('observation/index.html', 
                              params,
                              context_instance=RequestContext(request))


def register(request):
    '''
    Process the Registration form.
    '''
    if request.method == 'POST':
        form = RegistrationForm(request.POST)
        if form.is_valid():
            cd = form.cleaned_data
            # create new RegisteredObserver here
            rm = RegistrationManager()
            newuser = rm.create_new_observer(
                          email = cd['email'],
                          name = cd['name'],                                       
                          surname = cd['surname'],                                   
                          affiliation = cd['affiliation'],                                                                     
                          password = cd['password'])
            newuser.save()                                                                       
            form = RegistrationConfirmationForm()                                                                
            form.message = 'Thank you for Registering. Confirmation and activation key has been e-mailed to you.' 
                
            return render_to_response('observation/register_confirm.html', 
                              {'form': form},
                              context_instance=RequestContext(request))
        
    else:
        form = RegistrationForm()
        
    return render_to_response('observation/register.html',
                                        {'form': form},
                                        context_instance=RequestContext(request))



def register_confirm(request):
    '''
    Process the registration confirmation form with activation key.
    '''
    if request.method == 'POST':
        form = RegistrationConfirmationForm(request.POST)
        if form.is_valid():
            cd = form.cleaned_data
            # create new RegisteredObserver here
            rm = RegistrationManager()
            rm.confirm_observer(cd['activation_key'],)
                                                                       
            form = FeedbackForm()                                          
            form.message = 'Thank you for participating. You can now start logging the data on your Android.' 
            
            return render_to_response('observation/index.html', 
                              {'form': form},
                              context_instance=RequestContext(request))
        
    else:
        form = RegistrationConfirmationForm()
        
    return render_to_response('observation/register_confirm.html',
                                        {'form': form},
                                        context_instance=RequestContext(request))
    


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
                          cd.get('email', 'noreply@nzdis.org'),
                            ['nowostawski@gmail.com', ],)
                
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

        if (u.password_hash == data_pass_hash) and u.registration_confirmed:
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
