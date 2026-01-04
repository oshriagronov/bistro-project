package subscriber;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponseStatus;
import gui.LoginMenuScreen;
import gui.Main;
import gui.OrderScreen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import logic.LoggedUser;
import logic.Subscriber;

/**
 * Controller for the subscriber login view.
 * Handles basic input validation and navigation to the order screen.
 */
public class SubscriberLoginScreen {

    public static final String fxmlPath = "/subscriber/SubscriberLogin.fxml";

    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private TextField subscriberCodeField;

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
        String subscriberCode = subscriberCodeField.getText();
        String password = codeField.getText();
        StringBuilder errors = new StringBuilder();
        boolean ok = true;

        if (subscriberCode == null || subscriberCode.isBlank()) {
            ok = false;
            errors.append("Please enter subscriber ID\n");
        }
        if (password == null || password.isBlank()) {
            ok = false;
            errors.append("Please enter password\n");
        }

        if (ok) {
            // Send login request
            Main.client.accept(new BistroRequest(
                    BistroCommand.SUBSCRIBER_LOGIN,
                    new Subscriber(Integer.parseInt(subscriberCode), password)
            ));

            if (Main.client.getResponse().getStatus() == BistroResponseStatus.SUCCESS) {
                try {
                    // Get subscriber returned from server
                    Subscriber logged = (Subscriber) Main.client.getResponse().getData();

                    // Save subscriber globally
                    LoggedUser.setSubscriber(logged.getSubscriberId());

                    // Load OrderScreen
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(OrderScreen.fxmlPath));
                    // Get controller and set subscriber mode
                    OrderScreen controller = loader.getController();
                    controller.setSubscriber();

                    // Switch screen
                    Main.changeRoot(OrderScreen.fxmlPath);

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
