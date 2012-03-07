/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

/**
 * Constants for the project. 
 * 
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Jan 12, 2012 11:44:42 AM
 */
public interface Constants {

	
	/* For local testing on home LAN */
	String URL_OBSERVATION_ADD = "http://192.168.2.200:8000/observation/add";
    String HASH_SALT_PRE = "put salt here";
    String HASH_SALT_POST = "put pepper here";
    /**/
    
	
	
    // 1 - broken indexes, incomplete data
    // 2 - all fine, but only aggregated data
    // 3 - all fine, aggregated data together with details
	int CURRENT_PROTOCOL_VERSION = 3;
	
	
// DB schema related constants, used for SQLite and JSON
	
	String OBSERVATION_LATITUDE = "latitude";
	String OBSERVATION_LONGITUDE = "longitude";
	String OBSERVATION_START = "start_time";
	String OBSERVATION_FINISH = "finish_time";
	String OBSERVATION_UPLOADED = "uploaded";
	String DETAILS_UPLOADED = "details_uploaded";

	String USER_DEVICE = "device";
	String USER_USER_EMAIL = "user_email";
	
	String USER_PASSWORD_HASH = "pass_hash";
    
    int SMOKING_ID_NO_SMOKING = 1;
    int SMOKING_ID_ADULT_SMOKING_ALONE = 2;
    int SMOKING_ID_ADULT_SMOKING_OTHERS = 3;
    int SMOKING_ID_ADULT_SMOKING_CHILD = 4;
    
}