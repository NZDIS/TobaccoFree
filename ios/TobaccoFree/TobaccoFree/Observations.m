//
//  Observations.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 5/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <CommonCrypto/CommonDigest.h>

#import "Observations.h"
#import "Details.h"


@implementation Observations

@dynamic latitude;
@dynamic longitude;
@dynamic timestamp_start;
@dynamic timestamp_stop;
@dynamic uploaded;
@dynamic details;



/*!
 Returns the number of observed cars.
 */
- (int) countCars {
    return [self.details count];
}


/*!
 @brief Count number of records of particular smoking type.
 @param smoking type constant
 @return number of instances of particular smoking type.
 */
- (int) countSmokingType:(int)type {
    int count = 0;
    NSEnumerator *e = [self.details objectEnumerator];
    Details *obs;
    while ((obs = [e nextObject])) {
        if (obs.type == type) count++;
    }    
    return count;    
}


- (int) noSmoking {
    return [self countSmokingType:SMOKING_ID_NO_SMOKING];
}

/*!
 @brief Smoking with sole driver in this observation set.
 @return number of instances of smoking by sole adult in the car.
 */
- (int) smokingLoneAdult {
    return [self countSmokingType:SMOKING_ID_ADULT_SMOKING_ALONE];
}

/*!
 @brief Smoking with other adults in this observation set.
 @return number of instances of smoking with other adults in the car.
 */
- (int) smokingAdultOthers {
    return [self countSmokingType:SMOKING_ID_ADULT_SMOKING_OTHERS];
}

/*!
 @brief Smoking with child in this observation set.
 @return number of instances of smoking with child in the car.
 */
- (int) smokingChild {
    return [self countSmokingType:SMOKING_ID_ADULT_SMOKING_CHILD];
}


/*!
 Prepare the Json for this observation record.
 */
- (NSMutableDictionary *) toDictionary {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    
    [dict setValue:[NSNumber numberWithInt:CURRENT_PROTOCOL_VERSION] forKey:OBSERVATION_PROTOCOL_VERSION];
    [dict setValue:[NSNumber numberWithDouble:self.longitude] forKey:OBSERVATION_LONGITUDE];
    [dict setValue:[NSNumber numberWithDouble:self.latitude] forKey:OBSERVATION_LATITUDE];
    [dict setValue:[NSNumber numberWithUnsignedInt:self.timestamp_start] forKey:OBSERVATION_START];
    [dict setValue:[NSNumber numberWithUnsignedInt:self.timestamp_stop] forKey:OBSERVATION_FINISH];
    [dict setValue:[self observationHash] forKey:OBSERVATION_HASH];
    [dict setValue:[NSNumber numberWithUnsignedInt:[self noSmoking]] forKey:OBSERVATION_NO_SMOKING];
    [dict setValue:[NSNumber numberWithUnsignedInt:[self smokingLoneAdult]] forKey:OBSERVATION_LONE_ADULT];
    [dict setValue:[NSNumber numberWithUnsignedInt:[self smokingAdultOthers]] forKey:OBSERVATION_OTHER_ADULTS];
    [dict setValue:[NSNumber numberWithUnsignedInt:[self smokingChild]] forKey:OBSERVATION_CHILD];
    // Prepare Details for Json serialization
    NSMutableArray *a = [[NSMutableArray alloc] init];
    for (Details *d in self.details) { 
        [a addObject:d.toDictionary];
    }
    [dict setValue:a forKey:OBSERVATION_DETAILS];

    return dict;
}


/*!
 @brief Unique hash for this observation instance
 
 Creates a hash of all of the values stored in this instance. 
 Should be considered unique. Used for verification of recording 
 and syncing between phone and server data.
 
 @return The hash that should be considered unique for this observation instance
 
 */
- (NSString *) observationHash {
    /*  ANDROID code
     final MessageDigest md = MessageDigest.getInstance("MD5");
     md.reset();
     final String hashString = location.getLatitude() + location.getLongitude() 
     + "salt" + finish + start 
     + noSmoking + otherAdults + loneAdult + child;
     
     md.update(hashString.getBytes());
     final byte hash[] = md.digest();
     
     StringBuffer hex = new StringBuffer();
     for(int i = 0; i < hash.length; i++) {
     hex.append(Integer.toHexString(0xFF & hash[i]));
     }
     return hex.toString();
     */
    NSMutableString *input = [[NSMutableString alloc] initWithFormat:@"%f%fsalt%d%d%d%d%d%d", 
                              self.latitude, self.longitude, self.timestamp_stop, self.timestamp_start,
                              [self noSmoking], [self smokingLoneAdult], [self smokingAdultOthers], [self smokingChild]];
    const char* str = [input UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(str, strlen(str), result);
    
    NSMutableString *ret = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH*2];
    for(int i = 0; i<CC_MD5_DIGEST_LENGTH; i++) {
        [ret appendFormat:@"%02x",result[i]];
    }
    return ret;
}

@end
