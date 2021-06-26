package de.quantumrange.webUtils.models;

import de.quantumrange.webUtils.connections.HTTPRequestType;
import de.quantumrange.webUtils.connections.WebConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;

/**
 * Saves the result of a {@link WebConnection}.
 * The send and receive time of the request are stored as {@link LocalDateTime}.
 * Information from the WebConnection like url (as {@link URL}) and the RequestType (as {@link HTTPRequestType}) are also
 * stored.
 * The response code and the body (mapped as the specified type T) of the server is also stored.
 *
 * If the request is failed the responseCode is set to -1 and the receiveResponse Time is set to null.
 *
 * @author QuantumRange
 * @since 1.0.1
 * @param <T> the required result data type.
 */
public record WebResult<T>(@Nonnull LocalDateTime requestSend, @Nullable LocalDateTime receiveResponse,
						   @Nonnull URL url, @Nonnull HTTPRequestType type,
						   int responseCode, @Nullable T response) {

	/**
	 * Maps from data type T to O.
	 *
	 * @param map The function that maps the type from T to O.
	 * @param <O> the requested data type.
	 * @return the new WebResult with the new data type as result.
	 */
	public <O> WebResult<O> map(Function<T, O> map) {
		return new WebResult<>(requestSend, receiveResponse, url, type, responseCode, map.apply(response));
	}

	/**
	 * Calculates the time it took to receive the response from server.
	 * @return how long it took in milliseconds from program to server to program. If there was no response from
	 * server, -1 is returned.
	 */
	public long getRequestDuration() {
		if (receiveResponse == null) return -1L;
		return requestSend.until(receiveResponse, ChronoUnit.MILLIS);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WebResult<?> webResult = (WebResult<?>) o;
		return responseCode == webResult.responseCode && Objects.equals(requestSend, webResult.requestSend) && Objects.equals(receiveResponse, webResult.receiveResponse) && Objects.equals(url, webResult.url) && type == webResult.type && Objects.equals(response, webResult.response);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestSend, receiveResponse, url, type, responseCode, response);
	}

	@Override
	public String toString() {
		return "WebResult{" +
				"requestSend=" + requestSend +
				", receiveResponse=" + receiveResponse +
				", url=" + url +
				", type=" + type +
				", responseCode=" + responseCode +
				", response=" + response +
				'}';
	}

}
