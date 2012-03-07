//
//  AccountDetailsViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <CommonCrypto/CommonDigest.h>

#import "AccountDetailsViewController.h"


@implementation AccountDetailsViewController


@synthesize txtFieldEmail;
@synthesize txtFieldPassword;
@synthesize txtDeviceID;
@synthesize txtDeviceType;


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}


- (NSString *) hashPassword:(NSString *) pass {
    NSMutableString *input = [[NSMutableString alloc] initWithFormat:@"%@%@%@", HASH_SALT_PRE, pass, HASH_SALT_POST];

    const char* str = [input UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(str, strlen(str), result);
    
    NSMutableString *ret = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH*2];
    for(int i = 0; i<CC_MD5_DIGEST_LENGTH; i++) {
        [ret appendFormat:@"%02x",result[i]];
    }
    return ret;
}


#pragma mark - Action handlers

/*!
 Create universally unique identifier (object)
 */
- (NSString *)createUUID {
    CFUUIDRef uuidObject = CFUUIDCreate(kCFAllocatorDefault);
    // Get the string representation of CFUUID object.
    NSString *uuidStr = (__bridge NSString *)CFUUIDCreateString(kCFAllocatorDefault, uuidObject);
    CFRelease(uuidObject);
    return uuidStr;
}

- (IBAction) saveCredentials {
    /*
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"hej stary"
                                                    message:[@"Email: " stringByAppendingString:self.txtFieldEmail.text]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
    */
    if (self.txtDeviceID == nil || [self.txtDeviceID isEqualToString:@""]) {
        self.txtDeviceID = [self createUUID];
    }
    
    self.txtDeviceType = [NSString stringWithFormat:@"%@, %@, %@, %@", 
        [UIDevice currentDevice].model, 
        [UIDevice currentDevice].name,
        [UIDevice currentDevice].systemName,
        [UIDevice currentDevice].systemVersion];
    
    NSString *error;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"User.plist"];
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:
                                self.txtDeviceID,
                                self.txtDeviceType,
                                txtFieldEmail.text, 
                                [self hashPassword:txtFieldPassword.text], 
                                nil]
                            forKeys:[NSArray arrayWithObjects: 
                                USER_DEVICE,
                                USER_DEVICE_TYPE,
                                USER_USER_EMAIL, 
                                USER_PASSWORD_HASH, 
                                nil]];
    NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
                                                                   format:NSPropertyListXMLFormat_v1_0
                                                         errorDescription:&error];
    if(plistData) {
        [plistData writeToFile:plistPath atomically:YES];
    }
    else {
        NSLog(@"Error writing properties for User. %@", error);
    }
    
    // Go back to the main screen
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction) openTobaccoFreeWebsite
{
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString: @"http://tobaccofree.nzdis.org"]];
}

- (IBAction)textFieldPasswordFocus:(id)sender {
    [self.txtFieldPassword becomeFirstResponder];
}


- (IBAction) textFieldShouldReturn:(id) textField {
    [textField resignFirstResponder];
}

- (IBAction) backgroundTouched:(id)sender {
    [self.view endEditing:YES];
}


#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
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
    self.txtFieldEmail.text = [temp objectForKey:USER_USER_EMAIL];
    self.txtFieldPassword.text = [temp objectForKey:USER_PASSWORD_HASH];
    self.txtDeviceID = [temp objectForKey:USER_DEVICE];
    self.txtDeviceType = [temp objectForKey:USER_DEVICE_TYPE];
}

- (void)viewDidUnload
{
    [self setTxtFieldEmail:nil];
    [self setTxtFieldPassword:nil];
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
    // Return YES for supported orientations
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
    } else {
        return YES;
    }
}

@end
