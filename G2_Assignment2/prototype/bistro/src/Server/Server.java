package Server;
import java.io.*;
import java.time.LocalDate;
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
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the server.
   *
   * @param port The port number to connect on.
   */
  public Server(int port) 
  {
    super(port);
  }

  //Instance methods ************************************************
  /**
   * This method handles any messages received from the client.
   * TODO: 
   * 1. random confirmation code or using Auto Increment in the database. 
   * 2. handle orders from non subscriber.
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
	public void handleMessageFromClient(Object msg, ConnectionToClient client){
		int result;
		String respond = "";
		String str_msg = (String)msg;
		String[] input = str_msg.split("_");
		  /*
		   * examples for how date should look like as string in the ArrayList<String>
		   * 27-11-2025
		   * 30-02-2025
		  */
		  //if the query is for adding new order to the DB.
		  if(input[0].equals("insert")) {
			  Reservation reservation = parsingDataIntoReservation(input);
			  ConnectionToDB db = new ConnectionToDB();
			  result = db.insertNewOrder(reservation);
			  respond = client + ": " + (result >= 1 ? "Order inserted succesfully." : "Order wasn't inserted due to error.");

		  }
		  //if the query is for update an existing order, fields to update are: order_date, number_of_guests. search the order by order_number.
		  else if(input[0].equals("update")){
			  String order_number = input[1];
			  String order_date = input[2];
			  String number_of_guests = input[3];
			  ConnectionToDB db = new ConnectionToDB();
			  result = db.updateOrder(Integer.parseInt(order_number), parseStringIntoDate(order_date), Integer.parseInt(number_of_guests));
			  respond = client + ": order: " +(order_number + (result >= 1 ? " was updated succesfully." : " wasn't updated due to error."));
		  }
		  // if none of the above then the server doesn't know how to handle such request.
		  else {
				respond = client + (": Request query doesn't exist.");
		  }
		  // if it log, then return the log of the server, if not then return the respond.
		  try {
			  client.sendToClient(respond);
			  log(respond);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	/**
	 * This method get data of the reservation and create an Reservation object and parsing the data in correct way for query.
	 * @param data
	 * @return Reservation object after parsing the data and ready to be inserted to the DB
	 */
	public Reservation parsingDataIntoReservation(String[] data) {
			LocalDate order_date = parseStringIntoDate(data[1]);
			LocalDate date_of_placing_order = parseStringIntoDate(data[5]);
			return new Reservation(
					order_date,
					Integer.parseInt(data[2]),
					Integer.parseInt(data[3]), 
					Integer.parseInt(data[4]),
					date_of_placing_order);
			
	}
	/**
	 * This method parse string of date to LocalDate type to easily insert order into the database.
	 * @param date - a string of date we get.
	 * @return the date the method got but returned as LocalDate type.
	 */
	  
	public LocalDate parseStringIntoDate(String date) {
		String [] string_arr_date = date.split("-");
		LocalDate result = LocalDate.of(
				Integer.parseInt(string_arr_date[2]), 
				Integer.parseInt(string_arr_date[1]),
				Integer.parseInt(string_arr_date[0]));
		return result;
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
	  }
  
	  /**
	   * This method overrides the one in the superclass.  Called
	   * when the server stops listening for connections.
	   */
	protected void serverStopped()
	  {
	    System.out.println
	      ("Server has stopped listening for connections.");
	    log("Server has stopped listening for connections.");
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
