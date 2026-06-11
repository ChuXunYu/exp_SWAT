package assistant.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryTaskRepositoryTest {
    private static final LocalDate JUNE_30 = LocalDate.of(2026, 6, 30);
    private static final LocalDate JULY_1 = LocalDate.of(2026, 7, 1);

    @Test
    void saveAndFindByIdReturnsStoredTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskItem task = todoTask(1, TaskPriority.MEDIUM, JUNE_30);

        repository.save(task);

        assertSame(task, repository.findById(new EntityId(1)).orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyWhenTaskDoesNotExist() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void saveReplacesTaskWithSameId() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskItem original = todoTask(1, TaskPriority.LOW, JUNE_30);
        TaskItem replacement = completedTask(1, TaskPriority.HIGH, JULY_1);

        repository.save(original);
        repository.save(replacement);

        assertEquals(List.of(replacement), repository.findAll());
        assertSame(replacement, repository.findById(new EntityId(1)).orElseThrow());
    }

    @Test
    void findAllReturnsTasksInInsertionOrder() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskItem first = todoTask(1, TaskPriority.LOW, JUNE_30);
        TaskItem second = todoTask(2, TaskPriority.HIGH, JULY_1);

        repository.save(first);
        repository.save(second);

        assertEquals(List.of(first, second), repository.findAll());
    }

    @Test
    void findAllReturnsUnmodifiableSnapshotList() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskItem first = todoTask(1, TaskPriority.LOW, JUNE_30);
        TaskItem second = todoTask(2, TaskPriority.HIGH, JULY_1);
        repository.save(first);

        List<TaskItem> snapshot = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
        repository.save(second);
        assertEquals(1, snapshot.size());
    }

    @Test
    void findByFiltersByStatus() {
        InMemoryTaskRepository repository = repositoryWithMixedTasks();

        List<TaskItem> result = repository.findBy(TaskQuery.byStatus(TaskStatus.COMPLETED));

        assertEquals(List.of(new EntityId(3)), idsOf(result));
    }

    @Test
    void findByFiltersByPriority() {
        InMemoryTaskRepository repository = repositoryWithMixedTasks();

        List<TaskItem> result = repository.findBy(TaskQuery.byPriority(TaskPriority.HIGH));

        assertEquals(List.of(new EntityId(2), new EntityId(3)), idsOf(result));
    }

    @Test
    void findByFiltersByDueDate() {
        InMemoryTaskRepository repository = repositoryWithMixedTasks();

        List<TaskItem> result = repository.findBy(TaskQuery.byDueDate(JUNE_30));

        assertEquals(List.of(new EntityId(1), new EntityId(3)), idsOf(result));
    }

    @Test
    void findByAppliesCombinedQueryInInsertionOrder() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskItem first = completedTask(1, TaskPriority.HIGH, JUNE_30);
        TaskItem second = todoTask(2, TaskPriority.HIGH, JUNE_30);
        TaskItem third = completedTask(3, TaskPriority.HIGH, JUNE_30);
        repository.save(first);
        repository.save(second);
        repository.save(third);

        List<TaskItem> result = repository.findBy(TaskQuery.of(TaskStatus.COMPLETED, TaskPriority.HIGH, JUNE_30));

        assertEquals(List.of(first, third), result);
    }

    @Test
    void findByReturnsUnmodifiableSnapshotList() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        TaskItem first = todoTask(1, TaskPriority.LOW, JUNE_30);
        TaskItem second = todoTask(2, TaskPriority.LOW, JUNE_30);
        repository.save(first);

        List<TaskItem> snapshot = repository.findBy(TaskQuery.byPriority(TaskPriority.LOW));

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
        repository.save(second);
        assertEquals(1, snapshot.size());
    }

    @Test
    void deleteByIdRemovesExistingTask() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        repository.save(todoTask(1, TaskPriority.MEDIUM, JUNE_30));

        assertTrue(repository.deleteById(new EntityId(1)));
        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void deleteByIdReturnsFalseWhenTaskDoesNotExist() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertFalse(repository.deleteById(new EntityId(1)));
    }

    @Test
    void methodsRejectNullArguments() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.findBy(null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    private static InMemoryTaskRepository repositoryWithMixedTasks() {
        InMemoryTaskRepository repository = new InMemoryTaskRepository();
        repository.save(todoTask(1, TaskPriority.LOW, JUNE_30));
        repository.save(todoTask(2, TaskPriority.HIGH, JULY_1));
        repository.save(completedTask(3, TaskPriority.HIGH, JUNE_30));
        return repository;
    }

    private static List<EntityId> idsOf(List<TaskItem> tasks) {
        return tasks.stream().map(TaskItem::getId).toList();
    }

    private static TaskItem todoTask(long id, TaskPriority priority, LocalDate dueDate) {
        return TaskItem.createTodo(new EntityId(id), "Task " + id, "Description", priority, dueDate);
    }

    private static TaskItem completedTask(long id, TaskPriority priority, LocalDate dueDate) {
        return new TaskItem(new EntityId(id), "Task " + id, "Description", priority, dueDate, TaskStatus.COMPLETED);
    }
}
