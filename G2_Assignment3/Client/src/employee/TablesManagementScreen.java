package employee;

import javafx.scene.control.TextField;
import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.BistroResponseStatus;
import communication.TableSizeUpdate;
import communication.TableStatusUpdate;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Table;
import logic.TableStatus;

/**
 * Controller for the Tables Management screen.
 * <p>
 * This screen allows employees to:
 * <ul>
 *   <li>View all tables</li>
 *   <li>Add new tables</li>
 *   <li>Delete existing tables</li>
 *   <li>Change table size</li>
 *   <li>Change table status</li>
 * </ul>
 * All changes are sent to the server and reflected back in the UI.
 */
public class TablesManagementScreen {

	/**
	 * FXML path for the tables management screen.
	 */
	public static final String fxmlPath = "/employee/TableManagement.fxml";

	/**
	 * Shared alert dialog used for user feedback.
	 */
	Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	private Button addBtn;

	@FXML
	private Button deleteBtn;

	@FXML
	private TextField newTableNumber;

	@FXML
	private ComboBox<Integer> newTableSize;

	@FXML
	private Button applySizeBtn;

	@FXML
	private Button applyStatusBtn;

	@FXML
	private Button menuBtn;

	@FXML
	private ComboBox<TableStatus> statusCombo;

	@FXML
	private ComboBox<Integer> sizeCombo;

	@FXML
	private TableColumn<Table, Integer> tableNumberCol;

	@FXML
	private TableColumn<Table, Integer> tableSizeCol;

	@FXML
	private TableColumn<Table, TableStatus> tableStatusCol;

	@FXML
	private TableView<Table> tablesTable;

	/**
	 * Reloads the tables list from the server and updates the TableView.
	 */
	private void reloadTables() {
		Main.client.accept(new BistroRequest(BistroCommand.GET_TABLES, null));
		Object data = Main.client.getResponse().getData();
		ArrayList<Table> tables = (ArrayList<Table>) data;
		tablesTable.setItems(javafx.collections.FXCollections.observableArrayList(tables));
	}

	/**
	 * Displays an alert dialog with the given title and message.
	 *
	 * @param title alert window title
	 * @param body  message to display
	 */
	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	/**
	 * Initializes the screen after the FXML is loaded.
	 * <p>
	 * Sets up combo boxes, table columns, button states,
	 * loads existing tables, and registers selection listeners.
	 */
	public void initialize() {
		applySizeBtn.setDisable(true);
		applyStatusBtn.setDisable(true);
		deleteBtn.setDisable(true);

		sizeCombo.getItems().addAll(java.util.stream.IntStream.rangeClosed(2, 10).boxed().toList());
		statusCombo.getSelectionModel().selectFirst();

		newTableSize.getItems().addAll(java.util.stream.IntStream.rangeClosed(2, 10).boxed().toList());
		newTableSize.getSelectionModel().selectFirst();

		statusCombo.getItems().setAll(TableStatus.values());
		statusCombo.getSelectionModel().selectFirst();

		tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("table_number"));
		tableSizeCol.setCellValueFactory(new PropertyValueFactory<>("table_size"));
		tableStatusCol.setCellValueFactory(new PropertyValueFactory<>("table_status"));

		reloadTables();

		tablesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				applyStatusBtn.setDisable(false);
				applySizeBtn.setDisable(false);
				deleteBtn.setDisable(false);
			} else {
				applyStatusBtn.setDisable(true);
				applySizeBtn.setDisable(true);
				deleteBtn.setDisable(true);
			}
		});
	}

	/**
	 * Applies a new status to the selected table.
	 *
	 * @param event button click event
	 */
	@FXML
	void clickApplyStatus(ActionEvent event) {
		Table selected = tablesTable.getSelectionModel().getSelectedItem();
		selected.setTable_status(statusCombo.getValue());

		Main.client.accept(new BistroRequest(
				BistroCommand.CHANGE_TABLE_STATUS,
				new TableStatusUpdate(selected.getTable_number(), statusCombo.getValue())
		));

		BistroResponse resp = Main.client.getResponse();
		if (resp != null && resp.getStatus() == BistroResponseStatus.SUCCESS) {
			tablesTable.refresh();
		} else {
			showAlert("Error", "Failed to update table status");
			tablesTable.refresh();
		}
	}

	/**
	 * Applies a new size to the selected table.
	 *
	 * @param event button click event
	 */
	@FXML
	void clickEditSize(ActionEvent event) {
		Table selected = tablesTable.getSelectionModel().getSelectedItem();
		selected.setTable_size(sizeCombo.getValue());

		Main.client.accept(new BistroRequest(
				BistroCommand.CHANGE_TABLE_SIZE,
				new TableSizeUpdate(selected.getTable_number(), sizeCombo.getValue())
		));

		BistroResponse resp = Main.client.getResponse();
		if (resp != null && resp.getStatus() == BistroResponseStatus.SUCCESS) {
			tablesTable.refresh();
		} else {
			showAlert("Error", "Failed to update table size");
			tablesTable.refresh();
		}
	}

	/**
	 * Adds a new table using the entered table number and selected size.
	 *
	 * @param event button click event
	 */
	@FXML
	void addNewTable(ActionEvent event) {
		String tableNumber = newTableNumber.getText();

		if (tableNumber == null || tableNumber.isBlank()) {
			showAlert("Failed to add table", "Please enter a table number");
		} else if (!tableNumber.matches("^[0-9]+$")) {
			showAlert("Failed to add table", "Table number should only contain numbers");
		} else {
			Main.client.accept(new BistroRequest(
					BistroCommand.ADD_TABLE,
					new Table(Integer.parseInt(tableNumber), newTableSize.getValue())
			));

			BistroResponse response = Main.client.getResponse();
			if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
				showAlert("Success", "Table added successfuly");
				reloadTables();
			} else {
				showAlert("Error", "Failed to add table");
			}
		}
	}

	/**
	 * Deletes the currently selected table.
	 *
	 * @param event button click event
	 */
	@FXML
	void deleteTable(ActionEvent event) {
		Main.client.accept(new BistroRequest(
				BistroCommand.DELETE_TABLE,
				tablesTable.getSelectionModel().getSelectedItem().getTable_number()
		));

		BistroResponse response = Main.client.getResponse();
		if (response != null && response.getStatus() == BistroResponseStatus.SUCCESS) {
			showAlert("Success", "Table deleted successfuly");
			reloadTables();
		} else {
			showAlert("Error", "Failed to deleted table");
		}
	}

	/**
	 * Navigates back to the employee menu screen.
	 *
	 * @param event button click event
	 */
	@FXML
	void clickMenu(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
