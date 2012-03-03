//
//  AccountDetailsViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AccountDetailsViewController : UIViewController <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UITextField *txtFieldEmail;
@property (weak, nonatomic) IBOutlet UITextField *txtFieldPassword;

- (IBAction) saveCredentials;
- (IBAction) openTobaccoFreeWebsite;
- (IBAction) textFieldShouldReturn:(id)sender;
- (IBAction) backgroundTouched:(id)sender;

@end
