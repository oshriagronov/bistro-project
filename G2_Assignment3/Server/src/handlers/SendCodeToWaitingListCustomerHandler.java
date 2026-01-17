package handlers;

import java.util.ArrayList;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;

public class SendCodeToWaitingListCustomerHandler implements RequestHandler {

    @Override
    public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		String phone = null;
		String email = null;
        String confirmation_code = null;

		if (data instanceof ArrayList<?>) {
			ArrayList<?> params = (ArrayList<?>) data;

			if (params.size() > 0 && params.get(0) instanceof String) {
				phone = ((String) params.get(0)).trim();
				if (phone.isEmpty()) {
					phone = null;
				}
			}

			if (params.size() > 1 && params.get(1) instanceof String) {
				email = ((String) params.get(1)).trim();
				if (email.isEmpty()) {
					email = null;
				}
			}

            if (params.size() > 2 && params.get(2) instanceof String){
                confirmation_code = ((String) params.get(2)).trim();
            }
		}
		if (hasText(phone) || hasText(email)) {
		    String message = "Your confirmation code is: " + confirmation_code;
			server.sendNotification(phone, email, message);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
		} else {
			response = new BistroResponse(BistroResponseStatus.FAILURE, null);
		}
		return response;
	}

	/**
	 * Returns whether the given value contains non-blank text that is not "null".
	 *
	 * @param value candidate string to check
	 * @return {@code true} if the value is non-null and contains text
	 */
	private static boolean hasText(String value) {
		if (value == null)
			return false;
		String trimmed = value.trim();
		return !trimmed.isEmpty() && !"null".equalsIgnoreCase(trimmed);
	}
}