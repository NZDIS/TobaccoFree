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
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Represents the top level activity for the smoking in cars logger.
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Jan 13, 2012 12:48:02 PM
 */
public class GlobalinkActivity extends Activity 
	implements Constants, OnClickListener {
	
	private Button btnNew,btnExisting,btnPreferences,btnUpload;

	
	private UsersDetails user;
	private AlertDialog alert,non_finish_alert,upload_failed_alert;
	private ProgressDialog upload_dialog;
	private Boolean displayingMessage = false;
	private UploadTask uploadTask;
	private String uploadErrorResponse = "";
	public static final int 
		GPS_DIALOG = 2,
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
        setContentView(R.layout.activity_main);
        
        btnNew = (Button)findViewById(R.id.btnNew);
        btnExisting = (Button)findViewById(R.id.btnExisting);
        btnPreferences = (Button)findViewById(R.id.btnPreferences);
        btnUpload = (Button)findViewById(R.id.btnUpload);
        
        btnNew.setOnClickListener(this);
        btnExisting.setOnClickListener(this);
        btnPreferences.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        
		final DatabaseHelper db = new DatabaseHelper(this);
		try {
			db.getUserDetails();
		} catch (Exception e) {
			final Intent signup = new Intent(this, ObserverAccountActivity.class);
			startActivity(signup);
		}
		checkUploadButtonStatus();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	checkUploadButtonStatus();
    }
    
    private void checkUploadButtonStatus() {
    	final DatabaseHelper db = new DatabaseHelper(this);
		List<Observation> obs = db.getObservationsNotUploaded();
		if (obs.size()>0) {
			btnUpload.setEnabled(true);
		} else {
			btnUpload.setEnabled(false);
		}
		db.close();
    }

	@Override
	public void onClick(View arg0) {
		if (arg0 == btnNew) {			
			//check if gps is enabled
			LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				//start new observation
				Intent i = new Intent(this, ObservationActivity.class);
				startActivity(i);			
			} else {
				//display gps notification
				showDialog(GPS_DIALOG);
			}
			return;
		}
		
		if (arg0 == btnExisting) {
		 	Intent i = new Intent(this,ViewObservationsActivity.class);
			startActivity(i);
			return;
		}
		
		if (arg0 == btnPreferences) {
	        Intent i = new Intent(this,PreferencesActivity.class);
	        startActivity(i);
	        return;
		}
		
		if (arg0 == btnUpload) {
	        uploadData();
	        return;
		}
	}
	

	
	@Override
	protected Dialog onCreateDialog(int d) {
		final DialogInterface.OnClickListener doNothingListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				displayingMessage = false;
			}
		};
		switch (d) {
		case GPS_DIALOG:
				alert = new AlertDialog.Builder(this).create();
				alert.setIcon(android.R.drawable.ic_menu_mylocation);
				alert.setTitle(getString(R.string.enable_gps));
				alert.setMessage(getString(R.string.gps_error_gps));
				alert.setButton(getString(R.string.ok), new DialogInterface.OnClickListener (){
		    		@Override
					public void onClick(DialogInterface dialog, int which) {
		    			dialog.dismiss();
		    			displayingMessage = false;
		           	   	Intent showGPSSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		           	   	startActivity(showGPSSettings);
		    		}});
				return alert;
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
		    			displayingMessage = false;
		    			final Intent i = new Intent(GlobalinkActivity.this, ObserverAccountActivity.class);
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
				if(uploadErrorResponse.length()>5) {
					upload_failed_alert.setMessage(uploadErrorResponse);
				} else {
					upload_failed_alert.setMessage(getString(R.string.upload_error));
				}
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
	

	private void onTaskCompleted(){
		//stops the uploadTask being re-created if the device is rotated again after completion/cancellation
		uploadTask = null;
		checkUploadButtonStatus();
	}
	
	
	private void uploadData() { 
        if (!isNetworkAvailable()) {
			showDialog(NO_NET);
			return;
		}
        
        final DatabaseHelper db = new DatabaseHelper(this);
        try {
			user = db.getUserDetails();
		} catch (NoSuchAlgorithmException e) {
			// display error message
			if(!displayingMessage){
				displayingMessage = true;
				showDialog(NO_MD5);
				e.printStackTrace();
				return;
			}
		} catch (UsernameNotSetException e) {
			if(!displayingMessage){
				displayingMessage = true;
				showDialog(NO_DETAILS);
				return;
			}
		}
        
        // Upload all the observations in an aggregated format
        List<Observation> observationsForUpload = db.getObservationsNotUploaded();
		uploadTask = new UploadTask();
		Observation[] obs = observationsForUpload.toArray(new Observation[observationsForUpload.size()]);
		uploadTask.execute(obs);
	}
	
	
	
	
	
	private class UploadTask extends AsyncTask<Observation,Integer,Boolean>{

		private String currentMessage = "";
		private int total = 0;
		private boolean errored = false;
		private int errorCode = -1;
		
		
	
    	
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
				
				/*************************************************
				 * Upload aggregated data together with details of 
				 * all observations (category + timestamp list)
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
// TODO debugging
// Log.i("Globalink","Got from server:" + s);
					if (response.getStatusLine().getStatusCode() == 200) {
// Log.i("Globalink","Data saved correctly to server.");						
						// if upload succeeded then set 'upload' tag to 1
			        	db = new DatabaseHelper(GlobalinkActivity.this);
						db.setUploaded(observation.getId());
						db.close();
					} else {
						// TODO Debugging printout: Prints out response from server
// Log.i("Globalink","ERROR: status line " + response.getStatusLine() + " from server.\nResponse: " + s);
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
			currentMessage = 
					GlobalinkActivity.this.getString(R.string.uploading_observation_number) + " " 
							+ progress[0] + " " 
							+ GlobalinkActivity.this.getString(R.string.uploading_observation_of) 
							+ " "  + total;
			upload_dialog.setMessage(currentMessage);
			
		}
		
		@Override
		protected void onCancelled(){
			if(upload_dialog != null){
				upload_dialog.dismiss();
			}
			GlobalinkActivity.this.onTaskCompleted();
		}
		
		@Override
		protected void onPostExecute(Boolean result){
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
			GlobalinkActivity.this.onTaskCompleted();
		}
		
		@Override
		protected void onPreExecute(){
			Log.i("Globalink", user.toString());
			upload_dialog = new ProgressDialog(GlobalinkActivity.this);
			upload_dialog.setCancelable(true);
			upload_dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if(dialog == upload_dialog) {
						uploadTask.cancel(false);
					}
				}
			});
			upload_dialog.setMessage("Uploading");
			upload_dialog.show();
		}
	}
	
}