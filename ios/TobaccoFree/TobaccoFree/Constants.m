//
//  Constants.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 4/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "Constants.h"

/* For local testing on home LAN */ 
NSString * const URL_OBSERVATION_ADD;
NSString * const HASH_SALT_PRE;
NSString * const HASH_SALT_POST;
/**/



// Details constants
int const SMOKING_ID_NO_SMOKING = 1;
int const SMOKING_ID_ADULT_SMOKING_ALONE = 2;
int const SMOKING_ID_ADULT_SMOKING_OTHERS = 3;
int const SMOKING_ID_ADULT_SMOKING_CHILD = 4;


// 1 - broken indexes, incomplete data
// 2 - all fine, but only aggregated data
// 3 - all fine, aggregated data together with details
int const CURRENT_PROTOCOL_VERSION = 3;


// constants for the SQLite datastore
NSString * const DATA_MODEL_NAME = @"ObservationsModel";
NSString * const DATA_MODEL_FILENAME = @"ObservationsModel.sqlite";


// DB schema related constants, used for SQLite and JSON
NSString * const DETAILS_TYPE = @"type";
NSString * const DETAILS_TIMESTAMP = @"timestamp";

NSString * const OBSERVATION_NO_SMOKING = @"no_smoking";
NSString * const OBSERVATION_LONE_ADULT = @"lone_adult";
NSString * const OBSERVATION_OTHER_ADULTS = @"other_adults";
NSString * const OBSERVATION_CHILD = @"child";
NSString * const OBSERVATION_DETAILS = @"details";
NSString * const OBSERVATION_LATITUDE = @"latitude";
NSString * const OBSERVATION_LONGITUDE = @"longitude";
NSString * const OBSERVATION_START = @"start_time";
NSString * const OBSERVATION_FINISH = @"finish_time";
NSString * const OBSERVATION_HASH = @"hash";
NSString * const OBSERVATION_UPLOADED = @"uploaded";

NSString * const OBSERVATION_PROTOCOL_VERSION = @"version";

NSString * const USER_DEVICE = @"device";
NSString * const USER_DEVICE_TYPE = @"device_type";
NSString * const USER_USER_EMAIL = @"user_email";
NSString * const USER_PASSWORD_HASH = @"pass_hash";