/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

/**
 * Represents a generic exception when Globalink credentials are not 
 * present in the user account settings.
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Jan 13, 2012 12:55:40 PM
 */
public class UsernameNotSetException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsernameNotSetException(String message){
		super(message);
	}

}
