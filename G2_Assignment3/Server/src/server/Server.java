package server;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import service.NotificationService;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import db.ConnectionToDB;
import handlers.AddReservationHandler;
import handlers.AddSubscriberHandler;
import handlers.AddTableHandler;
import handlers.CancelReservationHandler;
import handlers.ChangeStatusHandler;
import handlers.ChangeTableSizeHandler;
import handlers.DeleteTableHandler;
import handlers.ForgotConfirmationCodeHandler;
import handlers.GetBillHandler;
import handlers.GetDailyAverageWaitTimeHandler;
import handlers.GetOpeningHoursHandler;
import handlers.GetOrdersInRangeHandler;
import handlers.GetStayingTimesHandler;
import handlers.GetSubscriberByIdHandler;
import handlers.GetSubscriberConfirmationCodeForPaymentHandler;
import handlers.GetSubscriberHistoryHandler;
import handlers.GetSubscriberOrdersHandler;
import handlers.GetSubscribersConfirmationCodesHandler;
import handlers.GetSubscribersOrdersCountsHandler;
import handlers.GetTablesHandler;
import handlers.GetTimingsHandler;
import handlers.GetTodaysReservationsHandler;
import handlers.GetWaitingListHandler;
import handlers.GetWorkerHandler;
import handlers.LoadDinersHandler;
import handlers.LoadSpecialDatesHandler;
import handlers.LoadWeeklyScheduleHandler;
import handlers.RequestHandler;
import handlers.ReservationByOrderNumberHandler;
import handlers.ReservationsByEmailHandler;
import handlers.ReservationsByPhoneHandler;
import handlers.SearchSubByEmailHandler;
import handlers.SearchSubByPhoneHandler;
import handlers.SendCodeToWaitingListCustomerHandler;
import handlers.SubscriberLoginHandler;
import handlers.TableByIdentifierAndCodeHandler;
import handlers.UpdateRegularScheduleHandler;
import handlers.UpdateSpecialDayHandler;
import handlers.UpdateSubscriberInfoHandler;
import handlers.WorkerLoginHandler;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 */
public class Server extends AbstractServer {
	// Class variables *************************************************
	public static final int DEFAULT_PORT = 5555; // The default port to listen on.
	private static final long CHECK_INTERVAL = 5; // Check every 5 minutes
	private ScheduledExecutorService checkReservationsService;
	private ConnectionToDB db;
	private final Map<BistroCommand, RequestHandler> handlers = new EnumMap<>(BistroCommand.class);

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the server.
	 * 
	 * @param port The port number to connect on.
	 */
	public Server(int port) {
		super(port);
		// Reservations
		handlers.put(BistroCommand.ADD_RESERVATION, new AddReservationHandler());
		handlers.put(BistroCommand.CANCEL_RESERVATION, new CancelReservationHandler());
		handlers.put(BistroCommand.CHANGE_STATUS, new ChangeStatusHandler());
		handlers.put(BistroCommand.GET_BILL, new GetBillHandler());

		handlers.put(BistroCommand.GET_RESERVATION_BY_ORDER_NUMBER, new ReservationByOrderNumberHandler());
		handlers.put(BistroCommand.GET_RESERVATIONS_BY_EMAIL, new ReservationsByEmailHandler());
		handlers.put(BistroCommand.GET_ACTIVE_RESERVATIONS_BY_PHONE, new ReservationsByPhoneHandler());
		handlers.put(BistroCommand.GET_TODAYS_ORDERS, new GetTodaysReservationsHandler());

		handlers.put(BistroCommand.GET_TABLE_BY_IDENTIFIER_AND_CODE, new TableByIdentifierAndCodeHandler());
		handlers.put(BistroCommand.FORGOT_CONFIRMATION_CODE, new ForgotConfirmationCodeHandler());
		handlers.put(BistroCommand.SEND_CODE_TO_WAITING_LIST, new SendCodeToWaitingListCustomerHandler());

		// Subscribers
		handlers.put(BistroCommand.ADD_SUBSCRIBER, new AddSubscriberHandler());
		handlers.put(BistroCommand.SUBSCRIBER_LOGIN, new SubscriberLoginHandler());
		handlers.put(BistroCommand.SEARCH_SUB_BY_EMAIL, new SearchSubByEmailHandler());
		handlers.put(BistroCommand.SEARCH_SUB_BY_PHONE, new SearchSubByPhoneHandler());
		handlers.put(BistroCommand.GET_SUBSCRIBER_BY_ID, new GetSubscriberByIdHandler());
		handlers.put(BistroCommand.GET_SUBSCRIBER_HISTORY, new GetSubscriberHistoryHandler());
		handlers.put(BistroCommand.GET_SUBSCRIBER_CONFIRMATION_CODE_FOR_PAYMENT,new GetSubscriberConfirmationCodeForPaymentHandler());
		handlers.put(BistroCommand.GET_SUBSCRIBER_CONFIRMATION_CODES, new GetSubscribersConfirmationCodesHandler());
		handlers.put(BistroCommand.GET_SUBSCRIBER_ORDER_COUNTS, new GetSubscribersOrdersCountsHandler());
		handlers.put(BistroCommand.UPDATE_SUBSCRIBER_INFO, new UpdateSubscriberInfoHandler());
		handlers.put(BistroCommand.GET_SUB, new GetSubscriberByIdHandler());
		handlers.put(BistroCommand.GET_SUBSCRIBER_ORDERS, new GetSubscriberOrdersHandler());

		// Tables
		handlers.put(BistroCommand.GET_TABLES, new GetTablesHandler());
		handlers.put(BistroCommand.ADD_TABLE, new AddTableHandler());
		handlers.put(BistroCommand.DELETE_TABLE, new DeleteTableHandler());
		handlers.put(BistroCommand.CHANGE_TABLE_SIZE, new ChangeTableSizeHandler());
		handlers.put(BistroCommand.LOAD_DINERS, new LoadDinersHandler());

		// Schedule
		handlers.put(BistroCommand.LOAD_WEEKLY_SCHEDULE, new LoadWeeklyScheduleHandler());
		handlers.put(BistroCommand.LOAD_SPECIAL_DATES, new LoadSpecialDatesHandler());
		handlers.put(BistroCommand.UPDATE_REGULAR_SCHEDULE, new UpdateRegularScheduleHandler());
		handlers.put(BistroCommand.UPDATE_SPECIAL_DAY, new UpdateSpecialDayHandler());
		handlers.put(BistroCommand.GET_OPENING_HOURS, new GetOpeningHoursHandler());

		// Reports
		handlers.put(BistroCommand.GET_TIMINGS, new GetTimingsHandler());
		handlers.put(BistroCommand.GET_STAYING_TIMES, new GetStayingTimesHandler());
		handlers.put(BistroCommand.GET_ORDERS_IN_RANGE, new GetOrdersInRangeHandler());
		handlers.put(BistroCommand.GET_DAILY_AVG_WAIT_TIME, new GetDailyAverageWaitTimeHandler());

		// Worker
		handlers.put(BistroCommand.WORKER_LOGIN, new WorkerLoginHandler());
		handlers.put(BistroCommand.GET_WORKER, new GetWorkerHandler());
		handlers.put(BistroCommand.GET_WAITING_LIST, new GetWaitingListHandler());
	}


