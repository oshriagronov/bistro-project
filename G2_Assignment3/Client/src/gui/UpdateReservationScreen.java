/**
 * Controller class for the UpdateScreen.fxml view.
 * This class allows users to search for an existing reservation using 
 * an order number, view the current details, and submit updates for 
 * the reservation date and the number of diners.
 */
package gui;
import java.time.LocalDate;
import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import employee.employeeMenu;
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
import logic.LoggedUser;
import logic.Reservation;
import logic.UserType;
import subscriber.SubscriberScreen;

public class UpdateReservationScreen {
	public static final String fxmlPath = "/gui/UpdateReservation.fxml";
	/** Alert object used to display success or failure messages to the user. */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);
	
	    @FXML
	    private HBox amountHbox;

	    @FXML
	    private HBox dateHbox;

		@FXML
		private ComboBox<String> dinersAmount;

	    @FXML
	    private VBox infoVbox;

		@FXML
	    private VBox ordersVBox;

	    @FXML
	    private Button backBtn;

	    @FXML
	    private DatePicker orderDate;

	    @FXML
	    private TextField phoneNum;

	    @FXML
	    private Button searchBTN;

	    @FXML
	    private Button submitBTN;

		@FXML
		private ComboBox<String> orderNumber;

	    
	
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
		ordersVBox.setVisible(false);
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

	    String phone = phoneNum.getText().trim();

	    if (phone.isEmpty()) {
	        showAlert("Search Failure", "Please enter a phone number.");
	        return;
	    }

	    // Send request to server
	    Main.client.accept(
	        new BistroRequest(BistroCommand.GET_ACTIVE_RESERVATIONS_BY_PHONE, phone)
	    );

	    BistroResponse response = Main.client.getResponse();

	    if (response.getStatus() != BistroResponseStatus.SUCCESS) {
	        showAlert("Search Failure", "No reservations found for this phone number.");
	        return;
	    }

	    ArrayList<Reservation> reservations = (ArrayList<Reservation>) response.getData();

	    if (reservations == null || reservations.isEmpty()) {
	        showAlert("Search Failure", "No reservations found.");
	        return;
	    }

	    // Show the order number dropdown
	    ordersVBox.setVisible(true);
	    orderNumber.getItems().clear();

	    for (Reservation r : reservations) {
	        orderNumber.getItems().add(String.valueOf(r.getOrderNumber()));
	    }
	}


	@FXML
	void cancelReservation(ActionEvent event) {

	    String selected = orderNumber.getValue();

	    if (selected == null) {
	        showAlert("Cancellation Failed", "Please choose an order number first.");
	        return;
	    }

	    int orderNum = Integer.parseInt(selected);

	    // Send cancel request to server
	    Main.client.accept(
	        new BistroRequest(BistroCommand.CANCEL_RESERVATION, orderNum)
	    );

	    BistroResponse res = Main.client.getResponse();

	    if (res.getStatus() == BistroResponseStatus.SUCCESS) {
	        showAlert("Reservation Canceled", "Reservation #" + orderNum + " has been canceled successfully.");
	    } else {
	        showAlert("Cancellation Failed", "Unable to cancel reservation.");
	    }
	}



	public void CreateTable(ArrayList<Reservation> reservations) { 
		TableView<Reservation> table = new TableView<>();
		TableColumn<Reservation, String> idCol = new TableColumn<>("Order Number");
		TableColumn<Reservation, String> orderDateCol = new TableColumn<>("Order Date");
		TableColumn<Reservation, String> NumOfGuestsCol = new TableColumn<>("Number Of Guests");

		for(Reservation r : reservations) {// for each reservation found create a row in the table
			idCol.setCellValueFactory(new PropertyValueFactory<>("order_number"));
			orderDateCol.setCellValueFactory(new PropertyValueFactory<>("order_date"));
			NumOfGuestsCol.setCellValueFactory(new PropertyValueFactory<>("number_of_guests"));
			table.getColumns().addAll(idCol, orderDateCol, NumOfGuestsCol);
			table.getItems().add(r);
		}
		Stage stage = new Stage();
		stage.setScene(new Scene(table, 600, 400));
		stage.show();
	}
	
	@FXML
	void choose(ActionEvent event) {
		Reservation reservation;
		String order_number;
		order_number = orderNumber.getValue(); // get the selected order number from the combobox
		// Find the reservation object corresponding to the selected order number with the server
		Main.client.accept(order_number);
		// another example on how to get data after request, should make try & catch for the casting
		reservation = (Reservation)Main.client.getResponse().getData();
		infoVbox.setVisible(true);
		orderDate.setValue(reservation.getOrderDate());
		dinersAmount.setValue(String.valueOf(reservation.getNumberOfGuests()));
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
		order_number =  (String) orderNumber.getValue();
		number_of_guests = (String) dinersAmount.getValue();
		// 2. Validate Order Number
		if (order_number == null || order_number.isBlank()) {
			check = false;
			str.append("Please enter an order number\n");
		}
		// 3. Validate Date
		if (date == null) {
			check = false;
			str.append("Please pick a date\n");
		}
		// 4. Validate Diners Amount
		if (number_of_guests == null) {
			check = false;
			str.append("Please choose the diners amount\n");
		}
		// Final Check: Display errors or process update
		if (!check) {
			showAlert("Update Failure", str.toString());
		} else {
			showAlert("Update Success", "Reservation successfully updated.");
			// Send the update command and new details to the server
			//Reservation updated = new Reservation(date, order_number,number_of_guests,);
			//Main.client.accept(updated);
		}
}

	@FXML
	void back(ActionEvent event) {
		try {
			// Use the static method in Main to switch the scene root
			Main.changeRoot(getBackFxmlPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
}