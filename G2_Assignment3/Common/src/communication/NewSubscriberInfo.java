package communication;

import java.io.Serializable;

import logic.Subscriber;

/** Payload for creating a new subscriber along with raw credentials. */
public class NewSubscriberInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private final Subscriber subscriber;
    private final String rawPassword;

    /**
     * @param subscriber subscriber details to register
     * @param rawPassword password in plain text (to be hashed on the server)
     */
    public NewSubscriberInfo(Subscriber subscriber, String rawPassword) {
        this.subscriber = subscriber;
        this.rawPassword = rawPassword;
    }

    /** @return the subscriber profile data */
    public Subscriber getSubscriber() {
        return subscriber;
    }

    /** @return the provided raw password */
    public String getRawPassword() {
        return rawPassword;
    }
}
