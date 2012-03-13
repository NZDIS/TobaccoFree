//
//  NGFirstViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 24/02/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MainViewController : UIViewController {
    
    NSMutableArray *observationsForUpload;
    NSManagedObjectContext *managedObjectContext;
    
}

@property (weak, nonatomic) IBOutlet UIButton *btnUloadData;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicator;

@property (nonatomic, retain) NSMutableArray *observationsForUpload;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, retain) NSData *receivedData;
@property (nonatomic, retain) NSHTTPURLResponse *receivedResponse;


- (IBAction)uploadData:(id)sender;
- (BOOL) isCredentialsReady;
- (NSDictionary *) userAccountPreferences;

@end
