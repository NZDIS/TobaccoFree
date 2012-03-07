//
//  Details.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "Details.h"
#import "Observations.h"

@implementation Details

@dynamic type;
@dynamic timestamp;
@dynamic observation;

/*!
 Returns a dictionary representation of this object.
 */
- (NSDictionary *) toDictionary {
    NSMutableDictionary * dict = [[NSMutableDictionary alloc] init];
    [dict setValue:[NSNumber numberWithInt:self.type] forKey:DETAILS_TYPE];
    [dict setValue:[NSNumber numberWithLong:self.timestamp] forKey:DETAILS_TIMESTAMP];
    return dict;
}

@end
