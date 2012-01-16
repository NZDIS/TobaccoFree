
from django import forms
from django.core.exceptions import ObjectDoesNotExist
from django.contrib.auth.models import check_password
from globalink.observation.models import RegisteredObserver
from mongoengine.django.auth import User

import re


class RegistrationForm(forms.Form):
    '''
    New observers registration form
    '''
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge'}))
    email.help_text="Your email"
    name = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    name.help_text="Real name"
    surname = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    surname.help_text="Real surname"
    affiliation = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    affiliation.help_text="Your affiliation"
    password = forms.CharField(widget=forms.PasswordInput(attrs={'class':'xlarge'}))
    password.help_text = 'Your password (at least 6 characters)'


    def clean_email(self):
        '''
        Validate that the supplied email address is unique.
        '''
        if User.objects.filter(email__iexact=self.cleaned_data['email']):
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



class ProfileForm(forms.Form):
    '''
    Observer's profile
    '''
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge uneditable-input'}))
    email.help_text="Your email/Username. Cannot be changed."
    name = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    name.help_text="Name"
    surname = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    surname.help_text="Surname"
    affiliation = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    affiliation.help_text="Affiliation"
    
    
    
class PasswordChangeForm(forms.Form):
    '''
    Observer's password change form
    '''
    user = None
    old_password = forms.CharField(widget=forms.PasswordInput(attrs={'class':'xlarge'}))
    old_password.help_text = 'Existing password'
    new_password = forms.CharField(widget=forms.PasswordInput(attrs={'class':'xlarge'}))
    new_password.help_text = 'New password (at last 6 characters)'

    def __init__(self, user, *args, **kwargs):
        self.user = user;
        return super(PasswordChangeForm, self).__init__(*args, **kwargs)
        
    def clean_old_password(self):                                                             
        '''                                                                            
        Validate that the supplied old password is correct                            
        '''                                               
                                     
        if not check_password(self.cleaned_data['old_password'], self.user.password):
            raise forms.ValidationError('Password incorrect')              
        return self.cleaned_data['old_password']


    def clean_new_password(self):                                                             
        '''                                                                            
        Validate that the supplied password is at least 8 characters                            
        '''                                                                            
        if len(self.cleaned_data['new_password']) < 6:
            raise forms.ValidationError('Password too short')              
        return self.cleaned_data['new_password']
