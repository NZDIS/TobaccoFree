//
//  ObservationsDataCell.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 12/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ObservationsDataCell : UITableViewCell

@property (nonatomic, strong) IBOutlet UILabel *label_num_cars;
@property (nonatomic, strong) IBOutlet UILabel *label_date;

@property (nonatomic, strong) IBOutlet UILabel *label_no_smoking;
@property (nonatomic, strong) IBOutlet UILabel *label_lone_adult;
@property (nonatomic, strong) IBOutlet UILabel *label_other_adults;
@property (nonatomic, strong) IBOutlet UILabel *label_child;

@property (nonatomic, strong) IBOutlet UILabel *label_not_uploaded;

@end
