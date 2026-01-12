package handlers;

import java.time.YearMonth;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve the number of orders made by subscribers for a
 * specific {@link YearMonth}.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link YearMonth}.
 * </p>
 */
public class GetSubscribersOrdersCountsHandler implements RequestHandler {

	/**
	 * Retrieves subscribers' order counts for the given month.
	 *
	 * @param request the incoming request (payload must be {@link YearMonth})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the order counts, or failure if input is
	 *         invalid
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (!(data instanceof YearMonth)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		YearMonth ym = (YearMonth) data;
		Object counts = db.getSubscriberOrderCounts(ym.getYear(), ym.getMonthValue());

		return new BistroResponse(BistroResponseStatus.SUCCESS, counts);
	}
}
