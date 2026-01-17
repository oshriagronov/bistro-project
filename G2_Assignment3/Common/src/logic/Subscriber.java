package logic;

import java.io.Serializable;

/** Represents a subscriber account with contact and authentication details. */
public class Subscriber implements Serializable {

	/** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private Integer subscriberId; // DB-generated
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private String passwordHash;

	/** Builds a subscriber with an existing account ID and password hash. */
	public Subscriber(int subscriberId, String passwordHash) {
		this.subscriberId = subscriberId;
		this.passwordHash = passwordHash;
	}
	/** Creates a new subscriber profile before a password hash is assigned. */
	public Subscriber(String username, String firstName, String lastName, String email, String phone) {
		this.subscriberId = null;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.passwordHash = null;
	}

	/** Populates a full subscriber record with account and contact data. */
	public Subscriber(Integer subscriberId, String username, String firstName, String lastName, String email,
			String phone, String passwordHash) {
		this.subscriberId = subscriberId;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.passwordHash = passwordHash;
	}

	/** @return the database ID assigned to this subscriber, if known */
	public Integer getSubscriberId() {
		return subscriberId;
	}

	/** @param subscriberId the database ID to assign */
	public void setSubscriberId(Integer subscriberId) {
		this.subscriberId = subscriberId;
	}

	/** @return the subscriber's username */
	public String getUsername() {
		return username;
	}

	/** @param username the login name to set */
	public void setUsername(String username) {
		this.username = username;
	}

	/** @return the subscriber's first name */
	public String getFirstName() {
		return firstName;
	}

	/** @param firstName the first name to record */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/** @return the subscriber's last name */
	public String getLastName() {
		return lastName;
	}

	/** @param lastName the last name to record */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/** @return the subscriber's contact email */
	public String getEmail() {
		return email;
	}

	/** @param email the contact email to store */
	public void setEmail(String email) {
		this.email = email;
	}

	/** @return the subscriber's contact phone number */
	public String getPhone() {
		return phone;
	}

	/** @param phone the phone number to store */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/** @return the stored hash of the subscriber credentials */
	public String getPasswordHash() {
		return passwordHash;
	}

	/** @param passwordHash the credential hash to save */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
