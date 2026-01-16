package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventType;
import communication.ServerEvent;
import communication.TableSizeUpdate;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code CHANGE_TABLE_SIZE} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link TableSizeUpdate}. Updates the size of an existing table in the
 * database.
 * </p>
 */
public class ChangeTableSizeHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to update a table size.
	 *
	 * @param request the request containing {@link TableSizeUpdate} payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} indicating success or failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof TableSizeUpdate) {
			TableSizeUpdate update = (TableSizeUpdate) data;

			Object dbReturnedValue = db.changeTableSize(update.getTable_number(), update.getTable_size());

			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			server.sendToAllClients(new ServerEvent(EventType.TABLE_CHANGED));
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
		}

		return response;
	}
}
