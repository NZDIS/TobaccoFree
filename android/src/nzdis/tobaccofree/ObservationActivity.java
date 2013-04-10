/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Represents the act of logging smoking.
 * 
 * @author Hamish Medlin
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Dec 2011
 */
public class ObservationActivity extends Activity implements LocationListener, OnClickListener {
	
	private LocationManager locManager;
	private ProgressDialog gpsDialog;
	private MediaPlayer clickSound;
	private SharedPreferences preferences;
	private long observationId;
	private Button btnFinish,btnHelp,btnNoSmoking,btnNoOccupants,btnOtherAdults,btnChild;
	private boolean showingGPSDialog;
	public static final int GPS_DIALOG = 2;
	public static final int CONFIRM_DIALOG = 1;
	private TextView tvAdults,tvAlone,tvChild,tvNone;
	private int contextSelected = -1;
	
	private static final int CONTEXT_CHILD = 1,CONTEXT_NONE = 2,CONTEXT_ALONE = 3,CONTEXT_ADULTS = 4;
	
	// GPS constants
	private static long GPS_UPDATE_TIME_INTERVAL = 1000;
	private static long GPS_UPDATE_DISTANCE_INTERVAL = 10;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        if(preferences.getBoolean("left_hand", false)){
        	setContentView(R.layout.activity_observation_left);
        	//Log.i("Globalink","Using Left Hand Layout: " + preferences.getBoolean("left_hand", true));
        }else{
        	setContentView(R.layout.activity_observation);
        	//Log.i("Globalink","Using Right Hand Layout:" + preferences.getBoolean("left_hand", true));
        }
        
        tvAlone = (TextView)findViewById(R.id.tvSingleAdultCount);
        tvAdults = (TextView)findViewById(R.id.tvMultipleAdultCount);
        tvChild = (TextView)findViewById(R.id.tvChildCount);
        tvNone = (TextView)findViewById(R.id.tvNoSmokingCount);
        
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
        
        registerForContextMenu(btnNoSmoking);
        registerForContextMenu(btnNoOccupants);
        registerForContextMenu(btnOtherAdults);
        registerForContextMenu(btnChild);
        
        final DatabaseHelper db = new DatabaseHelper(this);
        if(savedInstanceState != null) {
        	observationId = savedInstanceState.getLong("observationId", -1);
        	
            if(observationId < 1){		    
    		    try{
    		    	observationId = db.getNewObservationId();
    		    }catch(DatabaseException e){
    		    	Log.e("Globalink",e.getMessage());
    		    	db.close();
    		    	finish();
    		    }		    
            }
            
            if (savedInstanceState.getBoolean("showingGPSDialog", false)) {
            	gpsDialog = ProgressDialog.show(this, "", getString(R.string.gps_fix), true, true);
            	showingGPSDialog = true;
            }
            
            tvAlone.setText(db.getLoneSmokerCount(observationId) + "");
            tvAdults.setText(db.getOtherAdultsSmokingCount(observationId) + "");
            tvChild.setText(db.getChildCount(observationId) + "");
            tvNone.setText(db.getNoSmokingCount(observationId)+"");
        } else {
		    try {
		    	observationId = db.getNewObservationId();
		    } catch(DatabaseException e){
		    	Log.e("Globalink",e.getMessage());
		    	db.close();
		    	finish();
		    }
        }
    
