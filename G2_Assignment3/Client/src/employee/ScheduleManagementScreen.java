package employee;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.RequestFactory;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.SpecialDay;
import logic.WeeklySchedule;

/**
 * Controller for the Schedule Management screen.
 * <p>
 * This screen allows employees to manage the restaurant opening and closing
 * hours:
 * </p>
 * <ul>
 * <li><b>Weekly schedule</b> - update opening/closing times for each day of
 * week.</li>
 * <li><b>Special dates</b> - set opening/closing times for a specific future
 * date (e.g., holidays), using an "upsert" logic on the server side.</li>
 * </ul>
 *
 * <p>
 * Time choices are provided as whole hours (HH:00), and support selecting
 * midnight ("00:00") as a valid closing time.
 * </p>
 */
public class ScheduleManagementScreen {

	/**
	 * FXML path for this screen.
	 */
	public static final String fxmlPath = "/employee/ScheduleManagement.fxml";

	@FXML
	private Button MenuBtn;

	@FXML
	private CheckBox closedChceckBox;

	@FXML
	private ComboBox<String> closeCB;
	@FXML
	private TableColumn<WeeklySchedule, LocalTime> closeCol;

	@FXML
	private TableColumn<WeeklySchedule, DayOfWeek> dayCol;

	@FXML
	private Label lblWeeklyStatus;

	@FXML
	private ComboBox<String> openCB;
	@FXML
	private TableColumn<WeeklySchedule, LocalTime> openCol;

	@FXML
	private Button saveBtn;

	@FXML
	private Button specialClearBtn;
	@FXML
	private ComboBox<String> specialCloseCB;

	@FXML
	private DatePicker specialDate;

	@FXML
	private ComboBox<String> specialOpenCB;
	@FXML
	private Button specialSaveBtn;

	@FXML
	private Tab specialTab;

	@FXML
	private Tab regualrTab;

	@FXML
	private TableColumn<SpecialDay, LocalDate> specialDateCol;

	@FXML
	private TableColumn<SpecialDay, LocalTime> SpecialClosingCol;

	@FXML
	private TableColumn<SpecialDay, LocalTime> SpecialOpeningCol;

	@FXML
	private TableView<SpecialDay> specialTable;

	@FXML
	private TableView<WeeklySchedule> weeklyHoursTable;

	@FXML
	private TabPane tableTabs;

