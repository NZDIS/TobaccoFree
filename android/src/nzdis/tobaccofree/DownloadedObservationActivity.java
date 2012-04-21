/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * Displays current observations on that have been uploaded to the Website
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Apr 2012
 */
public class DownloadedObservationActivity extends Activity implements OnClickListener, OnItemClickListener {

	private DatabaseHelper db;
	private Button btnUpdate,btnViewMap;
	private DownloadTask task;
	private ListView lv;
	private DownloadedObservationListAdapter adapter;
	private ObservationStat[] countries,cities;
	private boolean country = true;
	private String currentCountry = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState != null){
        	country = savedInstanceState.getBoolean("country",true);
        	currentCountry = savedInstanceState.getString("current");
        }
        
        setContentView(R.layout.activity_downloaded_observations);
        db = new DatabaseHelper(this);
        
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnViewMap = (Button)findViewById(R.id.btnViewMap);
        
        btnUpdate.setOnClickListener(this);
        btnViewMap.setOnClickListener(this);
        
        lv = (ListView)findViewById(R.id.lvObservationList);        
        
        if(!country && currentCountry != null){
        	cities = db.getDownloadedCityStatsForCountry(currentCountry);
        	adapter = new DownloadedObservationListAdapter(this,R.layout.list_item_stats,cities);        	
        }else{
        	countries = db.getDownloadedCountryStats();
        	adapter = new DownloadedObservationListAdapter(this,R.layout.list_item_stats,countries);
        }
        
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        db.close();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {    	
    	savedInstanceState.putBoolean("country", country);
    	savedInstanceState.putString("current", currentCountry);
    	super.onSaveInstanceState(savedInstanceState);
    }
    
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {		
		if(country){
			//show country stats
			db = new DatabaseHelper(this);
			cities = db.getDownloadedCityStatsForCountry(countries[arg2].getTopText());
			db.close();
			adapter = new DownloadedObservationListAdapter(this,R.layout.list_item_stats,cities);
			lv.setAdapter(adapter);		
			country = false;
			currentCountry = countries[arg2].getTopText();
		}else{
			//check if to go back to countries
			if(arg2 == 0){
				//back button
				if(countries == null){
					//would be null after a rotation
					db = new DatabaseHelper(this);
					countries = db.getDownloadedCountryStats();
					db.close();
				}
				adapter = new DownloadedObservationListAdapter(this,R.layout.list_item_stats,countries);
				lv.setAdapter(adapter);	
				country = true;
				currentCountry = null;
			}else{
				//clicked on a city name, maybe show on map?
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if(v == btnUpdate){
			if(!isNetworkAvailable()){
				AlertDialog non_finish_alert = new AlertDialog.Builder(this).create();
				non_finish_alert.setTitle(getString(R.string.error));
				non_finish_alert.setMessage(getString(R.string.no_internet_update));
				non_finish_alert.setButton(getString(R.string.ok), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
				non_finish_alert.setIcon(android.R.drawable.ic_dialog_alert);
				non_finish_alert.show();
			}else{
				btnUpdate.setEnabled(false);
				btnUpdate.setText(getString(R.string.updating));
				db = new DatabaseHelper(this);
				task = new DownloadTask(db);
		        task.execute();
			}
		}
		
		if(v == btnViewMap){
			if(task != null){
				task.cancel(true);
			}
			Intent i = new Intent(this, DownloadedObservationMap.class);
			startActivity(i);	
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
	 * Stop the async task before destruction of activity
	 */
	@Override
	public void onStop() {
		if(task != null){
			task.cancel(true);
		}
		super.onStop();
	}
	
	/**
	 * Updates the listview to the newly downloaded stats
	 */
	private void updateList(){
		countries = db.getDownloadedCountryStats();
    	adapter = new DownloadedObservationListAdapter(this,R.layout.list_item_stats,countries);
    	lv.setAdapter(adapter);
    	country = true;
    	currentCountry = null;
	}
	
    private class DownloadTask extends AsyncTask<Void,Integer,Boolean>{

    	private DatabaseHelper db;
    	private List<DownloadedObservation> observations;
    	
    	public DownloadTask(DatabaseHelper db){
    		this.db = db;
    	}
    	
    	@Override
    	protected void onPostExecute(Boolean result){    		
    		if(result){
    			if(observations != null || observations.size() > 0){
    				db.insertDownloadedObservations(observations);
    			}
    			//update view here
    			updateList();
    		}
    		
    		btnUpdate.setEnabled(true);
    		btnUpdate.setText(getString(R.string.update_stats));
    		db.close();
    	}
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			JSONObject js;
			JSONObject meta;
			JSONArray objects;
			observations = new ArrayList<DownloadedObservation>();			
			try {
				js = getObservation("http://tobaccofree.nzdis.org/api/v1/observation/?format=json&limit=50");
				meta = js.getJSONObject("meta");
				objects = js.getJSONArray("objects");
				for(int i = 0;i < objects.length();i++){
					if(this.isCancelled()){
						return false;
					}
					observations.add(new DownloadedObservation(objects.getJSONObject(i)));
				}
				
				while(!meta.getString("next").equalsIgnoreCase("null")){
					if(this.isCancelled()){
						return false;
					}
					js = getObservation("http://tobaccofree.nzdis.org" + meta.getString("next"));
					meta = js.getJSONObject("meta");
					objects = js.getJSONArray("objects");
					for(int i = 0;i < objects.length();i++){
						if(this.isCancelled()){
							return false;
						}
						observations.add(new DownloadedObservation(objects.getJSONObject(i)));
					}
				}
				
				Log.i("Observations",observations.size() + "");
			}catch(MalformedURLException e){
				e.printStackTrace();
				return false;
			}catch(IOException e){
				e.printStackTrace();
				return false;
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}	
			
			return true;
		}
    	
		private JSONObject getObservation(String url) throws MalformedURLException, IOException, JSONException{
			InputStream is = (InputStream)fetch(url);
			StringBuffer buf = new StringBuffer();
			int i = -1;
			while((i = is.read()) > 0){
				buf.append((char)i);
			}				
			return new JSONObject(buf.toString());
		}
		
		private Object fetch(String address) throws MalformedURLException,IOException{
			URL url = new URL(address);
			Object content = url.getContent();
			return content;
		}
    }

}
