package org.akigrafsoft.apachehttpkonnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class HttpServerConfigTest {

	@Test
	public void test() {

		ObjectMapper mapper = new ObjectMapper();

		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(new HttpServerConfig().url(
					"http://localhost:7777").trustStore("type", "path",
					"PASSWD"));
			System.out.println(jsonString);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		HttpServerConfig config;
		try {
			config = mapper.readValue(jsonString, HttpServerConfig.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		assertEquals("http://localhost:7777", config.getUrl());
		assertEquals("PASSWD", config.getTrustStore().getPassword());

	}

}