	/**
	 * Shared information alert used by this screen.
	 */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	/**
	 * Initializes the controller after the FXML elements are injected.
	 * <p>
	 * Responsibilities:
	 * </p>
	 * <ul>
	 * <li>Binds weekly schedule table columns to {@link WeeklySchedule}
	 * getters.</li>
	 * <li>Loads weekly schedule from the server.</li>
	 * <li>Populates time ComboBoxes with whole hours + optional midnight.</li>
	 * <li>Sets default selections for the ComboBoxes and DatePicker.</li>
	 * <li>Enables/disables the weekly "Save" button based on row selection.</li>
	 * <li>Prevents choosing past dates (and today) in the special-date
	 * DatePicker.</li>
	 * </ul>
	 */
	public void initialize() {
		// Weekly save is enabled only when a row is selected
		saveBtn.setDisable(true);
		closedChceckBox.selectedProperty().addListener((obs, was, isNow) -> {
			specialOpenCB.setDisable(isNow);
			specialCloseCB.setDisable(isNow);
			if (isNow) {
			    specialOpenCB.getSelectionModel().clearSelection();
			    specialCloseCB.getSelectionModel().clearSelection();
			}
			else {
				specialOpenCB.getSelectionModel().selectFirst();
				specialCloseCB.getSelectionModel().selectLast();
			}
		});
		
		
		// Bind table columns to WeeklySchedule properties by getter names
		dayCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dayOfWeek"));
		openCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("open"));
		closeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("close"));
		specialDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("day"));
		SpecialOpeningCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("open"));
		SpecialClosingCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("close"));
		// Load the current weekly schedule
		loadSchedule();

		// Populate time selection (09:00..23:00 plus 00:00 if includeMidnight=true)
		fillWholeHours(openCB, 9, 23, true);
		fillWholeHours(closeCB, 9, 23, true);
		fillWholeHours(specialOpenCB, 9, 23, true);
		fillWholeHours(specialCloseCB, 9, 23, true);

		// Default selections: open = first, close = last (usually midnight)
		openCB.getSelectionModel().selectFirst();
		closeCB.getSelectionModel().selectLast();
		specialOpenCB.getSelectionModel().selectFirst();
		specialCloseCB.getSelectionModel().selectLast();

		// Default special date: tomorrow
		specialDate.setValue(LocalDate.now().plusDays(1));

		// Enable weekly save only when a row is selected
		weeklyHoursTable.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> saveBtn.setDisable(newSelection == null));

		// Block past dates (and today) from being selected
		specialDate.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
			@Override
			public void updateItem(java.time.LocalDate date, boolean empty) {
				super.updateItem(date, empty);

				if (empty || date == null)
					return;

				// Blocks past and current date; allows only dates strictly after today
				boolean isPastOrToday = !date.isAfter(java.time.LocalDate.now());
				setDisable(isPastOrToday);

				if (isPastOrToday) {
					setStyle("-fx-opacity: 0.45;");
				}
			}
		});
	}

	/**
	 * Shows an information alert dialog with a title and body message.
	 *
	 * @param title alert title
	 * @param body  alert content text
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Populates a ComboBox with whole-hour time strings in "HH:00" format.
	 * <p>
	 * Example: startHour=9, finishHour=23 => 09:00..23:00
	 * </p>
	 * <p>
	 * If {@code includeMidnight} is true, "00:00" is appended at the end. This is
	 * useful for allowing midnight as a valid closing time.
	 * </p>
	 *
	 * @param cb              the ComboBox to populate
	 * @param startHour       start hour (0-23)
	 * @param finishHour      finish hour (0-23), inclusive
	 * @param includeMidnight whether to append "00:00"
	 */
	private void fillWholeHours(ComboBox<String> cb, int startHour, int finishHour, boolean includeMidnight) {
		ObservableList<String> hours = FXCollections.observableArrayList();

		for (int h = startHour; h <= finishHour; h++) {
			hours.add(String.format("%02d:00", h));
		}

		if (includeMidnight) {
			hours.add("00:00");
		}

		cb.setItems(hours);
	}

	/**
	 * Loads the weekly schedule from the server and displays it in the table.
	 * <p>
	 * Sends a {@code GET_WEEKLY_SCHEDULE} request (via {@link RequestFactory}),
	 * reads the response from {@link Main#client}, and updates
	 * {@link #weeklyHoursTable}.
	 * </p>
	 */
	private void loadSchedule() {
		Main.client.accept(RequestFactory.getWeeklySchedule());
		Object data = Main.client.getResponse().getData();
		ArrayList<WeeklySchedule> schedule = (ArrayList<WeeklySchedule>) data;
		weeklyHoursTable.setItems(FXCollections.observableArrayList(schedule));
	}

	private void loadSpecialDates() {
		Main.client.accept(RequestFactory.getSpecialDates(20));
		Object data = Main.client.getResponse().getData();
		ArrayList<SpecialDay> dates = (ArrayList<SpecialDay>) data;
		specialTable.setItems(FXCollections.observableArrayList(dates));
	}

	/**
	 * Navigates back to the employee menu screen.
	 *
	 * @param event UI event
	 */
	@FXML
	void menu(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves weekly opening/closing time changes for the currently selected day.
	 * <p>
	 * Validation:
	 * </p>
	 * <ul>
	 * <li>Closing time must be after opening time.</li>
	 * <li>Exception: closing time may be midnight (00:00) to represent end-of-day
	 * closing.</li>
	 * </ul>
	 *
	 * <p>
	 * On success, the weekly schedule is reloaded from the server.
	 * </p>
	 *
	 * @param event UI event
	 */
	@FXML
	void save(ActionEvent event) {
		WeeklySchedule ws = weeklyHoursTable.getSelectionModel().getSelectedItem();

		// Safety: should not happen if button is disabled properly
		if (ws == null) {
			showAlert("Error", "Please select a day to update");
			return;
		}

		String openText = openCB.getValue();
		String closeText = closeCB.getValue();

		LocalTime open = LocalTime.parse(openText);
		LocalTime close = LocalTime.parse(closeText);

		// Allow midnight as a special closing time
		if (!close.isAfter(open) && !close.equals(LocalTime.MIDNIGHT)) {
			showAlert("Error", "Closing time should be later than the opening time");
			return;
		}

		Main.client.accept(RequestFactory.updateRegularDayTimes(ws.getDayOfWeek(), open, close));
		BistroResponse resp = Main.client.getResponse();

		if (resp != null && resp.getStatus() == BistroResponseStatus.SUCCESS) {
			loadSchedule();
		} else {
			showAlert("Error", "Failed to update the schedule");
		}
	}

	/**
	 * Saves (upserts) opening/closing hours for a special date.
	 * <p>
	 * Validation:
	 * </p>
	 * <ul>
	 * <li>Closing time must be after opening time.</li>
	 * <li>Exception: closing time may be midnight (00:00).</li>
	 * </ul>
	 *
	 * <p>
	 * Note: DatePicker already blocks choosing past dates (and today).
	 * </p>
	 *
	 * @param event UI event
	 */
	@FXML
	void specialSave(ActionEvent event) {
		// Basic null checks to avoid parse/null errors
		if (specialDate.getValue() == null) {
			showAlert("Error", "Please choose a date");
			return;
		}
		if (closedChceckBox.isSelected()) {
			Main.client.accept(RequestFactory.updateSpecialDayTimes(specialDate.getValue(), LocalTime.MIDNIGHT,
					LocalTime.MIDNIGHT));
			checkSpecialResponse();
			return;
		}

		if (specialOpenCB.getValue() == null || specialCloseCB.getValue() == null) {
			showAlert("Error", "Please choose opening and closing time");
			return;
		}

		LocalTime open = LocalTime.parse(specialOpenCB.getValue());
		LocalTime close = LocalTime.parse(specialCloseCB.getValue());

		if (!close.isAfter(open) && !close.equals(LocalTime.MIDNIGHT)) {
			showAlert("Error", "Closing time should be later than the opening time");
			return;
		}

		Main.client.accept(RequestFactory.updateSpecialDayTimes(specialDate.getValue(), open, close));
		checkSpecialResponse();
	}

	private void checkSpecialResponse() {
		BistroResponse resp = Main.client.getResponse();

		if (resp != null && resp.getStatus() == BistroResponseStatus.SUCCESS) {
			showAlert("Success", "Successfully updated the chosen date");
			loadSpecialDates();
		} else {
			showAlert("Error", "Failed to update the schedule");
		}
	}

	@FXML
	void switchToRegular(Event event) {
		loadSchedule();
	}

	@FXML
	void switchToSpecial(Event event) {
		loadSpecialDates();
	}
}
