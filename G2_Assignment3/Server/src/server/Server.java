package server;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.NewSubscriberInfo;
import communication.StatusUpdate;
import communication.TableSizeUpdate;
import communication.TableStatusUpdate;
import db.ConnectionToDB;
import logic.Subscriber;
import ocsf.server.*;
/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 */
public class Server extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  private ConnectionToDB db;
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the server.
   * @param port The port number to connect on.
   */
  public Server(int port) 
  {
    super(port);
  }
  
  public static void ServerScreen(String password) {
	  ConnectionToDB.setPassword(password);
  }
  //Handle Messages and parsing methods(override) ************************************************
  /**
   * This method handles any messages received from the client.
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
 * @return 
   */
	public void handleMessageFromClient(Object msg, ConnectionToClient client){
		BistroRequest request = (BistroRequest)msg; // try catch for casting
		Object dbReturnedValue = null;
		Object data = request.getData();
		BistroResponse response;
		switch (request.getCommand()) {
			case GET_ACTIVE_RESERVATIONS_BY_PHONE:
				if (!(data instanceof String)) { response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad phone number."); break; }
				dbReturnedValue = db.searchOrdersByPhoneNumberList((String)data);// try catch for casting
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				break;
			case GET_RESERVATION_BY_ORDER_NUMBER:
				int orderNumber = handleStringRequest(data);
				if (orderNumber == -1) { response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad order number."); break; }
				dbReturnedValue = db.searchOrderByOrderNumber(orderNumber);// try catch for casting
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				break;
			case CANCEL_RESERVATION:
            dbReturnedValue = db.deleteOrderByOrderNumber((int) data);
            response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
            break;
			case CHANGE_STATUS:
				if (data instanceof StatusUpdate) {
					dbReturnedValue = db.changeOrderStatus(((StatusUpdate) data).getOrderNumber(),
							((StatusUpdate) data).getStatus());
					response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				} else
					response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
				break;
			case GET_TABLES:
				dbReturnedValue = db.loadTables();
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				break;
			case CHANGE_TABLE_STATUS:
				if (data instanceof TableStatusUpdate) {
					dbReturnedValue = db.changeTableStatus(((TableStatusUpdate) data).getTableNumber(),
							((TableStatusUpdate) data).getStatus());
					response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				} else
					response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
				break;
			case CHANGE_TABLE_SIZE:
				if (data instanceof TableSizeUpdate) {
					dbReturnedValue = db.changeTableSize(((TableSizeUpdate) data).getTable_number(),
							((TableSizeUpdate) data).getTable_size());
					response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				} else
					response = new BistroResponse(BistroResponseStatus.FAILURE, "update failed.");
				break;
			case SUBSCRIBER_LOGIN:
				if (data instanceof Subscriber){
					Subscriber s = (Subscriber) data;
					//s.setPasswordHash(BCrypt.hashpw(s.getPasswordHash(), BCrypt.gensalt()));
					if(db.subscriberLogin(s.getUsername(), s.getPasswordHash()))
						// TODO: on success pull the order history of the subscriber
						response = new BistroResponse(BistroResponseStatus.SUCCESS, null);
					else
						response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				}
				else
					response = new BistroResponse(BistroResponseStatus.FAILURE, null);
				break;
			case ADD_SUBSCRIBER:
			if (data instanceof NewSubscriberInfo) {
				NewSubscriberInfo s = (NewSubscriberInfo) data;
				Subscriber subscriber = s.getSubscriber();
				String rawPassword = s.getRawPassword();
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

			default:
				response = new BistroResponse(BistroResponseStatus.INVALID_REQUEST, null);
				break;
			}
			try {
				client.sendToClient(response);
			} catch (IOException e) {
				System.out.println("Error: Can't send message to client.");
			}
	}
	
	//Server methods ************************************************
	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server starts listening for connections.
	*/
	protected void serverStarted()
	{
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
	 * This method overrides the one in the superclass.  Called
	 * when the server stops listening for connections.
	*/
	protected void serverStopped()
	{
		System.out.println
		("Server has stopped listening for connections.");
	}

	//Client methods ************************************************
	/**
	 * Records the remote host information when a new client connects and writes a
	 * connection log entry. The formatted host name/IP is stored via
	 * use setInfo(String, Object) so it can be retrieved to later even after the socket closes.
	 * @param client active connection that was just established
	*/
	@Override
	protected void clientConnected(ConnectionToClient client) {
		InetAddress addr = client.getInetAddress();
		if (addr != null) {
			client.setInfo("remoteAddress",
            addr.getHostName() + " (" + addr.getHostAddress() + ")");
    	}
    	log(client.getInfo("remoteAddress").toString() + " connected");
	}
	
	/**
	 * Logs a disconnection event for a client that terminated with an exception.
	 * The client socket has already been closed, so the previously stored
	 * "remoteAddress" info is used to identify which client disconnected.
	 * @param client connection that raised the exception
	 * @param exception cause of the disconnection
	*/
	@Override
	synchronized protected void clientException(ConnectionToClient client, Throwable exception){
		Object stored = client.getInfo("remoteAddress");
    	String id = stored != null ? stored.toString() : "Unknown client";
	    log(id + " disconnected");
	}

	//Instance methods ************************************************
	/**
	 * Method that take data Object and check if it a string, if not then throw exception. if everything alright then return the number in int.
	 * @param data
	 * @return
	*/
	private int handleStringRequest(Object data){
		int numberReturned = -1;
		try {
			if (!(data instanceof String)) {
				throw new IllegalArgumentException("Expected String, got: " +
				(data == null ? "null" : data.getClass().getName()));
			}
			numberReturned = Integer.parseInt((String)data);
			
		} catch (NumberFormatException e) {
			log(e.getMessage());
		} catch (IllegalArgumentException e) {
			log("Error: handleStringRequest: couldn't parse string to int.");
		}
		return numberReturned;
	}
	/**
	 * Writes the provided message to the GUI log if available, otherwise stdout.
	 * @param msg the text to append to the log.
	*/
	private void log(String msg) {
		if (ServerScreen.instance != null)
			ServerScreen.instance.appendLog(msg);
		else
			System.out.println(msg);	        	
	}
}
