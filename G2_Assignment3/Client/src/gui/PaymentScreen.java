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

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the view for the current user type and hides the total amount text.
     */
    @FXML
    void initialize() {
        total.setVisible(false);
        isSubscriber = LoggedUser.getType() == UserType.SUBSCRIBER;

        if (isSubscriber) {
            setupSubscriberView();
        } else {
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
     * and displays the total bill amount if found.
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
     */
    @FXML
    void back(ActionEvent event) {
        try {
            Main.changeRoot(getBackFxmlPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBackFxmlPath() {
        UserType type = LoggedUser.getType();
        if (type == UserType.SUBSCRIBER) {
            return SubscriberScreen.fxmlPath;
        } else if (type == UserType.EMPLOYEE || type == UserType.MANAGER) {
            return employeeMenu.fxmlPath;
        }
        return MainMenuScreen.fxmlPath;
    }


    private void setupDefaultView() {
        isSubscriber = false;
        applyUserView();
    }

    private void setupSubscriberView() {
        isSubscriber = true;
        applyUserView();
        populateSubscriberConfirmationCodes();
    }

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

    private void applyUserView() {
        if (isSubscriber) {
            toggleNode(detailsVbox, false);
            toggleNode(subscriberConfirmationBox, true);
            return;
        }
        toggleNode(detailsVbox, true);
        toggleNode(subscriberConfirmationBox, false);
    }

    private void toggleNode(Node node, boolean show) {
        if (node != null) {
            node.setVisible(show);
            node.setManaged(show);
        }
    }

    private BistroResponse sendRequest(BistroCommand command, Object data) {
        BistroRequest request = new BistroRequest(command, data);
        Main.client.accept(request);
        return Main.client.getResponse();
    }

    private String resolveSubscriberCode(StringBuilder errors) {
        String code = subscriberConfirmationCodes != null ? subscriberConfirmationCodes.getValue() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please select a valid confirmation code\n");
        }
        return code;
    }

    private String resolveGuestCode(StringBuilder errors) {
        String code = confirmationCode != null ? confirmationCode.getText().trim() : null;
        if (code == null || code.isBlank() || !code.matches("\\d+")) {
            errors.append("Please enter a valid confirmation code\n");
        }
        return code;
    }
}
