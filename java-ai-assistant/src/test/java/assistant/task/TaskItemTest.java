package assistant.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.BusinessException;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskItemTest {
    private static final EntityId ID = new EntityId(1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 6, 30);

    @Test
    void constructorStoresProvidedFields() {
        TaskItem task = new TaskItem(
                ID, "Write tests", "Cover task entity", TaskPriority.HIGH, DUE_DATE, TaskStatus.COMPLETED);

        assertAll(
                () -> assertEquals(ID, task.getId()),
                () -> assertEquals("Write tests", task.getTitle()),
                () -> assertEquals("Cover task entity", task.getDescription()),
                () -> assertEquals(TaskPriority.HIGH, task.getPriority()),
                () -> assertEquals(DUE_DATE, task.getDueDate()),
                () -> assertEquals(TaskStatus.COMPLETED, task.getStatus()),
                () -> assertTrue(task.isCompleted()));
    }

    @Test
    void createTodoCreatesTaskWithTodoStatus() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertFalse(task.isCompleted());
    }

    @Test
    void constructorNormalizesTitleAndDescription() {
        TaskItem task = new TaskItem(
                ID,
                "\u2003Write plan\t",
                " \tPrepare outline\u2003",
                TaskPriority.LOW,
                DUE_DATE,
                TaskStatus.TODO);

        assertEquals("Write plan", task.getTitle());
        assertEquals("Prepare outline", task.getDescription());
    }

    @Test
    void constructorConvertsNullDescriptionToEmptyString() {
        TaskItem task = new TaskItem(ID, "Plan work", null, TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO);

        assertEquals("", task.getDescription());
    }

    @Test
    void constructorAllowsBlankDescriptionAsEmptyString() {
        TaskItem task = new TaskItem(ID, "Plan work", " \t\u2003\n", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO);

        assertEquals("", task.getDescription());
    }

    @Test
    void normalizesTitleAndDescription() {
        TaskItem task = TaskItem.createTodo(ID, "\u2003Write plan\u2003", "\u2003Prepare outline\u2003", TaskPriority.LOW, DUE_DATE);

        assertEquals("Write plan", task.getTitle());
        assertEquals("Prepare outline", task.getDescription());
    }

    @Test
    void convertsNullDescriptionToEmptyString() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", null, TaskPriority.MEDIUM, DUE_DATE);

        assertEquals("", task.getDescription());
    }

    @Test
    void allowsBlankDescriptionAsEmptyString() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", " \t\n", TaskPriority.MEDIUM, DUE_DATE);

        assertEquals("", task.getDescription());
    }

    @Test
    void keepsInternalWhitespaceInTitleAndDescription() {
        TaskItem task = TaskItem.createTodo(
                ID, "Plan  daily\twork", "Keep  inner\nspacing", TaskPriority.MEDIUM, DUE_DATE);

        assertEquals("Plan  daily\twork", task.getTitle());
        assertEquals("Keep  inner\nspacing", task.getDescription());
    }

    @Test
    void rejectsNullId() {
        assertThrows(
                NullPointerException.class,
                () -> new TaskItem(null, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> TaskItem.createTodo(null, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE));
    }

    @Test
    void rejectsNullTitle() {
        assertThrows(
                NullPointerException.class,
                () -> new TaskItem(ID, null, "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class,
                () -> TaskItem.createTodo(ID, null, "Scope", TaskPriority.MEDIUM, DUE_DATE));
    }

    @Test
    void rejectsBlankTitle() {
        for (String blankTitle : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TaskItem(ID, blankTitle, "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.TODO));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> TaskItem.createTodo(ID, blankTitle, "Scope", TaskPriority.MEDIUM, DUE_DATE));
        }
    }

    @Test
    void rejectsNullPriority() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        assertThrows(
                NullPointerException.class,
                () -> new TaskItem(ID, "Plan work", "Scope", null, DUE_DATE, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class, () -> TaskItem.createTodo(ID, "Plan work", "Scope", null, DUE_DATE));
        assertThrows(
                NullPointerException.class, () -> task.updateDetails("Plan work", "Scope", null, DUE_DATE));
    }

    @Test
    void rejectsNullDueDate() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        assertThrows(
                NullPointerException.class,
                () -> new TaskItem(ID, "Plan work", "Scope", TaskPriority.MEDIUM, null, TaskStatus.TODO));
        assertThrows(
                NullPointerException.class, () -> TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, null));
        assertThrows(
                NullPointerException.class,
                () -> task.updateDetails("Plan work", "Scope", TaskPriority.MEDIUM, null));
    }

    @Test
    void rejectsNullStatus() {
        assertThrows(
                NullPointerException.class,
                () -> new TaskItem(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, null));
    }

    @Test
    void updateDetailsChangesEditableFieldsOnly() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
        LocalDate newDueDate = LocalDate.of(2026, 7, 15);

        task.updateDetails("Write tests", "Cover entity", TaskPriority.HIGH, newDueDate);

        assertAll(
                () -> assertEquals(ID, task.getId()),
                () -> assertEquals("Write tests", task.getTitle()),
                () -> assertEquals("Cover entity", task.getDescription()),
                () -> assertEquals(TaskPriority.HIGH, task.getPriority()),
                () -> assertEquals(newDueDate, task.getDueDate()),
                () -> assertEquals(TaskStatus.COMPLETED, task.getStatus()));
    }

    @Test
    void updateDetailsNormalizesNewTitleAndDescription() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        task.updateDetails("\u2003Write tests\u2003", "\u2003Cover task entity\u2003", TaskPriority.HIGH, DUE_DATE);

        assertEquals("Write tests", task.getTitle());
        assertEquals("Cover task entity", task.getDescription());
    }

    @Test
    void updateDetailsConvertsNullDescriptionToEmptyString() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        task.updateDetails("Write tests", null, TaskPriority.HIGH, LocalDate.of(2026, 7, 15));

        assertEquals("", task.getDescription());
    }

    @Test
    void updateDetailsAllowsBlankDescriptionAsEmptyString() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        task.updateDetails("Write tests", "\u2003 \t\n", TaskPriority.HIGH, LocalDate.of(2026, 7, 15));

        assertEquals("", task.getDescription());
    }

    @Test
    void updateDetailsKeepsInternalWhitespaceInNewTitleAndDescription() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        task.updateDetails("Write  daily\twork", "Keep  inner\nspacing", TaskPriority.HIGH, DUE_DATE);

        assertEquals("Write  daily\twork", task.getTitle());
        assertEquals("Keep  inner\nspacing", task.getDescription());
    }

    @Test
    void updateDetailsLeavesTaskUnchangedWhenTitleIsNull() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);

        assertThrows(
                NullPointerException.class,
                () -> task.updateDetails(null, "Changed", TaskPriority.HIGH, LocalDate.of(2026, 7, 15)));

        assertTaskState(task, ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
    }

    @Test
    void updateDetailsLeavesTaskUnchangedWhenTitleIsInvalid() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);

        assertThrows(
                IllegalArgumentException.class,
                () -> task.updateDetails(" \t\n", "Changed", TaskPriority.HIGH, LocalDate.of(2026, 7, 15)));

        assertTaskState(task, ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
    }

    @Test
    void updateDetailsLeavesTaskUnchangedWhenPriorityIsNull() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);

        assertThrows(
                NullPointerException.class,
                () -> task.updateDetails("Changed", "Changed", null, LocalDate.of(2026, 7, 15)));

        assertTaskState(task, ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
    }

    @Test
    void updateDetailsLeavesTaskUnchangedWhenDueDateIsNull() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);

        assertThrows(
                NullPointerException.class, () -> task.updateDetails("Changed", "Changed", TaskPriority.HIGH, null));

        assertTaskState(task, ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
    }

    @Test
    void markCompletedChangesTodoTaskToCompleted() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        task.markCompleted();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertTrue(task.isCompleted());
    }

    @Test
    void markCompletedRejectsAlreadyCompletedTaskAndKeepsState() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);

        BusinessException exception = assertThrows(BusinessException.class, task::markCompleted);

        assertEquals(ErrorCode.STATE_CONFLICT, exception.getErrorCode());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void reopenChangesCompletedTaskToTodo() {
        TaskItem task = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);

        task.reopen();

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertFalse(task.isCompleted());
    }

    @Test
    void reopenRejectsTodoTaskAndKeepsState() {
        TaskItem task = TaskItem.createTodo(ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE);

        BusinessException exception = assertThrows(BusinessException.class, task::reopen);

        assertEquals(ErrorCode.STATE_CONFLICT, exception.getErrorCode());
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void repeatedConflictDoesNotChangeEditableFields() {
        TaskItem completedTask = new TaskItem(
                ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
        TaskItem todoTask = TaskItem.createTodo(
                new EntityId(2), "Write tests", "Cover entity", TaskPriority.HIGH, LocalDate.of(2026, 7, 15));

        assertThrows(BusinessException.class, completedTask::markCompleted);
        assertThrows(BusinessException.class, todoTask::reopen);

        assertTaskState(completedTask, ID, "Plan work", "Scope", TaskPriority.MEDIUM, DUE_DATE, TaskStatus.COMPLETED);
        assertTaskState(
                todoTask,
                new EntityId(2),
                "Write tests",
                "Cover entity",
                TaskPriority.HIGH,
                LocalDate.of(2026, 7, 15),
                TaskStatus.TODO);
    }

    private static void assertTaskState(
            TaskItem task,
            EntityId id,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate,
            TaskStatus status) {
        assertAll(
                () -> assertEquals(id, task.getId()),
                () -> assertEquals(title, task.getTitle()),
                () -> assertEquals(description, task.getDescription()),
                () -> assertEquals(priority, task.getPriority()),
                () -> assertEquals(dueDate, task.getDueDate()),
                () -> assertEquals(status, task.getStatus()));
    }
}
