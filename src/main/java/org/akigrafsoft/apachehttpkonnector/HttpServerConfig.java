package org.akigrafsoft.apachehttpkonnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;

public class HttpServerConfig extends KonnectorConfiguration {
	public String url = "http://localhost:8080";
	public AuthenticationConfig authentication = null;
	public boolean needClientAuthentication = false;
	public KeyStoreConfig keyStore = null;
	public KeyStoreConfig trustStore = null;

	// ------------------------------------------------------------------------
	// Fluent API
	/**
	 * Set URL to listen on
	 * 
	 * @param value
	 * @return
	 */
	public HttpServerConfig url(String value) {
		this.url = value;
		return this;
	}

	/**
	 * 
	 * @param authentication
	 * @return
	 */
	public HttpServerConfig authentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
		return this;
	}

	/**
	 * 
	 * @param type
	 * @param path
	 * @param password
	 * @return
	 */
	public HttpServerConfig keyStore(String type, String path, String password) {
		this.keyStore = new KeyStoreConfig(type, path, password);
		return this;
	}

	/**
	 * 
	 * @param type
	 * @param path
	 * @param password
	 * @return
	 */
	public HttpServerConfig trustStore(String type, String path, String password) {
		this.trustStore = new KeyStoreConfig(type, path, password);
		return this;
	}

	// ------------------------------------------------------------------------

	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		try {
			new URL(this.url);
		} catch (MalformedURLException e) {
			throw new ExceptionAuditFailed("MalformedURLException:"
					+ e.getMessage());
		}

		if (authentication != null) {
			if ((authentication.username == null)
					|| authentication.username.isEmpty()) {
				throw new ExceptionAuditFailed(
						"authentication.username must be provided and non empty");
			}
			if ((authentication.password == null)
					|| authentication.password.isEmpty()) {
				throw new ExceptionAuditFailed(
						"authentication.password must be provided and non empty");
			}
		}

		if (keyStore != null) {
			if ((keyStore.type == null) || keyStore.type.isEmpty()) {
				throw new ExceptionAuditFailed(
						"keyStore.type must be provided and non empty");
			}
			if ((keyStore.path == null) || keyStore.path.isEmpty()) {
				throw new ExceptionAuditFailed(
						"keyStore.path must be provided and non empty");
			}
			if ((keyStore.password == null) || keyStore.password.isEmpty()) {
				throw new ExceptionAuditFailed(
						"keyStore.password must be provided and non empty");
			}
		}

		if (trustStore != null) {
			if ((trustStore.type == null) || trustStore.type.isEmpty()) {
				throw new ExceptionAuditFailed(
						"trustStore.type must be provided and non empty");
			}
			if ((trustStore.path == null) || trustStore.path.isEmpty()) {
				throw new ExceptionAuditFailed(
						"trustStore.path must be provided and non empty");
			}
			if ((trustStore.password == null) || trustStore.password.isEmpty()) {
				throw new ExceptionAuditFailed(
						"trustStore.password must be provided and non empty");
			}
		}

	}
}
