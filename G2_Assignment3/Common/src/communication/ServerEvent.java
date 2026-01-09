package communication;

import java.io.Serializable;

public class ServerEvent implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private final EventType type;

	public ServerEvent(EventType type) {
		this.type = type;
	}

	public EventType getType() {
		return type;
	}
}