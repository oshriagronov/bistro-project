package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventType;
import communication.ServerEvent;
import db.ConnectionToDB;
import logic.Reservation;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code ADD_RESERVATION} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link Reservation}. Inserts the reservation into the database and, on
 * success, broadcasts an {@link EventType#ORDER_CHANGED} server event.
 * </p>
 */
public class AddReservationHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to add a new {@link Reservation}.
	 *
	 * @param request the request containing the reservation payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance (used for broadcasting events)
	 * @return a {@link BistroResponse} with {@link BistroResponseStatus#SUCCESS} on
	 *         insert success, otherwise {@link BistroResponseStatus#FAILURE}
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		// Handle adding a new reservation to the database
		if (data instanceof Reservation) {
			String success = db.insertReservation((Reservation) data);
			if (success != null) {
				response = new BistroResponse(BistroResponseStatus.SUCCESS, success);
				server.sendToAllClients(new ServerEvent(EventType.ORDER_CHANGED));
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Reservation failed");
			}
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid reservation data");
		}

		return response;
	}
}
