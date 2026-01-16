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
 * Handles {@code DELETE_TABLE} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer} representing the table number to delete. Removes the table
 * from the database and broadcasts a {@link EventType#TABLE_CHANGED} event on
 * success.
 * </p>
 */
public class DeleteTableHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to delete an existing table.
	 *
	 * @param request the request containing the table number
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
			db.deleteTable((Integer) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
			server.sendToAllClients(new ServerEvent(EventType.TABLE_CHANGED));
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		return response;
	}
}
