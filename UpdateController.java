package GUI;

import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class UpdateController {

	@FXML
	private CheckBox amountCB;

	@FXML
	private CheckBox dateCB;

	@FXML
	private ComboBox<String> dinersAmount;

	@FXML
	private TextField orderNumber;

	@FXML
	private DatePicker orderDate;

	@FXML
	private Button submitBTN;

	@FXML
	private HBox dateHbox;

	@FXML
	private HBox amountHbox;

	@FXML
	public void initialize() {
		dateHbox.setVisible(false);
		amountHbox.setVisible(false);
		orderDate.setDayCellFactory(d -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);

				LocalDate today = LocalDate.now();
				LocalDate maxDate = today.plusMonths(1);

				if (item.isBefore(today) || item.isAfter(maxDate)) {
					setDisable(true);
					setStyle("-fx-background-color: #ccc;");
				}
			}
		});
		// diners amount: 1–10
		dinersAmount.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			dinersAmount.getItems().add(String.valueOf(i));
		}
	}

	@FXML
	public void checkAmountClicked(ActionEvent e) {
		if (amountCB.isSelected())
			amountHbox.setVisible(true);
		else
			amountHbox.setVisible(false);
	}

	@FXML
	public void checkDateClicked(ActionEvent e) {
		if (dateCB.isSelected())
			dateHbox.setVisible(true);
		else
			dateHbox.setVisible(false);
	}

	@FXML
	void submit(ActionEvent event) {

	}

}
