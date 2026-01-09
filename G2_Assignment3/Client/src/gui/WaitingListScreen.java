package gui;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
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
        else if (LoggedUser.getType()==UserType.EMPLOYEE) {
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
		StringBuilder str = new StringBuilder();
		boolean check = true;
		String phone_number,number_of_guests;
        int num=0;
		phone_number = prePhone.getValue() +phoneField.getText();
		number_of_guests = (String) diners.getValue();
		//Validate Diners Amount
		if (number_of_guests == null) {
			check = false;
			str.append("Please choose the diners amount\n");
		}
		// Final Check: Display errors or process update
		if (!check) {
			showAlert("Please enter number of diners", str.toString());
		} else {
			showAlert("Reservasion Success", "Reservation successfully created.");
			// Send the update command and new details to the server
			//TODO: fix the constructor
			//Reservation waitingList = new Reservation(Integer.parseInt(number_of_guests), num, phone_number);
			//Main.client.accept(waitingList);
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
