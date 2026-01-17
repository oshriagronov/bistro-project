package communication;

import java.io.Serializable;
import logic.TableStatus;

/** Payload for changing a table's availability status. */
public class TableStatusUpdate implements Serializable {
	private static final long serialVersionUID = 1L;
	private int tableNumber;
    private TableStatus status;

    /**
     * @param tableNumber table identifier to update
     * @param status new status to apply
     */
    public TableStatusUpdate(int tableNumber, TableStatus status) {
        this.tableNumber = tableNumber;
        this.status = status;
    }

	/** @return the table identifier */
	public int getTableNumber() {
		return tableNumber;
	}

	/** @param tableNumber the table identifier to store */
	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	/** @return the table status */
	public TableStatus getStatus() {
		return status;
	}

	/** @param status the status to store */
	public void setStatus(TableStatus status) {
		this.status = status;
	}

}
