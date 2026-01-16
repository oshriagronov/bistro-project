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
	private Button reportsBTN;

	@FXML
	private Button CostumerBtn;

	@FXML
	private Button infoBtn;

	@FXML
	private Button managementBtn;

	@FXML
	public void initialize() {
		if (LoggedUser.getType() == UserType.EMPLOYEE) {
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
    void manageRestaurant(ActionEvent event) {
		try {
			Main.changeRoot(RestaurantManagementScreen.fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @FXML
    void watchInfo(ActionEvent event) {
		try {
			Main.changeRoot(RestaurantInfoScreen.fxmlPath);
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
	void watchReports(ActionEvent event) {
		try {
			Main.changeRoot(ReportsMenuScreen.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
