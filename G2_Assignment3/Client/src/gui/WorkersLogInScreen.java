package gui;



import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Controller class for the WorkersLogInScreen.fxml view.
 * This class handles the authentication process for restaurant employees,
 * allowing them to access the employee management dashboard.
 */
public class WorkersLogInScreen {

    /** Alert object used to display success or failure messages to the user. */
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    /** Button to submit the login credentials. */
    @FXML
    private Button loginBTN;

    /** Container for the login button. */
    @FXML
    private HBox loginHbox;

    /** Text field for entering the employee password. */
    @FXML
    private TextField password;

    /** Container for the password input field. */
    @FXML
    private HBox passwordHbox;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    void initialize() {

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
     * Handles the login button action.
     * Validates the password input, communicates with the server to verify credentials,
     * and navigates to the employee screen upon successful authentication.
     * @param event The ActionEvent triggered by the login button.
     */
    @FXML
    void login(ActionEvent event) {
        String pass = password.getText();
        //TODO: add real check of password and remove the test
        if (pass.equals("12345")) {
            showAlert("Success", "Login Successful (Test)");
            try {
                Main.changeRoot("WorkerMenu.fxml");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Navigation Error", e.getMessage());
            }
            return;
        }
        if (pass == null || pass.isEmpty()) {
            showAlert("Error", "Please enter a password.");
        }
        Main.client.accept(pass);
        BistroResponse response = Main.client.getResponse();

        if ((response != null && response.getStatus() == BistroResponseStatus.SUCCESS)) {
            Object data = response.getData();
            if (data instanceof Boolean && (Boolean) data) {
                showAlert("Success", "Login Successful");
                try {
                	Main.changeRoot("WorkerMenu.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showAlert("Login Failed", "Incorrect password");
            }
        } else {
            showAlert("Error", "No response from server");
        }
    }

}
