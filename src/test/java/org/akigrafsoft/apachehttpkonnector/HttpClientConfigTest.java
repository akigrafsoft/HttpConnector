package org.akigrafsoft.apachehttpkonnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class HttpClientConfigTest {

	@Test
	public void test() {
		ObjectMapper mapper = new ObjectMapper();

		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(new HttpClientConfig().url(
					"http://localhost:7777/test").trustStore("type", "path",
					"PASSWD"));
			System.out.println(jsonString);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		HttpClientConfig config;

		try {
			config = mapper.readValue(jsonString, HttpClientConfig.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		assertEquals("http://localhost:7777/test", config.getUrl());
		assertEquals("PASSWD", config.getTrustStore().getPassword());

	}

}
