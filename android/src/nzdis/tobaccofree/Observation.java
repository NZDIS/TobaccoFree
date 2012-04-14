/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.text.format.DateFormat;

/**
 * Represents a single observation.
 * 
 * @author Hamish Medlin
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Dec 2011
 */
public class Observation {
	
	private int noSmoking = 0, child = 0, otherAdults = 0, loneAdult = 0;
	private Location location;
	private long start, finish;
	private boolean started = false;
	private long id = -1;
	
	private List<Detail> details = new ArrayList<Detail>(); 
	
	
	
	public Observation(long start){
		this.start = start;
		location = new Location("empty");
	}

	public String toString(){
		return "Total: " + (noSmoking + child + otherAdults + loneAdult);
	}
	
	
	
	public void addDetail(long timestamp, int category) {
		this.details.add(new Detail(timestamp, category));
	}
	
	
	
	/* Setters and getters */
	public int getNoSmoking() {
		return noSmoking;
	}

	public void setNoSmoking(int noSmoking) {
		if(noSmoking < 0){
			this.noSmoking = 0;
		}
		this.noSmoking = noSmoking;
	}

	public int getChild() {
		return child;
	}

	public void setChild(int child) {
		if(child < 0){
			this.child = 0;
		}
		this.child = child;
	}

	public int getOtherAdults() {
		return otherAdults;
	}

	public void setOtherAdults(int a_otherAduls) {
		if(a_otherAduls < 0){
			this.otherAdults = 0;
		}
		this.otherAdults = a_otherAduls;
	}

	public int getLoneAdult() {
		return loneAdult;
	}

	public void setLoneAdult(int a_loneAdult) {
		if(a_loneAdult < 0){
			this.loneAdult = 0;
		}
		this.loneAdult = a_loneAdult;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public long getStart() {
		return start;
	}
	
	public void setStart(long start) {
		if(start < 0){
			this.start = 0;
		}
		this.start = start;
	}
	
	public long getFinish() {
		return finish;
	}
	
	public void setFinish(long finish) {
		if(finish < 0){
			this.finish = 0;
		}
		this.finish = finish;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getTotal() {
		return (noSmoking + child + otherAdults + loneAdult);
	}

	public String getCSV() {
		return location.getLatitude() + "," 
				+ location.getLongitude() + "," 
				+ DateFormat.format("dd MMMM, yyyy h:mmaa",start).toString() + "," 
				+ DateFormat.format("dd MMMM, yyyy h:mmaa",finish).toString() + "," 
				+ noSmoking + "," 
				+ otherAdults + "," 
				+ loneAdult + "," 
				+ child + "\n";
	}
	
	/** 
	 * Creates a JSON representation of this class. 
	 * @return JSON representation of this observation.
	 **/
	public JSONObject getJSON() throws JSONException, NoSuchAlgorithmException{
		final JSONObject json = new JSONObject();
		json.put("version", Constants.CURRENT_PROTOCOL_VERSION);
		json.put("latitude", String.valueOf(location.getLatitude()));
		json.put("longitude", String.valueOf(location.getLongitude()));
		json.put("start", start);
		json.put("finish", finish);
		json.put("no_smoking", noSmoking);
		json.put("other_adults", otherAdults);
		json.put("lone_adult", loneAdult);
		json.put("child", child);
		json.put("hash", getHash());
		json.put("details", getDetailsJSONArray());
		return json;
	}
	
	private JSONArray getDetailsJSONArray() throws JSONException {
		final JSONArray json_arr = new JSONArray();
		for (Detail d : this.details) {
			final JSONObject json = new JSONObject();
			json.put("timestamp", d.timestamp);
			json.put("smoking_id", d.smoking_id);
			json_arr.put(json);
		}
		return json_arr;
	}
	
	/** 
	 * Creates a hash of all of the values stored in this instance. 
	 * Should be considered unique. Used for verification of recording 
	 * and syncing between phone and server data.
	 *  
	 * @return a unique hash of all the values of this observation instance.*/
	public String getHash() throws NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		final String hashString = location.getLatitude() + location.getLongitude() 
				+ "salt" + finish + start 
				+ noSmoking + otherAdults + loneAdult + child;
		
		md.update(hashString.getBytes());
		final byte hash[] = md.digest();
		
		StringBuffer hex = new StringBuffer();
		for(int i = 0; i < hash.length; i++) {
			hex.append(Integer.toHexString(0xFF & hash[i]));
		}
		return hex.toString();
	}
	
	
	public class Detail {
		public long timestamp;
		public int smoking_id;
		
		public Detail(long timestamp, int type) {
			this.timestamp = timestamp;
			this.smoking_id = type;
		}
	}
	
}
