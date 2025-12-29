package gui;

import java.util.ArrayList;

import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.BistroCommand;
import communication.BistroRequest;
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
    public static final String fxmlPath = "/gui/AcceptTable.fxml";
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
    
    @FXML
	private Button backBtn;

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
        BistroRequest request = new BistroRequest(BistroCommand.GET_TABLE_BY_PHONE_AND_CODE, search);
        Main.client.accept(request);
        
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

	/**
	 * Handles the action when the "Back to MainMenu" button is clicked.
	 * Navigates the application back to the main menu screen.
	 * * @param event The ActionEvent triggered by the Back button.
	 */
	@FXML
	void back(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot(MainMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
