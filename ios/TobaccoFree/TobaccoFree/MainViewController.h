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

@property (nonatomic, weak) IBOutlet UIButton *btnUloadData;

@property (nonatomic, retain) NSMutableArray *observationsForUpload;
@property (nonatomic, retain) NSManagedObjectContext *managedObjectContext;



- (IBAction)uploadData:(id)sender;


@end
