package gui;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventType;
import communication.RequestFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import logic.LoggedUser;
import logic.Reservation;
import logic.Status;
import logic.Subscriber;
import logic.UserType;
import logic.Worker;

public class WaitingListScreen {
	public static final String fxmlPath = "/gui/WaitingList.fxml";
	private Subscriber sub=null;
	private Worker worker=null;
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    private volatile boolean isActive = true;

     @FXML
    private Button backBtn;

    @FXML
    private ComboBox<String> diners;

    @FXML
    private TextField emailField;

    @FXML
    private VBox nonSubVbox;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> prePhone;

    @FXML
    private CheckBox subCheckBox;

    @FXML
    private Button submit;

	@FXML
    private VBox workerVbox;

    public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

    @FXML
    void initialize() {
		if (LoggedUser.getType()==UserType.SUBSCRIBER) {
        	this.sub = ScreenSetup.setupSubscriber(nonSubVbox, workerVbox, null);
        }
        else if (LoggedUser.getType()==UserType.EMPLOYEE || LoggedUser.getType()==UserType.MANAGER) {
        	ScreenSetup.setupWorkerView(nonSubVbox, workerVbox, null);
        }
        else {
            ScreenSetup.setupGuestView(nonSubVbox, workerVbox, null);
        }
        diners.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			diners.getItems().add(String.valueOf(i));
		}
        prePhone.getItems().clear();
        prePhone.getItems().addAll("050", "052", "053", "054", "055", "058");

