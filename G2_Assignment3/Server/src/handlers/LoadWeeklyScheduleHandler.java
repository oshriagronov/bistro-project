package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to load the weekly (regular) schedule.
 */
public class LoadWeeklyScheduleHandler implements RequestHandler {

	/**
	 * Loads the regular weekly schedule from the database.
	 *
	 * @param request the incoming request (no payload expected)
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the weekly schedule
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object schedule = db.loadRegularTimes();

		return new BistroResponse(BistroResponseStatus.SUCCESS, schedule);
	}
}
