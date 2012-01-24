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
    user = request.user
    if (user.is_staff):
        email = request.REQUEST["observer"]
        u = User.objects.get(username=email)
        obs = RegisteredObserver.objects.get(user=u)
        form = FullProfileForm(initial={
                               'email' : u.username,
                               'name' : obs.name,
                               'surname' : obs.surname,
                               'affiliation': obs.affiliation,
                               'activation_key': obs.activation_key,
                               'is_staff': u.is_staff,
                               'approved': obs.approved })
        return render_to_response('admin/full_profile.html',
                                        {'observer': obs,
                                         'form': form },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")
        


@login_required
def do_observers_list(request):
    user = request.user
    if (user.is_staff):
        obs = RegisteredObserver.objects
        return render_to_response('admin/observers_list.html',
                                        {'observers': obs },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")
        

@login_required
def do_observations_list(request):
    user = request.user
    if (user.is_staff):
        obs = Observation.objects
        return render_to_response('admin/observations_list.html',
                                        {'observations': obs },
                                        context_instance=RequestContext(request))
    else:
        return redirect_home_with_message(request, "You need to be staff to do that.")