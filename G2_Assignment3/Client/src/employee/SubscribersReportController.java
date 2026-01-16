package employee;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.AvgWaitTimePerDay;
import communication.EventBus;
import communication.EventListener;
import communication.EventType;
import communication.RequestFactory;
import communication.SubscriberOrderCounts; // <-- תוודא שזה ה-DTO שלך
import gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class SubscribersReportController {

	public static final String fxmlPath = "/employee/SubscribersReport.fxml";

	private final Alert alert = new Alert(Alert.AlertType.INFORMATION);

	private final EventListener ordersListener = t -> onRefresh();

	@FXML
	private Button backBtn;

	@FXML
	private ComboBox<Integer> monthCB;

	@FXML
	private PieChart ordersSplitPieChart;

	@FXML
	private Label reportSubtitleLabel;

	@FXML
	private Label reportTitleLabel;

	@FXML
	private ComboBox<Integer> yearCB;

	@FXML
	private Button refreshBtn;

	@FXML
	private ComboBox<String> reportsCB;

	@FXML
	private Label chartTitleLabel;

	@FXML
	private LineChart<Number, Number> avgWaitLineChart;

	@FXML
	private NumberAxis avgWaitDayAxis;

	@FXML
	private NumberAxis avgWaitMinutesAxis;

	@FXML
	public void initialize() {
		initYearPicker();
		initMonthPicker();
		initReportsPicker();
		setReportView(reportsCB.getValue());
		avgWaitLineChart.setAnimated(false);
		avgWaitLineChart.setLegendVisible(false);
		avgWaitDayAxis.setAutoRanging(false);
		avgWaitDayAxis.setLowerBound(1);
		avgWaitDayAxis.setUpperBound(31);
		avgWaitDayAxis.setTickUnit(1);
		avgWaitDayAxis.setMinorTickVisible(false);
		ordersSplitPieChart.setLegendVisible(true);
		ordersSplitPieChart.setLegendSide(javafx.geometry.Side.BOTTOM);
		ordersSplitPieChart.setStartAngle(90);
		reportTitleLabel.setText("Subscribers Report");
		reportSubtitleLabel.setText("Monthly overview: subscriber orders vs non-subscriber orders.");
		initDefaultSelectionToLastAvailable();
		EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, ordersListener);
		onRefresh();
	}

	private void initYearPicker() {
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= currentYear - 2; y--) {
			yearCB.getItems().add(y);
		}
		yearCB.setValue(currentYear);
		yearCB.setOnAction(e -> onRefresh());
	}

	private void initMonthPicker() {
		for (int m = 1; m <= 12; m++) {
			monthCB.getItems().add(m);
		}
		monthCB.setValue(LocalDate.now().getMonthValue());
		monthCB.setOnAction(e -> onRefresh());
	}

	private void initReportsPicker() {
		reportsCB.getItems().addAll("Orders split", "Average wait time");
		reportsCB.getSelectionModel().selectFirst();

		reportsCB.setOnAction(e -> {
			setReportView(reportsCB.getValue());
			onRefresh();
		});
	}

	private void setReportView(String type) {
		boolean split = "Orders split".equals(type);

		ordersSplitPieChart.setVisible(split);
		ordersSplitPieChart.setManaged(split);

		avgWaitLineChart.setVisible(!split);
		avgWaitLineChart.setManaged(!split);

		chartTitleLabel.setText(split ? "Orders split" : "Average wait time per day");
	}

	private void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	private void onRefresh() {
		Integer year = yearCB.getValue();
		Integer month = monthCB.getValue();
		if (year == null || month == null)
			return;

		if (!canViewMonthlyReport(year, month)) {
			showAlert("Not available yet", "This monthly report is available only after the month ends.");
			return;
		}

		String type = reportsCB.getValue();
		if (type == null)
			return;

		if ("Orders split".equals(type)) {
			loadSubscribersSplit(year, month);
		} else {
			loadAvgWaitTime(year, month);
		}
	}

	private void loadSubscribersSplit(int year, int month) {
		YearMonth ym = YearMonth.of(year, month);
		reportTitleLabel.setText("Subscribers Report – " + year + "-" + String.format("%02d", month));
		reportSubtitleLabel.setText("Orders split for " + ym);
		Main.client.accept(RequestFactory.getSubscriberOrderCounts(year, month));
		Object data = Main.client.getResponse().getData();
		SubscriberOrderCounts counts = null;
		if (data instanceof SubscriberOrderCounts) {
			counts = (SubscriberOrderCounts) data;
		}
		int subs = (counts == null) ? 0 : counts.getSubscriberOrders();
		int nonSubs = (counts == null) ? 0 : counts.getNonSubscriberOrders();
		int total = subs + nonSubs;
		if (total == 0) {
			ordersSplitPieChart.getData().clear();
			return;
		}

		double subsPct = subs * 100.0 / total;
		double nonSubsPct = nonSubs * 100.0 / total;

		ordersSplitPieChart.getData().setAll(new PieChart.Data(String.format("Subscribers (%.1f%%)", subsPct), subs),
				new PieChart.Data(String.format("Non-subscribers (%.1f%%)", nonSubsPct), nonSubs));

		if (subs == 0 && nonSubs == 0) {
			ordersSplitPieChart.setTitle("No orders found for " + ym);
		} else {
			ordersSplitPieChart.setTitle("Orders – " + ym);
		}

		ordersSplitPieChart.setLabelsVisible(false);
	}

	private void loadAvgWaitTime(int year, int month) {
		YearMonth ym = YearMonth.of(year, month);

		reportTitleLabel.setText("Subscribers Report – " + year + "-" + String.format("%02d", month));
		reportSubtitleLabel.setText("Average wait time per day for " + ym);

		Main.client.accept(RequestFactory.getDailyAverageWaitTimeReport(year, month));

		Object data = Main.client.getResponse().getData();

		List<AvgWaitTimePerDay> rows = new ArrayList<>();
		if (data instanceof List<?>) {
			for (Object o : (List<?>) data) {
				if (o instanceof AvgWaitTimePerDay r) {
					rows.add(r);
				}
			}
		}

		Map<Integer, AvgWaitTimePerDay> byDay = new HashMap<>();
		for (AvgWaitTimePerDay r : rows) {
			byDay.put(r.getDay(), r);
		}

		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Avg wait (min)");

		int daysInMonth = ym.lengthOfMonth();
		for (int day = 1; day <= daysInMonth; day++) {
			AvgWaitTimePerDay r = byDay.get(day);
			double avg = (r == null) ? 0.0 : r.getAvgWaitMinutes();
			series.getData().add(new XYChart.Data<>(day, avg));
		}

		avgWaitLineChart.getData().setAll(series);

		if (rows.isEmpty()) {
			avgWaitLineChart.setTitle("No waitlist data for " + ym);
		} else {
			avgWaitLineChart.setTitle("Average wait time – " + ym);
		}
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
		if (!yearCB.getItems().contains(ym.getYear())) {
			yearCB.getItems().add(ym.getYear());
		}
		yearCB.setValue(ym.getYear());
		monthCB.setValue(ym.getMonthValue());
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
