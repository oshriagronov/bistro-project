package employee;

import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class RestaurantManagementScreen {
	public static final String fxmlPath = "/employee/RestaurantManagement.fxml";
    @FXML
    private Button TablesBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Button scheduleBtn;

    @FXML
    private Button subBtn;

    @FXML
    void addSubscriber(ActionEvent event) {
		try {
			Main.changeRoot(AddSubscriberScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @FXML
    void back(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
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
    void manageTables(ActionEvent event) {
    	try {
			Main.changeRoot(TablesManagementScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
