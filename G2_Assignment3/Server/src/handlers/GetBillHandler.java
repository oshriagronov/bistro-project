package handlers;

import java.util.ArrayList;

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
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link ArrayList} containing:
 * <ol>
 * <li>identifier (phone or email)</li>
 * <li>confirmation code (string)</li>
 * </ol>
 * If a matching reservation in {@link Status#ACCEPTED} is found, it is marked
 * {@link Status#COMPLETED}, finish time is updated, and the assigned table is
 * cleared.
 * </p>
 */
public class GetBillHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to retrieve a bill for an
	 * accepted reservation and mark it as completed.
	 *
	 * @param request the request containing identifier and confirmation code
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} with the matching {@link Reservation} on
	 *         success
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof ArrayList) {
			ArrayList<?> params = (ArrayList<?>) data;

			String identifier = (String) params.get(0);
			int code = Integer.parseInt((String) params.get(1));

			Reservation res = identifier.contains("@")
					? db.getOrderByEmailAndCode(identifier, code, Status.ACCEPTED.name())
					: db.getOrderByPhoneAndCode(identifier, code, Status.ACCEPTED.name());

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
