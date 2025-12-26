package subscriber;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponseStatus;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Controller for the subscriber login view.
 * Handles basic input validation and navigation to the account screen.
 */
public class SubscriberLoginScreen {

	/** Alert used to show validation messages to the user. */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	/** Text field for the subscriber username. */
	private TextField usernameField;

	@FXML
	/** Text field for the subscriber code. */
	private TextField codeField;

	@FXML
	/** Button that triggers the login flow. */
	private Button loginBtn;

	@FXML
	/** Button that returns the user to the main menu. */
	private Button menuBtn;

	/**
	 * Shows a simple modal alert.
	 * @param title alert title
	 * @param body message to display
	 */
	private void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	/**
	 * Validates inputs and moves to the subscriber screen on success.
	 * @param event JavaFX action event
	 */
	void handleLogin(ActionEvent event) {
		String username = usernameField.getText();
		String code = codeField.getText();
		StringBuilder errors = new StringBuilder();
		boolean ok = true;

		if (username == null || username.isBlank()) {
			ok = false;
			errors.append("Please enter username\n");
		}
		if (code == null || code.isBlank()) {
			ok = false;
			errors.append("Please enter subscriber code\n");
		} else if (!code.matches("\\d+")) {
			ok = false;
			errors.append("Subscriber code must contain only digits\n");
		}
		if(!ok){
			Main.client.accept(new BistroRequest(BistroCommand.SUBSCRIBER_LOGIN, new String[] { username, code }));
			if (Main.client.getResponse().getStatus() == BistroResponseStatus.SUCCESS) {
				try {
					Main.changeRoot("SubscriberScreen.fxml");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				Object data = Main.client.getResponse().getData();
				if(data instanceof String)
					errors.append((String) data);
				ok = false;
			}
		}
		if (!ok)
			showAlert("Login Failure", errors.toString());
	}

	@FXML
	/**
	 * Returns the user to the main menu.
	 * @param event JavaFX action event
	 */
	void backToMenu(ActionEvent event) {
		try {
			Main.changeRoot("MainMenu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
