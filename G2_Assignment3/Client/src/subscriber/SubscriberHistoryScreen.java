package subscriber;

import java.util.ArrayList;

import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.Reservation;

/**
 * Controller for the subscriber reservation history screen.
 */
public class SubscriberHistoryScreen {
	public static final String fxmlPath = "/subscriber/SubscriberHistory.fxml";

	@FXML
	/** Table of visit history entries. */
	private TableView<Reservation> historyTable;

	@FXML
	private TableColumn<Reservation, String> orderDateCol;

	@FXML
	private TableColumn<Reservation, String> guestsCol;

	@FXML
	private TableColumn<Reservation, String> confirmationCodeCol;

	@FXML
	private TableColumn<Reservation, String> placedCol;

	@FXML
	private TableColumn<Reservation, String> statusCol;

	@FXML
	private TableColumn<Reservation, String> startTimeCol;

	@FXML
	private TableColumn<Reservation, String> finishTimeCol;

	@FXML
	/** Button that returns to the subscriber dashboard. */
	private Button backBtn;

	@FXML
	/**
	 * Initializes the view with reservation history.
	 */
	void initialize() {
		setColumnFactories();
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
				historyTable.setItems(FXCollections.observableArrayList());
				historyTable.setPlaceholder(new Label("No previous reservations found."));
			} else {
				historyTable.setItems(FXCollections.observableArrayList(reservations));
			}
		} else {
			historyTable.setItems(FXCollections.observableArrayList());
			historyTable.setPlaceholder(new Label("No previous reservations found."));
		}
	}

	private void setColumnFactories() {
		orderDateCol.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getOrderDate())));
		guestsCol.setCellValueFactory(data -> new SimpleStringProperty(
			Integer.toString(data.getValue().getNumberOfGuests())));
		confirmationCodeCol.setCellValueFactory(data -> new SimpleStringProperty(
			Integer.toString(data.getValue().getConfirmationCode())));
		placedCol.setCellValueFactory(data -> new SimpleStringProperty(
			formatDate(data.getValue().getDateOfPlacingOrder())));
		statusCol.setCellValueFactory(data -> new SimpleStringProperty(formatStatus(data.getValue().getStatus())));
		startTimeCol.setCellValueFactory(data -> new SimpleStringProperty(
			formatTime(data.getValue().getStart_time())));
		finishTimeCol.setCellValueFactory(data -> new SimpleStringProperty(
			formatTime(data.getValue().getFinish_time())));
	}

	private String formatDate(java.time.LocalDate date) {
		return date == null ? "" : date.toString();
	}

	private String formatTime(java.time.LocalTime time) {
		return time == null ? "" : time.toString();
	}

	private String formatStatus(Object status) {
		return status == null ? "" : status.toString();
	}

	@FXML
	/**
	 * Returns to the subscriber dashboard.
	 * @param event JavaFX action event
	 */
	void backToSubscriberScreen(ActionEvent event) {
		try {
			Main.changeRoot(SubscriberScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
