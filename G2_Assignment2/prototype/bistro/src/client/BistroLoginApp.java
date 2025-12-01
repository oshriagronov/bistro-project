package client;
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

public class BistroLoginApp extends Application {
    //also i need another option if user wants to register instead of login
    // i will add a register button below the login button

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bistro Login");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        Label userLabel = new Label("Username:");
        TextField userTextField = new TextField();
        grid.add(userLabel, 0, 0);
        grid.add(userTextField, 1, 0);

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);

        Button loginButton = new Button("Login");
        grid.add(loginButton, 1, 2);

        //add text with question if hr isnt registered yet i need it to be on the right side of the login button
        Label registerPrompt = new Label("Not registered yet?");
        grid.add(registerPrompt, 0, 3);

        Button registerButton = new Button("Register");
        grid.add(registerButton, 1, 3); 
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Register button clicked");
                // Here you can add the logic to open a registration form
            }
        });

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle register button action
        //I need to add register screen after the register button is clicked
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Register button clicked");
                // i need to add form to register new screen: username, password, email, phone number, information
                Stage registerStage = new Stage();
                registerStage.setTitle("Bistro Registration");
                GridPane registerGrid = new GridPane();
                registerGrid.setVgap(10);
                registerGrid.setHgap(10);   
                registerGrid.setPadding(new Insets(10));
                Label newUserLabel = new Label("New Username:");
                TextField newUserTextField = new TextField();
                registerGrid.add(newUserLabel, 0, 0);
                registerGrid.add(newUserTextField, 1, 0);
                Label newPassLabel = new Label("New Password:");
                PasswordField newPassField = new PasswordField();
                registerGrid.add(newPassLabel, 0, 1);
                registerGrid.add(newPassField, 1, 1);
                Label emailLabel = new Label("Email:");
                TextField emailTextField = new TextField();
                registerGrid.add(emailLabel, 0, 2);
                registerGrid.add(emailTextField, 1, 2);
                Label phoneLabel = new Label("Phone Number:");
                TextField phoneTextField = new TextField();
                registerGrid.add(phoneLabel, 0, 3);
                registerGrid.add(phoneTextField, 1, 3);
                Button submitButton = new Button("Submit");
                registerGrid.add(submitButton, 1, 4);
                Scene registerScene = new Scene(registerGrid, 350, 250);
                registerStage.setScene(registerScene);
                registerStage.show();
            
            }
        });
        // End of register button action handler
        //i need to add logic to handle login button click and ro open screen after successful login that can open an order screen

    }
    

    public static void main(String[] args) {
        Application.launch(args);
    }
}
