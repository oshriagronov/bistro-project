package employee;

import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.TableSizeUpdate;
import communication.TableStatusUpdate;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Table;
import logic.TableStatus;

public class TablesManagementScreen {

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

	public void initialize() {
		applySizeBtn.setDisable(true);
		applyStatusBtn.setDisable(true);
		sizeCombo.getItems().addAll(java.util.stream.IntStream.rangeClosed(2, 10).boxed().toList());
		statusCombo.setPromptText("Select size...");
		statusCombo.getItems().setAll(TableStatus.values());
		statusCombo.setPromptText("Select status...");
		tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("table_number"));
		tableSizeCol.setCellValueFactory(new PropertyValueFactory<>("table_size"));
		tableStatusCol.setCellValueFactory(new PropertyValueFactory<>("table_status"));
		Main.client.accept(new BistroRequest(BistroCommand.GET_TABLES, null));
		Object data = Main.client.getResponse().getData();
		ArrayList<Table> tables = (ArrayList<Table>) data;
		tablesTable.setItems(javafx.collections.FXCollections.observableArrayList(tables));
		tablesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

			if (newSelection != null) {
				applyStatusBtn.setDisable(false);
				applySizeBtn.setDisable(false);
			} else {
				applyStatusBtn.setDisable(true);
				applySizeBtn.setDisable(true);
			}
		});
	}

	@FXML
	void clickApplyStatus(ActionEvent event) {
		Table selected = tablesTable.getSelectionModel().getSelectedItem();
		selected.setTable_status(statusCombo.getValue());
		Main.client.accept(new BistroRequest(BistroCommand.CHANGE_TABLE_STATUS,
				new TableStatusUpdate(selected.getTable_number(), statusCombo.getValue())));
		tablesTable.refresh();

	}

	@FXML
	void clickEditSize(ActionEvent event) {
		Table selected = tablesTable.getSelectionModel().getSelectedItem();
		selected.setTable_size(sizeCombo.getValue());
		Main.client.accept(new BistroRequest(BistroCommand.CHANGE_TABLE_SIZE,
				new TableSizeUpdate(selected.getTable_number(), sizeCombo.getValue())));
		tablesTable.refresh();
	}
	
    @FXML
    void clickMenu(ActionEvent event) {
		try {
			Main.changeRoot("employeeScreen.fxml", 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
