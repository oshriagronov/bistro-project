package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve all tables from the system.
 */
public class GetTablesHandler implements RequestHandler {

	/**
	 * Loads all tables from the database.
	 *
	 * @param request the incoming request (no payload expected)
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the list of tables
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object tables = db.loadTables();

		return new BistroResponse(BistroResponseStatus.SUCCESS, tables);
	}
}
