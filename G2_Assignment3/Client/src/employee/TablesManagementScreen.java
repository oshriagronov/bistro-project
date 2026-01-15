package employee;

import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.EventBus;
import communication.EventType;
import communication.RequestFactory;
import communication.TableSizeUpdate;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Table;

/**
 * Controller class for the TableManagement.fxml view.
 * <p>
 * This screen allows employees to manage restaurant tables by:
 * </p>
 * <ul>
 * <li>Viewing all tables (loaded from the server on initialization)</li>
 * <li>Deleting an existing table</li>
 * <li>Updating the size (number of seats) of a selected table</li>
 * <li>Adding a new table (depending on server-side implementation of
 * {@link BistroCommand#ADD_TABLE})</li>
 * </ul>
 *
 * <p>
 * All operations are performed by sending {@link BistroRequest} messages to the
 * server and processing the returned {@link BistroResponse}.
 * </p>
 */
public class TablesManagementScreen {

	/** Path to the FXML file associated with this screen. */
	public static final String fxmlPath = "/employee/TableManagement.fxml";

	/** Shared alert dialog used for user feedback. */
	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	/** Button used to add a new table. */
	@FXML
	private Button addBtn;

	/** Button used to delete the selected table. */
	@FXML
	private Button deleteBtn;

	/** ComboBox for selecting the size of a new table to add. */
	@FXML
	private ComboBox<Integer> newTableSize;

	/** Button used to apply a new size to the selected table. */
	@FXML
	private Button applySizeBtn;

	/** Button used to navigate back to the employee menu. */
	@FXML
	private Button menuBtn;

	/** ComboBox for selecting a new size for the currently selected table. */
	@FXML
	private ComboBox<Integer> sizeCombo;

	/** Table column showing the table number. */
	@FXML
	private TableColumn<Table, Integer> tableNumberCol;

	/** Table column showing the table size (number of seats). */
	@FXML
	private TableColumn<Table, Integer> tableSizeCol;

	/**
	 * Table column showing reservation id or related reservation field (if
	 * applicable).
	 */
	@FXML
	private TableColumn<Table, Integer> reservationCol;

	/** TableView that displays all tables returned from the server. */
	@FXML
	private TableView<Table> tablesTable;

	/**
	 * Loads (or reloads) the tables list from the server and updates the TableView.
	 */
	private void reloadTables() {
		Main.client.accept(RequestFactory.getTables());
		Object data = Main.client.getResponse().getData();
		ArrayList<Table> tables = (ArrayList<Table>) data;
		tablesTable.setItems(FXCollections.observableArrayList(tables));
	}

	/**
	 * Displays an information alert to the user.
	 *
	 * @param title the title of the alert dialog
	 * @param body  the message shown in the alert dialog
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Initializes the controller after the FXML has been loaded.
	 * <p>
	 * Sets up table columns, initializes ComboBox values, loads the current tables
	 * from the server, and enables/disables action buttons based on the current
	 * selection.
	 * </p>
	 */
	@FXML
	public void initialize() {
		applySizeBtn.setDisable(true);
		deleteBtn.setDisable(true);
		tablesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		sizeCombo.setItems(FXCollections.observableArrayList(2, 4, 6, 8, 10));
		sizeCombo.getSelectionModel().selectFirst();
		newTableSize.setItems(FXCollections.observableArrayList(2, 4, 6, 8, 10));
		newTableSize.getSelectionModel().selectFirst();

		tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("table_number"));
		tableSizeCol.setCellValueFactory(new PropertyValueFactory<>("table_size"));
		reservationCol.setCellValueFactory(new PropertyValueFactory<>("res_id"));

		reloadTables();

		tablesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			boolean hasSelection = newSel != null;
			applySizeBtn.setDisable(!hasSelection);
			deleteBtn.setDisable(!hasSelection);
		});
	}

	/**
	 * Applies the size selected in {@code sizeCombo} to the currently selected
	 * table. Sends a {@link BistroCommand#CHANGE_TABLE_SIZE} request to the server.
	 *
	 * @param event the action event triggered by clicking the apply size button
	 */
	@FXML
	void clickEditSize(ActionEvent event) {
		Table selected = tablesTable.getSelectionModel().getSelectedItem();
		selected.setTable_size(sizeCombo.getValue());

		/*
		 * Main.client.accept(new BistroRequest(BistroCommand.CHANGE_TABLE_SIZE, new
		 * TableSizeUpdate(selected.getTable_number(), sizeCombo.getValue())));
		 */
		Main.client.accept(RequestFactory.changeTableSize(selected.getTable_number(), sizeCombo.getValue()));
		BistroResponse resp = Main.client.getResponse();
		if (resp != null && resp.getStatus() == BistroResponseStatus.SUCCESS) {
			tablesTable.refresh();
		} else {
			showAlert("Error", "Failed to update table size");
			tablesTable.refresh();
		}
	}

	/**
	 * Sends a request to add a new table.
	 * <p>
	 * Note: This method assumes the server can handle
	 * {@link BistroCommand#ADD_TABLE} using the selected size as the request data.
	 * If your server expects a {@link Table} object (table number + size + status),
	 * adjust the request data accordingly.
	 * </p>
	 *
	 * @param event the action event triggered by clicking the add button
	 */
	@FXML
	void addNewTable(ActionEvent event) {
		Main.client.accept(RequestFactory.addTable(newTableSize.getValue()));
		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			showAlert("Success", "Table added successfully");
			reloadTables();
		} else {
			showAlert("Error", "Failed to add table");
		}
	}

	/**
	 * Deletes the currently selected table. Sends a
	 * {@link BistroCommand#DELETE_TABLE} request to the server.
	 *
	 * @param event the action event triggered by clicking the delete button
	 */
	@FXML
	void deleteTable(ActionEvent event) {
		Main.client.accept(
				RequestFactory.deleteTable(tablesTable.getSelectionModel().getSelectedItem().getTable_number()));

		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			showAlert("Success", "Table deleted successfully");
			reloadTables();
		} else {
			showAlert("Error", "Failed to delete table");
		}
	}

	/**
	 * Navigates back to the employee menu screen.
	 *
	 * @param event the action event triggered by clicking the menu button
	 */
	@FXML
	void clickMenu(ActionEvent event) {
		try {
			Main.changeRoot(RestaurantManagementScreen.fxmlPath,600,500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
