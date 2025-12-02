package GUI;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	public void start(@SuppressWarnings("exports") Stage primaryStage) {
		Parent root;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("midPro.fxml"));
			root =  loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Scene s = new Scene(root);
		primaryStage.setScene(s);
		primaryStage.show();
		primaryStage.setTitle("New Order"); 
	}

	public static void main(String[] args) {
		launch(args);
	}
}


