package communication;

import java.io.Serializable;

public class TableSizeUpdate implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int table_number;
	private int table_size;

	public TableSizeUpdate(int table_number, int table_size) {
		super();
		this.table_number = table_number;
		this.table_size = table_size;
	}

	public int getTable_number() {
		return table_number;
	}

	public void setTable_number(int table_number) {
		this.table_number = table_number;
	}

	public int getTable_size() {
		return table_size;
	}

	public void setTable_size(int table_size) {
		this.table_size = table_size;
	}

}
