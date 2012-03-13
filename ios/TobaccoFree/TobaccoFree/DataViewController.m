//
//  DataViewController.m
//  TobaccoFree
//
//  Created by Mariusz Nowostawski on 1/03/12.
//  Copyright (c) 2012 Ngarua Technologies Ltd. All rights reserved.
//

#import "DataViewController.h"
#import "Observations.h"
#import "ObservationsDataCell.h"
#import "NGAppDelegate.h"

@implementation DataViewController

@synthesize observationsArray;
@synthesize managedObjContext;



- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // fetch all the data
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Observations" inManagedObjectContext:managedObjContext];
    [request setEntity:entity];
    
    NSError *error = nil;
    observationsArray = [[managedObjContext executeFetchRequest:request error:&error] mutableCopy];
    if (observationsArray == nil) {
        // Handle the error.
        NSLog(@"Error executing the query %@", error);
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // setup managedObjContext
    if (managedObjContext == nil)
    { 
        managedObjContext = [(NGAppDelegate *)[[UIApplication sharedApplication] delegate] managedObjectContext]; 
    }
        
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    
    self.observationsArray = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [observationsArray count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"ObservationsDataCell";
   
    ObservationsDataCell *cell = (ObservationsDataCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
   
    Observations *obs = [self.observationsArray objectAtIndex:indexPath.row];
    
    cell.label_num_cars.text = [NSString stringWithFormat:@"%d", [obs countCars]];
    //cell.label_date.text = [NSString stringWithFormat:@"%d", [obs timestamp_start]];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"YYYY-MM-dd HH:mm:ss"];
    cell.label_date.text = [formatter stringFromDate:[NSDate dateWithTimeIntervalSince1970:obs.timestamp_start]];
    
    cell.label_no_smoking.text = [NSString stringWithFormat:@"%d", obs.noSmoking];
    cell.label_lone_adult.text = [NSString stringWithFormat:@"%d", obs.smokingLoneAdult];
    cell.label_other_adults.text = [NSString stringWithFormat:@"%d", obs.smokingAdultOthers];
    cell.label_child.text = [NSString stringWithFormat:@"%d", obs.smokingChild];
    
    if (obs.uploaded) {
        [cell.label_not_uploaded setHidden:YES];
    }
    return cell;
}



- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the observation from the data array and from the DB
        Observations *o = [self.observationsArray objectAtIndex:indexPath.row];
        [self.observationsArray removeObjectAtIndex:indexPath.row];
        [self.managedObjContext deleteObject:o];
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }
//    [tableView reloadData];
}



// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}


/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     */
}

@end
