package gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import gui.Restaurant;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.LoggedUser;
import logic.Reservation;
import logic.Status;
import logic.Subscriber;
import logic.UserType;
import logic.Worker;
;

/**
 * Controller for the {@code Order.fxml} view.
 * <p>
 * This screen allows placing a new reservation by selecting a date, a time slot,
 * and the number of diners. It supports different flows depending on the logged-in
 * user type:
 * </p>
 * <ul>
 *   <li><b>Guest</b>: must enter email and phone number.</li>
 *   <li><b>Subscriber</b>: details are fetched from the server and the contact fields
 *       are auto-filled.</li>
 *   <li><b>Employee/Manager</b>: can place reservations with worker controls enabled
 *       (while guest fields may remain visible per UI design).</li>
 * </ul>
 *
 * <p>
 * Availability is calculated per 30-minute time slot by:
 * </p>
 * <ol>
 *   <li>Fetching opening/closing hours for the chosen date.</li>
 *   <li>Fetching existing reservations per slot (through {@link Restaurant#buildDinersByTime(LocalDate)}).</li>
 *   <li>Checking if the current tables configuration can accommodate the chosen group size.</li>
 * </ol>
 *
 * <p>
 * The DatePicker disables dates that are out of range (today..today+1 month) and
 * dates where the restaurant is closed. Closed-day checks are cached for performance.
 * </p>
 */
public class OrderScreen {

	/** FXML path for the order screen. */
	public static final String fxmlPath = "/gui/Order.fxml";

	/** Utility for generating random values (reserved for future use). */
	private final Random random = new Random();

	/** Loaded subscriber data when the logged-in user is a subscriber. */
	private Subscriber sub = null;

	/** Loaded worker data when the logged-in user is an employee/manager (currently unused). */
	private Worker worker = null;

	/** Alert dialog used to display information and error messages. */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML private Button backBtn;
	@FXML private VBox btnVbox;
	@FXML private CheckBox checkBox;
	@FXML private ComboBox<String> dinersAmmount;
	@FXML private VBox nonSubVbox;
	@FXML private Button orderBtn;
	@FXML private DatePicker orderDate;
	@FXML private TextField orderEmail;
	@FXML private ComboBox<String> orderHours;
	@FXML private TextField phoneNumber;
	@FXML private ComboBox<String> phoneStart;
	@FXML private HBox subHBOX;
	@FXML private TextField subID;
	@FXML private VBox subVbox;
	@FXML private VBox workerVbox;

	/**
	 * Mapping from a reservation time slot to a sorted list of diner group sizes
	 * already occupying that slot. Built by {@link Restaurant#buildDinersByTime(LocalDate)}.
	 */
	private Map<LocalTime, List<Integer>> dinersByTime;

	/**
	 * A sorted list of table capacities currently available in the restaurant.
	 * Loaded via {@link Restaurant#getTableSizes()}.
	 */
	private List<Integer> tablesSizes;

	/**
	 * Cache of "is closed day" checks to avoid repeated synchronous server calls,
	 * especially while DatePicker cells are being rendered.
	 */
	private final Map<LocalDate, Boolean> closedCache = new HashMap<>();

