package communication;

import java.io.Serializable;
import java.time.LocalTime;

/** Represents a single entry on the waitlist. */
public class WaitlistRow implements Serializable {

	/** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private final String email;
	private final String phone;
	private final int diners;
	private final LocalTime enteredAt;

	/**
	 * @param email contact email
	 * @param phone contact phone number
	 * @param diners party size
	 * @param enteredAt time the party joined the waitlist
	 */
	public WaitlistRow(String email, String phone, int diners, LocalTime enteredAt) {
		this.email = email;
		this.phone = phone;
		this.diners = diners;
		this.enteredAt = enteredAt;
	}

	/** @return the contact email */
	public String getEmail() {
		return email;
	}

	/** @return the contact phone number */
	public String getPhone() {
		return phone;
	}

	/** @return the party size */
	public int getDiners() {
		return diners;
	}

	/** @return when the party joined the waitlist */
	public LocalTime getEnteredAt() {
		return enteredAt;
	}
}
