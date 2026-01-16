package handlers;

import java.time.YearMonth;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve subscriber order counts for a specific month.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link YearMonth} representing the year and month for which the data is
 * requested.
 * </p>
 */
public class SubscriberOrderCountsHandler implements RequestHandler {

	/**
	 * Retrieves the number of orders made by subscribers during the specified
	 * month.
	 *
	 * @param request the incoming request (payload must be {@link YearMonth})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the subscriber order counts, or failure if
	 *         input is invalid
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof YearMonth ym)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Expected YearMonth");
		}

		Object dbReturnedValue = db.getSubscriberOrderCounts(ym.getYear(), ym.getMonthValue());

		return new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
	}
}
