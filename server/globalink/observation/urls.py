from django.conf.urls.defaults import patterns, url

urlpatterns = patterns('globalink.observation.views',
    url(r'^$', 'home'),
    url(r'^add', 'add'),
    url(r'^all', 'all_latlng'),
    url(r'^list', 'do_list'),
    url(r'^geocode', 'do_geocode_data'),
)
