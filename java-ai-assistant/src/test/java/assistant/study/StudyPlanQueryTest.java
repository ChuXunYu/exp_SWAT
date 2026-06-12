package assistant.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StudyPlanQueryTest {
    private static final LocalDate JUNE_10 = LocalDate.of(2026, 6, 10);
    private static final LocalDate JUNE_14 = LocalDate.of(2026, 6, 14);
    private static final LocalDate JUNE_15 = LocalDate.of(2026, 6, 15);
    private static final DateRange PERIOD =
            new DateRange(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14));

    @Test
    void allCreatesQueryWithoutFilters() {
        StudyPlanQuery query = StudyPlanQuery.all();

        assertEquals(new StudyPlanQuery(null, null), query);
        assertFalse(query.hasStatusFilter());
        assertFalse(query.hasPeriodFilter());
    }

    @Test
    void byStatusCreatesStatusOnlyFilter() {
        StudyPlanQuery query = StudyPlanQuery.byStatus(StudyPlanStatus.COMPLETED);

        assertEquals(StudyPlanStatus.COMPLETED, query.status());
        assertTrue(query.hasStatusFilter());
        assertFalse(query.hasPeriodFilter());
    }

    @Test
    void byPeriodCreatesPeriodOnlyFilter() {
        StudyPlanQuery query = StudyPlanQuery.byPeriod(PERIOD);

        assertEquals(PERIOD, query.period());
        assertFalse(query.hasStatusFilter());
        assertTrue(query.hasPeriodFilter());
    }

    @Test
    void ofAllowsCombinedFilters() {
        StudyPlanQuery query = StudyPlanQuery.of(StudyPlanStatus.IN_PROGRESS, PERIOD);

        assertEquals(StudyPlanStatus.IN_PROGRESS, query.status());
        assertEquals(PERIOD, query.period());
        assertTrue(query.hasStatusFilter());
        assertTrue(query.hasPeriodFilter());
    }

    @Test
    void matchesReturnsTrueWhenNoFilterIsConfigured() {
        StudyPlan plan = plan(1, PERIOD, 50);
        StudyPlanAnalysisService analysisService = new StudyPlanAnalysisService();

        assertTrue(StudyPlanQuery.all().matches(plan, analysisService, JUNE_10));
    }

    @Test
    void matchesFiltersByDynamicStatusUsingProvidedDate() {
        StudyPlan plan = plan(1, PERIOD, 50);
        StudyPlanQuery query = StudyPlanQuery.byStatus(StudyPlanStatus.IN_PROGRESS);
        StudyPlanAnalysisService analysisService = new StudyPlanAnalysisService();

        assertTrue(query.matches(plan, analysisService, JUNE_14));
        assertFalse(query.matches(plan, analysisService, JUNE_15));
    }

    @Test
    void matchesDelegatesStatusAnalysisToAnalysisService() {
        StudyPlan plan = plan(1, PERIOD, 50);
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(plan, JUNE_10)).thenReturn(StudyPlanStatus.COMPLETED);

        boolean matched = StudyPlanQuery.byStatus(StudyPlanStatus.COMPLETED).matches(plan, analysisService, JUNE_10);

        assertTrue(matched);
        verify(analysisService).analyzeStatus(plan, JUNE_10);
    }

    @Test
    void matchesFiltersByOverlappingPeriod() {
        StudyPlan plan = plan(1, PERIOD, 50);

        assertTrue(StudyPlanQuery.byPeriod(new DateRange(LocalDate.of(2026, 6, 7), LocalDate.of(2026, 6, 8)))
                .matches(plan, new StudyPlanAnalysisService(), JUNE_10));
        assertTrue(StudyPlanQuery.byPeriod(new DateRange(LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 16)))
                .matches(plan, new StudyPlanAnalysisService(), JUNE_10));
        assertFalse(StudyPlanQuery.byPeriod(new DateRange(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 16)))
                .matches(plan, new StudyPlanAnalysisService(), JUNE_10));
    }

    @Test
    void matchesRequiresBothFiltersWhenCombined() {
        StudyPlan plan = plan(1, PERIOD, 50);
        StudyPlanQuery query = StudyPlanQuery.of(
                StudyPlanStatus.IN_PROGRESS,
                new DateRange(LocalDate.of(2026, 6, 9), LocalDate.of(2026, 6, 11)));

        assertTrue(query.matches(plan, new StudyPlanAnalysisService(), JUNE_10));
        assertFalse(query.matches(plan, new StudyPlanAnalysisService(), JUNE_15));
    }

    @Test
    void factoryMethodsRejectNullRequiredArguments() {
        assertThrows(NullPointerException.class, () -> StudyPlanQuery.byStatus(null));
        assertThrows(NullPointerException.class, () -> StudyPlanQuery.byPeriod(null));
    }

    @Test
    void matchesRejectsNullArguments() {
        StudyPlan plan = plan(1, PERIOD, 50);
        StudyPlanAnalysisService analysisService = new StudyPlanAnalysisService();

        assertThrows(NullPointerException.class, () -> StudyPlanQuery.all().matches(null, analysisService, JUNE_10));
        assertThrows(NullPointerException.class, () -> StudyPlanQuery.all().matches(plan, null, JUNE_10));
        assertThrows(NullPointerException.class, () -> StudyPlanQuery.all().matches(plan, analysisService, null));
    }

    private static StudyPlan plan(long id, DateRange period, int progress) {
        return new StudyPlan(new EntityId(id), "Learn Java", period, 12, Progress.of(progress));
    }
}
