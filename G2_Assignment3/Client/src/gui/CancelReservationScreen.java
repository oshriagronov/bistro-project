package gui;

import java.util.ResourceBundle;

import communication.BistroCommand;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.RequestFactory;
import communication.StatusUpdate;
import employee.employeeMenu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
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
    private ComboBox<String> prePhone;

    @FXML
    private TextField ConfirmationCodeField;

    @FXML
    private Label messageLabel;

 
  

    /**
     * Handles the Cancel Reservation button click.
     * Validates the input and sends a cancellation request to the server.
     */
    @FXML
    private void cancelReservation(ActionEvent event) {
        String confirmationCode = ConfirmationCodeField.getText().trim();

        if (confirmationCode.isEmpty()) {
            messageLabel.setText("Please enter Confirmation code.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Object data = null;
        try {
            Main.client.accept(RequestFactory.getActiveReservationsByCode(confirmationCode));
            BistroResponse response = Main.client.getResponse();
            data = response.getData();
            
        } catch (Exception e) {
            messageLabel.setText("Error retrieving order number.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        int orderNumber = -1;

        // Safe conversion
        if (data instanceof Integer) {
            orderNumber = (Integer) data;
        } else if (data instanceof String) {
            try {
                orderNumber = Integer.parseInt((String) data);
            } catch (NumberFormatException e) {
                messageLabel.setText("Invalid order number format.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }
        } else {
            messageLabel.setText("Unknown data type received from server.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Main.client.accept(RequestFactory.cancelById(orderNumber));
        handleCancelResponse(orderNumber); 
        
    }

    /**
     * Handles the server response for the cancellation request.
     */
    public void handleCancelResponse(int orderNumber) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Reservation Canceled");
        alert.setHeaderText("Cancellation Successful");
        alert.setContentText("Reservation #" + orderNumber + " has been canceled successfully.");
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
