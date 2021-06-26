package de.quantumrange.webUtils.connections.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import de.quantumrange.actionlib.Action;
import de.quantumrange.actionlib.impl.actions.RateLimitedAction;
import de.quantumrange.actionlib.impl.manager.RateLimitedThreadManager;
import de.quantumrange.webUtils.Web;
import de.quantumrange.webUtils.connections.HTTPRequestType;
import de.quantumrange.webUtils.connections.WebConnection;
import de.quantumrange.webUtils.models.WebResult;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Is responsible for handling {@link HTTPRequestType#GET} request.
 *
 * @author QuantumRange
 * @since 1.0.1
 */
public class GetWebConnection extends WebConnection<HashMap<String, String>, GetWebConnection> {

	/**
	 * @param url    must be valid and is the URL to connect to at {@link WebConnection#request(Object)} (or
	 *               {@link WebConnection#requestJson(Object)}, {@link WebConnection#requestJson(Object, ObjectMapper)},
	 *               {@link WebConnection#request(Object)},
	 *               {@link WebConnection#requestAs(Object, Function)},
	 *               {@link WebConnection#requestJson(Object, ObjectReader)}).
	 * @param rateID is the RateID (see more: {@link RateLimitedThreadManager} and {@link RateLimitedAction})
	 */
	public GetWebConnection(URL url, int rateID) {
		super(url, rateID);
	}

	@Override
	public Action<WebResult<String>> request(HashMap<String, String> data) {
		AtomicReference<LocalDateTime> sendTime = new AtomicReference<>(null);

		return new RateLimitedAction<>(Web.MANAGER, getRateID(), throwable -> {
			StringBuilder urlBuilder = new StringBuilder();

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(urlBuilder.toString()))
					.GET()
					.build();

			try {
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				return new WebResult<>(sendTime.get(), LocalDateTime.now(), getUrl(), HTTPRequestType.GET,
						response.statusCode(), response.body());
			} catch (IOException | InterruptedException e) {
				throwable.accept(e);
				return new WebResult<>(sendTime.get(), null, getUrl(), HTTPRequestType.GET, -1, (String) null);
			}
		}).setCheck(() -> {
			sendTime.set(LocalDateTime.now());
			return true;
		});
	}
}
