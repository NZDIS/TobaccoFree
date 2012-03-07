/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * SQLite helper. See also Constants for shared DB and JSON constants.
 * 
 * @author Hamish Medlin
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Dec 2011
 */
public class DatabaseHelper extends SQLiteOpenHelper implements Constants {

	public static final String DATABASE_NAME = "globalink.sqlite";
	public static final int DATABASE_VESRION = 2; 
	
	//observation table
	public static final String TABLE_OBSERVATION = "observations";
	public static final String OBSERVATION_ID = "id";
	
	//observation details table
	public static final String TABLE_DETAILS = "details";
	public static final String DETAILS_ID = "observation_id";
	public static final String DETAILS_TYPE = "observation_type";
	public static final String DETAILS_TIMESTAMP = "obeservation_time";	
	
	//type constants
	public static final String NO_SMOKING = "NoSmoking"; //No Smokers
	public static final String ADULT_SMOKING = "AdultSmoking"; //smoking driver with no other occupants
	public static final String ADULT_SMOKING_OTHERS = "AdultSmokingOther"; //Adult with other smoking adults
	public static final String ADULT_SMOKING_WITH_CHILD = "AdultSmokingChild"; //with child <= 12;
	
	//user table
	public static final String TABLE_USER = "user";

	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VESRION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_Observation);
		db.execSQL(CREATE_TABLE_Details);
		db.execSQL(CREATE_TABLE_User);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			//drop types table
			db.execSQL("DROP TABLE IF EXISTS types"); //old types table, not used
			//add upload column
			db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD " + OBSERVATION_UPLOADED + " INTEGER NOT NULL DEFAULT 0");
			// create new table that wasn't here in version 1
			db.execSQL(CREATE_TABLE_User);
		} else { // for all other not covered version upgrades, re-start the DB from scratch
			//drop types table
			db.execSQL("DROP TABLE IF EXISTS types"); //old types table, not used
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATION);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILS);
			
			db.execSQL(CREATE_TABLE_User);
			db.execSQL(CREATE_TABLE_Observation);
			db.execSQL(CREATE_TABLE_Details);
		}
	}

	
	/**
	 * Creates, registers and generates unique ID for newly created observation.
	 * @return unique ID of newly created observation, or throws DatabaseException if operation fails.
	 */
	public long getNewObservationId() {
		final ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_START, System.currentTimeMillis());
		final SQLiteDatabase db = getWritableDatabase();
		final long observationId = db.insert(TABLE_OBSERVATION, null, cv);		
		if (observationId == -1) {
			db.close();
			throw new DatabaseException("Can't get Observation ID from database");
		}
		db.close();
		return observationId;
	}

	/**
	 * Increments a 'NoSmoking' count for the given observation. Throws
	 * a DatabaseException if getTypeIdFromName() doesn't exist.
	 */
	public void incrementNoSmoking(long observationId) throws DatabaseException{
		ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP,System.currentTimeMillis());
		cv.put(DETAILS_TYPE, SMOKING_ID_NO_SMOKING);
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}

	/**
	 * Increments a 'NoOccupants' count for the given observation. Throws
	 * a DatabaseException if getTypeIdFromName() doesn't exist.
	 */
	public void incrementLoneAdultSmoking(long observationId) throws DatabaseException{
		final ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP,System.currentTimeMillis());
		cv.put(DETAILS_TYPE, SMOKING_ID_ADULT_SMOKING_ALONE);
		final SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}
	
	/**
	 * Increments a 'OtherAdults' count for the given observation. Throws
	 * a DatabaseException if getTypeIdFromName() doesn't exist.
	 */
	public void incrementOtherAdults(long observationId) throws DatabaseException {
		final ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP, System.currentTimeMillis());
		cv.put(DETAILS_TYPE, SMOKING_ID_ADULT_SMOKING_OTHERS);
		final SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}
	
	/**
	 * Increments a 'Child' count for the given observation. Throws
	 * a DatabaseException if getTypeIdFromName() doesn't exist.
	 */
	public void incrementChild(long observationId) throws DatabaseException {
		final ContentValues cv = new ContentValues();
		cv.put(DETAILS_ID, observationId);
		cv.put(DETAILS_TIMESTAMP, System.currentTimeMillis());
		cv.put(DETAILS_TYPE, SMOKING_ID_ADULT_SMOKING_CHILD);
		final SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_DETAILS, null, cv);
		db.close();
	}
	
	/*
	 * Decrements a 'NoSmoking' count for the given observation, it will throw
	 * a DatabaseException if the query fails.
	 */
	public void decrementNoSmoking(long observationId) throws DatabaseException {
		final String typeID = String.valueOf(SMOKING_ID_NO_SMOKING);
		final SQLiteDatabase db = getWritableDatabase();
		final Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", 
				new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if (result.moveToFirst()) {
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?", 
					new String[]{observationId + "",typeID,result.getLong(0) + ""});
		} else {
			result.close();
			db.close();
			throw new DatabaseException("Invalid decrement");
		}
		result.close();
		db.close();
	}
	
	
	/*
	 * Decrements a 'NoOccupants' count for the given observation, it will throw
	 * a DatabaseException if the query fails.
	 */
	public void decrementLoneAdultSmoking(long observationId) throws DatabaseException {
		final String typeID = String.valueOf(SMOKING_ID_ADULT_SMOKING_ALONE);
		final SQLiteDatabase db = getWritableDatabase();
		final Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", 
				new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if (result.moveToFirst()) {
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?", 
					new String[]{observationId + "",typeID,result.getLong(0) + ""});
		} else {
			result.close();
			db.close();
			throw new DatabaseException("Invalid decrement");
		}
		result.close();
		db.close();
	}
	
	/*
	 * Decrements a 'OtherAdults' count for the given observation, it will throw
	 * a DatabaseException if the query fails.
	 */
	public void decrementOtherAdults(long observationId) throws DatabaseException{
		final String typeID = String.valueOf(SMOKING_ID_ADULT_SMOKING_OTHERS);
		final SQLiteDatabase db = getWritableDatabase();
		final Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", 
					new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if (result.moveToFirst()) {
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?", 
					new String[]{observationId + "",typeID,result.getLong(0) + ""});
		} else {
			result.close();
			db.close();
			throw new DatabaseException("Invalid decrement");
		}
		result.close();
		db.close();
	}
	
	/*
	 * Decrements a 'Child' count for the given observation, it will throw
	 * a DatabaseException if the query fails.
	 */
	public void decrementChild(long observationId) throws DatabaseException{
		final String typeID = String.valueOf(SMOKING_ID_ADULT_SMOKING_CHILD) + "";
		final SQLiteDatabase db = getWritableDatabase();
		final Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if (result.moveToFirst()) {
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?", 
					new String[]{observationId + "",typeID,result.getLong(0) + ""});
		} else {
			result.close();
			db.close();
			throw new DatabaseException("Invalid decrement");
		}
		result.close();
		db.close();
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
	 * To delete all of the data as well use deleteObservationDeep().
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
	
	private Observation createObservationFromCursor(final Cursor cur) {
		final Location tempLoc = new Location("TEMP");
		final Observation temp = new Observation(cur.getLong(cur.getColumnIndex(OBSERVATION_START)));
		temp.setFinish(cur.getLong(cur.getColumnIndex(OBSERVATION_FINISH)));
		tempLoc.setLatitude(cur.getDouble(cur.getColumnIndex(OBSERVATION_LATITUDE)));
		tempLoc.setLongitude(cur.getDouble(cur.getColumnIndex(OBSERVATION_LONGITUDE)));
		temp.setLocation(tempLoc);
		temp.setLoneAdult(cur.getInt(cur.getColumnIndex(ADULT_SMOKING)));
		temp.setNoSmoking(cur.getInt(cur.getColumnIndex(NO_SMOKING)));
		temp.setOtherAdults(cur.getInt(cur.getColumnIndex(ADULT_SMOKING_OTHERS)));
		temp.setChild(cur.getInt(cur.getColumnIndex(ADULT_SMOKING_WITH_CHILD)));
		temp.setId(cur.getLong(cur.getColumnIndex(OBSERVATION_ID)));
		return temp;
	}
	/*
	 * Returns all of the observations in the database along with the totals of each count.
	 */
	public List<Observation> getObservations() {
		final SQLiteDatabase db = this.getReadableDatabase();
		final List<Observation> result = new ArrayList<Observation>();
		
		// TODO ugly query. Can we do better than this?
		final String rawQuery = "SELECT observations.*, " 
				+ "(SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_NO_SMOKING  
				+ " AND observation_id = observations.id) AS " + NO_SMOKING 
				+ ", (SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_ADULT_SMOKING_ALONE
				+ " AND observation_id = observations.id) AS " + ADULT_SMOKING 
				+ ", (SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_ADULT_SMOKING_OTHERS 
				+ " AND observation_id = observations.id) AS " + ADULT_SMOKING_OTHERS 
				+ ", (SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_ADULT_SMOKING_CHILD 
				+ " AND observation_id = observations.id) AS " + ADULT_SMOKING_WITH_CHILD 
				+ " FROM observations WHERE finish_time > 0";
		final Cursor cur = db.rawQuery(rawQuery, null);		
		while (cur.moveToNext()) {
			final Observation obs = createObservationFromCursor(cur); 
			result.add(obs);
			setObservationDetails(obs);
		}
		cur.deactivate();
		db.close();
		return result;
	}
	
	private void setObservationDetails(Observation o) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final long id = o.getId();
