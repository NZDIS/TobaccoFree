/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

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
import org.apache.http.conn.HttpHostConnectException;
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
import android.content.Intent;
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

/**
 * Represents activity responsible for uploading observation data to the server.
 * 
 * @author Hamish Medlin
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Jan 2012
 */
public class UploadActivity extends Activity 
	implements Constants, OnClickListener, OnCancelListener {

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
	private String uploadErrorResponse = "";
	public static final int 
		NO_MD5 = 10, 
		NO_DETAILS = 11, 
		NO_NET = 12, 
		UPLOAD_PROGRESS = 13, 
		UPLOAD_ERROR = 14, 
		MD5_ERROR = 15,
		UPLOAD_ERROR_RESPONSE = 16,
		UPLOAD_SUCCESS = 100;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        
        // inflate layout items
        spnObservations = (Spinner) findViewById(R.id.spnUploadObservations);
        
        btnUploadSelected = (Button) findViewById(R.id.btnUploadObservation);
        btnUploadAll = (Button) findViewById(R.id.btnUploadAll);
        
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

        // setup spinner etc.
        updateView();
        
        //check if an upload is in progress and if so, show dialog
        //and assign new activity (when a rotation has happened)
        Object retained = getLastNonConfigurationInstance();
        if(retained instanceof UploadTask){
        	uploadTask = (UploadTask) retained;
        	uploadTask.setActivity(this);
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	updateView();
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
        if (names.length == 0 || noDetails) {
        	spnObservations.setEnabled(false);
        	btnUploadSelected.setEnabled(false);
        	btnUploadAll.setEnabled(false);
        }
    }
    
    @Override
	public Object onRetainNonConfigurationInstance(){
    	if (uploadTask != null) {
    		uploadTask.setActivity(null);
	    	return (uploadTask == null) ? null : uploadTask;
    	} else {
    		return null;
    	}
    }
    	
	@Override
	public void onClick(View v) {
		if (v == btnUploadAll) {
			// upload all
			if (!isNetworkAvailable()) {
				showDialog(NO_NET);
			} else {
				Observation[] output = new Observation[obs.size()];
				output = obs.toArray(output);
				uploadTask = new UploadTask(this);
				uploadTask.execute(output);
			}
			return;
		}
		
		if (v == btnUploadSelected) {
			// uploaded selected one
			if (!isNetworkAvailable()) {
				showDialog(NO_NET);
			} else {
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
		if(dialog == this.upload_dialog) {
			uploadTask.cancel(false);
		}
		
	}


	private void onTaskCompleted(){
		//stops the uploadTask being re-created if the device is rotated again after completion/cancellation
		uploadTask = null;
		updateView();
	}
	
	@Override
	protected Dialog onCreateDialog(int d) {
		final DialogInterface.OnClickListener doNothingListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		};
		switch (d) {
			case UPLOAD_SUCCESS:
				alert = new AlertDialog.Builder(this).create();
		    	alert.setTitle(getString(R.string.success_title));
		    	alert.setMessage(getString(R.string.upload_success));
		    	alert.setButton(getString(R.string.ok), doNothingListener);
		    	alert.setIcon(android.R.drawable.ic_dialog_info);
				return alert;
			case NO_MD5:
				alert = new AlertDialog.Builder(this).create();
		    	alert.setTitle(getString(R.string.error));
		    	alert.setMessage(getString(R.string.no_md5));
		    	alert.setButton(getString(R.string.ok), doNothingListener);
		    	alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return alert;
			case NO_DETAILS:
				alert = new AlertDialog.Builder(this).create();
		    	alert.setTitle(getString(R.string.error));
		    	alert.setMessage(getString(R.string.no_username_set));
		    	alert.setButton(getString(R.string.ok), new DialogInterface.OnClickListener (){
		    		@Override
					public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			final Intent i = new Intent(UploadActivity.this, PreferencesActivity.class);
		    			startActivity(i);
		    		}
				});
		    	alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return alert;
			case NO_NET:
				non_finish_alert = new AlertDialog.Builder(this).create();
				non_finish_alert.setTitle(getString(R.string.error));
				non_finish_alert.setMessage(getString(R.string.no_internet));
				non_finish_alert.setButton(getString(R.string.ok), doNothingListener);
				non_finish_alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return non_finish_alert;
			case UPLOAD_ERROR:
				upload_failed_alert = new AlertDialog.Builder(this).create();
				upload_failed_alert.setTitle(getString(R.string.error));
				upload_failed_alert.setMessage(getString(R.string.upload_error));
				upload_failed_alert.setButton(getString(R.string.ok), doNothingListener);
				upload_failed_alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return upload_failed_alert;
			case UPLOAD_ERROR_RESPONSE:
				upload_failed_alert = new AlertDialog.Builder(this).create();
				upload_failed_alert.setTitle(getString(R.string.error));
				upload_failed_alert.setMessage(uploadErrorResponse);
				upload_failed_alert.setButton(getString(R.string.ok), doNothingListener);
				upload_failed_alert.setIcon(android.R.drawable.ic_dialog_alert);	
		    	return upload_failed_alert;
			default:
				return null;
		}
	}
	
	/** 
	 * Checks if there is an Internet connection. 
	 * @return <code>true</code> if there is Internet connection, 
	 * 		<code>false</code> otherwise.
	 **/
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    /** 
     * Returns a unique device ID. 
     *@return unique device ID */
	private String getDeviceID() {
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
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
	        		temp.put(USER_DEVICE, deviceID);
	        		temp.put(USER_USER_EMAIL, user.getUserEmail());
	        		temp.put(USER_PASSWORD_HASH, user.getPasswordHash());
					final HttpClient client = new DefaultHttpClient();
		        	final HttpPost post = new HttpPost(URL_OBSERVATION_ADD);
					
		        	List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("Observation", temp.toString()));
		        	UrlEncodedFormEntity ent;
		        	ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);			
		            post.setEntity(ent);
		            final HttpResponse response = client.execute(post);
					final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();
					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
					}
					if (response.getStatusLine().getStatusCode() == 200) {
						Log.i("Globalink","Data saved correctly to server.");						
						// if upload succeeded then set 'upload' tag to 1
			        	db = new DatabaseHelper(act);
						db.setUploaded(observation.getId());
						db.close();
					} else {
						// TODO Debugging printout: Prints out response from server
						Log.i("Globalink","ERROR: status line " + response.getStatusLine() + " from server.\nResponse: " + s);
						errored = true;
						uploadErrorResponse = s.toString();
						errorCode = UPLOAD_ERROR_RESPONSE;
						return false;
					}
				} catch (JSONException e) {
					e.printStackTrace();
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
				} catch(HttpHostConnectException e) {
					// something wrong on the server side
					// tell user
					errored = true;
					errorCode = 4;
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
				case UPLOAD_ERROR_RESPONSE:
					showDialog(UPLOAD_ERROR_RESPONSE);
					break;
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
			} else {
				showDialog(UPLOAD_SUCCESS);
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
