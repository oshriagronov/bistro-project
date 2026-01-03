package gui;

import client.ClientController;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.StatusUpdate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import logic.Status;

/**
 * Controller for the Cancel Reservation screen.
 * Allows the user to enter an order number and send a cancellation request.
 */
public class CancelReservationScreen {

    public static String fxmlPath = "/gui/CancelReservation.fxml";

    @FXML
    private TextField codeField;

    @FXML
    private Label messageLabel;

    /**
     * Handles the Cancel Reservation button click.
     * Validates the input and sends a cancellation request to the server.
     */
    @FXML
    private void cancelReservation(ActionEvent event) {
        String text = codeField.getText().trim();

        if (text.isEmpty()) {
            messageLabel.setText("Please enter reservation number.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        int orderNumber;
        try {
            orderNumber = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            messageLabel.setText("Reservation number must be numeric.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        ClientController.getInstance(null, 0).accept(
                new BistroRequest(BistroCommand.CHANGE_STATUS,
                        new StatusUpdate(orderNumber, Status.CANCELLED)));
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
}
