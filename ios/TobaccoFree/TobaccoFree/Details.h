//
//  Details.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Observations;

@interface Details : NSManagedObject

@property (nonatomic) uint16_t type;
@property (nonatomic) uint32_t timestamp;
@property (nonatomic, retain) Observations *observation;

- (NSDictionary *) toDictionary;

@end
