package handlers;

import java.util.ArrayList;

import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import ocsf.server.ConnectionToClient;
import server.Server;
import service.NotificationService;

/**
 * Handles {@code FORGOT_CONFIRMATION_CODE} requests.
 * <p>
 * Expects the request payload ({@link BistroRequest#getData()}) to be an
 * {@link ArrayList} containing a phone number and/or an email address. If a
 * matching confirmed reservation is found, the confirmation code and start time
 * are sent via SMS and/or email.
 * </p>
 */
public class ForgotConfirmationCodeHandler implements RequestHandler {

	/**
	 * Processes an incoming {@link BistroRequest} to retrieve and send a forgotten
	 * confirmation code.
	 *
	 * @param request the request containing identifier data (phone/email)
	 * @param client  the client connection that sent the request
	 * @param db      database access layer
	 * @param server  the running server instance
	 * @return a {@link BistroResponse} indicating success or failure
	 */
	@Override
	public BistroResponse handle(BistroRequest request, ConnectionToClient client, ConnectionToDB db, Server server) {

		Object data = request.getData();
		BistroResponse response;

		String phone = null;
		String email = null;

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
		}

		if (hasText(phone) || hasText(email)) {
			String identifier = hasText(phone) ? phone : email;

			ArrayList<String> result = db.getForgotConfirmationCode(identifier);
			if (result != null) {
				String message = "Your confirmation code is: " + result.get(0) + "\nStart time is: " + result.get(1);

				NotificationService service = NotificationService.getInstance();

				if (hasText(phone)) {
					service.sendSmsMessage(phone, message);
				}
				if (hasText(email)) {
					service.sendEmailMessage(email, message);
				}

				response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			}
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
