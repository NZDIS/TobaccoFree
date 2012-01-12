from django.conf.urls.defaults import patterns, include, url

urlpatterns = patterns('globalink.observation.views',
    url(r'^$', 'home'),
    url(r'^add', 'add'),
)
