package org.nzdis;

public class UsernameNotSetException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsernameNotSetException(String message){
		super(message);
	}
}
