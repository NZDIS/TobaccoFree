
from django import forms



class FullProfileForm(forms.Form):
    '''
    Observer's full profile
    '''
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge uneditable-input'}))
    email.help_text="Your email/Username. Cannot be changed."
    name = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    name.help_text="Name"
    surname = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    surname.help_text="Surname"
    affiliation = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    affiliation.help_text="Affiliation"
    activation_key = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    activation_key.help_text="Activation key"
    is_staff = forms.BooleanField()
    is_staff.help_text = "Staff?"
    approved = forms.BooleanField()
    approved.help_text = "Approved?"
    