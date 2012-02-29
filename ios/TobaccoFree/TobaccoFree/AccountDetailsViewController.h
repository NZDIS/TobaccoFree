//
//  AccountDetailsViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AccountDetailsViewController : UIViewController <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UITextField *txtEmail;
@property (weak, nonatomic) IBOutlet UITextField *txtPassword;

- (IBAction) saveCredentials;
- (IBAction) openTobaccoFreeWebsite;

@end
