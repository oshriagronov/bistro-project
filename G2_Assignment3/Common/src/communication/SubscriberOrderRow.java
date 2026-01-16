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

	public int getOrderId() {
		return orderId;
	}

	public String getConfirmationCode() {
		return confirmationCode;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getFinishTime() {
		return finishTime;
	}

	public int getDiners() {
		return diners;
	}

	public Status getStatus() {
		return status;
	}
}
