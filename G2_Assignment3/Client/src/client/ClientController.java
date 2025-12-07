package client;
import java.io.*;
public class ClientController
{
  //Class variables *************************************************
  
  /**
   * The default port to connect on.
   */
	final public static int DEFAULT_PORT = 5555;
   // used to send messages to the server.
   private static ClientController clientController;
  //Instance variables **********************************************

  
  /**
   * The instance of the BistroClient.
   */
  protected BistroClient client;
  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the ClientController with singleton design pattern.
   *
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  private ClientController(String host, int port) 
  {
    try
    {
      client= new BistroClient(host, port);
    } 
    catch(IOException exception) 
    {
      System.out.println("Error: Can't setup connection!"+ " Terminating client.");
      System.exit(1);
    }
  }
  // Singleton design pattern
  public static ClientController getInstance(String host, int port) {
	  if (clientController == null){
		  clientController = new ClientController(host, port);
	  }
	  return clientController;
  }

  
  //Instance methods ************************************************

  /**
   * This method waits for input from the GUI. Once it is 
   * received, it sends it to the client's message handler.
   */
  public void accept(Object o) 
  {
	  client.handleMessageFromClientUI(o);
  }
  
  /*
   * This method quit close the connection of BistroClient, should be invoke when the GUI is closed.
   */
  public void quit() {
	  client.quit();
  }
  
}
