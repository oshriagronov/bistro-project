package communication;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class CancelledReservationInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int resId;
	private final String phone;
	private final String email;
	private final String confirmationCode;
	private final LocalDate orderDate;
	private final LocalTime startTime;

	public CancelledReservationInfo(int resId, String phone, String email, String confirmationCode, LocalDate orderDate,
			LocalTime startTime) {
		this.resId = resId;
		this.phone = phone;
		this.email = email;
		this.confirmationCode = confirmationCode;
		this.orderDate = orderDate;
		this.startTime = startTime;
	}

	public int getResId() {
		return resId;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public String getConfirmationCode() {
		return confirmationCode;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}
}
