package gui;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventType;
import communication.RequestFactory;
import employee.employeeMenu;
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
import subscriber.SubscriberScreen;

public class WaitingListScreen {
	public static final String fxmlPath = "/gui/WaitingList.fxml";
	private Subscriber sub=null;
	private Worker worker=null;
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    private volatile boolean isActive = true;
	private int numofhourstowait = 2;


    @FXML
    private Button backBtn;

	@FXML
    private VBox infoVbox;

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
    private Text tableResultText;

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
		LocalTime[] hours = Restaurant.getOpeningTime(LocalDate.now());
		LocalTime now = LocalTime.now();
		// Calculate the cutoff time for joining the waiting list, which is 2 hours before closing.
		// A special case handles midnight closing to prevent LocalTime wrap-around issues.
		LocalTime cutoff = (hours != null && hours[1] != null) ? (hours[1].equals(LocalTime.MIDNIGHT) ? LocalTime.of(22, 0) : hours[1].minusHours(2)) : LocalTime.MAX;
		if (hours == null || hours[0] == null || hours[1] == null || hours[0].equals(hours[1]) || now.isBefore(hours[0]) || now.isAfter(cutoff)) {
			infoVbox.setManaged(false);
			infoVbox.setVisible(false);
			tableResultText.setText("Restaurant is closed or closing soon\n see you next time!");
			tableResultText.setVisible(true);
			return;
		}
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
		Integer orderNumber = null;
		String phoneNumber = null;

		if (diners.getValue() == null) {
			showAlert("Error", "Please choose the diners amount");
			return;
		}
		int num_of_diners = Integer.parseInt(diners.getValue());

		if (sub != null){
			phoneNumber = sub.getPhone();
			Reservation r = new Reservation(today, num_of_diners, sub.getSubscriberId(), today, now, sub.getPhone(), Status.PENDING, sub.getEmail()) ;
			Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
			response = Main.client.getResponse();
		}
		
		else if (worker != null && subCheckBox.isSelected()){
			String phone = "";
			if (prePhone.getValue() != null) phone += prePhone.getValue();
			if (phoneField.getText() != null) phone += phoneField.getText();
			String email = emailField.getText();
			
			Subscriber foundSub = null;
			
			if (phone.length() ==  10) {
				Main.client.accept(new BistroRequest(BistroCommand.SEARCH_SUB_BY_PHONE, phone));
				BistroResponse subResp = Main.client.getResponse();
				if (subResp.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) subResp.getData();
				}
			} else if (email != null && !email.isEmpty()) {
				Main.client.accept(new BistroRequest(BistroCommand.SEARCH_SUB_BY_EMAIL, email));
				BistroResponse subResp = Main.client.getResponse();
				if (subResp.getStatus() == BistroResponseStatus.SUCCESS) {
					foundSub = (Subscriber) subResp.getData();
				}
			} else {
				showAlert("Error", "Please enter phone or email to place the order.");
				return;
			}
			
