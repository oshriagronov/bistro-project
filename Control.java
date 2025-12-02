package GUI;

import java.time.LocalDate;
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
	//ChatClient chatClient = new ChatClient("localhost", 5555, ui);

	@FXML
	private ComboBox<String> dinersAmmount;

	@FXML
	private Button orderBtn;

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

	@FXML
	public Reservation clickOrder(ActionEvent event) {
		int amount;
		String phone, ID = null;
		StringBuilder str = new StringBuilder();
		boolean check = true;
		LocalDate date;
		LocalDate today = LocalDate.now();
		LocalDate maxDate = today.plusMonths(1);
		List<String> info = new ArrayList<>();
		Alert alert = new Alert(Alert.AlertType.ERROR);
		info.add("insert"); // 1.add function type
		info.add(order_number.toString()); // 2. add order number
		date = orderDate.getValue(); // 3. add the date of the reservation
		info.add(date.toString());
		amount=Integer.parseInt(dinersAmmount.getValue());
		info.add(dinersAmmount.getValue().toString()); // 4. add the amount of diners
		info.add(confirmation_code.toString()); // 5. add confirmation code
		// info.add(orderHours.getValue().toString() + ":" +
		// orderMinutes.getValue().toString());
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
		if (phone.length() != 7 || phone.matches("[a-zA-Z]+")) {
			str.append("Please enter a valid phone number\n");
			check = false;
		}
		// info.add(phoneStart.getValue() + "-" + phoneNumber.getText());
		if (!check) {
			alert.setTitle("failure");
			alert.setHeaderText(null);
			alert.setContentText(str.toString());
			alert.showAndWait();
			info.removeAll(info);
			return null;
		} else {
			alert.setTitle("success");
			alert.setHeaderText(null);
			alert.setContentText("Your order has been placed!");
			alert.showAndWait();
			System.out.println("INFO ARRAYLIST = " + info);
			Reservation r = new Reservation( date , amount, confirmation_code, Integer.parseInt(ID), today);
			order_number++;
			confirmation_code++;
			return r;
			//ChatClient.handleMessageFromClientUI(r);
		}
	}

}
