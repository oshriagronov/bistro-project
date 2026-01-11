package server;

import db.ConnectionToDB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ServerLogIn {

	@FXML
	private Button enterBTN;

	@FXML
	private TextField pwText;

	@FXML
	void clickEnter(ActionEvent event) {
        ConnectionToDB.setPassword(pwText.getText());
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerScreen.fxml"));
			Parent root = loader.load();
			ServerScreen screen = loader.getController();

			// Get current stage from the button
			var stage = (javafx.stage.Stage) enterBTN.getScene().getWindow();

			// Replace the scene root
			var scene = stage.getScene();
			if (scene == null) {
				stage.setScene(new Scene(root, 450, 350));
			} else {
				scene.setRoot(root);
			}

			// Optional: handle close to stop server
			stage.setOnCloseRequest(e -> screen.stopServer());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
