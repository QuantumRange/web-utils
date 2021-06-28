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

	@Test
	void header() throws IOException {
//		ServerSocket ss = new ServerSocket(8080);
//
//		while (true) {
//			Socket s = ss.accept();
//			BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//			String line;
//			while ((line = sin.readLine()) != null) {
//				System.out.println(line);
//			}
//		}

		String head = """
				GET http://www.google.com/ HTTP/1.1
				Host: www.google.com
				User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0
				Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
				Accept-Language: de,en-US;q=0.7,en;q=0.3
				Accept-Encoding: gzip, deflate
				DNT: 1
				Connection: keep-alive
				Cookie: SID=_Acfvrk3pJuUuzTKqfUjpkcjXsX48PrOGBBGiQbXE9idIG9lj_irKu2KxdoeWGFEZyjg2Q.; HSID=A23HluyyUKnXa3SFX; APISID=4Fld3qU4QlQTk7Au/AiXJy5Qy_l5WFK9em; SIDCC=AJi4QfHsIdD8tx_2IBq7eTAntzBHZ1BIQLBUv4oyJzpbxWPk2onaZKkIAxdtBNSVCrTEJv4b-A
				Upgrade-Insecure-Requests: 1
				Sec-GPC: 1
				""";

		DataWebConnection webConnection = Web.parseOtherHeader(head);
		Action<WebResult<String>> request = webConnection.request("{\"jsonrpc\":\"2.0\",\"method\":\"getTermbaseAccessToken\",\"params\":{\"termbaseId\":\"8df4fc07-b59b-46fd-9af5-b0b176117b5e\"},\"id\":7920021}");
		WebResult<String> result = request.completion();

		System.out.println(result.response());
	}
}