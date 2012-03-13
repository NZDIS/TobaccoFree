//
//  NGFirstViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <SystemConfiguration/SCNetworkReachability.h>
#include <netinet/in.h>

#import "MainViewController.h"
#import "AccountDetailsViewController.h"
#import "NGAppDelegate.h"
#import "Observations.h"


@implementation MainViewController


@synthesize btnUloadData;
@synthesize activityIndicator;

@synthesize observationsForUpload;
@synthesize managedObjectContext;
@synthesize receivedData;
@synthesize receivedResponse;


BOOL connectionFailed = NO;

#pragma mark - Connection handling
/*

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    self.receivedResponse = (NSHTTPURLResponse *)response;
    DLog(@"connectionDidReceiveResponse: %d -- %@", [receivedResponse statusCode], receivedResponse.description);
}

- (void)connection:(NSURLConnection *)theConnection didReceiveData:(NSData *)data {
    DLog(@"connectionDidReceivedata: %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
}

- (void)connection:(NSURLConnection *)theConnection didFailWithError:(NSError *)error {
    DLog(@"connectionDidFail");
    connectionFailed = YES;
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

*/

- (BOOL) checkDataSubmission {
    BOOL resultOK = NO;
    if ([self.receivedResponse statusCode] == 403) {
        [self showUserErrorDialog:@"Wrong credentials. User authentication failed."];
    } else if ([self.receivedResponse statusCode] == 400) {
        [self showUserErrorDialog:@"Bad data request. Please try again later."];
    } else if([self.receivedResponse statusCode] == 200) {
        resultOK = YES;
    } else {
        [self showUserErrorDialog:@"Connection to server failed. Please try again later."];
    }
    return resultOK;
}


- (BOOL) isConnectedToNetwork {
    struct sockaddr_in zeroAddress;
    bzero(&zeroAddress, sizeof(zeroAddress));
    zeroAddress.sin_len = sizeof(zeroAddress);
    zeroAddress.sin_family = AF_INET;
	
    // Recover reachability flags
    SCNetworkReachabilityRef defaultRouteReachability = SCNetworkReachabilityCreateWithAddress(NULL, (struct sockaddr *)&zeroAddress);
    SCNetworkReachabilityFlags flags;
	
    BOOL didRetrieveFlags = SCNetworkReachabilityGetFlags(defaultRouteReachability, &flags);
    CFRelease(defaultRouteReachability);
	
    if (!didRetrieveFlags){
        NSLog(@"Error. Could not recover network reachability flags.");
        return NO;
    }
	
    BOOL isReachable = flags & kSCNetworkFlagsReachable;
    BOOL needsConnection = flags & kSCNetworkFlagsConnectionRequired;
    BOOL nonWiFi = flags & kSCNetworkReachabilityFlagsTransientConnection;
    return ((isReachable && !needsConnection) || nonWiFi) ? YES : NO;
}

- (BOOL) isConnectedToNZDIS {
    NSError *error;
    NSString *hack = [NSString stringWithContentsOfURL:[NSURL URLWithString:URL_OBSERVATION_ADD] encoding:NSUTF8StringEncoding error:&error];
    if (hack != NULL) return YES;
    return NO;
}

- (void) showUserErrorDialog:(NSString *)msg {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error"
                                       message:msg
                                      delegate:self
                             cancelButtonTitle:@"Ok"
                             otherButtonTitles:nil];
    [alert show];
}



#pragma mark - Utilities

- (void) toggleUploadButton {
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

    

- (BOOL) prePOSTChecksFailed {
    // check credentials
    if ([self isCredentialsReady] == NO) {
        [self performSegueWithIdentifier:@"AccountViewSegue" sender:self];
        return YES;
    } 
    // check network availability
    if (![self isConnectedToNetwork]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Unable to upload"
                                                        message:@"No Internet. Connect your iPhone to 3G or WiFi."
                                                       delegate:self
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil];
        [alert show];
        return YES;
    }
    // check if server is online
    if (![self isConnectedToNZDIS]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Unable to upload"
                                                        message:@"Connection to server failed. Please try again later."
                                                       delegate:self
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil];
        [alert show];
        return YES;
    }
    return NO;
}


