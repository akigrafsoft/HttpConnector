package org.akigrafsoft.apachehttpkonnector;

import java.net.MalformedURLException;
import java.net.URL;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnectorConfiguration;

/**
 * Configuration class for {@link ApacheHttpClientKonnector}
 * <p>
 * <b>This is a Java bean and all extension classes MUST be Java beans.</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class HttpClientConfig extends SessionBasedClientKonnectorConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7572328556052053031L;

	/**
	 * Fully qualified URL
	 * 
	 */
	private String url = "http://localhost:8080";

	// public HashMap<String, String> headers = new HashMap<String, String>();

	private AuthenticationConfig authentication = null;

	private ProxyConfig proxy = null;

	private KeyStoreConfig keyStore = null;
	private KeyStoreConfig trustStore = null;

	// ------------------------------------------------------------------------
	// Java bean

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

	public ProxyConfig getProxy() {
		return proxy;
	}

	public void setProxy(ProxyConfig proxy) {
		this.proxy = proxy;
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

	public HttpClientConfig url(String value) {
		this.url = value;
		return this;
	}

	public HttpClientConfig authentication(AuthenticationConfig authentication) {
		this.authentication = authentication;
		return this;
	}

	public HttpClientConfig proxy(ProxyConfig proxy) {
		this.proxy = proxy;
		return this;
	}

	public HttpClientConfig keyStore(String type, String path, String password) {
		// this.keyStore = new KeyStoreConfig(type, path, password);
		this.keyStore = new KeyStoreConfig();
		this.keyStore.setType(type);
		this.keyStore.setPath(path);
		this.keyStore.setPassword(password);
		return this;
	}

	public HttpClientConfig trustStore(String type, String path, String password) {
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

		if (proxy != null) {
			if (proxy.getPort() <= 0) {
				throw new ExceptionAuditFailed("proxyPort must be > 0");
			}
			if ((proxy.getScheme() == null) || proxy.getScheme().equals("")
					|| (!proxy.getScheme().equals("http") && !proxy.getScheme().equals("https"))) {
				throw new ExceptionAuditFailed("proxyScheme must be http or https");
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
