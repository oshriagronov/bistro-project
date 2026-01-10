package gui;

import java.io.IOException;
import java.util.ArrayList;
import logic.Subscriber;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import logic.LoggedUser;
import logic.UserType;
import subscriber.SubscriberScreen;
import employee.employeeMenu;

/**
 * Controller class for the AcceptTableScreen.fxml view.
 * This class handles the logic for customers to confirm their arrival
 * by entering their phone/email and confirmation code to receive their table number.
 */
public class AcceptTableScreen {
    public static final String fxmlPath = "/gui/AcceptTable.fxml";
    /** Alert object used to display success or failure messages to the user. */
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    /** Button that navigates back to the main menu. */
    @FXML
    private Button backBtn;

    @FXML
    private HBox confirmationBox;

    @FXML
    private TextField confirmationCode;

    @FXML
    private VBox detailsVbox;

    @FXML
    private TextField emailField;

    @FXML
    private CheckBox forgotCheckBox;

    @FXML
    private VBox infoVbox;

    @FXML
    private HBox phoneBox;

    @FXML
    private ComboBox<String> prePhone;

    @FXML
    private TextField restPhone;

    @FXML
    private Button submitBTN;

    @FXML
    private Button getConfirmationBtn;

    @FXML
    private HBox subscriberConfirmationBox;

    @FXML
    private ComboBox<String> subscriberConfirmationCodes;

    @FXML
    private Text tableResultText;

    @FXML
    private Text identifyingDetailsText;

    private boolean isSubscriber = false;
    private String subscriberPhone = null;

