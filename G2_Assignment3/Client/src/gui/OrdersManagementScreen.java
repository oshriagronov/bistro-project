package gui;

import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.StatusUpdate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

	@FXML
	private Button applyStatusBTN;

	@FXML
	private Button clearBTN;

	@FXML
	private TableColumn<Reservation, String> dateCol;

	@FXML
	private Button deleteBTN;

	@FXML
	private TableColumn<Reservation, Integer> dinersCol;

	@FXML
	private Button menuBTN;

	@FXML
	private TableColumn<Reservation, Integer> orderIdCol;

	@FXML
	private TableView<Reservation> ordersTable;

	@FXML
	private TextField phoneTXT;

	@FXML
	private Button searchBTN;

	@FXML
	private TableColumn<Reservation, Status> statusCol;

	@FXML
	private ComboBox<Status> statusCombo;

	@FXML
	private Label statusLabel;

	@FXML
	private TableColumn<Reservation, String> timeCol;

	@FXML
	public void initialize() {

		statusCombo.getItems().setAll(Status.values());
		statusCombo.setPromptText("Select status...");

		orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

		dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

		dinersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

		ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

			if (newSelection != null) {
				applyStatusBTN.setDisable(false);
				deleteBTN.setDisable(false);
			} else {
				applyStatusBTN.setDisable(true);
				deleteBTN.setDisable(true);
			}
		});
	}

	@FXML
	void clickApplyStatus(ActionEvent event) {
		Reservation selected = ordersTable.getSelectionModel().getSelectedItem();
		selected.setStatus(statusCombo.getValue());
		Main.client.accept(new BistroRequest(BistroCommand.CHANGE_STATUS,
				new StatusUpdate(selected.getOrderNumber(), statusCombo.getValue())));
		ordersTable.refresh();

	}

	@FXML
	void clickClear(ActionEvent event) {
		phoneTXT.setText("");
		phoneTXT.clear();
		ordersTable.getItems().clear();
		ordersTable.getSelectionModel().clearSelection();
		applyStatusBTN.setDisable(true);
		deleteBTN.setDisable(true);
	}

	@FXML
	void clickDeleteSelected(ActionEvent event) {
		Reservation selected = ordersTable.getSelectionModel().getSelectedItem();

		if (selected == null) {
			return;
		}
		System.out.print(selected.getOrderNumber());
		Main.client.accept(new BistroRequest(BistroCommand.CANCEL_RESERVATION, selected.getOrderNumber()));
		ordersTable.getItems().remove(selected);
	}

	@FXML
	void clickMenu(ActionEvent event) {
		try {
			Main.changeRoot("employeeScreen.fxml", 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void clickSearch(ActionEvent event) {
		String phone_number = phoneTXT.getText();
		Main.client.accept(new BistroRequest(BistroCommand.GET_ACTIVE_RESERVATIONS_BY_PHONE, phone_number));
		Object data = Main.client.getResponse().getData();
		ArrayList<Reservation> reservations = (ArrayList<Reservation>) data;
		ordersTable.setItems(javafx.collections.FXCollections.observableArrayList(reservations));

	}

}