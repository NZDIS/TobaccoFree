//
//  RecordingViewControllerViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

@interface RecordingViewController : UIViewController <CLLocationManagerDelegate> {
    
    CLLocationManager *locationManager;
    
}

@property (nonatomic, retain) CLLocationManager *locationManager;


- (IBAction)finishRecording:(id)sender;

@end
