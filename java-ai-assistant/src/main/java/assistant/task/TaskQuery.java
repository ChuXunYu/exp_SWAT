package assistant.task;

import java.time.LocalDate;
import java.util.Objects;

public record TaskQuery(TaskStatus status, TaskPriority priority, LocalDate dueDate) {
    public static TaskQuery all() {
        return new TaskQuery(null, null, null);
    }

    public static TaskQuery byStatus(TaskStatus status) {
        return new TaskQuery(Objects.requireNonNull(status, "status"), null, null);
    }

    public static TaskQuery byPriority(TaskPriority priority) {
        return new TaskQuery(null, Objects.requireNonNull(priority, "priority"), null);
    }

    public static TaskQuery byDueDate(LocalDate dueDate) {
        return new TaskQuery(null, null, Objects.requireNonNull(dueDate, "dueDate"));
    }

    public static TaskQuery of(TaskStatus status, TaskPriority priority, LocalDate dueDate) {
        return new TaskQuery(status, priority, dueDate);
    }

    public boolean hasStatusFilter() {
        return status != null;
    }

    public boolean hasPriorityFilter() {
        return priority != null;
    }

    public boolean hasDueDateFilter() {
        return dueDate != null;
    }

    public boolean matches(TaskItem task) {
        Objects.requireNonNull(task, "task");
        return (!hasStatusFilter() || task.getStatus() == status)
                && (!hasPriorityFilter() || task.getPriority() == priority)
                && (!hasDueDateFilter() || task.getDueDate().equals(dueDate));
    }
}
