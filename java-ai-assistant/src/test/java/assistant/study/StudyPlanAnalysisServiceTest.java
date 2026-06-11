package assistant.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StudyPlanAnalysisServiceTest {
    private static final DateRange PERIOD =
            new DateRange(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14));

    private final StudyPlanAnalysisService service = new StudyPlanAnalysisService();

    @Test
    void analyzeStatusReturnsInProgressForZeroProgressInsidePeriod() {
        StudyPlan plan = planWithProgress(Progress.zero());

        assertEquals(StudyPlanStatus.IN_PROGRESS, service.analyzeStatus(plan, LocalDate.of(2026, 6, 10)));
    }

    @Test
    void analyzeStatusReturnsCompletedForCompleteProgress() {
        StudyPlan plan = planWithProgress(Progress.complete());

        assertEquals(StudyPlanStatus.COMPLETED, service.analyzeStatus(plan, LocalDate.of(2026, 6, 10)));
    }

    @Test
    void analyzeStatusReturnsNotStartedBeforeStartDate() {
        StudyPlan plan = planWithProgress(Progress.of(50));

        assertEquals(StudyPlanStatus.NOT_STARTED, service.analyzeStatus(plan, LocalDate.of(2026, 6, 7)));
    }

    @Test
    void analyzeStatusReturnsInProgressAtStartDate() {
        StudyPlan plan = planWithProgress(Progress.of(50));

        assertEquals(StudyPlanStatus.IN_PROGRESS, service.analyzeStatus(plan, LocalDate.of(2026, 6, 8)));
    }

    @Test
    void analyzeStatusReturnsInProgressInsidePeriod() {
        StudyPlan plan = planWithProgress(Progress.of(50));

        assertEquals(StudyPlanStatus.IN_PROGRESS, service.analyzeStatus(plan, LocalDate.of(2026, 6, 10)));
    }

    @Test
    void analyzeStatusReturnsInProgressOnEndDate() {
        StudyPlan plan = planWithProgress(Progress.of(50));

        assertEquals(StudyPlanStatus.IN_PROGRESS, service.analyzeStatus(plan, LocalDate.of(2026, 6, 14)));
    }

    @Test
    void analyzeStatusReturnsOverdueIncompleteAfterEndDate() {
        StudyPlan plan = planWithProgress(Progress.of(50));

        assertEquals(StudyPlanStatus.OVERDUE_INCOMPLETE, service.analyzeStatus(plan, LocalDate.of(2026, 6, 15)));
    }

    @Test
    void completedProgressHasPriorityOverNotStartedDate() {
        StudyPlan plan = planWithProgress(Progress.complete());

        assertEquals(StudyPlanStatus.COMPLETED, service.analyzeStatus(plan, LocalDate.of(2026, 6, 7)));
    }

    @Test
    void completedProgressHasPriorityOverOverdueDate() {
        StudyPlan plan = planWithProgress(Progress.complete());

        assertEquals(StudyPlanStatus.COMPLETED, service.analyzeStatus(plan, LocalDate.of(2026, 6, 15)));
    }

    @Test
    void isCompletedReflectsProgressValue() {
        assertFalse(service.isCompleted(planWithProgress(Progress.zero())));
        assertFalse(service.isCompleted(planWithProgress(Progress.of(50))));
        assertTrue(service.isCompleted(planWithProgress(Progress.complete())));
    }

    @Test
    void isOverdueIncompleteReflectsAnalyzedStatus() {
        assertFalse(service.isOverdueIncomplete(planWithProgress(Progress.of(50)), LocalDate.of(2026, 6, 14)));
        assertTrue(service.isOverdueIncomplete(planWithProgress(Progress.of(50)), LocalDate.of(2026, 6, 15)));
        assertFalse(service.isOverdueIncomplete(planWithProgress(Progress.complete()), LocalDate.of(2026, 6, 15)));
    }

    @Test
    void methodsRejectNullArguments() {
        StudyPlan plan = planWithProgress(Progress.zero());
        LocalDate currentDate = LocalDate.of(2026, 6, 10);

        assertThrows(NullPointerException.class, () -> service.analyzeStatus(null, currentDate));
        assertThrows(NullPointerException.class, () -> service.analyzeStatus(plan, null));
        assertThrows(NullPointerException.class, () -> service.isCompleted(null));
        assertThrows(NullPointerException.class, () -> service.isOverdueIncomplete(null, currentDate));
        assertThrows(NullPointerException.class, () -> service.isOverdueIncomplete(plan, null));
    }

    private static StudyPlan planWithProgress(Progress progress) {
        return new StudyPlan(new EntityId(1), "Learn Java", PERIOD, 12, progress);
    }
}
