package common;

import communication.BistroResponse;

public interface BistroController {
    public BistroResponse getResponse();
    public void serverResponse(BistroResponse response);
    public void accept(Object o);
    public void quit();
}
