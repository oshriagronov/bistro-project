package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public interface RequestHandler {
	BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server);
}
