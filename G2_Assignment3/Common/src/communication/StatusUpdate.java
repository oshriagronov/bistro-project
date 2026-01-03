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
    private String phoneNumber;

    public StatusUpdate(String phoneNumber ,int orderNumber, Status status) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.phoneNumber = phoneNumber;
    }
    public String getPhoneNumber() { return phoneNumber; }
    public int getOrderNumber() { return orderNumber; }
	public Status getStatus() {return status;}
}