	// Handle Messages and parsing methods(override)
	// ************************************************
	/**
	 * This method handles any messages received from the client.
	 * 
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	@Override
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (!(msg instanceof BistroRequest)) {
			try {
				client.sendToClient(new BistroResponse(BistroResponseStatus.INVALID_REQUEST, "Expected BistroRequest"));
			} catch (IOException e) {
				log("The message is has invalid format.");
			}
			return;
		}

		BistroRequest request = (BistroRequest) msg;

		RequestHandler handler = handlers.get(request.getCommand());
		BistroResponse response = (handler == null)
				? new BistroResponse(BistroResponseStatus.INVALID_REQUEST,
						"No handler for command: " + request.getCommand())
				: handler.handle(request, client, db, this);

		try {
			client.sendToClient(response);
		} catch (IOException e) {
			log("Method sendToClient failed to send response.");
		}
	}

	// Server methods ************************************************
	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		String ipString = null;
		try {
			InetAddress host = InetAddress.getLocalHost();
			ipString = host.getHostAddress();
			log("Server listening for connections on port " + getPort());
		} catch (UnknownHostException e) {
			log("Server listening for connections on port " + getPort());
		}
		db = new ConnectionToDB();
		if (ServerScreen.instance != null) {
			ServerScreen.instance.updateServerInfo(ipString, ConnectionToDB.getDbPassword());
		}
		startCheckReservationsTimer();
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
		checkReservationsService.shutdown();
	}

		/**
	 * Updates the database password used by the server-side connection pool.
	 *
	 * @param password database password
	 */
	public static void ServerScreen(String password) {
		ConnectionToDB.setPassword(password);
	}

	/**
	 * Writes the provided message to the GUI log if available, otherwise stdout.
	 * 
	 * @param msg the text to append to the log.
	 */
	private void log(String msg) {
		if (ServerScreen.instance != null)
			ServerScreen.instance.appendLog(msg);
		else
			System.out.println("[SERVER] " + msg);
	}



