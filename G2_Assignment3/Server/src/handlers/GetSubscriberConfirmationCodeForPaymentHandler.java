package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public class GetSubscriberConfirmationCodeForPaymentHandler implements RequestHandler {

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
