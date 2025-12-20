package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class employeeMenu {

	@FXML
	private Button menuBTN;

	@FXML
	private Button ordersBTN;

	@FXML
	private Button reportsBTN;

	@FXML
	private Button tablesBTN;

	@FXML
	void mainMenu(ActionEvent event) {
		try {
			Main.changeRoot("MainMenu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void manageOrders(ActionEvent event) {
		try {
			Main.changeRoot("OrdersManagement.fxml", 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void manageTables(ActionEvent event) {

	}

	@FXML
	void watchReports(ActionEvent event) {

	}

}
