//
//  ObservationsDataCell.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 12/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "ObservationsDataCell.h"

@implementation ObservationsDataCell

@synthesize label_date;
@synthesize label_num_cars;
@synthesize label_no_smoking;
@synthesize label_lone_adult;
@synthesize label_other_adults;
@synthesize label_child;
@synthesize label_not_uploaded;


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
