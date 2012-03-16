'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''


from django.contrib.auth import login, authenticate, logout
from django.contrib.auth.decorators import login_required
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.http import HttpResponseRedirect
from globalink.observer.models import RegisteredObserver, RegistrationManager
from globalink.observer.forms import ProfileForm, RegistrationForm,\
        RegistrationConfirmationForm, PasswordChangeForm
from globalink.views import redirect_home_with_message

import logging
from globalink.observation.models import Observation


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")



    
def dologin(request):
    if request.method == 'POST':
        username = request.POST['email']                                                                          
        password = request.POST['password']                                                                       
        logger.debug("Got user %s %s" % (username, password))                                                     
        user = authenticate(username=username, password=password)                                                 
        if user is not None:                                                                                      
            if user.is_active:                                                                                    
                login(request, user)
            else:                                                                                                 
                return redirect_home_with_message(request, "ERROR: Login failed. This account is disabled.")
        else:                                                                                                     
            return redirect_home_with_message(request, "ERROR: Invalid credentials. Login failed.")
    
    return redirect_home_with_message(request, None)


def dologout(request):
    logout(request)
    return redirect_home_with_message(request, None)


@login_required
def profile(request):
    user = request.user
    observer = RegisteredObserver.objects.get(user=user)

    if request.method == 'POST':
        form = ProfileForm(request.POST)
        if form.is_valid():
            cd = form.cleaned_data
            # update user data
            observer.affiliation = cd['affiliation']
            observer.name = cd['name']
            observer.surname = cd['surname']
            observer.save()
            form.message = 'Your profile has been updated.' 
    else:
        form = ProfileForm(initial={'email': user.username,
                                    'name': observer.name,
                                    'surname': observer.surname,
                                    'affiliation': observer.affiliation,
                                    })
        
    b = {'form': form,
         'observer': observer}
    return render_to_response('observer/profile.html', b,
                                context_instance=RequestContext(request))


@login_required
def password_change(request):
    user = request.user
    observer = RegisteredObserver.objects.get(user=user)
    if request.method == 'POST':
        form = PasswordChangeForm(request.user, request.POST)
        if form.is_valid():
            cd = form.cleaned_data
            new_pass = cd['new_password']                                      
            user.set_password(new_pass)    
            user.save()                                               
            observer.set_password(new_pass)                            
            observer.save()
            return HttpResponseRedirect('/observer/profile')                                             
    else:
        form = PasswordChangeForm(request.user)
        
    return render_to_response('observer/password_change.html',{'form': form},
                                context_instance=RequestContext(request))


def count_observations_for_observer(o):
    return Observation.objects(user=o).count()

def count_cars_for_observer(o):
    cars = 0
    observations = Observation.objects(user=o)
    for obs in observations:
        cars = cars + obs.no_smoking + obs.lone_adult + obs.other_adults + obs.child 
    return cars

def count_duration_for_observer(o):
    duration = 0
    observations = Observation.objects(user=o)
    for obs in observations:
        duration = duration + obs.duration
    return duration

def sort_observers_for_hall_of_fame(list):
    '''
    each Observation is worth 1 point (scaling factor 1)
    each 80 cars is worth 1 point (scaling factor 80)
    each 10min (600k millis) of observing is worth 1 point (scaling factor 600 000)
    '''
    # lets calculate the ranking 
    observation_scale = 1
    duration_scale = 600000
    cars_scale = 80
    result = []
    for o in list:
        o.ranking = o.num_of_observations / observation_scale + \
                    o.duration / duration_scale + \
                    o.num_of_cars / cars_scale
        result.append(o)
    # now we just sort the list according to the ranking
    return sorted(result, key=lambda x: x.ranking, reverse=True)

def hall_of_fame(request):
    obs = RegisteredObserver.objects
    result = []
    for o in obs:
        o.num_of_observations = count_observations_for_observer(o)
        o.num_of_cars = count_cars_for_observer(o)
        o.duration = count_duration_for_observer(o)
        result.append(o)
        
    result_sorted = sort_observers_for_hall_of_fame(result)
    return render_to_response('observer/hall_of_fame.html',
                                        {'observers': result_sorted },
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
                
            return render_to_response('observer/register_confirm.html', 
                              {'form': form},
                              context_instance=RequestContext(request))
        
    else:
        form = RegistrationForm()
        
    return render_to_response('observer/register.html',
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
            return redirect_home_with_message(request, 'Thank you for participating. You can now start logging the data on your Android.')
    else:
        form = RegistrationConfirmationForm()
        
    return render_to_response('observer/register_confirm.html',
                                        {'form': form},
                                        context_instance=RequestContext(request))
    

