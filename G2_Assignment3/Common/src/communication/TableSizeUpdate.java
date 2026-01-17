package communication;

import java.io.Serializable;

/** Payload for resizing a table. */
public class TableSizeUpdate implements Serializable {
	/** Serialization version identifier. */
	private static final long serialVersionUID = 1L;
	private int table_number;
	private int table_size;

	/**
	 * @param table_number the table identifier to update
	 * @param table_size the new seating capacity
	 */
	public TableSizeUpdate(int table_number, int table_size) {
		super();
		this.table_number = table_number;
		this.table_size = table_size;
	}

	/** @return the table identifier */
	public int getTable_number() {
		return table_number;
	}

	/** @param table_number the table identifier to store */
	public void setTable_number(int table_number) {
		this.table_number = table_number;
	}

	/** @return the seating capacity */
	public int getTable_size() {
		return table_size;
	}

	/** @param table_size the seating capacity to store */
	public void setTable_size(int table_size) {
		this.table_size = table_size;
	}

}
