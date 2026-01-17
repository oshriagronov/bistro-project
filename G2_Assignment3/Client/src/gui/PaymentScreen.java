package gui;

import java.util.ArrayList;

import client.AlertUtil;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
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

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the view for the current user type, hides the total amount text,
     * and loads subscriber confirmation codes when applicable.
     */
    @FXML
    void initialize() {
        total.setVisible(false);
        bindManagedToVisible(detailsVbox);
        bindManagedToVisible(subscriberConfirmationBox);
        bindManagedToVisible(submitHbox);
        bindManagedToVisible(total);
        if (setupUserView()) {
            populateSubscriberConfirmationCodes();
        }
    }

    /**
     * Binds a node's managed property to its visible property.
     * @param node node to bind
     */
    private void bindManagedToVisible(Node node) {
        if (node != null) {
            node.managedProperty().bind(node.visibleProperty());
        }
    }


    /**
     * Applies the view configuration for the current user type using ScreenSetup.
     * @return true when the subscriber view is active and subscriber data is available
     */
    private boolean setupUserView() {
        UserType type = LoggedUser.getType();
        if (type == UserType.SUBSCRIBER) {
            boolean hasSubscriber = ScreenSetup.setupSubscriber(detailsVbox, null, null) != null;
            if (!hasSubscriber) {
                ScreenSetup.setupGuestView(detailsVbox, null, subscriberConfirmationBox);
            }
            return hasSubscriber;
        }
        if (type == UserType.EMPLOYEE || type == UserType.MANAGER) {
            ScreenSetup.setupWorkerView(detailsVbox, null, subscriberConfirmationBox);
            return false;
        }
        ScreenSetup.setupGuestView(detailsVbox, null, subscriberConfirmationBox);
        return false;
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
        String code = resolveConfirmationCode(errors);
        if (errors.length() > 0) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Input Error", errors.toString());
            return;
        }
        BistroResponse response = sendRequest(BistroCommand.GET_BILL, code);
        if (response != null) {
            switch (response.getStatus()) {
                case SUCCESS:                    
                    Object data = response.getData();
                    if (data != null && data instanceof Reservation) {
                        Reservation res = (Reservation) data;
                        double pay = PAYMENT_PER_DINER * res.getNumberOfGuests();                        
                        pay = res.getSubscriberId() > 0 ? pay * 0.9 : pay;
                        detailsVbox.setVisible(false);
                        subscriberConfirmationBox.setVisible(false);
                        submitHbox.setVisible(false);
                        total.setText("Total to pay: " + pay);
                        total.setVisible(true);
                    }
                    break;
                case NOT_FOUND:
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Error", "Could not find bill for these details.");
                    break;
                default:
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Error", "The server could not process your request.");
                    break;
            }
        }
        else{
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Error", "There was error sending the request to the server.");
        }
    }

    /**
     * Populates the subscriber confirmation code combo box from the server.
     */
    private void populateSubscriberConfirmationCodes() {
        if (subscriberConfirmationCodes == null) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Error", "Could not load subscriber confirmation codes.");
            return;
        }
        subscriberConfirmationCodes.getItems().clear();
        BistroResponse response = sendRequest(BistroCommand.GET_SUBSCRIBER_CONFIRMATION_CODE_FOR_PAYMENT, LoggedUser.getId());
        if (response != null) {
            Object data = response.getData();
            switch (response.getStatus()) {
                case SUCCESS:
                    if(data instanceof ArrayList<?>){
                        
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
                        else{
                            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Message", "There is no reservations for this subscriber to pay.");
                        }
                    }
                    else{
                        System.out.println(response.getStatus());
                        AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Error", "Could not load subscriber confirmation codes.");
                    }
                    break;
            
                default:
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Error", "The server could not process your request.");
                    break;
            }
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
     * Reads and validates the confirmation code based on which input is visible.
     * @param errors collector for validation messages
     * @return confirmation code, or null if missing/invalid
     */
    private String resolveConfirmationCode(StringBuilder errors) {
        if (subscriberConfirmationBox != null && subscriberConfirmationBox.isVisible()) {
            String code = subscriberConfirmationCodes != null ? subscriberConfirmationCodes.getValue() : null;
            if (code == null || code.isBlank() || !code.matches("\\d+")) {
                errors.append("Please select a valid confirmation code\n");
            }
            return code;
        }
        String code = confirmationCode != null ? confirmationCode.getText().trim() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please enter a valid confirmation code\n");
        }
        return code;
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

}
