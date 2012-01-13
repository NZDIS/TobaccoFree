/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package org.nzdis;

/**
 * Represents a generic Database exception.
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Dec 2011
 */
public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatabaseException(String message){
		super(message);
	}
	
}
