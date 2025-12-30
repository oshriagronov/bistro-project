package logic;

import java.io.Serializable;

/**
 * Represents a single row of current diner information displayed in the
 * employee dining/occupancy screen.
 * <p>
 * Each instance corresponds to one table and may include identifying
 * information about the diners currently seated at that table.
 * Some fields are nullable to allow representing partial or anonymous
 * dining data (e.g., walk-in customers without a subscription).
 * </p>
 *
 * <ul>
 *   <li>{@code tableNumber} – table identifier (always present)</li>
 *   <li>{@code phone} – contact phone number of the diner</li>
 *   <li>{@code email} – contact email of the diner</li>
 *   <li>{@code subscriberId} – subscriber ID if the diner is a registered subscriber, {@code null} otherwise</li>
 *   <li>{@code diners} – number of diners seated at the table, {@code null} if unknown</li>
 *   <li>{@code orderNumber} – active order number associated with the table, {@code null} if no order exists</li>
 * </ul>
 *
 * This class is {@link Serializable} to allow transferring instances
 * between client and server layers in the Bistro system.
 */
public class CurrentDinerRow implements Serializable {

    /** Serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** Table number associated with the current diners. */
    private final int tableNumber;

    /** Phone number of the diner. */
    private final String phone;

    /** Email address of the diner. */
    private final String email;

    /** Subscriber ID of the diner, or {@code null} if not a subscriber. */
    private final Integer subscriberId;

    /** Number of diners at the table, or {@code null} if not specified. */
    private final Integer diners;

    /** Active order number, or {@code null} if no order exists. */
    private final Integer orderNumber;

    /**
     * Constructs a new {@code CurrentDinerRow} with the provided diner and table details.
     *
     * @param tableNumber  the table number
     * @param phone        the diner's phone number
     * @param email        the diner's email address
     * @param subscriberId the subscriber ID, or {@code null} if the diner is not a subscriber
     * @param diners       the number of diners at the table, or {@code null} if unknown
     * @param orderNumber  the active order number, or {@code null} if none exists
     */
    public CurrentDinerRow(int tableNumber, String phone, String email,
                           Integer subscriberId, Integer diners, Integer orderNumber) {
        this.tableNumber = tableNumber;
        this.phone = phone;
        this.email = email;
        this.subscriberId = subscriberId;
        this.diners = diners;
        this.orderNumber = orderNumber;
    }

    /** @return the table number */
    public int getTableNumber() { return tableNumber; }

    /** @return the diner's phone number */
    public String getPhone() { return phone; }

    /** @return the diner's email address */
    public String getEmail() { return email; }

    /** @return the subscriber ID, or {@code null} if not a subscriber */
    public Integer getSubscriberId() { return subscriberId; }

    /** @return the number of diners, or {@code null} if unknown */
    public Integer getDiners() { return diners; }

    /** @return the order number, or {@code null} if no order exists */
    public Integer getOrderNumber() { return orderNumber; }
}
