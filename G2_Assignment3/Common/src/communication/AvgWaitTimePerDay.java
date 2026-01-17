package communication;

import java.io.Serializable;

/** Tracks the average customer wait time for a specific day. */
public class AvgWaitTimePerDay implements Serializable {

	private static final long serialVersionUID = 1L;

	private int year;
	private int month;
	private int day;
	private double avgWaitMinutes;

	/**
	 * Creates a wait-time metric entry.
	 *
	 * @param year calendar year
	 * @param month calendar month (1-12)
	 * @param day calendar day
	 * @param avgWaitMinutes average wait time in minutes
	 */
	public AvgWaitTimePerDay(int year, int month, int day, double avgWaitMinutes) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.avgWaitMinutes = avgWaitMinutes;
	}

	/** @return the calendar year */
	public int getYear() {
		return year;
	}

	/** @return the calendar month (1-12) */
	public int getMonth() {
		return month;
	}

	/** @return the calendar day */
	public int getDay() {
		return day;
	}

	/** @return the average wait time in minutes */
	public double getAvgWaitMinutes() {
		return avgWaitMinutes;
	}
}
