
from tastypie import fields
from tastypie.resources import Resource
from tastypie.bundle import Bundle
from tastypie.exceptions import InvalidFilterError, BadRequest
from tastypie.constants import ALL, ALL_WITH_RELATIONS
from tastypie.utils.dict import dict_strip_unicode_keys
from tastypie.authentication import Authentication
from tastypie.authorization import ReadOnlyAuthorization as Authorization
 
from mongoengine.queryset import QuerySet

from django.db.models.sql.constants import LOOKUP_SEP, QUERY_TERMS

from models import Observation

class MongoResource(Resource):
    '''
    Wrapper around tastypie Resource to be used with Mongo. 
    '''

    def obj_get_list(self, request=None, **kwargs):
        # Filtering 
        filters = {}

        if hasattr(request, 'GET'):
            # Grab a mutable copy.
            filters = request.GET.copy()

        # Update with the provided kwargs.
        filters.update(kwargs)
        applicable_filters = self.build_filters(filters=filters)

        try:
            base_object_list = self.apply_filters(request, applicable_filters)
            return self.apply_authorization_limits(request, base_object_list)
        except ValueError:
            raise BadRequest("Invalid resource lookup data provided (mismatched type).")
    
    

    
    def get_resource_uri(self, bundle_or_obj):
        kwargs = {
            'resource_name': self._meta.resource_name,
        }

        if isinstance(bundle_or_obj, Bundle):
            kwargs['pk'] = bundle_or_obj.obj.id
        else:
            kwargs['pk'] = bundle_or_obj.id

        if self._meta.api_name is not None:
            kwargs['api_name'] = self._meta.api_name

        return self._build_reverse_url("api_dispatch_detail", kwargs=kwargs)
    
   
    
    def check_filtering(self, field_name, filter_type='exact', filter_bits=None):
        """
        Given a field name, a optional filter type and an optional list of
        additional relations, determine if a field can be filtered on.

        If a filter does not meet the needed conditions, it should raise an
        ``InvalidFilterError``.

        If the filter meets the conditions, a list of attribute names (not
        field names) will be returned.
        """
        if filter_bits is None:
            filter_bits = []

        if not field_name in self._meta.filtering:
            raise InvalidFilterError("The '%s' field does not allow filtering." % field_name)

        # Check to see if it's an allowed lookup type.
        if not self._meta.filtering[field_name] in (ALL, ALL_WITH_RELATIONS):
            # Must be an explicit whitelist.
            if not filter_type in self._meta.filtering[field_name]:
                raise InvalidFilterError("'%s' is not an allowed filter on the '%s' field." % (filter_type, field_name))

        if self.fields[field_name].attribute is None:
            raise InvalidFilterError("The '%s' field has no 'attribute' for searching with." % field_name)

        # Check to see if it's a relational lookup and if that's allowed.
        if len(filter_bits):
            if not getattr(self.fields[field_name], 'is_related', False):
                raise InvalidFilterError("The '%s' field does not support relations." % field_name)

            if not self._meta.filtering[field_name] == ALL_WITH_RELATIONS:
                raise InvalidFilterError("Lookups are not allowed more than one level deep on the '%s' field." % field_name)

            # Recursively descend through the remaining lookups in the filter,
            # if any. We should ensure that all along the way, we're allowed
            # to filter on that field by the related resource.
            related_resource = self.fields[field_name].get_related_resource(None)
            return [self.fields[field_name].attribute] + related_resource.check_filtering(filter_bits[0], filter_type, filter_bits[1:])

        return [self.fields[field_name].attribute]



    def build_filters(self, filters=None):
        """
        Given a dictionary of filters, create the necessary ORM-level filters.

        Keys should be resource fields, **NOT** model fields.

        Valid values are either a list of Django filter types (i.e.
        ``['startswith', 'exact', 'lte']``), the ``ALL`` constant or the
        ``ALL_WITH_RELATIONS`` constant.
        """
        # At the declarative level:
        #     filtering = {
        #         'resource_field_name': ['exact', 'startswith', 'endswith', 'contains'],
        #         'resource_field_name_2': ['exact', 'gt', 'gte', 'lt', 'lte', 'range'],
        #         'resource_field_name_3': ALL,
        #         'resource_field_name_4': ALL_WITH_RELATIONS,
        #         ...
        #     }
        # Accepts the filters as a dict. None by default, meaning no filters.
        if filters is None:
            filters = {}

        qs_filters = {}

        for filter_expr, value in filters.items():
            filter_bits = filter_expr.split(LOOKUP_SEP)
            field_name = filter_bits.pop(0)
            filter_type = 'exact'

            if not field_name in self.fields:
                # It's not a field we know about. Move along citizen.
                continue

            if len(filter_bits) and filter_bits[-1] in QUERY_TERMS.keys():
                filter_type = filter_bits.pop()

            lookup_bits = self.check_filtering(field_name, filter_type, filter_bits)

            if value in ['true', 'True', True]:
                value = True
            elif value in ['false', 'False', False]:
                value = False
            elif value in ('nil', 'none', 'None', None):
                value = None

            # Split on ',' if not empty string and either an in or range filter.
            if filter_type in ('in', 'range') and len(value):
                if hasattr(filters, 'getlist'):
                    value = filters.getlist(filter_expr)
                else:
                    value = value.split(',')

            db_field_name = LOOKUP_SEP.join(lookup_bits)
            qs_filter = "%s%s%s" % (db_field_name, LOOKUP_SEP, filter_type)
            qs_filters[qs_filter] = value

        return dict_strip_unicode_keys(qs_filters)
    
    
    def apply_filters(self, request, applicable_filters):
        """
        An ORM-specific implementation of ``apply_filters``.

        The default simply applies the ``applicable_filters`` as ``**kwargs``,
        but should make it possible to do more advanced things.
        """
        return self.get_object_list(request).filter(**applicable_filters)
    


    def obj_update(self, bundle, request=None, **kwargs):
        return self.obj_create(bundle, request, **kwargs)
   


    def rollback(self, bundles):
        pass
    
    

