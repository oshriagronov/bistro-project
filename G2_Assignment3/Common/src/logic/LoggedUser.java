package logic;

/**
 * Holds information about the currently logged-in user.
 * This class is static so the data is accessible from any screen.
 */
public class LoggedUser {

    private static int id;
    private static UserType type;

    public LoggedUser(int id, UserType type) {
        LoggedUser.id = id;
        LoggedUser.type = type;
    }

    public static int getId() {
        return id;
    }

    public static UserType getType() {
        return type;
    }


    public static void setSubscriber(int id) {
        LoggedUser.id = id;
        LoggedUser.type = UserType.SUBSCRIBER;
    }
}
