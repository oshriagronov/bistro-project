package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

import static handlers.RequestParsingHelper.handleStringRequest;

/**
 * Handles a request to retrieve a reservation by its order number.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to represent an
 * order number that can be parsed by
 * {@link handlers.RequestParsingHelper#handleStringRequest(Object)}.
 * </p>
 */
public class ReservationByOrderNumberHandler implements RequestHandler {

	/**
	 * Retrieves a reservation by order number.
	 *
	 * @param request the incoming request (payload should represent an order
	 *                number)
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the reservation matching the order number
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		int orderNumber = handleStringRequest(request.getData());

		if (orderNumber == -1) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Bad order number.");
		}
		Object reservation = db.searchOrderByOrderNumber(orderNumber);
		return new BistroResponse(BistroResponseStatus.SUCCESS, reservation);
	}
}
