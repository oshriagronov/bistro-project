package communication;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class OrdersInRangeRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LocalDate date;
	private LocalTime time;
	public OrdersInRangeRequest(LocalDate date, LocalTime time) {
		super();
		this.date = date;
		this.time = time;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public LocalTime getTime() {
		return time;
	}
	public void setTime(LocalTime time) {
		this.time = time;
	}

}
