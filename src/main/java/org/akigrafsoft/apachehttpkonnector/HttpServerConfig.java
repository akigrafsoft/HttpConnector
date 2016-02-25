/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.apachehttpkonnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;

/**
 * Configuration class for {@link ApacheHttpServerKonnector}
 * <p>
 * <b>This is a Java bean and all extension classes MUST be Java beans.</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class HttpServerConfig extends KonnectorConfiguration {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1022416702344917100L;

	public String url = "http://localhost:8080";
	public AuthenticationConfig authentication = null;
	public boolean needClientAuthentication = false;
	public KeyStoreConfig keyStore = null;
	public KeyStoreConfig trustStore = null;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public AuthenticationConfig getAuthentication() {
		return authentication;
	}

	public void setAuthentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
	}

	public boolean isNeedClientAuthentication() {
		return needClientAuthentication;
	}

	public void setNeedClientAuthentication(boolean needClientAuthentication) {
		this.needClientAuthentication = needClientAuthentication;
	}

	public KeyStoreConfig getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(KeyStoreConfig keyStore) {
		this.keyStore = keyStore;
	}

	public KeyStoreConfig getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(KeyStoreConfig trustStore) {
		this.trustStore = trustStore;
	}

	// ------------------------------------------------------------------------
	// Fluent API

	public HttpServerConfig url(String value) {
		this.url = value;
		return this;
	}

	public HttpServerConfig authentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
		return this;
	}

	public HttpServerConfig keyStore(String type, String path, String password) {
		// this.keyStore = new KeyStoreConfig(type, path, password);
		this.keyStore = new KeyStoreConfig();
		this.keyStore.setType(type);
		this.keyStore.setPath(path);
		this.keyStore.setPassword(password);
		return this;
	}

	public HttpServerConfig trustStore(String type, String path, String password) {
		// this.trustStore = new KeyStoreConfig(type, path, password);

		this.trustStore = new KeyStoreConfig();
		this.trustStore.setType(type);
		this.trustStore.setPath(path);
		this.trustStore.setPassword(password);
		return this;
	}

	// ------------------------------------------------------------------------

	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		try {
			new URL(this.url);
		} catch (MalformedURLException e) {
			throw new ExceptionAuditFailed("MalformedURLException:" + e.getMessage());
		}

		if (authentication != null) {
			if ((authentication.getUsername() == null) || authentication.getUsername().isEmpty()) {
				throw new ExceptionAuditFailed("authentication.username must be provided and non empty");
			}
			if ((authentication.getPassword() == null) || authentication.getPassword().isEmpty()) {
				throw new ExceptionAuditFailed("authentication.password must be provided and non empty");
			}
		}

		if (keyStore != null) {
			if ((keyStore.getType() == null) || keyStore.getType().isEmpty()) {
				throw new ExceptionAuditFailed("keyStore.type must be provided and non empty");
			}
			if ((keyStore.getPath() == null) || keyStore.getPath().isEmpty()) {
				throw new ExceptionAuditFailed("keyStore.path must be provided and non empty");
			}
			if ((keyStore.getPassword() == null) || keyStore.getPassword().isEmpty()) {
				throw new ExceptionAuditFailed("keyStore.password must be provided and non empty");
			}
		}

		if (trustStore != null) {
			if ((trustStore.getType() == null) || trustStore.getType().isEmpty()) {
				throw new ExceptionAuditFailed("trustStore.type must be provided and non empty");
			}
			if ((trustStore.getPath() == null) || trustStore.getPath().isEmpty()) {
				throw new ExceptionAuditFailed("trustStore.path must be provided and non empty");
			}
			if ((trustStore.getPassword() == null) || trustStore.getPassword().isEmpty()) {
				throw new ExceptionAuditFailed("trustStore.password must be provided and non empty");
			}
		}

	}
}
