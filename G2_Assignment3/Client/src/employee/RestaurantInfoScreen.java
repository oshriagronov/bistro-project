package employee;

import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class RestaurantInfoScreen {
	public static final String fxmlPath = "/employee/RestaurantInfo.fxml";
    @FXML
    private Button SubBtn;

    @FXML
    private Button WaitlistBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Button dinersBtn;

    @FXML
    private Button ordersBtn;

    @FXML
    void back(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @FXML
    void searchSub(ActionEvent event) {
		try {
			Main.changeRoot(SubscribersInfoScreen .fxmlPath, 1000, 800);
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
    void watchOrders(ActionEvent event) {
		try {
			Main.changeRoot(OrdersManagementScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @FXML
    void watchWaitlist(ActionEvent event) {
		try {
			Main.changeRoot(WaitingListScreen.fxmlPath, 1000, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
