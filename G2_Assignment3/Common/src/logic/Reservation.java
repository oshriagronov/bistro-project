package logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a reservation request including guest counts, timing, and contact data.
 * Instances capture both subscribers and walk-ins and carry an optional confirmation code.
 */
public class Reservation implements Serializable {
	/** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	// private String time; check if to delete this.
	private LocalDate order_date;
	private int number_of_guests;
	private String confirmation_code;
	private int subscriber_id;
	private int orderNumber;
	private LocalDate date_of_placing_order;
	private LocalTime start_time;
	private LocalTime finish_time;
	private String phone_number;
	private Status status;
	private String email;

	/**
	 * Constructs a reservation without a confirmation code, setting a default two hour window.
	 */
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

	/**
	 * Constructs a reservation with an explicit confirmation code and explicit window.
	 */
	public Reservation(LocalDate order_date, int number_of_guests, String confirmation_code, int subscriber_id,
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

	/** @return the reservation start time */
	public LocalTime getStart_time() {
		return start_time;
	}

	/** @return the reservation end time */
	public LocalTime getFinish_time() {
		return finish_time;
	}

	/** @return the date the reservation is for */
	public LocalDate getOrderDate() {
		return order_date;
	}

	/** @return when the reservation was placed */
	public LocalDate getDateOfPlacingOrder() {
		return date_of_placing_order;
	}

	/** @return how many guests are expected */
	public int getNumberOfGuests() {
		return number_of_guests;
	}

	/** @return the optional confirmation code */
	public String getConfirmationCode() {
		return confirmation_code;
	}

	/** @return the subscriber identifier associated with the reservation */
	public int getSubscriberId() {
		return subscriber_id;
	}

	/** @return the contact phone number */
	public String getPhone_number() {
		return phone_number;
	}

	/** @return the internal order number (if assigned) */
	public int getOrderNumber() {
		return orderNumber;
	}

	/** @return the current reservation status */
	public Status getStatus() {
		return status;
	}

	/** @return the contact email address */
	public String getEmail() {
		return email;
	}
	/** @param order_date the reservation date to set */
	public void setOrder_date(LocalDate order_date) {
		this.order_date = order_date;
	}

	/** @param number_of_guests the guest count to set */
	public void setNumber_of_guests(int number_of_guests) {
		this.number_of_guests = number_of_guests;
	}

	/** @param confirmation_code the confirmation code to set */
	public void setConfirmation_code(String confirmation_code) {
		this.confirmation_code = confirmation_code;
	}

	/** @param subscriber_id the subscriber ID to record */
	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	/** @param date_of_placing_order when the reservation was placed */
	public void setDate_of_placing_order(LocalDate date_of_placing_order) {
		this.date_of_placing_order = date_of_placing_order;
	}

	/** @param phone_number the contact phone to use */
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	/** @param status the reservation status to store */
	public void setStatus(Status status) {
		this.status = status;
	}

	/** @param start_time the reservation start time */
	public void setStart_time(LocalTime start_time) {
		this.start_time = start_time;
	}

	/** @param finish_time the reservation end time */
	public void setFinish_time(LocalTime finish_time) {
		this.finish_time = finish_time;
	}

	/** @param orderNumber the internal order number to assign */
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	/** @param email the contact email address */
	public void setEmail(String email) {
		this.email=email;
	}

}
