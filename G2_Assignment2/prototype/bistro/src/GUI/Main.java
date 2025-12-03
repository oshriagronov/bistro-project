package GUI;
import client.ClientController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	public static ClientController client;
	private static Stage primaryStage;
	public void start(@SuppressWarnings("exports") Stage primaryStage) {
		Parent root;
		this.primaryStage = primaryStage;
		client=ClientController.getInstance("localhost", 5555);
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("menu.fxml"));
			root =  loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Scene s = new Scene(root);
		primaryStage.setScene(s);
		primaryStage.setTitle("New Order"); 
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public static void changeRoot(String fxmlName) throws IOException {
	    Parent newRoot = FXMLLoader.load(Main.class.getResource("/GUI/" + fxmlName));
	    primaryStage.getScene().setRoot(newRoot);
	}
}


