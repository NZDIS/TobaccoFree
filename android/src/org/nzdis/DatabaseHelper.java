package org.nzdis;

import java.security.NoSuchAlgorithmException;
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
	public static final int DATABASE_VESRION = 2;
	
	//observation table
	public static final String TABLE_OBSERVATION = "observations";
	public static final String OBSERVATION_ID = "id";
	public static final String OBSERVATION_LATITUDE = "latitude";
	public static final String OBSERVATION_LONGITUDE = "longitude";
	public static final String OBSERVATION_START = "start_time";
	public static final String OBSERVATION_FINISH = "finish_time";
	public static final String OBSERVATION_UPLOADED = "uploaded";
	
	//observation details table
	public static final String TABLE_DETAILS = "details";
	public static final String DETAILS_ID = "observation_id";
	public static final String DETAILS_TYPE = "observation_type";
	public static final String DETAILS_TIMESTAMP = "obeservation_time";	
	
	//type constants
	public static final String NO_SMOKING = "No Smokers";
	public static final String ADULT_SMOKING = "Adult with no other occupants";
	public static final String ADULT_SMOKING_OTHERS = "Adult with other smoking adults";
	public static final String ADULT_SMOKING_WITH_CHILD = "Smoking with child <= 12";
	
	//username table
	public static final String TABLE_USER = "user";
	public static final String USER_USERNAME = "username";
	public static final String USER_HASH = "hash";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VESRION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createObservation = "CREATE TABLE " + TABLE_OBSERVATION + " (" + OBSERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OBSERVATION_LATITUDE + " REAL NOT NULL DEFAULT 0, " + OBSERVATION_LONGITUDE + " REAL NOT NULL DEFAULT 0, " + OBSERVATION_START + " INTEGER NOT NULL DEFAULT 0, " + OBSERVATION_FINISH + " INTEGER NOT NULL DEFAULT 0, " + OBSERVATION_UPLOADED + " INTEGER NOT NULL DEFAULT 0)";
		db.execSQL(createObservation);
		
		String createDetails = "CREATE TABLE " + TABLE_DETAILS + " (" + DETAILS_ID + " INTEGER NOT NULL, " + DETAILS_TYPE + " INTEGER NOT NULL DEFAULT 0, " + DETAILS_TIMESTAMP + " INTEGNER NOT NULL DEFAULT 0)";
		db.execSQL(createDetails);
		
		String createUser = "CREATE TABLE " + TABLE_USER + " (" + USER_USERNAME + " VARCHAR(20) NOT NULL, " + USER_HASH + " VARCHARR(32) NOT NULL)";  
		db.execSQL(createUser);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == 1){
			//drop types table here
			db.execSQL("DROP TABLE IF EXISTS types");
			
			//add upload column
			db.execSQL("ALTER TABLE " + TABLE_OBSERVATION + " ADD " + OBSERVATION_UPLOADED + " INTEGER NOT NULL DEFAULT 0");
			
			//create username table
			String createUser = "CREATE TABLE " + TABLE_USER + " (" + USER_USERNAME + " VARCHAR(20) NOT NULL, " + USER_HASH + " VARCHARR(32) NOT NULL)";  
			db.execSQL(createUser);
		}
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
	 * Increments a 'Child' count for the given observation, it will throw
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
	 * Decrements a 'NoSmoking' count for the given observation, it will throw
	 * a DatabaseException if the query fails.
	 */
	public void decrementNoSmoking(long observationId) throws DatabaseException{
		String typeID = getTypeIdFromName(NO_SMOKING) + "";
		SQLiteDatabase db = getWritableDatabase();
		Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if(result.moveToFirst()){
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?" , new String[]{observationId + "",typeID,result.getLong(0) + ""});
		}else{
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
	public void decrementNoOccupants(long observationId) throws DatabaseException{
		String typeID = getTypeIdFromName(ADULT_SMOKING) + "";
		SQLiteDatabase db = getWritableDatabase();
		Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if(result.moveToFirst()){
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?" , new String[]{observationId + "",typeID,result.getLong(0) + ""});
		}else{
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
		String typeID = getTypeIdFromName(ADULT_SMOKING_OTHERS) + "";
		SQLiteDatabase db = getWritableDatabase();
		Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if(result.moveToFirst()){
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?" , new String[]{observationId + "",typeID,result.getLong(0) + ""});
		}else{
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
		String typeID = getTypeIdFromName(ADULT_SMOKING_WITH_CHILD) + "";
		SQLiteDatabase db = getWritableDatabase();
		Cursor result = db.query(TABLE_DETAILS, new String[]{DETAILS_TIMESTAMP}, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ?", new String[]{observationId + "",typeID}, null, null, DETAILS_TIMESTAMP + " DESC", 1 + "");
		if(result.moveToFirst()){
			db.delete(TABLE_DETAILS, DETAILS_ID + " = ? AND " + DETAILS_TYPE + " = ? AND " + DETAILS_TIMESTAMP + " = ?" , new String[]{observationId + "",typeID,result.getLong(0) + ""});
		}else{
			result.close();
			db.close();
			throw new DatabaseException("Invalid decrement");
		}
		result.close();
		db.close();
	}
	
	/*
	 * Returns the observation type ID from the name given. If no match is found for the
	 * name given, then a DatabaseException is thrown.
	 */
	private int getTypeIdFromName(String string) {
		if(string.equals(NO_SMOKING)){
			return 1;
		}else if(string.equals(ADULT_SMOKING)){
			return 2;
		}else if(string.equals(ADULT_SMOKING_OTHERS)){
			return 3;
		}else{
			return 4;
		}
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
	
	/*
	 * Returns all of the observations in the database along with the totals of each count.
	 */
	public List<Observation> getObservations() {
		SQLiteDatabase db = this.getReadableDatabase();
		List<Observation> result = new ArrayList<Observation>();
		//Cursor cur = db.query(TABLE_OBSERVATION,null,null,null,null,null,OBSERVATION_START + " ASC, " + OBSERVATION_FINISH + " ASC");
		
		//ugly query
		String rawQuery = "SELECT observations.*, (SELECT COUNT(observation_id) FROM details WHERE observation_type = 1 AND observation_id = observations.id) AS NoSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 2 AND observation_id = observations.id) AS AdultSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 3 AND observation_id = observations.id) AS AdultSmokingOthers,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 4 AND observation_id = observations.id) AS AdultSmokingChild FROM observations WHERE finish_time > 0";
		Cursor cur = db.rawQuery(rawQuery, null);		
		while(cur.moveToNext()){
			Location tempLoc = new Location("TEMP");
			Observation temp = new Observation(cur.getLong(3));
			temp.setFinish(cur.getLong(4));
			tempLoc.setLatitude(cur.getDouble(1));
			tempLoc.setLongitude(cur.getDouble(2));
			temp.setLocation(tempLoc);
			temp.setNoOthers(cur.getInt(7));
			temp.setNoSmoking(cur.getInt(6));
			temp.setOther(cur.getInt(8));
			temp.setChild(cur.getInt(9));
			temp.setId(cur.getLong(0));
			result.add(temp);
		}
		cur.deactivate();
		db.close();
		return result;
	}
	
	/*
	 * Returns all of the observations that haven't been marked uploaded in the database along with the totals of each count.
	 */
	public List<Observation> getObservationsNotUploaded() {
		SQLiteDatabase db = this.getReadableDatabase();
		List<Observation> result = new ArrayList<Observation>();
		//Cursor cur = db.query(TABLE_OBSERVATION,null,null,null,null,null,OBSERVATION_START + " ASC, " + OBSERVATION_FINISH + " ASC");
		
		//ugly query
		String rawQuery = "SELECT observations.*, (SELECT COUNT(observation_id) FROM details WHERE observation_type = 1 AND observation_id = observations.id) AS NoSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 2 AND observation_id = observations.id) AS AdultSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 3 AND observation_id = observations.id) AS AdultSmokingOthers,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 4 AND observation_id = observations.id) AS AdultSmokingChild FROM observations WHERE finish_time > 0 AND uploaded = 0";
		Cursor cur = db.rawQuery(rawQuery, null);		
		while(cur.moveToNext()){
			Location tempLoc = new Location("TEMP");
			Observation temp = new Observation(cur.getLong(3));
			temp.setFinish(cur.getLong(4));
			tempLoc.setLatitude(cur.getDouble(1));
			tempLoc.setLongitude(cur.getDouble(2));
			temp.setLocation(tempLoc);
			temp.setNoOthers(cur.getInt(7));
			temp.setNoSmoking(cur.getInt(6));
			temp.setOther(cur.getInt(8));
			temp.setChild(cur.getInt(9));
			temp.setId(cur.getLong(0));
			result.add(temp);
		}
		cur.deactivate();
		db.close();
		return result;
	}
	
	/* Deletes an observation and all of its associated observation data
	 * 
	 */
	public void deleteObservationDeep(long observationId) throws DatabaseException{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_OBSERVATION, OBSERVATION_ID + " = ?", new String[]{observationId + ""});
		db.delete(TABLE_DETAILS, DETAILS_ID + " = ?", new String[]{observationId + ""});
		db.close();
	}
	
	/*
	 * Returns an observation with all of its data.
	 * 
	 * ********** INCOMPLETE *************
	 */
	/*public Observation getObservationById(long id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Observation result;
		
		Cursor cur = db.query(TABLE_OBSERVATION,null,OBSERVATION_ID + " = ?",new String[]{id + ""},null,null,null,"1");

		if(cur.getCount() != 1){
			throw new DatabaseException("No observation for that ID");
		}else{
			cur.moveToFirst();
			result = new Observation(cur.getLong(3));
			Location loc = new Location("TEMP");
			loc.setLatitude(cur.getDouble(1));
			loc.setLongitude(cur.getDouble(2));
			result.setLocation(loc);
			result.setId(id);
			result.setFinish(cur.getLong(4));
		}
		cur.deactivate();
		db.close();
		return result;
	}*/
	
	/* Returns the number of cars without any smokers for the given observation id
	 * 
	 */
	public int getNoSmokerCount(long id){
		int result = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = 1 AND observation_id = ?", new String[]{id+""});
		if(curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	/* Returns the number of cars with alone adult smokers for the given observation id
	 * 
	 */
	public int getAloneSmokerCount(long id){
		int result = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = 2 AND observation_id = ?", new String[]{id+""});
		curs.moveToFirst();
		if(curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	/* Returns the number of cars with multiple adult smokers for the given observation id
	 * 
	 */
	public int getAdultSmokersCount(long id){
		int result = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = 3 AND observation_id = ?", new String[]{id+""});
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
	public int getAdultChildSmokerCount(long id){
		int result = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor curs = db.rawQuery("SELECT COUNT(observation_id) FROM details WHERE observation_type = 4 AND observation_id = ?", new String[]{id+""});
		curs.moveToFirst();
		if(curs.moveToFirst()){
			result = curs.getInt(0);
		}
		curs.deactivate();
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
		if(curs.getCount() == 1){
			result.setUsername(curs.getString(0));
			result.setPasswordHash(curs.getString(1), true);
		}else{
			curs.deactivate();
			db.close();
			throw new UsernameNotSetException("Username and/or password have not been set");
		}
		curs.deactivate();
		db.close();
		return result;
	}
	
	/*
	 * Sets the users username and password
	 */
	public void setUsersDetails(UsersDetails input) throws UsernameNotSetException{
		if(input.getPasswordHash() == null || input.getUsername() == null){
			throw new UsernameNotSetException("Username and/or password have not been set in the container \"input\"");
		}
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(USER_USERNAME, input.getUsername());
		cv.put(USER_HASH, input.getPasswordHash());
		
		//make sure there aren't existing details
		db.delete(TABLE_USER, null, null);
		
		//insert new details
		db.insert(TABLE_USER, null, cv);
		db.close();
	}

	/* Retrieve a selected observation from a given ID
	 * 
	 */
	public Observation getObservation(long id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Observation result;
		//Cursor cur = db.query(TABLE_OBSERVATION,null,null,null,null,null,OBSERVATION_START + " ASC, " + OBSERVATION_FINISH + " ASC");
		
		//ugly query
		String rawQuery = "SELECT observations.*, (SELECT COUNT(observation_id) FROM details WHERE observation_type = 1 AND observation_id = observations.id) AS NoSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 2 AND observation_id = observations.id) AS AdultSmoking,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 3 AND observation_id = observations.id) AS AdultSmokingOthers,(SELECT COUNT(observation_id) FROM details WHERE observation_type = 4 AND observation_id = observations.id) AS AdultSmokingChild FROM observations WHERE finish_time > 0 AND observations.id = " + id;
		Cursor cur = db.rawQuery(rawQuery, null);		
		if(cur.moveToNext()){
			Location tempLoc = new Location("TEMP");
			result = new Observation(cur.getLong(3));
			result.setFinish(cur.getLong(4));
			tempLoc.setLatitude(cur.getDouble(1));
			tempLoc.setLongitude(cur.getDouble(2));
			result.setLocation(tempLoc);
			result.setNoOthers(cur.getInt(6));
			result.setNoSmoking(cur.getInt(5));
			result.setOther(cur.getInt(7));
			result.setChild(cur.getInt(8));
			result.setId(cur.getLong(0));
		}else{
			cur.deactivate();
			db.close();
			return null;
		}
		cur.deactivate();
		db.close();
		return result;
	}

	/* Set the 'upload' column to 1 in the table TABLE_OBSERVATION for the given ID
	 * 
	 */
	public void setUploaded(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(OBSERVATION_UPLOADED, 1);
		db.update(TABLE_OBSERVATION, cv, OBSERVATION_ID + " = ?", new String[]{id + ""});
		db.close();
	}
}
