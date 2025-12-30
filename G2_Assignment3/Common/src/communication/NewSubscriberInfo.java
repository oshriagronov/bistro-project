package communication;

import java.io.Serializable;

import logic.Subscriber;

public class NewSubscriberInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private final Subscriber subscriber;
    private final String rawPassword;

    public NewSubscriberInfo(Subscriber subscriber, String rawPassword) {
        this.subscriber = subscriber;
        this.rawPassword = rawPassword;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public String getRawPassword() {
        return rawPassword;
    }
}
