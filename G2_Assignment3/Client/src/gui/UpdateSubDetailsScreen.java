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
    private TextField prephoneNum;

    @FXML
    private TextField phoneNum;

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
        

    }

	@FXML
	void loadSubscriberDetails(ActionEvent event) {

		// Load subscriber details from the database to the text fields
        Reservation currentReservation = BistroClient.getCurrentReservation();
        if (currentReservation != null) {
            phoneNum.setText(currentReservation.getPhoneNumber());
            phoneNum1.setText(currentReservation.getPhoneNumber());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load subscriber details.");
            alert.showAndWait();
        }
	}
}