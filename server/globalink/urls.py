from django.conf.urls.defaults import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^$', 'globalink.observation.views.home'),
    url(r'^observer/register', 'globalink.observation.views.register'),
    url(r'^observer/confirm', 'globalink.observation.views.register_confirm'),
    url(r'^observation/', include('globalink.observation.urls')),
    url(r'^feedback/send', 'globalink.observation.views.feedback'),
    url(r'^admin/', include(admin.site.urls)),
)
