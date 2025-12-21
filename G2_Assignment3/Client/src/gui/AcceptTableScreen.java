package gui;

import java.util.ArrayList;

import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Controller class for the AcceptTableScreen.fxml view.
 * This class handles the logic for customers to confirm their arrival
 * by entering their phone number and confirmation code to receive their table number.
 */
public class AcceptTableScreen {

    /** Alert object used to display success or failure messages to the user. */
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    /** Text field for entering the confirmation code. */
    @FXML
    private TextField confirmationCode;

    /** VBox containing the input fields, hidden upon successful table assignment. */
    @FXML
    private VBox infoVbox;

    /** Text field for entering the phone number. */
    @FXML
    private TextField phoneNumber;

    /** Button to submit the confirmation details. */
    @FXML
    private Button submitBTN;

    /** Text element to display the assigned table number. */
    @FXML
    private Text tableResultText;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    void initialize() {
        

    }

    //TODO: add phone number check
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
     * Validates the phone number, sends the phone number and confirmation code to the server,
     * and displays the assigned table number if successful.
     * @param event The ActionEvent triggered by the submit button.
     */
    @FXML
    void handleSubmit(ActionEvent event){
        StringBuilder str = new StringBuilder();
        boolean check = true;
        ArrayList <String> search = new ArrayList<>();
        String phoneNum = phoneNumber.getText();

		if (phoneNum == null || phoneNum.length() != 7 || !phoneNum.matches("\\d+")) {
			str.append("Please enter a valid 7-digit phone number\n");
			check = false;
		}
        String code = confirmationCode.getText();

        if (!check) {
            showAlert("Input Error", str.toString());
            return;
        }

        search.add(phoneNum);
        search.add(code);
        Main.client.accept(search); //TODO: edit the accept/send somthing else
        
        BistroResponse response = Main.client.getResponse();
        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data != null) { //TODO: returns the table number so maybe int/Integer
                infoVbox.setVisible(false);
                tableResultText.setText("Your table is: " + data.toString());
                tableResultText.setVisible(true);
            }
        } else {
            showAlert("Error", "Could not find a matching order.");
        }
    }

    //TODO: maybe add return to main menu

}
