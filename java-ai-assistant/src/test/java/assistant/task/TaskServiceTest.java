package assistant.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.IncrementalIdGenerator;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TaskServiceTest {
    private static final LocalDate JUNE_30 = LocalDate.of(2026, 6, 30);
    private static final LocalDate JULY_1 = LocalDate.of(2026, 7, 1);
    private static final LocalDate JULY_15 = LocalDate.of(2026, 7, 15);

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> new TaskService(null, new IncrementalIdGenerator(100)));
        assertThrows(NullPointerException.class, () -> new TaskService(new InMemoryTaskRepository(), null));
    }

    @Test
    void createTaskStoresTodoTaskAndReturnsTaskView() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));

        OperationResult<TaskView> result =
                service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        assertSuccess(result);
        TaskView view = result.getPayload();
        assertAll(
                () -> assertInstanceOf(TaskView.class, view),
                () -> assertEquals(new EntityId(100), view.id()),
                () -> assertEquals("Plan work", view.title()),
                () -> assertEquals("Scope", view.description()),
                () -> assertEquals(TaskPriority.MEDIUM, view.priority()),
                () -> assertEquals(JUNE_30, view.dueDate()),
                () -> assertEquals(TaskStatus.TODO, view.status()),
                () -> assertEquals(1, repository.findAll().size()));
    }

    @Test
    void createTaskAllowsNullDescriptionAsEmptyString() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.createTask("Plan work", null, TaskPriority.MEDIUM, JUNE_30);

        assertSuccess(result);
        assertEquals("", result.getPayload().description());
        assertEquals("", service.getTask(new EntityId(100)).getPayload().description());
    }

    @Test
    void createTaskRejectsBlankTitleAndDoesNotStoreTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));

        OperationResult<TaskView> result =
                service.createTask(" \t\n", "Scope", TaskPriority.MEDIUM, JUNE_30);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createTaskRejectsNullTitleAndDoesNotStoreTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));

        OperationResult<TaskView> result =
                assertDoesNotThrow(() -> service.createTask(null, "Scope", TaskPriority.MEDIUM, JUNE_30));

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createTaskRejectsNullPriorityAndDoesNotStoreTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));

        OperationResult<TaskView> result = service.createTask("Plan work", "Scope", null, JUNE_30);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createTaskRejectsNullDueDateAndDoesNotStoreTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));

        OperationResult<TaskView> result =
                service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void getTaskReturnsTaskViewForExistingTask() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        OperationResult<TaskView> result = service.getTask(new EntityId(100));

        assertSuccess(result);
        assertInstanceOf(TaskView.class, result.getPayload());
        assertEquals("Plan work", result.getPayload().title());
    }

    @Test
    void getTaskReturnsNotFoundForMissingTask() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.getTask(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void getTaskRejectsNullId() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.getTask(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void listTasksReturnsUnmodifiableTaskViewListInCreationOrder() {
        TaskService service = newService(100);
        service.createTask("First", "Scope", TaskPriority.LOW, JUNE_30);
        service.createTask("Second", "Scope", TaskPriority.HIGH, JULY_1);

        OperationResult<List<TaskView>> result = service.listTasks();

        assertSuccess(result);
        List<TaskView> views = result.getPayload();
        assertAll(
                () -> assertEquals(List.of(new EntityId(100), new EntityId(101)), idsOf(views)),
                () -> assertInstanceOf(TaskView.class, views.get(0)),
                () -> assertThrows(UnsupportedOperationException.class, () -> views.clear()));
    }

    @Test
    void listTasksWithStatusQueryFiltersTasks() {
        TaskService service = serviceWithMixedTasks();

        OperationResult<List<TaskView>> result = service.listTasks(TaskQuery.byStatus(TaskStatus.COMPLETED));

        assertSuccess(result);
        assertEquals(List.of(new EntityId(102)), idsOf(result.getPayload()));
    }

    @Test
    void listTasksWithPriorityQueryFiltersTasks() {
        TaskService service = serviceWithMixedTasks();

        OperationResult<List<TaskView>> result = service.listTasks(TaskQuery.byPriority(TaskPriority.HIGH));

        assertSuccess(result);
        assertEquals(List.of(new EntityId(101), new EntityId(102)), idsOf(result.getPayload()));
    }

    @Test
    void listTasksWithDueDateQueryFiltersTasks() {
        TaskService service = serviceWithMixedTasks();

        OperationResult<List<TaskView>> result = service.listTasks(TaskQuery.byDueDate(JUNE_30));

        assertSuccess(result);
        assertEquals(List.of(new EntityId(100), new EntityId(102)), idsOf(result.getPayload()));
    }

    @Test
    void listTasksWithCombinedQueryFiltersTasks() {
        TaskService service = serviceWithMixedTasks();

        OperationResult<List<TaskView>> result =
                service.listTasks(TaskQuery.of(TaskStatus.COMPLETED, TaskPriority.HIGH, JUNE_30));

        assertSuccess(result);
        assertEquals(List.of(new EntityId(102)), idsOf(result.getPayload()));
    }

    @Test
    void listTasksRejectsNullQuery() {
        TaskService service = newService(100);

        OperationResult<List<TaskView>> result = service.listTasks(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void updateTaskChangesEditableFieldsAndKeepsStatus() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);
        service.markTaskCompleted(new EntityId(100));

        OperationResult<TaskView> result =
                service.updateTask(new EntityId(100), "Write tests", "Cover service", TaskPriority.HIGH, JULY_15);

        assertSuccess(result);
        TaskView view = result.getPayload();
        assertAll(
                () -> assertEquals("Write tests", view.title()),
                () -> assertEquals("Cover service", view.description()),
                () -> assertEquals(TaskPriority.HIGH, view.priority()),
                () -> assertEquals(JULY_15, view.dueDate()),
                () -> assertEquals(TaskStatus.COMPLETED, view.status()));
    }

    @Test
    void updateTaskAllowsNullDescriptionAsEmptyStringAndPersistsChange() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        OperationResult<TaskView> result =
                service.updateTask(new EntityId(100), "Write tests", null, TaskPriority.HIGH, JULY_15);

        assertSuccess(result);
        assertEquals("", result.getPayload().description());
        assertEquals("", service.getTask(new EntityId(100)).getPayload().description());
    }

    @Test
    void updateTaskPersistsChangesWhenRepositoryReturnsDetachedCopies() {
        CopyingTaskRepository repository = new CopyingTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));
        EntityId id = new EntityId(100);
        repository.save(new TaskItem(
                id, "Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30, TaskStatus.COMPLETED));

        OperationResult<TaskView> result =
                service.updateTask(id, "Write tests", "Cover service", TaskPriority.HIGH, JULY_15);

        assertSuccess(result);
        TaskView stored = service.getTask(id).getPayload();
        assertAll(
                () -> assertEquals("Write tests", stored.title()),
                () -> assertEquals("Cover service", stored.description()),
                () -> assertEquals(TaskPriority.HIGH, stored.priority()),
                () -> assertEquals(JULY_15, stored.dueDate()),
                () -> assertEquals(TaskStatus.COMPLETED, stored.status()));
    }

    @Test
    void updateTaskReturnsNotFoundForMissingTask() {
        TaskService service = newService(100);

        OperationResult<TaskView> result =
                service.updateTask(new EntityId(999), "Write tests", "Cover service", TaskPriority.HIGH, JULY_15);

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void updateTaskRejectsNullTitleAndKeepsStoredTaskUnchanged() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        OperationResult<TaskView> result = assertDoesNotThrow(
                () -> service.updateTask(new EntityId(100), null, "Changed", TaskPriority.HIGH, JULY_15));

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
        TaskView stored = service.getTask(new EntityId(100)).getPayload();
        assertAll(
                () -> assertEquals("Plan work", stored.title()),
                () -> assertEquals("Scope", stored.description()),
                () -> assertEquals(TaskPriority.MEDIUM, stored.priority()),
                () -> assertEquals(JUNE_30, stored.dueDate()),
                () -> assertEquals(TaskStatus.TODO, stored.status()));
    }

    @Test
    void updateTaskRejectsInvalidFieldsAndKeepsStoredTaskUnchanged() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        assertFailure(
                service.updateTask(new EntityId(100), " \t\n", "Changed", TaskPriority.HIGH, JULY_15),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateTask(new EntityId(100), "Changed", "Changed", null, JULY_15),
                ErrorCode.VALIDATION_ERROR);
        assertFailure(
                service.updateTask(new EntityId(100), "Changed", "Changed", TaskPriority.HIGH, null),
                ErrorCode.VALIDATION_ERROR);

        TaskView stored = service.getTask(new EntityId(100)).getPayload();
        assertAll(
                () -> assertEquals("Plan work", stored.title()),
                () -> assertEquals("Scope", stored.description()),
                () -> assertEquals(TaskPriority.MEDIUM, stored.priority()),
                () -> assertEquals(JUNE_30, stored.dueDate()),
                () -> assertEquals(TaskStatus.TODO, stored.status()));
    }

    @Test
    void updateTaskRejectsNullId() {
        TaskService service = newService(100);

        OperationResult<TaskView> result =
                service.updateTask(null, "Write tests", "Cover service", TaskPriority.HIGH, JULY_15);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void deleteTaskRemovesExistingTask() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        OperationResult<Void> result = service.deleteTask(new EntityId(100));

        assertSuccess(result);
        assertFailure(service.getTask(new EntityId(100)), ErrorCode.NOT_FOUND);
    }

    @Test
    void deleteTaskReturnsNotFoundForMissingTask() {
        TaskService service = newService(100);

        OperationResult<Void> result = service.deleteTask(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void missingTaskMutationsReturnNotFoundWithoutChangingExistingTask() {
        TaskService service = newService(100);
        EntityId existingId = new EntityId(100);
        EntityId missingId = new EntityId(999);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);
        service.markTaskCompleted(existingId);
        TaskView before = service.getTask(existingId).getPayload();

        assertFailure(
                service.updateTask(missingId, "Write tests", "Cover service", TaskPriority.HIGH, JULY_15),
                ErrorCode.NOT_FOUND);
        assertSameTaskView(before, service.getTask(existingId).getPayload());

        assertFailure(service.deleteTask(missingId), ErrorCode.NOT_FOUND);
        assertSameTaskView(before, service.getTask(existingId).getPayload());

        assertFailure(service.markTaskCompleted(missingId), ErrorCode.NOT_FOUND);
        assertSameTaskView(before, service.getTask(existingId).getPayload());

        assertFailure(service.reopenTask(missingId), ErrorCode.NOT_FOUND);
        assertSameTaskView(before, service.getTask(existingId).getPayload());
    }

    @Test
    void deleteTaskRejectsNullId() {
        TaskService service = newService(100);

        OperationResult<Void> result = service.deleteTask(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void markTaskCompletedChangesTodoTaskToCompleted() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        OperationResult<TaskView> result = service.markTaskCompleted(new EntityId(100));

        assertSuccess(result);
        assertEquals(TaskStatus.COMPLETED, result.getPayload().status());
        assertEquals(TaskStatus.COMPLETED, service.getTask(new EntityId(100)).getPayload().status());
    }

    @Test
    void markTaskCompletedPersistsStatusWhenRepositoryReturnsDetachedCopies() {
        CopyingTaskRepository repository = new CopyingTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));
        EntityId id = new EntityId(100);
        repository.save(TaskItem.createTodo(id, "Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30));

        OperationResult<TaskView> result = service.markTaskCompleted(id);

        assertSuccess(result);
        assertEquals(TaskStatus.COMPLETED, service.getTask(id).getPayload().status());
    }

    @Test
    void markTaskCompletedReturnsNotFoundForMissingTask() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.markTaskCompleted(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void markTaskCompletedRejectsAlreadyCompletedTaskAndKeepsState() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);
        service.markTaskCompleted(new EntityId(100));

        OperationResult<TaskView> result = service.markTaskCompleted(new EntityId(100));

        assertFailure(result, ErrorCode.STATE_CONFLICT);
        assertEquals(TaskStatus.COMPLETED, service.getTask(new EntityId(100)).getPayload().status());
    }

    @Test
    void markTaskCompletedRejectsNullId() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.markTaskCompleted(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void reopenTaskChangesCompletedTaskToTodo() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);
        service.markTaskCompleted(new EntityId(100));

        OperationResult<TaskView> result = service.reopenTask(new EntityId(100));

        assertSuccess(result);
        assertEquals(TaskStatus.TODO, result.getPayload().status());
        assertEquals(TaskStatus.TODO, service.getTask(new EntityId(100)).getPayload().status());
    }

    @Test
    void reopenTaskPersistsStatusWhenRepositoryReturnsDetachedCopies() {
        CopyingTaskRepository repository = new CopyingTaskRepository();
        TaskService service = new TaskService(repository, new IncrementalIdGenerator(100));
        EntityId id = new EntityId(100);
        repository.save(new TaskItem(
                id, "Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30, TaskStatus.COMPLETED));

        OperationResult<TaskView> result = service.reopenTask(id);

        assertSuccess(result);
        assertEquals(TaskStatus.TODO, service.getTask(id).getPayload().status());
    }

    @Test
    void reopenTaskReturnsNotFoundForMissingTask() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.reopenTask(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND);
    }

    @Test
    void reopenTaskRejectsTodoTaskAndKeepsState() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        OperationResult<TaskView> result = service.reopenTask(new EntityId(100));

        assertFailure(result, ErrorCode.STATE_CONFLICT);
        assertEquals(TaskStatus.TODO, service.getTask(new EntityId(100)).getPayload().status());
    }

    @Test
    void reopenTaskRejectsNullId() {
        TaskService service = newService(100);

        OperationResult<TaskView> result = service.reopenTask(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void returnedTaskViewDoesNotChangeWhenStoredTaskIsUpdatedLater() {
        TaskService service = newService(100);
        TaskView original =
                service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30).getPayload();

        service.updateTask(new EntityId(100), "Write tests", "Cover service", TaskPriority.HIGH, JULY_15);
        service.markTaskCompleted(new EntityId(100));

        assertAll(
                () -> assertEquals("Plan work", original.title()),
                () -> assertEquals("Scope", original.description()),
                () -> assertEquals(TaskPriority.MEDIUM, original.priority()),
                () -> assertEquals(JUNE_30, original.dueDate()),
                () -> assertEquals(TaskStatus.TODO, original.status()));
    }

    @Test
    void returnedListSnapshotDoesNotChangeWhenStoredTasksChangeLater() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        List<TaskView> originalList = service.listTasks().getPayload();
        TaskView originalView = originalList.get(0);

        service.updateTask(new EntityId(100), "Write tests", "Cover service", TaskPriority.HIGH, JULY_15);
        service.markTaskCompleted(new EntityId(100));
        service.createTask("Second", "Scope", TaskPriority.LOW, JULY_1);

        assertAll(
                () -> assertEquals(1, originalList.size()),
                () -> assertEquals("Plan work", originalView.title()),
                () -> assertEquals("Scope", originalView.description()),
                () -> assertEquals(TaskPriority.MEDIUM, originalView.priority()),
                () -> assertEquals(JUNE_30, originalView.dueDate()),
                () -> assertEquals(TaskStatus.TODO, originalView.status()));
    }

    @Test
    void returnedListCannotModifyServiceStorage() {
        TaskService service = newService(100);
        service.createTask("Plan work", "Scope", TaskPriority.MEDIUM, JUNE_30);

        List<TaskView> views = service.listTasks().getPayload();

        assertThrows(UnsupportedOperationException.class, () -> views.add(new TaskView(
                new EntityId(999), "External", "Mutation", TaskPriority.LOW, JULY_1, TaskStatus.TODO)));
        assertEquals(1, service.listTasks().getPayload().size());
    }

    @Test
    void filteredResultsAreTaskViewsAndDoNotExposeTaskItems() {
        TaskService service = serviceWithMixedTasks();

        OperationResult<List<TaskView>> result =
                service.listTasks(TaskQuery.of(TaskStatus.COMPLETED, TaskPriority.HIGH, JUNE_30));

        assertSuccess(result);
        assertThrows(UnsupportedOperationException.class, () -> result.getPayload().clear());
        assertInstanceOf(TaskView.class, result.getPayload().get(0));
    }

    private static TaskService newService(long startInclusive) {
        return new TaskService(new InMemoryTaskRepository(), new IncrementalIdGenerator(startInclusive));
    }

    private static TaskService serviceWithMixedTasks() {
        TaskService service = newService(100);
        service.createTask("Low todo", "Scope", TaskPriority.LOW, JUNE_30);
        service.createTask("High todo", "Scope", TaskPriority.HIGH, JULY_1);
        service.createTask("High done", "Scope", TaskPriority.HIGH, JUNE_30);
        service.markTaskCompleted(new EntityId(102));
        return service;
    }

    private static List<EntityId> idsOf(List<TaskView> views) {
        return views.stream().map(TaskView::id).toList();
    }

    private static void assertSameTaskView(TaskView expected, TaskView actual) {
        assertAll(
                () -> assertEquals(expected.id(), actual.id()),
                () -> assertEquals(expected.title(), actual.title()),
                () -> assertEquals(expected.description(), actual.description()),
                () -> assertEquals(expected.priority(), actual.priority()),
                () -> assertEquals(expected.dueDate(), actual.dueDate()),
                () -> assertEquals(expected.status(), actual.status()));
    }

    private static <T> void assertSuccess(OperationResult<T> result) {
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
    }

    private static <T> void assertFailure(OperationResult<T> result, ErrorCode errorCode) {
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals(errorCode, result.getErrorCode());
    }

    private static final class CopyingTaskRepository implements TaskRepository {
        private final Map<EntityId, TaskItem> tasks = new LinkedHashMap<>();

        @Override
        public void save(TaskItem task) {
            Objects.requireNonNull(task, "task");
            tasks.put(task.getId(), copyOf(task));
        }

        @Override
        public Optional<TaskItem> findById(EntityId id) {
            Objects.requireNonNull(id, "id");
            TaskItem task = tasks.get(id);
            return task == null ? Optional.empty() : Optional.of(copyOf(task));
        }

        @Override
        public List<TaskItem> findAll() {
            return tasks.values().stream()
                    .map(CopyingTaskRepository::copyOf)
                    .toList();
        }

        @Override
        public List<TaskItem> findBy(TaskQuery query) {
            Objects.requireNonNull(query, "query");
            return tasks.values().stream()
                    .filter(query::matches)
                    .map(CopyingTaskRepository::copyOf)
                    .toList();
        }

        @Override
        public boolean deleteById(EntityId id) {
            Objects.requireNonNull(id, "id");
            return tasks.remove(id) != null;
        }

        private static TaskItem copyOf(TaskItem task) {
            return new TaskItem(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getDueDate(),
                    task.getStatus());
        }
    }
}
