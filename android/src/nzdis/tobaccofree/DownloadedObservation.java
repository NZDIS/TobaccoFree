/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import android.util.Log;

/**
 * Represents a single downloaded observation.
 * 
 * @author Hamish Medlin <android@hamishmedlin.com>
 *
 * @version $Revision$ <br>
 * Created: Apr 2012
 */

public class DownloadedObservation {
	
	// smoking stats
	private int adult = 0,adults = 0,child = 0,none = 0;
	
	private int duration;
	private double lat,lng;
	
	private String country= "",city = "",start = "",finish = "",uri = "";
	
	public DownloadedObservation(){}
	
	public DownloadedObservation(JSONObject observation) throws JSONException{
		
		adult = observation.getInt("lone_adult");
		adults = observation.getInt("other_adults");
		child = observation.getInt("child");
		none = observation.getInt("no_smoking");		
		duration = observation.getInt("duration");
		lat = observation.getDouble("latitude");
		lng = observation.getDouble("longitude");
		
		country = observation.getString("country");
		city = observation.getString("city");
		start = observation.getString("start");
		finish = observation.getString("finish");
		uri = observation.getString("resource_uri");
		
		
	}
	
	/**
	 * Gets a GeoPoint from this observation
	 * @return observation GeoPoint
	 */
	public GeoPoint getGeoPoint() {		
		return new GeoPoint((int)(lat * 1e6),(int)(lng * 1e6));
	}
	
	/**
	 * Get the total number of vehicles
	 * @return total number of vehicles
	 */
	public int getTotal(){
		return (adult + adults + child + none);
	}
	
	/**
	 * Get the total number of smokers in vehicles
	 * @return total number of smokers
	 */
	public int getTotalSmokers(){
		return (adult + adults + child);
	}
	
	/* Setters and getters */
	
	public int getAdult() {
		return adult;
	}

	public void setAdult(int adult) {
		if(adult < 0){
			adult = 0;
		}else{
			this.adult = adult;
		}
	}

	public int getAdults() {
		return adults;
	}

	public void setAdults(int adults) {
		if(adults < 0){
			adults = 0;
		}else{
			this.adults = adults;
		}
	}

	public int getChild() {
		return child;
	}

	public void setChild(int child) {
		if(child < 0){
			child = 0;
		}else{
			this.child = child;
		}
	}

	public int getNoSmokers() {
		return none;
	}

	public void setNoSmokers(int none) {
		if(none < 0){
			none = 0;
		}else{
			this.none = none;
		}
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		if(duration < 0){
			duration = 0;
		}else{
			this.duration = duration;
		}
	}

	public double getLatitude() {
		return lat;
	}

	public void setLatitude(double lat) {
		this.lat = lat;
	}

	public double getLongitude() {
		return lng;
	}

	public void setLongitude(double lng) {
		this.lng = lng;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getFinish() {
		return finish;
	}

	public void setFinish(String finish) {
		this.finish = finish;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
