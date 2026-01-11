package employee;

import gui.LoginMenuScreen;
import gui.Main;
import gui.MainMenuScreen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import logic.LoggedUser;
import logic.UserType;

public class employeeMenu {
	public static final String fxmlPath = "/employee/employeeScreen.fxml";
	@FXML
	private Button backBtn;

	@FXML
	private Button ordersBTN;

	@FXML
	private Button scheduleBtn;

	@FXML
	private Button reportsBTN;

	@FXML
	private Button tablesBTN;

	@FXML
	private Button subscriberBtn;

	@FXML
	private Button dinersBtn;

	@FXML
	private Button CostumerBtn;
	
	@FXML
	public void initialize() {
		if(LoggedUser.getType()== UserType.EMPLOYEE)
		{
			reportsBTN.setManaged(false);
		}
	}

	@FXML
	void backToLoginMenu(ActionEvent event) {
		try {
			LoggedUser.setId(0);
			LoggedUser.setGuest();
			Main.changeRoot(LoginMenuScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void goToCostumerManu(ActionEvent event) {
		try {
			Main.changeRoot(MainMenuScreen.fxmlPath);
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
	void manageSchedule(ActionEvent event) {
		try {
			Main.changeRoot(ScheduleManagementScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void watchDiners(ActionEvent event) {
		try {
			Main.changeRoot(CurrentDinersScreen.fxmlPath, 1000, 800);
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
		try {
			Main.changeRoot(StatusReportController.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
