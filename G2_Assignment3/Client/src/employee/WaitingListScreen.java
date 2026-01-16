package employee;

import java.time.LocalTime;
import java.util.ArrayList;

import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import communication.RequestFactory;
import communication.WaitlistRow;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Table;

public class WaitingListScreen {
	public static final String fxmlPath = "/employee/WaitingList.fxml";
	private final EventListener WaitlistListener = t -> loadWaitingList();
	@FXML
	private TableColumn<WaitlistRow, Integer> dinersCol;

	@FXML
	private TableColumn<WaitlistRow, String> emailCol;

	@FXML
	private TableColumn<WaitlistRow, LocalTime> enterTimeCol;

	@FXML
	private TableColumn<WaitlistRow, String> phoneCol;

	@FXML
	private TableView<WaitlistRow> waitlistTable;

	@FXML
	private Button BackBtn;

	@FXML
	public void initialize() {

		waitlistTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		dinersCol.setCellValueFactory(new PropertyValueFactory<>("diners"));
		emailCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		enterTimeCol.setCellValueFactory(new PropertyValueFactory<>("enteredAt"));
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		EventBus.getInstance().subscribe(EventType.WAITLIST_CHANGED, WaitlistListener);
		loadWaitingList();
	}

	private void loadWaitingList() {
		Main.client.accept(RequestFactory.getWaitingList());
		Object data = Main.client.getResponse().getData();
		ArrayList<WaitlistRow> waitingList = (ArrayList<WaitlistRow>) data;
		waitlistTable.setItems(FXCollections.observableArrayList(waitingList));
	}

	@FXML
	void BackToMenu(ActionEvent event) {
		try {
			Main.changeRoot(RestaurantInfoScreen.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClose() {
		EventBus.getInstance().unsubscribe(EventType.WAITLIST_CHANGED, WaitlistListener);
	}

}
