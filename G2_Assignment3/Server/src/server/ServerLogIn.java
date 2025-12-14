package server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
			ServerScreen screen = new ServerScreen();

			// Get current stage from the button
			var stage = (javafx.stage.Stage) enterBTN.getScene().getWindow();

			// Replace the scene root
			var scene = stage.getScene();
			if (scene == null) {
				stage.setScene(new javafx.scene.Scene(screen.createContent(), 450, 350));
			} else {
				scene.setRoot(screen.createContent());
			}

			// Optional: handle close to stop server
			stage.setOnCloseRequest(e -> screen.stopServer());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