		EventBus.getInstance().subscribe(EventType.TABLE_CHANGED, event -> {
			if (isActive) new Thread(this::checkWaitingList).start();
		});
        EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, event -> {
        	if (isActive) new Thread(this::checkWaitingList).start();
        });
    }

	@FXML
	void submit(ActionEvent event) {
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();
		BistroResponse response = null;
		if (diners.getValue() == null) {
			showAlert("Error", "Please choose the diners amount");
		}
		int num_of_diners = Integer.parseInt(diners.getValue());
		if (sub != null){
			Reservation r = new Reservation(today, num_of_diners, sub.getSubscriberId(), today, now, sub.getPhone(), Status.PENDING, sub.getEmail()) ;
			Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
			showAlert("Reservation Success", "Reservation successfully created.");
		}
		
		else if (worker != null && subCheckBox.isSelected()){
			String phone = "";
			if (prePhone.getValue() != null) phone += prePhone.getValue();
			if (phoneField.getText() != null) phone += phoneField.getText();
			String email = emailField.getText();
			
			Subscriber foundSub = null;
			
			if (phone.length() ==  10) {
				Main.client.accept(new BistroRequest(BistroCommand.SEARCH_SUB_BY_PHONE, phone));
				response = Main.client.getResponse();
				if (response.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) response.getData();
				}
			} else if (email != null && !email.isEmpty()) {
				Main.client.accept(new BistroRequest(BistroCommand.SEARCH_SUB_BY_EMAIL, email));
				response = Main.client.getResponse();
				if (response.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) response.getData();
				}
			} else {
				showAlert("Error", "Please enter phone or email to place the order.");
			}
			
			if (foundSub != null) {
				Reservation r = new Reservation(today, num_of_diners, foundSub.getSubscriberId(), today, now, foundSub.getPhone(), Status.PENDING, foundSub.getEmail());
				Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
				showAlert("Reservation Success", "Reservation successfully created for " + foundSub.getFirstName());
			} else {
				showAlert("Error", "Subscriber not found. Please check details.");
			}
		}
		else{
			String phone_number = "";
			if (prePhone.getValue() != null) phone_number += prePhone.getValue();
			if (phoneField.getText() != null) phone_number += phoneField.getText();
			String email = emailField.getText();
			int nonSub = 0;

			if (phone_number.length() > 0 && phone_number.length() < 10) {
				showAlert("Input Error", "Please enter a valid 10-digit phone number.");
			} else if (phone_number.isEmpty() && (email == null || email.trim().isEmpty())) {
				showAlert("Input Error", "Please enter identifying information (Phone or Email).");
			} else {
				Reservation r = new Reservation(today, num_of_diners, nonSub, today, now, phone_number, Status.PENDING, email);
				Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
				response = Main.client.getResponse();
				if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
					showAlert("Reservation Success", "Reservation successfully created.");
				} else {
					showAlert("Error", "Failed to create reservation.");
				}
			}
		}
		Boolean table = searchTable(num_of_diners, today, now);
		try{
			if (table == true){
				isActive = false;
			}
		} catch (Exception e) {
		e.printStackTrace();
		}
    }
	/**
	 * Checks whether a table is available for the given number of diners
	 * at the specified date and time.
	 *
	 * @param num_of_diners number of diners
	 * @param today the requested date
	 * @param now the requested time
	 * @return true if a suitable table is available, otherwise false
	 */
	public Boolean searchTable(int num_of_diners, LocalDate today, LocalTime now) {
		Main.client.accept(RequestFactory.getOrderIn4HoursRange(today, now));
		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			return false;
		}
		List <Integer> occupiedTables = (List<Integer>) response.getData();
		List<Integer> tableSizes = Restaurant.getTableSizes();
		tableSizes.add(num_of_diners);
		if(Restaurant.isAvailable(occupiedTables, tableSizes)){
			return true;
		}
		return false;
	}

	
	/**
	 * Finds a pending reservation that can be seated right now
	 * based on current diners and table availability.
	 *
	 * @param pending list of pending reservations
	 * @param date today's date
	 * @param time current time
	 * @return a reservation that can be seated now, or null if none fit
	 */
	public static Reservation findCandidateFromWaitingList(List<Reservation> pending, LocalDate date, LocalTime time) {

	    var map = Restaurant.buildDinersByTime(date);
	    List<Integer> current = map.getOrDefault(time, new java.util.ArrayList<>());

	    List<Integer> tables = Restaurant.getTableSizes();

	    for (Reservation r : pending) {
			if (!r.getOrderDate().equals(date)) continue;
	        List<Integer> test = new java.util.ArrayList<>(current);
	        test.add(r.getNumberOfGuests());
	        test.sort(Integer::compareTo);

	        if (Restaurant.isAvailable(test, tables)) {
	            return r;
	        }
	    }

	    return null;
	}

	/**
	 * Starts a background thread to monitor opening and closing times.
	 * Resets the waiting list when the restaurant closes.
	 */
	public static void checkOpeningAndClosingTime() {
		LocalDate today = LocalDate.now();
		new Thread(() -> {
			while (true) {
				LocalTime now = LocalTime.now();
				LocalTime[] openingTimes = Restaurant.getOpeningTime(today);
				if (now.isAfter(openingTimes[1])) {
					Main.client.accept(RequestFactory.resetWaitingList());
				}
				try {
					Thread.sleep(1800000); // Check every 30 minutes
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	



	private void checkWaitingList() {
		if (!isActive) return;
		Main.client.accept(RequestFactory.getAllPendingReservations());
		BistroResponse res = Main.client.getResponse();
		if (res.getStatus() == BistroResponseStatus.SUCCESS) {
			List<Reservation> pending = (List<Reservation>) res.getData();
			Reservation candidate = findCandidateFromWaitingList(pending, LocalDate.now(), LocalTime.now());
			if (candidate != null) {
				Main.client.accept(RequestFactory.changeStatus(candidate.getPhone_number(), candidate.getOrderNumber(), Status.CONFIRMED));
				Platform.runLater(() -> {
					if (isActive) {
						showAlert("Waiting List Update", "Reservation for " + candidate.getPhone_number() + " has been automatically accepted.");
						AcceptTableScreen.setPendingConfirmationCode(candidate.getConfirmationCode().toString());
						// Use the static method in Main to switch the scene root
						try {
							Main.changeRoot(AcceptTableScreen.fxmlPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
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
			isActive = false;
			// Use the static method in Main to switch the scene root
			Main.changeRoot(MainMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
