package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import logic.Reservation;
import ocsf.server.ConnectionToClient;
import server.Server;

import java.util.List;

/**
 * Handles a request to retrieve all reservations for the current day.
 * <p>
 * This handler delegates the data retrieval to
 * {@link ConnectionToDB#getTodayReservations()} and returns the result to the
 * client wrapped in a {@link BistroResponse}.
 * </p>
 */
public class GetTodaysReservationsHandler implements RequestHandler {

	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		List<Reservation> reservations = db.getTodayReservations();

		return new BistroResponse(BistroResponseStatus.SUCCESS, reservations);
	}
}
