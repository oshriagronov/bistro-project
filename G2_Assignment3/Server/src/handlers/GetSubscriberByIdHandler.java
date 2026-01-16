package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve a subscriber by their ID.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer}.
 * </p>
 */
public class GetSubscriberByIdHandler implements RequestHandler {

	/**
	 * Retrieves a subscriber by ID.
	 *
	 * @param request the incoming request (payload must be {@link Integer})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the subscriber if found, otherwise failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof Integer)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
		}

		Object subscriber = db.SearchSubscriberById((Integer) data);

		return new BistroResponse(subscriber != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				subscriber);
	}
}
