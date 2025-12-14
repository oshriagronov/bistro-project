package communication;

public class RequestFactory {
    public static BistroRequest requestInsert(BistroCommand cmd, Object payload) {
        return new BistroRequest(cmd, payload);
    }

    public static BistroRequest requestData(BistroCommand cmd) {
        return new BistroRequest(cmd, null);
    }
}
