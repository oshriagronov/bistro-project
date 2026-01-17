package communication;

/** Listener notified when an {@link EventType} is published. */
public interface EventListener {
	/** @param type event type that was raised */
	 void onEvent(EventType type);
}
