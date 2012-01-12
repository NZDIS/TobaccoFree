/**
 * Copyright (C) 2011-2012 NZDIS. All Rights Reserved. See LICENCE.
 */
package org.nzdis;

/**
 * Constants for the project. 
 * 
 * 
 * @author Mariusz Nowostawski <mariusz@ngarua.com>
 *
 * @version $Revision$ <br>
 * Created: Jan 12, 2012 11:44:42 AM
 */
public interface Constants {

// WEB API URLs
	
	String URL_OBSERVATION_ADD = "http://globalink.nzdis.org/observation/add";
	// For local testing on home LAN
	// String URL_OBSERVATION_ADD = "http://192.168.2.200:8000/observation/add";

// DB schema related constants, used for SQLite and JSON
	
	String OBSERVATION_LATITUDE = "latitude";
	String OBSERVATION_LONGITUDE = "longitude";
	String OBSERVATION_START = "start_time";
	String OBSERVATION_FINISH = "finish_time";
	String OBSERVATION_UPLOADED = "uploaded";

	String USER_DEVICE = "device";
	String USER_USER_EMAIL = "user_email";
	
	String USER_PASSWORD_HASH = "pass_hash";
	
	String HASH_SALT_PRE = "$709dfgssd*";
	String HASH_SALT_POST = "2356lkgjzxvhdsfg";
}