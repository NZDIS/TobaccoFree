package org.nzdis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UsersDetails {

	private String username;
	private String hash;
	
	public UsersDetails(){}
	
	public UsersDetails(String username, String password,boolean hashed) throws NoSuchAlgorithmException{
		this.username = username;
		if(hashed){
			this.hash = password;
		}else{
			this.hash = hashPassword(password);
		}
	}

	private String hashPassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		md.update(password.getBytes());
		byte hash[] = md.digest();
		
		StringBuffer hex = new StringBuffer();
		for(int i = 0;i < hash.length;i++){
			hex.append(Integer.toHexString(0xFF & hash[i]));
		}
		return hex.toString();
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPasswordHash(){
		return hash;
	}
	
	public void setPasswordHash(String password,boolean hashed) throws NoSuchAlgorithmException{
		if(hashed){
			this.hash = password;
		}else{
			this.hash = hashPassword(password);
		}
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String toString(){
		return "Username: " + username + " Password Hash: " + hash;
	}
}
