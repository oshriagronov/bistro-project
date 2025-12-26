package communication;

import java.io.Serializable;

import logic.Status;

public class StatusUpdate implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int orderNumber;
    private Status status;

    public StatusUpdate(int orderNumber, Status status) {
        this.orderNumber = orderNumber;
        this.status = status;
    }

    public int getOrderNumber() { return orderNumber; }

	public Status getStatus() {
		return status;
	}

}
