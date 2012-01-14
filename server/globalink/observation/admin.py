'''
Created on Jan 12, 2012

@author: mariusz
'''

from globalink import settings

from django.contrib import admin
from globalink.observation.models import RegisteredObserver, Observation

class RegisteredObserverAdmin(admin.ModelAdmin):
    def save_model(self, request, obj, form, change):
        plain_password = request.POST['password_hash']
        obj.password_hash = settings.hash_password(plain_password)
        obj.save()

admin.site.register(Observation)
admin.site.register(RegisteredObserver, RegisteredObserverAdmin)