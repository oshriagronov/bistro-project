import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene; 
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class ServerScreen extends Application {
    //I nees to create a server screen with javaFX that will present server status and data from clients
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Server Status");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));
        //I nees a text box that will show server connection status
        Label statusLabel = new Label();
        TextField statusTextField = new TextField();
        statusTextField.setPrefHeight(150);
        statusTextField.setPrefWidth(200);

        grid.add(statusLabel, 0, 0);
        grid.add(statusTextField, 1, 0);    

        Button refreshButton = new Button("Refresh");
        grid.add(refreshButton, 1, 2);
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Refresh button clicked");
                // Here you can add the logic to refresh server status and client data
            }
        });

        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.show();

}
    

    public static void main(String[] args) {
        Application.launch(args);
    }
}
