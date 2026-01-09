package communication;

import java.io.Serializable;
import logic.TableStatus;

public class TableStatusUpdate implements Serializable {
	private static final long serialVersionUID = 1L;
	private int tableNumber;
    private TableStatus status;

    public TableStatusUpdate(int tableNumber, TableStatus status) {
        this.tableNumber = tableNumber;
        this.status = status;
    }

	public int getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public TableStatus getStatus() {
		return status;
	}

	public void setStatus(TableStatus status) {
		this.status = status;
	}

}
