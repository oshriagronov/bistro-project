package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import logic.Reservation;
import logic.Status;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code GET_BILL} requests.
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * confirmation code {@link String}. If a matching reservation in
 * {@link Status#ACCEPTED} is found, it is marked {@link Status#COMPLETED},
 * finish time is updated, and the assigned table is cleared.
 */
public class GetBillHandler implements RequestHandler {

	/**
	 * Processes a {@link BistroRequest} containing a confirmation code string,
	 * retrieves the accepted reservation, marks it completed, updates finish
	 * time, and clears the assigned table.
	 *
	 * @param request the request containing the confirmation code string
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} with the matching {@link Reservation} on
	 *         success, {@link BistroResponseStatus#NOT_FOUND} when no accepted
	 *         reservation matches, or {@link BistroResponseStatus#INVALID_REQUEST}
	 *         when the payload is not a {@link String}
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();
		BistroResponse response;
		if (data instanceof String) {
			int code = Integer.parseInt((String) data);
			Reservation res = db.getAcceptedReservationByConfirmationCode(code);
			if (res != null) {
				db.changeOrderStatus(res.getPhone_number(), res.getOrderNumber(), Status.COMPLETED);
				db.updateReservationTimesAfterCompleting(res.getOrderNumber());
				db.clearTableByResId(res.getOrderNumber());
				response = new BistroResponse(BistroResponseStatus.SUCCESS, res);
			} else {
				response = new BistroResponse(BistroResponseStatus.NOT_FOUND, null);
			}
		} else {
			response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
		}
		return response;
	}
}
