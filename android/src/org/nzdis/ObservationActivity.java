package org.nzdis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ObservationActivity extends Activity implements LocationListener, OnClickListener{
	
	private LocationManager locManager;
	private ProgressDialog gpsDialog;
	private long observationId;
	private Button btnFinish,btnHelp,btnNoSmoking,btnNoOccupants,btnOtherAdults,btnChild;
	private boolean showingGPSDialog;
	public static final int GPS_DIALOG = 2;
	public static final int CONFIRM_DIALOG = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation);
        
        btnFinish = (Button)findViewById(R.id.btnFinish);
        btnHelp = (Button)findViewById(R.id.btnHelp);
        btnNoSmoking = (Button)findViewById(R.id.btnNoSmoking);
        btnNoOccupants = (Button)findViewById(R.id.btnNoOccupants);
        btnOtherAdults = (Button)findViewById(R.id.btnOtherAdults);
        btnChild = (Button)findViewById(R.id.btnChild);
        
        btnFinish.setOnClickListener(this);
        btnHelp.setOnClickListener(this);
        btnNoSmoking.setOnClickListener(this);
        btnNoOccupants.setOnClickListener(this);
        btnOtherAdults.setOnClickListener(this);
        btnChild.setOnClickListener(this);
        
        DatabaseHelper db = new DatabaseHelper(this);
        if(savedInstanceState != null){
        	observationId = savedInstanceState.getLong("observationId",-1);
        }
        
        if(observationId < 1){		    
		    try{
		    	observationId = db.getNewObservationId();
		    }catch(DatabaseException e){
		    	Log.e("Globalink",e.getMessage());
		    	db.close();
		    	finish();
		    }
		    
		    
        }
        
        if(!db.hasGPS(observationId)){
	       	locManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
	        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }else{
        	locManager = null;
        }
        db.close();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	/*if(observation != null){
    		return observation;
    	}else{
    		return null;
    	}*/
    	return null;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	outState.putLong("observationId", observationId);
    	super.onSaveInstanceState(outState);
    }
    
    
    /* if started then possibly save data to the database
     * incase of app being closed by the OS so that the
     * current observation can be restored
     */
    @Override
    protected void onPause(){   	    	
    	if(locManager != null){
    		locManager.removeUpdates(this);
    	}
    	super.onPause();
    }
    
    @Override
    public void onBackPressed(){
    	//check to see if the user has actually counted anything
    	DatabaseHelper db = new DatabaseHelper(this);
    	if(db.hasCounted(observationId)){
    		
    		//check if gps position has been found
    		if(db.hasGPS(observationId)){
    			// ask if finished
    			onCreateDialog(CONFIRM_DIALOG);    			
    		}else{
    			// ask to wait for GPS
    			showingGPSDialog = true;
    			gpsDialog = ProgressDialog.show(this, "",getString(R.string.gps_fix), true,false);
    		}
    	}else{
    		//delete unused observation
    		db.deleteObservation(observationId);
        	db.close();
        	finish();
    	}
    }
    
    /* 
     * Saves the given GPS Location to the current observation
     */
	private void finishGPS(Location loc) {
		locManager.removeUpdates(this);
		DatabaseHelper db = new DatabaseHelper(this);
		db.saveGPSLocation(observationId,loc);
		db.close();
		
		if(showingGPSDialog){
			//user must have selected finish button without a fix,
			//remove dialog and confirm finish observation
			gpsDialog.dismiss();
			onCreateDialog(CONFIRM_DIALOG);
		}
	}
    
	private void finishObservation(){
		//save finish time
		DatabaseHelper db = new DatabaseHelper(this);
		db.setFinishTime(observationId);
		db.close();
		finish();
	}
	@Override
	public void onLocationChanged(Location arg0) {		
		/* True values for when using the emulator */
		if(arg0.hasAccuracy() || true){
			if((arg0.getAccuracy() <= 10 && arg0.getAccuracy() > 0) || true){
				finishGPS(arg0);
			}
		}
		
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case CONFIRM_DIALOG:
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setTitle(getString(R.string.confirm_title));
				alert.setMessage(getString(R.string.confirm_close));
				
				alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   finishObservation();
			        	   dialog.cancel();
			           }
			       });
			    alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			    alert.show();
				
			default:
				return null;
		}
	}
	
	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	@Override
	public void onClick(View v) {
		
		if(v == btnFinish){
			onBackPressed();
			return;
		}
		
		if(v == btnHelp){
			//display help screen
			return;
		}
		
		if(v == btnNoSmoking){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementNoSmoking(observationId);
			db.close();
			return;
		}
		
		if(v == btnNoOccupants){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementNoOccupants(observationId);
			db.close();
			return;
		}
		
		if(v == btnOtherAdults){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementOtherAdults(observationId);
			db.close();
			return;
		}
		
		if(v == btnChild){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementChild(observationId);
			db.close();
			return;
		}
		
	}
}
