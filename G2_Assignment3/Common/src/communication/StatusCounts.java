package communication;

import java.io.Serializable;

public class StatusCounts implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int confirmed;
    public final int pending;
    public final int cancelled;

    public StatusCounts(int confirmed, int pending, int cancelled) {
        this.confirmed = confirmed;
        this.pending = pending;
        this.cancelled = cancelled;
    }
}