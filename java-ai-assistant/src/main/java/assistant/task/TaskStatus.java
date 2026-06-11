package assistant.task;

public enum TaskStatus {
    TODO,
    COMPLETED;

    public boolean isCompleted() {
        return this == COMPLETED;
    }
}
