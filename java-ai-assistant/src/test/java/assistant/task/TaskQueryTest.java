package assistant.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskQueryTest {
    private static final LocalDate JUNE_30 = LocalDate.of(2026, 6, 30);
    private static final LocalDate JULY_1 = LocalDate.of(2026, 7, 1);

    @Test
    void allQueryMatchesEveryTask() {
        TaskQuery query = TaskQuery.all();

        assertTrue(query.matches(todoTask(1, TaskPriority.LOW, JUNE_30)));
        assertTrue(query.matches(completedTask(2, TaskPriority.HIGH, JULY_1)));
    }

    @Test
    void statusQueryMatchesOnlySameStatus() {
        TaskQuery query = TaskQuery.byStatus(TaskStatus.COMPLETED);

        assertTrue(query.matches(completedTask(1, TaskPriority.MEDIUM, JUNE_30)));
        assertFalse(query.matches(todoTask(2, TaskPriority.MEDIUM, JUNE_30)));
    }

    @Test
    void priorityQueryMatchesOnlySamePriority() {
        TaskQuery query = TaskQuery.byPriority(TaskPriority.HIGH);

        assertTrue(query.matches(todoTask(1, TaskPriority.HIGH, JUNE_30)));
        assertFalse(query.matches(todoTask(2, TaskPriority.LOW, JUNE_30)));
    }

    @Test
    void dueDateQueryMatchesOnlySameDueDate() {
        TaskQuery query = TaskQuery.byDueDate(JUNE_30);

        assertTrue(query.matches(todoTask(1, TaskPriority.HIGH, JUNE_30)));
        assertFalse(query.matches(todoTask(2, TaskPriority.HIGH, JULY_1)));
    }

    @Test
    void combinedQueryRequiresEveryProvidedFilterToMatch() {
        TaskQuery query = TaskQuery.of(TaskStatus.COMPLETED, TaskPriority.HIGH, JUNE_30);

        assertTrue(query.matches(completedTask(1, TaskPriority.HIGH, JUNE_30)));
        assertFalse(query.matches(todoTask(2, TaskPriority.HIGH, JUNE_30)));
        assertFalse(query.matches(completedTask(3, TaskPriority.LOW, JUNE_30)));
        assertFalse(query.matches(completedTask(4, TaskPriority.HIGH, JULY_1)));
    }

    @Test
    void ofAllowsNullComponentsAsWildcards() {
        TaskQuery query = TaskQuery.of(null, TaskPriority.HIGH, null);

        assertTrue(query.matches(todoTask(1, TaskPriority.HIGH, JUNE_30)));
        assertTrue(query.matches(completedTask(2, TaskPriority.HIGH, JULY_1)));
        assertFalse(query.matches(todoTask(3, TaskPriority.MEDIUM, JUNE_30)));
    }

    @Test
    void exposesFilterPresenceFlags() {
        TaskQuery query = TaskQuery.of(TaskStatus.TODO, null, JUNE_30);

        assertTrue(query.hasStatusFilter());
        assertFalse(query.hasPriorityFilter());
        assertTrue(query.hasDueDateFilter());
    }

    @Test
    void singleCriterionFactoriesRejectNullCriterion() {
        assertThrows(NullPointerException.class, () -> TaskQuery.byStatus(null));
        assertThrows(NullPointerException.class, () -> TaskQuery.byPriority(null));
        assertThrows(NullPointerException.class, () -> TaskQuery.byDueDate(null));
    }

    @Test
    void matchesRejectsNullTask() {
        assertThrows(NullPointerException.class, () -> TaskQuery.all().matches(null));
    }

    private static TaskItem todoTask(long id, TaskPriority priority, LocalDate dueDate) {
        return TaskItem.createTodo(new EntityId(id), "Task " + id, "Description", priority, dueDate);
    }

    private static TaskItem completedTask(long id, TaskPriority priority, LocalDate dueDate) {
        return new TaskItem(new EntityId(id), "Task " + id, "Description", priority, dueDate, TaskStatus.COMPLETED);
    }
}
