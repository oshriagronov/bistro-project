package employee;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.AvgStayCounts;
import communication.BistroCommand;
import communication.BistroRequest;
import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import communication.StatusCounts;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * Controller for the Timing Report screen.
 * <p>
 * This screen allows employees to view statistical reports related to
 * reservation timings:
 * <ul>
 * <li>Arrival timing statistics (on-time, late, cancelled)</li>
 * <li>Average staying time of completed reservations</li>
 * </ul>
 * The user can switch between report types and select the desired year.
 */
public class StatusReportController {

	/** FXML path for the timing report screen */
	public static final String fxmlPath = "/employee/TimingReport.fxml";
	private final EventListener ordersListener = t -> onRefresh();

	/** Month labels used for chart X-axis */
	private final String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
			"Dec" };

	@FXML
	private Button menuBtn;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private Label reportTitle;

	@FXML
	private ComboBox<Integer> yearComboBox;

	@FXML
	private BarChart<String, Number> statusBarChart;

	@FXML
	private ComboBox<String> reportTypeComboBox;

	@FXML
	private LineChart<String, Number> departureLineChart;

	@FXML
	private NumberAxis departureYAxis;

	/**
	 * Initializes the controller.
	 * <p>
	 * Sets up the year selector, report type selector, default report view, and
	 * listeners for user interaction.
	 */
	@FXML
	public void initialize() {
		initYearPicker();
		reportTypeComboBox.getItems().addAll("Arrival times", "Departure times");
		reportTypeComboBox.getSelectionModel().selectFirst();
		setReportView("Arrival times");
		reportTypeComboBox.setOnAction(e -> setReportView(reportTypeComboBox.getValue()));
		EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, ordersListener);
	}

	/**
	 * Refreshes the currently selected report.
	 * <p>
	 * Reloads the data based on the selected year and report type.
	 *
	 * @param event the refresh button click event
	 */
	@FXML
	private void onRefresh() {
		Integer year = yearComboBox.getValue();
		if (year == null)
			return;

		String type = reportTypeComboBox.getValue();
		if (type == null)
			return;

		if ("Arrival times".equals(type)) {
			loadYear(year);
		} else {
			loadYearStayinTime(year);
		}
	}

	/**
	 * Initializes the year selection ComboBox.
	 * <p>
	 * Populates the ComboBox with the current year and the previous four years and
	 * registers listeners to reload reports when the year changes.
	 */
	private void initYearPicker() {
		int currentYear = LocalDate.now().getYear();

		for (int y = currentYear; y >= currentYear - 4; y--) {
			yearComboBox.getItems().add(y);
		}

		yearComboBox.setValue(currentYear);
		yearComboBox.setOnAction(e -> loadYear(yearComboBox.getValue()));

		reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == null)
				return;

			int year = yearComboBox.getValue();
			setReportView(newVal);

			if ("Arrival times".equals(newVal)) {
				loadYear(year);
			} else {
				loadYearStayinTime(year);
			}
		});

		yearComboBox.setOnAction(e -> {
			Integer year = yearComboBox.getValue();
			if (year == null)
				return;

			String type = reportTypeComboBox.getValue();
			if ("Departure times".equals(type)) {
				loadYearStayinTime(year);
			} else {
				loadYear(year);
			}
		});
	}

	/**
	 * Loads and displays arrival timing statistics for a given year.
	 * <p>
	 * The report includes counts of on-time, late, and cancelled reservations
	 * grouped by month.
	 *
	 * @param year the year to load data for
	 */
	private void loadYear(int year) {
		reportTitle.setText("Arrival times in – " + year);

		Main.client.accept(new BistroRequest(BistroCommand.GET_TIMINGS, year));

		List<StatusCounts> rows = (List<StatusCounts>) Main.client.getResponse().getData();
		if (rows == null)
			rows = new ArrayList<>();

		Map<Integer, StatusCounts> byMonth = new HashMap<>();
		for (StatusCounts r : rows) {
			byMonth.put(r.getMonth(), r);
		}

		XYChart.Series<String, Number> onTime = new XYChart.Series<>();
		onTime.setName("On time");

		XYChart.Series<String, Number> late = new XYChart.Series<>();
		late.setName("Late");

		XYChart.Series<String, Number> cancelled = new XYChart.Series<>();
		cancelled.setName("Cancelled");

		for (int m = 1; m <= 12; m++) {
			StatusCounts r = byMonth.get(m);
			int onTimeCount = (r == null) ? 0 : r.getOnTime();
			int lateCount = (r == null) ? 0 : r.getLate();
			int cancCount = (r == null) ? 0 : r.getCancelled();

			String label = months[m - 1];
			onTime.getData().add(new XYChart.Data<>(label, onTimeCount));
			late.getData().add(new XYChart.Data<>(label, lateCount));
			cancelled.getData().add(new XYChart.Data<>(label, cancCount));
		}

		statusBarChart.getData().clear();
		statusBarChart.getData().addAll(cancelled, late, onTime);
		forceIntegerYAxis(statusBarChart);
	}

	/**
	 * Loads and displays the average staying time report for a given year.
	 * <p>
	 * The data includes only completed reservations and is grouped by month.
	 *
	 * @param year the year to load data for
	 */
	private void loadYearStayinTime(int year) {
		reportTitle.setText("Average staying time in – " + year);

		Main.client.accept(new BistroRequest(BistroCommand.GET_STAYING_TIMES, year));

		List<AvgStayCounts> rows = (List<AvgStayCounts>) Main.client.getResponse().getData();
		if (rows == null)
			rows = new ArrayList<>();

		Map<Integer, AvgStayCounts> byMonth = new HashMap<>();
		for (AvgStayCounts r : rows) {
			byMonth.put(r.getMonth(), r);
		}

		XYChart.Series<String, Number> averageTimes = new XYChart.Series<>();
		averageTimes.setName("Avg stay (min)");

		for (int m = 1; m <= 12; m++) {
			AvgStayCounts r = byMonth.get(m);
			double avg = (r == null) ? 0.0 : r.getAvgMinutes();
			String label = months[m - 1];
			averageTimes.getData().add(new XYChart.Data<>(label, avg));
		}

		departureLineChart.getData().setAll(averageTimes);
	}

	/**
	 * Forces the Y-axis of a bar chart to display only integer values.
	 *
	 * @param chart the bar chart to update
	 */
	private void forceIntegerYAxis(BarChart<String, Number> chart) {
		int max = 0;

		for (XYChart.Series<String, Number> s : chart.getData()) {
			for (XYChart.Data<String, Number> d : s.getData()) {
				if (d.getYValue() != null) {
					max = Math.max(max, d.getYValue().intValue());
				}
			}
		}

		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(max + 1);
		yAxis.setTickUnit(1);
		yAxis.setMinorTickVisible(false);
	}

	/**
	 * Switches the visible report view between arrival and departure reports.
	 *
	 * @param type the selected report type
	 */
	private void setReportView(String type) {
		boolean arrival = "Arrival times".equals(type);

		statusBarChart.setVisible(arrival);
		statusBarChart.setManaged(arrival);

		departureLineChart.setVisible(!arrival);
		departureLineChart.setManaged(!arrival);

		reportTitle.setText(arrival ? "Timing Report (Arrival)" : "Timing Report (Average staying time)");
	}

	@FXML
	void backToMenu(ActionEvent event) {
		try {
			Main.changeRoot(employeeMenu.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClose() {
		EventBus.getInstance().unsubscribe(EventType.ORDER_CHANGED, ordersListener);
	}
}
