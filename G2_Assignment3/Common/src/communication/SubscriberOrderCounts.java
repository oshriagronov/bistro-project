package communication;

import java.io.Serializable;

public class SubscriberOrderCounts implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int year;
    private final int month;
    private final int subscriberOrders;
    private final int nonSubscriberOrders;

    public SubscriberOrderCounts(int year, int month,
                                  int subscriberOrders,
                                  int nonSubscriberOrders) {
        this.year = year;
        this.month = month;
        this.subscriberOrders = subscriberOrders;
        this.nonSubscriberOrders = nonSubscriberOrders;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getSubscriberOrders() {
        return subscriberOrders;
    }

    public int getNonSubscriberOrders() {
        return nonSubscriberOrders;
    }
}
