package org.akigrafsoft.apachehttpkonnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnectorConfiguration;

public class HttpClientConfig extends
		SessionBasedClientKonnectorConfiguration {

	/**
	 * Fully qualified URL
	 * 
	 */
	public String url = "http://localhost:8080";

	// public HashMap<String, String> headers = new HashMap<String, String>();

	public AuthenticationConfig authentication = null;

	public ProxyConfig proxy = null;

	public KeyStoreConfig keyStore = null;
	public KeyStoreConfig trustStore = null;

	// ------------------------------------------------------------------------
	// Fluent API

	public HttpClientConfig url(String value) {
		this.url = value;
		return this;
	}

	public HttpClientConfig authentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
		return this;
	}

	/**
	 * Set proxy
	 * 
	 * @param proxy
	 * @return
	 */
	public HttpClientConfig proxy(ProxyConfig proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 * 
	 * @param type
	 * @param path
	 * @param password
	 * @return
	 */
	public HttpClientConfig keyStore(String type, String path,
			String password) {
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
	public HttpClientConfig trustStore(String type, String path,
			String password) {
		this.trustStore = new KeyStoreConfig(type, path, password);
		return this;
	}

	// ------------------------------------------------------------------------

	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		// if ((hostName == null) || hostName.equals("")) {
		// throw new ExceptionAuditFailed(
		// "hostName must be provided and non empty");
		// }
		// if (port <= 0) {
		// throw new ExceptionAuditFailed("port must be > 0");
		// }
		// if ((url == null) || !url.startsWith("/")) {
		// throw new ExceptionAuditFailed("url must start with '/'");
		// }

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

		if (proxy != null) {
			if (proxy.port <= 0) {
				throw new ExceptionAuditFailed("proxyPort must be > 0");
			}
			if ((proxy.scheme == null)
					|| proxy.scheme.equals("")
					|| (!proxy.scheme.equals("http") && !proxy.scheme
							.equals("https"))) {
				throw new ExceptionAuditFailed(
						"proxyScheme must be http or https");
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
