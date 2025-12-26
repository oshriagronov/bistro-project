package subscriber;

import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the subscriber account screen.
 * Shows visit history and allows editing basic personal info.
 */
public class SubscriberScreen {

	/** Alert used to show validation messages to the user. */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	/** List of visit history entries (placeholder data for now). */
	private ListView<String> historyList;

	@FXML
	/** Input for updated phone number. */
	private TextField phoneField;

	@FXML
	/** Input for updated email address. */
	private TextField emailField;

	@FXML
	/** Container for the update form. */
	private VBox updateForm;

	@FXML
	/** Button that reveals the update form. */
	private Button revealUpdateBtn;

	@FXML
	/** Button that submits the update form. */
	private Button submitBtn;

	@FXML
	/** Button that returns the user to the main menu. */
	private Button menuBtn;

	@FXML
	/**
	 * Initializes the view with sample history data and hides the update form.
	 */
	void initialize() {
		historyList.getItems().setAll(
				"Order date: 2024-05-12 | Guests: 4 | Confirmation: 12345 | Subscriber: 54321 | Placed: 2024-05-01",
				"Order date: 2024-06-03 | Guests: 2 | Confirmation: 67890 | Subscriber: 54321 | Placed: 2024-05-20",
				"Order date: 2024-06-18 | Guests: 6 | Confirmation: 24680 | Subscriber: 54321 | Placed: 2024-06-05");
		updateForm.setVisible(false);
	}

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
	 * Reveals the update form and hides the update button.
	 * @param event JavaFX action event
	 */
	void revealUpdateForm(ActionEvent event) {
		updateForm.setVisible(true);
		revealUpdateBtn.setVisible(false);
	}

	@FXML
	/**
	 * Validates inputs, shows a success/failure alert, and hides the form on success.
	 * @param event JavaFX action event
	 */
	void handleSubmit(ActionEvent event) {
		String phone = phoneField.getText();
		String email = emailField.getText();
		StringBuilder errors = new StringBuilder();
		boolean ok = true;

		if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
			ok = false;
			errors.append("Please enter phone number or email\n");
		}

		if (phone != null && !phone.isBlank() && (!phone.matches("05\\d{8}"))) {
			ok = false;
			errors.append("Phone number must be 10 digits and start with 05\n");
		}

		if (email != null && !email.isBlank()
				&& !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			ok = false;
			errors.append("Please enter a valid Email\n");
		}

		if (!ok) {
			showAlert("Update Failure", errors.toString());
			return;
		}

		showAlert("Update Success", "Personal info updated.");
		updateForm.setVisible(false);
		revealUpdateBtn.setVisible(true);
		phoneField.clear();
		emailField.clear();
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
