package communication;

import java.io.Serializable;

/** Carries a server response status and optional payload back to the client. */
public class BistroResponse implements Serializable {
    private BistroResponseStatus status;
    private Object data;

    /**
     * @param status the response status
     * @param data optional response payload
     */
    public BistroResponse(BistroResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
    }

    /** @return the response status */
    public BistroResponseStatus getStatus() {
        return status;
    }

    /** @return the response payload, if any */
    public Object getData() {
        return data;
    }
}
