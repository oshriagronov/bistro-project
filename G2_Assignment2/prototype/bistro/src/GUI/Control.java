package GUI;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import java.util.ArrayList;
import java.util.List;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class Control {
	static Integer order_number = 0, confirmation_code = 10000;
	Alert alert = new Alert(Alert.AlertType.INFORMATION);

	@FXML
	private ComboBox<String> dinersAmmount;

	@FXML
	private Button orderBtn;

	@FXML
	private Button menuBtn;

	@FXML
	private DatePicker orderDate;

	@FXML
	private TextField orderEmail;

	@FXML
	private ComboBox<String> orderHours;

	@FXML
	private ComboBox<String> orderMinutes;

	@FXML
	private TextField phoneNumber;

	@FXML
	private ComboBox<String> phoneStart;

	@FXML
	private CheckBox checkBox;

	@FXML
	private TextField subID;

	@FXML
	private HBox subHBOX;

	@FXML
	public void initialize() {
		subHBOX.setVisible(false);
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
		dinersAmmount.getItems().clear();
		for (int i = 1; i <= 10; i++) {
			dinersAmmount.getItems().add(String.valueOf(i));
		}

		// hours: 00–23
		orderHours.getItems().clear();
		for (int i = 12; i < 23; i++) {
			orderHours.getItems().add(String.format("%02d", i));
		}

		// minutes: every 5 minutes (00, 05, 10, ... 55)
		orderMinutes.getItems().clear();
		for (int i = 0; i < 60; i += 30) {
			orderMinutes.getItems().add(String.format("%02d", i));
		}

		// phone prefixes
		phoneStart.getItems().clear();
		phoneStart.getItems().addAll("050", "052", "053", "054", "055", "058");

		// optional: select default values
		// dinersAmmount.getSelectionModel().selectFirst();
		// orderHours.getSelectionModel().select("19"); // example default
		// orderMinutes.getSelectionModel().select("00");
		// phoneStart.getSelectionModel().selectFirst();
	}

	@FXML
	public void checkClicked(ActionEvent e) {
		if (checkBox.isSelected())
			subHBOX.setVisible(true);
		else
			subHBOX.setVisible(false);
	}

	public void showAlert(String title, String body) {
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(body);
		alert.showAndWait();
	}

	@FXML
	public Reservation clickOrder(ActionEvent event) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourFromNow = now.plusHours(1);
		int amount = 0, hours, minutes;
		String phone, ID = null, amountStr = "1";
		StringBuilder str = new StringBuilder();
		boolean check = true;
		LocalDate date;
		LocalDate today = LocalDate.now();
		LocalDate maxDate = today.plusMonths(1);
		List<String> info = new ArrayList<>();
		info.add("insert"); // 1.add function type
		info.add(order_number.toString()); // 2. add order number
		date = orderDate.getValue(); // 3. add the date of the reservation
		if (date == null) {
			str.append("Please pick a reservation date\n");
			check = false;
		} else
			info.add(date.toString());
		amountStr = dinersAmmount.getValue();
		if (amountStr == null || amountStr.isBlank()) {
			str.append("Please choose diners amount\n");
			check = false;
		} else {
			amount = Integer.parseInt(amountStr); // safe now
			info.add(amountStr); // 4. add amount of diners
		}
		info.add(confirmation_code.toString()); // 5. add confirmation code
		if (orderHours.getValue() == null || orderMinutes.getValue() == null) {
			check = false;
			str.append("Please select reservation time\n");
		} else {
			hours = Integer.parseInt(orderHours.getValue());
			minutes = Integer.parseInt(orderMinutes.getValue());
			LocalDateTime selected = date.atTime(hours, minutes);
			if (date.equals(today) && selected.isBefore(oneHourFromNow)) {
				check = false;
				str.append("Please select a time that is at leaset one hour ahead of now\n");
			}
		}
		if (!orderEmail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			str.append("Please enter a valid Email\n");
			check = false;
		}
		if (!checkBox.isSelected())
			info.add("0");
		else {
			ID = subID.getText();
			if (ID == null || ID.length() != 5) {
				str.append("Please enter a valid subscriber ID\n");
				check = false;
			} else
				info.add(ID);// 6. add subscriber id
		}
		info.add(today.toString());// 7.add the date of order placement
		// info.add(orderEmail.getText());
		phone = phoneNumber.getText();
		if (phone == null || phone.length() != 7 || phone.matches("[a-zA-Z]+")) {
			str.append("Please enter a valid phone number\n");
			check = false;
		}
		// info.add(phoneStart.getValue() + "-" + phoneNumber.getText());
		if (!check) {
			showAlert("failure", str.toString());
			return null;
		} else {
			showAlert("success", "Reservation successfully placed");
			System.out.println("INFO ARRAYLIST = " + info);
			Reservation r = new Reservation(date, amount, confirmation_code, Integer.parseInt(ID), today);
			order_number++;
			confirmation_code++;
			return r;
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
