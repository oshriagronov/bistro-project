package employee;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.NewSubscriberInfo;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import logic.Subscriber;

/**
 * Controller for the "Add Subscriber" screen.
 * <p>
 * Handles user input validation, creation of a new {@link Subscriber},
 * sending the creation request to the server, and handling the server response.
 */
public class AddSubscriberScreen {
	public static final String fxmlPath ="/employee/AddSubscriber.fxml";
	@FXML
	private Button MenuBtn;

	@FXML
	private Button clearBtn;

	@FXML
	private Button createBtn;

	@FXML
	private ComboBox<String> PhoneStartCombo;

	@FXML
	private TextField emailTxt;

	@FXML
	private TextField firstNameTxt;

	@FXML
	private TextField lastNameTxt;

	@FXML
	private PasswordField passwordTxt;

	@FXML
	private TextField phoneTxt;

	@FXML
	private Label statusLabel;

	@FXML
	private TextField usernameTxt;
    
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	/**
	 * Displays an alert dialog with the given title and message.
	 *
	 * @param title the alert window title
	 * @param body  the message to display
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Initializes the screen after the FXML is loaded.
	 * <p>
	 * Populates the phone prefix combo box and selects a default value.
	 */
	@FXML
	void initialize() {
		PhoneStartCombo.getItems().clear();
		PhoneStartCombo.getItems().addAll("050", "052", "053", "054", "055", "058");
		PhoneStartCombo.getSelectionModel().selectFirst();
	}

	/**
	 * Navigates back to the employee menu screen.
	 *
	 * @param event the button click event
	 */
	@FXML
	void backToMenu(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears all input fields in the form.
	 *
	 * @param event the button click event
	 */
	@FXML
	void clear(ActionEvent event) {
		firstNameTxt.clear();
		lastNameTxt.clear();
		usernameTxt.clear();
		passwordTxt.clear();
		phoneTxt.clear();
		emailTxt.clear();
	}

	/**
	 * Handles creation of a new subscriber.
	 * <p>
	 * Validates user input, constructs a {@link Subscriber} object,
	 * sends a creation request to the server, and displays the server response.
	 *
	 * @param event the button click event
	 */
	@FXML
	void create(ActionEvent event) {
		StringBuilder error = new StringBuilder();
		boolean valid = true;

		if (!isInputValid(firstNameTxt.getText(), lastNameTxt.getText(),
				usernameTxt.getText(), passwordTxt.getText(),
				phoneTxt.getText(), emailTxt.getText())) {
			error.append("Please fill all fields");
			valid = false;
		} else {
			if (!firstNameTxt.getText().matches("^[A-Za-z]+$")) {
				error.append("First name can only contain letters\n");
				valid = false;
			}
			if (!lastNameTxt.getText().matches("^[A-Za-z]+$")) {
				error.append("Last name can only contain letters\n");
				valid = false;
			}
			if (!phoneTxt.getText().matches("^[0-9]+$")) {
				error.append("Phone number can only contain numbers\n");
				valid = false;
			}
			if (phoneTxt.getText().length() != 7) {
				error.append("Phone number should be 7 digits\n");
				valid = false;
			}
			if (!emailTxt.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				error.append("Please enter a valid Email\n");
				valid = false;
			}
		}

		if (!valid) {
			showAlert("Form error", error.toString());
			return;
		}

		Subscriber subscriber = new Subscriber(
				usernameTxt.getText(),
				firstNameTxt.getText(),
				lastNameTxt.getText(),
				emailTxt.getText(),
				PhoneStartCombo.getValue() + phoneTxt.getText()
		);

		String password = passwordTxt.getText();
		NewSubscriberInfo data = new NewSubscriberInfo(subscriber, password);

		Main.client.accept(new BistroRequest(BistroCommand.ADD_SUBSCRIBER, data));
		BistroResponse response = Main.client.getResponse();

		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			showAlert("Success", "Subscriber added successfully");
			clear(null);
		} else {
			showAlert("Error", "Failed to add subscriber");
		}
	}

	/**
	 * Checks whether all required input fields contain valid, non-empty values.
	 *
	 * @param firstName the first name
	 * @param lastName  the last name
	 * @param username  the username
	 * @param password  the password
	 * @param phone     the phone number (without prefix)
	 * @param email     the email address
	 * @return {@code true} if all fields are valid, {@code false} otherwise
	 */
	boolean isInputValid(String firstName, String lastName,
	                     String username, String password,
	                     String phone, String email) {

		if (firstName == null || firstName.isBlank()) return false;
		if (lastName == null || lastName.isBlank()) return false;
		if (username == null || username.isBlank()) return false;
		if (password == null || password.isBlank()) return false;
		if (phone == null || phone.isBlank()) return false;
		if (email == null || email.isBlank()) return false;

		return true;
	}
}
