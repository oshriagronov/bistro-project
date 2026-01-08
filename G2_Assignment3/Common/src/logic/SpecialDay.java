package logic;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class SpecialDay implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LocalDate day;
	private LocalTime open;
	private LocalTime close;

	public SpecialDay(LocalDate day, LocalTime open, LocalTime close) {
		super();
		this.day = day;
		this.open = open;
		this.close = close;
	}

	public LocalDate getDay() {
		return day;
	}

	public void setDay(LocalDate day) {
		this.day = day;
	}

	public LocalTime getOpen() {
		return open;
	}

	public void setOpen(LocalTime open) {
		this.open = open;
	}

	public LocalTime getClose() {
		return close;
	}

	public void setClose(LocalTime close) {
		this.close = close;
	}

}