        checkLastKnownLocation();
       	locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL, this);
        
        db.close();
        
        clickSound = MediaPlayer.create(this, R.raw.click);
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
    	outState.putBoolean("showingGPSDialog", showingGPSDialog);
    	super.onSaveInstanceState(outState);
    }
    
    
    /**
     * If started then possibly save data to the database
     * in case of app being closed by the OS so that the
     * current observation can be restored
     */
    @Override
    protected void onPause() {   	    	
    	locManager.removeUpdates(this);
    	try {
    		gpsDialog.dismiss();
    	} catch (NullPointerException e) {
    	}
    	clickSound.release();
    	locManager = null;
    	super.onPause();   	
    }
    
    @Override
    protected void onResume(){
       	locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL, this);
        super.onResume();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            quitPressed();
            return true;
        } else { 
        	return super.onKeyDown(keyCode, event);
        }
    }
    
    private void checkLastKnownLocation() {
    	final DatabaseHelper db = new DatabaseHelper(this);
    	// Get the location manager
    	final LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		final String bestProvider = locManager.getBestProvider(criteria, false);
		final Location location = locManager.getLastKnownLocation(bestProvider);
		if(location != null){
			db.saveGPSLocation(observationId, location);
		}
		db.close();

    }
    
    private void quitPressed() {
    	locManager.removeUpdates(this);
    	//check to see if the user has actually counted anything
    	DatabaseHelper db = new DatabaseHelper(this);
    	if(db.hasCounted(observationId)) {
    		//check if gps position has been found
    		if(db.hasGPS(observationId)) {
    			// ask if finished
    			onCreateDialog(CONFIRM_DIALOG);    			
    		} else {
    			// ask to wait for GPS
    			showingGPSDialog = true;
    			gpsDialog = ProgressDialog.show(this, "", getString(R.string.gps_fix), true, true);
    		}
    	} else {
    		//delete unused observation
    		db.deleteObservation(observationId);
        	db.close();
        	finish();
    	}
    }
    
    
	private void finishObservation() {
		//save finish time
		DatabaseHelper db = new DatabaseHelper(this);
		db.setFinishTime(observationId);
		db.close();
		finish();
	}
	
	@Override
	public void onLocationChanged(Location loc) {
		locManager.removeUpdates(this);
		DatabaseHelper db = new DatabaseHelper(this);
		db.saveGPSLocation(observationId, loc);
		db.close();
		
		if (showingGPSDialog) {
			//user must have selected finish button without a fix,
			//remove dialog and confirm finish observation
			gpsDialog.dismiss();
			showDialog(CONFIRM_DIALOG);
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
			quitPressed();
			return;
		}
		
		if(v == btnHelp){
			Intent instructions = new Intent(this,InstructionsActivity.class);
			startActivity(instructions);
			return;
		}
		
		if(v == btnNoSmoking){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementNoSmoking(observationId);
			tvNone.setText(db.getNoSmokingCount(observationId)+"");
			db.close();
			playSound();
			return;
		}
		
		if(v == btnNoOccupants){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementLoneAdultSmoking(observationId);
			tvAlone.setText(db.getLoneSmokerCount(observationId) + "");
			db.close();
			playSound();
			return;
		}
		
		if(v == btnOtherAdults){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementOtherAdults(observationId);
			tvAdults.setText(db.getOtherAdultsSmokingCount(observationId) + "");
			db.close();
			playSound();            
			return;
		}
		
		if(v == btnChild){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementChild(observationId);
			tvChild.setText(db.getChildCount(observationId) + "");
			db.close();
			playSound();
			return;
		}
		
	}
	
	
	
	@Override
	public void onClick(View v) {
		
		if(v == btnFinish){
			quitPressed();
			return;
		}
		
		if(v == btnHelp){
			Intent instructions = new Intent(this,InstructionsActivity.class);
			startActivity(instructions);
			return;
		}
		
		if(v == btnNoSmoking){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementNoSmoking(observationId);
			tvNone.setText(db.getNoSmokingCount(observationId)+"");
			db.close();
			playSound();
			return;
		}
		
		if(v == btnNoOccupants){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementLoneAdultSmoking(observationId);
			tvAlone.setText(db.getLoneSmokerCount(observationId) + "");
			db.close();
			playSound();
			return;
		}
		
		if(v == btnOtherAdults){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementOtherAdults(observationId);
			tvAdults.setText(db.getOtherAdultsSmokingCount(observationId) + "");
			db.close();
			playSound();            
			return;
		}
		
		if(v == btnChild){
			DatabaseHelper db = new DatabaseHelper(this);
			db.incrementChild(observationId);
			tvChild.setText(db.getChildCount(observationId) + "");
			db.close();
			playSound();
			return;
		}
		
	}

	/* 
	 * Will play a 'click' sound if it hasn't been disabled
	 * in the preferences
	 */
	private void playSound(){
		if (preferences.getBoolean("play_sound", true)) {
			try {
				clickSound.start();
			} catch(IllegalStateException ise) {}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.preferences_button:
	        Intent i = new Intent(this,PreferencesActivity.class);
	        startActivityForResult(i,0);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
		clickSound = MediaPlayer.create(this,R.raw.click);
		
		// set left/right hand layout if it was changed in the preferences
        if(preferences.getBoolean("left_hand", false)){
        	setContentView(R.layout.activity_observation_left);
        	Log.i("Globalink","Using Left Hand Layout: " + preferences.getBoolean("left_hand", true));
        }else{
        	setContentView(R.layout.activity_observation);
        	Log.i("Globalink","Using Right Hand Layout:" + preferences.getBoolean("left_hand", true));
        }
        
        tvAlone = (TextView)findViewById(R.id.tvSingleAdultCount);
        tvAdults = (TextView)findViewById(R.id.tvMultipleAdultCount);
        tvChild = (TextView)findViewById(R.id.tvChildCount);
        tvNone = (TextView)findViewById(R.id.tvNoSmokingCount);
        
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
        
        
        registerForContextMenu(btnNoSmoking);
        registerForContextMenu(btnNoOccupants);
        registerForContextMenu(btnOtherAdults);
        registerForContextMenu(btnChild);
        
		DatabaseHelper db = new DatabaseHelper(this);
		tvNone.setText(db.getNoSmokingCount(observationId)+"");
		tvChild.setText(db.getChildCount(observationId) + "");
		tvAdults.setText(db.getOtherAdultsSmokingCount(observationId) + "");
		tvAlone.setText(db.getLoneSmokerCount(observationId) + "");
		db.close();
	}
	
	/*
	 * Creates a context menu for the different buttons (no smoking, alone smoker etc..). Sets contextSelected
	 * so that the context handler knows what button the context menu is for.
	 */
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v == btnChild){
			menu.setHeaderTitle(R.string.context_child);
			contextSelected = CONTEXT_CHILD;
		}else if(v == btnOtherAdults){
			menu.setHeaderTitle(R.string.context_others);
			contextSelected = CONTEXT_ADULTS;
		}else if(v == btnNoOccupants){
			menu.setHeaderTitle(R.string.context_lone);
			contextSelected = CONTEXT_ALONE;
		}else if(v == btnNoSmoking){
			menu.setHeaderTitle(R.string.context_no_smoking);
			contextSelected = CONTEXT_NONE;
		}else{
			menu.setHeaderTitle(R.string.context_default);
			contextSelected = -1;
		}
		
		menu.add(0, v.getId(), 0, this.getString(R.string.subtract_one));
	}

	/* Decrements the count of a button. Based on the contextSelected variable set
	 * during the creation of the context menu.
	 */
	@Override  
	public boolean onContextItemSelected(MenuItem item) {
		try{
			DatabaseHelper db;
			switch(contextSelected){
			case CONTEXT_CHILD:
				db = new DatabaseHelper(this);
				if(db.getChildCount(observationId) > 0){
					db.decrementChild(observationId);
					tvChild.setText(db.getChildCount(observationId) + "");
				}
				db.close();			
				break;
			case CONTEXT_ADULTS:
				db = new DatabaseHelper(this);
				if(db.getOtherAdultsSmokingCount(observationId) > 0){
					db.decrementOtherAdults(observationId);
					tvAdults.setText(db.getOtherAdultsSmokingCount(observationId) + "");
				}
				db.close();		
				break;
			case CONTEXT_ALONE:
				db = new DatabaseHelper(this);
				if(db.getLoneSmokerCount(observationId) > 0){
					db.decrementLoneAdultSmoking(observationId);
					tvAlone.setText(db.getLoneSmokerCount(observationId) + "");
				}
				db.close();	
				break;
			case CONTEXT_NONE:
				db = new DatabaseHelper(this);
				if(db.getNoSmokingCount(observationId) > 0){
					db.decrementNoSmoking(observationId);
					tvNone.setText(db.getNoSmokingCount(observationId) + "");
				}
				db.close();	
				break;
			default:
				return false;
			}
			contextSelected = -1;
			return true;
		}catch(DatabaseException e){
			displayMessage(getString(R.string.failed_decrement));
			return false;
		}
	}
	
	/* Displays a simple 'toast' message
	 * 
	 */
	public void displayMessage(String message){
		Toast.makeText(getBaseContext(),message,Toast.LENGTH_SHORT).show();
	}
}
