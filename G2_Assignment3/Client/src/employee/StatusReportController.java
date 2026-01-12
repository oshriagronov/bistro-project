package employee;

import java.time.LocalDate;
import java.time.YearMonth;
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
import communication.RequestFactory;
import communication.StatusCounts;
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
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

	@FXML
	private Button menuBtn;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private Label reportTitle;

	@FXML
	private ComboBox<Integer> monthComboBox;

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

	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	/**
	 * Initializes the controller.
	 * <p>
	 * Sets up the year selector, report type selector, default report view, and
	 * listeners for user interaction.
	 */
	@FXML
	public void initialize() {
		initYearPicker();
		initMonthPicker();
		statusBarChart.setCategoryGap(8);
		statusBarChart.setBarGap(2);
		reportTypeComboBox.getItems().addAll("Arrival times", "Departure times");
		reportTypeComboBox.getSelectionModel().selectFirst();
		setReportView("Arrival times");
		EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, ordersListener);
		initDefaultSelectionToLastAvailable();
		onRefresh();
	}

	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
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
		Integer month = monthComboBox.getValue();
		if (year == null || month == null)
			return;

		if (!canViewMonthlyReport(year, month)) {
			showAlert("Not available yet", "This monthly report is available only after the month ends.");
			return;
		}

		String type = reportTypeComboBox.getValue();
		if (type == null)
			return;

		if ("Arrival times".equals(type)) {
			loadMonth(year, month);
		} else {
			loadMonthStayinTime(year, month);
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
		for (int y = currentYear; y >= currentYear - 2; y--) {
			yearComboBox.getItems().add(y);
		}
		yearComboBox.setValue(currentYear);

		yearComboBox.setOnAction(e -> onRefresh());
		reportTypeComboBox.setOnAction(e -> {
			setReportView(reportTypeComboBox.getValue());
			onRefresh();
		});
	}

	private void initMonthPicker() {
		for (int m = 1; m <= 12; m++) {
			monthComboBox.getItems().add(m);
		}
		monthComboBox.setValue(LocalDate.now().getMonthValue());
		monthComboBox.setOnAction(e -> onRefresh());
	}

	/**
	 * Loads and displays arrival timing statistics for a given year.
	 * <p>
	 * The report includes counts of on-time, late, and cancelled reservations
	 * grouped by month.
	 *
	 * @param year the year to load data for
	 */
	private void loadMonth(int year, int month) {
		reportTitle.setText("Arrival times – " + year + "-" + String.format("%02d", month));

		Main.client.accept(RequestFactory.getMonthlyArrivalTimesReport(YearMonth.of(year, month)));

		List<StatusCounts> rows = (List<StatusCounts>) Main.client.getResponse().getData();
		if (rows == null)
			rows = new ArrayList<>();

		Map<Integer, StatusCounts> byDay = new HashMap<>();
		for (StatusCounts r : rows) {
			byDay.put(r.getDay(), r);
		}

		XYChart.Series<String, Number> onTime = new XYChart.Series<>();
		onTime.setName("On time");

		XYChart.Series<String, Number> late = new XYChart.Series<>();
		late.setName("Late");

		XYChart.Series<String, Number> cancelled = new XYChart.Series<>();
		cancelled.setName("Cancelled");

		int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();

		for (int m = 1; m <= daysInMonth; m++) {
			StatusCounts r = byDay.get(m);
			int onTimeCount = (r == null) ? 0 : r.getOnTime();
			int lateCount = (r == null) ? 0 : r.getLate();
			int cancCount = (r == null) ? 0 : r.getCancelled();

			String label = String.valueOf(m);
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
	private void loadMonthStayinTime(int year, int month) {

		YearMonth ym = YearMonth.of(year, month);
		reportTitle.setText("Average staying time – " + year + "-" + String.format("%02d", month));

		Main.client.accept(RequestFactory.getMonthlyStayingTimesReport(ym));

		List<AvgStayCounts> rows = (List<AvgStayCounts>) Main.client.getResponse().getData();
		if (rows == null)
			rows = new ArrayList<>();

		Map<Integer, AvgStayCounts> byDay = new HashMap<>();
		for (AvgStayCounts r : rows) {
			byDay.put(r.getDay(), r);
		}

		XYChart.Series<String, Number> averageTimes = new XYChart.Series<>();
		averageTimes.setName("Avg stay (min)");

		int daysInMonth = ym.lengthOfMonth();

		for (int day = 1; day <= daysInMonth; day++) {
			AvgStayCounts r = byDay.get(day);
			double avg = (r == null) ? 0.0 : r.getAvgMinutes();

			String label = String.valueOf(day);
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

	private boolean canViewMonthlyReport(int year, int month) {
		YearMonth selected = YearMonth.of(year, month);
		LocalDate today = LocalDate.now();

		YearMonth current = YearMonth.from(today);
		if (selected.isAfter(current))
			return false;

		if (selected.isBefore(current))
			return true;

		return today.getDayOfMonth() == current.lengthOfMonth();
	}
	
	private YearMonth lastAvailableMonth() {
	    LocalDate today = LocalDate.now();
	    YearMonth current = YearMonth.from(today);

	    boolean monthEnded = today.getDayOfMonth() == current.lengthOfMonth();
	    return monthEnded ? current : current.minusMonths(1);
	}
	
	
	private void initDefaultSelectionToLastAvailable() {
	    YearMonth ym = lastAvailableMonth();

	    // תוודא שהשנה קיימת ברשימה (אם אתה מוסיף רק 4 שנים אחורה)
	    if (!yearComboBox.getItems().contains(ym.getYear())) {
	        yearComboBox.getItems().add(ym.getYear());
	    }

	    yearComboBox.setValue(ym.getYear());
	    monthComboBox.setValue(ym.getMonthValue());
	}


	@FXML
	void backToMenu(ActionEvent event) {
		try {
			Main.changeRoot(ReportsMenuScreen.fxmlPath, 600, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClose() {
		EventBus.getInstance().unsubscribe(EventType.ORDER_CHANGED, ordersListener);
	}
}
