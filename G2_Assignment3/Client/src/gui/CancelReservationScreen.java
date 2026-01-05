package gui;

import java.util.ResourceBundle;

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

    public static final String fxmlPath = "/gui/CancelReservation.fxml";

    @FXML
    private ResourceBundle resources;


    @FXML
    private TextField ReservationIdField;

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
        String reservationId = ReservationIdField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        
        if (reservationId.isEmpty() || (phone.isEmpty() && email.isEmpty())) {
            messageLabel.setText("Please fill in phone or email and reservation number.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (reservationId.isEmpty()) {
            messageLabel.setText("Please enter reservation number.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        int orderNumber;
        try {
            orderNumber = Integer.parseInt(reservationId);
        } catch (NumberFormatException e) {
            messageLabel.setText("Reservation number must be numeric.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        // Send cancellation request to the server, first if he entered phone else use email
        if(phone.isEmpty()) {
            Main.client.accept(new BistroRequest(BistroCommand.CHANGE_STATUS, new StatusUpdate(orderNumber,email, Status.CANCELLED)));
            BistroResponse response = Main.client.getResponse();
            handleCancelResponse(response);
            return;
        }
        else if(email.isEmpty()) {
            Main.client.accept(new BistroRequest(BistroCommand.CHANGE_STATUS, new StatusUpdate(phone ,orderNumber, Status.CANCELLED)));
            BistroResponse response = Main.client.getResponse();
            handleCancelResponse(response);
            return;
        }
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
            Main.changeRoot(MainMenuScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
