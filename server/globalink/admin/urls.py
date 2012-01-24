from django.conf.urls.defaults import patterns, url

urlpatterns = patterns('globalink.admin.views',
    url(r'^profile_delete', 'do_profile_delete'),
    url(r'^profile', 'do_full_profile'),
    url(r'^list_observers', 'do_observers_list'),
    url(r'^list_observations', 'do_observations_list'),
    url(r'^observation_delete', 'do_observation_delete'),
)

