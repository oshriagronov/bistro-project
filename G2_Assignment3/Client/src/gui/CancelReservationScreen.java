package gui;

import client.ClientController;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.BistroCommand;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Controller for the Cancel Reservation screen.
 * Allows the user to enter an order number and send a cancellation request.
 */
public class CancelReservationScreen {

    @FXML
    private TextField codeField; // Text field for entering the reservation/order number

    @FXML
    private Label messageLabel; // Label for displaying success/error messages

    /**
     * Triggered when the user clicks the "Cancel Reservation" button.
     * Validates input and sends the order number to the server.
     */
    @FXML
    public void cancelReservation() {
        String text = codeField.getText().trim();

        // Check if the field is empty
        if (text.isEmpty()) {
            messageLabel.setText("Please enter reservation number.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        int orderNumber;

        // Validate that the input is numeric
        try {
            orderNumber = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            messageLabel.setText("Reservation number must be numeric.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Send the request to the server
        ClientController.getInstance(null, 0).accept(
        		new BistroRequest(BistroCommand.CANCEL_RESERVATION, orderNumber)
        );
    }

    /**
     * Called when the server sends a response back to the client.
     * Updates the message label according to the result.
     */
    public void handleCancelResponse(BistroResponse response) {
        if (response.getStatus() == BistroResponseStatus.SUCCESS) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Reservation Canceled");
            alert.setHeaderText("Cancellation Successful");
            alert.setContentText("The reservation has been successfully canceled.");
            alert.showAndWait();

        } else {

        	int orderNumber = (int) response.getData();

        	Alert alert = new Alert(AlertType.INFORMATION);
        	alert.setTitle("Reservation Canceled");
        	alert.setHeaderText("Cancellation Successful");
        	alert.setContentText("Reservation #" + orderNumber + " has been canceled successfully.");
        	alert.showAndWait();
        }
    }

}
