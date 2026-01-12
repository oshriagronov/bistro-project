package subscriber;

import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import gui.LoginMenuScreen;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import logic.LoggedUser;

/**
 * Controller for the subscriber login view.
 * Handles basic input validation and navigation to the order screen.
 */
public class SubscriberLoginScreen {

    public static final String fxmlPath = "/subscriber/SubscriberLogin.fxml";

    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private TextField usernameField;

    @FXML
    private TextField codeField;

    @FXML
    private Button loginBtn;

    @FXML
    private Button backBtn;

    private void showAlert(String title, String body) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = codeField.getText();
        StringBuilder errors = new StringBuilder();
        boolean ok = true;

        if (username == null || username.isBlank()) {
            ok = false;
            errors.append("Please enter username\n");
        }
        if (password == null || password.isBlank()) {
            ok = false;
            errors.append("Please enter password\n");
        }

        if (ok) {
            // Send login request
            ArrayList<String> subscriberLoginInfo = new ArrayList<>();
            subscriberLoginInfo.add(username);
            subscriberLoginInfo.add(password);
            Main.client.accept(new BistroRequest(BistroCommand.SUBSCRIBER_LOGIN, subscriberLoginInfo));
            BistroResponse response = Main.client.getResponse();
            if (response.getStatus() == BistroResponseStatus.SUCCESS) {
                try {
                    Object subscriberCode = response.getData();
                    if(subscriberCode instanceof Integer)
                        // Save subscriber globally
                        LoggedUser.setSubscriber((Integer)subscriberCode);
                    // Switch screen
                    Main.changeRoot(SubscriberScreen.fxmlPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                errors.append("Subscriber ID or password are wrong.");
                ok = false;
            }
        }

        if (!ok)
            showAlert("Login Failure", errors.toString());
    }

    @FXML
    void backToLoginMenu(ActionEvent event) {
        try {
            Main.changeRoot(LoginMenuScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
