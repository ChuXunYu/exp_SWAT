package assistant.study;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.DateRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.testability.FixedTimeProvider;
import assistant.testability.IncrementalIdGenerator;
import assistant.testability.TimeProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudyPlanServiceTest {
    private static final LocalDate JUNE_11 = LocalDate.of(2026, 6, 11);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 11, 9, 30);

    @Test
    void constructorRejectsNullDependencies() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        IncrementalIdGenerator idGenerator = new IncrementalIdGenerator(100);
        FixedTimeProvider timeProvider = new FixedTimeProvider(NOW);
        StudyPlanAnalysisService analysisService = new StudyPlanAnalysisService();

        assertThrows(NullPointerException.class, () -> new StudyPlanService(null, idGenerator, timeProvider, analysisService));
        assertThrows(NullPointerException.class, () -> new StudyPlanService(repository, null, timeProvider, analysisService));
        assertThrows(NullPointerException.class, () -> new StudyPlanService(repository, idGenerator, null, analysisService));
        assertThrows(NullPointerException.class, () -> new StudyPlanService(repository, idGenerator, timeProvider, null));
    }

    @Test
    void createStudyPlanWithoutInitialProgressDefaultsToZeroAndReturnsView() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2035, 1, 20);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(currentDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            invocations.add(new AnalysisInvocation(plan.getId(), invocation.getArgument(1)));
            return StudyPlanStatus.COMPLETED;
        });
        StudyPlanService service = newService(repository, timeProvider, analysisService);

        OperationResult<StudyPlanView> result =
                service.createStudyPlan("Learn Java", LocalDate.of(2035, 1, 10), LocalDate.of(2035, 1, 15), 12);

        assertSuccess(result);
        StudyPlanView view = result.getPayload();
        assertAll(
                () -> assertInstanceOf(StudyPlanView.class, view),
                () -> assertEquals(new EntityId(100), view.id()),
                () -> assertEquals(0, view.progress().value()),
                () -> assertEquals(StudyPlanStatus.COMPLETED, view.status()),
                () -> assertEquals(1, repository.findAll().size()));
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(List.of(new AnalysisInvocation(new EntityId(100), currentDate)), List.copyOf(invocations));
    }

    @Test
    void createStudyPlanAcceptsExplicitZeroInitialProgress() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result =
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12, 0);

        assertSuccess(result);
        assertEquals(0, result.getPayload().progress().value());
    }

    @Test
    void createStudyPlanAcceptsExplicitCompleteInitialProgress() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2026, 6, 11);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(currentDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            invocations.add(new AnalysisInvocation(plan.getId(), invocation.getArgument(1)));
            return StudyPlanStatus.IN_PROGRESS;
        });
        StudyPlanService service = newService(repository, timeProvider, analysisService);

        OperationResult<StudyPlanView> result =
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12, 100);

        assertSuccess(result);
        assertEquals(100, result.getPayload().progress().value());
        assertEquals(StudyPlanStatus.IN_PROGRESS, result.getPayload().status());
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(List.of(new AnalysisInvocation(new EntityId(100), currentDate)), List.copyOf(invocations));
    }

    @Test
    void createStudyPlanRejectsNegativeInitialProgressAndKeepsRepositoryUnchanged() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlanService service = newService(repository, new FixedTimeProvider(NOW));

        OperationResult<StudyPlanView> result =
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12, -1);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "value must not be less than 0");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createStudyPlanRejectsProgressGreaterThanHundredAndKeepsRepositoryUnchanged() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlanService service = newService(repository, new FixedTimeProvider(NOW));

        OperationResult<StudyPlanView> result =
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12, 101);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "value must not be greater than 100");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createStudyPlanRejectsInvalidDateRangeAndKeepsRepositoryUnchanged() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlanService service = newService(repository, new FixedTimeProvider(NOW));

        OperationResult<StudyPlanView> result =
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 8), 12);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "startDate must not be after endDate");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void createStudyPlanRejectsNullDatesBlankGoalAndNonPositiveExpectedHours() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlanService service = newService(repository, new FixedTimeProvider(NOW));

        assertFailure(
                service.createStudyPlan(" \t\n", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12),
                ErrorCode.VALIDATION_ERROR,
                "goalName must not be blank");
        assertFailure(
                service.createStudyPlan("Learn Java", null, LocalDate.of(2026, 6, 14), 12),
                ErrorCode.VALIDATION_ERROR,
                "startDate");
        assertFailure(
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), null, 12),
                ErrorCode.VALIDATION_ERROR,
                "endDate");
        assertFailure(
                service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 0),
                ErrorCode.VALIDATION_ERROR,
                "expectedHours must be positive");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void getStudyPlanReturnsViewForExistingPlan() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(LocalDate.of(2040, 2, 1)));
        StudyPlanService service = newService(repository, timeProvider);
        repository.save(plan(100, period(2040, 2, 10, 2040, 2, 20), 0));

        OperationResult<StudyPlanView> result = service.getStudyPlan(new EntityId(100));

        assertSuccess(result);
        assertAll(
                () -> assertEquals("Learn 100", result.getPayload().goalName()),
                () -> assertEquals(StudyPlanStatus.NOT_STARTED, result.getPayload().status()));
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
    }

    @Test
    void getStudyPlanDelegatesViewProjectionToInjectedAnalysisServiceWithCurrentDate() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2040, 2, 1);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(currentDate));
        StudyPlan storedPlan = plan(100, period(2040, 2, 10, 2040, 2, 20), 0);
        repository.save(storedPlan);
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate)))
                .thenReturn(StudyPlanStatus.COMPLETED);
        StudyPlanService service = new StudyPlanService(
                repository,
                new IncrementalIdGenerator(100),
                timeProvider,
                analysisService);

        OperationResult<StudyPlanView> result = service.getStudyPlan(new EntityId(100));

        assertSuccess(result);
        assertEquals(StudyPlanStatus.COMPLETED, result.getPayload().status());
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        verify(analysisService).analyzeStatus(any(StudyPlan.class), eq(currentDate));
    }

    @Test
    void getStudyPlanReturnsNotFoundForMissingPlan() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result = service.getStudyPlan(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND, "study plan not found: 999");
    }

    @Test
    void getStudyPlanRejectsNullId() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result = service.getStudyPlan(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "id must not be null");
    }

    @Test
    void listStudyPlansReturnsUnmodifiableViewsInInsertionOrder() {
        StudyPlanService service = newService();
        service.createStudyPlan("First", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12);
        service.createStudyPlan("Second", LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 20), 16);

        OperationResult<List<StudyPlanView>> result = service.listStudyPlans();

        assertSuccess(result);
        List<StudyPlanView> views = result.getPayload();
        assertEquals(List.of(new EntityId(100), new EntityId(101)), idsOf(views));
        assertThrows(UnsupportedOperationException.class, () -> views.clear());
    }

    @Test
    void listStudyPlansComputesStatusesWithInjectedCurrentDate() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(JUNE_11));
        StudyPlan firstPlan = plan(100, period(2026, 6, 1, 2026, 6, 10), 20);
        StudyPlan secondPlan = plan(101, period(2026, 6, 8, 2026, 6, 14), 20);
        StudyPlan thirdPlan = plan(102, period(2026, 6, 20, 2026, 6, 25), 0);
        repository.save(firstPlan);
        repository.save(secondPlan);
        repository.save(thirdPlan);
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(JUNE_11))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            LocalDate currentDate = invocation.getArgument(1);
            invocations.add(new AnalysisInvocation(plan.getId(), currentDate));
            if (plan.getId().equals(firstPlan.getId())) {
                return StudyPlanStatus.COMPLETED;
            }
            if (plan.getId().equals(secondPlan.getId())) {
                return StudyPlanStatus.NOT_STARTED;
            }
            return StudyPlanStatus.OVERDUE_INCOMPLETE;
        });
        StudyPlanService service = new StudyPlanService(
                repository,
                new IncrementalIdGenerator(100),
                timeProvider,
                analysisService);

        OperationResult<List<StudyPlanView>> result = service.listStudyPlans();

        assertSuccess(result);
        assertEquals(
                List.of(
                        StudyPlanStatus.COMPLETED,
                        StudyPlanStatus.NOT_STARTED,
                        StudyPlanStatus.OVERDUE_INCOMPLETE),
                statusesOf(result.getPayload()));
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(
                List.of(
                        new AnalysisInvocation(firstPlan.getId(), JUNE_11),
                        new AnalysisInvocation(secondPlan.getId(), JUNE_11),
                        new AnalysisInvocation(thirdPlan.getId(), JUNE_11)),
                List.copyOf(invocations));
        assertEquals(0, timeProvider.nowCalls());
    }

    @Test
    void listStudyPlansWithQueryFiltersByStatusPeriodAndCombination() {
        StudyPlanService service = serviceWithMixedPlans(new FixedTimeProvider(NOW));

        OperationResult<List<StudyPlanView>> byStatus =
                service.listStudyPlans(StudyPlanQuery.byStatus(StudyPlanStatus.IN_PROGRESS));
        OperationResult<List<StudyPlanView>> byPeriod =
                service.listStudyPlans(StudyPlanQuery.byPeriod(period(2026, 6, 12, 2026, 6, 16)));
        OperationResult<List<StudyPlanView>> combined =
                service.listStudyPlans(StudyPlanQuery.of(
                        StudyPlanStatus.IN_PROGRESS,
                        period(2026, 6, 12, 2026, 6, 16)));

        assertSuccess(byStatus);
        assertSuccess(byPeriod);
        assertSuccess(combined);
        assertEquals(List.of(new EntityId(100)), idsOf(byStatus.getPayload()));
        assertEquals(List.of(new EntityId(100), new EntityId(101)), idsOf(byPeriod.getPayload()));
        assertEquals(List.of(new EntityId(100)), idsOf(combined.getPayload()));
        assertThrows(UnsupportedOperationException.class, () -> combined.getPayload().clear());
    }

    @Test
    void listStudyPlansWithQueryUsesOneCurrentDateForFilteringAndProjection() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2026, 6, 14);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(
                currentDate,
                LocalDate.of(2026, 6, 15)));
        StudyPlan firstPlan = plan(100, period(2026, 6, 8, 2026, 6, 14), 20);
        StudyPlan secondPlan = plan(101, period(2026, 6, 8, 2026, 6, 14), 20);
        repository.save(firstPlan);
        repository.save(secondPlan);
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        java.util.Map<EntityId, Integer> countsByPlan = new java.util.LinkedHashMap<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            LocalDate invocationDate = invocation.getArgument(1);
            invocations.add(new AnalysisInvocation(plan.getId(), invocationDate));
            int callIndex = countsByPlan.merge(plan.getId(), 1, Integer::sum);
            if (plan.getId().equals(firstPlan.getId())) {
                return callIndex == 1 ? StudyPlanStatus.IN_PROGRESS : StudyPlanStatus.COMPLETED;
            }
            return StudyPlanStatus.NOT_STARTED;
        });
        StudyPlanService service = newService(repository, timeProvider, analysisService);

        OperationResult<List<StudyPlanView>> result =
                service.listStudyPlans(StudyPlanQuery.byStatus(StudyPlanStatus.IN_PROGRESS));

        assertSuccess(result);
        assertEquals(List.of(firstPlan.getId()), idsOf(result.getPayload()));
        assertEquals(List.of(StudyPlanStatus.COMPLETED), statusesOf(result.getPayload()));
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(
                List.of(
                        new AnalysisInvocation(firstPlan.getId(), currentDate),
                        new AnalysisInvocation(secondPlan.getId(), currentDate),
                        new AnalysisInvocation(firstPlan.getId(), currentDate)),
                List.copyOf(invocations));
    }

    @Test
    void listStudyPlansWithQueryRejectsNullQuery() {
        StudyPlanService service = newService();

        OperationResult<List<StudyPlanView>> result = service.listStudyPlans(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "query must not be null");
    }

    @Test
    void queryRepositoryAndServiceReturnConsistentResultsForSameCurrentDate() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlanAnalysisService analysisService = new StudyPlanAnalysisService();
        LocalDate currentDate = JUNE_11;
        repository.save(plan(100, period(2026, 6, 8, 2026, 6, 14), 20));
        repository.save(plan(101, period(2026, 6, 12, 2026, 6, 20), 20));
        repository.save(plan(102, period(2026, 6, 1, 2026, 6, 10), 20));
        StudyPlanService service = new StudyPlanService(
                repository,
                new IncrementalIdGenerator(200),
                new CountingTimeProvider(List.of(currentDate)),
                analysisService);
        StudyPlanQuery query = StudyPlanQuery.of(
                StudyPlanStatus.IN_PROGRESS,
                period(2026, 6, 8, 2026, 6, 16));

        List<EntityId> matchedByQuery = repository.findAll().stream()
                .filter(plan -> query.matches(plan, analysisService, currentDate))
                .map(StudyPlan::getId)
                .toList();
        List<EntityId> matchedByRepository = repository.findBy(query, analysisService, currentDate).stream()
                .map(StudyPlan::getId)
                .toList();
        List<EntityId> matchedByService = service.listStudyPlans(query).getPayload().stream()
                .map(StudyPlanView::id)
                .toList();

        assertEquals(matchedByQuery, matchedByRepository);
        assertEquals(matchedByRepository, matchedByService);
    }

    @Test
    void updateStudyPlanDetailsPersistsChangedFieldsAndReturnsView() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2031, 4, 15);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(currentDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            invocations.add(new AnalysisInvocation(plan.getId(), invocation.getArgument(1)));
            return StudyPlanStatus.NOT_STARTED;
        });
        StudyPlanService service = newService(repository, timeProvider, analysisService);
        repository.save(plan(100, period(2031, 3, 1, 2031, 3, 20), 20));

        OperationResult<StudyPlanView> result = service.updateStudyPlanDetails(
                new EntityId(100),
                "Practice algorithms",
                LocalDate.of(2031, 4, 10),
                LocalDate.of(2031, 4, 30),
                20);

        assertSuccess(result);
        assertAll(
                () -> assertEquals("Practice algorithms", result.getPayload().goalName()),
                () -> assertEquals(LocalDate.of(2031, 4, 10), result.getPayload().startDate()),
                () -> assertEquals(LocalDate.of(2031, 4, 30), result.getPayload().endDate()),
                () -> assertEquals(20, result.getPayload().expectedHours()),
                () -> assertEquals(StudyPlanStatus.NOT_STARTED, result.getPayload().status()));
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(List.of(new AnalysisInvocation(new EntityId(100), currentDate)), List.copyOf(invocations));
        assertEquals("Practice algorithms", repository.findById(new EntityId(100)).orElseThrow().getGoalName());
    }

    @Test
    void updateStudyPlanDetailsRejectsInvalidDateRangeAndKeepsRepositoryState() {
        StudyPlanService service = newService();
        service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12);
        StudyPlanView before = service.getStudyPlan(new EntityId(100)).getPayload();

        OperationResult<StudyPlanView> result = service.updateStudyPlanDetails(
                new EntityId(100),
                "Practice algorithms",
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 7, 1),
                20);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "startDate must not be after endDate");
        assertSameView(before, service.getStudyPlan(new EntityId(100)).getPayload());
    }

    @Test
    void updateStudyPlanDetailsRejectsInvalidFieldsAndKeepsRepositoryState() {
        StudyPlanService service = newService();
        service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12);
        StudyPlanView before = service.getStudyPlan(new EntityId(100)).getPayload();

        assertFailure(
                service.updateStudyPlanDetails(
                        new EntityId(100), " \t\n", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 20),
                ErrorCode.VALIDATION_ERROR,
                "goalName must not be blank");
        assertFailure(
                service.updateStudyPlanDetails(
                        new EntityId(100), "Practice algorithms", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 0),
                ErrorCode.VALIDATION_ERROR,
                "expectedHours must be positive");

        assertSameView(before, service.getStudyPlan(new EntityId(100)).getPayload());
    }

    @Test
    void updateStudyPlanDetailsReturnsNotFoundForMissingPlan() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result = service.updateStudyPlanDetails(
                new EntityId(999),
                "Practice algorithms",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                20);

        assertFailure(result, ErrorCode.NOT_FOUND, "study plan not found: 999");
    }

    @Test
    void updateStudyPlanDetailsRejectsNullId() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result = service.updateStudyPlanDetails(
                null,
                "Practice algorithms",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                20);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "id must not be null");
    }

    @Test
    void updateStudyPlanDetailsRejectsNullDatesAndKeepsRepositoryState() {
        StudyPlanService service = newService();
        service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12);
        StudyPlanView before = service.getStudyPlan(new EntityId(100)).getPayload();

        assertFailure(
                service.updateStudyPlanDetails(
                        new EntityId(100),
                        "Practice algorithms",
                        null,
                        LocalDate.of(2026, 7, 31),
                        20),
                ErrorCode.VALIDATION_ERROR,
                "startDate");
        assertFailure(
                service.updateStudyPlanDetails(
                        new EntityId(100),
                        "Practice algorithms",
                        LocalDate.of(2026, 7, 1),
                        null,
                        20),
                ErrorCode.VALIDATION_ERROR,
                "endDate");

        assertSameView(before, service.getStudyPlan(new EntityId(100)).getPayload());
    }

    @Test
    void updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2050, 7, 25);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(currentDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            invocations.add(new AnalysisInvocation(plan.getId(), invocation.getArgument(1)));
            return StudyPlanStatus.NOT_STARTED;
        });
        StudyPlanService service = newService(repository, timeProvider, analysisService);
        repository.save(plan(100, period(2050, 7, 10, 2050, 7, 20), 50));

        OperationResult<StudyPlanView> result = service.updateStudyPlanProgress(new EntityId(100), 0);

        assertSuccess(result);
        assertEquals(0, result.getPayload().progress().value());
        assertEquals(StudyPlanStatus.NOT_STARTED, result.getPayload().status());
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(List.of(new AnalysisInvocation(new EntityId(100), currentDate)), List.copyOf(invocations));
        assertEquals(0, repository.findById(new EntityId(100)).orElseThrow().getProgress().value());
    }

    @Test
    void updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        LocalDate currentDate = LocalDate.of(2051, 8, 5);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(currentDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), eq(currentDate))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            invocations.add(new AnalysisInvocation(plan.getId(), invocation.getArgument(1)));
            return StudyPlanStatus.OVERDUE_INCOMPLETE;
        });
        StudyPlanService service = newService(repository, timeProvider, analysisService);
        repository.save(plan(100, period(2051, 8, 10, 2051, 8, 20), 50));

        OperationResult<StudyPlanView> result = service.updateStudyPlanProgress(new EntityId(100), 100);

        assertSuccess(result);
        assertEquals(100, result.getPayload().progress().value());
        assertEquals(StudyPlanStatus.OVERDUE_INCOMPLETE, result.getPayload().status());
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(List.of(new AnalysisInvocation(new EntityId(100), currentDate)), List.copyOf(invocations));
        assertEquals(100, repository.findById(new EntityId(100)).orElseThrow().getProgress().value());
    }

    @Test
    void updateStudyPlanProgressRejectsNegativeProgressAndKeepsRepositoryState() {
        StudyPlanService service = newService();
        service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12, 50);
        StudyPlanView before = service.getStudyPlan(new EntityId(100)).getPayload();

        OperationResult<StudyPlanView> result = service.updateStudyPlanProgress(new EntityId(100), -1);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "value must not be less than 0");
        assertSameView(before, service.getStudyPlan(new EntityId(100)).getPayload());
    }

    @Test
    void updateStudyPlanProgressRejectsProgressGreaterThanHundredAndKeepsRepositoryState() {
        StudyPlanService service = newService();
        service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12, 50);
        StudyPlanView before = service.getStudyPlan(new EntityId(100)).getPayload();

        OperationResult<StudyPlanView> result = service.updateStudyPlanProgress(new EntityId(100), 101);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "value must not be greater than 100");
        assertSameView(before, service.getStudyPlan(new EntityId(100)).getPayload());
    }

    @Test
    void updateStudyPlanProgressReturnsNotFoundForMissingPlan() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result = service.updateStudyPlanProgress(new EntityId(999), 50);

        assertFailure(result, ErrorCode.NOT_FOUND, "study plan not found: 999");
    }

    @Test
    void updateStudyPlanProgressRejectsNullId() {
        StudyPlanService service = newService();

        OperationResult<StudyPlanView> result = service.updateStudyPlanProgress(null, 50);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "id must not be null");
    }

    @Test
    void deleteStudyPlanRemovesExistingPlan() {
        StudyPlanService service = newService();
        service.createStudyPlan("Learn Java", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14), 12);

        OperationResult<Void> result = service.deleteStudyPlan(new EntityId(100));

        assertSuccess(result);
        assertFailure(service.getStudyPlan(new EntityId(100)), ErrorCode.NOT_FOUND, "study plan not found: 100");
    }

    @Test
    void deleteStudyPlanReturnsNotFoundForMissingPlan() {
        StudyPlanService service = newService();

        OperationResult<Void> result = service.deleteStudyPlan(new EntityId(999));

        assertFailure(result, ErrorCode.NOT_FOUND, "study plan not found: 999");
    }

    @Test
    void deleteStudyPlanRejectsNullId() {
        StudyPlanService service = newService();

        OperationResult<Void> result = service.deleteStudyPlan(null);

        assertFailure(result, ErrorCode.VALIDATION_ERROR, "id must not be null");
    }

    @Test
    void countCompletedPlansCountsOnlyCompletedStatusAtCurrentDate() {
        StudyPlanService service = serviceWithStatsPlans(new FixedTimeProvider(NOW));

        OperationResult<Integer> result = service.countCompletedPlans();

        assertSuccess(result);
        assertEquals(1, result.getPayload());
    }

    @Test
    void countIncompletePlansCountsAllNonCompletedStatusesAtCurrentDate() {
        StudyPlanService service = serviceWithStatsPlans(new FixedTimeProvider(NOW));

        OperationResult<Integer> result = service.countIncompletePlans();

        assertSuccess(result);
        assertEquals(3, result.getPayload());
    }

    @Test
    void countCompletedPlansReadsTodayOnceAndPassesThatDateToAnalysisService() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlan completedPlan = plan(100, period(2034, 9, 1, 2034, 9, 10), 100);
        StudyPlan incompletePlan = plan(101, period(2034, 9, 1, 2034, 9, 10), 20);
        repository.save(completedPlan);
        repository.save(incompletePlan);
        LocalDate injectedDate = LocalDate.of(2034, 9, 30);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(injectedDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), any(LocalDate.class))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            LocalDate currentDate = invocation.getArgument(1);
            invocations.add(new AnalysisInvocation(plan.getId(), currentDate));
            return analyzeStatus(plan, currentDate);
        });
        StudyPlanService service = new StudyPlanService(
                repository,
                new IncrementalIdGenerator(100),
                timeProvider,
                analysisService);

        OperationResult<Integer> completedResult = service.countCompletedPlans();

        assertSuccess(completedResult);
        assertEquals(1, completedResult.getPayload());
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(
                List.of(
                        new AnalysisInvocation(completedPlan.getId(), injectedDate),
                        new AnalysisInvocation(incompletePlan.getId(), injectedDate)),
                List.copyOf(invocations));
    }

    @Test
    void countIncompletePlansReadsTodayOnceAndPassesThatDateToAnalysisService() {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        StudyPlan completedPlan = plan(100, period(2034, 9, 1, 2034, 9, 10), 100);
        StudyPlan incompletePlan = plan(101, period(2034, 9, 1, 2034, 9, 10), 20);
        repository.save(completedPlan);
        repository.save(incompletePlan);
        LocalDate injectedDate = LocalDate.of(2034, 9, 30);
        CountingTimeProvider timeProvider = new CountingTimeProvider(List.of(injectedDate));
        java.util.ArrayList<AnalysisInvocation> invocations = new java.util.ArrayList<>();
        StudyPlanAnalysisService analysisService = mock(StudyPlanAnalysisService.class);
        when(analysisService.analyzeStatus(any(StudyPlan.class), any(LocalDate.class))).thenAnswer(invocation -> {
            StudyPlan plan = invocation.getArgument(0);
            LocalDate currentDate = invocation.getArgument(1);
            invocations.add(new AnalysisInvocation(plan.getId(), currentDate));
            return analyzeStatus(plan, currentDate);
        });
        StudyPlanService service = new StudyPlanService(
                repository,
                new IncrementalIdGenerator(100),
                timeProvider,
                analysisService);

        OperationResult<Integer> incompleteResult = service.countIncompletePlans();

        assertSuccess(incompleteResult);
        assertEquals(1, incompleteResult.getPayload());
        assertEquals(1, timeProvider.todayCalls());
        assertEquals(0, timeProvider.nowCalls());
        assertEquals(
                List.of(
                        new AnalysisInvocation(completedPlan.getId(), injectedDate),
                        new AnalysisInvocation(incompletePlan.getId(), injectedDate)),
                List.copyOf(invocations));
    }

    private static StudyPlanService newService() {
        return newService(new InMemoryStudyPlanRepository(), new FixedTimeProvider(NOW));
    }

    private static StudyPlanService newService(InMemoryStudyPlanRepository repository, TimeProvider timeProvider) {
        return newService(repository, timeProvider, new StudyPlanAnalysisService());
    }

    private static StudyPlanService newService(
            InMemoryStudyPlanRepository repository,
            TimeProvider timeProvider,
            StudyPlanAnalysisService analysisService) {
        return new StudyPlanService(
                repository,
                new IncrementalIdGenerator(100),
                timeProvider,
                analysisService);
    }

    private static StudyPlanService serviceWithMixedPlans(TimeProvider timeProvider) {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        repository.save(plan(100, period(2026, 6, 8, 2026, 6, 14), 20));
        repository.save(plan(101, period(2026, 6, 12, 2026, 6, 20), 60));
        repository.save(plan(102, period(2026, 7, 1, 2026, 7, 10), 0));
        return newService(repository, timeProvider);
    }

    private static StudyPlanService serviceWithStatsPlans(TimeProvider timeProvider) {
        InMemoryStudyPlanRepository repository = new InMemoryStudyPlanRepository();
        repository.save(plan(100, period(2026, 6, 8, 2026, 6, 14), 100));
        repository.save(plan(101, period(2026, 6, 8, 2026, 6, 14), 20));
        repository.save(plan(102, period(2026, 6, 1, 2026, 6, 10), 20));
        repository.save(plan(103, period(2026, 6, 20, 2026, 6, 25), 0));
        return newService(repository, timeProvider);
    }

    private static StudyPlan plan(long id, DateRange period, int progress) {
        return new StudyPlan(new EntityId(id), "Learn " + id, period, 12, assistant.common.Progress.of(progress));
    }

    private static DateRange period(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        return new DateRange(
                LocalDate.of(startYear, startMonth, startDay),
                LocalDate.of(endYear, endMonth, endDay));
    }

    private static List<EntityId> idsOf(List<StudyPlanView> views) {
        return views.stream().map(StudyPlanView::id).toList();
    }

    private static List<StudyPlanStatus> statusesOf(List<StudyPlanView> views) {
        return views.stream().map(StudyPlanView::status).toList();
    }

    private static StudyPlanStatus analyzeStatus(StudyPlan plan, LocalDate currentDate) {
        if (plan.getProgress().isComplete()) {
            return StudyPlanStatus.COMPLETED;
        }
        if (currentDate.isAfter(plan.getEndDate())) {
            return StudyPlanStatus.OVERDUE_INCOMPLETE;
        }
        if (currentDate.isBefore(plan.getStartDate())) {
            return StudyPlanStatus.NOT_STARTED;
        }
        return StudyPlanStatus.IN_PROGRESS;
    }

    private static <T> void assertSuccess(OperationResult<T> result) {
        assertTrue(result.isSuccess());
    }

    private static void assertFailure(OperationResult<?> result, ErrorCode errorCode) {
        assertTrue(result.isFailure());
        assertEquals(errorCode, result.getErrorCode());
    }

    private static void assertFailure(OperationResult<?> result, ErrorCode errorCode, String message) {
        assertFailure(result, errorCode);
        assertEquals(message, result.getMessage());
    }

    private static void assertSameView(StudyPlanView expected, StudyPlanView actual) {
        assertAll(
                () -> assertEquals(expected.id(), actual.id()),
                () -> assertEquals(expected.goalName(), actual.goalName()),
                () -> assertEquals(expected.period(), actual.period()),
                () -> assertEquals(expected.startDate(), actual.startDate()),
                () -> assertEquals(expected.endDate(), actual.endDate()),
                () -> assertEquals(expected.expectedHours(), actual.expectedHours()),
                () -> assertEquals(expected.progress(), actual.progress()),
                () -> assertEquals(expected.status(), actual.status()));
    }

    private static final class CountingTimeProvider implements TimeProvider {
        private final List<LocalDate> dates;
        private int todayCalls;
        private int nowCalls;

        private CountingTimeProvider(List<LocalDate> dates) {
            this.dates = dates;
        }

        @Override
        public LocalDate today() {
            LocalDate date = dates.get(Math.min(todayCalls, dates.size() - 1));
            todayCalls++;
            return date;
        }

        @Override
        public LocalDateTime now() {
            nowCalls++;
            return LocalDateTime.of(2099, 12, 31, 23, 59);
        }

        private int todayCalls() {
            return todayCalls;
        }

        private int nowCalls() {
            return nowCalls;
        }
    }

    private record AnalysisInvocation(EntityId planId, LocalDate currentDate) {}
}
