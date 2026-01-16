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
import javafx.scene.layout.Pane;
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

    @FXML
    private Pane knob;

    /**
     * Displays a blocking information alert with the provided title and body.
     *
     * @param title dialog title
     * @param body  dialog message body
     */
    private void showAlert(String title, String body) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }

    @FXML
    /**
     * Initializes the login screen with the Subscriber ID view visible by default.
     */
    public void initialize(){
        usernamelogin.setVisible(false);
        IDlogin.setVisible(true);
    }

    @FXML
    /**
     * Toggles between Subscriber ID login and username/password login views.
     */
    private void handleSwitchView() {
    boolean isIdView = switchView.isSelected();

    // Toggle Subscriber ID view
    IDlogin.setVisible(!isIdView);
    IDlogin.setManaged(!isIdView);
    
    // Toggle Username view
    usernamelogin.setVisible(isIdView);
    usernamelogin.setManaged(isIdView);
    }

    @FXML
    /**
     * Handles login by validating inputs, sending the relevant request,
     * and navigating on success.
     *
     * @param event UI action event from the login button
     */
    void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = codeField.getText();
        String subID = subIDfield.getText();
        StringBuilder errors = new StringBuilder();
        boolean ok = true;
        BistroResponse response = null;

        if(subID != null && !subID.isBlank() && !switchView.isSelected()) {
            // Send login request
            if (subID.length() != 5){
                errors.append("Please scan your card again\n");
            }
            else{
            int sub_id = Integer.parseInt(subID);
            Main.client.accept(new BistroRequest(BistroCommand.GET_SUBSCRIBER_BY_ID, sub_id));
            response = Main.client.getResponse();
            }
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
        if (ok && response.getStatus() == BistroResponseStatus.SUCCESS) {
            try {
                Object subscriberCode = response.getData();
                if(subscriberCode instanceof Integer){
                    // Save subscriber globally
                    LoggedUser.setSubscriber((Integer)subscriberCode);
                    // Switch screen
                    Main.changeRoot(SubscriberScreen.fxmlPath);
                }
                else{
                    errors.append("something with the server is wrong.");
                    ok = false;                    
                }
            } catch (Exception e) {
                showAlert("App Error", "The method changeRoot failed.");
                e.printStackTrace();
            }
        }
        else {
            errors.append("user info are wrong, please enter valid info.");
            ok = false;
        }
        if (!ok)
            showAlert("Login Failure", errors.toString());
    }

    @FXML
    /**
     * Returns the user to the main login menu.
     *
     * @param event UI action event from the back button
     */
    void backToLoginMenu(ActionEvent event) {
        try {
            Main.changeRoot(LoginMenuScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
