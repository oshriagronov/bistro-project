package gui;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import logic.LoggedUser;
import logic.Reservation;
import logic.Subscriber;
import logic.UserType;
import logic.Worker;

public class WaitingListScreen {
	public static final String fxmlPath = "/gui/WaitingList.fxml";
	private Subscriber sub=null;
	private Worker worker=null;
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

     @FXML
    private Button backBtn;

    @FXML
    private ComboBox<String> diners;

    @FXML
    private TextField emailField;

    @FXML
    private VBox nonSubVbox;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> prePhone;

    @FXML
    private CheckBox subCheckBox;

    @FXML
    private Button submit;

	@FXML
    private VBox workerVbox;

    public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

    @FXML
    void initialize() {
		if (LoggedUser.getType()==UserType.SUBSCRIBER) {
        	this.sub = ScreenSetup.setupSubscriber(nonSubVbox, workerVbox, null);
        }
        else if (LoggedUser.getType()==UserType.EMPLOYEE || LoggedUser.getType()==UserType.MANAGER) {
        	this.worker = ScreenSetup.setupWorkerView(nonSubVbox, workerVbox, null);
        }
        else {
            ScreenSetup.setupGuestView(nonSubVbox, workerVbox, null);
        }
		
        diners.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			diners.getItems().add(String.valueOf(i));
		}
        prePhone.getItems().clear();
        prePhone.getItems().addAll("050", "052", "053", "054", "055", "058");
    }

	@FXML
	void submit(ActionEvent event) {
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();
		
		if (diners.getValue() == null) {
			showAlert("Error", "Please choose the diners amount");
			return;
		}
		int num_of_diners = Integer.parseInt(diners.getValue());
		int con_code = new java.util.Random().nextInt(90000) + 10000; // TODO: just for now

		if (sub != null){
			Reservation r = new Reservation(today, num_of_diners, con_code, sub.getSubscriberId(), today, now, sub.getPhone(), sub.getEmail()) ;
			Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
			showAlert("Reservation Success", "Reservation successfully created.");
			return;
		}
		
		if (worker != null && subCheckBox.isSelected()){
			String phone = "";
			if (prePhone.getValue() != null) phone += prePhone.getValue();
			if (phoneField.getText() != null) phone += phoneField.getText();
			String email = emailField.getText();
			
			Subscriber foundSub = null;
			
			if (phone.length() ==  10) {
				Main.client.accept(new BistroRequest(BistroCommand.SEARCH_SUB_BY_PHONE, phone));
				BistroResponse response = Main.client.getResponse();
				if (response.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) response.getData();
				}
			} else if (email != null && !email.isEmpty()) {
				Main.client.accept(new BistroRequest(BistroCommand.SEARCH_SUB_BY_EMAIL, email));
				BistroResponse response = Main.client.getResponse();
				if (response.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) response.getData();
				}
			} else {
				showAlert("Error", "Please enter phone or email to place the order.");
				return;
			}
			
			if (foundSub != null) {
				Reservation r = new Reservation(today, num_of_diners, con_code, foundSub.getSubscriberId(), today, now, foundSub.getPhone(), foundSub.getEmail());
				Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
				showAlert("Reservation Success", "Reservation successfully created for " + foundSub.getFirstName());
			} else {
				showAlert("Error", "Subscriber not found. Please check details.");
			}
			return;
		}
		
		String phone_number = "";
		if (prePhone.getValue() != null) phone_number += prePhone.getValue();
		if (phoneField.getText() != null) phone_number += phoneField.getText();
		String email = emailField.getText();
		int nonSub = 0;

		if (phone_number.length() > 0 && phone_number.length() < 10) {
			showAlert("Input Error", "Please enter a valid 10-digit phone number.");
		} else if (phone_number.isEmpty() && (email == null || email.trim().isEmpty())) {
			showAlert("Input Error", "Please enter identifying information (Phone or Email).");
		} else {
			Reservation r = new Reservation(today, num_of_diners, con_code, nonSub, today, now, phone_number, email);
			Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
			BistroResponse response = Main.client.getResponse();
			if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
				showAlert("Reservation Success", "Reservation successfully created.");
			} else {
				showAlert("Error", "Failed to create reservation.");
			}
		}
    }

	/**
	 * Handles the action when the "Back to MainMenu" button is clicked.
	 * Navigates the application back to the main menu screen.
	 * * @param event The ActionEvent triggered by the Back button.
	 */
	@FXML
	void back(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot(MainMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
