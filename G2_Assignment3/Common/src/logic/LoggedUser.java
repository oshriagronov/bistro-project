package logic;

public class LoggedUser {
	private static int id;
	private static UserType type;

	public LoggedUser(int id, UserType type) {
		this.id = id;
		this.type = type;
	}

	public static int getId() {
		return id;
	}

	public static UserType getType() {
		return type;
	}
}
