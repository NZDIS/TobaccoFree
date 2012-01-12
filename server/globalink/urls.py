from django.conf.urls.defaults import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^$', 'globalink.observation.views.home'),
    url(r'^signup', 'globalink.observation.views.signup'),
    url(r'^observation/', include('globalink.observation.urls')),
    url(r'^admin/', include(admin.site.urls)),
)
