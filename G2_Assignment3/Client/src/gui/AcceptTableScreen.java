package gui;

import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Controller class for the AcceptTableScreen.fxml view.
 * This class handles the logic for customers to confirm their arrival
 * by entering their phone number and confirmation code to receive their table number.
 */
public class AcceptTableScreen {
    public static final String fxmlPath = "/gui/AcceptTable.fxml";
    /** Alert object used to display success or failure messages to the user. */
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    /** Text field for entering the confirmation code. */
    @FXML
    private Button backBtn;

    @FXML
    private TextField confirmationCode;

    @FXML
    private HBox confirmationBox;

    @FXML
    private Button forgotConfirmationBtn;

    @FXML
    private VBox infoVbox;

    @FXML
    private ComboBox<String> prePhone;

    @FXML
    private TextField restPhone;

    @FXML
    private Button submitBTN;

    @FXML
    private Text tableResultText;

    private boolean usePhoneAsConfirmation = false;

    private void resetToDefaultView() {
        infoVbox.setVisible(true);
        tableResultText.setVisible(false);
        confirmationBox.setVisible(true);
        confirmationBox.setManaged(true);
        forgotConfirmationBtn.setDisable(false);
        usePhoneAsConfirmation = false;
    }

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    void initialize() {
        // Initialize phone prefix options
		prePhone.getItems().clear();
		prePhone.getItems().addAll("050", "052", "053", "054", "055", "058");

    }

    /**
     * Displays an information alert to the user.
     * @param title The title of the alert window.
     * @param body The content text of the alert.
     */
    public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}


    /**
     * Handles the submit button action.
     * Validates the phone number, sends the phone number and confirmation code to the server,
     * and displays the assigned table number if successful.
     * @param event The ActionEvent triggered by the submit button.
     */
    
    @FXML
    void handleSubmit(ActionEvent event){
        StringBuilder str = new StringBuilder();
        boolean check = true;
        ArrayList <String> search = new ArrayList<>();
        String pre_phone = prePhone.getValue();
        String rest_phone = restPhone.getText();
        boolean phoneValid = true;

        if (pre_phone == null || pre_phone.isBlank()) {
            str.append("Please select a phone prefix\n");
            check = false;
            phoneValid = false;
        }

		if (rest_phone == null || rest_phone.length() != 7 || !rest_phone.matches("\\d+")) {
			str.append("Please enter a valid phone number\n");
			check = false;
            phoneValid = false;
		}
        String code = usePhoneAsConfirmation ? (phoneValid ? rest_phone : "") : confirmationCode.getText();

        if (!usePhoneAsConfirmation) {
            if (code == null || code.isBlank() || !code.matches("\\d+")) {
                str.append("Please enter a valid confirmation code\n");
                check = false;
            }
        }

        if (!check) {
            showAlert("Input Error", str.toString());
            return;
        }

        str.append(pre_phone);
        str.append(rest_phone);
        search.add(str.toString());
        search.add(code);
        BistroRequest request = new BistroRequest(BistroCommand.GET_TABLE_BY_PHONE_AND_CODE, search);
        Main.client.accept(request);
        
        BistroResponse response = Main.client.getResponse();
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null) { //TODO: returns the table number so maybe int/Integer
                infoVbox.setVisible(false);
                tableResultText.setText("Your table is: " + data.toString());
                tableResultText.setVisible(true);
                return;
            }
        }

        resetToDefaultView();
        showAlert("Error", "Could not find a matching order.");
    }

    @FXML
    void handleForgotConfirmation(ActionEvent event) {
        StringBuilder str = new StringBuilder();
        boolean check = true;
        String pre_phone = prePhone.getValue();
        String rest_phone = restPhone.getText();

        if (pre_phone == null || pre_phone.isBlank()) {
            str.append("Please select a phone prefix\n");
            check = false;
        }

        if (rest_phone == null || rest_phone.length() != 7 || !rest_phone.matches("\\d+")) {
            str.append("Please enter a valid phone number\n");
            check = false;
        }

        if (!check) {
            showAlert("Input Error", str.toString());
            return;
        }
        usePhoneAsConfirmation = true;
        confirmationCode.clear();
        confirmationBox.setVisible(false);
        confirmationBox.setManaged(false);
        forgotConfirmationBtn.setDisable(true);
        str.append(pre_phone);
        str.append(rest_phone);
        BistroRequest request = new BistroRequest(BistroCommand.FORGOT_CONFIRMATION_CODE, str.toString());
        Main.client.accept(request);
        BistroResponse response = Main.client.getResponse();
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null && data instanceof Integer)
                showAlert("Confirmation Code", "Your confirmation code is: " + data.toString());
        } else {
            showAlert("Error", "Could not find a matching order.");
            resetToDefaultView();
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
