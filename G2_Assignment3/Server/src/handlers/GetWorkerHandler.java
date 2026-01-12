package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a request to retrieve a worker by their ID.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link Integer}.
 * </p>
 */
public class GetWorkerHandler implements RequestHandler {

	/**
	 * Retrieves a worker by ID.
	 *
	 * @param request the incoming request (payload must be {@link Integer})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the worker if found, otherwise failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (data instanceof Integer) {
			Object worker = db.SearchWorkerById((Integer) data);

			return new BistroResponse(worker != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					worker);
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid worker ID");
	}
}