	// Client methods ************************************************
	/**
	 * Records the remote host information when a new client connects and writes a
	 * connection log entry. The formatted host name/IP is stored via use
	 * setInfo(String, Object) so it can be retrieved to later even after the socket
	 * closes.
	 * 
	 * @param client active connection that was just established
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		InetAddress addr = client.getInetAddress();
		if (addr != null) {
			client.setInfo("remoteAddress", addr.getHostName() + " (" + addr.getHostAddress() + ")");
		}
		log(client.getInfo("remoteAddress").toString() + " connected");
	}

	/**
	 * Logs a disconnection event for a client that terminated with an exception.
	 * The client socket has already been closed, so the previously stored
	 * "remoteAddress" info is used to identify which client disconnected.
	 * 
	 * @param client    connection that raised the exception
	 * @param exception cause of the disconnection
	 */
	@Override
	synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
		Object stored = client.getInfo("remoteAddress");
		String id = stored != null ? stored.toString() : "Unknown client";
		log(id + " disconnected");
	}


	// *********************** */ Notification service *******************
	/**
	 * Starts a scheduled task that periodically checks reservations for reminders.
	 */
	private void startCheckReservationsTimer() {
		checkReservationsService = Executors.newSingleThreadScheduledExecutor();
		checkReservationsService.scheduleAtFixedRate(this::checkReservations, CHECK_INTERVAL, CHECK_INTERVAL,
				TimeUnit.SECONDS);
	}

	/**
	 * Loads reservations that need reminders and dispatches notifications.
	 */
	private void checkReservations() {
		List<List<String>> reservationsToSendReminder = db.getReservationToSendReminder();
		List<List<String>> reservationsToSendPaymentReminder = db.getReservationToSendPaymentReminder();
		List<List<String>> reservationsToCancel = db.getLateReservationToCancel();
		// *** send reminder to arrive
		if (reservationsToSendReminder != null){
			for (List<String> reservation : reservationsToSendReminder) {
				StringBuilder sb = new StringBuilder();
				String phone = reservation.get(1);
				String email = reservation.get(2);
				sb.append("Hey, you have 2 hours before your reservation.\n");
				sb.append("Reservation Id: " + reservation.get(0) + "\nwith confirmation code: " + reservation.get(4)
				+ "\nat " + reservation.get(3));
				db.setRemindedFieldToTrue(Integer.valueOf(reservation.get(0)));
				sendNotification(phone, email, sb.toString());
			}
			if(reservationsToSendPaymentReminder.size() == db.setReservationsAsReminded(reservationsToSendPaymentReminder)){
				log("[SYSTEM] All reservation that got reminders marked as REMINDED.");
			}
			else{
				log("[SYSTEM] There was some problem with mark reservations as reminded.");
			}
		}
		// ** send the bill to the customer of the reservation that past the endtime and clear the tables
		if (reservationsToSendPaymentReminder != null){
			for (List<String> reservation : reservationsToSendPaymentReminder) {
				StringBuilder sb = new StringBuilder();
				String phone = reservation.get(1);
				String email = reservation.get(2);
				sb.append("Sent receipt for your reservation.\n");
				sb.append("Reservation Id: " + reservation.get(0));
				sendNotification(phone, email, sb.toString());
			}
			if(reservationsToSendPaymentReminder.size() == db.setReservationAfterEndTimeAsCompleted(reservationsToSendPaymentReminder)){
				log("[SYSTEM] All reservation that exceed the finish time are got the bill and cleared the tables.");
			}
			else{
				log("[SYSTEM] There was some problem with clearing the tables or set reservations as completed.");
			}
		}

		if (reservationsToCancel != null) {
			for (List<String> reservation : reservationsToCancel){
				StringBuilder sb = new StringBuilder();
				String phone = reservation.get(1);
				String email = reservation.get(2);
				sb.append("Customer is 15 minutes late, reservation is canceled.\n");
				sb.append("Reservation Id: " + reservation.get(0));
				sendNotification(phone, email, sb.toString());
			}
			if(reservationsToCancel.size() == db.setReservationToCancelIfCustomerLate(reservationsToCancel)){
				log("[SYSTEM] All the reservations that customer is late are canceled.");
			}
			else{
				log("[SYSTEM] There was some problem with clearing the tables or set reservations as cancel.");
			}
		}
	}

	/**
	 * Returns whether the given value contains non-blank text that is not "null".
	 *
	 * @param value candidate string to check
	 * @return true when value is non-null, trimmed non-empty, and not "null"
	 */
	private static boolean hasText(String value) {
		if (value == null)
			return false;
		String trimmed = value.trim();
		return !trimmed.isEmpty() && !"null".equalsIgnoreCase(trimmed);
	}

	/**
	 * Sends the reminder message via SMS when a phone number is available;
	 * otherwise falls back to email if present, or logs a skip.
	 *
	 * @param phone   phone number to send to (may be blank)
	 * @param email   email to send to if phone is missing
	 * @param message reminder text to send
	 */
	public void sendNotification(String phone, String email, String message) {
		NotificationService service = NotificationService.getInstance();
		boolean sent = false;
		if (hasText(phone)) {
			log(service.sendSmsMessage(phone, message));
			sent = true;
		}
		if (hasText(email)) {
			log(service.sendEmailMessage(email, message));
			sent = true;
		}
		if (!sent) {
			log("Skipping notification: missing phone and email.");
		}
	}

}
