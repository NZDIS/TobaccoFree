//
//  RecordingViewControllerViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "RecordingViewController.h"
#import "NGAppDelegate.h"
#import "Details.h"
#import <QuartzCore/CAAnimation.h>

@implementation RecordingViewController


@synthesize locationManager;
@synthesize managedObjectContext;

@synthesize count_no_smoking;
@synthesize txt_no_smoking;
@synthesize count_sole_adult;
@synthesize txt_sole_adult;
@synthesize count_other_adults;
@synthesize txt_other_adults;
@synthesize count_child;
@synthesize txt_child;


- (int) numOfObservedCars {
    return self.count_no_smoking +
            self.count_sole_adult +
            self.count_other_adults +
            self.count_child;
}


- (Details *) recordDetails:(int) type {
    // currentTimeInMillis
    long ct = (long)(CACurrentMediaTime() * 1000);
    
    // create a record
    Details *d = (Details *)[NSEntityDescription insertNewObjectForEntityForName:@"Details" inManagedObjectContext:managedObjectContext];
    d.timestamp = ct;
    d.type = type;
    NSError *error = nil;
    if (![managedObjectContext save:&error]) {
        NSLog(@"Error %@", error.localizedDescription);
    }
    return d;
}

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

- (IBAction)add_no_smoking:(id)sender {
    self.count_no_smoking++;
    self.txt_no_smoking.text = [NSString stringWithFormat:@"%d", self.count_no_smoking];
    [self recordDetails:SMOKING_ID_NO_SMOKING];
    
}

- (IBAction)add_sole_adult:(id)sender {
    self.count_sole_adult++;
    self.txt_sole_adult.text = [NSString stringWithFormat:@"%d", self.count_sole_adult];
    [self recordDetails:SMOKING_ID_ADULT_SMOKING_ALONE];
}

- (IBAction)add_other_adults:(id)sender {
    self.count_other_adults++;
    self.txt_other_adults.text = [NSString stringWithFormat:@"%d", self.count_other_adults];
    [self recordDetails:SMOKING_ID_ADULT_SMOKING_OTHERS];
}

- (IBAction)add_child:(id)sender {
    self.count_child++;
    self.txt_child.text = [NSString stringWithFormat:@"%d", self.count_child];
    [self recordDetails:SMOKING_ID_ADULT_SMOKING_CHILD];
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
    
    if (managedObjectContext == nil)
    { 
        managedObjectContext = [(NGAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext]; 
        NSLog(@"After managedObjectContext: %@",  managedObjectContext);
    }
}

- (void)viewDidUnload
{
    [self setTxt_no_smoking:nil];
    [self setTxt_sole_adult:nil];
    [self setTxt_other_adults:nil];
    [self setTxt_child:nil];
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
