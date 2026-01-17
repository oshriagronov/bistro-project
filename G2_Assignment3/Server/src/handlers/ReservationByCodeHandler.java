package handlers;

import static handlers.RequestParsingHelper.handleStringRequest;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public class ReservationByCodeHandler implements RequestHandler {

	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		String code = (String) request.getData();

		if (code.equals(null)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Invalid code.");
		}
		Object reservation = db.getOrderByConfirmationCode(code);
		return new BistroResponse(BistroResponseStatus.SUCCESS, reservation);
	}
	

}
