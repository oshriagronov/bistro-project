package logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private String time; check if to delete this.
	private LocalDate order_date;
	private int number_of_guests;
	private int confirmation_code;
	private int subscriber_id;
	private int orderNumber;
	private LocalDate date_of_placing_order;
	private LocalTime start_time;
	private LocalTime finish_time;
	private String phone_number;
	private Status status;
	private String email;

	public Reservation(LocalDate order_date, int number_of_guests, int subscriber_id,
			LocalDate date_of_placing_order, LocalTime start_time, String phone_number, Status status, String email) {
		this.order_date = order_date;
		this.start_time = start_time;
		this.finish_time = start_time.plusHours(2);
		this.date_of_placing_order = date_of_placing_order;
		this.number_of_guests = number_of_guests;
		this.status = status;
		this.subscriber_id = subscriber_id;
		this.phone_number = phone_number;
		this.status=status;
		this.email=email;
	}

	public Reservation(LocalDate order_date, int number_of_guests, int confirmation_code, int subscriber_id,
			LocalDate date_of_placing_order, LocalTime start_time, LocalTime finish_time, String phone_number,
			Status status, String email) {
		this.order_date = order_date;
		this.start_time = start_time;
		this.finish_time = finish_time;
		this.date_of_placing_order = date_of_placing_order;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.phone_number = phone_number;
		this.status = status;
		this.email=email;
	}

	public LocalTime getStart_time() {
		return start_time;
	}

	public LocalTime getFinish_time() {
		return finish_time;
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

	public Status getStatus() {
		return status;
	}
	public String getEmail() {
		return email;
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

	public void setStatus(Status status) {
		this.status = status;

	}

	public void setStart_time(LocalTime start_time) {
		this.start_time = start_time;
	}

	public void setFinish_time(LocalTime finish_time) {
		this.finish_time = finish_time;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public void setEmail(String email) {
		this.email=email;
	}

	
}
