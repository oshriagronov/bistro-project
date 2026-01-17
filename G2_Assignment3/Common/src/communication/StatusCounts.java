package communication;

import java.io.Serializable;

/** Aggregated arrival status counts for a specific calendar day. */
public class StatusCounts implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int year;
	private final int month; // 1-12
	private final int day;
	private final int onTime;
	private final int late;
	private final int cancelled;

	/**
	 * @param year calendar year
	 * @param month calendar month (1-12)
	 * @param day calendar day
	 * @param onTime number of on-time arrivals
	 * @param late number of late arrivals
	 * @param cancelled number of cancelled reservations
	 */
	public StatusCounts(int year, int month, int day, int onTime, int late, int cancelled) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.onTime = onTime;
		this.late = late;
		this.cancelled = cancelled;
	}

	/** @return the calendar year */
	public int getYear() {
		return year;
	}

	/** @return the calendar month */
	public int getMonth() {
		return month;
	}

	/** @return the calendar day */
	public int getDay() {
		return day;
	}

	/** @return how many arrivals were on time */
	public int getOnTime() {
		return onTime;
	}

	/** @return how many arrivals were late */
	public int getLate() {
		return late;
	}

	/** @return how many reservations were cancelled */
	public int getCancelled() {
		return cancelled;
	}
}
