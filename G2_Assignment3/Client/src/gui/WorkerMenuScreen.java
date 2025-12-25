package gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller class for the WorkerMenuScreen.fxml view.
 * This class handles the navigation for restaurant employees,
 * allowing them to access reports and reservations management.
 */
public class WorkerMenuScreen {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button reportsBtn;

    @FXML
    private Button reservationBtn;
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    void initialize() {
        assert reportsBtn != null : "fx:id=\"reportsBtn\" was not injected: check your FXML file 'Untitled'.";
        assert reservationBtn != null : "fx:id=\"reservationBtn\" was not injected: check your FXML file 'Untitled'.";

    }

    /**
     * Handles the action when the "Reports" button is clicked.
     * Navigates to the WorkerReportMenu.fxml view.
     */
    @FXML
    void goToReports() {
        try {
            Main.changeRoot("WorkerReportMenu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when the "Reservations" button is clicked.
     * Navigates to the Order.fxml view.
     */
    @FXML
    void goToReservations() {
        try {
            Main.changeRoot("Order.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
