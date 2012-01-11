package org.nzdis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.telephony.TelephonyManager;
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
	private AlertDialog alert,non_finish_alert,upload_failed_alert;
	private ProgressDialog upload_dialog;
	private Boolean displayingMessage = false;
	private UploadTask uploadTask;
	public static final int NO_MD5 = 10,NO_DETAILS = 11,NO_NET = 12,UPLOAD_PROGRESS = 13,UPLOAD_ERROR = 14,MD5_ERROR = 15;
	
	
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

        // setup spinner etc..
        updateView();
        
        //check if an upload is in progress and if so, show dialog
        //and assign new activity (when a rotation has happened)
        Object retained = getLastNonConfigurationInstance();
        if(retained instanceof UploadTask){
        	uploadTask = (UploadTask) retained;
        	uploadTask.setActivity(this);
        }
    }
    
    private void updateView(){
    	DatabaseHelper db = new DatabaseHelper(this);
        obs = db.getObservationsNotUploaded();
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
		updateView();
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
			case UPLOAD_ERROR:
				upload_failed_alert = new AlertDialog.Builder(this).create();
				upload_failed_alert.setTitle(getString(R.string.error));
				upload_failed_alert.setMessage(getString(R.string.upload_error));
				upload_failed_alert.setButton(getString(R.string.ok), this);
				upload_failed_alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return upload_failed_alert;
			default:
				return null;
		}
	}
	
	/* Check if there is a internet connection */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    /* Code to get a unique device ID */
	private String getDeviceID(){
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        Log.i("LENGTH",deviceUuid.toString().length() + "");
        return deviceUuid.toString().toUpperCase();		
	}
	
	private class UploadTask extends AsyncTask<Observation,Integer,Boolean>{

		private UploadActivity act;
		private boolean completed = false;
		private String currentMessage = "";
		private int total = 0;
		private boolean errored = false;
		private int errorCode = -1;
		
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
			String deviceID = getDeviceID();
			total = arg0.length;
			int count = 1;
			DatabaseHelper db;
			JSONObject temp;
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
	        		temp = observation.getJSON();
	        		temp.put("device", deviceID);
	        		temp.put("username", user.getUsername());
	        		temp.put("passwordHash", user.getPasswordHash());
					HttpClient client = new DefaultHttpClient();
		        	HttpPost post = new HttpPost("http://globalink.nzdis.org/observation/add");
					//HttpPost post = new HttpPost("http://www.hamishmedlin.com/upload.php");
		        	List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("JSON",temp.toString()));
		        	UrlEncodedFormEntity ent;
		        	ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);			
		            post.setEntity(ent);
		            HttpResponse response = client.execute(post);
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();
					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
					}
					
					// Prints out response from server
		            Log.i("Globalink","Response: " + s);
				} catch (JSONException e1) {
					e1.printStackTrace();
					errored = true;
					errorCode = 1;
					return false;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					errored = true;
					errorCode = 2;
					return false;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					errored = true;
					errorCode = 3;
					return false;
				} catch (IOException e) {					
					e.printStackTrace();
					errored = true;
					errorCode = 4;
					return false;
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					errored = true;
					errorCode = 5;
					return false;
				}
	        	
				
				/******************************
				 * Simple offline testing code
				 *
				try {
					Log.i("GlobaLink",observation.getJSON().toString());
					Thread.sleep(5000);
					
					//Error message test
					//JSONObject json = new JSONObject("Asda2%");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					errored = true;
					errorCode = 1;
					return false;
				}
				/*****************************/
				
				//if upload succeeded then set 'upload' tag to 1
				db = new DatabaseHelper(act);
				db.setUploaded(observation.getId());
				db.close();
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
			
			if(!result && errored){
				//Each exception type. Could be customised
				switch(errorCode){
				case 1:
					//JSON error
					showDialog(UPLOAD_ERROR);
					break;
				case 2:
					//Unsupported Encoding
					showDialog(UPLOAD_ERROR);
					break;
				case 3:
					//ClientProto exception
					showDialog(UPLOAD_ERROR);
					break;
				case 4:
					//IOException
					showDialog(UPLOAD_ERROR);
					break;
				case -1:
				default:
					break;
				}
			}
			act.onTaskCompleted();
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
