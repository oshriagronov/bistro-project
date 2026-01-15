package handlers;

import java.time.YearMonth;
import java.util.List;

import communication.AvgWaitTimePerDay;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public class GetDailyAverageWaitTimeHandler implements RequestHandler {

	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = (request == null) ? null : request.getData();
		if (!(data instanceof YearMonth ym)) {
			return new BistroResponse(BistroResponseStatus.FAILURE, null);
		}

		int year = ym.getYear();
		int month = ym.getMonthValue();

		List<AvgWaitTimePerDay> rows = db.getDailyAverageWaitTime(year, month);

		return new BistroResponse(BistroResponseStatus.SUCCESS, rows);
	}
}
