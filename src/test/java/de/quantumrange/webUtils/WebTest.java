package de.quantumrange.webUtils;

import de.quantumrange.actionlib.Action;
import de.quantumrange.webUtils.connections.HTTPRequestType;
import de.quantumrange.webUtils.connections.impl.DataWebConnection;
import de.quantumrange.webUtils.connections.impl.GetWebConnection;
import de.quantumrange.webUtils.models.WebResult;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class WebTest {

	@Test
	void url() throws MalformedURLException {
		HashMap<String, String> params = new HashMap<>();
		params.put("query", "GET request test site");

		WebResult<String> result = Web.url(new URL("https://www.startpage.com/do/dsearch"))
				.request(params)
				.completion();

		assertNotNull(result.receiveResponse());
		assertNotEquals(-1, result.responseCode());
	}

	@Test
	void testUrl() throws MalformedURLException {
		WebResult<String> result = Web.url(new URL("https://www.urlencoder.org/"), HTTPRequestType.POST)
				.request("""
						{
							"input": "LOL"
						}
						""")
				.completion();

		assertNotNull(result.receiveResponse());
		assertNotEquals(-1, result.responseCode());
	}
}