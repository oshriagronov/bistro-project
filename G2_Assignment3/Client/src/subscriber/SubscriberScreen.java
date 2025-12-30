package subscriber;

import java.lang.reflect.Array;
import java.util.ArrayList;

import logic.Reservation;
import gui.Main;
import gui.UpdateScreen;
import gui.WaitingListScreen;
import gui.OrderScreen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the subscriber account screen.
 * Shows visit history and allows editing basic personal info.
 */
public class SubscriberScreen {
	public static final String fxmlPath = "/subscriber/SubscriberScreen.fxml";
	/** Alert used to show validation messages to the user. */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	/** List of visit history entries (placeholder data for now). */
	private ListView<String> historyList;

	@FXML
	/** Input for updated phone number. */
	private TextField phoneField;

	@FXML
	/** Input for updated email address. */
	private TextField emailField;

	@FXML
	/** Container for the update form. */
	private VBox updateForm;

	@FXML
	/** Button that reveals the update form. */
	private Button revealUpdateBtn;

	@FXML
	/** Button that submits the update form. */
	private Button submitBtn;

	@FXML
	/** Button that returns the user to the main menu. */
	private Button backBtn;

	@FXML
	/** Button to navigate to update reservation screen. */
	private Button updateReservationBtn;

	@FXML
	/** Button to navigate to waiting list screen. */
	private Button waitingListBtn;

	@FXML
	/** Button to navigate to new reservation screen. */
	private Button newReservationBtn;

	@FXML
	/**
	 * Initializes the view with sample history data and hides the update form.
	 */
	void initialize() {
		Object data = Main.client.getResponse().getData();
		if (data instanceof ArrayList<?>) {
			ArrayList<Reservation> reservations = new ArrayList<>();
			// Safely cast and filter the list items
			for (Object obj : (ArrayList<?>) data) {
				if (obj instanceof Reservation) {
					reservations.add((Reservation) obj);
				}
			}

			if (reservations.isEmpty()) {
				historyList.getItems().setAll("No previous reservations found.");
			} else {
				ArrayList<String> historyStrings = new ArrayList<>();
				for (Reservation res : reservations) {
					historyStrings.add(String.format(
						"Order date: %s | Guests: %d | Confirmation: %d | Diners: %d | Placed: %s | Status: %s",
						res.getOrderDate(), res.getNumberOfGuests(), res.getConfirmationCode(),
						res.getSubscriberId(), res.getDateOfPlacingOrder(), res.getStatus()));
				}
				historyList.getItems().setAll(historyStrings);
			}
		}
		updateForm.setVisible(false);
	}

	/**
	 * Shows a simple modal alert.
	 * @param title alert title
	 * @param body message to display
	 */
	private void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	/**
	 * Reveals the update form and hides the update button.
	 * @param event JavaFX action event
	 */
	void revealUpdateForm(ActionEvent event) {
		updateForm.setVisible(true);
		revealUpdateBtn.setVisible(false);
	}

	@FXML
	/**
	 * Validates inputs, shows a success/failure alert, and hides the form on success.
	 * @param event JavaFX action event
	 */
	void handleSubmit(ActionEvent event) {
		String phone = phoneField.getText();
		String email = emailField.getText();
		StringBuilder errors = new StringBuilder();
		boolean ok = true;

		if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
			ok = false;
			errors.append("Please enter phone number or email\n");
		}

		if (phone != null && !phone.isBlank() && (!phone.matches("05\\d{8}"))) {
			ok = false;
			errors.append("Phone number must be 10 digits and start with 05\n");
		}

		if (email != null && !email.isBlank()
				&& !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			ok = false;
			errors.append("Please enter a valid Email\n");
		}

		if (!ok) {
			showAlert("Update Failure", errors.toString());
			return;
		}

		showAlert("Update Success", "Personal info updated.");
		updateForm.setVisible(false);
		revealUpdateBtn.setVisible(true);
		phoneField.clear();
		emailField.clear();
	}

	@FXML
	/**
	 * Returns the user to the main menu.
	 * @param event JavaFX action event
	 */
	void backToSubscriberLogin(ActionEvent event) {
		try {
			Main.changeRoot(SubscriberLoginScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * Navigates to the update reservation screen.
	 * @param event JavaFX action event
	 */
	void goToUpdateReservation(ActionEvent event) {
		try {
			Main.changeRoot(UpdateScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * Navigates to the waiting list screen.
	 * @param event JavaFX action event
	 */
	void goToWaitingList(ActionEvent event) {
		try {
			Main.changeRoot(WaitingListScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * Navigates to the new reservation screen.
	 * @param event JavaFX action event
	 */
	void goToNewReservation(ActionEvent event) {
		try {
			Main.changeRoot(OrderScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
