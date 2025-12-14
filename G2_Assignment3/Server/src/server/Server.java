package server;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
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

  //SuperClass methods(override) ************************************************
  /**
   * This method handles any messages received from the client.
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
	public void handleMessageFromClient(Object msg, ConnectionToClient client){
		BistroRequest request = (BistroRequest)msg; // try catch for casting
		Object dbReturnedValue = null;
		Object data = request.getData();
		BistroResponse response;
		switch (request.getCommand()) {
			case GET_ACTIVE_RESERVATIONS_BY_PHONE:
				int phoneNumber = handleStringRequest(data);
				if (phoneNumber == -1) { response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad phone number."); break; }
				dbReturnedValue = db.searchOrdersByPhoneNumberList(phoneNumber);// try catch for casting
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
				break;
			case GET_RESERVATION_BY_ORDER_NUMBER:
				int orderNumber = handleStringRequest(data);
				if (orderNumber == -1) { response = new BistroResponse(BistroResponseStatus.FAILURE, "Bad order number."); break; }
				dbReturnedValue = db.searchOrderByOrderNumber(orderNumber);// try catch for casting
				response = new BistroResponse(BistroResponseStatus.SUCCESS, dbReturnedValue);
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
    	db = ConnectionToDB.getConnInstance();
    	if (ServerScreen.instance != null) {
			ServerScreen.instance.updateServerInfo(ipString, db.getDbPassword());
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
			if (!(data instanceof String s)) {
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
