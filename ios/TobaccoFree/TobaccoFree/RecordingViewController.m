//
//  RecordingViewControllerViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "RecordingViewController.h"
#import <QuartzCore/CAAnimation.h>

@implementation RecordingViewController


@synthesize locationManager;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


#pragma mark - Location management

- (IBAction)finishRecording:(id)sender {
    
    
    
    // Go back to the main screen
    [self.navigationController popViewControllerAnimated:YES];
}

- (CLLocationManager *)locationManager {
    
    if (locationManager != nil) {
        return locationManager;
    }
    
    locationManager = [[CLLocationManager alloc] init];
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    locationManager.delegate = self;
    
    return locationManager;
}

- (void)locationManager:(CLLocationManager *)manager
    didUpdateToLocation:(CLLocation *)newLocation
           fromLocation:(CLLocation *)oldLocation {
    // TODO implement the location update
}

- (void)locationManager:(CLLocationManager *)manager
       didFailWithError:(NSError *)error {
    
}



#pragma mark - View Lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Start the location manager.
    [[self locationManager] startUpdatingLocation];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    self.locationManager = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
     
    double currentTime = CACurrentMediaTime(); 
    long ct = (long)(currentTime * 1000);
    
    // Hide the back button
    self.navigationItem.hidesBackButton = YES;
    
    NSLog(@"Got timestamp: %d", ct);
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Info"
                                                    message:[@"View has loaded. Ready to record data. Time stamp:" stringByAppendingString:[NSString stringWithFormat:@"%d", ct]]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
    
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait || 
            interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown);
}


@end
