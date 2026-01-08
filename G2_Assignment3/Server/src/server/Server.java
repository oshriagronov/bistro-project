package server;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import service.NotificationService;
import org.mindrot.jbcrypt.BCrypt;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventType;
import communication.NewSubscriberInfo;
import communication.ServerEvent;
import communication.StatusUpdate;
import communication.TableSizeUpdate;
import communication.TableStatusUpdate;
import communication.WorkerLoginRequest;
import db.ConnectionToDB;
import logic.Reservation;
import logic.SpecialDay;
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
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad phone number.");
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
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad order number.");
			}
			dbReturnedValue = db.searchOrderByOrderNumber(orderNumber);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_TABLE_BY_PHONE_AND_CODE:
			// Expect ArrayList [phone, confirmationCode]; fetch reservation then find an
			// available table.
			if (data instanceof ArrayList) {
				ArrayList<?> params = (ArrayList<?>) data;
				String phone = (String) params.get(0);
				int code = Integer.parseInt((String) params.get(1));

				Reservation res = db.getOrderByPhoneAndCode(phone, code);
				if (res != null) {
					int tableNum = db.searchAvailableTableBySize(res.getNumberOfGuests());
					if (tableNum > 0) {
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
			if (data instanceof String)
				dbReturnedValue = db.getForgotConfirmationCode((String) data);
			response = new BistroResponse(
					dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
					dbReturnedValue);
			break;

		case ADD_RESERVATION:
			// Handle adding a new reservation to the database
			if (data instanceof Reservation) {
				int success = db.insertReservation((Reservation) data);
				response = new BistroResponse(success > 0 ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
						success > 0 ? "Reservation saved" : "Reservation failed");
			} else {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid reservation data");
			}
			break;

		case CANCEL_RESERVATION:
			// Expect Integer order number; delete reservation and return rows affected.
			dbReturnedValue = db.deleteOrderByOrderNumber((int) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
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
			// Expect Integer table number; resolve active reservation, clear table, return
			// reservation.
			if (data instanceof Integer) {
				int tableNumber = (int) data;
				int order_number = db.getOrderNumberByTableNumber(tableNumber);
				if (order_number > 0) {
					Reservation res = db.searchOrderByOrderNumber(order_number);
					if (res != null) {
						db.changeTableResId(tableNumber);
						response = new BistroResponse(BistroResponseStatus.SUCCESS, res);
						sendToAllClients(new ServerEvent(EventType.TABLE_CHANGED));
						sendToAllClients(new ServerEvent(EventType.ORDER_CHANGED));
					} else {
						response = new BistroResponse(BistroResponseStatus.FAILURE, "Order not found.");
					}
				} else {
					response = new BistroResponse(BistroResponseStatus.FAILURE, "No active order for this table.");
				}
			} else
				response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
			break;
		case SUBSCRIBER_LOGIN:
			// Expect Subscriber with id+raw password; verify and return reservations list.
			if (data instanceof Subscriber) {
				subscriber = (Subscriber) data;
				response = new BistroResponse(
						db.subscriberLogin(subscriber.getSubscriberId(), subscriber.getPasswordHash())
								? BistroResponseStatus.SUCCESS
								: BistroResponseStatus.FAILURE,
						null);
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
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
		case GET_SUB:
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
		case GET_SUBSCRIBER_CONFIRMATION_CODES:
			if (!(data instanceof Integer)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Invalid subscriber ID format");
				break;
			}

			dbReturnedValue = db.getConfirmedReservationCodesBySubscriber((Integer) data);
			response = new BistroResponse(
				dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				dbReturnedValue
			);
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
		default:
			// Expect unknown/unsupported command; return INVALID_REQUEST.
			response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
			break;
				break;
		case GET_STAYING_TIMES:
			if (data instanceof Integer)
				dbReturnedValue = db.getMonthlyAverageStay((int) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
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
				TimeUnit.MINUTES);
	}

	/**
	 * Loads reservations that need reminders and dispatches notifications.
	 */
	private void checkReservations() {
		List<List<String>> reservationsToSendReminder = db.getReservationToSendReminder();
		List<List<String>> reservationsToSendPaymentReminder = db.getReservationToSendPaymentReminder();
		if (reservationsToSendReminder != null)
			for (List<String> reservation : reservationsToSendReminder) {
				StringBuilder sb = new StringBuilder();
				String phone = reservation.get(1);
				String email = reservation.get(2);
				sb.append("Hey, you have 2 hours before your reservation.\n");
				sb.append("Reservation Id: " + reservation.get(0) + "\nwith confirmation code: " + reservation.get(4)
						+ "\nat " + reservation.get(3));
				sendReminder(phone, email, sb.toString());
			}
		if (reservationsToSendPaymentReminder != null)
			for (List<String> reservation : reservationsToSendPaymentReminder) {
				String phone = reservation.get(1);
				String email = reservation.get(2);
				StringBuilder sb = new StringBuilder();
				sb.append("Sent receipt for your reservation.\n");
				sb.append("Reservation Id: " + reservation.get(0));
				sendReminder(phone, email, sb.toString());
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
		if (hasText(phone)) {
			log(service.sendSmsMessage(phone, message));
		} else if (hasText(email)) {
			log(service.sendEmailMessage(email, message));
		} else {
			log("Skipping notification: missing phone and email.");
		}
	}

}
