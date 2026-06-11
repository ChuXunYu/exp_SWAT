package assistant.task;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import java.time.LocalDate;
import java.util.Objects;

public class TaskItem {
    private final EntityId id;
    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDate dueDate;
    private TaskStatus status;

    public TaskItem(
            EntityId id,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate,
            TaskStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = normalizeTitle(title);
        this.description = normalizeDescription(description);
        this.priority = Objects.requireNonNull(priority, "priority");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
        this.status = Objects.requireNonNull(status, "status");
    }

    public static TaskItem createTodo(
            EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate) {
        return new TaskItem(id, title, description, priority, dueDate, TaskStatus.TODO);
    }

    public EntityId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public boolean isCompleted() {
        return status.isCompleted();
    }

    public void updateDetails(String title, String description, TaskPriority priority, LocalDate dueDate) {
        String normalizedTitle = normalizeTitle(title);
        String normalizedDescription = normalizeDescription(description);
        TaskPriority requiredPriority = Objects.requireNonNull(priority, "priority");
        LocalDate requiredDueDate = Objects.requireNonNull(dueDate, "dueDate");

        this.title = normalizedTitle;
        this.description = normalizedDescription;
        this.priority = requiredPriority;
        this.dueDate = requiredDueDate;
    }

    public void markCompleted() {
        if (status == TaskStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "task is already completed");
        }
        status = TaskStatus.COMPLETED;
    }

    public void reopen() {
        if (status == TaskStatus.TODO) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "task is already todo");
        }
        status = TaskStatus.TODO;
    }

    private static String normalizeTitle(String title) {
        String normalizedTitle = Objects.requireNonNull(title, "title").strip();
        if (normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return normalizedTitle;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.strip();
    }
}
