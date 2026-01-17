package communication;

import java.io.Serializable;

import logic.Status;

/** Payload for changing a reservation status by phone or email. */
public class StatusUpdate implements Serializable{
    /** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private int orderNumber;
    private Status status;
    private String phoneNumber;
    private String email;

    /**
     * @param phoneNumber phone number tied to the reservation
     * @param orderNumber reservation order number
     * @param status new status to apply
     */
    public StatusUpdate(String phoneNumber ,int orderNumber, Status status) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.phoneNumber = phoneNumber;
    }

    /**
     * @param orderNumber reservation order number
     * @param email email tied to the reservation
     * @param status new status to apply
     */
    public StatusUpdate(int orderNumber, String email , Status status) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.email = email;
    }

    /** @return the phone number used for lookup, if provided */
    public String getPhoneNumber() { return phoneNumber; }

    /** @return the order number of the reservation */
    public int getOrderNumber() { return orderNumber; }

	/** @return the new status to set */
	public Status getStatus() {return status;}

    /** @return the email used for lookup, if provided */
    public String getEmail() {return email;}
}
