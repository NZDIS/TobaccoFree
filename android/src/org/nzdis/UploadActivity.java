package org.nzdis;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

public class UploadActivity extends Activity implements OnClickListener, OnItemSelectedListener, android.content.DialogInterface.OnClickListener {

	private Spinner spnObservations;
	private List<Observation> obs;
	private String[] names;
	private Button btnUploadSelected,btnUploadAll;
	private UsersDetails user;
	private boolean noDetails = true;
	private AlertDialog alert;
	private Boolean displayingMessage = false;
	public static final int NO_MD5 = 10,NO_DETAILS = 11;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        
        if(getLastNonConfigurationInstance() != null){
        	if(getLastNonConfigurationInstance() instanceof Boolean){
        		displayingMessage = (Boolean)getLastNonConfigurationInstance();
        	}
        }
        
        // inflate layout items
        spnObservations = (Spinner)findViewById(R.id.spnUploadObservations);
        spnObservations.setOnItemSelectedListener(this);
        
        btnUploadSelected = (Button)findViewById(R.id.btnUploadObservation);
        btnUploadAll = (Button)findViewById(R.id.btnUploadAll);
        
        btnUploadSelected.setOnClickListener(this);
        btnUploadAll.setOnClickListener(this);
        
        //get observations from database. And the users details 
        DatabaseHelper db = new DatabaseHelper(this);
        obs = db.getObservations();
        try {
			user = db.getUserDetails();
			noDetails = false;
		} catch (NoSuchAlgorithmException e) {
			// display error message
			if(!displayingMessage){
				displayingMessage = true;
				showDialog(NO_MD5);
			}
			e.printStackTrace();
		} catch (UsernameNotSetException e) {
			if(!displayingMessage){
				displayingMessage = true;
				showDialog(NO_DETAILS);
			}
		}
        db.close();
        
        
        //add to spinner
        names = new String[obs.size()];
        ArrayAdapter <String> adapter = new ArrayAdapter <String> (this, android.R.layout.simple_spinner_item,names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnObservations.setAdapter(adapter);
        
        for(int i = 0;i < names.length;i++){
        	names[i] = DateFormat.format("dd MMMM, yyyy h:mmaa",obs.get(i).getStart()).toString();
        }
        
        
        //disable views if there are no observations
        if(names.length == 0 || noDetails){
        	spnObservations.setEnabled(false);
        	btnUploadSelected.setEnabled(false);
        	btnUploadAll.setEnabled(false);
        }

        
    }
    
    
    @Override
	public Object onRetainNonConfigurationInstance(){
    	return displayingMessage;
    }
    
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(dialog == alert){
			alert.dismiss();
			
			//ignore this for now
			//finish();
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if(v == btnUploadAll){
			// upload all
			return;
		}
		
		if(v == btnUploadSelected){
			// uploaded selected one
			return;
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Dialog onCreateDialog(int d){
		switch(d){
			case NO_MD5:
				alert = new AlertDialog.Builder(this).create();
		    	alert.setTitle(getString(R.string.error));
		    	alert.setMessage(getString(R.string.no_md5));
		    	alert.setButton(getString(R.string.ok), this);
		    	alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return alert;
			case NO_DETAILS:
				alert = new AlertDialog.Builder(this).create();
		    	alert.setTitle(getString(R.string.error));
		    	alert.setMessage(getString(R.string.no_username_set));
		    	alert.setButton(getString(R.string.ok), this);
		    	alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return alert;
			default:
				return null;
		}
	}
	
	private class UploadTask extends AsyncTask<String,Integer,Boolean>{

		@Override
		protected Boolean doInBackground(String... arg0) {
			int count = 1;
			for(String jsonString : arg0){
				//post query etc...
				publishProgress(count);
				count++;
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress){
			
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			
		}
		
		@Override
		protected void onPreExecute(){
			
		}
	}
}
