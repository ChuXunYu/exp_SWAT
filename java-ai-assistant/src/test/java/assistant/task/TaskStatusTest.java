package assistant.task;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TaskStatusTest {
    @Test
    void exposesFixedStatusValuesInDeclaredOrder() {
        assertArrayEquals(new TaskStatus[] {TaskStatus.TODO, TaskStatus.COMPLETED}, TaskStatus.values());
    }

    @Test
    void todoIsNotCompleted() {
        assertFalse(TaskStatus.TODO.isCompleted());
    }

    @Test
    void completedIsCompleted() {
        assertTrue(TaskStatus.COMPLETED.isCompleted());
    }

    @Test
    void valueOfParsesDeclaredStatusName() {
        assertEquals(TaskStatus.COMPLETED, TaskStatus.valueOf("COMPLETED"));
    }

    @Test
    void valueOfRejectsUnknownStatusName() {
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.valueOf("DONE"));
    }

    @Test
    void nameUsesStableEnumConstantName() {
        assertEquals("TODO", TaskStatus.TODO.name());
    }
}
