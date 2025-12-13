package server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;

/**
 * Simple JavaFX screen that boots the server and shows a rolling log.
 */
public class ServerScreen  {
    public static ServerScreen instance;
    private Server server;
    private TextArea logArea;
    private Label ipLabel;
    private Label dbPasswordLabel;
    private String serverPort = "5555";
    /**
     * Builds the UI and launches the server listening on the configured port.
     * @param primaryStage the hosting stage supplied by JavaFX.
     */
    public Parent createContent() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefSize(350, 200);
        grid.add(logArea, 0, 1);

        ipLabel = new Label("Server IP: pending...");
        dbPasswordLabel = new Label("DB Password: pending...");

        VBox infoBox = new VBox(10, ipLabel, dbPasswordLabel);
        grid.add(infoBox, 1, 1);

        // start server after UI exists
        startServer(serverPort);

        return grid;
    }
    
    /**
     * Default constructor keeps a static reference for the server callbacks.
     */
    public ServerScreen() {
        instance = this; // server will use this
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
