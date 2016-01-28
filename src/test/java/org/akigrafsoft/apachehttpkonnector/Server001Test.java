package org.akigrafsoft.apachehttpkonnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.akigrafsoft.knetthreads.Dispatcher;
import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.FlowProcessContext;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.RequestEnum;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

@Ignore
public class Server001Test {
	static String SERVER_NAME = "Server01";
	// static String CLIENT_NAME = "Client01";

	// private final static XStream xstream = new XStream();

	// static ApacheHttpClientKonnector m_clientKonnector;
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
			m_serverKonnector = new ApacheHttpServerKonnector(SERVER_NAME);
		} catch (ExceptionDuplicate e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		m_serverKonnector.destroy();
	}

	@Test
	public void test() {

		try {
			m_serverKonnector.configure(new HttpServerConfig()
			// .url("http://localhost:" + port));
					.url("http://10.100.115.25:" + port));
		} catch (ExceptionAuditFailed e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		System.out.println(port);

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

					// String xml = "<ok/>";
					// System.out.println(this.getClass().getName() +
					// "|encoded: "
					// + xml);
					String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
							+ "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"><SOAP-ENV:Header/><SOAP-ENV:Body><PlexViewRequest Command=\"ent-ngfs-subscriber-v2\" SwitchName=\"ACH4CTS01\" Fsdb=\"autoselect\" RequestId=\"\" SessionId=\"Aude2:81776647\" MaxRows=\"-1\"><SubParty><Category>RESIDENTIALSUBSCRIBER_R2</Category><PartyId>0256431111[AutoSelect]</PartyId>"
							+ "<PrimaryPUID>+33256431111</       PrimaryPUID>"
							+ "<testSFR>aaaaaaaaaaaaaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeerrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyynnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn</testSFR>"
							+ "</SubParty></SOAP-ENV:Body></SOAP-ENV:Envelope>";

					StringEntity body = new StringEntity(xml,
							ContentType.create("text/xml", "UTF-8"));
					l_dataobject.httpResponse.setEntity(body);
					System.out.println(l_dataobject.httpResponse);

					// System.out.println(new Date() +
					// "|WAIT before Handle...");
					// try {
					// Thread.sleep(1000);
					// } catch (InterruptedException e) {
					// }

					m_serverKonnector.handle(dataobject);

					// leave null as this is fake anyway
					return null;
				}
			};
			nap.setDispatcher(new Dispatcher() {
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

		// try {
		// m_clientKonnector.configure(new HttpClientConfig()
		// .url("http://localhost:" + port));
		// } catch (ExceptionAuditFailed e) {
		// e.printStackTrace();
		// fail(e.getMessage());
		// return;
		// }

		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.start());
		// assertEquals(Konnector.CommandResult.Success,
		// m_clientKonnector.start());

		Utils.sleep(1000);

		// Message message = new Message();
		//
		// System.out.println("TEST ASYNC MODE");
		// {
		// ApacheHttpDataobject dataobject = new ApacheHttpDataobject(message,
		// false);
		// dataobject.operationMode = KonnectorDataobject.OperationMode.TWOWAY;
		// dataobject.httpRequest = new HttpGet(
		// "/?msisdn='0689'&transactionId='t001'");
		// message.associateDataobject("test", dataobject);
		//
		// dataobject.operationSyncMode = KonnectorDataobject.SyncMode.ASYNC;
		// m_clientKonnector.handle(dataobject);
		// System.out.println(new Date() + "|WAIT...");
		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e) {
		// }
		// String o_body;
		// try {
		// o_body = EntityUtils.toString(dataobject.httpResponse
		// .getEntity());
		// } catch (ParseException | IOException e) {
		// e.printStackTrace();
		// fail(e.getMessage());
		// return;
		// }
		// assertEquals("<ok/>", o_body);
		// }
		//
		// assertEquals(Konnector.CommandResult.Success,
		// m_clientKonnector.stop());
		assertEquals(Konnector.CommandResult.Success, m_serverKonnector.stop());

		// fail("Not yet implemented");
	}
}
