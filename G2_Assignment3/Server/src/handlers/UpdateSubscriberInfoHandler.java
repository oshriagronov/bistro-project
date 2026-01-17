package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import logic.Subscriber;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to update subscriber personal information.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link Subscriber} containing updated subscriber details.
 * </p>
 */
public class UpdateSubscriberInfoHandler implements RequestHandler {

	/**
	 * Updates subscriber information in the database.
	 *
	 * @param request the incoming request (payload must be {@link Subscriber})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response indicating whether the update was successful
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof Subscriber)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		Subscriber subscriber = (Subscriber) data;

		if (subscriber.getSubscriberId() == null) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Missing subscriber id.");
		}

		String username = subscriber.getUsername();
		if (username != null && !username.trim().isEmpty()
				&& db.isSubscriberUsernameTaken(username, subscriber.getSubscriberId())) {
			return new BistroResponse(BistroResponseStatus.ALREADY_EXISTS, "Username already exists.");
		}

		int updatedRows = db.updateSubscriberInfo(subscriber);

		return new BistroResponse(updatedRows >= 1 ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				updatedRows >= 1 ? null : "Failed to update subscriber info.");
	}
}
