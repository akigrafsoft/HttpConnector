/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.apachehttpkonnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.auth.AUTH;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class ApacheNHttpServerKonnector extends Konnector {

	private static final String HTTPS = "https";
	private static final int SERVERSOCKET_TIMEOUT = 5000;

	private HttpServerConfig m_config;
	private URL m_url;

	private Thread m_listenerThread;
	private ListeningIOReactor m_ioReactor;

	protected ApacheNHttpServerKonnector(String name) throws ExceptionDuplicate {
		super(name);
	}

	@Override
	public Class<? extends KonnectorConfiguration> getConfigurationClass() {
		return HttpServerConfig.class;
	}

	@Override
	public void doLoadConfig(KonnectorConfiguration config) {
		// super.doLoadConfig(config);

		HttpServerConfig l_config = (HttpServerConfig) config;

		try {
			m_url = new URL(l_config.url);
		} catch (MalformedURLException e) {
			// should not happen as it was audited
		}

		m_config = l_config;
	}

	@Override
	protected CommandResult doStart() {
		// Set up the HTTP protocol processor
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate())
				.add(new ResponseServer("HTTPKonnector/1.1"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Set up request handlers
		// UriHttpRequestHandlerMapper reqistry = new
		// UriHttpRequestHandlerMapper();
		UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
		reqistry.register("*", new HttpHandler());

		// Set up the HTTP service
		// HttpService httpService = new HttpService(httpproc, reqistry);

		HttpAsyncService protocolHandler = new HttpAsyncService(httpproc,
				reqistry) {
			@Override
			public void connected(final NHttpServerConnection conn) {
				System.out.println(conn + ": connection open");
				super.connected(conn);
			}

			@Override
			public void closed(final NHttpServerConnection conn) {
				System.out.println(conn + ": connection closed");
				super.closed(conn);
			}
		};

		// ServerSocket serversocket;

		NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;

		// SSLServerSocketFactory sf = null;
		if (m_url.getProtocol().equalsIgnoreCase(HTTPS)) {

			KeyManager[] keymanagers = null;

			if (m_config.keyStore != null) {
				KeyManagerFactory kmfactory;
				try {
					KeyStore keyStore = KeyStore.getInstance(m_config.keyStore
							.getType());
					keyStore.load(
							new FileInputStream(m_config.keyStore.getPath()),
							m_config.keyStore.getPassword().toCharArray());
					kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory
							.getDefaultAlgorithm());
					kmfactory.init(keyStore, m_config.keyStore.getPassword()
							.toCharArray());
				} catch (KeyStoreException | NoSuchAlgorithmException
						| CertificateException | IOException
						| UnrecoverableKeyException e) {
					e.printStackTrace();
					return CommandResult.Fail;
				}
				keymanagers = kmfactory.getKeyManagers();
			}

			// Initialize SSL context
			SSLContext sslcontext;
			try {
				sslcontext = SSLContext.getInstance("TLS");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return CommandResult.Fail;
			}
			try {
				sslcontext.init(keymanagers, null, null);
			} catch (KeyManagementException e) {
				e.printStackTrace();
				return CommandResult.Fail;
			}
			// sf = sslcontext.getServerSocketFactory();
			connFactory = new SSLNHttpServerConnectionFactory(sslcontext, null,
					ConnectionConfig.DEFAULT);

			// SSLServerSocket sslss;
			// try {
			// sslss = (SSLServerSocket) sf.createServerSocket(port);
			// } catch (IOException e) {
			// e.printStackTrace();
			// return CommandResult.Fail;
			// }
			//
			// sslss.setNeedClientAuth(m_config.needClientAuthentication);
			// TODO : how to do needClientAuthentication?

			// serversocket = sslss;
		} else {
			// try {
			// serversocket = new ServerSocket(port);
			// } catch (IOException e) {
			// e.printStackTrace();
			// return CommandResult.Fail;
			// }
			connFactory = new DefaultNHttpServerConnectionFactory(
					ConnectionConfig.DEFAULT);
		}

		// Create server-side I/O event dispatch
		IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(
				protocolHandler, connFactory);
		// Set I/O reactor defaults
		IOReactorConfig config = IOReactorConfig.custom().setIoThreadCount(1)
				.setSoTimeout(SERVERSOCKET_TIMEOUT).setConnectTimeout(3000)
				.build();

		// try {
		// serversocket.setSoTimeout(SERVERSOCKET_TIMEOUT);
		// } catch (SocketException e) {
		// e.printStackTrace();
		// return CommandResult.Fail;
		// }

		// Create server-side I/O reactor
		try {
			m_ioReactor = new DefaultListeningIOReactor(config);
		} catch (IOReactorException e) {
			e.printStackTrace();
			return CommandResult.Fail;
		}

		m_listenerThread = new RequestListenerThread(m_ioReactor,
				ioEventDispatch, m_url.getPort());
		m_listenerThread.setDaemon(false);
		m_listenerThread.start();

		return CommandResult.Success;
	}

	class RequestListenerThread extends Thread {

		private final ListeningIOReactor ioReactor;
		private final IOEventDispatch ioEventDispatch;
		private final int port;

		public RequestListenerThread(final ListeningIOReactor ioReactor,
				IOEventDispatch ioEventDispatch, final int port) {
			this.ioReactor = ioReactor;
			this.ioEventDispatch = ioEventDispatch;
			this.port = port;
		}

		@Override
		public void run() {
			System.out.println("Listening on port " + this.port);
			ApacheNHttpServerKonnector.this.setStarted();
			try {
				// Listen of the given port
				ioReactor.listen(new InetSocketAddress(port));
				// Ready to go!
				ioReactor.execute(ioEventDispatch);
			} catch (InterruptedIOException ex) {
				System.err.println("Interrupted");
			} catch (IOException e) {
				System.err.println("I/O error: " + e.getMessage());
			}
			ApacheNHttpServerKonnector.this.setStopped();
			System.out.println("Stop listening on port " + this.port);
		}
	}

	public class HttpHandler implements HttpAsyncRequestHandler<HttpRequest> {

		@Override
		public void handle(HttpRequest httpRequest,
				HttpAsyncExchange httpAsyncExchange, HttpContext httpContext)
				throws HttpException, IOException {

			// HttpCoreContext coreContext = HttpCoreContext.adapt(httpContext);
			// NHttpConnection conn = coreContext
			// .getConnection(NHttpConnection.class);

			HttpResponse httpResponse = httpAsyncExchange.getResponse();

			if (m_config.authentication != null) {
				if (!performAuthentication(httpRequest, httpResponse)) {
					httpAsyncExchange
							.submitResponse(new BasicAsyncResponseProducer(
									httpResponse));
					return;
				}
			}

			String method = httpRequest.getRequestLine().getMethod()
					.toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD")
					&& !method.equals("POST")) {
				throw new MethodNotSupportedException(method
						+ " method not supported");
			}

			handleRequest(httpRequest, httpAsyncExchange, httpResponse);
		}

		@Override
		public HttpAsyncRequestConsumer<HttpRequest> processRequest(
				HttpRequest arg0, HttpContext arg1) throws HttpException,
				IOException {
			// Buffer request content in memory for simplicity
			return new BasicAsyncRequestConsumer();
		}

		private boolean performAuthentication(HttpRequest request,
				HttpResponse response) {
			Header h_auth = request.getFirstHeader(AUTH.WWW_AUTH_RESP);

			// If the request doesn't contain an "Authorization"
			// header, then we need to request the sender to
			// authenticate. We do that by sending back a
			// "WWW-Authenticate" header and the configured
			// realm
			//
			if (h_auth == null) {
				response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
				response.addHeader(AUTH.WWW_AUTH, "Basic realm=\""
						+ m_config.authentication.getRealm() + "\"");
				return false;
			}

			// header contains authorization data. Decode and
			// compare to configured values...
			//
			Header h_realm = request.getFirstHeader("Realm");

			int i = h_auth.toString().lastIndexOf(" ") + 1;
			String tmp = h_auth.toString().substring(i);

			Base64 decoder = new Base64();
			byte[] decodedBytes = decoder.decode(tmp);
			String credentials = new String(decodedBytes);
			String username = credentials.split(":")[0];
			String password = credentials.split(":")[1];

			if (!m_config.authentication.getUsername().equals(username)
					|| !m_config.authentication.getPassword().equals(password)) {

				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				try {
					response.setEntity(new StringEntity("Authentication Failed"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return false;
			}

			if (m_config.authentication.getRealm() != null
					&& !m_config.authentication.getRealm().isEmpty()) {

				String realm[] = h_realm.toString().split(" ");
				if (!m_config.authentication.getRealm().equals(realm[1])) {
					response.setStatusCode(HttpStatus.SC_FORBIDDEN);
					try {
						response.setEntity(new StringEntity(
								"Authentication Failed"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					return false;
				}
			}
			return true;
		}

		protected void handleRequest(HttpRequest httpRequest,
				HttpAsyncExchange httpAsyncExchange, HttpResponse httpResponse) {

			Message message = new Message();
			ApacheHttpDataobject l_dataobject = new ApacheHttpDataobject(
					message, getName());

			if (ActivityLogger.isDebugEnabled())
				ActivityLogger.debug(buildActivityLog(null, "handleRequest<"
						+ httpRequest + ">"));

			l_dataobject.httpRequest = httpRequest;
			l_dataobject.httpAsyncExchange = httpAsyncExchange;
			l_dataobject.httpResponse = httpResponse;

			injectMessageInApplication(message, l_dataobject);
		}

	}

	@Override
	public void doHandle(KonnectorDataobject dataobject) {
		ApacheHttpDataobject l_dataobject = (ApacheHttpDataobject) dataobject;

		if (ActivityLogger.isDebugEnabled())
			ActivityLogger.debug(buildActivityLog(dataobject.getMessage(),
					"submitResponse<" + l_dataobject.httpResponse + ">"));

		try {
			if (l_dataobject.httpAsyncExchange == null) {
				resumeWithNetworkError(l_dataobject,
						"transient httpAsyncExchange == null");
				return;
			}
			l_dataobject.httpAsyncExchange
					.submitResponse(new BasicAsyncResponseProducer(
							l_dataobject.httpResponse));
		} catch (IllegalStateException e) {
			e.printStackTrace();
			resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		}

		resumeWithExecutionComplete(l_dataobject);
	}

	@Override
	protected CommandResult doStop() {
		try {
			m_ioReactor.shutdown(2000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_listenerThread.interrupt();
		return CommandResult.Success;
	}

}