// TODO debugging
//Log.i("Globalink","Searching details for id:" + id);
		final Cursor cur =
                db.query(true, TABLE_DETAILS, new String[] 
                               {DETAILS_TIMESTAMP, DETAILS_TYPE}, 
                		DETAILS_ID + "=?", new String[] {String.valueOf(id)}, 
                		null, null, null, null);
        if (cur == null || cur.getCount() == 0) {
        	cur.close();
        	return;
        }
        while (cur.moveToNext()) {
        	o.addDetail(cur.getLong(cur.getColumnIndex(DETAILS_TIMESTAMP)), 
        			cur.getInt(cur.getColumnIndex(DETAILS_TYPE)));
        }
        cur.close();
	}
	
	/*
	 * Returns all of the observations that haven't been marked uploaded in the database along with the totals of each count.
	 */
	public List<Observation> getObservationsNotUploaded() {
		final SQLiteDatabase db = this.getReadableDatabase();
		final List<Observation> result = new ArrayList<Observation>();
		
		// ugly query
		final String rawQuery = "SELECT observations.*, " 
				+ "(SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_NO_SMOKING  
				+ " AND observation_id = observations.id) AS " + NO_SMOKING 
				+ ", (SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_ADULT_SMOKING_ALONE
				+ " AND observation_id = observations.id) AS " + ADULT_SMOKING 
				+ ", (SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_ADULT_SMOKING_OTHERS 
				+ " AND observation_id = observations.id) AS " + ADULT_SMOKING_OTHERS 
				+ ", (SELECT COUNT(observation_id) FROM details WHERE observation_type = " + SMOKING_ID_ADULT_SMOKING_CHILD 
				+ " AND observation_id = observations.id) AS " + ADULT_SMOKING_WITH_CHILD 
				+ " FROM observations WHERE finish_time > 0 AND uploaded = 0";

		final Cursor cur = db.rawQuery(rawQuery, null);		
		while(cur.moveToNext()) {
			final Observation obs = createObservationFromCursor(cur);
			result.add(obs);
			setObservationDetails(obs);
		}
		cur.deactivate();
		db.close();
		return result;
	}
	
	/* Deletes an observation and all of its associated observation data
	 * 
	 */
	public void deleteObservationDeep(final long observationId) throws DatabaseException{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_OBSERVATION, OBSERVATION_ID + " = ?", new String[]{observationId + ""});
		db.delete(TABLE_DETAILS, DETAILS_ID + " = ?", new String[]{observationId + ""});
		db.close();
	}
	

	
	/* Returns the number of cars without any smokers for the given observation id
	 * 
	 */
	public int getNoSmokingCount(final long id) {
		int result = 0;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = "
				+ SMOKING_ID_NO_SMOKING + " AND observation_id = ?", new String[]{id+""});
		if (curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	/* Returns the number of cars with alone adult smokers for the given observation id
	 * 
	 */
	public int getLoneSmokerCount(final long id) {
		int result = 0;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = "
				+ SMOKING_ID_ADULT_SMOKING_ALONE + " AND observation_id = ?", new String[]{id+""});
		curs.moveToFirst();
		if (curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	/* Returns the number of cars with multiple adult smokers for the given observation id
	 * 
	 */
	public int getOtherAdultsSmokingCount(long id){
		int result = 0;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = "
				+ SMOKING_ID_ADULT_SMOKING_OTHERS + " AND observation_id = ?", new String[]{id+""});
		curs.moveToFirst();
		if(curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	/* Returns the number of cars with smokers with children under 12 for the given observation id
	 * 
	 */
	public int getChildCount(long id){
		int result = 0;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = "
				+ SMOKING_ID_ADULT_SMOKING_CHILD + " AND observation_id = ?", new String[]{id+""});		
		if (curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	public long getObservationStart(long id){
		long result = 0;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cur = db.query(TABLE_OBSERVATION, new String[] 
	               {OBSERVATION_START}, 
	        		OBSERVATION_ID + " = ?", 
	        		new String[] {String.valueOf(id)}, 
	        		null, null, null);
		
		if(cur.moveToFirst()) {
			result = cur.getLong(cur.getColumnIndex(OBSERVATION_START));
		}
		cur.deactivate();
		db.close();
		return result;
	}
	
	public long getObservationFinish(long id){
		long result = 0;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cur = db.query(TABLE_OBSERVATION, new String[] 
	               {OBSERVATION_FINISH}, 
	        		OBSERVATION_ID + " = ?", 
	        		new String[] {String.valueOf(id)}, 
	        		null, null, null);
		
		if (cur.moveToFirst()) {
			result = cur.getLong(cur.getColumnIndex(OBSERVATION_FINISH));
		}
		cur.deactivate();
		db.close();
		return result;
	}
	

	public Location getObservationLocation(long id){
		final Location result = new Location("empty");
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cur = db.query(TABLE_OBSERVATION, new String[] 
	               {OBSERVATION_LATITUDE, OBSERVATION_LONGITUDE}, 
	        		OBSERVATION_ID + " = ?", 
	        		new String[] {String.valueOf(id)}, 
	        		null, null, null);
		
		if (cur.moveToFirst()) {
			final double latitude = cur.getDouble(cur.getColumnIndex(OBSERVATION_LATITUDE));
			final double longitude = cur.getDouble(cur.getColumnIndex(OBSERVATION_LONGITUDE));
			result.setLatitude(latitude);
			result.setLongitude(longitude);
		}
		cur.deactivate();
		db.close();
		return result;
	}

	
	/*
	 * Get the users username and password hash
	 */
	public UsersDetails getUserDetails() throws NoSuchAlgorithmException, UsernameNotSetException{
		UsersDetails result = new UsersDetails();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor curs = db.query(TABLE_USER, null, null, null, null, null, null, "1");
		curs.moveToFirst();
		
		if (curs.getCount() == 0 || curs.getString(0).length() == 0) {
			curs.deactivate();
			db.close();
			throw new UsernameNotSetException("Username and/or password have not been set");
		}
		
		result.setUserEmail(curs.getString(0));
		result.setPasswordHash(curs.getString(1), true);
		curs.deactivate();
		db.close();
		return result;
	}
	
	/**
	 * Sets the user email and password.
	 */
	public void setUsersDetails(UsersDetails input) throws UsernameNotSetException {
		if (input.getPasswordHash() == null || input.getUserEmail() == null) {
			throw new UsernameNotSetException("Username and/or password have not been set in the container \"input\"");
		}
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(USER_USER_EMAIL, input.getUserEmail());
		cv.put(USER_PASSWORD_HASH, input.getPasswordHash());
		
		// make sure there aren't existing user details
		db.delete(TABLE_USER, null, null);
		
		//insert new details
		db.insert(TABLE_USER, null, cv);
		db.close();
	}

	
	/** 
	 * Retrieves a selected observation for a given ID.
	 */
	public Observation getObservation(long id) {
		Observation result;
		result = new Observation(getObservationStart(id));
		result.setId(id);
		result.setStart(getObservationStart(id));
		result.setFinish(getObservationFinish(id));
		result.setLocation(getObservationLocation(id));
		result.setLoneAdult(getLoneSmokerCount(id));
		
		result.setNoSmoking(getNoSmokingCount(id));
		result.setChild(getChildCount(id));
		result.setOtherAdults(getOtherAdultsSmokingCount(id));
		return result;
	}

	/**
	 * Sets the 'upload' column to 1 in the table TABLE_OBSERVATION for the given ID.
	 */
	public void setUploaded(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_UPLOADED, 1);
		db.update(TABLE_OBSERVATION, cv, OBSERVATION_ID + " = ?", new String[] {String.valueOf(id)} );
		db.close();
	}
	
	
	
	
	
	static final String CREATE_TABLE_Observation = "CREATE TABLE " + TABLE_OBSERVATION + " (" 
			+ OBSERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ OBSERVATION_LATITUDE + " REAL NOT NULL DEFAULT 0, " 
			+ OBSERVATION_LONGITUDE + " REAL NOT NULL DEFAULT 0, " 
			+ OBSERVATION_START + " INTEGER NOT NULL DEFAULT 0, " 
			+ OBSERVATION_FINISH + " INTEGER NOT NULL DEFAULT 0, "
			+ OBSERVATION_UPLOADED + " INTEGER NOT NULL DEFAULT 0)";
	
	static final String CREATE_TABLE_Details = "CREATE TABLE " + TABLE_DETAILS + " (" 
			+ DETAILS_ID + " INTEGER NOT NULL, " 
			+ DETAILS_TYPE + " INTEGER NOT NULL DEFAULT 0, " 
			+ DETAILS_TIMESTAMP + " INTEGNER NOT NULL DEFAULT 0)";
	
	static final String CREATE_TABLE_User = "CREATE TABLE " + TABLE_USER + " (" 
			+ USER_USER_EMAIL + " VARCHAR(32) NOT NULL, " 
			+ USER_PASSWORD_HASH + " VARCHARR(32) NOT NULL)";  
	
	
}
