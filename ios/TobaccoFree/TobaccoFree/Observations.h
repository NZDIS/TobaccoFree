//
//  Observations.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 5/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Details;

@interface Observations : NSManagedObject

@property (nonatomic) double latitude;
@property (nonatomic) double longitude;
@property (nonatomic) uint32_t timestamp_start;
@property (nonatomic) uint32_t timestamp_stop;
@property (nonatomic) BOOL uploaded;
@property (nonatomic, retain) NSSet *details;


- (int) noSmoking;
- (int) smokingLoneAdult;
- (int) smokingAdultOthers;
- (int) smokingChild;

- (NSString *) observationHash;
- (NSMutableDictionary *) toDictionary;

@end



@interface Observations (CoreDataGeneratedAccessors)

- (void)addDetailsObject:(Details *)value;
- (void)removeDetailsObject:(Details *)value;
- (void)addDetails:(NSSet *)values;
- (void)removeDetails:(NSSet *)values;

@end
