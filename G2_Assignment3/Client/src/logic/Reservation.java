package logic;
import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable{
	//private String time; check if to delete this.
	private LocalDate order_date;
	private int number_of_guests;
	private int confirmation_code;
	private int subscriber_id;
	private LocalDate date_of_placing_order;
	
	public Reservation(LocalDate order_date, int number_of_guests, int confirmation_code, int subscriber_id, LocalDate date_of_placing_order) {
		this.order_date = order_date;
		this.date_of_placing_order = date_of_placing_order;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
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
}
