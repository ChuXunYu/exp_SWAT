package assistant.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.EntityId;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskViewTest {
    private static final EntityId ID = new EntityId(1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 6, 30);

    @Test
    void constructorStoresProvidedSnapshotFields() {
        TaskView view = new TaskView(
                ID, "Write tests", "Cover task service", TaskPriority.HIGH, DUE_DATE, TaskStatus.COMPLETED);

        assertAll(
                () -> assertEquals(ID, view.id()),
                () -> assertEquals("Write tests", view.title()),
                () -> assertEquals("Cover task service", view.description()),
                () -> assertEquals(TaskPriority.HIGH, view.priority()),
                () -> assertEquals(DUE_DATE, view.dueDate()),
                () -> assertEquals(TaskStatus.COMPLETED, view.status()));
    }

    @Test
    void constructorPreservesProvidedTitleAndDescriptionText() {
        TaskView view = new TaskView(
                ID, "  Write tests  ", "\tCover task service\n", TaskPriority.HIGH, DUE_DATE, TaskStatus.TODO);

        assertAll(
                () -> assertEquals("  Write tests  ", view.title()),
                () -> assertEquals("\tCover task service\n", view.description()));
    }

    @Test
    void constructorRejectsNullRequiredFields() {
        assertThrows(
                NullPointerException.class,
                () -> new TaskView(null, "Title", "Description", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> new TaskView(ID, null, "Description", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> new TaskView(ID, "Title", null, TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> new TaskView(ID, "Title", "Description", null, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> new TaskView(ID, "Title", "Description", TaskPriority.MEDIUM, null, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> new TaskView(ID, "Title", "Description", TaskPriority.MEDIUM, DUE_DATE, null));
    }

    @Test
    void constructorRejectsBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TaskView(ID, " \t\n", "Description", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
    }

    @Test
    void fromCopiesAllTaskFields() {
        TaskItem task = new TaskItem(
                ID, "Write tests", "Cover task service", TaskPriority.HIGH, DUE_DATE, TaskStatus.COMPLETED);

        TaskView view = TaskView.from(task);

        assertAll(
                () -> assertEquals(task.getId(), view.id()),
                () -> assertEquals(task.getTitle(), view.title()),
                () -> assertEquals(task.getDescription(), view.description()),
                () -> assertEquals(task.getPriority(), view.priority()),
                () -> assertEquals(task.getDueDate(), view.dueDate()),
                () -> assertEquals(task.getStatus(), view.status()));
    }

    @Test
    void fromRejectsNullTask() {
        assertThrows(NullPointerException.class, () -> TaskView.from(null));
    }

    @Test
    void isCompletedReflectsSnapshotStatus() {
        TaskView completedView = new TaskView(
                ID, "Write tests", "Cover task service", TaskPriority.HIGH, DUE_DATE, TaskStatus.COMPLETED);
        TaskView todoView = new TaskView(
                new EntityId(2), "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO);

        assertTrue(completedView.isCompleted());
        assertFalse(todoView.isCompleted());
    }

    @Test
    void fromCreatesSnapshotIndependentFromLaterTaskMutation() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);
        TaskView view = TaskView.from(task);

        task.updateDetails("Write tests", "Cover task service", TaskPriority.HIGH, LocalDate.of(2026, 7, 1));
        task.markCompleted();

        assertAll(
                () -> assertEquals("Plan work", view.title()),
                () -> assertEquals("Scope", view.description()),
                () -> assertEquals(TaskPriority.MEDIUM, view.priority()),
                () -> assertEquals(DUE_DATE, view.dueDate()),
                () -> assertEquals(TaskStatus.TODO, view.status()));
    }
}
