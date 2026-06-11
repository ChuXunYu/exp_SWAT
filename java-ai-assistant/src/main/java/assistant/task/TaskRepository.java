package assistant.task;

import assistant.common.EntityId;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    void save(TaskItem task);

    Optional<TaskItem> findById(EntityId id);

    List<TaskItem> findAll();

    List<TaskItem> findBy(TaskQuery query);

    boolean deleteById(EntityId id);
}
