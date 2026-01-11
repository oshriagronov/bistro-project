
package gui;

import communication.BistroCommand;
import communication.BistroRequest;
import communication.BistroResponse;
import javafx.scene.Node;
import logic.LoggedUser;
import logic.Subscriber;
import logic.Worker;

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

		BistroRequest request = new BistroRequest(BistroCommand.GET_SUBSCRIBER_BY_ID, id);
		Main.client.accept(request);

		BistroResponse response = Main.client.getResponse();

		if (response == null || response.getData() == null) {
			// If something went wrong, fallback to guest mode
			setupGuestView(nonSubContainer, workerContainer, subContainer);
			return null;
		}
		
		Subscriber sub = (Subscriber) response.getData();
		// Hide fields that are not relevant for subscribers
		if (nonSubContainer != null) nonSubContainer.setVisible(false);
		if (workerContainer != null) workerContainer.setVisible(false);
		if (subContainer != null) subContainer.setVisible(false); // subscriber does NOT need to enter subscriber ID manually
		
		return sub;
	}

	/**
	 * Configures the screen for a non‑logged guest user.
	 */
	public static void setupGuestView(Node nonSubContainer, Node workerContainer, Node subContainer) {
		if (nonSubContainer != null) nonSubContainer.setVisible(true);
		if (workerContainer != null) workerContainer.setVisible(false);
		if (subContainer != null) subContainer.setVisible(false);
	}

	/**
	 * Configures the screen for a logged‑in worker.
	 * @return The Worker object if found, null otherwise.
	 */
	public static Worker setupWorkerView(Node nonSubContainer, Node workerContainer, Node subContainer) {
		int id = LoggedUser.getId();
		BistroRequest request = new BistroRequest(BistroCommand.GET_WORKER, id);
		Main.client.accept(request);

		BistroResponse response = Main.client.getResponse();

		if (response == null || response.getData() == null) {
			// If something went wrong, hide everything
			if (nonSubContainer != null) nonSubContainer.setVisible(false);
			if (workerContainer != null) workerContainer.setVisible(false);
			if (subContainer != null) subContainer.setVisible(false);
			return null;
		}
		
		Worker worker = (Worker) response.getData();
		// Hide fields that are not relevant for subscribers
		if (nonSubContainer != null) nonSubContainer.setVisible(true);
		if (workerContainer != null) workerContainer.setVisible(true);
		if (subContainer != null) subContainer.setVisible(false); // worker does NOT need to enter subscriber ID manually
		
		return worker;
	}
}
