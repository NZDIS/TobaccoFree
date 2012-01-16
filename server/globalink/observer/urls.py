from django.conf.urls.defaults import patterns, url

urlpatterns = patterns('globalink.observer.views',
    url(r'^register', 'register'),
    url(r'^login', 'dologin'),
    url(r'^logout', 'dologout'),
    url(r'^confirm', 'register_confirm'),
    url(r'^profile', 'profile'),
    url(r'^password_change', 'password_change'),
)

