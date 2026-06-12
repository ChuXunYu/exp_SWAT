package assistant.ai;

import assistant.task.TaskPriority;
import java.time.LocalDate;
import java.util.Objects;

public record TaskDraftItem(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate) {
    public TaskDraftItem {
        title = Objects.requireNonNull(title, "title").strip();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        description = description == null ? "" : description.strip();
        priority = Objects.requireNonNull(priority, "priority");
    }

    public boolean hasDueDate() {
        return dueDate != null;
    }
}
