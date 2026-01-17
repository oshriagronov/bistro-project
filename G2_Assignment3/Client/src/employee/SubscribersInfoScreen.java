package employee;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import communication.BistroResponse;
import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import communication.RequestFactory;
import communication.SubscriberOrderRow;
import gui.BarcodeUtil;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import logic.Status;
import logic.Subscriber;

public class SubscribersInfoScreen {

	public static final String fxmlPath = "/employee/SubscriberInfo.fxml";
	private final EventListener ordersListener = t -> loadResults();

	@FXML
	private ImageView BarcodeIMG;

	@FXML
	private Button clearBTN;

	@FXML
	private TableColumn<SubscriberOrderRow, String> confirmationCodeCol;

	@FXML
	private TableColumn<SubscriberOrderRow, LocalDate> dateCol;

	@FXML
	private TableColumn<SubscriberOrderRow, Integer> dinersCol;

	@FXML
	private TextField emailTxt;

	@FXML
	private TableColumn<SubscriberOrderRow, LocalTime> finishTimeCol;

	@FXML
	private TextField inputTXT;

	@FXML
	private Button menuBTN;

	@FXML
	private TableColumn<SubscriberOrderRow, Integer> orderIdCol;

	@FXML
	private TableView<SubscriberOrderRow> ordersTable;

	@FXML
	private TextField phoneTxt;

	@FXML
	private Button searchBTN;

	@FXML
	private TableColumn<SubscriberOrderRow, LocalTime> startTimeCol;

	@FXML
	private TableColumn<SubscriberOrderRow, Status> statusCol;

	@FXML
	private Label statusLabel;

	public void initialize() {
		ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		confirmationCodeCol.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		dinersCol.setCellValueFactory(new PropertyValueFactory<>("diners"));
		startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
		finishTimeCol.setCellValueFactory(new PropertyValueFactory<>("finishTime"));
		EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, ordersListener);
	}

	private void loadResults() {
		String s = inputTXT.getText();
		if (s == null || s.isBlank()) {
			return;
		}

		int subId;
		try {
			subId = Integer.parseInt(s.trim());
		} catch (NumberFormatException e) {
			return;
		}

		Main.client.accept(RequestFactory.getSubscriberById(subId));
		BistroResponse response = Main.client.getResponse();

		if (response == null || response.getData() == null) {
			return;
		}

		Subscriber sub = (Subscriber) response.getData();
		emailTxt.setText(sub.getEmail());
		phoneTxt.setText(sub.getPhone());
		BarcodeIMG.setImage(BarcodeUtil.createQr(String.format("%05d", subId), 220));
		BarcodeIMG.setVisible(true);

		Main.client.accept(RequestFactory.getSubscriberReservationHistory(subId));
		Object data = Main.client.getResponse().getData();
		if (data instanceof List<?>) {
			ArrayList<SubscriberOrderRow> reservations = (ArrayList<SubscriberOrderRow>) data;
			ordersTable.setItems(FXCollections.observableArrayList(reservations));
		}
	}

	@FXML
	void clickClear(ActionEvent event) {
		emailTxt.clear();
		phoneTxt.clear();
		ordersTable.getItems().clear();
		BarcodeIMG.setImage(null);
		BarcodeIMG.setVisible(false);
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
}