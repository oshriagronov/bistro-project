package handlers;

import java.util.ArrayList;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a subscriber login request.
 *
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link ArrayList} containing:
 * </p>
 * <ul>
 * <li>index 0: username (String)</li>
 * <li>index 1: password (String)</li>
 * </ul>
 *
 * <p>
 * Returns {@link BistroResponseStatus#SUCCESS} with the subscriber id (Integer)
 * when authentication succeeds; otherwise returns
 * {@link BistroResponseStatus#FAILURE}.
 * </p>
 */
public class SubscriberLoginHandler implements RequestHandler {

	/**
	 * Performs subscriber login authentication.
	 *
	 * @param request the incoming request (payload must be {@link ArrayList})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response indicating whether login was successful
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof ArrayList<?> params) || params.size() < 2) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid login payload");
		}

		Object u = params.get(0);
		Object p = params.get(1);

		if (!(u instanceof String) || !(p instanceof String)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid login payload");
		}

		String username = ((String) u).trim();
		String password = (String) p;

		if (!hasText(username) || !hasText(password)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Missing credentials");
		}

		int subscriberId = db.subscriberLogin(username, password);

		if (subscriberId > 0) {
			return new BistroResponse(BistroResponseStatus.SUCCESS, subscriberId);
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, null);
	}

	/**
	 * Returns whether the given value contains non-blank text that is not "null".
	 *
	 * @param value candidate string to check
	 * @return true when value is non-null, trimmed non-empty, and not "null"
	 */
	private static boolean hasText(String value) {
		if (value == null)
			return false;
		String trimmed = value.trim();
		return !trimmed.isEmpty() && !"null".equalsIgnoreCase(trimmed);
	}
}
