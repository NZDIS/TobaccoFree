//
//  AccountDetailsViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AccountDetailsViewController : UIViewController <UITextFieldDelegate>

@property (nonatomic, weak) IBOutlet UITextField *txtFieldEmail;
@property (nonatomic, weak) IBOutlet UITextField *txtFieldPassword;
@property (nonatomic, retain) NSString *txtDeviceID;
@property (nonatomic, retain) NSString *txtDeviceType;

- (IBAction) saveCredentials;
- (IBAction) openTobaccoFreeWebsite;
- (IBAction)textFieldPasswordFocus:(id)sender;
- (IBAction) textFieldShouldReturn:(id)sender;
- (IBAction) backgroundTouched:(id)sender;

@end
