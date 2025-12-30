package logic;

import java.io.Serializable;

public class Subscriber implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer subscriberId; // DB-generated
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private String passwordHash;

	public Subscriber(String username, String passwordHash) {
		this.username = username;
		this.passwordHash = passwordHash;
	}
	public Subscriber(String username, String firstName, String lastName, String email, String phone) {
		this.subscriberId = null;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.passwordHash = null;
	}

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

	public Integer getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(Integer subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
