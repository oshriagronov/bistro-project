package communication;

import java.io.Serializable;

/** Simple event envelope sent from the server to notify clients. */
public class ServerEvent implements Serializable {

	/** Serialization version identifier. */

	private static final long serialVersionUID = 1L;

	private final EventType type;
	private final Object data; // payload

	/** @param type event type being broadcast */
	public ServerEvent(EventType type) {
		this(type, null);
	}

	public ServerEvent(EventType type, Object data) {
		this.type = type;
		this.data = data;
	}

	/** @return the event type */
	public EventType getType() {
		return type;
	}

	public Object getData() {
		return data;
	}

}
