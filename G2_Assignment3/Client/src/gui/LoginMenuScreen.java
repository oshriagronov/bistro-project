/**
 * Controller class for the LoginMenuScreen.fxml view.
 * 
 * This class is responsible for handling the navigation logic
 * of the login menu. It allows the user to choose how to continue
 * in the system: as a subscriber, as an employee, or as a guest.
 */
package gui;

import employee.WorkersLogInScreen;
import employee.employeeMenu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import subscriber.SubscriberLoginScreen;
public class LoginMenuScreen {
    public static final String fxmlPath = "/gui/LoginMenu.fxml";
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
            Main.changeRoot(SubscriberLoginScreen.fxmlPath);
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
            Main.changeRoot(WorkersLogInScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action when the "Continue as Guest" button is clicked.
     * 
     * Changes the application's view to the Order.fxml.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void continueAsGuest(ActionEvent event) {
        try {
            // Switch to the guest screen
            Main.changeRoot(MainMenuScreen.fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
