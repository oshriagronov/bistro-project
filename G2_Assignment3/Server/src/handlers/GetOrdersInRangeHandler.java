package handlers;

import java.time.LocalDate;
import java.time.LocalTime;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.OrdersInRangeRequest;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code GET_ORDERS_IN_RANGE} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link OrdersInRangeRequest} containing a date and a time. Returns the list
 * of diners counts for all reservations that intersect a two-hour window around
 * the given time.
 * </p>
 */
public class GetOrdersInRangeHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to retrieve orders within a
	 * two-hour time window.
	 *
	 * @param request the request containing {@link OrdersInRangeRequest} payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} containing the diners counts list on success
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof OrdersInRangeRequest) {
			OrdersInRangeRequest req = (OrdersInRangeRequest) data;

			LocalDate date = req.getDate();
			LocalTime time = req.getTime();

			Object dbReturnedValue = db.getNumDinersInTwoHoursWindow(date, time);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		return response;
	}
}
