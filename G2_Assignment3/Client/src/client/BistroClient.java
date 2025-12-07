package client;
import ocsf.client.*;
import java.io.*;
import logic.Reservation;
/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class BistroClient extends AbstractClient
{
  //Instance variables **********************************************
	// a boolean variable to time the wait for the server response.
  private static boolean awaitResponse = false;
  //ArrayList which will hold the order details that was asked from the client, it will hold null if there wasn't a order with that number
  public static Reservation orderedReturned = null;

  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the Bistro client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   */
	 
  public BistroClient(String host, int port) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
  }

  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
	  awaitResponse = false;
	  if(msg instanceof Reservation) {
		  orderedReturned = (Reservation)msg;
	  }
	  else {
		  orderedReturned = null;
	  }
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  
  public void handleMessageFromClientUI(Object message)  
  {
    try
    {
    	openConnection();//in order to send more than one message
       	awaitResponse = true;
    	sendToServer(message);
		// wait for response
		while (awaitResponse) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
    catch(IOException e)
    {
    	e.printStackTrace();
    	quit();
    }
  }

  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
//End of ChatClient class
