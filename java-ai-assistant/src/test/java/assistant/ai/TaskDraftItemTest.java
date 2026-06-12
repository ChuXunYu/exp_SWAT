package assistant.ai;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.task.TaskPriority;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskDraftItemTest {
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 6, 30);

    @Test
    void constructorNormalizesFieldsAndStoresDueDate() {
        TaskDraftItem item = new TaskDraftItem(
                "\u2003Write tests\t",
                " \tCover parser\u2003",
                TaskPriority.HIGH,
                DUE_DATE);

        assertAll(
                () -> assertEquals("Write tests", item.title()),
                () -> assertEquals("Cover parser", item.description()),
                () -> assertEquals(TaskPriority.HIGH, item.priority()),
                () -> assertEquals(DUE_DATE, item.dueDate()),
                () -> assertTrue(item.hasDueDate()));
    }

    @Test
    void nullDescriptionBecomesEmptyAndDueDateIsOptional() {
        TaskDraftItem item = new TaskDraftItem("Write tests", null, TaskPriority.MEDIUM, null);

        assertEquals("", item.description());
        assertNull(item.dueDate());
        assertFalse(item.hasDueDate());
    }

    @Test
    void rejectsInvalidRequiredFields() {
        assertThrows(NullPointerException.class, () -> new TaskDraftItem(null, "", TaskPriority.LOW, null));
        assertThrows(IllegalArgumentException.class, () -> new TaskDraftItem(" \t\n", "", TaskPriority.LOW, null));
        assertThrows(NullPointerException.class, () -> new TaskDraftItem("Title", "", null, null));
    }
}
