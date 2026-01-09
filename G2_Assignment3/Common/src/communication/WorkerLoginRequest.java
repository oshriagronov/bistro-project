// communication/WorkerLoginRequest.java
package communication;

import java.io.Serializable;

public class WorkerLoginRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String password;

	public WorkerLoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
