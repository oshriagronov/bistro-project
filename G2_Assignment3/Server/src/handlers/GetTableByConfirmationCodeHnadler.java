package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import logic.Reservation;
import ocsf.server.ConnectionToClient;
import server.Server;

public class GetTableByConfirmationCodeHnadler  implements RequestHandler{

    @Override
    public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {
        // Expect String of confirmation code; fetch reservation then find an available table.
        BistroResponse response;
        Object data = request.getData();
        if (data instanceof String) {
            Reservation res = db.getConfirmedReservationByConfirmationCode(Integer.parseInt((String)data));
            if (res != null) {
                int tableNum = db.searchAvailableTableBySize(res.getNumberOfGuests());
                if (tableNum > 0) {
                    db.updateTableResId(tableNum, res.getOrderNumber());
                    db.updateReservationTimesAfterAcceptation(res.getOrderNumber());
                    response = new BistroResponse(BistroResponseStatus.SUCCESS, tableNum);
                } else {
                    response = new BistroResponse(BistroResponseStatus.NO_AVAILABLE_TABLE, null);
                }
            } else {
                response = new BistroResponse(BistroResponseStatus.NOT_FOUND, null);
            }
        } else {
            response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
        }
        return response;
    }

}
