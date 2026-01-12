package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.StatusUpdate;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code CANCEL_RESERVATION} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link StatusUpdate} containing the reservation identifier and either a phone
 * number or an email address. Updates the reservation status to cancelled.
 * </p>
 */
public class CancelReservationHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to cancel an existing
	 * reservation.
	 *
	 * @param request the request containing {@link StatusUpdate} payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} indicating success or failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof StatusUpdate) {
			StatusUpdate statusUpdate = (StatusUpdate) data;

			if (statusUpdate.getEmail() == null && statusUpdate.getPhoneNumber() == null) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "invalid information.");
			} else {
				int result = db.CancelReservation(statusUpdate.getOrderNumber(), statusUpdate.getEmail(),
						statusUpdate.getPhoneNumber());

				if (result > 0) {
					response = new BistroResponse(BistroResponseStatus.SUCCESS, "Cancel succeeded.");
				} else {
					response = new BistroResponse(BistroResponseStatus.FAILURE, "Cancel failed.");
				}
			}
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, "Cancel failed.");
		}

		return response;
	}
}
