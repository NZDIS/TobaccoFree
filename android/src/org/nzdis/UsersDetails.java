package org.nzdis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents the user profile on the phone. 
 * Note, it does only contain user_email and password, 
 * the rest of the profile is stored on the server only.
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Jan 12, 2012 11:27:24 AM
 */
public class UsersDetails {

	private String user_email;
	private String hash;
	
	public UsersDetails(){}
	
	public UsersDetails(String username, String password,boolean hashed) throws NoSuchAlgorithmException {
		this.user_email = username;
		if (hashed) {
			this.hash = password;
		} else {
			this.hash = hashPassword(password);
		}
	}
	
	public String getUserEmail(){
		return user_email;
	}
	
	public String getPasswordHash(){
		return hash;
	}
	
	public void setPasswordHash(String password,boolean hashed) throws NoSuchAlgorithmException{
		if (hashed) {
			this.hash = password;
		} else {
			this.hash = hashPassword(password);
		}
	}
	
	public void setUserEmail(String email){
		this.user_email = email;
	}
	
	public String toString(){
		return "User email: " + user_email + " Password hash: " + hash;
	}
	
	/**
	 * Salt and hash the password.
	 *@return MD5 hash of the salted password. 
	 */
	private String hashPassword(final String password) throws NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		final String saltedPassword = Constants.HASH_SALT_PRE + password + Constants.HASH_SALT_POST;
		md.update(saltedPassword.getBytes());
		byte hash[] = md.digest();
		
		final StringBuffer hex = new StringBuffer();
		for(int i = 0;i < hash.length;i++){
			hex.append(Integer.toHexString(0xFF & hash[i]));
		}
		return hex.toString();
	}
	
}
