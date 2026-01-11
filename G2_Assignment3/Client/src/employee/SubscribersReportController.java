package employee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class SubscribersReportController {

	@FXML
	private ComboBox<Integer> reportsCB;

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
	void backToMenu(ActionEvent event) {

	}

}
