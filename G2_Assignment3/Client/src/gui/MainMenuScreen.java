/**
 * Controller class for the MainMenuScreen.fxml view.
 * This class provides the navigation logic for the main menu, 
 * allowing the user to transition to the new reservation screen 
 * or the reservation update screen.
 */
package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainMenuScreen {
    public static final String fxmlPath = "/gui/MainMenu.fxml";
    /**
     * Button used to navigate to the waiting list screen.
     */
    @FXML
    private Button WaitingListBtn;
        /**
     * Button used to navigate to the new reservation screen.
     */
    @FXML
    private Button NewReservationBtn;

        /**
     * Button used to navigate to the Accept Table screen.
     */
    @FXML
    private Button AcceptTable;
    /**
     * Button used to navigate to the payment screen.
     */
    @FXML
    private Button paymentBtn;
    /**
     * Button used to return to the login menu screen.
     */
    @FXML
    private Button backBtn;

    /**
     * Handles the action when the "New Reservation" button is clicked.
     * Changes the application's view to the {@code Order.fxml} screen.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void openNewReservation(ActionEvent event) {
        try {
            Main.changeRoot(OrderScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when the "Waiting List" button is clicked.
     * Changes the application's view to the {@code WaitingList.fxml} screen.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void openWaitingList(ActionEvent event) {
        try {
            Main.changeRoot(WaitingListScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles the action when the "Accept Table" button is clicked.
     * 
     * Change the application to the accept table screen.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void AcceptTable(ActionEvent event) {
        try {
            Main.changeRoot(AcceptTableScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Navigates to the payment screen.
     */
    @FXML
    void openPaymentScreen(ActionEvent event) {
        try {
            Main.changeRoot(PaymentScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * Handles the action when the "Cancel Reservation" button is clicked.
     * Changes the application's view to the {@code Update.fxml} screen.
     * * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void CancelReservationScreen(ActionEvent event) {
        try {
            // Use the static method in Main to switch the scene root to the Update screen
            Main.changeRoot(CancelReservationScreen.fxmlPath);
        } catch (Exception e) {
            // Log error if navigation fails (e.g., FXML file not found)
            e.printStackTrace();
        }
    }
    /**
     * Handles the action when the "Back" button is clicked.
     * Navigates the application back to the login menu screen.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void backToLogin(ActionEvent event) {
        try {
            Main.changeRoot(LoginMenuScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
