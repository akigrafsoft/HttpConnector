/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
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
public class ProxyConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7709241563729115250L;

	private String host;
	private int port;
	private String scheme;

	private AuthenticationConfig authentication = null;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public AuthenticationConfig getAuthentication() {
		return authentication;
	}

	public void setAuthentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
	}

}
