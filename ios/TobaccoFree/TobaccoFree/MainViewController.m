//
//  NGFirstViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "SBJson.h"
#import "MainViewController.h"
#import "NGAppDelegate.h"
#import "Observations.h"


@implementation MainViewController


@synthesize btnUloadData;

@synthesize observationsForUpload;
@synthesize managedObjectContext;



#pragma mark - Utilities

- (void) toggleUploadButton 
{
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Observations" inManagedObjectContext:managedObjectContext];
    [request setEntity:entity];
    
    // setup the predicate
    NSPredicate *testForNotUploaded = [NSPredicate predicateWithFormat:@"uploaded == NO"];
    [request setPredicate:testForNotUploaded];
    
    NSError *error = nil;
    observationsForUpload = [[managedObjectContext executeFetchRequest:request error:&error] mutableCopy];
    if (observationsForUpload == nil) {
        // Handle the error.
        NSLog(@"Error executing the query %@", error);
    }

    if ([observationsForUpload count] > 0 ) {
        [self.btnUloadData setEnabled:YES];
    } else {
        [self.btnUloadData setEnabled:NO];
    }
}

/*!
 setup the credentials in the Json structure.
 */
- (void) setUserCredentials:(NSMutableDictionary *)dict {
    NSString *errorDesc = nil;
    NSPropertyListFormat format;
    NSString *plistPath;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                              NSUserDomainMask, YES) objectAtIndex:0];
    plistPath = [rootPath stringByAppendingPathComponent:@"User.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
        plistPath = [[NSBundle mainBundle] pathForResource:@"User" ofType:@"plist"];
    }
    NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
    NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
                                          propertyListFromData:plistXML
                                          mutabilityOption:NSPropertyListMutableContainersAndLeaves
                                          format:&format
                                          errorDescription:&errorDesc];
    if (!temp) {
        NSLog(@"Error reading plist: %@, format: %d", errorDesc, format);
    }
    [dict setValue:[temp objectForKey:@"user_email"] forKey:USER_USER_EMAIL];
    [dict setValue:[temp objectForKey:@"user_password_hash"] forKey:USER_PASSWORD_HASH];
}

/*!
 Prepare the Json for a given observation record
 */
- (NSDictionary *) prepareDictionary:(Observations *)item {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    
    NSLog(@"%@", [[UIDevice currentDevice] uniqueIdentifier]);
    
    [dict setValue:[NSNumber numberWithInt:CURRENT_PROTOCOL_VERSION] forKey:OBSERVATION_PROTOCOL_VERSION];
    [dict setValue:[NSNumber numberWithDouble:item.longitude] forKey:OBSERVATION_LONGITUDE];
    [dict setValue:[NSNumber numberWithDouble:item.latitude] forKey:OBSERVATION_LATITUDE];
    [dict setValue:[NSNumber numberWithUnsignedInt:item.timestamp_start] forKey:OBSERVATION_START];
    [dict setValue:[NSNumber numberWithUnsignedInt:item.timestamp_stop] forKey:OBSERVATION_FINISH];
    [dict setValue:[item observationHash] forKey:OBSERVATION_HASH];
    [dict setValue:[NSNumber numberWithUnsignedInt:[item noSmoking]] forKey:OBSERVATION_NO_SMOKING];
    [dict setValue:[NSNumber numberWithUnsignedInt:[item smokingLoneAdult]] forKey:OBSERVATION_LONE_ADULT];
    [dict setValue:[NSNumber numberWithUnsignedInt:[item smokingAdultOthers]] forKey:OBSERVATION_OTHER_ADULTS];
    [dict setValue:[NSNumber numberWithUnsignedInt:[item smokingChild]] forKey:OBSERVATION_CHILD];
    [self setUserCredentials:dict];
    return dict;
}
    

- (IBAction)uploadData:(id)sender {
    for (Observations *o in observationsForUpload) {
        SBJsonWriter *writer = [[SBJsonWriter alloc] init];
        // prepare Dictionary first.
        
        NSDictionary *data = [self prepareDictionary:o];
        NSString *json = [writer stringWithObject:data];
        NSLog(@"got observations: %@", [data description]);
        NSLog(@"Got json: %@", json);
    }
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // setup managedObjectContext
    if (managedObjectContext == nil)
    { 
        managedObjectContext = [(NGAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext]; 
    }
}

- (void)viewDidUnload
{
    [self setBtnUloadData:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self toggleUploadButton];
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
    // Return YES for portrait
    return (interfaceOrientation == UIInterfaceOrientationPortrait || 
            interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown);
}

@end
