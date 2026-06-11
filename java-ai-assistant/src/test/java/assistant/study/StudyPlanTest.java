package assistant.study;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StudyPlanTest {
    private static final EntityId ID = new EntityId(1);
    private static final DateRange PERIOD =
            new DateRange(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14));

    @Test
    void constructorStoresProvidedFields() {
        Progress progress = Progress.of(50);

        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, progress);

        assertStudyPlanState(plan, ID, "Learn Java", PERIOD, 12, progress);
    }

    @Test
    void createFactoryDefaultsProgressToZero() {
        StudyPlan plan = StudyPlan.create(ID, "Learn Java", PERIOD, 12);

        assertEquals(Progress.zero(), plan.getProgress());
    }

    @Test
    void createFactoryStoresSpecifiedProgress() {
        Progress progress = Progress.of(75);

        StudyPlan plan = StudyPlan.create(ID, "Learn Java", PERIOD, 12, progress);

        assertEquals(progress, plan.getProgress());
    }

    @Test
    void constructorNormalizesGoalName() {
        StudyPlan plan = new StudyPlan(ID, "\u2003Learn Java\t", PERIOD, 12, Progress.zero());

        assertEquals("Learn Java", plan.getGoalName());
    }

    @Test
    void keepsInternalWhitespaceInGoalName() {
        StudyPlan plan = StudyPlan.create(ID, "Learn  Java\twith\npractice", PERIOD, 12);

        assertEquals("Learn  Java\twith\npractice", plan.getGoalName());
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () -> new StudyPlan(null, "Learn Java", PERIOD, 12, Progress.zero()));
        assertThrows(NullPointerException.class, () -> new StudyPlan(ID, null, PERIOD, 12, Progress.zero()));
        assertThrows(NullPointerException.class, () -> new StudyPlan(ID, "Learn Java", null, 12, Progress.zero()));
        assertThrows(NullPointerException.class, () -> new StudyPlan(ID, "Learn Java", PERIOD, 12, null));

        assertThrows(NullPointerException.class, () -> StudyPlan.create(null, "Learn Java", PERIOD, 12));
        assertThrows(NullPointerException.class, () -> StudyPlan.create(ID, null, PERIOD, 12));
        assertThrows(NullPointerException.class, () -> StudyPlan.create(ID, "Learn Java", null, 12));
        assertThrows(NullPointerException.class, () -> StudyPlan.create(ID, "Learn Java", PERIOD, 12, null));
    }

    @Test
    void rejectsBlankGoalName() {
        for (String blankGoalName : new String[] {"", " \t\n", "\u2003"}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new StudyPlan(ID, blankGoalName, PERIOD, 12, Progress.zero()));
            assertThrows(
                    IllegalArgumentException.class, () -> StudyPlan.create(ID, blankGoalName, PERIOD, 12));
        }
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 8)));
    }

    @Test
    void rejectsNonPositiveExpectedHours() {
        for (int expectedHours : new int[] {0, -1}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new StudyPlan(ID, "Learn Java", PERIOD, expectedHours, Progress.zero()));
            assertThrows(
                    IllegalArgumentException.class, () -> StudyPlan.create(ID, "Learn Java", PERIOD, expectedHours));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> StudyPlan.create(ID, "Learn Java", PERIOD, expectedHours, Progress.of(25)));
        }
    }

    @Test
    void exposesStartAndEndDatesFromPeriod() {
        StudyPlan plan = StudyPlan.create(ID, "Learn Java", PERIOD, 12);

        assertEquals(PERIOD.startDate(), plan.getStartDate());
        assertEquals(PERIOD.endDate(), plan.getEndDate());
    }

    @Test
    void isCompletedReflectsProgressOnly() {
        assertTrue(new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.complete()).isCompleted());
        assertFalse(new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(99)).isCompleted());
    }

    @Test
    void updateDetailsChangesEditableFieldsOnly() {
        Progress progress = Progress.of(40);
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, progress);
        DateRange newPeriod = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        plan.updateDetails("Practice algorithms", newPeriod, 20);

        assertStudyPlanState(plan, ID, "Practice algorithms", newPeriod, 20, progress);
    }

    @Test
    void updateDetailsNormalizesNewGoalName() {
        StudyPlan plan = StudyPlan.create(ID, "Learn Java", PERIOD, 12);

        plan.updateDetails("\u2003Practice algorithms\u2003", PERIOD, 20);

        assertEquals("Practice algorithms", plan.getGoalName());
    }

    @Test
    void updateDetailsRejectsInvalidGoalNameAndKeepsFieldsUnchanged() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        assertThrows(NullPointerException.class, () -> plan.updateDetails(null, newPeriod(), 20));
        assertStudyPlanState(plan, ID, "Learn Java", PERIOD, 12, Progress.of(40));

        assertThrows(IllegalArgumentException.class, () -> plan.updateDetails(" \t\n", newPeriod(), 20));
        assertStudyPlanState(plan, ID, "Learn Java", PERIOD, 12, Progress.of(40));
    }

    @Test
    void updateDetailsRejectsNullPeriodAndKeepsFieldsUnchanged() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        assertThrows(NullPointerException.class, () -> plan.updateDetails("Practice algorithms", null, 20));

        assertStudyPlanState(plan, ID, "Learn Java", PERIOD, 12, Progress.of(40));
    }

    @Test
    void updateDetailsRejectsNonPositiveExpectedHoursAndKeepsFieldsUnchanged() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        assertThrows(IllegalArgumentException.class, () -> plan.updateDetails("Practice algorithms", newPeriod(), 0));
        assertStudyPlanState(plan, ID, "Learn Java", PERIOD, 12, Progress.of(40));

        assertThrows(IllegalArgumentException.class, () -> plan.updateDetails("Practice algorithms", newPeriod(), -1));
        assertStudyPlanState(plan, ID, "Learn Java", PERIOD, 12, Progress.of(40));
    }

    @Test
    void updateProgressAcceptsZeroProgress() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        plan.updateProgress(Progress.zero());

        assertEquals(Progress.zero(), plan.getProgress());
    }

    @Test
    void updateProgressAcceptsCompleteProgress() {
        StudyPlan plan = StudyPlan.create(ID, "Learn Java", PERIOD, 12);

        plan.updateProgress(Progress.complete());

        assertEquals(Progress.complete(), plan.getProgress());
        assertTrue(plan.isCompleted());
    }

    @Test
    void updateProgressStoresIntermediateProgress() {
        StudyPlan plan = StudyPlan.create(ID, "Learn Java", PERIOD, 12);

        plan.updateProgress(Progress.of(55));

        assertEquals(Progress.of(55), plan.getProgress());
    }

    @Test
    void updateProgressRejectsNullProgressAndKeepsFieldsUnchanged() {
        StudyPlan plan = new StudyPlan(ID, "Learn Java", PERIOD, 12, Progress.of(40));

        assertThrows(NullPointerException.class, () -> plan.updateProgress(null));

        assertEquals(Progress.of(40), plan.getProgress());
    }

    @Test
    void progressValueObjectRejectsOutOfRangeProgress() {
        assertThrows(IllegalArgumentException.class, () -> Progress.of(-1));
        assertThrows(IllegalArgumentException.class, () -> Progress.of(101));
    }

    private static DateRange newPeriod() {
        return new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    }

    private static void assertStudyPlanState(
            StudyPlan plan,
            EntityId id,
            String goalName,
            DateRange period,
            int expectedHours,
            Progress progress) {
        assertAll(
                () -> assertEquals(id, plan.getId()),
                () -> assertEquals(goalName, plan.getGoalName()),
                () -> assertEquals(period, plan.getPeriod()),
                () -> assertEquals(period.startDate(), plan.getStartDate()),
                () -> assertEquals(period.endDate(), plan.getEndDate()),
                () -> assertEquals(expectedHours, plan.getExpectedHours()),
                () -> assertEquals(progress, plan.getProgress()));
    }
}
