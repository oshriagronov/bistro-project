package communication;
import java.io.Serializable;

public class BistroRequest implements Serializable{
    private BistroCommand command;
    private Object data;
    public BistroRequest(BistroCommand cmd, Object data) {
        this.command = cmd;
        this.data = data;
    }

    public BistroCommand getCommand() {
        return command;
    }
    public Object getData() {
        return data;
    }
}

