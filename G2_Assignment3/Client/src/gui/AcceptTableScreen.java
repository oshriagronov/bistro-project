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

    /** Button that navigates back to the main menu. */
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

    /**
     * Restores the view to its default state after an error or reset.
     */
    private void resetToDefaultView() {
        infoVbox.setVisible(true);
        tableResultText.setVisible(false);
        confirmationBox.setVisible(true);
        confirmationBox.setManaged(true);
        forgotConfirmationBtn.setDisable(false);
        usePhoneAsConfirmation = false;
    }

    /**
     * Validates the phone prefix and number input fields.
     *
     * @param errors collects validation messages when inputs are invalid
     * @return true when both prefix and number are valid
     */
    private boolean validatePhoneInputs(StringBuilder errors) {
        String prefix = prePhone.getValue();
        String rest = restPhone.getText();
        boolean valid = true;

        if (prefix == null || prefix.isBlank()) {
            errors.append("Please select a phone prefix\n");
            valid = false;
        }

        if (rest == null || rest.length() != 7 || !rest.matches("\\d+")) {
            errors.append("Please enter a valid phone number\n");
            valid = false;
        }

        return valid;
    }

    /**
     * Builds the full phone number from the prefix and rest of the input.
     *
     * @return concatenated phone number string
     */
    private String buildPhoneNumber() {
        return prePhone.getValue() + restPhone.getText();
    }

    /**
     * Sends a request to the server and returns the response.
     *
     * @param command server command to execute
     * @param data request payload
     * @return response received from the server
     */
    private BistroResponse sendRequest(BistroCommand command, Object data) {
        BistroRequest request = new BistroRequest(command, data);
        Main.client.accept(request);
        return Main.client.getResponse();
    }

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    void initialize() {
        // Initialize phone prefix options.
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
     * Validates inputs, sends the phone and confirmation code to the server,
     * and displays the assigned table number when found.
     * @param event The ActionEvent triggered by the submit button.
     */
    
    @FXML
    void handleSubmit(ActionEvent event){
        StringBuilder errors = new StringBuilder();
        boolean phoneValid = validatePhoneInputs(errors);
        String code = usePhoneAsConfirmation ? (phoneValid ? restPhone.getText() : "") : confirmationCode.getText();

        if (!usePhoneAsConfirmation) {
            if (code == null || code.isBlank() || !code.matches("\\d+")) {
                errors.append("Please enter a valid confirmation code\n");
            }
        }

        if (errors.length() > 0) {
            showAlert("Input Error", errors.toString());
            return;
        }

        ArrayList <String> search = new ArrayList<>();
        search.add(buildPhoneNumber());
        search.add(code);
        BistroResponse response = sendRequest(BistroCommand.GET_TABLE_BY_PHONE_AND_CODE, search);
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

    /**
     * Handles the "Forgot Confirmation" action.
     * Retrieves the confirmation code and start time for the given phone number.
     *
     * @param event The ActionEvent triggered by the forgot confirmation button.
     */
    @FXML
    void handleForgotConfirmation(ActionEvent event) {
        StringBuilder errors = new StringBuilder();
        ArrayList<String> result;

        if (!validatePhoneInputs(errors)) {
            showAlert("Input Error", errors.toString());
            return;
        }
        confirmationCode.clear();
        String phoneNumber = buildPhoneNumber();
        BistroResponse response = sendRequest(BistroCommand.FORGOT_CONFIRMATION_CODE, phoneNumber);
        StringBuilder message = new StringBuilder();
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null && data instanceof ArrayList<?>){
                result = new ArrayList<>();
                // Safely cast and filter the list items
                for (Object obj : (ArrayList<?>) data) {
                    if (obj instanceof String) {
                        result.add((String) obj);
                    }
                }
                message.append("Your confirmation code is: " + result.get(0) + "\nStart time is: " + result.get(1));
            }
        } else {
            message.append("Could not find a matching order.");
        }
        showAlert("Message", message.toString());
    }

    /**
     * Handles the action when the "Back to MainMenu" button is clicked.
     * Navigates the application back to the main menu screen.
     * @param event The ActionEvent triggered by the Back button.
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
