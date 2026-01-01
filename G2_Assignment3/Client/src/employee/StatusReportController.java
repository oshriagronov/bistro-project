package employee; // תעדכן לפי ה-package שלך

import communication.BistroCommand;
import communication.BistroRequest;
import communication.StatusCounts;
import gui.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

public class StatusReportController {
	public static final String fxmlPath = "/employee/TimingReport.fxml";
    @FXML
    private BarChart<String, Number> statusBarChart;

    @FXML
    public void initialize() {
        loadStatusBarChart();
    }

    private void loadStatusBarChart() {
    	
    		
      	Main.client.accept(new BistroRequest(BistroCommand.GET_TIMINGS,null));
      	StatusCounts counts = (StatusCounts)Main.client.getResponse().getData();
        if (counts == null) {
            System.out.println("StatusCounts is null – no data to display");
            return;
        }

        statusBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservation Status");

        series.getData().add(new XYChart.Data<>("Confirmed", counts.confirmed));
        series.getData().add(new XYChart.Data<>("Pending", counts.pending));
        series.getData().add(new XYChart.Data<>("Cancelled", counts.cancelled));

        statusBarChart.getData().add(series);

        // צבעים (אופציונלי)
        Platform.runLater(() -> {
            var data = series.getData();
            data.get(0).getNode().setStyle("-fx-bar-fill: green;");
            data.get(1).getNode().setStyle("-fx-bar-fill: orange;");
            data.get(2).getNode().setStyle("-fx-bar-fill: red;");
        });
    }
}