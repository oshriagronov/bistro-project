package gui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import communication.BistroCommand;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import communication.RequestFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import employee.employeeMenu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import logic.LoggedUser;
import logic.Reservation;
import logic.Status;
import logic.Subscriber;
import logic.UserType;
import subscriber.SubscriberScreen;

/**
 * Controller for the {@code Order.fxml} view.
 *
 * This screen allows placing a new reservation by selecting a date, a time
 * slot, and the number of diners. It supports different flows depending on the
 * logged-in user type:
 * - Guest: must enter email and phone number.
 * - Subscriber: details are fetched from the server and the contact fields are
 *   auto-filled.
 * - Employee/Manager: can place reservations with worker controls enabled
 *   (while guest fields may remain visible per UI design).
 *
 * Availability is calculated per 30-minute time slot by:
 * 1) Fetching opening/closing hours for the chosen date.
 * 2) Fetching existing reservations per slot (through
 *    {@link Restaurant#buildDinersByTime(LocalDate)}).
 * 3) Checking if the current tables configuration can accommodate the chosen
 *    group size.
 *
 * The DatePicker disables dates that are out of range (today..today+1 month)
 * and dates where the restaurant is closed. Closed-day checks are cached for
 * performance.
 */
public class OrderScreen {

	/** FXML path for the order screen. */
	public static final String fxmlPath = "/gui/Order.fxml";
	private boolean suppressDateListener = false;

	/** Loaded subscriber data when the logged-in user is a subscriber. */
	private Subscriber sub = null;

	/** Alert dialog used to display information and error messages. */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	private Button backBtn;
	@FXML
	private VBox btnVbox;
	@FXML
	private CheckBox checkBox;
	@FXML
	private ComboBox<String> dinersAmmount;
	@FXML
	private VBox nonSubVbox;
	@FXML
	private Button orderBtn;
	@FXML
	private DatePicker orderDate;
	@FXML
	private TextField orderEmail;
	@FXML
	private ComboBox<String> orderHours;
	@FXML
	private TextField phoneNumber;
	@FXML
	private ComboBox<String> phoneStart;
	@FXML
	private VBox subVbox;
	@FXML
	private VBox workerVbox;

	/**
	 * Mapping from a reservation time slot to a sorted list of diner group sizes
	 * already occupying that slot. Built by
	 * {@link Restaurant#buildDinersByTime(LocalDate)}.
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
	private final EventListener tableListener = t -> {
		tablesSizes = Restaurant.getTableSizes();

		Platform.runLater(() -> {
			LocalDate d = orderDate.getValue();
			if (d == null)
				return;

			populateAvailableTimesForCurrentAmount(d);
			handleNoTimesUI(d);
		});
	};
	private final EventListener scheduleListener = t -> updateAvailableTime(t);

	/**
	 * Initializes the Order screen.
	 *
	 * Called automatically after the FXML is loaded. This method:
	 * - Configures the view according to the logged-in user type.
	 * - Sets a DatePicker cell factory that disables:
	 *   - Past dates.
	 *   - Dates more than one month ahead.
	 *   - Dates where the restaurant is closed.
	 * - Initializes ComboBoxes (diners amount, phone prefix) and default
	 *   selections.
	 * - Updates available time slots whenever the date changes.
	 * - Selects the first open day and first available time slot by default.
	 */
	@FXML
	public void initialize() {
		orderBtn.setDisable(true);
		orderHours.setDisable(true);
		orderHours.getItems().clear();
		orderHours.setPromptText("Loading...");
		EventBus.getInstance().subscribe(EventType.TABLE_CHANGED, tableListener);
		EventBus.getInstance().subscribe(EventType.SCHEDULE_CHANGED, scheduleListener);
		if (LoggedUser.getType() == UserType.SUBSCRIBER) {
			this.sub = ScreenSetup.setupSubscriber(nonSubVbox, workerVbox, null);
		} else if (LoggedUser.getType() == UserType.EMPLOYEE || LoggedUser.getType() == UserType.MANAGER) {
			ScreenSetup.setupWorkerView(nonSubVbox, workerVbox, null);
		} else {
			ScreenSetup.setupGuestView(nonSubVbox, workerVbox, null);
		}
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

		orderDate.valueProperty().addListener((obs, oldDate, newDate) -> {
			if (newDate == null)
				return;
			if (suppressDateListener)
				return;

			orderDate.setDisable(true);
			orderBtn.setDisable(true);
			orderHours.setDisable(true);
			orderHours.getItems().clear();
			orderHours.setPromptText("Loading...");

			Task<Void> task = new Task<>() {
				@Override
				protected Void call() {
					try {
						Map<LocalTime, List<Integer>> map = Restaurant.buildDinersByTime(newDate);
						List<Integer> tables = Restaurant.getTableSizes();

						Platform.runLater(() -> {
							dinersByTime = map;
							tablesSizes = tables;

							populateAvailableTimesForCurrentAmount(newDate);
							handleNoTimesUI(newDate);

							boolean hasTimes = !orderHours.getItems().isEmpty();
							orderHours.setDisable(!hasTimes);
							orderBtn.setDisable(!hasTimes);
							orderHours.setPromptText(null);

							if (hasTimes)
								orderHours.getSelectionModel().selectFirst();
						});
					} finally {
						Platform.runLater(() -> orderDate.setDisable(false));
					}
					return null;
				}
			};

			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
		});
		
		

		dinersAmmount.valueProperty().addListener((obs, o, v) -> {
			LocalDate d = orderDate.getValue();
			if (d != null && v != null) {
				populateAvailableTimesForCurrentAmount(d);
				handleNoTimesUI(d);
				if (!orderHours.getItems().isEmpty()) {
					orderHours.getSelectionModel().selectFirst();
				}
			}
		});

		dinersAmmount.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			dinersAmmount.getItems().add(String.valueOf(i));
		}

