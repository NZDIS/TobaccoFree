
from django import forms
from django.utils.translation import ugettext as _


class FullProfileForm(forms.Form):
    '''
    Observer's full profile
    '''
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge uneditable-input'}))
    email.help_text = _("Your email/Username. Cannot be changed.")
    name = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    name.help_text = _("Name")
    surname = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    surname.help_text = _("Surname")
    affiliation = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    affiliation.help_text = _("Affiliation")
    activation_key = forms.CharField(max_length=64, widget=forms.TextInput(attrs={'class':'xlarge'}))
    activation_key.help_text = _("Activation key")
    is_active = forms.BooleanField(required=False)
    is_active.help_text = _("Is active?")
    is_staff = forms.BooleanField(required=False)
    is_staff.help_text = _("Staff?")
    approved = forms.BooleanField(required=False)
    approved.help_text = _("Approved?")
    