#pragma mark - Button actions

- (IBAction) uploadData:(id)sender {
    if ([self prePOSTChecksFailed]) return;
    [self.activityIndicator setHidden:NO];
    [self.activityIndicator startAnimating];
    [self performSelector:@selector(uploadAllObservationData) withObject:nil afterDelay:0];
}


- (void) uploadAllObservationData {
    BOOL allWentWell = YES;
    for (Observations *o in observationsForUpload) {
        // prepare Dictionary first.
        NSMutableDictionary *dict = [o toDictionary];
        [self setUserCredentials:dict];
        
        /* 
         // SBJson version, compatible with iOS 4.xx
         SBJsonWriter *writer = [[SBJsonWriter alloc] init];
         NSString *json = [writer stringWithObject:data];
        */
        
        // For iOS 5.xx we use native JSON support
        NSError *error = nil;  
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
        /* DEBUGGING
        DLog(@"got observations: %@", [dict description]);
        DLog(@"Prepared json: %@", [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding]);
        */
        // Prepare the payload
        NSString *requestString = [NSString stringWithFormat:@"Observation=%@",
                                   [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding]];
        // requestString = [requestString stringByAddingPercentEscapesUsingEncoding:4];
        NSData *requestData = [NSData dataWithBytes:[requestString UTF8String] length:[requestString length]];
        // Let's create asynchronous HTTP POST call
        NSURL *url = [NSURL URLWithString:URL_OBSERVATION_ADD];
        NSMutableURLRequest *req = [NSMutableURLRequest requestWithURL:url];
        [req setHTTPMethod:@"POST"];
        [req setValue:@"application/json" forHTTPHeaderField:@"Accept"];
        [req setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        [req setValue:[NSString stringWithFormat:@"%d", [requestData length]] forHTTPHeaderField:@"Content-Length"];
        [req setHTTPBody: requestData];
        
        /* Asynchronous call
        NSURLConnection *connection = [[NSURLConnection alloc]initWithRequest:req delegate:self];
        if (connection) {
            self.receivedData = [NSMutableData data];
            DLog(@"Got receivedData pass here");
            if (connectionFailed) return;
        } else {
            DLog(@"Got connection empty");
            return;
        }*/
        
        // Synchronous version
        NSURLResponse *response;
        self.receivedData = [NSURLConnection sendSynchronousRequest:req returningResponse:&response error:&error];
        self.receivedResponse = (NSHTTPURLResponse *) response;
        DLog(@"finishedJsonPOST and got data back: %@", [[NSString alloc] initWithData:self.receivedData encoding:NSUTF8StringEncoding]);
        if ([self checkDataSubmission]) {
            o.uploaded = YES;
            error = nil;
            if (![self.managedObjectContext save:&error]) {
                NSLog(@"Saving of the data to SQLite DB failed.");
                [self showUserErrorDialog:@"Saving data to DB failed."];
            }
        } else {
            // something went wrong, so we do not try to send the rest of data records
            allWentWell = NO;
            break;
        }
    }
    [self.activityIndicator startAnimating];
    [self.activityIndicator setHidden:YES];
    [self toggleUploadButton];
    if (allWentWell) {
        UIAlertView *alert;
        alert = [[UIAlertView alloc] initWithTitle:@"Info"
                                           message:@"Data uploaded. Thank you."
                                          delegate:self
                                 cancelButtonTitle:@"Ok"
                                 otherButtonTitles:nil];
        [alert show];
    }
}




#pragma mark - View lifecycle

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}


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
    [self setActivityIndicator:nil];
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
    [self.activityIndicator setHidden:YES];
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
