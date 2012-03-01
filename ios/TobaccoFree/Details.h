//
//  Details.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Details : NSManagedObject

@property (nonatomic) int32_t details_id;
@property (nonatomic) int16_t type;
@property (nonatomic) int32_t timestamp;

@end
