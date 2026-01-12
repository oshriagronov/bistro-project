package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve reservations by phone number.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link String} representing a phone number.
 * </p>
 */
public class ReservationsByPhoneHandler implements RequestHandler {

	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof String)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Bad phone number.");
		}

		Object reservations = db.searchOrdersByPhoneNumberList((String) data);

		return new BistroResponse(BistroResponseStatus.SUCCESS, reservations);
	}
}
