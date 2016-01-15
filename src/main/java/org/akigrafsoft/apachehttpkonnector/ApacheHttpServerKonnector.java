package org.akigrafsoft.apachehttpkonnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AUTH;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.EntityUtils;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class ApacheHttpServerKonnector extends Konnector {

	private static final String HTTPS = "https";
	private static final int SERVERSOCKET_TIMEOUT = 5000;

	private HttpServerConfig m_config;
	private URL m_url;

	private ServerSocket m_serversocket;
	private Thread m_listenerThread;

	Object m_latch;

	public ApacheHttpServerKonnector(String name) throws ExceptionDuplicate {
		super(name);
	}

	@Override
	public void doLoadConfig(KonnectorConfiguration config) {

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
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new HttpHandler());

		// Set up the HTTP service
		HttpService httpService = new HttpService(httpproc, reqistry);

		int port = m_url.getPort();

		SSLServerSocketFactory sf = null;
		if (m_url.getProtocol().equalsIgnoreCase(HTTPS)) {

			KeyManager[] keymanagers = null;

			// Initialize SSL context
			if (m_config.keyStore != null) {
				KeyManagerFactory kmfactory;
				try {
					KeyStore keyStore = KeyStore
							.getInstance(m_config.keyStore.type);
					keyStore.load(new FileInputStream(m_config.keyStore.path),
							m_config.keyStore.password.toCharArray());
					kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory
							.getDefaultAlgorithm());
					kmfactory.init(keyStore,
							m_config.keyStore.password.toCharArray());
				} catch (KeyStoreException | NoSuchAlgorithmException
						| CertificateException | IOException
						| UnrecoverableKeyException e) {
					e.printStackTrace();
					return CommandResult.Fail;
				}
				keymanagers = kmfactory.getKeyManagers();
			}

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
			sf = sslcontext.getServerSocketFactory();

			SSLServerSocket sslss;
			try {
				sslss = (SSLServerSocket) sf.createServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
				return CommandResult.Fail;
			}

			sslss.setNeedClientAuth(m_config.needClientAuthentication);

			m_serversocket = sslss;
		} else {
			try {
				m_serversocket = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
				return CommandResult.Fail;
			}
		}

		try {
			m_serversocket.setSoTimeout(SERVERSOCKET_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
			return CommandResult.Fail;
		}

		try {
			m_latch = new Object();
			m_listenerThread = new RequestListenerThread(m_url.getPort(),
					httpService, m_serversocket, m_latch);
			m_listenerThread.setDaemon(false);
			m_listenerThread.start();
		} catch (IOException e) {
			e.printStackTrace();
			return CommandResult.Fail;
		}

		setStarted();

		return CommandResult.Success;
	}

	static class RequestListenerThread extends Thread {

		private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
		private final ServerSocket serversocket;
		private final HttpService httpService;
		private final Object m_latch;

		public RequestListenerThread(final int port,
				final HttpService httpService, final ServerSocket serversocket,
				final Object latch) throws IOException {
			this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
			this.serversocket = serversocket;
			this.httpService = httpService;
			this.m_latch = latch;
		}

		@Override
		public void run() {
			System.out.println("Listening on port "
					+ this.serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = this.serversocket.accept();
					// System.out.println("Incoming connection from "
					// + socket.getInetAddress());
					HttpServerConnection conn = this.connFactory
							.createConnection(socket);
					// Use a network thread to service the connection
					executeInNetworkThread(new WorkerThread(this.httpService,
							conn));
				} catch (SocketTimeoutException se) {
					continue;
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					System.err
							.println("I/O error initialising connection thread: "
									+ e.getMessage());
					break;
				}
			}
			System.out.println("Stop listening on port "
					+ this.serversocket.getLocalPort());

			try {
				serversocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			synchronized (m_latch) {
				m_latch.notify();
			}
		}
	}

	static class WorkerThread implements Runnable {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
			// System.out.println("New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (HttpException ex) {
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
				}
			}
		}

	}

	public class HttpHandler implements HttpRequestHandler {

		@Override
		public void handle(HttpRequest httpRequest, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			try {
				if (m_config.authentication != null) {
					if (!performAuthentication(httpRequest, response, context))
						return;
				}
				// String method = httpRequest.getRequestLine().getMethod()
				// .toUpperCase(Locale.ENGLISH);
				// if (!method.equals("GET") && !method.equals("HEAD")
				// && !method.equals("POST") && !method.equals("PUT")) {
				// try {
				// response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				// response.setEntity(new StringEntity(
				// "Method not supported"));
				// } catch (UnsupportedEncodingException e) {
				// }
				//
				// ActivityLogger.warn(buildActivityLog(null,
				// "unsupported method<" + httpRequest + ">"));
				// return;
				// }
				handleRequest(httpRequest, response, context);
			} finally {

				// try to consume all content so the connection can be
				// reused
				//
				if (httpRequest instanceof HttpEntityEnclosingRequest) {
					HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest)
							.getEntity();
					if (entity != null) {
						try {
							EntityUtils.consume(entity);
						} catch (IOException e) {
							// nop
						}
					}
				}
			}

		}

		private boolean performAuthentication(HttpRequest request,
				HttpResponse response, HttpContext context) {
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
						+ m_config.authentication.realm + "\"");
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

			if (!m_config.authentication.username.equals(username)
					|| !m_config.authentication.password.equals(password)) {

				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				try {
					response.setEntity(new StringEntity("Authentication Failed"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return false;
			}

			if (m_config.authentication.realm != null
					&& !m_config.authentication.realm.isEmpty()) {

				String realm[] = h_realm.toString().split(" ");
				if (!m_config.authentication.realm.equals(realm[1])) {
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
				HttpResponse httpResponse, HttpContext context) {

			Message message = new Message();
			ApacheHttpDataobject l_dataobject = new ApacheHttpDataobject(
					message, getName());

			if (ActivityLogger.isDebugEnabled())
				ActivityLogger.debug(buildActivityLog(null, "handleRequest<"
						+ httpRequest + ">"));

			l_dataobject.httpRequest = httpRequest;
			l_dataobject.httpResponse = httpResponse;

			// StringEntity body = new StringEntity(xml, ContentType.create(
			// "text/html", "UTF-8"));
			// l_dataobject.httpResponse.setEntity(body);

			injectMessageInApplication(message, l_dataobject);

			boolean responded = false;
			try {
				responded = l_dataobject.waitForReponse(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (ActivityLogger.isDebugEnabled())
				if (responded) {
					ActivityLogger.debug(buildActivityLog(message,
							"responded <" + l_dataobject.httpResponse + ">"));
				} else {
					ActivityLogger.debug(buildActivityLog(message,
							"no response>"));
				}
		}
	}

	@Override
	public void doHandle(KonnectorDataobject dataobject) {
		ApacheHttpDataobject l_dataobject = (ApacheHttpDataobject) dataobject;
		// notify Server thread that it can resume
		l_dataobject.done();
		// TODO : according to notification succeeded or not we should resume
		// technical error or success
		resumeWithExecutionComplete(l_dataobject);
	}

	@Override
	protected CommandResult doStop() {

		m_listenerThread.interrupt();

		// Wait for threads to finish
		try {
			synchronized (m_latch) {
				while (!m_serversocket.isClosed())
					m_latch.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return CommandResult.Fail;
		}

		return CommandResult.Success;
	}

}
