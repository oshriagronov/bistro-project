package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to load the current diners in the restaurant.
 */
public class LoadDinersHandler implements RequestHandler {

	/**
	 * Loads the list of current diners from the database.
	 *
	 * @param request the incoming request (no payload expected)
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the current diners
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object diners = db.loadCurrentDiners();

		return new BistroResponse(BistroResponseStatus.SUCCESS, diners);
	}
}
