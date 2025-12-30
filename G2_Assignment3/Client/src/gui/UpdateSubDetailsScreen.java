package gui;

import java.net.URL;
import java.util.ResourceBundle;
import client.BistroClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import logic.Reservation;

public class UpdateSubDetailsScreen {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox infoVbox;

    @FXML
    private Button menuBTN;

    @FXML
    private TextField phoneNum;

    @FXML
    private TextField phoneNum1;

    @FXML
    private Button submitBTN;

    @FXML
    void backToMenu(ActionEvent event) {

    }

    @FXML
    void submit(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert infoVbox != null : "fx:id=\"infoVbox\" was not injected: check your FXML file 'Update.fxml'.";
        assert menuBTN != null : "fx:id=\"menuBTN\" was not injected: check your FXML file 'Update.fxml'.";
        assert phoneNum != null : "fx:id=\"phoneNum\" was not injected: check your FXML file 'Update.fxml'.";
        assert phoneNum1 != null : "fx:id=\"phoneNum1\" was not injected: check your FXML file 'Update.fxml'.";
        assert submitBTN != null : "fx:id=\"submitBTN\" was not injected: check your FXML file 'Update.fxml'.";

    }
	//I need to show the currect details of the subscriber from DB,
	//and let the user update the parameters he wants to update

	@FXML
	void loadSubscriberDetails(ActionEvent event) {

		// Load subscriber details from the database
		Reservation reservation = BistroClient.getSubscriberDetails(phoneNum.getText());
		if (reservation != null) {
			// Populate the fields with the subscriber details
			phoneNum1.setText(reservation.getPhoneNumber());
			// Add other fields as necessary
		} else {
			// Show an error message if subscriber not found
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Subscriber not found.");
			alert.showAndWait();
		}
	}

}
