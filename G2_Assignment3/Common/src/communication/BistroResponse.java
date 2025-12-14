package communication;

import java.io.Serializable;

public class BistroResponse implements Serializable{
    private BistroResponseStatus status;
    private Object data;
    public BistroResponse(BistroResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
    }
    public BistroResponseStatus getStatus() {
        return status;
    }
    public Object getData() {
        return data;
    }
}
