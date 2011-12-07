package org.nzdis;

import android.location.Location;

public class Observation {
	
	private int noSmoking = 0,child = 0, other = 0, noOthers = 0;
	private Location location;
	private long start,finish;
	private boolean started = false;
	private long id = -1;
	
	public Observation(long start){
		this.start = start;
		location = new Location("empty");
	}

	public String toString(){
		return "Total: " + (noSmoking + child + other + noOthers);
	}
	/* Incrementers */
	public void incrementNoSmoking(){
		noSmoking++;
	}
	
	public void incrementChild(){
		child++;
	}
	
	public void incrementOther(){
		other++;
	}
	
	public void incrementNoOthers(){
		noOthers++;
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

	public int getOther() {
		return other;
	}

	public void setOther(int other) {
		if(other < 0){
			this.other = 0;
		}
		this.other = other;
	}

	public int getNoOthers() {
		return noOthers;
	}

	public void setNoOthers(int noOthers) {
		if(noOthers < 0){
			this.noOthers = 0;
		}
		this.noOthers = noOthers;
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
		return (noSmoking + child + other + noOthers);
	}

	public String getCSV() {
		return location.getLatitude() + "," + location.getLongitude() + "," + start + "," + finish + "," + noSmoking + "," + other + "," + noOthers + "," + child + "\n";
	}
}
