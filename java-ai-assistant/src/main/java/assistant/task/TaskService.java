package assistant.task;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.IdGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class TaskService {
    private final TaskRepository repository;
    private final IdGenerator idGenerator;

    public TaskService(TaskRepository repository, IdGenerator idGenerator) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public OperationResult<TaskView> createTask(
            String title, String description, TaskPriority priority, LocalDate dueDate) {
        try {
            TaskItem task = TaskItem.createTodo(idGenerator.nextId(), title, description, priority, dueDate);
            repository.save(task);
            return OperationResult.success(toView(task));
        } catch (NullPointerException | IllegalArgumentException exception) {
            return validationFailure(exception.getMessage());
        }
    }

    public OperationResult<TaskView> getTask(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id)
                .map(task -> OperationResult.success(toView(task)))
                .orElseGet(() -> notFound(id));
    }

    public OperationResult<List<TaskView>> listTasks() {
        return OperationResult.success(toUnmodifiableViews(repository.findAll()));
    }

    public OperationResult<List<TaskView>> listTasks(TaskQuery query) {
        if (query == null) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "query must not be null");
        }
        return OperationResult.success(toUnmodifiableViews(repository.findBy(query)));
    }

    public OperationResult<TaskView> updateTask(
            EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(task -> {
            try {
                task.updateDetails(title, description, priority, dueDate);
                repository.save(task);
                return OperationResult.success(toView(task));
            } catch (NullPointerException | IllegalArgumentException exception) {
                return validationFailure(exception.getMessage());
            }
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<Void> deleteTask(EntityId id) {
        if (id == null) {
            return validationFailureVoid("id must not be null");
        }
        if (!repository.deleteById(id)) {
            return notFoundVoid(id);
        }
        return OperationResult.success();
    }

    public OperationResult<TaskView> markTaskCompleted(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(task -> {
            try {
                task.markCompleted();
                repository.save(task);
                return OperationResult.success(toView(task));
            } catch (BusinessException exception) {
                return OperationResult.<TaskView>failure(exception.getErrorCode(), exception.getMessage());
            }
        }).orElseGet(() -> notFound(id));
    }

    public OperationResult<TaskView> reopenTask(EntityId id) {
        if (id == null) {
            return validationFailure("id must not be null");
        }
        return repository.findById(id).map(task -> {
            try {
                task.reopen();
                repository.save(task);
                return OperationResult.success(toView(task));
            } catch (BusinessException exception) {
                return OperationResult.<TaskView>failure(exception.getErrorCode(), exception.getMessage());
            }
        }).orElseGet(() -> notFound(id));
    }

    private OperationResult<TaskView> validationFailure(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<TaskView> notFound(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "task not found: " + id.value());
    }

    private OperationResult<Void> validationFailureVoid(String message) {
        return OperationResult.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    private OperationResult<Void> notFoundVoid(EntityId id) {
        return OperationResult.failure(ErrorCode.NOT_FOUND, "task not found: " + id.value());
    }

    private static TaskView toView(TaskItem task) {
        return TaskView.from(task);
    }

    private static List<TaskView> toUnmodifiableViews(List<TaskItem> tasks) {
        return tasks.stream().map(TaskService::toView).toList();
    }
}
