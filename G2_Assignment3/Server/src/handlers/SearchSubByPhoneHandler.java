package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve a subscriber by phone number.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link String} representing a phone number.
 * </p>
 */
public class SearchSubByPhoneHandler implements RequestHandler {

	/**
	 * Retrieves a subscriber by phone number.
	 *
	 * @param request the incoming request (payload must be {@link String})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the subscriber if found, otherwise failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (data instanceof String) {
			Object subscriber = db.SearchSubscriberByPhone((String) data);

			return new BistroResponse(subscriber != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					subscriber);
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid phone format");
	}
}
