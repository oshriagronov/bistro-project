package communication;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;

/**
 * A simple client-side Event Bus implementation.
 * <p>
 * The EventBus allows different parts of the application (e.g. JavaFX screens)
 * to communicate using events without being directly coupled to each other.
 * <p>
 * Screens can subscribe to specific {@link EventType}s and will be notified
 * whenever such an event is published.
 * <p>
 * All event listeners are executed on the JavaFX Application Thread using
 * {@link Platform#runLater(Runnable)}, making it safe to update UI components.
 *
 * <h2>Typical usage:</h2>
 * 
 * <pre>
 * EventBus.getInstance().subscribe(EventType.ORDER_CHANGED, listener);
 * EventBus.getInstance().publish(EventType.ORDER_CHANGED);
 * </pre>
 *
 * <p>
 * This class is implemented as a Singleton, since the application requires a
 * single shared event dispatcher on the client side.
 */
public class EventBus {

	/**
	 * The single instance of the EventBus.
	 */
	private static final EventBus INSTANCE = new EventBus();

	/**
	 * Maps each {@link EventType} to the list of listeners interested in that
	 * event.
	 * <p>
	 * A {@link ConcurrentHashMap} is used to allow safe access from multiple
	 * threads (e.g. JavaFX thread and network thread).
	 */
	private final Map<EventType, List<EventListener>> listeners = new ConcurrentHashMap<>();

	/**
	 * Private constructor to enforce Singleton pattern.
	 */
	private EventBus() {
	}

	/**
	 * Returns the single instance of the EventBus.
	 *
	 * @return the EventBus instance
	 */
	public static EventBus getInstance() {
		return INSTANCE;
	}

	/**
	 * Subscribes a listener to a specific event type.
	 * <p>
	 * The listener will be notified every time the given event type is published.
	 *
	 * @param type     the event type to listen for
	 * @param listener the listener to register
	 */
	public void subscribe(EventType type, EventListener listener) {
		listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
	}

	/**
	 * Unsubscribes a listener from a specific event type.
	 * <p>
	 * This should be called when a screen is closed or no longer needs to receive
	 * events, in order to avoid memory leaks.
	 *
	 * @param type     the event type
	 * @param listener the listener to remove
	 */
	public void unsubscribe(EventType type, EventListener listener) {
		List<EventListener> list = listeners.get(type);
		if (list != null) {
			list.remove(listener);
		}
	}

	/**
	 * Publishes an event of the given type.
	 * <p>
	 * All registered listeners for this event type will be notified. The
	 * notification is executed on the JavaFX Application Thread, making it safe to
	 * update UI components inside the listener.
	 *
	 * @param type the event type to publish
	 */
	public void publish(EventType type) {
		List<EventListener> list = listeners.get(type);
		if (list == null)
			return;

		for (EventListener l : list) {
			Platform.runLater(() -> l.onEvent(type));
		}
	}

	/**
	 * Removes all registered listeners.
	 * <p>
	 * This method can be useful when logging out or resetting the application
	 * state.
	 */
	public void clear() {
		listeners.clear();
	}
}