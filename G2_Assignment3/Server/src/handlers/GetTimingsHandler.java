package handlers;

import java.time.YearMonth;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve daily timing statistics for a given
 * {@link YearMonth}.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link YearMonth}.
 * </p>
 */
public class GetTimingsHandler implements RequestHandler {

	/**
	 * Retrieves daily timing statistics for the specified month.
	 *
	 * @param request the incoming request (payload must be {@link YearMonth})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the timing statistics
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof YearMonth)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		YearMonth ym = (YearMonth) data;
		Object stats = db.getDailySlotStats(ym.getYear(), ym.getMonthValue());

		return new BistroResponse(BistroResponseStatus.SUCCESS, stats);
	}
}
