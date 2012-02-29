//
//  AccountDetailsViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "AccountDetailsViewController.h"

@implementation AccountDetailsViewController

@synthesize txtEmail;
@synthesize txtPassword;

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - Action handlers

- (IBAction) saveCredentials {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"hej stary"
                                                    message:[@"Email: " stringByAppendingString:txtEmail.text]
                                                   delegate:self
                                          cancelButtonTitle:@"Ok"
                                          otherButtonTitles:nil];
    [alert show];
}

- (IBAction) openTobaccoFreeWebsite
{
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString: @"http://tobaccofree.nzdis.org"]];
}


- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)viewDidUnload
{
    [self setTxtEmail:nil];
    [self setTxtPassword:nil];
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
