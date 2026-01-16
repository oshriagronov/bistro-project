package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import logic.WeeklySchedule;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to update the regular (weekly) opening and closing times
 * for a specific day of the week.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link WeeklySchedule} containing the day of week and opening/closing times.
 * </p>
 */
public class UpdateRegularScheduleHandler implements RequestHandler {

	/**
	 * Updates the regular schedule for a given day of the week.
	 *
	 * @param request the incoming request (payload must be {@link WeeklySchedule})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response indicating whether the update was successful
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (data instanceof WeeklySchedule) {
			WeeklySchedule ws = (WeeklySchedule) data;

			Object result = db.updateRegularDayTimes(ws.getDayOfWeek().toString(), ws.getOpen(), ws.getClose());

			return new BistroResponse(BistroResponseStatus.SUCCESS, result);
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, "Failed to update schedule");
	}
}
