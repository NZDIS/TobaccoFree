'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''

from django.contrib.auth.decorators import login_required

from django.template import RequestContext
from django.shortcuts import render_to_response
from mongoengine.django.auth import User

from globalink.admin.forms import FullProfileForm
from globalink.observation.models import RegisteredObserver, Observation

from globalink.views import redirect_home_with_message

import logging


# Get an instance of a logger
logger = logging.getLogger("globalink.custom")





@login_required
def do_full_profile(request):
    current_user = request.user
    if (current_user.is_staff):
        email = request.REQUEST["observer"]
        u = User.objects.get(username=email)
        observer = RegisteredObserver.objects.get(user=u)

        if request.method == 'POST':
            form = FullProfileForm(request.POST)  
            if form.is_valid():                                
                cd = form.cleaned_data                         
                # update user data                             
                observer.affiliation = cd['affiliation']       
                observer.name = cd['name']                     
                observer.surname = cd['surname']
                observer.approved = cd['approved']
                observer.activation_key = cd['activation_key']
                observer.user.is_staff = cd['is_staff']
                observer.user.is_active = cd['is_active']
                observer.save()                                
                form.message = 'Profile has been updated.' 
        else:
            form = FullProfileForm(initial={
                               'email' : u.username,
                               'name' : observer.name,
                               'surname' : observer.surname,
                               'affiliation': observer.affiliation,
                               'activation_key': observer.activation_key,
                               'is_active': u.is_active,
                               'is_staff': u.is_staff,
                               'approved': observer.approved })
        return render_to_response('admin/full_profile.html',
                                        {'observer': observer,
                                         'form': form },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")
        


@login_required
def do_observers_list(request):
    current_user = request.user
    if (current_user.is_staff):
        obs = RegisteredObserver.objects
        return render_to_response('admin/observers_list.html',
                                        {'observers': obs },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")


@login_required
def do_profile_delete(request):
    current_user = request.user
    if (current_user.is_staff):        
        email = request.REQUEST["observer"]
        u = User.objects.get(username=email)
        try:
            observer = RegisteredObserver.objects.get(user=u)
            u.delete(safe=True)
            observer.delete(safe=True)
        except:
            pass # ignore request with wrong data
        obs = RegisteredObserver.objects
        return render_to_response('admin/observers_list.html',
                                        {'observers': obs, 
                                         'message': 'Observer profile has been removed.'},
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")
        
@login_required
def do_observation_delete(request):
    current_user = request.user
    if (current_user.is_staff):        
        id = request.REQUEST["id"]
        try:
            o = Observation.objects.get(id=id)
            o.delete(safe=True)
        except:
            pass # no obsernation with this IDh
        obs = Observation.objects
        return render_to_response('admin/observations_list.html',
                                        {'observations': obs },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")


        

@login_required
def do_observations_list(request):
    user = request.user
    if (user.is_staff):
        obs = Observation.objects
        obs_conv = []
        for o in obs:
            sec = o.duration / 1000
            min = sec / 60
            sec = sec - (min * 60)
            o.duration = '%dmin %dsec' % (min, sec)
            obs_conv.append(o) 
        return render_to_response('admin/observations_list.html',
                                        {'observations': obs_conv },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")