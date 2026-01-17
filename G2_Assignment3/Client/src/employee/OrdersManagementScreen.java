package employee;

import java.time.LocalTime;
import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import communication.RequestFactory;
import communication.StatusUpdate;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Reservation;
import logic.Status;

public class OrdersManagementScreen {
	public static final String fxmlPath = "/employee/OrdersManagement.fxml";
	private final EventListener ordersListener = t -> loadResults();

	@FXML
	private Button clearBTN;

	@FXML
	private TableColumn<Reservation, String> confirmationCodeCol;

	@FXML
	private ComboBox<String> inputChoiceCB;

	@FXML
	private TableColumn<Reservation, String> phoneCol;

	@FXML
	private TableColumn<Reservation, String> emailCol;

	@FXML
	private TableColumn<Reservation, String> dateCol;

	@FXML
	private TableColumn<Reservation, Integer> dinersCol;

	@FXML
	private Button menuBTN;

	@FXML
	private TableColumn<Reservation, Integer> orderIdCol;

	@FXML
	private TableView<Reservation> ordersTable;

	@FXML
	private TextField inputTXT;

	@FXML
	private Button searchBTN;

	@FXML
	private TableColumn<Reservation, Status> statusCol;

	@FXML
	private TableColumn<Reservation, LocalTime> startTimeCol;

	@FXML
	private TableColumn<Reservation, LocalTime> finishTimeCol;

	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	public void initialize() {

		ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		inputChoiceCB.setItems(FXCollections.observableArrayList("Phone number:", "email:"));
		inputChoiceCB.getSelectionModel().selectFirst();

		orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

		confirmationCodeCol.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));

		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

		dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

		dinersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

		startTimeCol.setCellValueFactory(new PropertyValueFactory<>("start_time"));

		finishTimeCol.setCellValueFactory(new PropertyValueFactory<>("finish_time"));

		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone_number"));

		EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, ordersListener);

		Main.client.accept(RequestFactory.getTodaysReservations());
		getResponseFromServer();

	}

	@FXML
	void clickClear(ActionEvent event) {
		inputTXT.setText("");
		inputTXT.clear();
		ordersTable.getItems().clear();
	}

	@FXML
	void clickMenu(ActionEvent event) {
		try {
			Main.changeRoot(RestaurantInfoScreen.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void clickSearch(ActionEvent event) {
		loadResults();
	}

	private void loadResults() {
		String inputChoice = inputChoiceCB.getValue();
		if (inputChoice.equals("Phone number:")) {
			String phone_number = inputTXT.getText();
			if (phone_number == null || phone_number.isBlank() || phone_number.length() != 10) {
				showAlert("Error", "Please enter a valid phone number");
				return;
			}
			Main.client.accept(RequestFactory.getActiveReservationsByPhone(phone_number));
			getResponseFromServer();

		} else {
			String email = inputTXT.getText();
			if (email == null || email.isBlank() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				showAlert("Error", "Please enter a valid email");
				return;
			}
			Main.client.accept(RequestFactory.getReservationsByEmail(email));
			getResponseFromServer();
		}
	}

	public void getResponseFromServer() {
		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			Object data = Main.client.getResponse().getData();
			ArrayList<Reservation> reservations = (ArrayList<Reservation>) data;
			ordersTable.setItems(javafx.collections.FXCollections.observableArrayList(reservations));
		} else {
			showAlert("Error", "An error occured while loading orders");
		}

	}

	public void onClose() {
		EventBus.getInstance().unsubscribe(EventType.ORDER_CHANGED, ordersListener);
	}

}