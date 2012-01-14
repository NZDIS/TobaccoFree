from settings import *


# PRODUCTION settings

DEBUG = False
TEMPLATE_DEBUG = DEBUG

STATIC_ROOT = "/home/www/public_html/static/"


# But, for Sessions we need to have a DB backend. We use sqlite at the moment
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3', # Add 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': '/home/www/db/globalink.sqlite',  
        'USER': '',                      # Not used with sqlite3.
        'PASSWORD': '',                  # Not used with sqlite3.
        'HOST': '',                      # Set to empty string for localhost. Not used with sqlite3.
        'PORT': '',                      # Set to empty string for default. Not used with sqlite3.
    }
}

