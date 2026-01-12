package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to load upcoming special dates.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer} representing the number of days ahead to load.
 * </p>
 */
public class LoadSpecialDatesHandler implements RequestHandler {

	/**
	 * Loads upcoming special dates from the database.
	 *
	 * @param request the incoming request (payload must be {@link Integer})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the special dates, or failure if input is
	 *         invalid
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (data instanceof Integer) {
			Object specialDates = db.loadUpcomingSpecialDates((Integer) data);

			return new BistroResponse(BistroResponseStatus.SUCCESS, specialDates);
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, "Failed loading the special dates");
	}
}
