package gui;



import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class WorkersLonInScreen {

    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private Button loginBTN;

    @FXML
    private HBox loginHbox;

    @FXML
    private TextField password;

    @FXML
    private HBox passwordHbox;

    @FXML
    void initialize() {

    }

    public void showAlert(String title, String body) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }

    @FXML
    void login(ActionEvent event) {
        String pass = password.getText();
        if (pass == null || pass.isEmpty()) {
            showAlert("Error", "Please enter a password.");
        }
        Main.client.accept(pass);
        BistroResponse response = Main.client.getResponse();

        if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            Object data = response.getData();
            if (data instanceof Boolean && (Boolean) data) {
                showAlert("Success", "Login Successful");
                // TODO: Navigate to workers operation screen
            } else {
                showAlert("Login Failed", "Incorrect password");
            }
        } else {
            showAlert("Error", "No response from server");
        }
    }

}
