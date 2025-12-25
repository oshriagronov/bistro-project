package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
/**
 * Controller class for the WorkerReportMenuScreen.fxml view.
 * This class handles the navigation for restaurant employees,
 * allowing them to access various report management functionalities.
 */
public class WorkerReportMenuScreen {
 
	@FXML
	private Button menuBTN;

	@FXML
	private Button ordersBTN;

	@FXML
	private Button reportsBTN;

	@FXML
	private Button tablesBTN;
	/**
	 * Initializes the controller.
	 * This method is automatically called after the FXML file has been loaded.
	 */
	@FXML
	void mainMenu(ActionEvent event) {
		try {
			Main.changeRoot("MainMenu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Handles the action when the "Manage Menu" button is clicked.
	 * Navigates to the MenuManagement.fxml view.
	 */
	@FXML
	void manageOrders(ActionEvent event) {
		try {
			Main.changeRoot("OrdersManagement.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Handles the action when the "View Reports" button is clicked.
	 * Navigates to the ReportsView.fxml view.
	 */
	@FXML
	void manageTables(ActionEvent event) {

	}
	/**
	 * Handles the action when the "Manage Tables" button is clicked.
	 * Navigates to the TablesManagement.fxml view.
	 */
	@FXML
	void watchReports(ActionEvent event) {

	}

}
