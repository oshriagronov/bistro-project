package handlers;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

/**
 * Handles {@code CANCEL_RESERVATION_BY_ID} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be a
 * confirmation code (String or Integer). Cancels the reservation by setting
 * its status to 'CANCELLED'.
 * </p>
 */
public class CancelReservationByIdHandler implements RequestHandler {

    /**
     * Processes an incoming {@link BistroRequest} to cancel a reservation using only the confirmation code.
     *
     * @param request the request containing the confirmation code as payload
     * @param client  the client connection that sent the request
     * @param db      database access layer
     * @param server  the running server instance
     * @return a {@link BistroResponse} indicating success or failure
     */
    @Override
    public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

        Object data = request.getData();
        int id;

        // Try to parse the code safely
        if (data instanceof Integer) {
            id = (Integer) data;
        } else if (data instanceof String) {
            try {
                id = Integer.parseInt((String) data);
            } catch (NumberFormatException e) {
                return new BistroResponse(BistroResponseStatus.INVALID_REQUEST, "Confirmation code must be numeric.");
            }
        } else {
            return new BistroResponse(BistroResponseStatus.INVALID_REQUEST, "Invalid confirmation code.");
        }

        // Call the database method to cancel
        int result = db.CancelReservationByID(id);

        if (result > 0) {
            return new BistroResponse(BistroResponseStatus.SUCCESS, "Reservation canceled successfully.");
        } else {
            return new BistroResponse(BistroResponseStatus.NOT_FOUND, "Reservation not found or already canceled.");
        }
    }
}
