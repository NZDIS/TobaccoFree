package org.nzdis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ObservationActivity extends Activity implements LocationListener, OnCancelListener, OnClickListener{
	
	private boolean started = false, gotGPS = false;
	private ProgressDialog dialog;
	private LocationManager locManager;
	private Location loc;
	private Observation observation;
	private Button btnFinish,btnHelp,btnNoSmoking,btnNoOccupants,btnOtherAdults,btnChild;
	
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
        
        if(savedInstanceState != null){
        	started = savedInstanceState.getBoolean("started", false);
        	gotGPS = savedInstanceState.getBoolean("gps", false);
        }
        
        if(gotGPS){
        	// got gps fix
        	// observation instance must have been created, retrieve
        	Observation temp = (Observation)getLastNonConfigurationInstance();
        	if(temp == null){
        		//something not right, make new observation instance and get gps fix
        		gotGPS = false;
        		getGPSLocation();
        	}else{
        		observation = temp;
        		//enabled finish button if the test has started
        		if(observation.isStarted()){
        			btnFinish.setEnabled(true);
        		}
        		loc = temp.getLocation();
        	}
        }else{
        	getGPSLocation();
        }
    }
    
    private void getGPSLocation(){
    	// get gps fix
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	dialog = ProgressDialog.show(this, "",this.getString(R.string.getting_gps), true,true);
    	dialog.setOnCancelListener(this);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	if(observation != null){
    		return observation;
    	}else{
    		return null;
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	outState.putBoolean("started", started);
    	outState.putBoolean("gps", gotGPS);
    	super.onSaveInstanceState(outState);
    }
    
    
    /* if started then possibly save data to the database
     * incase of app being closed by the OS so that the
     * current observation can be restored
     */
    @Override
    protected void onPause(){   	    	
    	if(dialog != null){
    		dialog.dismiss();
    	}
    	if(locManager != null){
    		locManager.removeUpdates(this);
    	}
    	super.onPause();
    }
    
    @Override
    public void onBackPressed(){
    	if(started){
    		//show message about finishing observation
    		//or return to home (android home screen)
    		
    	}else{
    		finish();
    	}
    	return;
    }
    
    private void retreivedGPS(){
		// start observation
		// maybe show message
    	locManager.removeUpdates(this);
    	gotGPS = true;
    	started = true;
		dialog.dismiss();
		observation = new Observation(System.currentTimeMillis());
		observation.setLocation(loc);
		Log.i("globalink",loc.getLatitude() + " " + loc.getLongitude());
    }
    
	@Override
	public void onLocationChanged(Location arg0) {
		
		/* True values for when using the emulator */
		if(arg0.hasAccuracy() || true){
			if((arg0.getAccuracy() <= 10 && arg0.getAccuracy() > 0) || true){
				loc = arg0;
				retreivedGPS();
			}
		}
		
	}

	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	@Override
	public void onCancel(DialogInterface arg0) {
		locManager.removeUpdates(this);
		finish();		
	}

	@Override
	public void onClick(View v) {
		
		if(v == btnFinish){
			//observation finished, save to database
			Log.i("globalink",observation.toString());
			//save to db here
			//ask to upload now maybe?
			finish();
			return;
		}
		
		if(v == btnHelp){
			//display help screen
			return;
		}
		
		if(v == btnNoSmoking){
			observation.incrementNoSmoking();
			observation.setStarted(true);
			btnFinish.setEnabled(true);
			started = true;
			return;
		}
		
		if(v == btnNoOccupants){
			observation.incrementNoOthers();
			observation.setStarted(true);
			btnFinish.setEnabled(true);
			started = true;
			return;
		}
		
		if(v == btnOtherAdults){
			observation.incrementOther();
			observation.setStarted(true);
			btnFinish.setEnabled(true);
			started = true;
			return;
		}
		
		if(v == btnChild){
			observation.incrementChild();
			observation.setStarted(true);
			btnFinish.setEnabled(true);
			started = true;
			return;
		}
		
	}
}
