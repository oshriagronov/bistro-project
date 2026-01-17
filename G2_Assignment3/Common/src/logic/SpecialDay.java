package logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a special operating day that overrides default opening/closing hours.
 */
public class SpecialDay implements Serializable {
	private static final long serialVersionUID = 1L;
	private LocalDate day;
	private LocalTime open;
	private LocalTime close;

	/** Creates a special day with the provided open and close times. */
	public SpecialDay(LocalDate day, LocalTime open, LocalTime close) {
		super();
		this.day = day;
		this.open = open;
		this.close = close;
	}

	/** @return the calendar date for this special day */
	public LocalDate getDay() {
		return day;
	}

	/** @param day the calendar date to set */
	public void setDay(LocalDate day) {
		this.day = day;
	}

	/** @return the time when service begins on this special day */
	public LocalTime getOpen() {
		return open;
	}

	/** @param open the opening time to set */
	public void setOpen(LocalTime open) {
		this.open = open;
	}

	/** @return the time when service ends on this special day */
	public LocalTime getClose() {
		return close;
	}

	/** @param close the closing time to set */
	public void setClose(LocalTime close) {
		this.close = close;
	}

}
