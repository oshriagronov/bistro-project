package logic;

import java.io.Serializable;

/** Models a restaurant table, including its assigned reservation if any. */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private int table_number;   // PK, AUTO_INCREMENT
    private int table_size;     // size
    private Integer res_id;     // nullable FK

    /** Full constructor (used when loading from DB) */
    public Table(int table_number, int table_size, Integer res_id) {
        this.table_number = table_number;
        this.table_size = table_size;
        this.res_id = res_id;
    }

    /** Convenience constructor (res_id defaults to null) */
    public Table(int table_number, int table_size) {
        this(table_number, table_size, null);
    }

    /** @return the table's numeric identifier */
    public int getTable_number() {
        return table_number;
    }

    /** @param table_number the table identifier to record */
    public void setTable_number(int table_number) {
        this.table_number = table_number;
    }

    /** @return how many guests the table can seat */
    public int getTable_size() {
        return table_size;
    }

    /** @param table_size the seating capacity to record */
    public void setTable_size(int table_size) {
        this.table_size = table_size;
    }

    /** @return the reservation ID assigned to this table, or {@code null} */
    public Integer getRes_id() {
        return res_id;
    }

    /** @param res_id the reservation ID to associate */
    public void setRes_id(Integer res_id) {
        this.res_id = res_id;
    }

    /** @return {@code true} when no reservation currently claims the table */
    public boolean isAvailable() {
        return res_id == null;
    }
}