class EmbeddedObject(object):
    '''
    We need a generic embedded object to shove data in/get data from.
    Mongo generally just tosses around dictionaries, so we'll lightly
    wrap that.
    '''
    def __init__(self, initial=None):
        self.__dict__['_data'] = {}

        if hasattr(initial, 'items'):
            self.__dict__['_data'] = initial

    def __getattr__(self, name):
        return self._data.get(name, None)

    def __setattr__(self, name, value):
        self.__dict__['_data'][name] = value

    def to_dict(self):
        return self._data





import tastypie



class ObservationResource(MongoResource):

    #author = fields.ApiField(attribute='author')

    city = fields.CharField(attribute='loc_city', null=True)
    country = fields.CharField(attribute='loc_country', null=True)
    start = fields.DateField(attribute='start', null=True)
    finish = fields.DateField(attribute='finish', null=True)
    duration = fields.IntegerField(attribute='duration', null=True)
    no_smoking = fields.IntegerField(attribute='no_smoking', null=True)
    lone_adult = fields.IntegerField(attribute='lone_adult', null=True)
    other_adults = fields.IntegerField(attribute='other_adults', null=True)
    child = fields.IntegerField(attribute='child', null=True)
    longitude = fields.FloatField(attribute='longitude')
    latitude = fields.FloatField(attribute='latitude')
    
    class Meta:
        resource_name = 'observation'
        authorization = Authorization()
        authentication = Authentication()
        filtering = {
                     'no_smoking': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'lone_adult': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'other_adults': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'child': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'start': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'finish': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'duration': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'city' : ['exact', 'startswith', 'contains', 'endswith'],
                     'country' : ['exact', 'startswith', 'contains', 'endswith'],
                     'latitude': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     'longitude': ['exact', 'gt', 'gte', 'lt', 'lte'],
                     }


    def get_object_list(self, request):
        return Observation.objects



    def obj_get(self, request=None, **kwargs):
        print kwargs
        pk = kwargs['pk']
        obs = Observation.objects.get(id=pk)
        return obs
    
    
        




