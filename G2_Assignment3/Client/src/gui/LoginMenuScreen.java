/**
 * Controller class for the LoginMenuScreen.fxml view.
 * 
 * This class is responsible for handling the navigation logic
 * of the login menu. It allows the user to choose how to continue
 * in the system: as a subscriber, as an employee, or as a guest.
 */
package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
public class LoginMenuScreen {

    /**
     * Handles the action when the "Login as Subscriber" button is clicked.
     * 
     * Changes the application's view to the SubscriberLoginScreen.fxml.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void loginAsSubscriber(ActionEvent event) {
        try {
            // Switch to the subscriber login screen
            Main.changeRoot("/subscriber/SubscriberLogin.fxml");
        } catch (Exception e) {
            // Prints the error if the FXML file cannot be loaded
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when the "Login as Employee" button is clicked.
     * 
     * Changes the application's view to the EmployeeLoginScreen.fxml.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void loginAsEmployee(ActionEvent event) {
        try {
            // Switch to the employee login screen
            Main.changeRoot("WorkersLogin.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when the "Continue as Guest" button is clicked.
     * 
     * Changes the application's view to the GuestScreen.fxml.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void continueAsGuest(ActionEvent event) {
        try {
            // Switch to the guest screen
            Main.changeRoot("MainMenu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
