package server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Simple JavaFX screen that boots the server and shows a rolling log.
 */
public class ServerScreen  {
  protected static final String fxmlPath = "ServerScreen.fxml";
  public static ServerScreen instance;
  private Server server;
  private String serverPort = "5555";

  @FXML
  private TextArea logArea;

  @FXML
  private Label ipLabel;

  @FXML
  private Label dbPasswordLabel;

  /**
   * Default constructor is used by the FXMLLoader.
   */
  public ServerScreen() {
      // FXMLLoader will initialize fields before calling initialize().
  }

  @FXML
  private void initialize() {
      instance = this; // server will use this
      startServer(serverPort);
  }

  /**
   * Appends the supplied message to the UI log on the FX thread.
   * @param msg text that should appear in the log view.
   */
  public void appendLog(String msg) {
      Platform.runLater(() -> logArea.appendText(msg + "\n"));
  }
  /**
   * Print the server ip and DB password to the Server UI on the FX thread.
   * @param ip the server got on startup.
   * * @param dbPassword that is known form the beginning.
   */
  public void updateServerInfo(String ip, String dbPassword) {
      Platform.runLater(() -> {
          ipLabel.setText("Server IP: " + (ip == null ? "Unavailable" : ip));
          dbPasswordLabel.setText("DB Password: " + (dbPassword == null ? "Unavailable" : dbPassword));
      });
  }
  
  /**
   * This method is responsible for the creation of 
   * the server instance (The UI already exist in this phase).
   * @param p the port string to parse, defaulting to 5555 if invalid.
   */
  
  private void startServer(String p) {
    int port = 0; //Port to listen on
  
    try
    {
      port = Integer.parseInt(p); //Get port from command line
    }
    catch(Throwable t)
    {
      port = Server.DEFAULT_PORT; //Set port to 5555
    }
  
    server = new Server(port);
    
    try 
    {
      server.listen(); //Start listening for connections
    } 
    catch (Exception e) 
    {
      System.out.println("ERROR - Could not listen for clients!");
      appendLog("Server error: " + e.getMessage());
    }
  }
  public void stopServer() {
      if (server != null) {
          try { server.close(); } catch (Exception ignored) {}
      }
  }

}