			if (foundSub != null) {
				phoneNumber = foundSub.getPhone();
				Reservation r = new Reservation(today, num_of_diners, foundSub.getSubscriberId(), today, now, foundSub.getPhone(), Status.PENDING, foundSub.getEmail());
				Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
				response = Main.client.getResponse();
			} else {
				showAlert("Error", "Subscriber not found. Please check details.");
				return;
			}
		}
		else{
			String phone_number = "";
			if (prePhone.getValue() != null) phone_number += prePhone.getValue();
			if (phoneField.getText() != null) phone_number += phoneField.getText();
			String email = emailField.getText();
			int nonSub = 0;

			if (phone_number.length() != 10) {
				showAlert("Input Error", "Please enter a valid 10-digit phone number.");
				return;
			} else if (phone_number.isEmpty() && (email == null || email.trim().isEmpty())) {
				showAlert("Input Error", "Please enter identifying information (Phone or Email).");
				return;
			} else {
				phoneNumber = phone_number;
				Reservation r = new Reservation(today, num_of_diners, nonSub, today, now, phone_number, Status.PENDING, email);
				Main.client.accept(new BistroRequest(BistroCommand.ADD_RESERVATION, r));
				response = Main.client.getResponse();
			}
		}

		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			Object data = response.getData();
			if (data instanceof Integer) {
				orderNumber = (Integer) data;
			} else if (data != null) {
				try {
					orderNumber = Integer.parseInt(data.toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if (orderNumber != null && searchTable(num_of_diners, today, now)) {
				Main.client.accept(RequestFactory.changeStatus(phoneNumber, orderNumber, Status.CONFIRMED));
				BistroResponse resResp = sendRequest(BistroCommand.GET_RESERVATION_BY_ORDER_NUMBER, orderNumber);
				String confirmationCode = null;
				if (resResp != null && resResp.getStatus() == BistroResponseStatus.SUCCESS && resResp.getData() instanceof Reservation) {
					confirmationCode = ((Reservation) resResp.getData()).getConfirmationCode();

				}
				BistroResponse tableResp = sendRequest(BistroCommand.GET_TABLE_BY_CONFIRMATION_CODE, confirmationCode);
				if (tableResp != null && tableResp.getStatus() == BistroResponseStatus.SUCCESS && tableResp.getData() != null) {
					System.out.println("hi2"); //TODO delete
					infoVbox.setVisible(false);
					tableResultText.setText("Your table is: " + tableResp.getData().toString());
					tableResultText.setVisible(true);
					isActive = false;
					return;
				}
			}
			showAlert("Reservation Success", "Reservation successfully created.");
		} else if (response != null) {
			showAlert("Error", "Failed to create reservation.");
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
		if (response == null || response.getStatus() != BistroResponseStatus.SUCCESS) {
			return false;
		}
		List <Integer> occupiedTables = (List<Integer>) response.getData();
		List<Integer> groups = new ArrayList<>(occupiedTables);
		groups.add(num_of_diners);
		groups.sort(Integer::compareTo);
		List<Integer> tableSizes = Restaurant.getTableSizes();
		if(Restaurant.isAvailable(groups, tableSizes)){
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

		Main.client.accept(RequestFactory.getAllPendingReservations());
		BistroResponse res = Main.client.getResponse();
		if (res.getStatus() == BistroResponseStatus.SUCCESS) {
			List<Reservation> pending = (List<Reservation>) res.getData();
			LocalDate today = LocalDate.now();
			LocalTime now = LocalTime.now();
			for (Reservation r : pending) {
				if (r.getOrderDate().equals(today) && r.getStart_time().plusHours(1).isBefore(now)) {
					Main.client.accept(RequestFactory.changeStatus(r.getPhone_number(), r.getOrderNumber(), Status.CANCELLED));
				}
			}
		}
	}
	*/
	/**
	 * Checks with thread the waiting list for pending reservations that already past 1 hour
	 * If a reservation already past 1 hour, change its status to CANCELLED 
	 */
	public void checkWaitingListExpired() {
		if (!isActive) return;
		new Thread(() -> {
			while (isActive) {
				LocalDate today = LocalDate.now();
				LocalTime now = LocalTime.now();
				Main.client.accept(RequestFactory.getAllPendingReservations());
				BistroResponse res = Main.client.getResponse();
				if (res.getStatus() == BistroResponseStatus.SUCCESS) {
					List<Reservation> pending = (List<Reservation>) res.getData();
					for (Reservation r : pending) {
						if (r.getOrderDate().equals(today) && r.getStart_time().plusHours(numofhourstowait).isBefore(now)) {
							Main.client.accept(RequestFactory.changeStatus(r.getPhone_number(), r.getOrderNumber(), Status.CANCELLED));
						}
					}
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
				String code = String.valueOf(candidate.getOrderNumber());
				ArrayList<String> info = new ArrayList<>();
				info.add(candidate.getPhone_number());
				info.add(candidate.getEmail());
				info.add(candidate.getConfirmationCode());
				sendRequest(BistroCommand.SEND_CODE_TO_WAITING_LIST, info);
				BistroResponse response = sendRequest(BistroCommand.GET_TABLE_BY_CONFIRMATION_CODE, code);
				Platform.runLater(() -> {
					if (isActive) {
						showAlert("Waiting List Update", "Reservation for " + candidate.getPhone_number() + " has been automatically accepted.");
       		 			if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
            			Object data = response.getData();
            				if (data != null) {
								infoVbox.setVisible(false);
								tableResultText.setText("Your table is: " + data.toString());
								tableResultText.setVisible(true);
								return;
            				}
       					}
					}
				});
			}
		}
	}

	/**
	 * Sends a request to the server and returns the response.
	 *
	 * @param command server command to execute
	 * @param data request payload
	 * @return response received from the server
	 */
	private BistroResponse sendRequest(BistroCommand command, Object data) {
		BistroRequest request = new BistroRequest(command, data);
		Main.client.accept(request);
		return Main.client.getResponse();
	}

    /**
     * Handles the action when the "Back to MainMenu" button is clicked.
     * Navigates the application back to the main menu screen.
     * @param event The ActionEvent triggered by the Back button.
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
