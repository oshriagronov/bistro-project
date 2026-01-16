package handlers;

import java.util.ArrayList;
import java.util.List;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.SubscriberOrderRow;
import db.ConnectionToDB;
import logic.Reservation;
import ocsf.server.ConnectionToClient;
import server.Server;

public class GetSubscriberOrdersHandler implements RequestHandler {

	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		// 1) validation
		if (request == null || request.getData() == null) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Please enter a valid sub id");
		}

		// 2) extract subscriberId (payload expected: Integer)
		final int subscriberId;
		try {
			subscriberId = (Integer) request.getData();
		} catch (ClassCastException e) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Please enter a valid sub id");
		}

		if (subscriberId <= 0) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "No subscriber with the number entered exisits");
		}

		// 3) fetch from DB (domain objects)
		List<Reservation> reservations;
		try {
			reservations = db.getOrdersBySubscriberId(subscriberId);
		} catch (Exception e) {
			return new BistroResponse(BistroResponseStatus.FAILURE, "Could't get a list of orders");
		}

		if (reservations == null) {
			reservations = new ArrayList<>();
		}

		// 4) map to DTO for TableView
		List<SubscriberOrderRow> rows = new ArrayList<>(reservations.size());

		for (Reservation r : reservations) {
			if (r == null)
				continue;

			// order id (res_id)
			int orderId = 0;
			try {

				Integer idObj = r.getOrderNumber();
				orderId = (idObj != null) ? idObj : 0;
			} catch (Exception ignore) {

			}

			String confirmationCode = r.getConfirmationCode();

			rows.add(new SubscriberOrderRow(orderId, confirmationCode, r.getOrderDate(), r.getStart_time(),
					r.getFinish_time(), r.getNumberOfGuests(), r.getStatus()));
		}

		// 5) respond
		return new BistroResponse(BistroResponseStatus.SUCCESS, rows);
	}
}
