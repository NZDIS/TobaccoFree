from django.conf.urls.defaults import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('globalink.observation.views',
    url(r'^$', 'home'),
    url(r'^observer/register', 'register'),
    url(r'^observer/confirm', 'register_confirm'),
    url(r'^feedback/send', 'feedback'),
)

urlpatterns += patterns('',
    url(r'^observation/', include('globalink.observation.urls')),
    url(r'^admin/', include(admin.site.urls)),
)