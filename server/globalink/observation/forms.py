
from django import forms
from django.core.exceptions import ObjectDoesNotExist
from observation.models import RegisteredObserver

import re


class FeedbackForm(forms.Form):
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge'}))
    email.help_text="Your email"
    subject = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    subject.help_text="Topic, main subject for your feedback"
    description = forms.CharField(max_length=3000, widget=forms.Textarea(attrs={'class':'xxlarge', 'rows':'4'}))




class RegistrationForm(forms.Form):
    '''
    New observers registration form
    '''
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge'}))
    email.help_text="Your email"
    name = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    name.help_text="Topic, main subject for your feedback"
    surname = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    surname.help_text="Topic, main subject for your feedback"
    affiliation = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    affiliation.help_text="Topic, main subject for your feedback"
    password = forms.CharField(widget=forms.PasswordInput(attrs={'class':'xlarge'}))
    password.help_text = 'Your password (at least 6 characters)'


    def clean_email(self):
        '''
        Validate that the supplied email address is unique.
        '''
        if RegisteredObserver.objects.filter(email__iexact=self.cleaned_data['email']):
            raise forms.ValidationError('This e-mail is already in use.')
        return self.cleaned_data['email']

    def clean_password(self):                                                                     '''                                                                                    Validate that the supplied password is at least 8 characters                                    '''                                                                                    if len(self.cleaned_data['password']) < 6:            raise forms.ValidationError('Password too short')                      return self.cleaned_data['password']
    


SHA1_RE = re.compile('^[a-f0-9]{40}$')

class RegistrationConfirmationForm(forms.Form):
    '''
    New observers registration confirmation form
    '''
    activation_key = forms.CharField(max_length=40, widget=forms.TextInput(attrs={'class':'xlarge'}))

    def clean_activation_key(self):
        '''
        Validates if the key is SHA1
        '''
        data = self.cleaned_data['activation_key']
        if not SHA1_RE.search(data):
            raise forms.ValidationError('This activation key is corrupt.')
        try:                                                               
            RegisteredObserver.objects.get(activation_key=data) 
        except ObjectDoesNotExist:                                    
            raise forms.ValidationError('This activation key is wrong.')
        return self.cleaned_data['activation_key']

