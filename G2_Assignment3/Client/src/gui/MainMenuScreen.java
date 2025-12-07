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

}