from django.conf.urls.defaults import patterns, url

urlpatterns = patterns('globalink.observation.views',
    url(r'^$', 'home'),
    url(r'^add', 'add'),
    url(r'^list', 'do_list'),
)
