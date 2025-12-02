package GUI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class UpdateController {
	Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
	private Button menuBTN;

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

	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	List<String> submit(ActionEvent event) {
		StringBuilder str = new StringBuilder();
		boolean check = true;
		List<String> info = new ArrayList<>();
		String order_number;
		LocalDate date = orderDate.getValue();
		order_number = orderNumber.getText();
		info.add("update"); // 1. add type
		if (order_number == null || order_number.isBlank()) // maybe check other stuff?
		{
			check = false;
			str.append("Please enter an order number\n");
		} else
			info.add(order_number); // 2. add order number
		if (date == null && dateHbox.isVisible()) {
			check = false;
			str.append("please pick a date\n");
		} else if (dateHbox.isVisible())
			info.add(date.toString()); // 3. add reservation date 
		if (dinersAmount.getValue() == null && amountHbox.isVisible()) {
			check = false;
			str.append("Please choose the diners amount\n");
		} else if (amountHbox.isVisible())
			info.add(dinersAmount.getValue().toString()); // 4. add diners amount
		if (!check) {
			showAlert("failure", str.toString());
			return null;
		} else {
			showAlert("success", "Reservation successfuly placed");
			System.out.println("INFO ARRAYLIST = " + info);
			return info;
		}

	}

	@FXML
	void backToMenu(ActionEvent event) {
		try {
			Main.changeRoot("menu.fxml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
