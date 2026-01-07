package communication;

import java.io.Serializable;

public class AvgStayCounts implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int year;
	private final int month; // 1..12
	private final int day;
	private final double avgMinutes; // can be 0.0

	public AvgStayCounts(int year, int month, int day, double avgMinutes) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.avgMinutes = avgMinutes;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public double getAvgMinutes() {
		return avgMinutes;
	}
}
