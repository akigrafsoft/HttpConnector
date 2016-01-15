package org.akigrafsoft.apachehttpkonnector;

/**
 * 
 * @author kmoyse
 * 
 */
public class AuthenticationConfig {
	public final String realm;
	public final String username;
	public final String password;

	/**
	 * @param realm
	 * @param username
	 * @param password
	 */
	public AuthenticationConfig(String realm, String username, String password) {
		super();
		this.realm = realm;
		this.username = username;
		this.password = password;
	}
}
