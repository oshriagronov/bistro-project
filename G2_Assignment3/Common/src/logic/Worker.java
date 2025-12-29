package logic;

import java.io.Serializable;

public class Worker implements Serializable {

    private static final long serialVersionUID = 1L;

    private int workerId;
    private String username;
    private WorkerType workerType;
    private String passwordHash;

    public Worker(int workerId, String username, String passwordHash, WorkerType workerType) {
        this.workerId = workerId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.workerType = workerType;
    }

    public Worker(int workerId, String username, WorkerType workerType) {
        this.workerId = workerId;
        this.username = username;
        this.workerType = workerType;
    }

    public int getWorkerId() {
        return workerId;
    }

    public String getUsername() {
        return username;
    }

    public WorkerType getWorkerType() {
        return workerType;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}