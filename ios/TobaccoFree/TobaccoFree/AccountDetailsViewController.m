//
//  AccountDetailsViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "AccountDetailsViewController.h"

@implementation AccountDetailsViewController


@synthesize txtFieldEmail;
@synthesize txtFieldPassword;



- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - Action handlers

- (IBAction) saveCredentials {
    NSLog(@"Got email: %@", txtFieldEmail);
    
    /*
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"hej stary"
                                                    message:[@"Email: " stringByAppendingString:self.txtFieldEmail.text]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
    */
    NSString *error;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"User.plist"];
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
                               [NSArray arrayWithObjects: txtFieldEmail.text, txtFieldPassword.text, nil]
                                                          forKeys:[NSArray arrayWithObjects: @"user_email", @"user_password_hash", nil]];
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
    self.txtFieldEmail.text = [temp objectForKey:@"user_email"];
    self.txtFieldPassword.text = [temp objectForKey:@"user_password_hash"];
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
