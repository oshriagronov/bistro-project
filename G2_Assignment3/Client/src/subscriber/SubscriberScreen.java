package subscriber;

import communication.BistroCommand;
import communication.BistroRequest;
import gui.Main;
import gui.UpdateScreen;
import gui.UpdateSubDetailsScreen;
import gui.WaitingListScreen;
import gui.OrderScreen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import logic.LoggedUser;

/**
 * Controller for the subscriber account screen.
 * Provides navigation and basic personal info updates.
 */
public class SubscriberScreen {
	public static final String fxmlPath = "/subscriber/SubscriberScreen.fxml";
	/** Alert used to show validation messages to the user. */

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
	/** Button to navigate to reservation history screen. */
	private Button historyBtn;

	@FXML
	/** Button to navigate to new reservation screen. */
	private Button newReservationBtn;


	@FXML
	/**
	 * Reveals the update form and hides the update button.
	 * @param event JavaFX action event
	 */
	void updateInfo(ActionEvent event) {
		try {
			Main.changeRoot(UpdateSubDetailsScreen.fxmlPath, 640, 560);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * Returns the user to the main menu.
	 * @param event JavaFX action event
	 */
	void backToSubscriberLogin(ActionEvent event) {
		try {
			LoggedUser.setGuest();
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

	@FXML
	/**
	 * Navigates to the reservation history screen.
	 * @param event JavaFX action event
	 */
	void goToReservationHistory(ActionEvent event) {
		try {
			Main.client.accept(new BistroRequest(BistroCommand.GET_SUBSCRIBER_HISTORY, LoggedUser.getId()));
			Main.changeRoot(SubscriberHistoryScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
