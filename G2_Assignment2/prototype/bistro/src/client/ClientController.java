package client;
import java.io.*;
import common.ChatIF;
/**
 * This class constructs the UI for a chat client.  It implements the
 * chat interface in order to activate the display() method.
 * Warning: Some of the code here is cloned in ServerConsole 
 */
public class ClientController implements ChatIF 
{
  //Class variables *************************************************
  
  /**
   * The default port to connect on.
   */
   public static int DEFAULT_PORT ;
   private static ClientController clientController;
  //Instance variables **********************************************

  
  /**
   * The instance of the client that created this ConsoleChat.
   */
  BistroClient client;
  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the ClientConsole UI.
   *
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  private ClientController(String host, int port) 
  {
    try 
    {
      client= new BistroClient(host, port, this);
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
   * This method waits for input from the console.  Once it is 
   * received, it sends it to the client's message handler.
   */
  public void accept(Object o) 
  {
	  client.handleMessageFromClientUI(o);
  }
  public void quit() {
	  client.quit();
  }
  
  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message) 
  {
    System.out.println("> " + message);
  }
}
//End of ConsoleChat class
