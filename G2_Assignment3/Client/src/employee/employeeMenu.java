package employee;

import gui.LoginMenuScreen;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class employeeMenu {
	public static final String fxmlPath = "/employee/employeeScreen.fxml";
	@FXML
	private Button backBtn;

	@FXML
	private Button ordersBTN;

	@FXML
	private Button reportsBTN;

	@FXML
	private Button tablesBTN;
	
    @FXML
    private Button subscriberBtn;

	@FXML
	void backToLoginMenu(ActionEvent event) {
		try {
			Main.changeRoot(LoginMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void manageOrders(ActionEvent event) {
		try {
			Main.changeRoot(OrdersManagementScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void manageTables(ActionEvent event) {
		try {
			Main.changeRoot(TablesManagementScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @FXML
    void addSubscriber(ActionEvent event) {
    	try {
			Main.changeRoot(AddSubscriberScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@FXML
	void watchReports(ActionEvent event) {

	}

}
