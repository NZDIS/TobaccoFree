package org.nzdis;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class UploadActivity extends Activity implements OnClickListener, android.content.DialogInterface.OnClickListener, OnCancelListener {

	private Spinner spnObservations;
	private List<Observation> obs;
	private String[] names;
	private Button btnUploadSelected,btnUploadAll;
	private UsersDetails user;
	private boolean noDetails = true;
	private AlertDialog alert,non_finish_alert;
	private ProgressDialog upload_dialog;
	private Boolean displayingMessage = false;
	private UploadTask uploadTask;
	public static final int NO_MD5 = 10,NO_DETAILS = 11,NO_NET = 12,UPLOAD_PROGRESS = 13;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        
        // inflate layout items
        spnObservations = (Spinner)findViewById(R.id.spnUploadObservations);
        
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

        
        //check if an upload is in progress and if so, show dialog
        //and assign new activity (when a rotation has happened)
        Object retained = getLastNonConfigurationInstance();
        if(retained instanceof UploadTask){
        	uploadTask = (UploadTask) retained;
        	uploadTask.setActivity(this);
        }
    }
    
    
    @Override
	public Object onRetainNonConfigurationInstance(){
    	if(uploadTask != null){
    		uploadTask.setActivity(null);
	    	return (uploadTask == null) ? null : uploadTask;
    	}else{
    		return null;
    	}
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
			if(!isNetworkAvailable()){
				showDialog(NO_NET);
			}else{
				Observation[] output = new Observation[obs.size()];
				output = obs.toArray(output);
				uploadTask = new UploadTask(this);
				uploadTask.execute(output);
			}
			return;
		}
		
		if(v == btnUploadSelected){
			// uploaded selected one
			if(!isNetworkAvailable()){
				showDialog(NO_NET);
			}else{
				DatabaseHelper db = new DatabaseHelper(this);
				Observation selectedObservation = db.getObservation(obs.get(spnObservations.getSelectedItemPosition()).getId());
				db.close();
				uploadTask = new UploadTask(this);
				uploadTask.execute(selectedObservation);
			}
			return;
		}

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if(dialog == this.upload_dialog){
			uploadTask.cancel(false);
		}
		
	}


	private void onTaskCompleted(){
		//stops the uploadTask being re-created if the device is rotated again after completion/cancellation
		uploadTask = null;
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
			case NO_NET:
				non_finish_alert = new AlertDialog.Builder(this).create();
				non_finish_alert.setTitle(getString(R.string.error));
				non_finish_alert.setMessage(getString(R.string.no_internet));
				non_finish_alert.setButton(getString(R.string.ok), this);
				non_finish_alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return non_finish_alert;
			default:
				return null;
		}
	}
	
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
	private class UploadTask extends AsyncTask<Observation,Integer,Boolean>{

		private UploadActivity act;
		private boolean completed = false;
		private String currentMessage = "";
		private int total = 0;
		public UploadTask(UploadActivity act){
			this.act = act;
		}
		
		private void setActivity(UploadActivity act){			
    		if(act == null && upload_dialog != null){
    			upload_dialog.dismiss();
    			upload_dialog = null;
    		}
    		
    		this.act = act;

    		if(completed){
    			notifyActivityTaskCompleted();
    		}
    		
    		if(!completed && act != null){
    			upload_dialog = ProgressDialog.show(act, "",currentMessage, true);
    			upload_dialog.setCancelable(true);
    			upload_dialog.setOnCancelListener(UploadActivity.this);
    		} 
		}
		
    	private void notifyActivityTaskCompleted(){
    		if(null != act){
    			act.onTaskCompleted();
    		}
    	}
    	
		@Override
		protected Boolean doInBackground(Observation... arg0) {
			total = arg0.length;
			int count = 1;
			for(Observation observation : arg0){
				if(isCancelled()){
					return false;
				}
				publishProgress(count);
				count++;
				
				/***************************
				 * Upload code here
				 */
				try {
					Log.i("GlobaLink",observation.getJSON().toString());
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/**************************/
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress){
			currentMessage = act.getString(R.string.uploading_observation_number) + " " + progress[0] + " " + act.getString(R.string.uploading_observation_of) + " "  + total;
			upload_dialog.setMessage(currentMessage);
			
		}
		
		@Override
		protected void onCancelled(){
			if(upload_dialog != null){
				upload_dialog.dismiss();
			}
			act.onTaskCompleted();
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			completed = true;
			if(upload_dialog != null){
				upload_dialog.dismiss();
			}
		}
		
		@Override
		protected void onPreExecute(){
			Log.i("Globalink",user.toString());
			upload_dialog = new ProgressDialog(act);
			upload_dialog.setCancelable(true);
			upload_dialog.setOnCancelListener(act);
			upload_dialog.setMessage("Uploading");
			upload_dialog.show();
		}
	}

}
