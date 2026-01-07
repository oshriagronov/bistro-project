package subscriber;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import logic.LoggedUser;
import logic.Subscriber;
import org.mindrot.jbcrypt.BCrypt;


public class UpdateSubDetailsScreen {

    public static final String fxmlPath = "/subscriber/UpdateSubDetails.fxml";

    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox infoVbox;

    @FXML
    private Button backBTN;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Button submitBTN;

    @FXML
    /**
     * Initializes the view with reservation history.
     */
    void initialize() {
        Main.client.accept(new BistroRequest(BistroCommand.GET_SUB, LoggedUser.getId()));
        BistroResponse response = Main.client.getResponse();
        if(response.getStatus() == BistroResponseStatus.SUCCESS && response.getData() instanceof Subscriber){
            Subscriber sub = (Subscriber)Main.client.getResponse().getData();
            phoneNumberField.setText(sub.getPhone());
            emailField.setText(sub.getEmail());
            usernameField.setText(sub.getUsername());
            firstNameField.setText(sub.getFirstName());
            lastNameField.setText(sub.getLastName());
        }
        else{
            showAlert("Error", "Something went wrong");
        }
    }

    @FXML
    void back(ActionEvent event) {
        try {
            Main.changeRoot(SubscriberScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    void submit(ActionEvent event) {
        StringBuilder errors = new StringBuilder();
        String phone = phoneNumberField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (phone.isBlank() && email.isBlank() && username.isBlank() && password.isBlank()
                && firstName.isBlank() && lastName.isBlank()) {
            errors.append("Please enter at least one field to update\n");
        }

        if (errors.length() > 0) {
            showAlert("Input Error", errors.toString());
            return;
        }

        ArrayList<String> updateDetails = new ArrayList<>();
        if (!phone.isBlank()) {
            updateDetails.add(phone);
        }
        if (!email.isBlank()) {
            updateDetails.add(email);
        }
        if (!username.isBlank()) {
            updateDetails.add(username);
        }
        if (!password.isBlank()) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            updateDetails.add(hash);
        }
        if (!firstName.isBlank()) {
            updateDetails.add(firstName);
        }
        if (!lastName.isBlank()) {
            updateDetails.add(lastName);
        }
        Main.client.accept(new BistroRequest(BistroCommand.UPDATE_SUBSCRIBER_INFO, updateDetails));
        boolean isSuccess = Main.client.getResponse().getStatus() == BistroResponseStatus.SUCCESS;
        String title = isSuccess ? "Success" : "Error";
        String message = isSuccess
                ? "Information updated successfully."
                : "Something went wrong with sending the request to the server.";
        showAlert(title, message);



    }


    private void showAlert(String title, String body) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }
}
