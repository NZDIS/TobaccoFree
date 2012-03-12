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

@synthesize observation;
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



#pragma mark - Utilities

- (int) numOfObservedCars {
    return self.count_no_smoking +
            self.count_sole_adult +
            self.count_other_adults +
            self.count_child;
}

/*! 
 Prepares recording of data. Used upon first car observation.
 */
- (void) prepareRecording {
    if ([self numOfObservedCars] == 0) 
    {
        if (observation == nil) // we check again, because perhaps user added and removed a car, but observation has been initialised already
        {
            // create new observation record
            observation = (Observations *)[NSEntityDescription insertNewObjectForEntityForName:@"Observations" inManagedObjectContext:managedObjectContext];
            // currentTimeInMillis
            // long ct = (long)(CACurrentMediaTime() * 1000);
            NSTimeInterval ct = [[NSDate date] timeIntervalSince1970];
            observation.timestamp_start = ct;
        }
    }
}


/*!
 @brief Returns the last Detail of a given type
 */
- (Details *) getLastDetailForType:(int)type {
    NSEnumerator *e = [observation.details objectEnumerator];
    Details *obs, *result_obs = nil;
    while ((obs = [e nextObject])) {
        if (obs.type == type && obs.timestamp > result_obs.timestamp) 
        {
            result_obs = obs;
        }
    }
    return result_obs;
}

/*!
 @brief Prepares a persistant storage for a Detail of a given type.
 */
- (Details *) recordDetails:(int) type {
    // currentTimeInMillis
    // long ct = (long)(CACurrentMediaTime() * 1000);
    NSDate *now = [NSDate date];
    
    // create a record
    Details *d = (Details *)[NSEntityDescription insertNewObjectForEntityForName:@"Details" inManagedObjectContext:managedObjectContext];
    d.timestamp = [now timeIntervalSince1970];
    NSLog(@"TIMESTAMP %d", d.timestamp);
    d.type = type;
    [self.observation addDetailsObject:d];
    
    return d;
}


#pragma mark - Location management

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
    CLLocationCoordinate2D loc = [newLocation coordinate];
    self.observation.latitude = loc.latitude;
    self.observation.longitude = loc.longitude;
}

- (void)locationManager:(CLLocationManager *)manager
       didFailWithError:(NSError *)error {
    
}



#pragma mark - Button press handling

- (void) clickSound {
    //Get the filename of the sound file:
	NSString *path = [NSString stringWithFormat:@"%@%@", [[NSBundle mainBundle] resourcePath], @"/click.wav"];
    
	//declare a system sound
	SystemSoundID soundID;
    
	//Get a URL for the sound file
	NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:NO];
    
	//Use audio sevices to create the sound
	AudioServicesCreateSystemSoundID((__bridge CFURLRef)filePath, &soundID);
	//Use audio services to play the sound
	AudioServicesPlaySystemSound(soundID);
}

- (IBAction)finishRecording:(id)sender {

    if ([self numOfObservedCars] > 0)
    {
        // record the finish time of the recording
        // long ct = (long)(CACurrentMediaTime() * 1000);
        NSTimeInterval ct = [[NSDate date] timeIntervalSince1970];
        observation.timestamp_stop = ct;

        NSError *error = nil;
        if (![managedObjectContext save:&error]) {
            NSLog(@"Error %@", error.localizedDescription);
        }

        // Debugging NSLog(@"Got timestamp: %ld", ct);
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Thanks you"
                                                    message:[@"You have observed " stringByAppendingString:[NSString stringWithFormat:@"%d cars", [self numOfObservedCars]]]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
        [alert show];
    }
    // Go back to the main screen
    [self.navigationController popViewControllerAnimated:YES];
}


- (IBAction)add_no_smoking:(id)sender {
    [self clickSound];
    [self prepareRecording];
    self.count_no_smoking++;
    self.txt_no_smoking.text = [NSString stringWithFormat:@"%d", self.count_no_smoking];
    [self recordDetails:SMOKING_ID_NO_SMOKING];
    
}

