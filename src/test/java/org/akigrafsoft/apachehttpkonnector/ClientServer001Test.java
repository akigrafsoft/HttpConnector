package org.akigrafsoft.apachehttpkonnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.akigrafsoft.knetthreads.Dispatcher;
import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.FlowProcessContext;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.RequestEnum;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class ClientServer001Test {
	static String SERVER_NAME = "Server01";
	static String CLIENT_NAME = "Client01";

	// private final static XStream xstream = new XStream();

	static ApacheHttpClientKonnector m_clientKonnector;
	static Konnector m_serverKonnector;

	static int port = Utils.findFreePort();

	class Received {
		Message message;
		KonnectorDataobject dataobject;

		public Received(Message message, KonnectorDataobject dataobject) {
			super();
			this.message = message;
			this.dataobject = dataobject;
		}
	}

	static Received received;

	@BeforeClass
	public static void setUpClass() {
		try {
			m_clientKonnector = new ApacheHttpClientKonnector(CLIENT_NAME);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		try {
			m_serverKonnector = new ApacheHttpServerKonnector(SERVER_NAME);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		m_clientKonnector.destroy();
		m_serverKonnector.destroy();
	}

	@Test
	public void testKonnectorImplementation() {
		System.out.println("// -- testKonnectorImplementation");
		assertEquals(HttpServerConfig.class,
				m_serverKonnector.getConfigurationClass());

		assertEquals(HttpClientConfig.class,
				m_clientKonnector.getConfigurationClass());
	}

	@Test
	public void test() {

		try {
			m_serverKonnector.configure(new HttpServerConfig()
					.url("http://localhost:" + port));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		try {
			Endpoint nap = new Endpoint("test") {
				@Override
				public KonnectorRouter getKonnectorRouter(Message message,
						KonnectorDataobject dataobject) {
					return new KonnectorRouter() {
						public Konnector resolveKonnector(Message message,
								KonnectorDataobject dataobject) {
							return m_serverKonnector;
						}
					};
				}

				@Override
				public RequestEnum classifyInboundMessage(Message message,
						KonnectorDataobject dataobject) {
					received = new Received(message, dataobject);

					// Fake flow by submitting a response directly
					// TODO should do that in a different threads also
					// to check it works!
					ApacheHttpDataobject l_dataobject = (ApacheHttpDataobject) dataobject;

					String xml = "<ok/>";
					System.out.println(this.getClass().getName() + "|encoded: "
							+ xml);
					StringEntity body = new StringEntity(xml,
							ContentType.create("text/xml", "UTF-8"));
					l_dataobject.httpResponse.setEntity(body);
					System.out.println(l_dataobject.httpResponse);

					System.out.println(new Date() + "|WAIT before Handle...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					m_serverKonnector.handle(dataobject);

					// leave null as this is fake anyway
					return null;
				}
			};
			nap.setDispatcher(new Dispatcher<RequestEnum>("foo") {
				@Override
				public FlowProcessContext getContext(Message message,
						KonnectorDataobject dataobject, RequestEnum request) {
					return null;
				}
			});
			m_serverKonnector.setEndpoint(nap);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			m_clientKonnector.configure(new HttpClientConfig()
					.url("http://localhost:" + port));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.start());
		assertEquals(Konnector.CommandResult.Success, m_clientKonnector.start());

		Utils.sleep(2);

		Message message = new Message();

		System.out.println("TEST ASYNC MODE");
		{
			ApacheHttpDataobject dataobject = new ApacheHttpDataobject(message);
			dataobject.operationMode = KonnectorDataobject.OperationMode.TWOWAY;
			dataobject.httpRequest = new HttpGet(
					"/?msisdn='0689'&transactionId='t001'");
			message.associateDataobject("test", dataobject);

			dataobject.operationSyncMode = KonnectorDataobject.SyncMode.ASYNC;
			m_clientKonnector.handle(dataobject);
			System.out.println(new Date() + "|WAIT...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			String o_body;
			try {
				o_body = EntityUtils.toString(dataobject.httpResponse
						.getEntity());
			} catch (ParseException | IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
				return;
			}
			assertEquals("<ok/>", o_body);
		}

		assertEquals(Konnector.CommandResult.Success, m_clientKonnector.stop());
		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.stop());

		// fail("Not yet implemented");
	}

}
