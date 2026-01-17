package communication;

import java.io.Serializable;

/** Carries a command and optional payload from client to server. */
public class BistroRequest implements Serializable {
    private BistroCommand command;
    private Object data;

    /**
     * Constructs a request envelope.
     *
     * @param cmd the command to execute on the server
     * @param data the optional payload to accompany the command
     */
    public BistroRequest(BistroCommand cmd, Object data) {
        this.command = cmd;
        this.data = data;
    }

    /** @return the remote command */
    public BistroCommand getCommand() {
        return command;
    }

    /** @return the accompanying payload, or {@code null} */
    public Object getData() {
        return data;
    }
}
