package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public class GetSubscriberConfirmationCodeForPaymentHandler implements RequestHandler {

	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		// TODO Auto-generated method stub
		return null;
	}

}
