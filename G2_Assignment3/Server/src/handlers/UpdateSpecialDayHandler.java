package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import logic.SpecialDay;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to update opening and closing times for a special day.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link SpecialDay} containing the date and opening/closing times.
 * </p>
 */
public class UpdateSpecialDayHandler implements RequestHandler {

	/**
	 * Updates the schedule of a specific special day.
	 *
	 * @param request the incoming request (payload must be {@link SpecialDay})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response indicating whether the update was successful
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (data instanceof SpecialDay) {
			SpecialDay sd = (SpecialDay) data;

			Object result = db.updateSpecialDay(sd.getDay(), sd.getOpen(), sd.getClose());

			return new BistroResponse(BistroResponseStatus.SUCCESS, result);
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, "Failed to update the chosen date");
	}
}
