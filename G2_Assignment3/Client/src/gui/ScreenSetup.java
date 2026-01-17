
package gui;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import communication.RequestFactory;
import javafx.scene.Node;
import logic.LoggedUser;
import logic.Subscriber;

public class ScreenSetup {

	/**
	 * Configures the screen for a logged-in subscriber.
	 * Fetches subscriber details from the database and auto-fills the UI fields.
	 * @param nonSubContainer Container for non-subscriber fields (e.g., guest contact info)
	 * @param workerContainer Container for worker fields
	 * @param subContainer Container for subscriber specific fields (e.g. ID input)
	 * @return The Subscriber object if found, null otherwise.
	 */
	public static Subscriber setupSubscriber(Node nonSubContainer, Node workerContainer, Node subContainer) {
		int id = LoggedUser.getId();

		BistroRequest request = RequestFactory.getSubscriberById(id);
		Main.client.accept(request);

		BistroResponse response = Main.client.getResponse();

		if (response == null || response.getData() == null) {
			// If something went wrong, fallback to guest mode
			setupGuestView(nonSubContainer, workerContainer, subContainer);
			return null;
		}
		
		Subscriber sub = (Subscriber) response.getData();
		// Hide fields that are not relevant for subscribers
		setVisibleAndManaged(nonSubContainer, false);
		setVisibleAndManaged(workerContainer, false);
		setVisibleAndManaged(subContainer, false); // subscriber does NOT need to enter subscriber ID manually
		
		return sub;
	}

	/**
	 * Configures the screen for a non‑logged guest user.
	 */
	public static void setupGuestView(Node nonSubContainer, Node workerContainer, Node subContainer) {
		setVisibleAndManaged(nonSubContainer, true);
		setVisibleAndManaged(workerContainer, false);
		setVisibleAndManaged(subContainer, false);
	}

	/**
	 * Configures the screen for a logged‑in worker.
	 * @return The Worker object if found, null otherwise.
	 */
	public static void setupWorkerView(Node nonSubContainer, Node workerContainer, Node subContainer) {
		int id = LoggedUser.getId();
		BistroRequest request = RequestFactory.getWorkerById(id);
		Main.client.accept(request);

		BistroResponse response = Main.client.getResponse();

		if (response == null || response.getData() == null) {
			// If something went wrong, hide everything
			setVisibleAndManaged(nonSubContainer, false);
			setVisibleAndManaged(workerContainer, false);
			setVisibleAndManaged(subContainer, false);
			return;
		}
		
		// Hide fields that are not relevant for subscribers
		setVisibleAndManaged(nonSubContainer, true);
		setVisibleAndManaged(workerContainer, true);
		setVisibleAndManaged(subContainer, false); // worker does NOT need to enter subscriber ID manually
		
	}

	private static void setVisibleAndManaged(Node node, boolean visible) {
		if (node != null) {
			node.setVisible(visible);
			node.setManaged(visible);
		}
	}
}
