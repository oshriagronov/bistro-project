package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventType;
import communication.ServerEvent;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code ADD_TABLE} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer} representing the table size. Inserts a new table into the
 * database and broadcasts a {@link EventType#TABLE_CHANGED} event on success.
 * </p>
 */
public class AddTableHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to add a new table.
	 *
	 * @param request the request containing the table size
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance (used for broadcasting events)
	 * @return a {@link BistroResponse} indicating success or failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof Integer) {
			db.addTable((Integer) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
			server.sendToAllClients(new ServerEvent(EventType.TABLE_CHANGED));
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		return response;
	}
}
