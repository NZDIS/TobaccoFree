'''
Created on Dec 19, 2011

@author: Mariusz Nowostawski <mariusz@nowostawski.org>
'''



from django.http import HttpResponse

def home(request):
    return HttpResponse("Place holder for the HOME PAGE.")