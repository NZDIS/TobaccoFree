package org.nzdis;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "globalink.sqlite";
	public static final int DATABASE_VESRION = 1;
	
	//observation table
	public static final String TABLE_OBSERVATION = "observations";
	public static final String OBSERVATION_ID = "id";
	public static final String OBSERVATION_LATITUDE = "latitude";
	public static final String OBSERVATION_LONGITUDE = "longitude";
	public static final String OBSERVATION_START = "start_time";
	public static final String OBSERVATION_FINISH = "finish_time";
	
	//observation details table
	public static final String TABLE_DETAILS = "details";
	public static final String DETAILS_ID = "observation_id";
	public static final String DETAILS_TYPE = "observation_type";
	public static final String DETAILS_TIMESTAMP = "obeservation_time";
	
	//detail type table
	public static final String TABLE_TYPES = "types";
	public static final String TYPES_ID = "id";
	public static final String TYPES_NAME = "name";
	
	
	//type contants
	public static final String NO_SMOKING = "No Smokers";
	public static final String ADULT_SMOKING = "Adult with no other occupants";
	public static final String ADULT_SMOKING_OTHERS = "Adult with other smoking adults";
	public static final String ADULT_SMOKING_WITH_CHILD = "Smoking with child <= 12";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VESRION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createObservation = "CREATE TABLE " + TABLE_OBSERVATION + " (" + OBSERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OBSERVATION_LATITUDE + " REAL NOT NULL DEFAULT 0, " + OBSERVATION_LONGITUDE + " REAL NOT NULL DEFAULT 0, " + OBSERVATION_START + " INTEGER NOT NULL DEFAULT 0, " + OBSERVATION_FINISH + " NOT NULL DEFAULT 0)";
		db.execSQL(createObservation);
		
		String createDetails = "CREATE TABLE " + TABLE_DETAILS + " (" + DETAILS_ID + " INTEGER NOT NULL, " + DETAILS_TYPE + " INTEGER NOT NULL DEFAULT 0, " + DETAILS_TIMESTAMP + " INTEGNER NOT NULL DEFAULT 0)";
		db.execSQL(createDetails);
		
		String createTypes = "CREATE TABLE " + TABLE_TYPES + " ( " + TYPES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TYPES_NAME + " TEXT NOT NULL)";
		db.execSQL(createTypes);
		
		//insert types
		ContentValues cv = new ContentValues();
		cv.put(TYPES_NAME, NO_SMOKING);
		db.insert(TABLE_TYPES, null, cv);
		cv.put(TYPES_NAME, ADULT_SMOKING);
		db.insert(TABLE_TYPES, null, cv);
		cv.put(TYPES_NAME, ADULT_SMOKING_OTHERS);
		db.insert(TABLE_TYPES, null, cv);
		cv.put(TYPES_NAME, ADULT_SMOKING_WITH_CHILD);
		db.insert(TABLE_TYPES, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	/* debug method */
	public void dummyDBRead(){
		SQLiteDatabase db = getReadableDatabase();
		db.close();
	}
	
	/* not used */
	public Observation newObservation(){
		long startTime = System.currentTimeMillis();
		Observation result = new Observation(startTime);
		
		ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_START, startTime);
		SQLiteDatabase db = getWritableDatabase();
		long observationId = db.insert(TABLE_OBSERVATION, null, cv);
		
		if(observationId == -1){
			db.close();
			throw new DatabaseException("Can't get Observation ID from database");
		}
		db.close();
		result.setId(observationId);
		return result;
	}
	
	public long getNewObservationId(){
		ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_START, System.currentTimeMillis());
		SQLiteDatabase db = getWritableDatabase();
		long observationId = db.insert(TABLE_OBSERVATION, null, cv);		
		if(observationId == -1){
			db.close();
			throw new DatabaseException("Can't get Observation ID from database");
		}
		db.close();
		return observationId;
	}

	/*
	 * Increments a 'NoSmoking' count for the given observation, it will throw
	 * a DatabaseException if getTypeIdFromName() doesn't exist
	 */
	public void incrementNoSmoking(long observationId) throws DatabaseException{
		ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP,System.currentTimeMillis());
		cv.put(DETAILS_TYPE, getTypeIdFromName(NO_SMOKING));
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}

	/*
	 * Increments a 'NoOccupants' count for the given observation, it will throw
	 * a DatabaseException if getTypeIdFromName() doesn't exist
	 */
	public void incrementNoOccupants(long observationId) throws DatabaseException{
		ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP,System.currentTimeMillis());
		cv.put(DETAILS_TYPE, getTypeIdFromName(ADULT_SMOKING));
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}
	
	/*
	 * Increments a 'OtherAdults' count for the given observation, it will throw
	 * a DatabaseException if getTypeIdFromName() doesn't exist
	 */
	public void incrementOtherAdults(long observationId) throws DatabaseException{
		ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP,System.currentTimeMillis());
		cv.put(DETAILS_TYPE, getTypeIdFromName(ADULT_SMOKING_OTHERS));
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}
	
	/*
	 * Increments a 'OtherAdults' count for the given observation, it will throw
	 * a DatabaseException if getTypeIdFromName() doesn't exist
	 */
	public void incrementChild(long observationId) throws DatabaseException{
		ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP,System.currentTimeMillis());
		cv.put(DETAILS_TYPE, getTypeIdFromName(ADULT_SMOKING_WITH_CHILD));
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}
	
	/*
	 * Returns the observation type ID from the name given. If no match is found for the
	 * name given, then a DatabaseException is thrown.
	 */
	private int getTypeIdFromName(String string) throws DatabaseException{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(TABLE_TYPES, new String[]{TYPES_ID}, TYPES_NAME + " = ?", new String[]{string}, null, null, null);
		int id;
		if(cur.getCount() == 1){
			cur.moveToFirst();
			id =cur.getInt(0);
		}else{
			throw new DatabaseException("Can't get type ID from name");
		}
		
		cur.deactivate();
		db.close();
		return id;
	}

	/* Checks to see if anything has been counted yet for the given observation id
	 * 
	 */
	public boolean hasCounted(long observationId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(TABLE_DETAILS,null,DETAILS_ID + " = ?",new String[]{observationId + ""},null,null,null);
		boolean result = false;
		if(cur.getCount() > 0){
			result = true;
		}
		cur.deactivate();
		db.close();
		return result;
	}

	/* Deletes an observation. Does not delete any data associated with it. Is used when a observation
	 * has been created but no data has been saved. Should be used in conjunction with hasCounted().
	 */
	public void deleteObservation(long observationId) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_OBSERVATION, OBSERVATION_ID + " = ?", new String[]{observationId + ""});
		db.close();
	}

	/* Checks to see if the observation from the given ID has a GPS coordinate saved. Checks to see
	 * if the latitude and longitude are not 0. While it is possible to have a latitude and longitude
	 * of 0, it is in the middle of the ocean so there won't be any cars.
	 */
	public boolean hasGPS(long observationId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(TABLE_OBSERVATION,null,OBSERVATION_ID + " = ? AND " + OBSERVATION_LATITUDE + " != 0 AND " + OBSERVATION_LONGITUDE + " != 0",new String[]{observationId + ""},null,null,null);
		boolean result = false;
		if(cur.getCount() > 0){
			result = true;
		}
		cur.deactivate();
		db.close();
		return result;
	}

	/*
	 * Saves the given GPS cooardinates to the observation given by the
	 * observation ID
	 */
	public void saveGPSLocation(long observationId, Location loc) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_LATITUDE, loc.getLatitude());
		cv.put(OBSERVATION_LONGITUDE, loc.getLongitude());
		db.update(TABLE_OBSERVATION, cv, OBSERVATION_ID + " = ? ", new String[]{observationId + ""});
		db.close();		
	}

	/*
	 * Sets a finished timestamp for the given observation ID
	 */
	public void setFinishTime(long observationId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_FINISH, System.currentTimeMillis());
		db.update(TABLE_OBSERVATION, cv, OBSERVATION_ID + " = ? ", new String[]{observationId + ""});
		db.close();		
	}
	
	public List<Observation> getObservations() {
		SQLiteDatabase db = this.getReadableDatabase();
		List<Observation> result = new ArrayList<Observation>();
		//Cursor cur = db.query(TABLE_OBSERVATION,null,null,null,null,null,OBSERVATION_START + " ASC, " + OBSERVATION_FINISH + " ASC");
		
		//ugly query
		String rawQuery = "SELECT observations.*, (SELECT COUNT(observation_id) FROM details WHERE observation_type = 1 AND observation_id = observations.id) AS NoSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 2 AND observation_id = observations.id) AS AdultSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 3 AND observation_id = observations.id) AS AdultSmokingOthers,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 4 AND observation_id = observations.id) AS AdultSmokingChild FROM observations WHERE finish_time > 0";
		Cursor cur = db.rawQuery(rawQuery, null);
		Location tempLoc = new Location("TEMP");
		while(cur.moveToNext()){
			Observation temp = new Observation(cur.getLong(3));
			temp.setFinish(cur.getLong(4));
			tempLoc.setLatitude(cur.getDouble(1));
			tempLoc.setLongitude(cur.getDouble(2));
			temp.setLocation(tempLoc);
			temp.setNoOthers(cur.getInt(6));
			temp.setNoSmoking(cur.getInt(5));
			temp.setOther(cur.getInt(7));
			temp.setChild(cur.getInt(8));
			result.add(temp);
		}
		cur.deactivate();
		db.close();
		return result;
	}
}
