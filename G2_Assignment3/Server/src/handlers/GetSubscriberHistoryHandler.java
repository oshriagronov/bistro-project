package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve a subscriber's order history.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer} representing the subscriber ID.
 * </p>
 */
public class GetSubscriberHistoryHandler implements RequestHandler {

	/**
	 * Retrieves the order history for a given subscriber.
	 *
	 * @param request the incoming request (payload must be {@link Integer})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the subscriber history if found
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof Integer)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		int subId = (Integer) data;
		Object history = db.getSubscriberHistory(subId);

		return new BistroResponse(history != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				history);
	}
}
