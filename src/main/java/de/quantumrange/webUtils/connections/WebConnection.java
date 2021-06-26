package de.quantumrange.webUtils.connections;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.quantumrange.actionlib.Action;
import de.quantumrange.actionlib.impl.actions.RateLimitedAction;
import de.quantumrange.actionlib.impl.manager.RateLimitedThreadManager;
import de.quantumrange.webUtils.Web;
import de.quantumrange.webUtils.connections.impl.DataWebConnection;
import de.quantumrange.webUtils.connections.impl.GetWebConnection;
import de.quantumrange.webUtils.models.WebResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Is the base class for the {@link DataWebConnection} and the {@link GetWebConnection}.
 * These implementations implement how the request is sent.
 * The {@link WebConnection} takes care of the rest by itself.
 *
 * @author QuantumRange
 * @since 1.0.1
 */
public abstract class WebConnection<T, J> {

	/**
	 * The standard for Json LocalDateTime.
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ISO_DATE_TIME)
			.appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			.appendOptional(DateTimeFormatter.ISO_INSTANT)
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SX"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
			.toFormatter()
			.withZone(ZoneOffset.UTC);

	/**
	 * The standard for Json LocalTime.
	 */
	private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ISO_TIME)
			.appendOptional(DateTimeFormatter.ISO_OFFSET_TIME)
			.parseDefaulting(ChronoField.YEAR, 2021)
			.parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
			.parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
			.toFormatter()
			.withZone(ZoneOffset.UTC);

	/**
	 * The URL to connect to.
	 */
	private final URL url;
	/**
	 * Header attributes are cached here until the request is sent.
	 */
	private final HashMap<String, String> headerProperties;
	private int rateID;

	/**
	 * @param url must be valid and is the URL to connect to at {@link #request(Object)} (or
	 * {@link #requestJson(Object)}, {@link #requestJson(Object, ObjectMapper)}, {@link #request(Object)},
	 * {@link #requestAs(Object, Function)}, {@link #requestJson(Object, ObjectReader)}).
	 * @param rateID is the RateID (see more: {@link RateLimitedThreadManager} and {@link RateLimitedAction})
	 */
	public WebConnection(URL url, int rateID) {
		this.url = url;
		this.rateID = rateID;
		this.headerProperties = new HashMap<>();
		Web.MANAGER.registerRateLimit(0, 0L);
	}

	/**
	 * Returns the current headers.
	 * @return the current headers.
	 */
	public HashMap<String, String> getHeaderProperties() {
		return headerProperties;
	}

	/**
	 * Adds a header.
	 * Headers are always key, value enters.
	 * <p>
	 * Example:
	 * <pre>addHeader("User-Agent", "[product] / [product-version] [comment]");</pre>
	 * or often used:
	 * <pre>addHeader("User-Agent", "Mozilla/[version] ([system-information]) [platform] ([platform-details]) [extensions]");</pre>
	 * But for User-Agent there is an own method ({@link #setUserAgent(String)},
	 *               {@link #setUserAgent(String, String, String)}), here it is used only as an example
	 * because it is the most known header.
	 *
	 * @param name the key for the value
	 * @param value the value for the key
	 */
	public void addHeader(String name, String value) {
		if (headerProperties.containsKey(name)) headerProperties.replace(name, value);
		else headerProperties.put(name, value);
	}

	/**
	 * Executes the {@link #addHeader(String, String)} method with the given string as value and "User-Agent" as name.
	 *
	 * More about the <a href="https://developer.mozilla.org/de/docs/Web/HTTP/Headers/User-Agent">User Agent</a>.
	 * @param agent the specified user agent.
	 */
	public void setUserAgent(String agent) {
		addHeader("User-Agent", agent);
	}

	/**
	 * Executes the {@link #setUserAgent(String)} method with the crawler user agent format.
	 *
	 * @param crawlerName The name of the crawler, for example: ImageBot
	 * @param version The version of the crawler, for example: 2.0
	 * @param website The website where you can learn more about the crawler.
	 */
	public void setUserAgent(String crawlerName, String version, String website) {
		addHeader("User-Agent", "%s/%s (+%s)".formatted(crawlerName, version, website));
	}

	/**
	 * Executes the request.
	 * Sends the request with all parameters to the server and waits for response.
	 * Once the response of the body arrives it notify the WebResult.
	 *
	 * @return The action that sends the request when {@link RateLimitedAction#queue()}, {@link RateLimitedAction#queue(Consumer)} ()},
	 * {@link RateLimitedAction#queue(long, TimeUnit)}, {@link RateLimitedAction#queue(Consumer, Consumer)} or
	 * {@link RateLimitedAction#completion()} is executed.
	 */
	public abstract Action<WebResult<String>> request(T data);

	/**
	 * Executes the request (more on {@link #request(Object)}) and maps the result to the requested result O using the function.
	 *
	 * @param data The data that should be sent
	 * @param map The function that maps the result.
	 * @param <O> The new data type.
	 * @return The action with a mapper that makes the result the requested type O.
	 */
	public <O> Action<WebResult<O>> requestAs(T data, Function<String, O> map) {
		return request(data).map(res -> res.map(map));
	}

	/**
	 * Sends the request and converts the data type to the requested object via JSON
	 * (more on {@link #requestAs(Object, Function)}).
	 *
	 * @param data The data that should be sent.
	 * @param reader The Jackson JSON ObjectReader.
	 * @param <O> The object to which the result is to be mapped via JSON.
	 * @return The result of the request mapped to the requested object.
	 */
	public <O> Action<WebResult<O>> requestJson(T data, ObjectReader reader) {
		return requestAs(data, input -> {
			try {
				return reader.readValue(input);
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException("The receiving data is not JSON or the ObjectReader/Mapper is configured incorrectly.");
			}
		});
	}

	/**
	 * Sends the request and converts the data type to the requested object via JSON
	 * (more on {@link #requestJson(Object, ObjectReader)}).
	 *
	 * @param data The data that should be sent.
	 * @param mapper The Jackson JSON ObjectMapper.
	 * @param <O> The object to which the result is to be mapped via JSON.
	 * @return  The result of the request mapped to the requested object.
	 */
	public <O> Action<WebResult<O>> requestJson(T data, ObjectMapper mapper) {
		return requestJson(data, mapper.reader());
	}

	/**
	 * Sends the request and converts the data type to the requested object via JSON
	 * (more on {@link #requestJson(Object, ObjectMapper)}).
	 *
	 * @param data The data that should be sent.
	 * @param <O> The object to which the result is to be mapped via JSON.
	 * @return  The result of the request mapped to the requested object.
	 */
	public <O> Action<WebResult<O>> requestJson(T data) {
		return requestJson(data, createObjectMapper().reader());
	}

	protected URI getURI() {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			return null;
		}
	}

	protected ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();

		mapper.findAndRegisterModules();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
			@Override
			public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
				return parseDateTimeString(jsonParser.getText());
			}
		});
		module.addDeserializer(LocalTime.class, new JsonDeserializer<>() {
			@Override
			public LocalTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
				return parseTimeString(jsonParser.getText());
			}
		});

		mapper.registerModule(module);

		return mapper;
	}

	/**
	 * Returns the request URL.
	 * @return the request URL.
	 */
	public URL getUrl() {
		return url;
	}

	private static LocalDateTime parseDateTimeString(String str) {
		return ZonedDateTime.from(DATE_TIME_FORMATTER.parse(str)).toLocalDateTime();
	}

	private static LocalTime parseTimeString(String str) {
		return ZonedDateTime.from(TIME_FORMATTER.parse(str)).toOffsetDateTime().toLocalTime();
	}

	/**
	 * Sets the RateID (more on: {@link RateLimitedThreadManager})
	 *
	 * @param rateID the rateID. More at: {@link RateLimitedThreadManager#registerRateLimit(int, long)}.
	 * @return itself
	 */
	public J rateID(int rateID) {
		this.rateID = rateID;
		return (J) this;
	}

	/**
	 * Returns the RateID
	 * @return the rateID
	 */
	public int getRateID() {
		return rateID;
	}
}
