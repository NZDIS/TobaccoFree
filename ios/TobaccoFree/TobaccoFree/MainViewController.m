//
//  NGFirstViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "MainViewController.h"
#import "AccountDetailsViewController.h"
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


- (NSDictionary *) userAccountPreferences {
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
    return temp;
}


- (BOOL) isCredentialsReady {
    NSDictionary *pref = [self userAccountPreferences];
    return ([pref objectForKey:USER_USER_EMAIL] != nil) && (![[pref objectForKey:USER_USER_EMAIL] isEqualToString:@""]);
}


/*!
 setup the credentials in the Json structure.
 */
- (void) setUserCredentials:(NSMutableDictionary *)dict {
    NSDictionary *temp = [self userAccountPreferences];
    [dict setValue:[temp objectForKey:USER_USER_EMAIL] forKey:USER_USER_EMAIL];
    [dict setValue:[temp objectForKey:USER_PASSWORD_HASH] forKey:USER_PASSWORD_HASH];
    [dict setValue:[temp objectForKey:USER_DEVICE] forKey:USER_DEVICE];
    [dict setValue:[temp objectForKey:USER_DEVICE_TYPE] forKey:USER_DEVICE_TYPE];
}

    

- (IBAction)uploadData:(id)sender {
    if ([self isCredentialsReady] == NO) {
        [self performSegueWithIdentifier:@"AccountViewSegue" sender:self];
        return;
    } 
    for (Observations *o in observationsForUpload) {
        // prepare Dictionary first.
        NSMutableDictionary *dict = [o toDictionary];
        [self setUserCredentials:dict];
        /* 
         
         SBJson version, compatible with iOS 4.xx
         
         SBJsonWriter *writer = [[SBJsonWriter alloc] init];
         NSString *json = [writer stringWithObject:data];
         
         For iOS 5.xx we use native JSON support
        */
        NSError *error = nil;  
        NSData *json = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
        NSLog(@"got observations: %@", [dict description]);
        NSLog(@"Got json: %@", [[NSString alloc] initWithData:json encoding:NSUTF8StringEncoding]);
    }
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
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
