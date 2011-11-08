package org.nzdis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GlobalinkActivity extends Activity implements OnClickListener {
	
	private Button btnNew,btnExisting;
	public static final int GPS_DIALOG = 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnNew = (Button)findViewById(R.id.btnNew);
        btnExisting = (Button)findViewById(R.id.btnExisting);
        
        btnNew.setOnClickListener(this);
        btnExisting.setOnClickListener(this);
        
    }

	@Override
	public void onClick(View arg0) {
		if(arg0 == btnNew){			
			//check if gps is enabled
			LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				//start new observation
				Intent i = new Intent(this,ObservationActivity.class);
				startActivity(i);			
			}else{
				onCreateDialog(GPS_DIALOG);
			}
			return;
		}
		
		if(arg0 == btnExisting){
			//show exiting observations activity
			return;
		}
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case GPS_DIALOG:
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setIcon(android.R.drawable.ic_menu_mylocation);
				alert.setTitle(getString(R.string.enable_gps));
				alert.setMessage(getString(R.string.gps_error_gps));
				
				alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Intent showGPSSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			        	   startActivity(showGPSSettings);
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
}