package org.akigrafsoft.apachehttpkonnector;

/**
 * Holds either keyStore or trustStore information
 * 
 * @author kmoyse
 * 
 */
public class KeyStoreConfig {
	public final String type;
	public final String path;
	public final String password;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param path
	 * @param password
	 */
	public KeyStoreConfig(String type, String path, String password) {
		super();
		this.type = type;
		this.path = path;
		this.password = password;
	}
}
