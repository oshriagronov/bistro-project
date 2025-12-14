package client;
import ocsf.client.*;
import java.io.*;

import common.BistroController;
import communication.BistroResponse;
/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class BistroClient extends AbstractClient
{
  //Instance variables **********************************************
  private static BistroClient bistroClient;
	// a boolean variable to time the wait for the server response.
  private static boolean awaitResponse = false;
  
  private BistroController controller;
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the Bistro client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   */
	 
  public BistroClient(String host, int port, BistroController controller) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.controller = controller;
    openConnection();//in order to send more than one message
  }

  // Singleton design pattern
  public static BistroClient getInstance(String host, int port, BistroController controller) throws IOException {
	  if (bistroClient == null){
		  bistroClient = new BistroClient(host, port, controller);
	  }
	  return bistroClient;
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
    controller.serverResponse((BistroResponse)msg);
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
      awaitResponse = true;
    	sendToServer(message);
		  // wait for response
		  while (awaitResponse) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          System.out.println("Error at handleMessageFromClientUI: Can't sleep!\n");
          quit();
        }
		  }
    }
    catch(IOException e)
    {
    	e.printStackTrace();
      System.out.println("Error at handleMessageFromClientUI: Can't send message to server!");
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
    catch(IOException e) {
      System.out.println("Error at quit method: Can't close connection!");
    }
    finally{
      bistroClient = null;
    }
    System.exit(0);
  }
}
//End of ChatClient class
