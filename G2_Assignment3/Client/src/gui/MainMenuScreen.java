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

    /**
     * Button used to navigate to the reservation update screen.
     */
    @FXML
    private Button updateReservation;

    /**
     * Button used to navigate to the subscriber login screen.
     */
    @FXML
    private Button subscriberLogin;

    /**
     * Handles the action when the "New Reservation" button is clicked.
     * Changes the application's view to the {@code Order.fxml} screen.
     * * @param event The ActionEvent triggered by the button click.
     */
    /**
     * Handles the action when the "Update Reservation" button is clicked.
     * Changes the application's view to the {@code Update.fxml} screen.
     * * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void updateScreen(ActionEvent event) {
        try {
            // Use the static method in Main to switch the scene root to the Update screen
            Main.changeRoot("Update.fxml");
        } catch (Exception e) {
            // Log error if navigation fails (e.g., FXML file not found)
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when the "Subscriber Login" button is clicked.
     * Changes the application's view to the {@code SubscriberLogin.fxml} screen.
     * * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void subscriberLogin(ActionEvent event) {
        try {
            Main.changeRoot("SubscriberLogin.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles the action when the "New Reservation" button is clicked.
     * Changes the application's view to the {@code Order.fxml} screen.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void openNewReservation(ActionEvent event) {
        try {
            Main.changeRoot("Order.fxml");
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
            Main.changeRoot("WaitingList.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles the action when the "Back to Login" button is clicked.
     * 
     * Returns the user to the login menu screen.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void backToLoginMenu(ActionEvent event) {
        try {
            Main.changeRoot("LoginMenu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
