package server;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mindrot.jbcrypt.BCrypt;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.NewSubscriberInfo;
import communication.StatusUpdate;
import communication.TableSizeUpdate;
import communication.TableStatusUpdate;
import communication.WorkerLoginRequest;
import db.ConnectionToDB;
import logic.Reservation;
import logic.Subscriber;
import logic.Table;
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
	final public static int DEFAULT_PORT = 5555;
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
		case GET_RESERVATION_BY_ORDER_NUMBER:
			// Expect String order number; parse and return the matching reservation.
			int orderNumber = handleStringRequest(data);
			if (orderNumber == -1) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad order number.");
				break;
			}
			dbReturnedValue = db.searchOrderByOrderNumber(orderNumber);// try catch for casting
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case GET_TABLE_BY_PHONE_AND_CODE:
			// Expect ArrayList [phone, confirmationCode]; fetch reservation then find an available table.
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
			if(data instanceof String)
				dbReturnedValue = db.getForgotConfirmationCode((String) data);
			response = new BistroResponse(
				dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				dbReturnedValue);
			break;
		case CANCEL_RESERVATION:
			// Expect Integer order number; delete reservation and return rows affected.
			dbReturnedValue = db.deleteOrderByOrderNumber((int) data);
			response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
			break;
		case CHANGE_STATUS:
			// Expect StatusUpdate; update reservation status and return rows affected.
			if (data instanceof StatusUpdate) {
				dbReturnedValue = db.changeOrderStatus(((StatusUpdate) data).getOrderNumber(),
						((StatusUpdate) data).getStatus());
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
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
			// Expect Integer table number; resolve active reservation, clear table, return reservation.
			if (data instanceof Integer){
				int tableNumber = (int) data;
				int order_number = db.getOrderNumberByTableNumber(tableNumber);
				if (order_number > 0) {
					Reservation res = db.searchOrderByOrderNumber(order_number);
					if (res != null) {
						db.changeTableResId(tableNumber);
						response = new BistroResponse(BistroResponseStatus.SUCCESS, res);
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
			if (!(data instanceof Subscriber)) {
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				break;
			}
			subscriber = (Subscriber) data;
			dbReturnedValue = db.subscriberLogin(subscriber.getSubscriberId(), subscriber.getPasswordHash());
			response = new BistroResponse(
				dbReturnedValue != null ? BistroResponseStatus.SUCCESS : BistroResponseStatus.FAILURE,
				dbReturnedValue
			);
			break;
		case ADD_TABLE:
			// Expect Integer table size; insert table and return success.
			if (data instanceof Integer) {
				
				dbReturnedValue = db.addTable((int)data);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
			} else
				response = new BistroResponse(BistroResponseStatus.FAILURE, null);
			break;
		case DELETE_TABLE:
			// Expect Integer table number; delete table and return success.
			if (data instanceof Integer) {
				dbReturnedValue = db.deleteTable((int) data);
				response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
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
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
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
}
