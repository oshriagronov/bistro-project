package communication;

import java.io.Serializable;

public class AvgWaitTimePerDay implements Serializable {

	private static final long serialVersionUID = 1L;

	private int year;
	private int month;
	private int day;
	private double avgWaitMinutes;

	public AvgWaitTimePerDay(int year, int month, int day, double avgWaitMinutes) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.avgWaitMinutes = avgWaitMinutes;
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

	public double getAvgWaitMinutes() {
		return avgWaitMinutes;
	}
}
