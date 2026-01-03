package employee;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.StatusCounts;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class StatusReportController {

	public static final String fxmlPath = "/employee/TimingReport.fxml";
	@FXML
	private NumberAxis yAxis;

	@FXML
	private Label reportTitle;

	@FXML
	private ComboBox<Integer> yearComboBox;

	@FXML
	private BarChart<String, Number> statusBarChart;

	@FXML
	public void initialize() {
		initYearPicker();
	}

	private void initYearPicker() {
		int currentYear = LocalDate.now().getYear();

		for (int y = currentYear; y >= currentYear - 4; y--) {
			yearComboBox.getItems().add(y);
		}

		yearComboBox.setValue(currentYear);
		yearComboBox.setOnAction(e -> loadYear(yearComboBox.getValue()));

		loadYear(currentYear);
	}

	private void loadYear(int year) {
		reportTitle.setText("Timing Report – " + year);

		Main.client.accept(new BistroRequest(BistroCommand.GET_TIMINGS, year));

		@SuppressWarnings("unchecked")
		List<StatusCounts> rows = (List<StatusCounts>) Main.client.getResponse().getData();

		if (rows == null)
			rows = new ArrayList<>();

		// Map month → StatusCounts
		Map<Integer, StatusCounts> byMonth = new HashMap<>();
		for (StatusCounts r : rows) {
			byMonth.put(r.month, r);
		}

		String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

		XYChart.Series<String, Number> onTime = new XYChart.Series<>();
		onTime.setName("On time");

		XYChart.Series<String, Number> late = new XYChart.Series<>();
		late.setName("Late");

		XYChart.Series<String, Number> cancelled = new XYChart.Series<>();
		cancelled.setName("Cancelled");

		for (int m = 1; m <= 12; m++) {
			StatusCounts r = byMonth.get(m);

			int onTimeCount = (r == null) ? 0 : r.onTime;
			int lateCount = (r == null) ? 0 : r.late;
			int cancCount = (r == null) ? 0 : r.cancelled;

			String label = months[m - 1];

			onTime.getData().add(new XYChart.Data<>(label, onTimeCount));
			late.getData().add(new XYChart.Data<>(label, lateCount));
			cancelled.getData().add(new XYChart.Data<>(label, cancCount));
		}

		statusBarChart.getData().clear();
		statusBarChart.getData().addAll(cancelled, late, onTime);
		forceIntegerYAxis(statusBarChart);
	}

	@FXML
	void onRefresh(ActionEvent event) {
		Integer year = yearComboBox.getValue();
		if (year != null)
			loadYear(year);
	}

	private void forceIntegerYAxis(BarChart<String, Number> chart) {
		int max = 0;

		for (XYChart.Series<String, Number> s : chart.getData()) {
			for (XYChart.Data<String, Number> d : s.getData()) {
				if (d.getYValue() != null) {
					max = Math.max(max, d.getYValue().intValue());
				}
			}
		}

		int upper = max + 1;

		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(upper);
		yAxis.setTickUnit(1);
		yAxis.setMinorTickVisible(false);
	}
}
