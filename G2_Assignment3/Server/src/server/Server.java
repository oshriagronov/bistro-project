package server;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import service.NotificationService;
import org.mindrot.jbcrypt.BCrypt;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventType;
import communication.NewSubscriberInfo;
import communication.OrdersInRangeRequest;
import communication.ServerEvent;
import communication.StatusUpdate;
import communication.TableSizeUpdate;
import communication.TableStatusUpdate;
import communication.WorkerLoginRequest;
import db.ConnectionToDB;
import logic.Reservation;
import logic.SpecialDay;
import logic.Status;
import logic.Subscriber;
import logic.Table;
import logic.WeeklySchedule;
import logic.Worker;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 */
public class Server extends AbstractServer {
	// Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	public static final int DEFAULT_PORT = 5555;
	private static final long CHECK_INTERVAL = 5; // Check every 5 minutes
	private ScheduledExecutorService checkReservationsService;
	private ConnectionToDB db;
	// Constructors ****************************************************

	/**
	 * Constructs an instance of the server.
	 * 
	 * @param port The port number to connect on.
	 */
	public Server(int port) {
		super(port);
	}

	/**
	 * Updates the database password used by the server-side connection pool.
	 *
	 * @param password database password
	 */
	public static void ServerScreen(String password) {
		ConnectionToDB.setPassword(password);
	}

