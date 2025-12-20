package gui;

import javafx.scene.control.TextField;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import logic.Reservation;

public class WaitingListScreen {

    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private ComboBox<String> diners;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> prePhone;

    @FXML
    private Button submit;

    public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

    @FXML
    void initialize() {
        diners.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			diners.getItems().add(String.valueOf(i));
		}
        prePhone.getItems().clear();
		for (int i = 0; i <= 8; i++) {
			prePhone.getItems().add(String.valueOf("05"+i));
		}

    }

    @FXML
	void submit(ActionEvent event) {
		StringBuilder str = new StringBuilder();
		boolean check = true;
		String phone_number,number_of_guests;
        int num=0;
		phone_number = prePhone.getValue() +phoneField.getText();
		number_of_guests = (String) diners.getValue();
		//Validate Diners Amount
		if (number_of_guests == null) {
			check = false;
			str.append("Please choose the diners amount\n");
		}
		// Final Check: Display errors or process update
		if (!check) {
			showAlert("Please enter number of diners", str.toString());
		} else {
			showAlert("Reservasion Success", "Reservation successfully created.");
			// Send the update command and new details to the server
			Reservation waitingList = new Reservation(Integer.parseInt(number_of_guests), num, phone_number);
			Main.client.accept(waitingList);
		}
    }

    @FXML
	void backToMenu(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot("MainMenu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