    /**
     * Restores the view to its default state after an error or reset.
     */
    private void resetToDefaultView() {
        detailsVbox.setVisible(true);
        infoVbox.setVisible(true);
        tableResultText.setVisible(false);
        confirmationCode.clear();
        restPhone.clear();
        if (emailField != null) {
            emailField.clear();
        }
        if (subscriberConfirmationCodes != null) {
            subscriberConfirmationCodes.getSelectionModel().clearSelection();
        }
        if (!isSubscriber && forgotCheckBox != null) {
            forgotCheckBox.setSelected(false);
        }
        applyUserView();
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
     * Toggles visibility and layout management for a node.
     *
     * @param node node to show or hide
     * @param show true to show, false to hide
     */
    private void toggleNode(Node node, boolean show) {
        if (node != null) {
            node.setVisible(show);
            node.setManaged(show);
        }
    }

    /**
     * Applies the correct UI elements depending on the logged-in user type.
     */
    private void applyUserView() {
        if (isSubscriber) {
            toggleNode(detailsVbox, false);
            toggleNode(subscriberConfirmationBox, true);
            toggleNode(getConfirmationBtn, false);
            toggleNode(submitBTN, true);
            return;
        }

        toggleNode(detailsVbox, true);
        toggleNode(subscriberConfirmationBox, false);
        boolean forgotSelected = forgotCheckBox != null && forgotCheckBox.isSelected();
        toggleNode(confirmationBox, !forgotSelected);
        toggleNode(identifyingDetailsText, forgotSelected);
        toggleNode(submitBTN, !forgotSelected);
        toggleNode(getConfirmationBtn, forgotSelected);
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
        isSubscriber = LoggedUser.getType() == UserType.SUBSCRIBER;
        tableResultText.setVisible(false);

        if (isSubscriber) {
            setupSubscriberView();
            BistroResponse response = sendRequest(BistroCommand.GET_SUBSCRIBER_BY_ID, LoggedUser.getId());
            if (response.getData() instanceof Subscriber) {
                subscriberPhone = ((Subscriber) response.getData()).getPhone();
            }
            else {
                showAlert("Error", "There was error getting your subscriber information.");
                try {
                    Main.changeRoot(SubscriberScreen.fxmlPath);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            setupDefaultView();
        }
    }

    /**
     * Handles toggling of the forgot confirmation checkbox.
     */
    @FXML
    void handleForgotCheckBox(ActionEvent event) {
        applyUserView();
    }

    /**
     * Handles the "Forgot Confirmation" action.
     * Retrieves the confirmation code and start time for the provided phone or email.
     *
     * @param event The ActionEvent triggered by the forgot confirmation button.
     */
    @FXML
    void handleGetConfirmationCode(ActionEvent event) {
        String email = emailField != null ? emailField.getText().trim() : "";
        String prefix = prePhone.getValue();
        String rest = restPhone.getText();
        String phone = null;
        if (rest != null && !rest.trim().isEmpty()) {
            String phonePrefix = prefix != null ? prefix : "";
            phone = phonePrefix + rest.trim();
        }
        confirmationCode.clear();
        ArrayList<String> contactDetails = new ArrayList<>(2);
        contactDetails.add(phone);
        contactDetails.add(email);
        BistroResponse response = sendRequest(BistroCommand.FORGOT_CONFIRMATION_CODE, contactDetails);
        StringBuilder message = new StringBuilder();
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            message.append("Sent the confirmation code to your email and phone.");
        } else {
            message.append("Could not find a matching order.");
        }
        showAlert("Message", message.toString());
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
     * Configures the UI for a non-subscriber flow.
     */
    private void setupDefaultView() {
        isSubscriber = false;
        if (forgotCheckBox != null) {
            forgotCheckBox.setSelected(false);
        }
        applyUserView();
        prePhone.getItems().clear();
        prePhone.getItems().addAll("050", "052", "053", "054", "055", "058");
    }

    /**
     * Configures the UI for a subscriber and loads their confirmation codes.
     */
    private void setupSubscriberView() {
        isSubscriber = true;
        applyUserView();
        populateSubscriberConfirmationCodes();
    }

    /**
     * Populates the subscriber confirmation code combo box from active reservations.
     */
    private void populateSubscriberConfirmationCodes() {

        if (subscriberConfirmationCodes == null) {
            // Fallback to default flow if subscriber information is unavailable.
            showAlert("Error", "Could not load subscriber confirmation codes.");
            setupDefaultView();
            return;
        }
        subscriberConfirmationCodes.getItems().clear();
        BistroResponse response = sendRequest(BistroCommand.GET_SUBSCRIBER_CONFIRMATION_CODES, LoggedUser.getId());
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS && response.getData() instanceof ArrayList<?>) {
            ArrayList<String> codes = new ArrayList<>();
            for (Object obj : (ArrayList<?>) response.getData()) {
                if (obj instanceof String) {
                    codes.add(String.valueOf(obj));
                }
            }
            subscriberConfirmationCodes.getItems().addAll(codes);
            if (!codes.isEmpty()) {
                subscriberConfirmationCodes.getSelectionModel().selectFirst();
            }
        }
    }

    /**
     * Handles the submit button action.
     * Validates inputs, sends the phone/email and confirmation code to the server,
     * and displays the assigned table number when found.
     * @param event The ActionEvent triggered by the submit button.
     */
    
    @FXML
    void handleSubmit(ActionEvent event){
        StringBuilder errors = new StringBuilder();
        String code = isSubscriber ? resolveSubscriberCode(errors) : resolveGuestCode(errors);
        String identifier = isSubscriber ? resolveSubscriberIdentifier(errors) : resolveGuestIdentifier(errors);

        if (errors.length() > 0) {
            showAlert("Input Error", errors.toString());
            return;
        }

        ArrayList <String> search = new ArrayList<>(2);
        search.add(identifier);
        search.add(code);
        BistroResponse response = sendRequest(BistroCommand.GET_TABLE_BY_IDENTIFIER_AND_CODE, search);
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null) {
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
     * Collects and validates the subscriber confirmation code selected in the combo box.
     * Called from {@link #handleSubmit(ActionEvent)} whenever a subscriber submits form.
     */
    private String resolveSubscriberCode(StringBuilder errors) {
        String code = subscriberConfirmationCodes != null ? subscriberConfirmationCodes.getValue() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please select a valid confirmation code\n");
        }
        return code;
    }

    /**
     * Ensures the stored subscriber phone number is present before using it for lookup.
     * Append an error when it is missing so {@link #handleSubmit(ActionEvent)} can alert the user.
     */
    private String resolveSubscriberIdentifier(StringBuilder errors) {
        if (subscriberPhone == null || subscriberPhone.isBlank()) {
            errors.append("Missing subscriber phone number\n");
            return null;
        }
        return subscriberPhone;
    }

    /**
     * Reads the confirmation code entered by a guest user and validates that it is numeric.
     */
    private String resolveGuestCode(StringBuilder errors) {
        String code = confirmationCode != null ? confirmationCode.getText().trim() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please enter a valid confirmation code\n");
        }
        return code;
    }

    /**
     * Chooses either the full phone number or email to use for guest lookup.
     * Phone takes precedence once it validates; errors are appended when both contacts are missing.
     */
    private String resolveGuestIdentifier(StringBuilder errors) {
        String email = emailField != null ? emailField.getText().trim() : "";
        boolean hasEmail = !email.isEmpty();
        String prefix = prePhone.getValue();
        String rest = restPhone.getText();
        boolean hasPhoneInput = (prefix != null && !prefix.isBlank()) || (rest != null && !rest.isBlank());

        if (hasPhoneInput) {
            StringBuilder phoneErrors = new StringBuilder();
            boolean phoneValid = validatePhoneInputs(phoneErrors);
            if (phoneValid) {
                return buildPhoneNumber();
            }
            if (!hasEmail) {
                errors.append(phoneErrors);
            }
        }

        if (hasEmail) {
            return email;
        }

        errors.append("Please enter a phone number or email\n");
        return null;
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
			Main.changeRoot(getBackFxmlPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private String getBackFxmlPath() {
        UserType type = LoggedUser.getType();
        if (type == UserType.SUBSCRIBER) {
            return SubscriberScreen.fxmlPath;
        }
        else if (type == UserType.EMPLOYEE || type == UserType.MANAGER) {
            return employeeMenu.fxmlPath;
        }
        return MainMenuScreen.fxmlPath;
    }

}
