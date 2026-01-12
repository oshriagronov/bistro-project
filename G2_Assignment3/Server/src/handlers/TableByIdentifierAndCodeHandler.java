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
 * Handles a request to assign a table to a reservation using an identifier
 * (email or phone) and a confirmation code.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link ArrayList} with the following structure:
 * <ul>
 * <li>index 0 – {@link String} identifier (email or phone)</li>
 * <li>index 1 – {@link String} confirmation code</li>
 * </ul>
 * </p>
 */
public class TableByIdentifierAndCodeHandler implements RequestHandler {

	/**
	 * Attempts to find a confirmed reservation by identifier and code, allocate an
	 * available table, and update the reservation accordingly.
	 *
	 * @param request the incoming request (payload must be {@link ArrayList})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the allocated table number, or an error status
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof ArrayList<?> params)) {
			return new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
		}

		String identifier = (String) params.get(0);
		int code = Integer.parseInt((String) params.get(1));

		Reservation res = identifier.contains("@")
				? db.getOrderByEmailAndCode(identifier, code, Status.CONFIRMED.name())
				: db.getOrderByPhoneAndCode(identifier, code, Status.CONFIRMED.name());

		if (res == null) {
			return new BistroResponse(BistroResponseStatus.NOT_FOUND, null);
		}

		int tableNum = db.searchAvailableTableBySize(res.getNumberOfGuests());

		if (tableNum <= 0) {
			return new BistroResponse(BistroResponseStatus.NO_AVAILABLE_TABLE, null);
		}

		db.updateTableResId(tableNum, res.getOrderNumber());
		db.updateReservationTimesAfterAcceptation(res.getOrderNumber());

		return new BistroResponse(BistroResponseStatus.SUCCESS, tableNum);
	}
}
