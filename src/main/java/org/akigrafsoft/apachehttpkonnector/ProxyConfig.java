package org.akigrafsoft.apachehttpkonnector;

/**
 * 
 * @author kmoyse
 * 
 */
public class ProxyConfig {
	public final String host;
	public final int port;
	public final String scheme;

	public AuthenticationConfig authentication = null;

	/**
	 * 
	 * @param proxyHost
	 * @param proxyPort
	 * @param proxyScheme
	 */
	public ProxyConfig(String proxyHost, int proxyPort, String proxyScheme) {
		super();
		this.host = proxyHost;
		this.port = proxyPort;
		this.scheme = proxyScheme;
	}

	/**
	 * 
	 * @param authentication
	 * @return
	 */
	public ProxyConfig setAuthentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
		return this;
	}

}
