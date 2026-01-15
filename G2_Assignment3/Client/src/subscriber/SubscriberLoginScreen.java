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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import logic.LoggedUser;

/**
 * Controller for the subscriber login view.
 * Handles basic input validation and navigation to the order screen.
 */
public class SubscriberLoginScreen {

    public static final String fxmlPath = "/subscriber/SubscriberLogin.fxml";

    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

      @FXML
    private VBox IDlogin;

    @FXML
    private Button backBtn;

    @FXML
    private PasswordField codeField;

    @FXML
    private Button loginBtn;

    @FXML
    private TextField subIDfield;

    @FXML
    private ToggleButton switchView;

    @FXML
    private TextField usernameField;

    @FXML
    private VBox usernamelogin;

    private void showAlert(String title, String body) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }

    @FXML
    void handleSwitchView(ActionEvent event) {
        if (switchView.isSelected()) {
            usernamelogin.setVisible(true);
            IDlogin.setVisible(false);
        } else {
            usernamelogin.setVisible(false);
            IDlogin.setVisible(true);
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = codeField.getText();
        String subID = subIDfield.getText();
        StringBuilder errors = new StringBuilder();
        boolean ok = true;
        BistroResponse response = null;

        if(subID != null && !subID.isBlank() && !switchView.isSelected()) {
            // Send login request
            ArrayList<String> subscriberLoginInfo = new ArrayList<>();
            subscriberLoginInfo.add(subID);
            Main.client.accept(new BistroRequest(BistroCommand.GET_SUBSCRIBER_BY_ID, subscriberLoginInfo));
            response = Main.client.getResponse();
            
        }
        else {
            if (username == null || username.isBlank()) {
                ok = false;
                errors.append("Please enter username\n");
            }
            if (password == null || password.isBlank()) {
                ok = false;
                errors.append("Please enter password\n");
            }
            // Send login request
            ArrayList<String> subscriberLoginInfo = new ArrayList<>();
            subscriberLoginInfo.add(username);
            subscriberLoginInfo.add(password);
            Main.client.accept(new BistroRequest(BistroCommand.SUBSCRIBER_LOGIN, subscriberLoginInfo));
            response = Main.client.getResponse();
        }
        
        if (ok) {
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
                errors.append("user info are wrong, please enter valid info.");
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
