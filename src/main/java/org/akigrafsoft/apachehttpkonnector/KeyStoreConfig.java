/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.apachehttpkonnector;

import java.io.Serializable;

/**
 * Holds either keyStore or trustStore information
 * <p>
 * <b>This is a Java bean and all extension classes MUST be Java beans.</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class KeyStoreConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4072995081831491759L;

	private String type;
	private String path;
	private String password;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
