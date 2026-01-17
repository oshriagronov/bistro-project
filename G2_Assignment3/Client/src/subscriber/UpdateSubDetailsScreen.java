package subscriber;

import java.net.URL;
import java.util.ResourceBundle;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.RequestFactory;
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
        Main.client.accept(RequestFactory.getSubscriberById(LoggedUser.getId()));
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

        Subscriber updatedSubscriber = new Subscriber(LoggedUser.getId(), (String) null);
        if (!phone.isBlank()) {
            updatedSubscriber.setPhone(phone);
        }
        if (!email.isBlank()) {
            updatedSubscriber.setEmail(email);
        }
        if (!username.isBlank()) {
            updatedSubscriber.setUsername(username);
        }
        if (!password.isBlank()) {
            updatedSubscriber.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
        if (!firstName.isBlank()) {
            updatedSubscriber.setFirstName(firstName);
        }
        if (!lastName.isBlank()) {
            updatedSubscriber.setLastName(lastName);
        }
        Main.client.accept(RequestFactory.getUpdateSubscriberInfo(updatedSubscriber));
        BistroResponse response = Main.client.getResponse();
        BistroResponseStatus status = response.getStatus();
        if (status == BistroResponseStatus.SUCCESS) {
            showAlert("Success", "Information updated successfully.");
        } else if (status == BistroResponseStatus.ALREADY_EXISTS) {
            showAlert("Username Already Exists", "Please choose a different username.");
        } else {
            String message = "Something went wrong with sending the request to the server.";
            if (response.getData() instanceof String) {
                message = (String) response.getData();
            }
            showAlert("Error", message);
        }



    }


    private void showAlert(String title, String body) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }
}
