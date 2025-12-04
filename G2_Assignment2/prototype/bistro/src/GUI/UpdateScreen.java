/**
 * Controller class for the UpdateScreen.fxml view.
 * This class allows users to search for an existing reservation using 
 * an order number, view the current details, and submit updates for 
 * the reservation date and the number of diners.
 */
package GUI;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import client.BistroClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class UpdateScreen {
	
	/** Alert object used to display success or failure messages to the user. */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);
	
	@FXML
	private HBox amountHbox;

	@FXML
	private HBox dateHbox;

	@FXML
	private ComboBox<String> dinersAmount;

	@FXML
	private Button menuBTN;

	@FXML
	private DatePicker orderDate;

	@FXML
	private TextField orderNumber;

	@FXML
	private Button searchBTN;

	@FXML
	private Button submitBTN;

	/** Container for the reservation information fields (Date and Diners Amount). */
	@FXML
	private VBox infoVbox;

	/**
	 * Initializes the controller. This method is called automatically after the FXML 
	 * file has been loaded.
	 * It hides the update fields initially and configures the DatePicker 
	 * to restrict date selection to within one month from today.
	 */
	@FXML
	public void initialize() {
		// Hide the update fields until a successful search is performed
		infoVbox.setVisible(false);
		
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
		dinersAmount.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			dinersAmount.getItems().add(String.valueOf(i));
		}
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
	 * Handles the action when the "Search" button is clicked.
	 * Sends the entered order number to the server to retrieve reservation details.
	 * If the order is found, the update fields are displayed and populated with 
	 * the existing reservation details. Otherwise, an error alert is shown.
	 * * @param event The ActionEvent triggered by the Search button.
	 */
	@FXML
	void search(ActionEvent event) {
		List<String> search = new ArrayList<>();
		String order_number;
		
		// Prepare search command for the server
		search.add("search");
		order_number = orderNumber.getText();
		search.add(order_number);
		
		// Send search request to the server via the client controller
		Main.client.accept(search); 
		
		// Retrieve the ordered data returned from the client (assumes static variable usage)
		search = BistroClient.orderedReturned;
		
		if (search != null && search.size() >= 2) {
			// Success: Display fields and populate with returned data
			infoVbox.setVisible(true);
			orderDate.setValue(LocalDate.parse(search.get(0))); // Index 0: Date
			dinersAmount.setValue(search.get(1)); // Index 1: Diners Amount
		} else {
			// Failure: Hide fields and show error
			infoVbox.setVisible(false);
			showAlert("Search Failure", "Please enter a valid order number.");
		}
	}

	/**
	 * Handles the action when the "Submit" button is clicked after searching.
	 * Validates the updated date and diners amount. 
	 * If valid, sends the updated information to the server to modify the reservation.
	 * * @param event The ActionEvent triggered by the Submit button.
	 */
	@FXML
	void submit(ActionEvent event) {
		StringBuilder str = new StringBuilder();
		boolean check = true;
		List<String> info = new ArrayList<>();
		
		info.add("update"); // 1. Add "update" command flag
		
		String order_number;
		LocalDate date = orderDate.getValue();
		order_number = orderNumber.getText();
		
		// 2. Validate Order Number
		if (order_number == null || order_number.isBlank()) {
			check = false;
			str.append("Please enter an order number\n");
		} else {
			info.add(order_number); // 2. Add order number
		}
		
		// 3. Validate Date
		if (date == null) {
			check = false;
			str.append("Please pick a date\n");
		} else {
			info.add(date.toString()); // 3. Add reservation date
		}
		
		// 4. Validate Diners Amount
		if (dinersAmount.getValue() == null) {
			check = false;
			str.append("Please choose the diners amount\n");
		} else {
			info.add(dinersAmount.getValue().toString()); // 4. Add diners amount
		}
		
		// Final Check: Display errors or process update
		if (!check) {
			showAlert("Update Failure", str.toString());
		} else {
			showAlert("Update Success", "Reservation successfully updated.");
			System.out.println("INFO ARRAYLIST = " + info);
			
			// Send the update command and new details to the server
			Main.client.accept(info);
		}
	}

	/**
	 * Handles the action when the "Back to Menu" button is clicked.
	 * Navigates the application back to the main menu screen.
	 * * @param event The ActionEvent triggered by the Back button.
	 */
	@FXML
	void backToMenu(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot("MainMenu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}