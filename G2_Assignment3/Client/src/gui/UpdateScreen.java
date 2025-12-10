/**
 * Controller class for the UpdateScreen.fxml view.
 * This class allows users to search for an existing reservation using 
 * an order number, view the current details, and submit updates for 
 * the reservation date and the number of diners.
 */
package gui;
import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.Reservation;

public class UpdateScreen {
	
	/** Alert object used to display success or failure messages to the user. */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);
	
	    @FXML
	    private HBox amountHbox;

	    @FXML
	    private HBox dateHbox;

	    @FXML
	    private ComboBox<?> dinersAmount;

	    @FXML
	    private VBox infoVbox;

	    @FXML
	    private Button menuBTN;

	    @FXML
	    private DatePicker orderDate;

	    @FXML
	    private TextField phoneNum;

	    @FXML
	    private Button searchBTN;

	    @FXML
	    private Button submitBTN;

	    
	
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
		Reservation reservation;
		String phone_number;
		// Prepare search command for the server
		search.add("search");
		phone_number = phoneNum.getText();
		search.add(phone_number);
		// Send search request to the server via the client controller
		Main.client.accept(search); 
		// Retrieve the ordered data returned from the client (assumes static variable usage)
		search = BistroClient.orderedReturned; // need to return array list of all the orders found
		if (search != null) { 
			public void start(Stage stage) {
			        TableView<Reservation> table = new TableView<>();
			        TableColumn<Reservation, String> idCol = new TableColumn<>("Order Number");
			        TableColumn<Reservation, String> orderDateCol = new TableColumn<>("Order Date");
			        TableColumn<Reservation, String> NumOfGuestsCol = new TableColumn<>("Number Of Guests");
	
			        for(Reservation r : search) {
			        	idCol.setCellValueFactory(new PropertyValueFactory<>("order_number"));
			        	orderDateCol.setCellValueFactory(new PropertyValueFactory<>("order_date"));
			        	NumOfGuestsCol.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
	
			        	table.getColumns().addAll(idCol, orderDateCol, NumOfGuestsCol);
			        	
			        }
			        stage.setScene(new Scene(table, 600, 400));
			        stage.show();
			    }

			// Success: Display fields and populate with returned data
		
			infoVbox.setVisible(true);
			orderDate.setValue(reservation.getOrderDate());
			dinersAmount.setValue(number_of_guests);
		} else {
			// Failure: Hide fields and show error
			infoVbox.setVisible(false);
			showAlert("Search Failure", "Please enter a valid phone number.");
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
	
		String order_number,number_of_guests;
		LocalDate date = orderDate.getValue();
		order_number = orderNumber.getText();
		number_of_guests = dinersAmount.getValue();
		// 2. Validate Order Number
		if (order_number == null || order_number.isBlank()) {
			check = false;
			str.append("Please enter an order number\n");

		
		// 3. Validate Date
		if (date == null) {
			check = false;
			str.append("Please pick a date\n");

		
		// 4. Validate Diners Amount
		if (number_of_guests == null) {
			check = false;
			str.append("Please choose the diners amount\n");

		
		// Final Check: Display errors or process update
		if (!check) {
			showAlert("Update Failure", str.toString());
		} else {
			showAlert("Update Success", "Reservation successfully updated.");
			// Send the update command and new details to the server
			Reservation updated = new Reservation(date, order_number,number_of_guests,);
			Main.client.accept(updated);
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