		phoneStart.getItems().clear();
		phoneStart.getItems().addAll("050", "052", "053", "054", "055", "058");
		orderHours.getSelectionModel().selectFirst();
		phoneStart.getSelectionModel().selectFirst();
		dinersAmmount.getSelectionModel().selectFirst();
		loadInitialDataInBackground();
	}

	/**
	 * Loads the first available date and its reservations in the background to
	 * keep the UI responsive.
	 */
	private void loadInitialDataInBackground() {
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {

				LocalDate today = LocalDate.now();
				LocalDate firstOpen = findNextOpenDate(today);

				if (firstOpen == null) {
					Platform.runLater(() -> {
						orderDate.setDisable(true);
						orderHours.setDisable(true);
						orderBtn.setDisable(true);
						showAlert("Closed", "No available days in the next month.");
					});
					return null;
				}

				Map<LocalTime, List<Integer>> map = Restaurant.buildDinersByTime(firstOpen);
				List<Integer> tables = Restaurant.getTableSizes();

				Platform.runLater(() -> {
					dinersByTime = map;
					tablesSizes = tables;

					suppressDateListener = true;
					orderDate.setValue(firstOpen);
					suppressDateListener = false;

					populateAvailableTimesForCurrentAmount(firstOpen);
					handleNoTimesUI(firstOpen);

					orderHours.setDisable(orderHours.getItems().isEmpty());
					orderBtn.setDisable(orderHours.getItems().isEmpty());
					orderHours.setPromptText(null);

					if (!orderHours.getItems().isEmpty()) {
						orderHours.getSelectionModel().selectFirst();
					}
				});

				return null;
			}
		};

		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Finds the next open date starting from {@code start} (inclusive), scanning
	 * forward up to one month ahead.
	 *
	 * @param start the first date to check
	 * @return the first date that is not closed; {@code null} if none are open in
	 *         the next month window
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
	 *
	 * If selected, shows the subscriber-id input field; otherwise hides it.
	 *
	 * @param e the action event triggered by clicking the checkbox
	 */
	@FXML
	public void checkClicked(ActionEvent e) {

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
	 *
	 * Validates inputs:
	 * - Date is selected.
	 * - Diners amount is selected.
	 * - Subscriber ID is numeric (if checkbox is selected).
	 * - Email is valid (for non-subscribers).
	 * - Phone is a valid 10-digit number (for non-subscribers).
	 * - Time slot is selected.
	 * - Selected time slot has enough availability based on current table sizes.
	 *
	 * If the selected slot is not available, the method attempts to suggest the
	 * nearest adjacent slots (+30/-30 minutes) when they are valid candidates.
	 *
	 * On success, creates a {@link Reservation} and sends it to the server using
	 * {@link BistroCommand#ADD_RESERVATION}.
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
		int subId = 0;
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

		// 5) Time selection
		String hourStr = orderHours.getValue();
		if (hourStr == null) {
			errors.append("Please select a time\n");
			valid = false;
		} else {
			selected = LocalTime.parse(hourStr);
		}

		List<Integer> diners = dinersByTime.getOrDefault(selected, new ArrayList<>());
		List<Integer> test = new ArrayList<>(diners);
		test.add(amount);
		test.sort(Integer::compareTo);

		if (!Restaurant.isAvailable(test, tablesSizes)) {
			showAlert("Reservation Failure", "The selected time is no longer available.\nPlease choose another time.");

			// Refresh UI to a valid choice
			populateAvailableTimesForCurrentAmount(date);
			handleNoTimesUI(date);
			if (!orderHours.getItems().isEmpty()) {
				orderHours.getSelectionModel().selectFirst();
				orderBtn.setDisable(false);
			} else {
				orderHours.getSelectionModel().clearSelection();
				orderBtn.setDisable(true);
			}
			return;
		}

		// 4) Contact fields
		if (LoggedUser.getType() == UserType.SUBSCRIBER) {
			subId = LoggedUser.getId();
			email = sub.getEmail();
			phone = sub.getPhone();

		} else if ((LoggedUser.getType() == UserType.EMPLOYEE || LoggedUser.getType() == UserType.MANAGER)
				&& checkBox.isSelected()) {
			phone = "";
			phone += (String) phoneStart.getValue();
			phone += (String) phoneNumber.getText();
			email = orderEmail.getText();
			Subscriber foundSub = null;
			
			if (phone.length() ==  10) {
				Main.client.accept(RequestFactory.getSubscriberByPhone(phone));
				BistroResponse response = Main.client.getResponse();
				if (response.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) response.getData();
				}
			} else if (email != null && !email.isEmpty()) {
				Main.client.accept(RequestFactory.getSubscriberByEmail(email));
				BistroResponse response = Main.client.getResponse();
				if (response.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) response.getData();
				} else {
					showAlert("Error", "Please enter phone or email to place the order.");
				}
			}
			if (foundSub == null) {
			    showAlert("Error", "Subscriber not found. Please enter valid phone/email.");
			    return;
			}
			phone = foundSub.getPhone();
			email = foundSub.getEmail();
			subId = foundSub.getSubscriberId();

		} else {
			email = orderEmail.getText();

			String phonePrefix = phoneStart.getValue() != null ? phoneStart.getValue() : "";
			String phoneNumberText = phoneNumber.getText() != null ? phoneNumber.getText().trim() : "";

			boolean phoneProvided = !phoneNumberText.isBlank();
			boolean emailProvided = email != null && !email.isBlank();

			if (!emailProvided && !phoneProvided) {
				errors.append("Please enter at least an Email or a Phone number\n");
				valid = false;
			}

			if (emailProvided && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				errors.append("Please enter a valid Email\n");
				valid = false;
			}

			if (phoneProvided) {
				String fullPhone = phonePrefix + phoneNumberText;
				if (fullPhone.length() != 10 || !fullPhone.matches("\\d+")) {
					errors.append("Please enter a valid 10-digit phone number\n");
					valid = false;
				}
				phone = fullPhone;
			} else {
				phone = ""; 
			}

		}


		if (!valid) {
			showAlert("Form error", errors.toString());
			return;
		}

		Reservation r = new Reservation(date, amount, subId, today, selected, phone, Status.CONFIRMED, email);

		Main.client.accept(RequestFactory.addReservation(r));

		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			applyLocalReservationToMap(selected, amount);
			populateAvailableTimesForCurrentAmount(date);
			handleNoTimesUI(date);
			orderHours.getSelectionModel().selectFirst();
			showAlert("Reservation Success", "Your confirmation code is : " + response.getData());
		} else {
			showAlert("Error", "Failed placing the order");
		}
	}

	/**
	 * Populates available time slots for the selected date and diner count.
	 *
	 * @param date the date currently selected in the UI
	 */
	private void populateAvailableTimesForCurrentAmount(LocalDate date) {
		orderHours.getItems().clear();
		if (dinersByTime == null || tablesSizes == null)
			return;

		String amountStr = dinersAmmount.getValue();
		if (amountStr == null)
			return;
		int amount = Integer.parseInt(amountStr);

		for (LocalTime time : dinersByTime.keySet()) {
			if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now().plusHours(1)))
				continue;

			List<Integer> diners = dinersByTime.get(time);

			List<Integer> test = new ArrayList<>(diners);
			test.add(amount);
			test.sort(Integer::compareTo);

			if (Restaurant.isAvailable(test, tablesSizes)) {
				orderHours.getItems().add(time.toString());
			}
		}
	}

	/**
	 * Updates the UI state when no time slots are available for a given date.
	 *
	 * @param date the date currently selected in the UI
	 */
	private void handleNoTimesUI(LocalDate date) {
	    boolean hasTimes = orderHours != null && !orderHours.getItems().isEmpty();

	    if (!hasTimes) {
	        orderHours.getSelectionModel().clearSelection();
	        orderHours.setDisable(true);
	        orderBtn.setDisable(true);
	        orderHours.setPromptText("No available times for this day");
	        return;
	    }

	    orderHours.setDisable(false);
	    orderBtn.setDisable(false);
	    if (orderHours.getValue() == null) {
	        orderHours.getSelectionModel().selectFirst();
	    }
	    orderHours.setPromptText(null);
	}

	/**
	 * Checks whether the restaurant is closed on the given date.
	 *
	 * Results are cached in {@link #closedCache} because DatePicker cell
	 * rendering may call this method many times for the same month.
	 *
	 * A day is considered closed when:
	 * - Opening hours could not be fetched (null or invalid response), or
	 * - Either opening or closing time is {@code null}, or
	 * - Opening time equals closing time (meaning "no working hours").
	 *
	 * @param date the date to check
	 * @return {@code true} if the restaurant is closed; otherwise {@code false}
	 */
	private boolean isClosedDay(LocalDate date) {
		Boolean cached = closedCache.get(date);
		if (cached != null)
			return cached;

		LocalTime[] hours = Restaurant.getOpeningTime(date);

		if (hours == null || hours.length < 2) {
			return true;
		}

		LocalTime open = hours[0];
		LocalTime close = hours[1];
		boolean closed = open == null || close == null || open.equals(close);

		closedCache.put(date, closed);
		return closed;
	}

	/**
	 * Refreshes availability when the schedule or tables configuration changes.
	 *
	 * @param payload event payload from the {@link EventBus}
	 */
	private void updateAvailableTime(Object payload) {
		LocalDate selected = orderDate.getValue();
		if (selected == null)
			return;

		if (payload instanceof LocalDate) {
			LocalDate changedDate = (LocalDate) payload;

			closedCache.remove(changedDate);

			if (changedDate.equals(selected)) {
				Platform.runLater(() -> {
					suppressDateListener = true;
					orderDate.setValue(null);
					suppressDateListener = false;
					orderDate.setValue(selected);
				});
			}
			return;
		}

		if (payload instanceof DayOfWeek) {
			DayOfWeek changedDow = (DayOfWeek) payload;

			if (selected.getDayOfWeek() == changedDow) {
				closedCache.remove(selected);
				Platform.runLater(() -> {
					suppressDateListener = true;
					orderDate.setValue(null);
					suppressDateListener = false;
					orderDate.setValue(selected);
				});
			}
			return;
		}

		closedCache.clear();
		Platform.runLater(() -> {
			suppressDateListener = true;
			orderDate.setValue(null);
			suppressDateListener = false;
			orderDate.setValue(selected);
		});
	}

	/**
	 * Converts a {@link LocalTime} to minutes since midnight.
	 *
	 * @param t time to convert
	 * @return minutes since midnight
	 */
	private static int toMinutes(LocalTime t) {
	    return t.getHour() * 60 + t.getMinute();
	}

	/**
	 * Applies a successful reservation locally to keep the availability map
	 * consistent until a full refresh is fetched.
	 *
	 * @param selected the selected reservation start time
	 * @param amount   number of diners in the reservation
	 */
	private void applyLocalReservationToMap(LocalTime selected, int amount) {
	    if (dinersByTime == null) return;

	    int sMin = toMinutes(selected);
	    int eMin = sMin + 120; 

	    List<LocalTime> keys = new ArrayList<>(dinersByTime.keySet());

	    for (LocalTime t : keys) {
	        int tMin = toMinutes(t);
	        int tEnd = tMin + 120;

	        boolean overlaps = tMin < eMin && tEnd > sMin; 
	        if (!overlaps) continue;

	        List<Integer> slot = dinersByTime.getOrDefault(t, new ArrayList<>());
	        List<Integer> updated = new ArrayList<>(slot);
	        updated.add(amount);
	        updated.sort(Integer::compareTo);
	        dinersByTime.put(t, updated);
	    }
	}

    /**
     * Handles the action when the "Back to MainMenu" button is clicked.
     *
     * Navigates the application back to the main menu screen.
     *
     * @param event the action event triggered by the Back button
     */
	@FXML
	void back(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot(getBackFxmlPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Determines the correct destination screen based on the current user type.
     *
     * @return FXML path to navigate back to
     */
    private String getBackFxmlPath() {
        UserType type = LoggedUser.getType();
        if (type == UserType.SUBSCRIBER) {
            return SubscriberScreen.fxmlPath;
        }
        else if (type == UserType.EMPLOYEE || type == UserType.MANAGER) {
            return employeeMenu.fxmlPath;
        }
        return MainMenuScreen.fxmlPath;
    }

	/**
	 * Unsubscribes from event listeners when the screen is closed.
	 */
	public void onClose() {
		EventBus.getInstance().unsubscribe(EventType.TABLE_CHANGED, tableListener);
		EventBus.getInstance().unsubscribe(EventType.SCHEDULE_CHANGED, scheduleListener);

	}
}
