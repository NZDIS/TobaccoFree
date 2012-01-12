'''
Created on Jan 12, 2012

@author: mariusz
'''
import hashlib
from globalink import settings

from django.contrib import admin
from globalink.observation.models import GlobalinkUser, Observation

class GlobalinkUserAdmin(admin.ModelAdmin):
    def save_model(self, request, obj, form, change):
        plain_password = request.POST['password_hash']
        m = hashlib.md5()
        m.update(settings.HASH_SALT_PRE)
        m.update(plain_password)
        m.update(settings.HASH_SALT_POST)
        hashed_password = str(m.hexdigest())
        obj.password_hash = hashed_password
        obj.save()

admin.site.register(Observation)
admin.site.register(GlobalinkUser, GlobalinkUserAdmin)