package logic;

import java.io.Serializable;

public class Table implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int table_number;
	private int table_size;
	private TableStatus table_status; // false=not occupied, true=occupied

	public Table(int table_number, int table_size) {
		super();
		this.table_number = table_number;
		this.table_size = table_size;
		table_status=TableStatus.AVAILABLE;
	}
	
	public Table(int table_number, int table_size, TableStatus table_status)
	{
		this(table_number, table_size);
		this.table_status = table_status;
	}

	public int getTable_number() {
		return table_number;
	}

	public void setTable_number(int table_number) {
		this.table_number = table_number;
	}

	public TableStatus isTable_status() {
		return table_status;
	}

	public int getTable_size() {
		return table_size;
	}

	public void setTable_size(int table_size) {
		this.table_size = table_size;
	}

	public void setTable_status(TableStatus table_status) {
		this.table_status = table_status;
	}

}
