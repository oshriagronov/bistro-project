package communication;

import java.io.Serializable;

public class ServerEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private final EventType type;
	private final Object data; // payload (למשל LocalDate)

	public ServerEvent(EventType type) {
		this(type, null);
	}

	public ServerEvent(EventType type, Object data) {
		this.type = type;
		this.data = data;
	}

	public EventType getType() {
		return type;
	}

	public Object getData() {
		return data;
	}
}
