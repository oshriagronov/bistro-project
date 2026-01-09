package gui;

import java.util.ResourceBundle;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.StatusUpdate;
import employee.employeeMenu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import logic.LoggedUser;
import logic.Status;
import logic.UserType;
import subscriber.SubscriberScreen;

/**
 * Controller for the Cancel Reservation screen.
 * Allows the user to enter an order number and send a cancellation request.
 */
public class CancelReservationScreen {

    public static final String fxmlPath = "/gui/CancelReservation.fxml";

    @FXML
    private ResourceBundle resources;


    @FXML
    private TextField ConfirmationCodeField;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;
    /**
     * Handles the Cancel Reservation button click.
     * Validates the input and sends a cancellation request to the server.
     */
    @FXML
    private void cancelReservation(ActionEvent event) {
        String confirmationCode = ConfirmationCodeField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        
        if (phone.isEmpty() && email.isEmpty()) {
            messageLabel.setText("Please fill in phone or email and Confirmation code.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (confirmationCode.isEmpty()) {
            messageLabel.setText("Please enter Confirmation code.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        int orderNumber;
        try {
            orderNumber = Integer.parseInt(confirmationCode);
        } catch (NumberFormatException e) {
            messageLabel.setText("Confirmation code must be numeric.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        StatusUpdate update = buildCancelUpdate(orderNumber, phone, email);
        Main.client.accept(new BistroRequest(BistroCommand.CANCEL_RESERVATION, update));
        BistroResponse response = Main.client.getResponse();
        handleCancelResponse(response);
    }

    private StatusUpdate buildCancelUpdate(int orderNumber, String phone, String email) {
        if (!email.isEmpty()) {
            return new StatusUpdate(orderNumber, email, Status.CANCELLED);
        }
        return new StatusUpdate(phone, orderNumber, Status.CANCELLED);
    }

    /**
     * Handles the server response for the cancellation request.
     */
    public void handleCancelResponse(BistroResponse response) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Reservation Canceled");

        if (response.getStatus() == BistroResponseStatus.SUCCESS) {
            alert.setHeaderText("Cancellation Successful");
            alert.setContentText("The reservation has been successfully canceled.");
        } else {
            int orderNumber = (int) response.getData();
            alert.setHeaderText("Cancellation Successful");
            alert.setContentText("Reservation #" + orderNumber + " has been canceled successfully.");
        }

        alert.showAndWait();
    }
        /**
     * Navigates the application back to the main menu screen.
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
