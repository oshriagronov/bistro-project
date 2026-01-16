package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve reservations by subscriber email.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link String} representing an email address.
 * </p>
 */
public class ReservationsByEmailHandler implements RequestHandler {

	/**
	 * Retrieves reservations associated with the given email address.
	 *
	 * @param request the incoming request (payload must be {@link String})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the reservations, or failure if input is
	 *         invalid
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof String)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid email address");
		}

		Object reservations = db.searchOrdersByEmail((String) data);

		return new BistroResponse(BistroResponseStatus.SUCCESS, reservations);
	}
}
