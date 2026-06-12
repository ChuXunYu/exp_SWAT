package assistant.study;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StudyPlanViewTest {
    private static final EntityId ID = new EntityId(1);
    private static final DateRange PERIOD =
            new DateRange(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14));

    @Test
    void fromMapsAllFieldsFromPlan() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        StudyPlanView view = StudyPlanView.from(plan, new StudyPlanAnalysisService(), LocalDate.of(2026, 6, 10));

        assertAll(
                () -> assertEquals(plan.getId(), view.id()),
                () -> assertEquals(plan.getGoalName(), view.goalName()),
                () -> assertEquals(plan.getPeriod(), view.period()),
                () -> assertEquals(plan.getStartDate(), view.startDate()),
                () -> assertEquals(plan.getEndDate(), view.endDate()),
                () -> assertEquals(plan.getExpectedHours(), view.expectedHours()),
                () -> assertEquals(plan.getProgress(), view.progress()),
                () -> assertEquals(StudyPlanStatus.IN_PROGRESS, view.status()));
    }

    @Test
    void fromComputesDynamicStatusWithProvidedCurrentDate() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        StudyPlanView inProgress = StudyPlanView.from(plan, new StudyPlanAnalysisService(), LocalDate.of(2026, 6, 10));
        StudyPlanView overdue = StudyPlanView.from(plan, new StudyPlanAnalysisService(), LocalDate.of(2026, 6, 15));

        assertEquals(StudyPlanStatus.IN_PROGRESS, inProgress.status());
        assertEquals(StudyPlanStatus.OVERDUE_INCOMPLETE, overdue.status());
    }

    @Test
    void fromDelegatesStatusAnalysisToProvidedServiceWithProvidedDate() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        LocalDate currentDate = LocalDate.of(2033, 3, 21);
        when(analysisService.analyzeStatus(plan, currentDate)).thenReturn(StudyPlanStatus.COMPLETED);

        StudyPlanView view = StudyPlanView.from(plan, analysisService, currentDate);

        assertEquals(StudyPlanStatus.COMPLETED, view.status());
        verify(analysisService).analyzeStatus(plan, currentDate);
    }

    @Test
    void compactConstructorRejectsInvalidFieldValues() {
        Progress progress = Progress.zero();
        StudyPlanStatus status = StudyPlanStatus.NOT_STARTED;

        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(null, "Learn Java", PERIOD, PERIOD.startDate(), PERIOD.endDate(), 12, progress, status));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(ID, null, PERIOD, PERIOD.startDate(), PERIOD.endDate(), 12, progress, status));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(ID, "Learn Java", null, PERIOD.startDate(), PERIOD.endDate(), 12, progress, status));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, null, PERIOD.endDate(), 12, progress, status));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, PERIOD.startDate(), null, 12, progress, status));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, PERIOD.startDate(), PERIOD.endDate(), 12, null, status));
        assertThrows(NullPointerException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, PERIOD.startDate(), PERIOD.endDate(), 12, progress, null));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanView(ID, " \t\n", PERIOD, PERIOD.startDate(), PERIOD.endDate(), 12, progress, status));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, PERIOD.startDate(), PERIOD.endDate(), 0, progress, status));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, PERIOD.startDate().minusDays(1), PERIOD.endDate(), 12, progress, status));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanView(ID, "Learn Java", PERIOD, PERIOD.startDate(), PERIOD.endDate().plusDays(1), 12, progress, status));
    }

    @Test
    void fromRejectsNullArguments() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.zero());
        StudyPlanAnalysisService analysisService = new StudyPlanAnalysisService();

        assertThrows(NullPointerException.class, () -> StudyPlanView.from(null, analysisService, LocalDate.of(2026, 6, 10)));
        assertThrows(NullPointerException.class, () -> StudyPlanView.from(plan, null, LocalDate.of(2026, 6, 10)));
        assertThrows(NullPointerException.class, () -> StudyPlanView.from(plan, analysisService, null));
    }
}
