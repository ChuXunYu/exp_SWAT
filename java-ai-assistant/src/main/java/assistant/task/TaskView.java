package assistant.task;

import assistant.common.EntityId;
import java.time.LocalDate;
import java.util.Objects;

public record TaskView(
        EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate, TaskStatus status) {
    public TaskView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(dueDate, "dueDate");
        Objects.requireNonNull(status, "status");
        if (title.strip().isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }

    public static TaskView from(TaskItem task) {
        Objects.requireNonNull(task, "task");
        return new TaskView(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getStatus());
    }

    public boolean isCompleted() {
        return status.isCompleted();
    }
}
