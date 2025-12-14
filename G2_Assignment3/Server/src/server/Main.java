package server;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	/**
	 * Static reference to the primary Stage of the application.
	 */
	private static Stage primaryStage;

	/**
	 * The main entry method for all JavaFX applications. This method initializes
	 * the client connection, loads the serverLogIn FXML, and sets up the primary
	 * stage. * @param primaryStage The primary stage for this application, onto
	 * which the application scene can be set.
	 */
	public void start(@SuppressWarnings("exports") Stage primaryStage) {
		Parent root;
		Main.primaryStage = primaryStage;
		try {
			// Load the main menu FXML file
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("serverLogin.fxml"));
			root = loader.load();
		} catch (IOException e) {
			// Print stack trace if FXML loading fails and exit
			e.printStackTrace();
			return;
		}
		// Initialize the client connection with host name and default port.
		// Set up the scene and display the stage
		Scene s = new Scene(root);
		primaryStage.setScene(s);
		primaryStage.setOnCloseRequest(event -> {

			System.exit(0);
		});
		primaryStage.setTitle("Server");
		primaryStage.show();
	}

	/**
	 * The main method required for all Java programs. Calls the JavaFX launch
	 * method. * @param args Command line arguments (not used in this application).
	 */
	public static void main(String[] args) {
		launch(args);
	}

}