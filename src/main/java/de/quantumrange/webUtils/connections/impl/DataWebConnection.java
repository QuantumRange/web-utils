package de.quantumrange.webUtils.connections.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.quantumrange.actionlib.Action;
import de.quantumrange.actionlib.impl.actions.RateLimitedAction;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Is responsible for handling {@link HTTPRequestType#HEAD}, {@link HTTPRequestType#POST}, {@link HTTPRequestType#PUT},
 * {@link HTTPRequestType#DELETE}, {@link HTTPRequestType#OPTIONS}, {@link HTTPRequestType#TRACE} and
 * {@link HTTPRequestType#PATCH} requests.
 *
 * @author QuantumRange
 * @since 1.0.1
 */
public class DataWebConnection extends WebConnection<String, DataWebConnection> {

	private final HTTPRequestType type;

	/**
	 * @param url must be valid and is the URL to connect to at {@link WebConnection#request(Object)} (more on
	 * {@link WebConnection#WebConnection(URL, int)})
	 */
	public DataWebConnection(URL url, int id, HTTPRequestType type) {
		super(url, id);
		this.type = type;
	}

	/**
	 * Sends data to the server using JSON formatting.
	 *
	 * @param data The sent data optionally as JSON.
	 * @return The result of the server.
	 */
	@Override
	public Action<WebResult<String>> request(String data) {
		AtomicReference<LocalDateTime> sendTime = new AtomicReference<>(null);

		return new RateLimitedAction<>(Web.MANAGER, getRateID(), throwable -> {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(getURI())
					.method(type.name(), HttpRequest.BodyPublishers.ofString(data))
					.build();

			try {
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				return new WebResult<>(sendTime.get(), LocalDateTime.now(), getUrl(), type, response.statusCode(),
						response.body());
			} catch (IOException | InterruptedException e) {
				throwable.accept(e);
				return new WebResult<>(sendTime.get(), null, getUrl(), type, -1, (String) null);
			}
		}).setCheck(() -> {
			sendTime.set(LocalDateTime.now());
			return true;
		});
	}

	/**
	 * Maps the object via {@link ObjectWriter} and puts the result into {@link #request(String)}.
	 *
	 * @param data The object what will be sent.
	 * @param writer The writer to turn the object into JSON.
	 * @param <T> The Object typ.
	 * @throws JsonProcessingException If the ObjectWriter is set incorrectly or the object is null.
	 * @return the result of the method: {@link #request(String)} (Object)}.
	 */
	public <T> Action<WebResult<String>> requestWithObject(T data, ObjectWriter writer) throws JsonProcessingException {
		return requestWithObject(writer.writeValueAsString(data));
	}

	/**
	 * Automatically creates an object mapper using the protected method in WebConnection (createObjectMapper).
	 * Returns the result of {@link #requestWithObject(Object, ObjectWriter)} (Object, ObjectWriter)}.
	 *
	 * @param data The object what  will be sent.
	 * @param <T> the Object typ.
	 * @throws JsonProcessingException If the ObjectWriter is set incorrectly or the object is null.
	 * @return the result of {@link #requestWithObject(Object, ObjectWriter)} (Object, ObjectWriter)}.
	 */
	public <T> Action<WebResult<String>> requestWithObject(T data) throws JsonProcessingException {
		return requestWithObject(data, createObjectMapper().writer());
	}

}
