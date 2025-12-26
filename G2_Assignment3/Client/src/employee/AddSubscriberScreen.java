package employee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AddSubscriberScreen {

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

	Alert alert = new Alert(Alert.AlertType.INFORMATION);

	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	void initialize() {
		PhoneStartCombo.getItems().clear();
		PhoneStartCombo.getItems().addAll("050", "052", "053", "054", "055", "058");

		PhoneStartCombo.getSelectionModel().selectFirst();
	}

	@FXML
	void backToMenu(ActionEvent event) {

	}

	@FXML
	void clear(ActionEvent event) {
		firstNameTxt.clear();
		lastNameTxt.clear();
		usernameTxt.clear();
		passwordTxt.clear();
		phoneTxt.clear();
		emailTxt.clear();
	}

	@FXML
	void create(ActionEvent event) {
		StringBuilder error = new StringBuilder();
		boolean check = true;
		if (!isInputValid(firstNameTxt.getText(), lastNameTxt.getText(), usernameTxt.getText(), passwordTxt.getText(),
				phoneTxt.getText(), emailTxt.getText())) {
			error.append("Please fill all fields");
			check = false;
		} else {
			if (!firstNameTxt.getText().matches("^[A-Za-z]+$")) {
				error.append("First name can only contain letters\n");
				check = false;
			}
			if (!lastNameTxt.getText().matches("^[A-Za-z]+$")) {
				error.append("Last name can only contain letters\n");
				check = false;
			}
			if (!phoneTxt.getText().matches("^[0-9]+$")) {
				error.append("Phone number can only contain numbers\n");
				check = false;
			}
			if (phoneTxt.getText().length() != 7) {
				error.append("Phone number should be 7 digits\n");
				check = false;
			}
			if (!emailTxt.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				error.append("Please enter a valid Email\n");
				check = false;
			}

		}

		if (!check) {
			showAlert("Form error", error.toString());
		} else {
			showAlert("Success", "Subscriber added successfuly");
			// create a subscriber object
			// add to DB

		}
	}

	boolean isInputValid(String firstName, String lastName, String username, String password, String phone,
			String email) {
		if (firstName == null || firstName.isBlank())
			return false;
		if (lastName == null || lastName.isBlank())
			return false;
		if (username == null || username.isBlank())
			return false;
		if (password == null || password.isBlank())
			return false;
		if (phone == null || phone.isBlank())
			return false;
		if (email == null || email.isBlank())
			return false;
		return true;
	}

}
