package communication;

import java.io.Serializable;

public class StatusCounts implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int year;
    private final int month; // 1-12
    private final int onTime;
    private final int late;
    private final int cancelled;

    public StatusCounts(int year, int month, int onTime, int late, int cancelled) {
        this.year = year;
        this.month = month;
        this.onTime = onTime;
        this.late = late;
        this.cancelled = cancelled;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getOnTime() { return onTime; }
    public int getLate() { return late; }
    public int getCancelled() { return cancelled; }
}