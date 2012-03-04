//
//  RecordingViewControllerViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

#import "Observations.h"
#import "Details.h"

@interface RecordingViewController : UIViewController <CLLocationManagerDelegate> {
    
    int count_no_smoking;
    int count_sole_adult;
    int count_other_adults;
    int count_child;
    
    Observations *observation;
    
    CLLocationManager *locationManager;
    NSManagedObjectContext *managedObjectContext;
    
}

@property (nonatomic, retain) Observations *observation;
@property (nonatomic, retain) CLLocationManager *locationManager;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;

@property (nonatomic) int count_no_smoking;
@property (weak, nonatomic) IBOutlet UILabel *txt_no_smoking;

@property (nonatomic) int count_sole_adult;
@property (weak, nonatomic) IBOutlet UILabel *txt_sole_adult;

@property (nonatomic) int count_other_adults;
@property (weak, nonatomic) IBOutlet UILabel *txt_other_adults;

@property (nonatomic) int count_child;
@property (weak, nonatomic) IBOutlet UILabel *txt_child;

- (IBAction) finishRecording:(id) sender;

- (IBAction) add_no_smoking:(id) sender;
- (IBAction) add_sole_adult:(id) sender;
- (IBAction) add_other_adults:(id) sender;
- (IBAction) add_child:(id)sender;


- (int) numOfObservedCars;
- (Details *) recordDetails:(int) type;

@end
