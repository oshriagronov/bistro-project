package communication;

import java.io.Serializable;

/** Counts of subscriber vs non-subscriber orders for a given month. */
public class SubscriberOrderCounts implements Serializable {

    /** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private final int year;
    private final int month;
    private final int subscriberOrders;
    private final int nonSubscriberOrders;

    /**
     * @param year calendar year
     * @param month calendar month (1-12)
     * @param subscriberOrders number of subscriber orders
     * @param nonSubscriberOrders number of non-subscriber orders
     */
    public SubscriberOrderCounts(int year, int month,
                                  int subscriberOrders,
                                  int nonSubscriberOrders) {
        this.year = year;
        this.month = month;
        this.subscriberOrders = subscriberOrders;
        this.nonSubscriberOrders = nonSubscriberOrders;
    }

    /** @return the calendar year */
    public int getYear() {
        return year;
    }

    /** @return the calendar month (1-12) */
    public int getMonth() {
        return month;
    }

    /** @return number of subscriber orders */
    public int getSubscriberOrders() {
        return subscriberOrders;
    }

    /** @return number of non-subscriber orders */
    public int getNonSubscriberOrders() {
        return nonSubscriberOrders;
    }
}
