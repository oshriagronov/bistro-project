package logic;
import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable{
	//private String time; check if to delete this.
	private LocalDate order_date;
	private int number_of_guests;
	private int confirmation_code;
	private int subscriber_id;
	private int orderNumber;
	private LocalDate date_of_placing_order;
	private String phone_number;
	
	public Reservation(LocalDate order_date, int number_of_guests, int confirmation_code, int subscriber_id, LocalDate date_of_placing_order) {
		this.order_date = order_date;
		this.date_of_placing_order = date_of_placing_order;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
	}
	
	public Reservation(int number_of_guests, int subscriber_id, String phone_number) {
		this.number_of_guests = number_of_guests;
		this.subscriber_id = subscriber_id;
		this.phone_number = phone_number;
	}


	public LocalDate getOrderDate() {
		return order_date;
	}
	public LocalDate getDateOfPlacingOrder() {
		return date_of_placing_order;
	}
	public int getNumberOfGuests() {
		return number_of_guests;
	}
	public int getConfirmationCode() {
		return confirmation_code;
	}
	public int getSubscriberId() {
		return subscriber_id;
	}
	
	public String getPhone_number() {
		return phone_number;
	}
	
	public int getOrderNumber() {
		return orderNumber;
	}


	public void setOrder_date(LocalDate order_date) {
		this.order_date = order_date;
	}

	public void setNumber_of_guests(int number_of_guests) {
		this.number_of_guests = number_of_guests;
	}

	public void setConfirmation_code(int confirmation_code) {
		this.confirmation_code = confirmation_code;
	}

	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	public void setDate_of_placing_order(LocalDate date_of_placing_order) {
		this.date_of_placing_order = date_of_placing_order;
	}
	
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
	
}
