package employee;

import java.util.ResourceBundle;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.WorkerLoginRequest;
import gui.LoginMenuScreen;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import logic.LoggedUser;
import logic.UserType;
import logic.Worker;

/**
 * Controller class for the WorkersLogInScreen.fxml view. This class handles the
 * authentication process for restaurant employees, allowing them to access the
 * employee management dashboard.
 */
public class WorkersLogInScreen {
	public static final String fxmlPath ="/employee/WorkersLogIn.fxml";

	/** Alert object used to display success or failure messages to the user. */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);

	/** Button to submit the login credentials. */
	 @FXML
    private ResourceBundle resources;

    @FXML
    private Button backBtn;

    @FXML
    private Button loginBTN;

    @FXML
    private PasswordField passwordTxt;

    @FXML
    private Text statusText;

    @FXML
    private TextField usernameTxt;

    @FXML
    void back(ActionEvent event) {

    }

	/**
	 * Initializes the controller. This method is automatically called after the
	 * FXML file has been loaded.
	 */
	@FXML
	void initialize() {

	}

	/**
	 * Displays an information alert to the user.
	 * 
	 * @param title The title of the alert window.
	 * @param body  The content text of the alert.
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Handles the login button action. Validates the password input, communicates
	 * with the server to verify credentials, and navigates to the employee screen
	 * upon successful authentication.
	 * 
	 * @param event The ActionEvent triggered by the login button.
	 */
	@FXML
	void login(ActionEvent event) {
		String username = usernameTxt.getText();
		String pass = passwordTxt.getText();
		boolean check = true;
		StringBuilder error = new StringBuilder();
		if (username == null || username.isEmpty()) {
			error.append("Please enter a username\n");
			check = false;
		}
		if (pass == null || pass.isEmpty()) {
			error.append("Please enter a password");
			check = false;
		}
		if (!check) {
			showAlert("Form error", error.toString());
		} else {
			Main.client.accept(new BistroRequest(BistroCommand.WORKER_LOGIN, new WorkerLoginRequest(username, pass)));
			BistroResponse response = Main.client.getResponse();
			if ((response != null && response.getStatus() == BistroResponseStatus.SUCCESS)) {
				showAlert("Success", "Login Successful");
				Worker w=(Worker)response.getData();
				new LoggedUser(w.getWorkerId(),UserType.valueOf(w.getWorkerType().name()));
				try {
					Main.changeRoot(employeeMenu.fxmlPath);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else
				showAlert("Login error", (String) response.getData());
		}

	}
	/**
	 * Handles the back button action. Navigates back to the previous screen.
	 * @param event The ActionEvent triggered by the back button.
	 */
	@FXML
	void backBtnAction(ActionEvent event) {
		try {
			Main.changeRoot(LoginMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
