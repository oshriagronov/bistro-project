/**
 * Controller class for the OrderScreen.fxml view.
 * This class handles the user input for placing a new reservation, 
 * including validating the date, time, contact information, and subscriber ID,
 * and then sending the reservation request to the server.
 */
package gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

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
import logic.Reservation;

public class OrderScreen {
	public static final String fxmlPath = "/gui/Order.fxml";
	/** Utility for generating a random confirmation code. */
	private Random random = new Random();
	
	/** Alert object used to display success or failure messages to the user. */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	private ComboBox<String> dinersAmmount;

	@FXML
	private Button orderBtn;

	@FXML
	private Button backBtn;

	@FXML
	private DatePicker orderDate;

	@FXML
	private TextField orderEmail;

	@FXML
	private ComboBox<String> orderHours;

	@FXML
	private ComboBox<String> orderMinutes;

	@FXML
	private TextField phoneNumber;

	@FXML
	private ComboBox<String> phoneStart;

	@FXML
	private CheckBox checkBox;

	@FXML
	private TextField subID;

	@FXML
	private HBox subHBOX;

	/**
	 * Initializes the controller. This method is called automatically after the FXML 
	 * file has been loaded.
	 * It sets up the available options for ComboBoxes (diners amount, hours, minutes, phone prefixes)
	 * and configures the DatePicker to only allow future dates within the next month.
	 */
	@FXML
	public void initialize() {
		// Hide the subscriber ID input field initially
		subHBOX.setVisible(false);
		
		// Configure the DatePicker to restrict selection: only today up to one month ahead is allowed
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

			    // Disable dates before today or more than one month in the future
			    if (item.isBefore(today) || item.isAfter(maxDate)) {
			        setDisable(true);
			        setStyle("-fx-background-color: #ccc;");
			    } else {
			        setDisable(false);
			        setStyle("");
			    }
			}
		});
		
		// Initialize diners amount options (1–10)
		dinersAmmount.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			dinersAmmount.getItems().add(String.valueOf(i));
		}

		// Initialize reservation hours options (12:00–22:30, in half-hour increments)
		orderHours.getItems().clear();
		for (int i = 12; i < 23; i++) { // From 12:00 up to 22:xx
			orderHours.getItems().add(String.format("%02d", i));
		}

		// Initialize reservation minutes options (00, 30)
		orderMinutes.getItems().clear();
		for (int i = 0; i < 60; i += 30) {
			orderMinutes.getItems().add(String.format("%02d", i));
		}

		// Initialize phone prefix options
		phoneStart.getItems().clear();
		phoneStart.getItems().addAll("050", "052", "053", "054", "055", "058");
	}

	/**
	 * Handles the action when the subscriber CheckBox is clicked.
	 * Toggles the visibility of the subscriber ID input field (subHBOX).
	 * * @param e The ActionEvent triggered by the CheckBox.
	 */
	@FXML
	public void checkClicked(ActionEvent e) {
		if (checkBox.isSelected())
			subHBOX.setVisible(true);
		else
			subHBOX.setVisible(false);
	}

	/**
	 * Utility method to display a modal alert to the user.
	 * * @param title The title of the alert window.
	 * @param body The main content text to be displayed in the alert.
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Handles the action when the "Order" button is clicked.
	 * It validates all input fields (Date, Time, Diners, Email, Subscriber ID, Phone Number).
	 * If validation fails, an alert with error messages is shown.
	 * If successful, a new Reservation object is created, a confirmation code is generated,
	 * and the reservation is sent to the client controller for processing.
	 * * @param event The ActionEvent triggered by the Order button.
	 */
	@FXML
	public void clickOrder(ActionEvent event) {
		LocalDateTime now = LocalDateTime.now();
		// Reservation must be at least one hour from the current time
		LocalDateTime oneHourFromNow = now.plusHours(1); 
		int amount = 0, hours, minutes;
		String phone, ID = null, amountStr = "1";
		StringBuilder str = new StringBuilder();
		boolean check = true;
		LocalDate date;
		LocalDate today = LocalDate.now();
		
		date = orderDate.getValue();
		
		// 1. Validate Date selection
		if (date == null) {
			str.append("Please pick a reservation date\n");
			check = false;
		}
		
		// 2. Validate Diners Amount selection
		amountStr = dinersAmmount.getValue();
		if (amountStr == null || amountStr.isBlank()) {
			str.append("Please choose diners amount\n");
			check = false;
		} else
			amount = Integer.parseInt(amountStr); // safe now
		
		// 3. Validate Time selection and minimum time rule (1 hour ahead)
		if (orderHours.getValue() == null || orderMinutes.getValue() == null) {
			check = false;
			str.append("Please select reservation time\n");
		} else if (date != null) {
			hours = Integer.parseInt(orderHours.getValue());
			minutes = Integer.parseInt(orderMinutes.getValue());
			LocalDateTime selected = date.atTime(hours, minutes);
			
			// Check if selected time is less than one hour from now
			if (date.equals(today) && selected.isBefore(oneHourFromNow)) {
				check = false;
				str.append("Please select a time that is at least one hour ahead of now\n");
			}
		}
		
		// 4. Validate Email format
		if (!orderEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			str.append("Please enter a valid Email\n");
			check = false;
		}
		
		// 5. Validate Subscriber ID
		if (!checkBox.isSelected()) {
			ID = "0"; // Non-subscriber
		} else {
			ID = subID.getText();
			// Check if subscriber ID is provided and is exactly 5 digits
			if (ID == null || ID.length() != 5 || !ID.matches("\\d+")) {
				ID = "0";
				str.append("Please enter a valid 5-digit subscriber ID\n");
				check = false;
			}
		}

		// 6. Validate Phone Number (must be 7 digits and contain only numbers)
		phone = phoneNumber.getText();
		if (phone == null || phone.length() != 7 || !phone.matches("\\d+")) {
			str.append("Please enter a valid 7-digit phone number\n");
			check = false;
		}
		//TODO: fix the proccess according to the tables of the db
		// Final Check: Display errors or process reservation
		if (!check) {
			showAlert("Reservation Failure", str.toString());
		} else {
			showAlert("Reservation Success", "Reservation successfully placed!");
			
			// Generate a 5-digit confirmation code (10000 to 99999)
			int confirmation_code = random.nextInt(90000) + 10000;
			
			// Create Reservation object: date, amount, code, subscriber ID, today's date (for tracking)
			//Reservation r = new Reservation(date, amount, confirmation_code, Integer.parseInt(ID), today);
			
			// Send the reservation object to the client controller for server communication
			//Main.client.accept(r);
		}
	}

	/**
	 * Handles the action when the "Back to MainMenu" button is clicked.
	 * Navigates the application back to the main menu screen.
	 * * @param event The ActionEvent triggered by the Back button.
	 */
	@FXML
	void back(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot(MainMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}