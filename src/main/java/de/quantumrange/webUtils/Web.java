package de.quantumrange.webUtils;

import de.quantumrange.actionlib.Action;
import de.quantumrange.actionlib.ActionManager;
import de.quantumrange.actionlib.impl.actions.RateLimitedAction;
import de.quantumrange.actionlib.impl.manager.RateLimitedThreadManager;
import de.quantumrange.webUtils.connections.HTTPRequestType;
import de.quantumrange.webUtils.connections.WebConnection;
import de.quantumrange.webUtils.connections.impl.DataWebConnection;
import de.quantumrange.webUtils.connections.impl.GetWebConnection;
import de.quantumrange.webUtils.models.WebResult;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link Web} is for easier creation of a WebConnection.
 * You can think of it as a WebConnectionBuilder, but shorter to write and more intuitive.
 * Important <code>static</code> components are also stored here.
 * For example the {@link ActionManager}, if interested look here:
 * <a href="https://github.com/QuantumRange/actionlib">Action Lib (On GitHub)</a>
 *
 * Note:
 * These methods are also there in case anything is extended so that the methods that are accessible from outside remain.
 *
 * @author QuantumRange
 * @since 1.0.0
 */
public class Web {

	/**
	 * This is the RateLimited ThreadManager.
	 * It takes 40% of the available threads.
	 * If this is too little/much you can simply overwrite the variable.
	 */
	public static RateLimitedThreadManager MANAGER = new RateLimitedThreadManager(.4f);

	public static GetWebConnection url(URL url) {
		return new GetWebConnection(url, 0);
	}

	public static DataWebConnection url(URL url, HTTPRequestType type) {
		if (type == HTTPRequestType.GET) throw new IllegalArgumentException("type cannot be HTTPRequestType.GET. " +
				"Please use url(URL url) then.");
		return new DataWebConnection(url, 0, type);
	}

	public static GetWebConnection parseGETHeader(String data) {
		return null;
	}

}
