import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
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
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
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
		System.out.println(client); // print the client details.
		int result;	
		ArrayList<String> input = new ArrayList<>();
		//check if the msg is ArrayList and check if every value in there is string.
		if((msg instanceof ArrayList<?> list)) {
			  for(Object o: list) {
				  input.add((String) o);
			  }
		}
		// send a message to client if something went wrong with the data type
		else {
		  try {
			  client.sendToClient("Invalid input.");
			  return;
		  }catch(IOException e){
			  e.printStackTrace();
		  }
		}
		  /*
		   * examples for how date should look like as string in the ArrayList<String>
		   * 27-11-2025
		   * 30-02-2025
		  */
		  try {
			  //if the query is for adding new order to the DB.
			  if(input.get(0).equals("insert")) {
				  Reservation reservation = parsingDataIntoReservation(input);
				  ConnectionToDB db = new ConnectionToDB();
				  result = db.insertNewOrder(reservation);
				  client.sendToClient(result >= 1 ? "Order inserted succesfully." : "Order wasn't inserted due to error.");
			  }
			  //if the query is for update an existing order, fields to update are: order_date, number_of_guests. search the order by order_number.
			  else if(input.get(0).equals("update")){
				  String order_number = input.get(1);
				  String order_date = input.get(2);
				  String number_of_guests = input.get(3);
				  ConnectionToDB db = new ConnectionToDB();
				  result = db.updateOrder(Integer.parseInt(order_number), parseStringIntoDate(order_date), Integer.parseInt(number_of_guests));
				  client.sendToClient("order: " + order_number + (result >= 1 ? " was updated succesfully." : " wasn't updated due to error."));
			  }
			  // if none of the above then the server doesn't know how to handle such request.
			  else
					client.sendToClient("Request query doesn't exist.");
		  }catch (IOException e) {
			  e.printStackTrace();
		  }
	}
	/**
	 * This method get data of the reservation and create an Reservation object and parsing the data in correct way for query.
	 * @param data
	 * @return Reservation object after parsing the data and ready to be inserted to the DB
	 */
	public Reservation parsingDataIntoReservation(ArrayList<String> data) {
			LocalDate order_date = parseStringIntoDate(data.get(1));
			LocalDate date_of_placing_order = parseStringIntoDate(data.get(5));
			return new Reservation(
					order_date,
					Integer.parseInt(data.get(2)),
					Integer.parseInt(data.get(3)), 
					Integer.parseInt(data.get(4)),
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
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
	public static void main(String[] args) 
	  {
	    int port = 0; //Port to listen on
	
	    try
	    {
	      port = Integer.parseInt(args[0]); //Get port from command line
	    }
	    catch(Throwable t)
	    {
	      port = DEFAULT_PORT; //Set port to 5555
	    }
		
	    Server sv = new Server(port);
	    
	    try 
	    {
	      sv.listen(); //Start listening for connections
	    } 
	    catch (Exception ex) 
	    {
	      System.out.println("ERROR - Could not listen for clients!");
	    }
	  }
	}
