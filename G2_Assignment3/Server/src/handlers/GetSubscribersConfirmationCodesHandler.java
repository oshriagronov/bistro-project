package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve confirmation codes of confirmed reservations
 * for a specific subscriber.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer} representing the subscriber ID.
 * </p>
 */
public class GetSubscribersConfirmationCodesHandler implements RequestHandler {

	/**
	 * Retrieves confirmation codes for confirmed reservations of a subscriber.
	 *
	 * @param request the incoming request (payload must be {@link Integer})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the confirmation codes, or failure if invalid
	 *         input
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof Integer)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
		}

		Object codes = db.getConfirmedReservationCodesBySubscriber((Integer) data);

		return new BistroResponse(codes != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE, codes);
	}
}
