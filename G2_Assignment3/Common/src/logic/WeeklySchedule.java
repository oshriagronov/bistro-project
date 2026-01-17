package logic;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Holds the recurring open/close times for a specific weekday. */
public class WeeklySchedule implements Serializable {

	/** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private DayOfWeek dayOfWeek;
	private LocalTime open;
	private LocalTime close;

	/** Creates a schedule entry for a weekday with its working hours. */
	public WeeklySchedule(DayOfWeek dayOfWeek, LocalTime open, LocalTime close) {
		this.dayOfWeek = dayOfWeek;
		this.open = open;
		this.close = close;
	}

	/** @return the day of the week this schedule describes */
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	/** @param dayOfWeek the day to associate with this schedule */
	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/** @return the opening time for the day */
	public LocalTime getOpen() {
		return open;
	}

	/** @param open the opening time to store */
	public void setOpen(LocalTime open) {
		this.open = open;
	}

	/** @return the closing time for the day */
	public LocalTime getClose() {
		return close;
	}

	/** @param close the closing time to store */
	public void setClose(LocalTime close) {
		this.close = close;
	}
}
