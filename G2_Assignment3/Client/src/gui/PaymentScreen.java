package gui;

import java.util.ArrayList;

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
 * Controller class for the Payment.fxml view.
 * This class handles the logic for customers to view their bill
 * by entering their phone number and confirmation code.
 */
public class PaymentScreen {
    public static final String fxmlPath = "/gui/Payment.fxml";
    /** Alert object used to display success or failure messages to the user. */
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private HBox codeHbox;

    @FXML
    private TextField confirmationCode;

    @FXML
    private VBox infoVbox;

    @FXML
    private HBox phoneHbox;

    @FXML
    private ComboBox<String> prePhone;

    @FXML
    private TextField restPhone;

    @FXML
    private Button submitBTN;

    @FXML
    private HBox submitHbox;

    @FXML
    private Text total;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the phone number prefix options and hides the total amount text.
     */
    @FXML
    void initialize() {
        prePhone.getItems().clear();
        prePhone.getItems().addAll("050", "052", "053", "054", "055", "058");
        total.setVisible(false);
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
     * Validates the phone number and confirmation code, sends them to the server,
     * and displays the total bill amount if found.
     * @param event The ActionEvent triggered by the submit button.
     */
    @FXML
    void handleSubmit(ActionEvent event) {
        StringBuilder str = new StringBuilder();
        boolean check = true;
        ArrayList<String> search = new ArrayList<>();

        String pre = prePhone.getValue();
        String rest = restPhone.getText();

        if (pre == null || rest == null || rest.length() != 7 || !rest.matches("\\d+")) {
            str.append("Please enter a valid phone number\n");
            check = false;
        }

        String code = confirmationCode.getText();
        if (code == null || code.isEmpty()) {
            str.append("Please enter confirmation code\n");
            check = false;
        }

        if (!check) {
            showAlert("Input Error", str.toString());
            return;
        }

        String phoneNum = pre + rest;
        search.add(phoneNum);
        search.add(code);

        Main.client.accept(search);

        BistroResponse response = Main.client.getResponse();
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null) {
                infoVbox.setVisible(false);
                total.setText("Total to pay: " + data.toString() + "₪");
                total.setVisible(true);
            }
        } else {
            showAlert("Error", "Could not find bill for these details.");
        }
    }

}
