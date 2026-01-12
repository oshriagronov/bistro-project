package employee;

import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ReportsMenuScreen {
	public static final String fxmlPath = "/employee/ReportsMenu.fxml";

	@FXML
	private Button SubscribersReportsBtn;

	@FXML
	private Button TimingReportsBtn;

	@FXML
	private Button menuBtn;

	@FXML
	void Back(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void subscribersReports(ActionEvent event) {
		try {
			Main.changeRoot(SubscribersReportController.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void timingReports(ActionEvent event) {
		try {
			Main.changeRoot(StatusReportController.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
