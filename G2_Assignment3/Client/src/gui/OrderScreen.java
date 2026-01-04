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
import java.time.LocalTime;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
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

public class OrderScreen {
	public static final String fxmlPath = "/gui/Order.fxml";
	private static final UserType SUBSCRIBER = null;
	private static final UserType EMPLOYEE = null;
	/** Utility for generating a random confirmation code. */
	private Random random = new Random();
	private Subscriber sub;
	private Worker worker;

	/** Alert object used to display success or failure messages to the user. */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);

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
    private ComboBox<String> orderMinutes;

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


	/**
	 * Initializes the controller. This method is called automatically 
	 * file has been loaded.
	 * It sets up the available options for Comb
	 * and configures the DatePicker to only allow future dates within the next month.
	 */
    @FXML
    public void initialize() {
		
        if (LoggedUser.getType()==SUBSCRIBER) {
        	sub =  setupSubscriber();
        }
        else if (LoggedUser.getType()==EMPLOYEE) {
        	worker = setupWorkerView();
        }
        else {
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

        orderHours.getItems().clear();
        for (int i = 12; i < 23; i++) {
            orderHours.getItems().add(String.format("%02d", i));
        }

        orderMinutes.getItems().clear();
        for (int i = 0; i < 60; i += 30) {
            orderMinutes.getItems().add(String.format("%02d", i));
        }

        phoneStart.getItems().clear();
        phoneStart.getItems().addAll("050", "052", "053", "054", "055", "058");
    }

	/**
     * Configures the screen for a logged-in subscriber.
     * Fetches subscriber details from the database and auto-fills the UI fields.
     */
    @FXML
    public Subscriber setupSubscriber() {

        // Get subscriber ID from LoggedUser
        int id = LoggedUser.getId();

        BistroRequest request= new BistroRequest(BistroCommand.GET_SUB, id);
        Main.client.accept(request);
        
        BistroResponse response= Main.client.getResponse();

        if (response == null) {
            // If something went wrong, fallback to guest mode
            setupGuestView();
            return null;
        }
        Object data = response.getData();
        if(data!= null) {
        	Subscriber sub=(Subscriber)data;
        	 // Hide fields that are not relevant for subscribers
            nonSubVbox.setVisible(false);
            workerVbox.setVisible(false);
            subHBOX.setVisible(false); // subscriber does NOT need to enter subscriber ID manually
            return sub;
        }
        return null;
    }

	/**
	 * Configures the screen for a non‑logged guest user.
	 * <p>
	 * This mode is used when the user arrives from the main menu without logging in.
	 * All contact fields (phone and email) remain visible because the guest must
	 * manually provide this information in order to place a reservation.
	 * Worker‑specific and subscriber‑specific fields are hidden.
	 */
	private void setupGuestView() {
	    nonSubVbox.setVisible(true);  
	    workerVbox.setVisible(false);  
	    subHBOX.setVisible(true);     
	}
	
	/**
	 * Configures the screen for a logged‑in worker.
	 * <p>
	 * Workers do not need to enter personal contact information when placing
	 * a reservation for a customer. Therefore, guest fields are hidden while
	 * worker‑related controls remain visible.
	 */
	private Worker setupWorkerView() {
		// Get worker ID from LoggedUser
        int id = LoggedUser.getId();
        BistroRequest request= new BistroRequest(BistroCommand.GET_WORKER, id);
        Main.client.accept(request);
        
        BistroResponse response= Main.client.getResponse();

        if (response == null) {
            // If something went wrong, fallback to guest mode
            nonSubVbox.setVisible(false);  
	    	workerVbox.setVisible(false);
	    	subHBOX.setVisible(false);
        }
        Object data = response.getData();
        if(data!= null) {
        	Worker worker=(Worker)data;
        	 // Hide fields that are not relevant for subscribers
            nonSubVbox.setVisible(true);
            workerVbox.setVisible(true);
            subHBOX.setVisible(false); // worker does NOT need to enter subscriber ID manually
			return worker;
        }
		return null;
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
		int amount=0, hours, minutes;
		String phone, email, ID = null, amountStr = "1";
		StringBuilder str = new StringBuilder();
		boolean check = true;
		LocalDate date;
		LocalDate today = LocalDate.now();
		LocalDateTime selected= null;
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
			selected = date.atTime(hours, minutes);
			
			// Check if selected time is less than one hour from now
			if (date.equals(today) && selected.isBefore(oneHourFromNow)) {
				check = false;
				str.append("Please select a time that is at least one hour ahead of now\n");
			}
		}
		
		
		// 4. Validate Subscriber ID
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
		// 5 & 6. Validate Email and Phone Number only for Guests
		if( LoggedUser.getType()==SUBSCRIBER) {
			ID= String.valueOf(LoggedUser.getId());
			email = sub.getEmail();
			phone = sub.getPhone();
		}
		else {
			// 5. Validate Email format
			email = orderEmail.getText();
			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				str.append("Please enter a valid Email\n");
				check = false;
			}
			// 6. Validate Phone Number (must be 7 digits and contain only numbers)
			phone = phoneNumber.getText();
			if (phone == null || phone.length() != 7 || !phone.matches("\\d+")) {
				str.append("Please enter a valid 7-digit phone number\n");
				check = false;
			}
		}
		// Final Check: Display errors or process reservation
		if (!check) {
			showAlert("Reservation Failure", str.toString());
		} else {
			showAlert("Reservation Success", "Reservation successfully placed!");
			
			// Generate a 5-digit confirmation code (10000 to 99999)
			int confirmation_code = random.nextInt(90000) + 10000;
			
			// Create Reservation object: date, amount, code, subscriber ID, today's date (for tracking)
			Reservation r = new Reservation(date, amount, confirmation_code, Integer.parseInt(ID), today, selected.toLocalTime(), phone, email);
			// Send the reservation object to the client controller for server communication
			Main.client.accept(r);
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