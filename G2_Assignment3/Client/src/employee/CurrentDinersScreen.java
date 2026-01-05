package employee;

import java.util.ArrayList;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.CurrentDinerRow;

/**
 * Controller class for the CurrentDiners.fxml view.
 * <p>
 * This screen displays a real-time overview of diners currently seated in the
 * restaurant. Each row in the table represents a table that is currently
 * occupied and includes contact information, subscription details, number of
 * diners, and an associated order number if available.
 * </p>
 *
 * <p>
 * The data is retrieved from the server using the
 * {@link BistroCommand#LOAD_DINERS} command and displayed in a
 * {@link TableView}. Users can refresh the table to fetch updated information
 * or return to the employee main menu.
 * </p>
 */
public class CurrentDinersScreen {

	/** Path to the FXML file associated with this screen. */
	public static final String fxmlPath = "/employee/CurrentDiners.fxml";
	private final EventListener tableListener = t -> loadTables();

	/** Table view displaying the current diner rows. */
	@FXML
	private TableView<CurrentDinerRow> tableView;

	/** Column displaying the table number. */
	@FXML
	private TableColumn<CurrentDinerRow, Integer> tableNumberCol;

	/** Column displaying the diner's phone number. */
	@FXML
	private TableColumn<CurrentDinerRow, String> phoneCol;

	/** Column displaying the diner's email address. */
	@FXML
	private TableColumn<CurrentDinerRow, String> emailCol;

	/** Column displaying the subscriber ID, if available. */
	@FXML
	private TableColumn<CurrentDinerRow, Integer> subscriberIdCol;

	/** Column displaying the number of diners seated at the table. */
	@FXML
	private TableColumn<CurrentDinerRow, Integer> dinersCol;

	/** Column displaying the active order number, if available. */
	@FXML
	private TableColumn<CurrentDinerRow, Integer> orderNumberCol;

	/** Button used to navigate back to the employee main menu. */
	@FXML
	private Button menuBtn;

	/** Button used to refresh the diner data from the server. */
	@FXML
	private Button refreshBtn;

	/**
	 * Requests the current diners data from the server and populates the table
	 * view.
	 * <p>
	 * Sends a {@link BistroCommand#LOAD_DINERS} request to the server and converts
	 * the returned data into an observable list for display.
	 * </p>
	 */
	private void loadTables() {
		Main.client.accept(new BistroRequest(BistroCommand.LOAD_DINERS, null));
		Object data = Main.client.getResponse().getData();

		ArrayList<CurrentDinerRow> tables = (ArrayList<CurrentDinerRow>) data;

		tableView.setItems(javafx.collections.FXCollections.observableArrayList(tables));
	}

	/**
	 * Initializes the controller after the FXML file is loaded.
	 * <p>
	 * Sets up the table column bindings and loads the initial diner data from the
	 * server.
	 * </p>
	 */
	@FXML
	public void initialize() {
		tableNumberCol.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
		subscriberIdCol.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));
		dinersCol.setCellValueFactory(new PropertyValueFactory<>("diners"));
		orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		EventBus.getInstance().subscribe(EventType.TABLE_CHANGED, tableListener);
		loadTables();
	}

	/**
	 * Handles navigation back to the employee main menu.
	 *
	 * @param event the action event triggered by clicking the menu button
	 */
	@FXML
	void menu(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles refreshing the diner data from the server.
	 *
	 * @param event the action event triggered by clicking the refresh button
	 */
	@FXML
	void refresh(ActionEvent event) {
		loadTables();
	}
	
	public void onClose() {
	    EventBus.getInstance().unsubscribe(EventType.TABLE_CHANGED, tableListener);
	}
}
