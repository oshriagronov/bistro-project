package handlers;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.NewSubscriberInfo;
import db.ConnectionToDB;
import logic.Subscriber;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code ADD_SUBSCRIBER} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * {@link NewSubscriberInfo}. Hashes the provided raw password using BCrypt and
 * persists the subscriber to the database.
 * </p>
 */
public class AddSubscriberHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to add a new subscriber.
	 *
	 * @param request the request containing {@link NewSubscriberInfo} payload
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} indicating success or failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		if (data instanceof NewSubscriberInfo) {
			NewSubscriberInfo newSubscriberInfo = (NewSubscriberInfo) data;

			Subscriber subscriber = newSubscriberInfo.getSubscriber();
			String rawPassword = newSubscriberInfo.getRawPassword();

			String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
			subscriber.setPasswordHash(hash);

			try {
				db.addSubscriber(subscriber);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
			} catch (SQLException e) {
				e.printStackTrace();
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			}

		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
		}

		return response;
	}
}
