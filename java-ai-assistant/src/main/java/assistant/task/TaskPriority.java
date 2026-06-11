package assistant.task;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH;

    public static TaskPriority defaultPriority() {
        return MEDIUM;
    }
}
