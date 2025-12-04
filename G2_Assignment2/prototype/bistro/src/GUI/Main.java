/**
 * The main entry point for the JavaFX GUI application.
 * This class handles the initialization of the primary stage, 
 * the loading of the main menu FXML, and the initialization of the client controller.
 */
package GUI;

import client.ClientController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	
	/**
	 * Static reference to the client controller used for communication with the server.
	 */
	public static ClientController client;
	
	/**
	 * Static reference to the primary Stage of the application.
	 */
	private static Stage primaryStage;
	
	/**
	 * The main entry method for all JavaFX applications. 
	 * This method initializes the client connection, loads the MainMenu FXML, 
	 * and sets up the primary stage.
	 * * @param primaryStage The primary stage for this application, onto which 
	 * the application scene can be set.
	 */
	public void start(@SuppressWarnings("exports") Stage primaryStage) {
		Parent root;
		this.primaryStage = primaryStage;
		// Initialize the client connection to localhost on port 5555
		client = ClientController.getInstance("localhost", 5555);
		try {
			// Load the main menu FXML file
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("MainMenu.fxml"));
			root = loader.load();
		} catch (IOException e) {
			// Print stack trace if FXML loading fails and exit
			e.printStackTrace();
			return;
		}
		
		// Set up the scene and display the stage
		Scene s = new Scene(root);
		primaryStage.setScene(s);
		primaryStage.setTitle("New Order"); 
		primaryStage.show();
	}

	/**
	 * The main method required for all Java programs.
	 * Calls the JavaFX launch method.
	 * * @param args Command line arguments (not used in this application).
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * Dynamically changes the root node of the current scene to switch views/screens 
	 * within the application.
	 * * @param fxmlName The name of the FXML file (e.g., "Login.fxml") to load as the new root.
	 * @throws IOException If the FXML file cannot be found or loaded.
	 */
	public static void changeRoot(String fxmlName) throws IOException {
	    Parent newRoot = FXMLLoader.load(Main.class.getResource("/GUI/" + fxmlName));
	    primaryStage.getScene().setRoot(newRoot);
	}
}