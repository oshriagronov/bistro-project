package handlers;

import java.time.LocalDate;
import java.time.LocalTime;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code GET_OPENING_HOURS} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link LocalDate}. Returns the opening and closing times for that date,
 * taking into account special days and regular weekly schedules.
 * </p>
 */
public class GetOpeningHoursHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to retrieve opening hours for a
	 * specific date.
	 *
	 * @param request the request containing a {@link LocalDate} payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} containing a {@link LocalTime} array
	 *         (opening, closing) on success
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof LocalDate) {
			LocalTime[] times = db.getOpeningHours((LocalDate) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, times);
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		return response;
	}
}
