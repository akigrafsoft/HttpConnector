package org.akigrafsoft.apachehttpkonnector;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.util.EntityUtils;

import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

/**
 * 
 * @author kmoyse
 *
 */
public class ApacheHttpDataobject extends KonnectorDataobject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1225861194005663729L;

	/**
	 * Must be filled before being used by ClientKonnector <br>
	 * GET :
	 * 
	 * <pre>
	 * do.httpRequest = new HttpGet("/?msisdn='068966'");
	 * </pre>
	 * 
	 * POST :
	 * 
	 * <pre>
	 * o_dataobject.httpRequest = new HttpPost();
	 * ((HttpPost) o_dataobject.httpRequest).setEntity(new StringEntity(body, ContentType.create("text/xml", Consts.UTF_8)));
	 * do.httpRequest = post;
	 * </pre>
	 */
	transient public HttpRequest httpRequest;

	/**
	 * When used with a server, httpResponse is pre-filled with a default
	 * HttpResponse that needs to be completed :<br>
	 * <code>
	 * dataobject.httpResponse.setEntity(new StringEntity("...xml...",
	 * 					ContentType.create("text/xml", "UTF-8")));
	 * 	</code> <br>
	 * When used with client, it contains the HttpResponse received.
	 * 
	 */
	transient public HttpResponse httpResponse;

	public final boolean isCreatedByServer;

	transient HttpAsyncExchange httpAsyncExchange;

	transient private final CountDownLatch m_latch = new CountDownLatch(1);

	/**
	 * Constructor
	 * 
	 * @param message
	 *            message to be associated to this dataobject
	 */
	public ApacheHttpDataobject(final Message message) {
		super(message);
		this.isCreatedByServer = false;
	}

	/**
	 * Internal use, this sets <code>isCreatedByServer = true</code>
	 * 
	 * @param message
	 *            message to be associated to this dataobject
	 * @param konnectorName
	 *            name of Konnector that created this dataobject
	 */
	ApacheHttpDataobject(final Message message, String konnectorName) {
		super(message);
		this.isCreatedByServer = true;
		this.setKonnectorName(konnectorName);
	}

	boolean waitForReponse(long timeoutSeconds) throws InterruptedException {
		return m_latch.await(timeoutSeconds, TimeUnit.SECONDS);
	}

	void done() {
		if (m_latch != null)
			m_latch.countDown();
	}

	public String getHttpRequestBody() {
		String o_body = null;
		if ((httpRequest != null) && (httpRequest instanceof HttpEntityEnclosingRequest)) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
			try {
				o_body = EntityUtils.toString(entity);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		return o_body;
	}

	public String getHttpResponseBody() {
		String o_body = null;
		if (httpResponse != null) {
			HttpEntity entity = httpResponse.getEntity();
			try {
				o_body = EntityUtils.toString(entity);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		return o_body;
	}
}
