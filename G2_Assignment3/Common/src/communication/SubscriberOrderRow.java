package communication;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import logic.Status;

/**
 * DTO for displaying a subscriber's order history in TableView. Used for
 * read-only presentation purposes.
 */
public class SubscriberOrderRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private int orderId;
	private String confirmationCode;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime finishTime;
	private int diners;
	private Status status;

	/**
	 * @param orderId order identifier
	 * @param confirmationCode confirmation code assigned to the order
	 * @param date reservation date
	 * @param startTime reservation start time
	 * @param finishTime reservation finish time
	 * @param diners number of diners
	 * @param status reservation status
	 */
	public SubscriberOrderRow(int orderId, String confirmationCode, LocalDate date, LocalTime startTime,
			LocalTime finishTime, int diners, Status status) {
		this.orderId = orderId;
		this.confirmationCode = confirmationCode;
		this.date = date;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.diners = diners;
		this.status = status;
	}

	/** @return the order identifier */
	public int getOrderId() {
		return orderId;
	}

	/** @return the confirmation code */
	public String getConfirmationCode() {
		return confirmationCode;
	}

	/** @return the reservation date */
	public LocalDate getDate() {
		return date;
	}

	/** @return the reservation start time */
	public LocalTime getStartTime() {
		return startTime;
	}

	/** @return the reservation finish time */
	public LocalTime getFinishTime() {
		return finishTime;
	}

	/** @return the number of diners */
	public int getDiners() {
		return diners;
	}

	/** @return the reservation status */
	public Status getStatus() {
		return status;
	}
}