- (IBAction)substract_no_smoking:(UILongPressGestureRecognizer *)sender {
    if (sender.state == UIGestureRecognizerStateEnded) {
        // do nothing at the end
    } else {
        if (count_no_smoking > 0) 
        {
            Details *d = [self getLastDetailForType:SMOKING_ID_NO_SMOKING];
            [observation removeDetailsObject:d];
            [managedObjectContext deleteObject:d];
            
            self.count_no_smoking--;
            self.txt_no_smoking.text = [NSString stringWithFormat:@"%d", self.count_no_smoking];
        }
    }
}

- (IBAction)add_sole_adult:(id)sender {
    [self clickSound];
    [self prepareRecording];
    self.count_sole_adult++;
    self.txt_sole_adult.text = [NSString stringWithFormat:@"%d", self.count_sole_adult];
    [self recordDetails:SMOKING_ID_ADULT_SMOKING_ALONE];
}

- (IBAction)substract_sole_adult:(UILongPressGestureRecognizer *)sender {
    if (sender.state == UIGestureRecognizerStateEnded) {
        // do nothing at the end
    } else {
        if (count_sole_adult > 0) 
        {
            Details *d = [self getLastDetailForType:SMOKING_ID_ADULT_SMOKING_ALONE];
            [observation removeDetailsObject:d];
            [managedObjectContext deleteObject:d];
            
            self.count_sole_adult--;
            self.txt_sole_adult.text = [NSString stringWithFormat:@"%d", self.count_sole_adult];
        }
    }
}

- (IBAction)add_other_adults:(id)sender {
    [self clickSound];
    [self prepareRecording];    
    self.count_other_adults++;
    self.txt_other_adults.text = [NSString stringWithFormat:@"%d", self.count_other_adults];
    [self recordDetails:SMOKING_ID_ADULT_SMOKING_OTHERS];
}

- (IBAction)substract_other_adults:(UILongPressGestureRecognizer *)sender {
    if (sender.state == UIGestureRecognizerStateEnded) {
        // do nothing at the end
    } else {
        if (count_other_adults > 0) 
        {
            Details *d = [self getLastDetailForType:SMOKING_ID_ADULT_SMOKING_OTHERS];
            [observation removeDetailsObject:d];
            [managedObjectContext deleteObject:d];
            
            self.count_other_adults--;
            self.txt_other_adults.text = [NSString stringWithFormat:@"%d", self.count_other_adults];
        }
    }
}

- (IBAction)add_child:(id)sender {
    [self clickSound];
    [self prepareRecording];    
    self.count_child++;
    self.txt_child.text = [NSString stringWithFormat:@"%d", self.count_child];
    [self recordDetails:SMOKING_ID_ADULT_SMOKING_CHILD];
}

- (IBAction)substract_child:(UILongPressGestureRecognizer *)sender {
    if (sender.state == UIGestureRecognizerStateEnded) {
        // do nothing at the end
    } else {
        if (count_child > 0) 
        {
            Details *d = [self getLastDetailForType:SMOKING_ID_ADULT_SMOKING_CHILD];
            [observation removeDetailsObject:d];
            [managedObjectContext deleteObject:d];
            
            self.count_child--;
            self.txt_child.text = [NSString stringWithFormat:@"%d", self.count_child];
        }
    }
}




#pragma mark - View Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Hide the back button
    self.navigationItem.hidesBackButton = YES;

    // Start the location manager.
    [[self locationManager] startUpdatingLocation];
    
    if (managedObjectContext == nil)
    { 
        managedObjectContext = [(NGAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext]; 
    }
    
    // Debugging NSLog(@"Loaded Data Recording view");
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
     
    /*
    double currentTime = CACurrentMediaTime(); 
    long ct = (long)(currentTime * 1000);
    
    // Hide the back button
    self.navigationItem.hidesBackButton = YES;
    
    NSLog(@"Got timestamp: %ld", ct);
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Info"
                                                    message:[@"View has loaded. Ready to record data. Time stamp:" stringByAppendingString:[NSString stringWithFormat:@"%d", ct]]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
    */
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
