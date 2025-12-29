package logic;

import java.io.Serializable;

public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private int table_number;   // PK, AUTO_INCREMENT
    private int table_size;     // size
    private Integer res_id;     // nullable FK

    // Full constructor (used when loading from DB)
    public Table(int table_number, int table_size, Integer res_id) {
        this.table_number = table_number;
        this.table_size = table_size;
        this.res_id = res_id;
    }

    // Convenience constructor (res_id defaults to null)
    public Table(int table_number, int table_size) {
        this(table_number, table_size, null);
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

    public Integer getRes_id() {
        return res_id;
    }

    public void setRes_id(Integer res_id) {
        this.res_id = res_id;
    }

    // Optional helper for UI / logic
    public boolean isAvailable() {
        return res_id == null;
    }
}
