package communication;

import java.io.Serializable;

/** Holds average stay duration metrics for a specific calendar day. */
public class AvgStayCounts implements Serializable {
	/** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private final int year;
	private final int month; // 1..12
	private final int day;
	private final double avgMinutes; // can be 0.0

	/**
	 * Constructs an average stay metric entry for the provided date.
	 *
	 * @param year calendar year for the metric entry
	 * @param month calendar month (1-12)
	 * @param day calendar day of month
	 * @param avgMinutes average stay duration in minutes
	 */
	public AvgStayCounts(int year, int month, int day, double avgMinutes) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.avgMinutes = avgMinutes;
	}

	/** @return the calendar year */
	public int getYear() {
		return year;
	}

	/** @return the calendar month (1-12) */
	public int getMonth() {
		return month;
	}

	/** @return the calendar day of month */
	public int getDay() {
		return day;
	}

	/** @return the average stay duration in minutes */
	public double getAvgMinutes() {
		return avgMinutes;
	}
}
