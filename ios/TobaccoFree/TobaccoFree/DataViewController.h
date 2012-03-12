//
//  DataViewController.h
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DataViewController : UITableViewController <UITableViewDelegate, UITableViewDataSource> {

    NSMutableArray *observationsArray;
    NSManagedObjectContext *managedObjContext;

}

@property (nonatomic, retain) NSMutableArray *observationsArray;
@property (nonatomic, retain) NSManagedObjectContext *managedObjContext;

@end
