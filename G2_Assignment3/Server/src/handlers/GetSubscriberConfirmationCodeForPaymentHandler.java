package handlers;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles requests for a subscriber's accepted reservation confirmation code during payment flow.
 */
public class GetSubscriberConfirmationCodeForPaymentHandler implements RequestHandler {

	/**
	 * Looks up the accepted reservation code for the subscriber ID provided in the request data.
	 *
	 * @param request the incoming request containing a subscriber ID as its data payload
	 * @param client the client connection that issued the request
	 * @param db the database connection used to retrieve the confirmation code
	 * @param server the server instance handling the request
	 * @return a success response with the confirmation code if found, or a failure response otherwise
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		Object data = request.getData();
		BistroResponse response;
		if (!(data instanceof Integer)) {
			response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
		}
		else{
			Object dbReturnedValue = db.getAcceptedReservationCodeBySubscriber((Integer) data);
			response = new BistroResponse(
				dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				dbReturnedValue);
		}
		return response;
	}
}
