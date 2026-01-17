package communication;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/** Request body for fetching reservations inside a time window. */
public class OrdersInRangeRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	private LocalDate date;
	private LocalTime time;

	/** @param date target date
	 *  @param time target time */
	public OrdersInRangeRequest(LocalDate date, LocalTime time) {
		super();
		this.date = date;
		this.time = time;
	}

	/** @return the requested date */
	public LocalDate getDate() {
		return date;
	}

	/** @param date the date to request */
	public void setDate(LocalDate date) {
		this.date = date;
	}

	/** @return the requested time */
	public LocalTime getTime() {
		return time;
	}

	/** @param time the time to request */
	public void setTime(LocalTime time) {
		this.time = time;
	}

}
