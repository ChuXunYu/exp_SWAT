package assistant.task;

import assistant.common.EntityId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryTaskRepository implements TaskRepository {
    private final Map<EntityId, TaskItem> tasks = new LinkedHashMap<>();

    @Override
    public void save(TaskItem task) {
        Objects.requireNonNull(task, "task");
        tasks.put(task.getId(), task);
    }

    @Override
    public Optional<TaskItem> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<TaskItem> findAll() {
        return List.copyOf(tasks.values());
    }

    @Override
    public List<TaskItem> findBy(TaskQuery query) {
        Objects.requireNonNull(query, "query");
        return tasks.values().stream().filter(query::matches).toList();
    }

    @Override
    public boolean deleteById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return tasks.remove(id) != null;
    }
}
