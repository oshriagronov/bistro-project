package logic;

import java.io.Serializable;

/** Represents a Bistro staff member with an assigned worker type role. */
public class Worker implements Serializable {

    private static final long serialVersionUID = 1L;

    private int workerId;
    private String username;
    //private String passwordHash;
    private WorkerType workerType;


   /* public Worker(int workerId, String username, String passwordHash, WorkerType workerType) {
        this.workerId = workerId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.workerType = workerType;
    } */

    /**
     * Constructs a worker record with a fixed ID, username, and role.
     *
     * @param workerId the staff identifier
     * @param username the login name for the worker
     * @param workerType the role assigned to the worker
     */
    public Worker(int workerId, String username, WorkerType workerType) {
        this.workerId = workerId;
        this.username = username;
        this.workerType = workerType;
    }

    /** @return the internal worker identifier */
    public int getWorkerId() {
        return workerId;
    }

    /** @return the worker's username */
    public String getUsername() {
        return username;
    }

    /** @return the assigned worker role */
    public WorkerType getWorkerType() {
        return workerType;
    }

 /*   public String getPasswordHash() {
        return passwordHash;
    }*/
}
