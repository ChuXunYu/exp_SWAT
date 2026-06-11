package assistant.study;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StudyPlanStatusTest {
    @Test
    void exposesFixedStatusValuesInDeclaredOrder() {
        assertArrayEquals(
                new StudyPlanStatus[] {
                    StudyPlanStatus.NOT_STARTED,
                    StudyPlanStatus.IN_PROGRESS,
                    StudyPlanStatus.COMPLETED,
                    StudyPlanStatus.OVERDUE_INCOMPLETE
                },
                StudyPlanStatus.values());
    }

    @Test
    void displayNameReturnsStableChineseText() {
        assertEquals("未开始", StudyPlanStatus.NOT_STARTED.displayName());
        assertEquals("进行中", StudyPlanStatus.IN_PROGRESS.displayName());
        assertEquals("已完成", StudyPlanStatus.COMPLETED.displayName());
        assertEquals("逾期未完成", StudyPlanStatus.OVERDUE_INCOMPLETE.displayName());
    }

    @Test
    void notStartedSemanticFlagsMatchOnlyNotStarted() {
        assertTrue(StudyPlanStatus.NOT_STARTED.isNotStarted());
        assertFalse(StudyPlanStatus.IN_PROGRESS.isNotStarted());
        assertFalse(StudyPlanStatus.COMPLETED.isNotStarted());
        assertFalse(StudyPlanStatus.OVERDUE_INCOMPLETE.isNotStarted());
    }

    @Test
    void inProgressSemanticFlagsMatchOnlyInProgress() {
        assertFalse(StudyPlanStatus.NOT_STARTED.isInProgress());
        assertTrue(StudyPlanStatus.IN_PROGRESS.isInProgress());
        assertFalse(StudyPlanStatus.COMPLETED.isInProgress());
        assertFalse(StudyPlanStatus.OVERDUE_INCOMPLETE.isInProgress());
    }

    @Test
    void completedSemanticFlagsMatchOnlyCompleted() {
        assertFalse(StudyPlanStatus.NOT_STARTED.isCompleted());
        assertFalse(StudyPlanStatus.IN_PROGRESS.isCompleted());
        assertTrue(StudyPlanStatus.COMPLETED.isCompleted());
        assertFalse(StudyPlanStatus.OVERDUE_INCOMPLETE.isCompleted());
    }

    @Test
    void overdueIncompleteSemanticFlagsMatchOnlyOverdueIncomplete() {
        assertFalse(StudyPlanStatus.NOT_STARTED.isOverdueIncomplete());
        assertFalse(StudyPlanStatus.IN_PROGRESS.isOverdueIncomplete());
        assertFalse(StudyPlanStatus.COMPLETED.isOverdueIncomplete());
        assertTrue(StudyPlanStatus.OVERDUE_INCOMPLETE.isOverdueIncomplete());
    }

    @Test
    void valueOfParsesDeclaredStatusName() {
        assertEquals(StudyPlanStatus.IN_PROGRESS, StudyPlanStatus.valueOf("IN_PROGRESS"));
    }

    @Test
    void valueOfRejectsUnknownStatusName() {
        assertThrows(IllegalArgumentException.class, () -> StudyPlanStatus.valueOf("ACTIVE"));
    }

    @Test
    void nameUsesStableEnumConstantName() {
        assertEquals("NOT_STARTED", StudyPlanStatus.NOT_STARTED.name());
    }
}
