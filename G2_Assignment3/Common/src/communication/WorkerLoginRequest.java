// communication/WorkerLoginRequest.java
package communication;

import java.io.Serializable;

/** Payload for authenticating a worker via username and password. */
public class WorkerLoginRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String password;

	/**
	 * @param username worker username
	 * @param password worker password
	 */
	public WorkerLoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/** @return the worker username */
	public String getUsername() {
		return username;
	}

	/** @return the worker password */
	public String getPassword() {
		return password;
	}

	/** @param password the worker password to store */
	public void setPassword(String password) {
		this.password = password;
	}

}