	/**
	 * Initializes the Order screen.
	 * <p>
	 * Called automatically after the FXML is loaded. This method:
	 * </p>
	 * <ul>
	 *   <li>Configures the view according to the logged-in user type.</li>
	 *   <li>Sets a DatePicker cell factory that disables:
	 *     <ul>
	 *       <li>Past dates</li>
	 *       <li>Dates more than one month ahead</li>
	 *       <li>Dates where the restaurant is closed</li>
	 *     </ul>
	 *   </li>
	 *   <li>Initializes ComboBoxes (diners amount, phone prefix) and default selections.</li>
	 *   <li>Updates available time slots whenever the date changes.</li>
	 *   <li>Selects the first open day and first available time slot by default.</li>
	 * </ul>
	 */
	@FXML
	public void initialize() {
		if (LoggedUser.getType()==UserType.SUBSCRIBER) {
        	this.sub = ScreenSetup.setupSubscriber(nonSubVbox, workerVbox, null);
        }
        else if (LoggedUser.getType()==UserType.EMPLOYEE || LoggedUser.getType()==UserType.MANAGER) {
        	this.worker = ScreenSetup.setupWorkerView(nonSubVbox, workerVbox, null);
        }
        else {
            ScreenSetup.setupGuestView(nonSubVbox, workerVbox, null);
        }
		subHBOX.setVisible(false);

		orderDate.setDayCellFactory(d -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setDisable(false);
					setStyle("");
					return;
				}

				LocalDate today = LocalDate.now();
				LocalDate maxDate = today.plusMonths(1);

				boolean outOfRange = item.isBefore(today) || item.isAfter(maxDate);
				if (outOfRange) {
					setDisable(true);
					setStyle("-fx-background-color: #ccc;");
					return;
				}

				boolean closedDay = isClosedDay(item);
				if (closedDay) {
					setDisable(true);
					setStyle("-fx-background-color: #ccc;");
				} else {
					setDisable(false);
					setStyle("");
				}
			}
		});

		dinersAmmount.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			dinersAmmount.getItems().add(String.valueOf(i));
		}

		orderDate.valueProperty().addListener((obs, oldDate, newDate) -> {
			if (newDate != null) {
				showOnlyAvailableTime(newDate);
				if (!orderHours.getItems().isEmpty()) {
					orderHours.getSelectionModel().selectFirst();
				} else {
					orderHours.getSelectionModel().clearSelection();
				}
			}
		});

		phoneStart.getItems().clear();
		phoneStart.getItems().addAll("050", "052", "053", "054", "055", "058");

		LocalDate today = LocalDate.now();
		LocalDate firstOpen = findNextOpenDate(today);

		if (firstOpen == null) {
			orderDate.setDisable(true);
			orderHours.setDisable(true);
			orderBtn.setDisable(true);
			showAlert("Closed", "No available days in the next month.");
			return;
		}

		orderDate.setValue(firstOpen);
		showOnlyAvailableTime(firstOpen);
		orderHours.getSelectionModel().selectFirst();
		phoneStart.getSelectionModel().selectFirst();
		dinersAmmount.getSelectionModel().selectFirst();
	}

	/**
	 * Finds the next open date starting from {@code start} (inclusive), scanning forward
	 * up to one month ahead.
	 *
	 * @param start the first date to check
	 * @return the first date that is not closed; {@code null} if none are open in the next month window
	 */
	private LocalDate findNextOpenDate(LocalDate start) {
		LocalDate max = start.plusMonths(1);
		for (LocalDate d = start; !d.isAfter(max); d = d.plusDays(1)) {
			if (!isClosedDay(d)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Handles the subscriber CheckBox click event.
	 * <p>
	 * If selected, shows the subscriber-id input field; otherwise hides it.
	 * </p>
	 *
	 * @param e the action event triggered by clicking the checkbox
	 */
	@FXML
	public void checkClicked(ActionEvent e) {
		subHBOX.setVisible(checkBox.isSelected());
	}

	/**
	 * Displays a modal informational alert dialog.
	 *
	 * @param title alert title
	 * @param body  alert message body
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Handles the "Order" button click event.
	 * <p>
	 * Validates inputs:
	 * </p>
	 * <ul>
	 *   <li>Date is selected</li>
	 *   <li>Diners amount is selected</li>
	 *   <li>Subscriber ID is numeric (if checkbox is selected)</li>
	 *   <li>Email is valid (for non-subscribers)</li>
	 *   <li>Phone is a valid 10-digit number (for non-subscribers)</li>
	 *   <li>Time slot is selected</li>
	 *   <li>Selected time slot has enough availability based on current table sizes</li>
	 * </ul>
	 *
	 * <p>
	 * If the selected slot is not available, the method attempts to suggest the nearest
	 * adjacent slots (+30/-30 minutes) when they are valid candidates.
	 * </p>
	 *
	 * <p>
	 * On success, creates a {@link Reservation} and sends it to the server using
	 * {@link BistroCommand#ADD_RESERVATION}.
	 * </p>
	 *
	 * @param event the action event triggered by clicking the order button
	 */
	@FXML
	public void clickOrder(ActionEvent event) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourFromNow = LocalDateTime.now().plusHours(1);

		int amount = 0;
		String phone;
		String email;
		String idStr;
		String amountStr;

		StringBuilder errors = new StringBuilder();
		boolean valid = true;

		LocalDate date = orderDate.getValue();
		LocalDate today = LocalDate.now();
		LocalTime selected = null;

		// 1) Validate date
		if (date == null) {
			errors.append("Please pick a reservation date\n");
			valid = false;
		}

		// 2) Validate diners amount
		amountStr = dinersAmmount.getValue();
		if (amountStr == null || amountStr.isBlank()) {
			errors.append("Please choose diners amount\n");
			valid = false;
		} else {
			amount = Integer.parseInt(amountStr);
		}

		// 3) Subscriber ID (if checkbox selected)
		if (!checkBox.isSelected() || subID.getText() == null || subID.getText().isBlank()) {
			idStr = "0";
		} else {
			idStr = subID.getText();
			if (!idStr.matches("\\d+")) {
				errors.append("Please enter a valid subscriber ID\n");
				valid = false;
				idStr = "0";
			}
		}

		// 4) Contact fields
		if (LoggedUser.getType() == UserType.SUBSCRIBER) {
			idStr = String.valueOf(LoggedUser.getId());
			email = (sub != null) ? sub.getEmail() : "";
			phone = (sub != null) ? sub.getPhone() : "";
		} else {
			email = orderEmail.getText();
			if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				errors.append("Please enter a valid Email\n");
				valid = false;
			}

			phone = (phoneStart.getValue() != null ? phoneStart.getValue() : "") + phoneNumber.getText();
			if (phone == null || phone.length() != 10 || !phone.matches("\\d+")) {
				errors.append("Please enter a valid 10-digit phone number\n");
				valid = false;
			}
		}

		// 5) Time selection
		String hourStr = orderHours.getValue();
		if (hourStr == null) {
			errors.append("Please select a time\n");
			valid = false;
		} else {
			selected = LocalTime.parse(hourStr);
		}

		// 6) Availability check + suggestions
		List<Integer> diners = dinersByTime.get(selected);
		diners.add(Integer.parseInt(amountStr));
		StringBuilder suggestions = new StringBuilder();
		if (!isAvailable(diners)) {
			errors.append("Chosen time isn't available, please choose another\n");
			valid = false;
			int groupSize = Integer.parseInt(amountStr);
			diners.remove(diners.size() - 1);
			LocalTime plus = selected.plusMinutes(30);
			diners = dinersByTime.get(plus);
			if (!(date.equals(LocalDate.now()) && plus.isBefore(LocalTime.now().plusHours(1))) && diners != null) {
				diners.add(groupSize);
				if (isAvailable(diners)) {
					suggestions.append("• ").append(plus).append("\n");
				}
				diners.remove(diners.size() - 1);
			}
			LocalTime minus = selected.minusMinutes(30);
			diners = dinersByTime.get(minus);
			if (!(date.equals(LocalDate.now()) && minus.isBefore(LocalTime.now().plusHours(1))) && diners != null) {
				diners.add(groupSize);
				if (isAvailable(diners)) {
					suggestions.append("• ").append(minus).append("\n");
				}
				diners.remove(diners.size() - 1);
			}

			if (suggestions.length() > 0) {
				errors.append("Suggested times:\n").append(suggestions);
			}
		}
		if (!valid) {
			showAlert("Reservation Failure", errors.toString());
			return;
		}
		Reservation r = new Reservation(date, amount, Integer.parseInt(idStr), today, selected, phone, Status.CONFIRMED, email);
		BistroRequest req = new BistroRequest(BistroCommand.ADD_RESERVATION, r);
		Main.client.accept(req);
		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			showAlert("Reservation Success", "Reservation successfully placed!");
		} else {
			showAlert("Error", "Failed placing the order");
		}
	}

	/**
	 * Rebuilds and populates the available time slots for the given date.
	 * <p>
	 * The method fetches:
	 * </p>
	 * <ul>
	 *   <li>The diners-per-time mapping via {@link Restaurant#buildDinersByTime(LocalDate)}</li>
	 *   <li>The table capacities via {@link Restaurant#getTableSizes()}</li>
	 * </ul>
	 *
	 * <p>
	 * Only time slots that satisfy both conditions are shown:
	 * </p>
	 * <ul>
	 *   <li>If {@code date} is today, the slot must be at least one hour from now</li>
	 *   <li>The current diners list for that slot can be seated using {@link #isAvailable(List)}</li>
	 * </ul>
	 *
	 * @param date the reservation date for which to compute and display available times
	 */
	private void showOnlyAvailableTime(LocalDate date) {
		orderHours.getItems().clear();
		dinersByTime = Restaurant.buildDinersByTime(date);
		tablesSizes = Restaurant.getTableSizes();

		for (LocalTime time : dinersByTime.keySet()) {
			List<Integer> diners = dinersByTime.get(time);
			if (!(date.equals(LocalDate.now()) && time.isBefore(LocalTime.now().plusHours(1))) && isAvailable(diners)) {
				orderHours.getItems().add(time.toString());
			}
		}
	}

	/**
	 * Determines whether the restaurant can accommodate the given diner groups in a
	 * single time slot using the currently loaded {@link #tablesSizes}.
	 * <p>
	 * The algorithm assumes:
	 * </p>
	 * <ul>
	 *   <li>{@link #tablesSizes} is sorted in ascending order.</li>
	 *   <li>Each diner group requires one table with capacity {@code >= group size}.</li>
	 *   <li>Tables are used at most once per time slot.</li>
	 * </ul>
	 *
	 * @param diners a sorted list of diner group sizes occupying that time slot
	 * @return {@code true} if all groups can be seated; otherwise {@code false}
	 */
	private boolean isAvailable(List<Integer> diners) {
		int i = 0;

		if (diners.size() > tablesSizes.size()) {
			return false;
		}

		for (int num : diners) {
			boolean found = false;

			while (i < tablesSizes.size()) {
				if (num <= tablesSizes.get(i)) {
					found = true;
					i++;
					break;
				}
				i++;
			}

			if (i == tablesSizes.size() && !found) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the restaurant is closed on the given date.
	 * <p>
	 * Results are cached in {@link #closedCache} because DatePicker cell rendering
	 * may call this method many times for the same month.
	 * </p>
	 *
	 * <p>
	 * A day is considered closed when:
	 * </p>
	 * <ul>
	 *   <li>Opening hours could not be fetched (null or invalid response), or</li>
	 *   <li>Either opening or closing time is {@code null}, or</li>
	 *   <li>Opening time equals closing time (meaning "no working hours").</li>
	 * </ul>
	 *
	 * @param date the date to check
	 * @return {@code true} if the restaurant is closed; otherwise {@code false}
	 */
	private boolean isClosedDay(LocalDate date) {
		return closedCache.computeIfAbsent(date, d -> {
			LocalTime[] hours = Restaurant.getOpeningTime(d);
			if (hours == null || hours.length < 2) {
				return true;
			}
			LocalTime open = hours[0];
			LocalTime close = hours[1];
			return open == null || close == null || open.equals(close);
		});
	}

	/**
	 * Handles the "Back" button click event.
	 * <p>
	 * Navigates back to the main menu screen.
	 * </p>
	 *
	 * @param event the action event triggered by clicking the back button
	 */
	@FXML
	void back(ActionEvent event) {
		try {
			Main.changeRoot(MainMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
