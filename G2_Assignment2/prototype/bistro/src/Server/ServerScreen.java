package Server;
import client.BistroClient;
import client.ClientController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class ServerScreen extends Application {

    private int portnumber = Server.DEFAULT_PORT;

    private String [] logger;
    private ClientController clientController;

    private TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Server Status");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        Label statusLabel = new Label("Server port:");
        Label statusValue = new Label("Server is running on port " + portnumber);

        grid.add(statusLabel, 0, 0);
        grid.add(statusValue, 1, 0);

        // TEXT AREA TO DISPLAY SERVER LOGS
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefSize(350, 200);
        grid.add(logArea, 0, 1, 2, 1); // span 2 columns

        // REFRESH BUTTON
        Button refreshButton = new Button("Refresh logs");
        refreshButton.setOnAction((ActionEvent event) -> {
            clientController.accept("log");
            setLogs();
        });

        grid.add(refreshButton, 1, 2);

        Scene scene = new Scene(grid, 450, 350);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize ClientController AFTER UI
        clientController = ClientController.getInstance("localhost", Server.DEFAULT_PORT);
    }

    // Update the logs visually
    public void setLogs() {
    	this.clientController.accept("log");
        this.logger = BistroClient.result.split(",");
        Platform.runLater(() -> {
            logArea.clear();
            for (String log : logger) {
                logArea.appendText(log + "\n");
            }
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
