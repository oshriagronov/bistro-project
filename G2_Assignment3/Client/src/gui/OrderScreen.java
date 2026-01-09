package gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import logic.Subscriber;
import logic.UserType;
import logic.Worker;

/**
 * Controller for the Order.fxml view.
 * <p>
 * Handles user interaction for placing a new reservation, including:
 * <ul>
 * <li>Date and time selection</li>
 * <li>Diners amount selection</li>
 * <li>Guest / subscriber / worker flows</li>
 * <li>Input validation</li>
 * <li>Sending the reservation request to the server</li>
 * </ul>
 */
public class OrderScreen {

	/** FXML path for the order screen. */
	public static final String fxmlPath = "/gui/Order.fxml";

	/** Utility for generating a random confirmation code. */
	private final Random random = new Random();

	private Subscriber sub = null;
	private Worker worker = null;

	/** Alert object used to display success or failure messages to the user. */
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
	private HBox subHBOX;

	@FXML
	private TextField subID;

	@FXML
	private VBox subVbox;

	@FXML
	private VBox workerVbox;

	Map<LocalTime, List<Integer>> dinersByTime;
	List<Integer> tablesSizes;

	/**
	 * Initializes the Order screen.
	 * <p>
	 * This method is called automatically after the FXML file is loaded. It
	 * performs the following actions:
	 * <ul>
	 * <li>Configures the UI according to the logged-in user type</li>
	 * <li>Restricts date selection to today through one month ahead</li>
	 * <li>Initializes diners amount, phone prefixes, and default selections</li>
	 * <li>Updates available reservation times when the date changes</li>
	 * </ul>
	 */
	@FXML
	public void initialize() {
		if (LoggedUser.getType() == UserType.SUBSCRIBER) {
			setupSubscriber();
		} else if (LoggedUser.getType() == UserType.EMPLOYEE || LoggedUser.getType() == UserType.MANAGER) {
			setupWorkerView();
		} else {
			setupGuestView();
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

				if (item.isBefore(today) || item.isAfter(maxDate)) {
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
				orderHours.getSelectionModel().selectFirst();
			}
		});

		orderDate.setValue(LocalDate.now());
		showOnlyAvailableTime(LocalDate.now());

		phoneStart.getItems().clear();
		phoneStart.getItems().addAll("050", "052", "053", "054", "055", "058");

		orderHours.getSelectionModel().selectFirst();
		dinersAmmount.getSelectionModel().selectFirst();
		phoneStart.getSelectionModel().selectFirst();
	}

	/**
	 * Configures the screen for a logged-in subscriber.
	 * <p>
	 * Fetches the subscriber details from the server and automatically fills the
	 * email, phone number, and subscriber ID fields. Guest-specific and
	 * worker-specific UI elements are hidden.
	 */
	@FXML
	public void setupSubscriber() {
		int id = LoggedUser.getId();

		BistroRequest request = new BistroRequest(BistroCommand.GET_SUB, id);
		Main.client.accept(request);

		BistroResponse response = Main.client.getResponse();
		if (response == null) {
			setupGuestView();
			return;
		}

		Object data = response.getData();
		if (data != null) {
			this.sub = (Subscriber) data;

			orderEmail.setText(sub.getEmail());

			String p = sub.getPhone();
			if (p != null && p.length() == 10) {
				phoneStart.setValue(p.substring(0, 3));
				phoneNumber.setText(p.substring(3));
			}

			subID.setText(String.valueOf(sub.getSubscriberId()));

			nonSubVbox.setVisible(false);
			workerVbox.setVisible(false);
		}
	}

	/**
	 * Configures the screen for a guest user.
	 * <p>
	 * Guest users must manually provide contact information (email and phone).
	 * Subscriber-specific and worker-specific UI elements are hidden.
	 */
	private void setupGuestView() {
		nonSubVbox.setVisible(true);
		workerVbox.setVisible(false);
	}

	/**
	 * Configures the screen for a logged-in worker or manager.
	 * <p>
	 * Workers can place reservations without entering personal contact details.
	 * Guest fields remain visible while worker-related controls are enabled.
	 */
	private void setupWorkerView() {
		nonSubVbox.setVisible(true);
		workerVbox.setVisible(true);
	}

	/**
	 * Handles the subscriber CheckBox click event.
	 * <p>
	 * Toggles the visibility of the subscriber ID input field.
	 *
	 * @param e the ActionEvent triggered by clicking the CheckBox
	 */
	@FXML
	public void checkClicked(ActionEvent e) {
		subHBOX.setVisible(checkBox.isSelected());
	}

	/**
	 * Displays an informational alert dialog to the user.
	 *
	 * @param title the title of the alert window
	 * @param body  the message displayed inside the alert
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
	 * Validates all user inputs including:
	 * <ul>
	 * <li>Reservation date</li>
	 * <li>Reservation time (must be at least one hour in advance)</li>
	 * <li>Diners amount</li>
	 * <li>Email and phone number (for guests)</li>
	 * <li>Subscriber ID (if applicable)</li>
	 * </ul>
	 *
	 * If validation fails, an error alert is displayed. If validation succeeds, a
	 * reservation is created and sent to the server.
	 *
	 * @param event the ActionEvent triggered by clicking the Order button
	 */
	@FXML
	public void clickOrder(ActionEvent event) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourFromNow = LocalDateTime.now().plusHours(1);
		;

		int amount = 0;
		String hours;
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

		// 4) Subscriber ID (if checkbox selected)
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

		// 5-6) Contact fields
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
		String hourStr = orderHours.getValue();
		if (hourStr == null) {
			errors.append("Please select a time\n");
			valid = false;
		} else {
			selected = LocalTime.parse(hourStr);
		}
		
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
		int confirmationCode = random.nextInt(90000) + 10000;

		Reservation r = new Reservation(date, amount, confirmationCode, Integer.parseInt(idStr), today, selected, phone,
				email);

		BistroRequest req = new BistroRequest(BistroCommand.ADD_RESERVATION, r);
		Main.client.accept(req);
		BistroResponse response = Main.client.getResponse();
		if ((response != null && response.getStatus() == BistroResponseStatus.SUCCESS)) {
			showAlert("Reservation Success", "Reservation successfully placed!");
		}
		else
			showAlert("Error", "Failed placing the order");
	}

	/**
	 * Populates the time ComboBox with only times that have enough available
	 * tables.
	 *
	 * @param date the reservation date for which to calculate available times
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
	 * Checks whether the restaurant can accommodate a list of diner groups using
	 * the available table sizes.
	 *
	 * @param diners     a list of diner group sizes already occupying that timeslot
	 * @param tableSizes a list of available table sizes
	 * @return {@code true} if the diners can be placed into the tables, otherwise
	 *         {@code false}
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
	 * Handles the action when the "Back to MainMenu" button is clicked. Navigates
	 * the application back to the main menu screen.
	 *
	 * @param event the ActionEvent triggered by clicking the Back button
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