	// Handle Messages and parsing methods(override)
	// ************************************************
	/**
	 * This method handles any messages received from the client.
	 * 
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		BistroRequest request = (BistroRequest) msg; // try catch for casting
		Object dbReturnedValue = null;
		Object data = request.getData();
		Subscriber subscriber;
		BistroResponse response;
		switch (request.getCommand()) {
		case GET_ACTIVE_RESERVATIONS_BY_PHONE:
			// Expect String phone; query reservations by phone and return list.
			if (!(data instanceof String)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid phone number.");
				break;
			}
			dbReturnedValue = db.searchOrdersByPhoneNumberList((String) data);// try catch for casting
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_RESERVATIONS_BY_EMAIL:
			// Expect String phone; query reservations by email and return list.
			if (!(data instanceof String)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid email adress");
				break;
			}
			dbReturnedValue = db.searchOrdersByEmail((String) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_RESERVATION_BY_ORDER_NUMBER:
			// Expect String order number; parse and return the matching reservation.
			int orderNumber = handleStringRequest(data);
			if (orderNumber == -1) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Wrong order number.");
			}
			dbReturnedValue = db.searchOrderByOrderNumber(orderNumber);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_ALL_PENDING_RESERVATIONS:
			dbReturnedValue = db.getAllPendingReservationsOrdered();
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_TABLE_BY_CONFIRMATION_CODE:
			// Expect String of confirmation code; fetch reservation then find an available table.
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
			break;
			
		case FORGOT_CONFIRMATION_CODE:
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
						log(service.sendSmsMessage(phone, message));
					}
					if (hasText(email)) {
						log(service.sendEmailMessage(email, message));
					}
					response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
				} else {
					response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				}
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			}
			break;
		case SEARCH_SUB_BY_PHONE:
			if (data instanceof String) {
				dbReturnedValue = db.SearchSubscriberByPhone((String) data);
				response = new BistroResponse(
						dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
						dbReturnedValue);
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid phone format");
			}
			break;
		case SEARCH_SUB_BY_EMAIL:
			if (data instanceof String) {
				dbReturnedValue = db.SearchSubscriberByEmail((String) data);
				response = new BistroResponse(
						dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
						dbReturnedValue);
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid email format");
			}
			break;

		case ADD_RESERVATION:
			// Handle adding a new reservation to the database
			if (data instanceof Reservation) {
				String success = db.insertReservation((Reservation) data);
				if(success!=null)
				{
					response = new BistroResponse(BistroResponseStatus.SUCCESS, success);
					sendToAllClients(new ServerEvent(EventType.ORDER_CHANGED));
				}
				else
					response = new BistroResponse(BistroResponseStatus.FAILURE, "Reservation failed");
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid reservation data");
			}
			break;
			
		case CANCEL_RESERVATION:
			// Expect StatusUpdate; update reservation status and return rows affected.

			if (data instanceof StatusUpdate) {
				StatusUpdate statusUpdate = (StatusUpdate) data;
				if(statusUpdate.getEmail() == null && statusUpdate.getPhoneNumber() == null)
					response = new BistroResponse(BistroResponseStatus.FAILURE, "invalid information.");
				else{

					int result = db.CancelReservation(statusUpdate.getOrderNumber(), statusUpdate.getEmail(), statusUpdate.getPhoneNumber());
					
					if (result > 0)
						response = new BistroResponse(BistroResponseStatus.SUCCESS, "Cancel succeeded.");
					else
						response = new BistroResponse(BistroResponseStatus.FAILURE, "Cancel failed."); 
				}
			}
			else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Cancel failed.");
			break;
		case CHANGE_STATUS:
			// Expect StatusUpdate; update reservation status and return rows affected.
			if (data instanceof StatusUpdate) {
				StatusUpdate statusUpdate = (StatusUpdate) data;
				dbReturnedValue = db.changeOrderStatus(statusUpdate.getPhoneNumber(), statusUpdate.getOrderNumber(),
						statusUpdate.getStatus());
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				sendToAllClients(new ServerEvent(EventType.ORDER_CHANGED));
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
			break;
		case GET_TABLES:
			// Expect no payload; return all tables with current status from DB.
			dbReturnedValue = db.loadTables();
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case CHANGE_TABLE_SIZE:
			// Expect TableSizeUpdate; update table size and return rows affected.
			if (data instanceof TableSizeUpdate) {
				dbReturnedValue = db.changeTableSize(((TableSizeUpdate) data).getTable_number(),
						((TableSizeUpdate) data).getTable_size());
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
			break;
		case GET_BILL:
			// Expect ArrayList [identifier, confirmationCode]; fetch reservation then get bill.
			if (data instanceof String) {
				int code = Integer.parseInt((String) data);
				Reservation res = db.getAcceptedReservationByConfirmationCode(code);
				if (res != null) {
					db.changeOrderStatus(res.getPhone_number(), res.getOrderNumber(), Status.COMPLETED);
					db.updateReservationTimesAfterCompleting(res.getOrderNumber());
					db.clearTableByResId(res.getOrderNumber());
					response = new BistroResponse(BistroResponseStatus.SUCCESS, res);
				} else {
					response = new BistroResponse(BistroResponseStatus.NOT_FOUND, null);
				}
			} else {
				response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
			}
			break;
		case SUBSCRIBER_LOGIN:
			// Validates login payload ([subscriberId, rawPassword]) and checks credentials via
			// db.subscriberLogin; triggered by Client/src/subscriber/SubscriberLoginScreen.java.
			String username = null;
			String password = null;
			// Expect [subscriberId, raw password]; verify and return subscriber id on success.
			if (data instanceof ArrayList<?>) {
				ArrayList<?> params = (ArrayList<?>) data;
				if (params.size() >= 2) {
					if (params.get(0) instanceof String) {
						username = ((String) params.get(0)).trim();
						if (username.isEmpty()) {
							username = null;
						}
					}
					if (params.get(1) instanceof String) {
						password = (String) params.get(1);
						if (!hasText(password)) {
							password = null;
						}
					}
				}
			}
			if (!hasText(username) || !hasText(password)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				break;
			}
			int subscriberId = db.subscriberLogin(username, password);
			response = new BistroResponse(subscriberId > 0 ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE, 
				subscriberId > 0 ? subscriberId :null);
			break;
		case GET_SUBSCRIBER_HISTORY:
			// Expect Subscriber with id+raw password; verify and return reservations list.
			if (!(data instanceof Integer)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				break;
			}
			int sub_id = (Integer) data;
			dbReturnedValue = db.getSubscriberHistory(sub_id);
			response = new BistroResponse(
					dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					dbReturnedValue);
			break;
		// Handles a request to retrieve a subscriber by ID.
		// Validates the incoming data type, queries the database, and returns the
		// result.
		case GET_SUBSCRIBER_BY_ID:
			// Validate that the received data is an Integer (subscriber ID)
			if (!(data instanceof Integer)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
				break;
			}
			// Query the database for the subscriber
			dbReturnedValue = db.SearchSubscriberById((Integer) data);
			// Build the response based on whether the subscriber was found
			response = new BistroResponse(
					dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					dbReturnedValue);
			break;

		case GET_SUBSCRIBER_CONFIRMATION_CODES:
			if (!(data instanceof Integer)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
				break;
			}
			dbReturnedValue = db.getConfirmedReservationCodesBySubscriber((Integer) data);
			response = new BistroResponse(
					dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					dbReturnedValue);
			break;
		case GET_SUBSCRIBER_CONFIRMATION_CODE_FOR_PAYMENT:
			if (!(data instanceof Integer)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
				break;
			}
			dbReturnedValue = db.getAcceptedReservationCodeBySubscriber((Integer) data);
			response = new BistroResponse(
					dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					dbReturnedValue);
			break;
		case UPDATE_SUBSCRIBER_INFO:
			if (!(data instanceof Subscriber)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				break;
			}
			subscriber = (Subscriber) data;
			if (subscriber.getSubscriberId() == null) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Missing subscriber id.");
				break;
			}
			int updatedRows = db.updateSubscriberInfo(subscriber);
			response = new BistroResponse(updatedRows >= 1 ? BistroResponseStatus.SUCCESS
					: BistroResponseStatus.FAILURE, null);
			break;

		case ADD_TABLE:
			// Expect Integer table size; insert table and return success.
			if (data instanceof Integer) {

				dbReturnedValue = db.addTable((int) data);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
				sendToAllClients(new ServerEvent(EventType.TABLE_CHANGED));
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			break;
		case DELETE_TABLE:
			// Expect Integer table number; delete table and return success.
			if (data instanceof Integer) {
				dbReturnedValue = db.deleteTable((int) data);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
				sendToAllClients(new ServerEvent(EventType.TABLE_CHANGED));
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			break;
		case ADD_SUBSCRIBER:
			// Expect NewSubscriberInfo; hash password, insert subscriber, return success.
			if (data instanceof NewSubscriberInfo) {
				NewSubscriberInfo newSubscriberInfo = (NewSubscriberInfo) data;
				subscriber = newSubscriberInfo.getSubscriber();
				String rawPassword = newSubscriberInfo.getRawPassword();
				String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
				subscriber.setPasswordHash(hash);
				try {
					db.addSubscriber(subscriber);
				} catch (SQLException e) {

					e.printStackTrace();
				}
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
			break;
		case WORKER_LOGIN:
			// Expect WorkerLoginRequest; verify and return Worker on success.
			if (data instanceof WorkerLoginRequest) {
				WorkerLoginRequest w = (WorkerLoginRequest) data;
				Worker res = db.workerLogin(w.getUsername(), w.getPassword());
				if (res != null)
					response = new BistroResponse(BistroResponseStatus.SUCCESS, res);
				else
					response = new BistroResponse(BistroResponseStatus.FAILURE, "Wrong username or password.");
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Failed to login");
			break;
		case GET_WORKER:
			if (data instanceof Integer) {
				dbReturnedValue = db.SearchWorkerById((int) data);
				response = new BistroResponse(
						dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
						dbReturnedValue);
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid worker ID");
			}
			break;
		case LOAD_DINERS:
			// Expect no payload; return current diners per table for staff view.
			dbReturnedValue = db.loadCurrentDiners();
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;

		case GET_TIMINGS:
			if (data instanceof YearMonth)
				dbReturnedValue = db.getDailySlotStats(((YearMonth) data).getYear(),
						((YearMonth) data).getMonthValue());
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_STAYING_TIMES:
			if (data instanceof YearMonth)
				dbReturnedValue = db.getDailyAverageStay(((YearMonth) data).getYear(),
						((YearMonth) data).getMonthValue());
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case LOAD_WEEKLY_SCHEDULE:
			dbReturnedValue = db.loadRegularTimes();
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case LOAD_SPECIAL_DATES:
			if (data instanceof Integer) {
				dbReturnedValue = db.loadUpcomingSpecialDates((int) data);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			}
			else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Failed loading the spedcial dates");
			break;
		case UPDATE_REGULAR_SCHEDULE:
			if (data instanceof WeeklySchedule) {
				WeeklySchedule ws = (WeeklySchedule) data;
				dbReturnedValue = db.updateRegularDayTimes(ws.getDayOfWeek().toString(), ws.getOpen(), ws.getClose());
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Failed to update schedule");
			break;
		case UPDATE_SPECIAL_DAY:
			if (data instanceof SpecialDay) {
				SpecialDay sd = (SpecialDay) data;
				dbReturnedValue = db.updateSpecialDay(sd.getDay(), sd.getOpen(), sd.getClose());
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Failed to update the chosen date");
			break;
			case GET_OPENING_HOURS:
			if (data instanceof LocalDate) {
				LocalTime[] times = db.getOpeningHours((LocalDate) data);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, times);
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			}
			break;
		case GET_ORDERS_IN_RANGE:
			if (data instanceof OrdersInRangeRequest) {
				OrdersInRangeRequest req = (OrdersInRangeRequest) data;

				LocalDate date = req.getDate();
				LocalTime time = req.getTime();
				dbReturnedValue = db.getNumDinersInTwoHoursWindow(date, time);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			}
			break;
		default:
			// Expect unknown/unsupported command; return INVALID_REQUEST.
			response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
			break;
		}
		try {
			client.sendToClient(response);
		} catch (IOException e) {
			System.out.println("Error: Can't send message to client.");
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

	// Instance methods ************************************************
	/**
	 * Method that take data Object and check if it a string, if not then throw
	 * exception. if everything alright then return the number in int.
	 * 
	 * @param data request payload to parse
	 * @return parsed integer, or -1 if invalid
	 */
	private int handleStringRequest(Object data) {
		int numberReturned = -1;
		try {
			if (!(data instanceof String)) {
				throw new IllegalArgumentException(
						"Expected String, got: " + (data == null ? "null" : data.getClass().getName()));
			}
			numberReturned = Integer.parseInt((String) data);

		} catch (NumberFormatException e) {
			log(e.getMessage());
		} catch (IllegalArgumentException e) {
			log("Error: handleStringRequest: couldn't parse string to int.");
		}
		return numberReturned;
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
			System.out.println(msg);
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
				sendReminder(phone, email, sb.toString());
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
				sendReminder(phone, email, sb.toString());
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
				sendReminder(phone, email, sb.toString());
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
	private void sendReminder(String phone, String email, String message) {
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
		if(!sent) {
			log("Skipping notification: missing phone and email.");
		}
	}

}
