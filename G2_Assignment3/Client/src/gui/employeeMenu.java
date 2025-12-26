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
    private Button subscriberBtn;

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
		try {
			Main.changeRoot("TableManagement.fxml", 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @FXML
    void addSubscriber(ActionEvent event) {
    	try {
			Main.changeRoot("AddSubscriber.fxml", 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@FXML
	void watchReports(ActionEvent event) {

	}

}
