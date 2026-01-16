package communication;

import java.io.Serializable;
import java.time.LocalTime;

public class WaitlistRow implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String email;
	private final String phone;
	private final int diners;
	private final LocalTime enteredAt;

	public WaitlistRow(String email, String phone, int diners, LocalTime enteredAt) {
		this.email = email;
		this.phone = phone;
		this.diners = diners;
		this.enteredAt = enteredAt;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public int getDiners() {
		return diners;
	}

	public LocalTime getEnteredAt() {
		return enteredAt;
	}
}
