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
@synthesize receivedData;
@synthesize receivedResponse;



#pragma mark - URL connection handling

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    self.receivedResponse = (NSHTTPURLResponse *)response;
    DLog(@"connectionDidReceiveResponse: %d -- %@", [receivedResponse statusCode], receivedResponse.description);
}

- (void)connection:(NSURLConnection *)theConnection didReceiveData:(NSData *)data {
    DLog(@"connectionDidReceivedata: %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
}

- (void)connection:(NSURLConnection *)theConnection didFailWithError:(NSError *)error {
    DLog(@"connectionDidFail");
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error"
                                                    message:[@"Connection to server failed: " stringByAppendingFormat:@"%@", 
                                                              [error localizedDescription]]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)theConnection {
    DLog(@"connectionDidFinishLoading");
    UIAlertView *alert;
    if ([self.receivedResponse statusCode] == 403) {
        alert = [[UIAlertView alloc] initWithTitle:@"Error"
                                           message:@"Wrong credentials. User authentication failed."
                                          delegate:self
                                 cancelButtonTitle:@"Ok"
                                 otherButtonTitles:nil];
        
    } else if([self.receivedResponse statusCode] == 200) {
        alert = [[UIAlertView alloc] initWithTitle:@"Info"
                                           message:@"Data uploaded. Thank you."
                                          delegate:self
                                 cancelButtonTitle:@"Ok"
                                 otherButtonTitles:nil];
    } else {
        alert = [[UIAlertView alloc] initWithTitle:@"Error"
                                           message:@"Connection to server failed. Please try again later."
                                          delegate:self
                                 cancelButtonTitle:@"Ok"
                                 otherButtonTitles:nil];
    }
    [alert show];
}




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
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
        /* DEBUGGING */
        DLog(@"got observations: %@", [dict description]);
        DLog(@"Got json: %@", [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding]);
        /**/
        // Prepare the payload
        NSString *requestString = [NSString stringWithFormat:@"Observation=%@",
                                   [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding]];
        //requestString = [myRequestString stringByAddingPercentEscapesUsingEncoding:4];
        NSData *requestData = [NSData dataWithBytes:[requestString UTF8String] length:[requestString length]];
        // Let's create asynchronous HTTP POST call
        NSURL *url = [NSURL URLWithString:URL_OBSERVATION_ADD];
        NSMutableURLRequest *req = [NSMutableURLRequest requestWithURL:url];
        [req setHTTPMethod:@"POST"];
        [req setValue:@"application/json" forHTTPHeaderField:@"Accept"];
        [req setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        [req setValue:[NSString stringWithFormat:@"%d", [requestData length]] forHTTPHeaderField:@"Content-Length"];
        [req setHTTPBody: requestData];
        
        NSURLConnection *connection = [[NSURLConnection alloc]initWithRequest:req delegate:self];
        if (connection) {
            self.receivedData = [NSMutableData data];
            DLog(@"Got receivedData pass here");
        } else {
            DLog(@"Got connection empty");
        }
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
