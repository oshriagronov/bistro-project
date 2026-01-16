package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.WorkerLoginRequest;
import db.ConnectionToDB;
import logic.Worker;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles a worker login request.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link WorkerLoginRequest} containing the worker's credentials.
 * </p>
 */
public class WorkerLoginHandler implements RequestHandler {

	/**
	 * Authenticates a worker using username and password.
	 *
	 * @param request the incoming request (payload must be
	 *                {@link WorkerLoginRequest})
	 * @param client  the client that sent the request
	 * @param db      database access layer
	 * @param server  server instance
	 * @return a response containing the authenticated {@link Worker} on success, or
	 *         a failure status otherwise
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();

		if (data instanceof WorkerLoginRequest) {
			WorkerLoginRequest w = (WorkerLoginRequest) data;

			Worker worker = db.workerLogin(w.getUsername(), w.getPassword());

			if (worker != null) {
				return new BistroResponse(BistroResponseStatus.SUCCESS, worker);
			}

			return new BistroResponse(BistroResponseStatus.FAILURE, "Wrong username or password.");
		}

		return new BistroResponse(BistroResponseStatus.FAILURE, "Failed to login");
	}
}
