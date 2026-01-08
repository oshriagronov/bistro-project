package logic;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class WeeklySchedule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DayOfWeek dayOfWeek;
	private LocalTime open;
	private LocalTime close;

	public WeeklySchedule(DayOfWeek dayOfWeek, LocalTime open, LocalTime close) {
		this.dayOfWeek = dayOfWeek;
		this.open = open;
		this.close = close;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
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
