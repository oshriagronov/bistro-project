package gui;

import java.util.ArrayList;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import logic.LoggedUser;
import logic.Reservation;
import logic.Subscriber;
import logic.UserType;
import subscriber.SubscriberScreen;
import employee.employeeMenu;

/**
 * Controller class for the Payment.fxml view.
 * This class handles the logic for customers to view their bill
 * by entering their confirmation code.
 */
public class PaymentScreen {
	public static final int PAYMENT_PER_DINER=100;
    public static final String fxmlPath = "/gui/Payment.fxml";
    /** Alert object used to display success or failure messages to the user. */
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private HBox codeHbox;

    @FXML
    private TextField confirmationCode;

    @FXML
    private VBox detailsVbox;

    @FXML
    private VBox infoVbox;

    @FXML
    private HBox subscriberConfirmationBox;

    @FXML
    private ComboBox<String> subscriberConfirmationCodes;

    @FXML
    private Button submitBTN;

    @FXML
    private HBox submitHbox;

    @FXML
    private Text total;

    @FXML
    private Button backBtn;

    private boolean isSubscriber = false;
    private Subscriber sub;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the view for the current user type, hides the total amount text,
     * and loads subscriber confirmation codes when applicable.
     */
    @FXML
    void initialize() {
        total.setVisible(false);
        UserType type = LoggedUser.getType();
        if (type == UserType.SUBSCRIBER) {
            this.sub = ScreenSetup.setupSubscriber(detailsVbox, null, null);
            setupSubscriberView();
        } else if (type == UserType.EMPLOYEE || type == UserType.MANAGER) {
            ScreenSetup.setupWorkerView(detailsVbox, null, null);
            setupDefaultView();
        } else {
            ScreenSetup.setupGuestView(detailsVbox, null, null);
            setupDefaultView();
        }
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
     * Validates the confirmation code, sends it to the server,
     * and displays the total bill amount if found (including subscriber discount).
     * @param event The ActionEvent triggered by the submit button.
     */
    
    @FXML
    void handleSubmit(ActionEvent event) {
        StringBuilder errors = new StringBuilder();
        String code = isSubscriber ? resolveSubscriberCode(errors) : resolveGuestCode(errors);

        if (errors.length() > 0) {
            showAlert("Input Error", errors.toString());
            return;
        }
        BistroResponse response = sendRequest(BistroCommand.GET_BILL, code);
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null && data instanceof Reservation) {
            	Reservation res = (Reservation) data;
            	int num_guests=res.getNumberOfGuests();
            	int sub_id = res.getSubscriberId();
            	double pay = PAYMENT_PER_DINER * num_guests;            	
            	if(sub_id > 0)
            		pay = pay * 0.9;
                toggleNode(infoVbox, false);
                total.setText("Total to pay: " + pay);
                toggleNode(total, true);
            }
        } else {
            showAlert("Error", "Could not find bill for these details.");
        }
    }

    /**
     * Handles the back button action and routes to the appropriate previous screen.
     * @param event The ActionEvent triggered by the Back button.
     */
    @FXML
    void back(ActionEvent event) {
        try {
            Main.changeRoot(getBackFxmlPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resolves the FXML path to return to based on the logged-in user type.
     * @return fxml path for the previous screen
     */
    private String getBackFxmlPath() {
        UserType type = LoggedUser.getType();
        if (type == UserType.SUBSCRIBER) {
            return SubscriberScreen.fxmlPath;
        } else if (type == UserType.EMPLOYEE || type == UserType.MANAGER) {
            return employeeMenu.fxmlPath;
        }
        return MainMenuScreen.fxmlPath;
    }

    /**
     * Configures the UI for non-subscriber flows.
     */
    private void setupDefaultView() {
        isSubscriber = false;
        applyUserView();
    }

    /**
     * Configures the UI for subscriber flows and loads confirmation codes.
     */
    private void setupSubscriberView() {
        isSubscriber = true;
        applyUserView();
        populateSubscriberConfirmationCodes();
    }

    /**
     * Populates the subscriber confirmation code combo box from the server.
     */
    private void populateSubscriberConfirmationCodes() {
        if (subscriberConfirmationCodes == null) {
            showAlert("Error", "Could not load subscriber confirmation codes.");
            setupDefaultView();
            return;
        }
        subscriberConfirmationCodes.getItems().clear();
        BistroResponse response = sendRequest(BistroCommand.GET_SUBSCRIBER_CONFIRMATION_CODE_FOR_PAYMENT, LoggedUser.getId());
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS
                && response.getData() instanceof ArrayList<?>) {
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
     * Applies the UI visibility rules based on the current user type.
     */
    private void applyUserView() {
        if (isSubscriber) {
            toggleNode(detailsVbox, false);
            toggleNode(subscriberConfirmationBox, true);
            return;
        }
        toggleNode(detailsVbox, true);
        toggleNode(subscriberConfirmationBox, false);
    }

    /**
     * Toggles visibility and layout management for a node.
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
     * Sends a request to the server and returns the response.
     * @param command server command to execute
     * @param data request payload
     * @return server response, or null if unavailable
     */
    private BistroResponse sendRequest(BistroCommand command, Object data) {
        BistroRequest request = new BistroRequest(command, data);
        Main.client.accept(request);
        return Main.client.getResponse();
    }

    /**
     * Reads the confirmation code selected by a subscriber and validates it.
     * @param errors collector for validation messages
     * @return confirmation code, or null if missing/invalid
     */
    private String resolveSubscriberCode(StringBuilder errors) {
        String code = subscriberConfirmationCodes != null ? subscriberConfirmationCodes.getValue() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please select a valid confirmation code\n");
        }
        return code;
    }

    /**
     * Reads the confirmation code entered by a guest user and validates it.
     * @param errors collector for validation messages
     * @return confirmation code, or null if missing/invalid
     */
    private String resolveGuestCode(StringBuilder errors) {
        String code = confirmationCode != null ? confirmationCode.getText().trim() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please enter a valid confirmation code\n");
        }
        return code;
    }
}
