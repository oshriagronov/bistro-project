package communication;

import java.io.Serializable;

public class StatusCounts implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int year;
	public final int month; // 1-12
	public final int onTime;
	public final int late;
	public final int cancelled;

	public StatusCounts(int year, int month, int onTime, int late, int cancelled) {
		this.year = year;
		this.month = month;
		this.onTime = onTime;
		this.late = late;
		this.cancelled = cancelled;
	}
}
