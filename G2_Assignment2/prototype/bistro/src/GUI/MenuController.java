package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MenuController {

    @FXML
    private Button newReservation;

    @FXML
    private Button updateReservation;

    @FXML
    void newScreen(ActionEvent event) {
    	try {
            Main.changeRoot("MidPro.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateScreen(ActionEvent event) {
    	   try {
    	        Main.changeRoot("updateScreen.fxml");
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    }

}