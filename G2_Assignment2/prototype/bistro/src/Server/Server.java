package Server;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

import ocsf.server.*;
import common.ConnectionToDB;
import Logic.Reservation;
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

  //Instance methods ************************************************
  /**
   * This method handles any messages received from the client.
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
	public void handleMessageFromClient(Object msg, ConnectionToClient client){
		Object result = null;
		  //if the query is for adding new order to the DB.
		  if(msg instanceof Reservation) {
			  db.insertNewOrder((Reservation)msg);
			  log(client + ": Inserted new order.");
		  }
		  //if the query is for update an existing order, fields to update are: order_date, number_of_guests. search the order by order_number.
		  else if(msg instanceof ArrayList<?>){
			  ArrayList<String> arrayListMsg = new ArrayList<>();
			    for (Object o : (ArrayList <?>)msg) {
			    	arrayListMsg.add((String) o);   // safe if you know they are strings
			    }
			  if(arrayListMsg.get(0).toLowerCase().equals("search")) {
				  result = db.searchOrder(Integer.valueOf(arrayListMsg.get(1)));
				  log(client + ": Asked for order number: " + arrayListMsg.get(1) + " .");
			  }
			  else if(arrayListMsg.get(0).toLowerCase().equals("update")){				  
				  db.updateOrder(Integer.parseInt(arrayListMsg.get(1)), LocalDate.parse(arrayListMsg.get(2)), Integer.parseInt(arrayListMsg.get(3)));
				  log(client + ": Updated order number: " + arrayListMsg.get(1) + ".");
			  }
			  
		  }
		  // if none of the above then the server doesn't know how to handle such request.
		  else {
				result = client + (": Request query doesn't exist.");
		  }
		  // if it log, then return the log of the server, if not then return the respond.
		  try {
			  client.sendToClient(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
	  protected void serverStarted()
	  {
	    System.out.println
	      ("Server listening for connections on port " + getPort());
	    log("Server listening for connections on port " + getPort());
	    db = new ConnectionToDB();
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
	 * Logs when a client establishes a new connection with the server.
	 * @param client the client that connected.
	 */
	protected void clientConnected(ConnectionToClient client) {
		log(client.toString() + " connected");
	}
	
	  /**
	   * Logs when a client disconnects to keep the session history consistent.
	   * @param client the client that disconnected.
	   */
	  synchronized protected void clientDisconnected(
		    ConnectionToClient client) {
		  log(client.toString() + " disconnected");
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
