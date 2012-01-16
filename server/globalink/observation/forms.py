'''
Created on Jan 12, 2012

@author: mariusz
'''


from django import forms


class FeedbackForm(forms.Form):
    email = forms.EmailField(widget=forms.TextInput(attrs={'class':'xlarge'}))
    email.help_text="Your email"
    subject = forms.CharField(max_length=256, widget=forms.TextInput(attrs={'class':'xlarge'}))
    subject.help_text="Topic, main subject for your feedback"
    description = forms.CharField(max_length=3000, widget=forms.Textarea(attrs={'class':'xxlarge', 'rows':'4'}))


