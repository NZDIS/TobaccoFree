from django.conf.urls.defaults import patterns, include, url

urlpatterns = patterns('globalink.observation.views',
    url(r'^$', 'home'),
    url(r'^feedback/send', 'feedback'),
)

urlpatterns += patterns('',
    url(r'^observation/', include('globalink.observation.urls')),
    url(r'^observer/', include('globalink.observer.urls')),
)