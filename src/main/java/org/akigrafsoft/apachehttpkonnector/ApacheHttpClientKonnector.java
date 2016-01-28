package org.akigrafsoft.apachehttpkonnector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.konnector.ExceptionCreateSessionFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnector;

public class ApacheHttpClientKonnector extends SessionBasedClientKonnector {

	private HttpClientConfig m_config = null;

	private HttpHost httpHost = null;
	private CloseableHttpClient httpClient = null;
	private AuthCache authCache = null;

	public ApacheHttpClientKonnector(String name) throws ExceptionDuplicate {
		super(name);
	}

	@Override
	public Class<? extends KonnectorConfiguration> getConfigurationClass() {
		return HttpClientConfig.class;
	}

	@Override
	public void doLoadConfig(KonnectorConfiguration config) {
		super.doLoadConfig(config);

		HttpClientConfig l_config = (HttpClientConfig) config;
		m_config = l_config;
	}

	@Override
	protected void execute(KonnectorDataobject dataobject, Session session) {
		HttpClientContext httpClientContext = (HttpClientContext) session
				.getUserObject();
		ApacheHttpDataobject l_dataobject = (ApacheHttpDataobject) dataobject;

		try {
			if (ActivityLogger.isDebugEnabled())
				ActivityLogger.debug(this.buildActivityLog(
						dataobject.getMessage(), "send<"
								+ l_dataobject.httpRequest + ">"));

			l_dataobject.httpResponse = httpClient.execute(httpHost,
					l_dataobject.httpRequest, httpClientContext);

			if (ActivityLogger.isDebugEnabled())
				ActivityLogger.debug(this.buildActivityLog(
						dataobject.getMessage(), "received<"
								+ l_dataobject.httpResponse + ">"));
			this.notifyExecuteCompleted(dataobject);
		} catch (IOException e) {
			e.printStackTrace();
			this.notifyNetworkError(l_dataobject, session, e.getMessage());
		} finally {
			HttpRequestBase reqBase = (HttpRequestBase) l_dataobject.httpRequest;
			reqBase.releaseConnection();
		}
	}

	@Override
	protected CommandResult preSessionCreation() {
		URL l_url;
		try {
			l_url = new URL(m_config.getUrl());
		} catch (MalformedURLException e) {
			// Should not happen if audit does it !
			e.printStackTrace();
			return CommandResult.Fail;
		}

		httpHost = new HttpHost(l_url.getHost(), l_url.getPort(),
				l_url.getProtocol());

		PoolingHttpClientConnectionManager connManager;

		if (l_url.getProtocol().equalsIgnoreCase("https")) {
			SSLContextBuilder sslcb = SSLContexts.custom();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
			if (m_config.getTrustStore() != null) {
				KeyStore truststore;
				try {
					truststore = KeyStore.getInstance(m_config.getTrustStore()
							.getType());
					truststore.load(new FileInputStream(m_config
							.getTrustStore().getPath()), m_config
							.getTrustStore().getPassword().toCharArray());
					TrustStrategy trustStrategy = new TrustSelfSignedStrategy();
					sslcb.loadTrustMaterial(truststore, trustStrategy);
				} catch (KeyStoreException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (CertificateException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (m_config.getKeyStore() != null) {
				KeyStore keyStore;
				try {
					keyStore = KeyStore.getInstance(m_config.getKeyStore()
							.getType());
					keyStore.load(new FileInputStream(m_config.getKeyStore()
							.getPath()), m_config.getKeyStore().getPassword()
							.toCharArray());
					sslcb.loadKeyMaterial(keyStore, m_config.getKeyStore()
							.getPassword().toCharArray(), null);
				} catch (KeyStoreException | NoSuchAlgorithmException
						| CertificateException | IOException
						| UnrecoverableKeyException e) {
					e.printStackTrace();
				}
			}

			SSLContext sslc;
			try {
				sslc = sslcb.build();
			} catch (KeyManagementException | NoSuchAlgorithmException e) {
				e.printStackTrace();
				return CommandResult.Fail;
			}

			X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
			socketFactoryRegistry = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register(
							"https",
							new SSLConnectionSocketFactory(sslc,
									hostnameVerifier))
					// need in case of connection through HTTP proxy :
					.register("http", new PlainConnectionSocketFactory())
					.build();
			connManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
		} else {
			connManager = new PoolingHttpClientConnectionManager();
		}

		connManager.setDefaultMaxPerRoute(m_config.getNumberOfSessions());

		HttpClientBuilder hcBuilder = HttpClients.custom();
		hcBuilder.setConnectionManager(connManager);

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		if (m_config.getAuthentication() != null) {
			AuthCache authCache = new BasicAuthCache();
			authCache.put(httpHost, new BasicScheme());
			credentialsProvider.setCredentials(
					new AuthScope(httpHost.getHostName(), httpHost.getPort(),
							m_config.getAuthentication().getRealm()),
					new UsernamePasswordCredentials(m_config
							.getAuthentication().getUsername(), m_config
							.getAuthentication().getPassword()));
		}

		if (m_config.getProxy() != null) {
			HttpHost proxy = new HttpHost(m_config.getProxy().getHost(),
					m_config.getProxy().getPort(), m_config.getProxy()
							.getScheme());
			// hcBuilder.setProxy(proxy);
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(
					proxy);
			hcBuilder.setRoutePlanner(routePlanner);
			if (m_config.getProxy().getAuthentication() != null) {
				credentialsProvider.setCredentials(new AuthScope(m_config
						.getProxy().getHost(), m_config.getProxy().getPort(),
						m_config.getProxy().getAuthentication().getRealm()),
						new UsernamePasswordCredentials(m_config.getProxy()
								.getAuthentication().getUsername(), m_config
								.getProxy().getAuthentication().getPassword()));
			}
		}

		hcBuilder.setDefaultCredentialsProvider(credentialsProvider);

		httpClient = hcBuilder.build();
		return CommandResult.Success;
	};

	@Override
	protected void createSession(Session session)
			throws ExceptionCreateSessionFailed {
		HttpClientContext httpClientContext;

		if (authCache != null) {
			httpClientContext = HttpClientContext.create();
			httpClientContext.setAuthCache(authCache);
		} else {
			httpClientContext = HttpClientContext.adapt(new BasicHttpContext());
		}
		session.setUserObject(httpClientContext);
	}

	@Override
	public void async_startSession(Session session) {
		this.sessionStarted(session);
	}

	@Override
	protected void async_stopSession(Session session) {
		this.sessionStopped(session);
	}

	@Override
	protected void onAllSessionStopped() {
		HttpClientUtils.closeQuietly(httpClient);
		super.onAllSessionStopped();
	};

}
