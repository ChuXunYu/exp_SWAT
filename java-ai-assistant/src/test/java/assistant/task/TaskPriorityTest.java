package assistant.task;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TaskPriorityTest {
    @Test
    void exposesFixedPriorityValuesInDeclaredOrder() {
        assertArrayEquals(
                new TaskPriority[] {TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH},
                TaskPriority.values());
    }

    @Test
    void defaultPriorityReturnsMedium() {
        assertEquals(TaskPriority.MEDIUM, TaskPriority.defaultPriority());
    }

    @Test
    void valueOfParsesDeclaredPriorityName() {
        assertEquals(TaskPriority.HIGH, TaskPriority.valueOf("HIGH"));
    }

    @Test
    void valueOfRejectsUnknownPriorityName() {
        assertThrows(IllegalArgumentException.class, () -> TaskPriority.valueOf("URGENT"));
    }

    @Test
    void nameUsesStableEnumConstantName() {
        assertEquals("LOW", TaskPriority.LOW.name());
    }
}
