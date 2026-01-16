package handlers;

import java.util.List;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.WaitlistRow;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public class GetWaitingListHandler implements RequestHandler {
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
		List<WaitlistRow> rows = db.getTodayWaitlist();
		return new BistroResponse(BistroResponseStatus.SUCCESS, rows);
	}
}
