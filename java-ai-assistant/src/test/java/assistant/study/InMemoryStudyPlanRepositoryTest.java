package assistant.study;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryStudyPlanRepositoryTest {
    private static final LocalDate JUNE_10 = LocalDate.of(2026, 6, 10);
    private static final LocalDate JUNE_15 = LocalDate.of(2026, 6, 15);

    @Test
    void saveAndFindByIdReturnsDetachedSnapshot() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlan plan = plan(1, period(2026, 6, 8, 2026, 6, 14), 20);

        repository.save(plan);

        StudyPlan stored = repository.findById(new EntityId(1)).orElseThrow();
        assertNotSame(plan, stored);
        assertEquals(plan.getGoalName(), stored.getGoalName());
    }

    @Test
    void saveCopiesInputPlanSoLaterCallerMutationsDoNotAffectRepository() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlan plan = plan(1, period(2026, 6, 8, 2026, 6, 14), 20);
        repository.save(plan);

        plan.updateDetails("Mutated", period(2026, 7, 1, 2026, 7, 31), 40);
        plan.updateProgress(Progress.complete());

        StudyPlan stored = repository.findById(new EntityId(1)).orElseThrow();
        assertEquals("Learn 1", stored.getGoalName());
        assertEquals(period(2026, 6, 8, 2026, 6, 14), stored.getPeriod());
        assertEquals(20, stored.getProgress().value());
    }

    @Test
    void findByIdReturnsEmptyWhenPlanDoesNotExist() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();

        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void saveReplacesPlanWithSameIdAndKeepsInsertionOrder() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        repository.save(plan(1, period(2026, 6, 8, 2026, 6, 14), 20));
        repository.save(plan(2, period(2026, 6, 10, 2026, 6, 12), 30));
        repository.save(new StudyPlan(new EntityId(1), "Updated", period(2026, 7, 1, 2026, 7, 31), 12, Progress.zero()));

        List<StudyPlan> all = repository.findAll();
        assertEquals(List.of(new EntityId(1), new EntityId(2)), idsOf(all));
        assertEquals("Updated", all.get(0).getGoalName());
    }

    @Test
    void findAllReturnsPlansInInsertionOrder() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        repository.save(plan(1, period(2026, 6, 8, 2026, 6, 14), 20));
        repository.save(plan(2, period(2026, 6, 10, 2026, 6, 12), 30));

        assertEquals(List.of(new EntityId(1), new EntityId(2)), idsOf(repository.findAll()));
    }

    @Test
    void findAllReturnsUnmodifiableDetachedSnapshotList() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        repository.save(plan(1, period(2026, 6, 8, 2026, 6, 14), 20));

        List<StudyPlan> snapshot = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(plan(2, period(2026, 6, 10, 2026, 6, 12), 30)));
        snapshot.get(0).updateDetails("Changed", period(2026, 7, 1, 2026, 7, 31), 40);
        StudyPlan stored = repository.findById(new EntityId(1)).orElseThrow();
        assertEquals("Learn 1", stored.getGoalName());
    }

    @Test
    void findByFiltersByDynamicStatusAtProvidedCurrentDate() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        List<StudyPlan> result = repository.findBy(
                StudyPlanQuery.byStatus(StudyPlanStatus.OVERDUE_INCOMPLETE),
                new StudyPlanAnalysisService(),
                JUNE_15);

        assertEquals(List.of(new EntityId(1)), idsOf(result));
    }

    @Test
    void findByDelegatesDynamicStatusFilteringToInjectedAnalysisServiceWithProvidedCurrentDate() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();
        LocalDate currentDate = JUNE_10;
        ArrayList<AnalysisInvocation> invocations = new ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            invocations.add(new AnalysisInvocation(plan.getId(), invocation.getArgument(1)));
            return plan.getId().equals(new EntityId(2))
                    ? StudyPlanStatus.COMPLETED
                    : StudyPlanStatus.IN_PROGRESS;
        });

        List<StudyPlan> result = repository.findBy(
                StudyPlanQuery.byStatus(StudyPlanStatus.COMPLETED),
                analysisService,
                currentDate);

        assertEquals(List.of(new EntityId(2)), idsOf(result));
        assertEquals(
                List.of(
                        new AnalysisInvocation(new EntityId(1), currentDate),
                        new AnalysisInvocation(new EntityId(2), currentDate),
                        new AnalysisInvocation(new EntityId(3), currentDate)),
                List.copyOf(invocations));
    }

    @Test
    void findByFiltersByOverlappingPeriod() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        List<StudyPlan> result = repository.findBy(
                StudyPlanQuery.byPeriod(period(2026, 6, 12, 2026, 6, 16)),
                new StudyPlanAnalysisService(),
                JUNE_10);

        assertEquals(List.of(new EntityId(1), new EntityId(2)), idsOf(result));
    }

    @Test
    void findByAppliesCombinedQueryInInsertionOrder() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        List<StudyPlan> result = repository.findBy(
                StudyPlanQuery.of(
                        StudyPlanStatus.IN_PROGRESS,
                        period(2026, 6, 12, 2026, 6, 16)),
                new StudyPlanAnalysisService(),
                JUNE_10);

        assertEquals(List.of(new EntityId(1)), idsOf(result));
    }

    @Test
    void findByReturnsUnmodifiableDetachedSnapshotList() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        List<StudyPlan> snapshot = repository.findBy(
                StudyPlanQuery.all(),
                new StudyPlanAnalysisService(),
                JUNE_10);

        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));
        snapshot.get(0).updateProgress(Progress.complete());
        StudyPlan stored = repository.findById(new EntityId(1)).orElseThrow();
        assertEquals(20, stored.getProgress().value());
    }

    @Test
    void mutatingPlanReturnedFromFindByIdDoesNotAffectStoredState() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        StudyPlan returned = repository.findById(new EntityId(1)).orElseThrow();
        returned.updateProgress(Progress.complete());

        assertEquals(20, repository.findById(new EntityId(1)).orElseThrow().getProgress().value());
    }

    @Test
    void mutatingPlanReturnedFromFindAllDoesNotAffectStoredState() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        StudyPlan returned = repository.findAll().get(0);
        returned.updateProgress(Progress.complete());

        assertEquals(20, repository.findById(new EntityId(1)).orElseThrow().getProgress().value());
    }

    @Test
    void mutatingPlanReturnedFromFindByDoesNotAffectStoredState() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        StudyPlan returned = repository.findBy(StudyPlanQuery.all(), new StudyPlanAnalysisService(), JUNE_10).get(0);
        returned.updateProgress(Progress.complete());

        assertEquals(20, repository.findById(new EntityId(1)).orElseThrow().getProgress().value());
    }

    @Test
    void deleteByIdRemovesExistingPlan() {
        InMemoryStudyPlanRepository repository = repositoryWithMixedPlans();

        assertTrue(repository.deleteById(new EntityId(1)));
        assertTrue(repository.findById(new EntityId(1)).isEmpty());
    }

    @Test
    void deleteByIdReturnsFalseWhenPlanDoesNotExist() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();

        assertFalse(repository.deleteById(new EntityId(1)));
    }

    @Test
    void methodsRejectNullArguments() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.findBy(null, new StudyPlanAnalysisService(), JUNE_10));
        assertThrows(NullPointerException.class, () -> repository.findBy(StudyPlanQuery.all(), null, JUNE_10));
        assertThrows(NullPointerException.class, () -> repository.findBy(StudyPlanQuery.all(), new StudyPlanAnalysisService(), null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
    }

    private static InMemoryStudyPlanRepository repositoryWithMixedPlans() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        repository.save(plan(1, period(2026, 6, 8, 2026, 6, 14), 20));
        repository.save(plan(2, period(2026, 6, 12, 2026, 6, 20), 60));
        repository.save(plan(3, period(2026, 7, 1, 2026, 7, 10), 0));
        return repository;
    }

    private static StudyPlan plan(long id, DateRange period, int progress) {
        return new StudyPlan(new EntityId(id), "Learn " + id, period, 12, Progress.of(progress));
    }

    private static DateRange period(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        return new DateRange(
                LocalDate.of(startYear, startMonth, startDay),
                LocalDate.of(endYear, endMonth, endDay));
    }

    private static List<EntityId> idsOf(List<StudyPlan> plans) {
        return plans.stream().map(StudyPlan::getId).toList();
    }

    private record AnalysisInvocation(EntityId planId, LocalDate currentDate) {}
}
