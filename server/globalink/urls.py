from django.conf.urls.defaults import patterns, include, url
from observation.resources import ObservationResource
from tastypie.api import Api

v1_api = Api(api_name='v1')
v1_api.register(ObservationResource())


urlpatterns = patterns('globalink.observation.views',
    url(r'^$', 'home'),
    url(r'^translators', 'translators'),
    url(r'^contributors', 'contributors'),
    url(r'^api/$', 'api_info'),
    (r'^api/', include(v1_api.urls)),
    url(r'^feedback/send', 'feedback'),
)

urlpatterns += patterns('',
    url(r'^observation/', include('globalink.observation.urls')),
    url(r'^observer/', include('globalink.observer.urls')),
    url(r'^admin/', include('globalink.admin.urls')),
    
    url(r'^i18n/', include('django.conf.urls.i18n')),
)