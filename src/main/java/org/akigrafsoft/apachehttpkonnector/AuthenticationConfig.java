package org.akigrafsoft.apachehttpkonnector;

import java.io.Serializable;

/**
 * 
 * <p>
 * <b>This is a Java bean and all extension classes MUST be Java beans.</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class AuthenticationConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2006411919662820169L;

	private String realm;
	private String username;
	private String password;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
