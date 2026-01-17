package logic;

/** Tracks whether a table is free, reserved, or otherwise unavailable. */
public enum TableStatus {
    AVAILABLE,
    RESERVED,
    OCCUPIED,
    OUT_OF_SERVICE
}
