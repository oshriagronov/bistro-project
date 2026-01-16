package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventType;
import communication.ServerEvent;
import communication.StatusUpdate;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code CHANGE_STATUS} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link StatusUpdate}. Updates the reservation status in the database and, on
 * success, broadcasts an {@link EventType#ORDER_CHANGED} server event.
 * </p>
 */
public class ChangeStatusHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to update the status of an
	 * existing reservation.
	 *
	 * @param request the request containing {@link StatusUpdate} payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance (used for broadcasting events)
	 * @return a {@link BistroResponse} indicating success or failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof StatusUpdate) {
			StatusUpdate statusUpdate = (StatusUpdate) data;

			Object dbReturnedValue = db.changeOrderStatus(statusUpdate.getPhoneNumber(), statusUpdate.getOrderNumber(),
					statusUpdate.getStatus());

			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			server.sendToAllClients(new ServerEvent(EventType.ORDER_CHANGED));
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
		}

		return response;
	}
}
