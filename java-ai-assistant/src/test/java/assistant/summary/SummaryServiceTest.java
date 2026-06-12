package assistant.summary;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import assistant.common.DateRange;
import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.common.Progress;
import assistant.common.Tag;
import assistant.common.TransactionAmount;
import assistant.finance.FinanceService;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionQuery;
import assistant.finance.TransactionType;
import assistant.finance.TransactionView;
import assistant.note.NoteService;
import assistant.note.NoteView;
import assistant.schedule.ScheduleService;
import assistant.schedule.ScheduleStatus;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanQuery;
import assistant.study.StudyPlanService;
import assistant.study.StudyPlanStatus;
import assistant.study.StudyPlanView;
import assistant.task.TaskPriority;
import assistant.task.TaskQuery;
import assistant.task.TaskService;
import assistant.task.TaskStatus;
import assistant.task.TaskView;
import assistant.testability.TimeProvider;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class SummaryServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 2, 18);
    private static final LocalDate WEEK_START = LocalDate.of(2026, 2, 16);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 2, 22);
    private static final LocalDate MONTH_START = LocalDate.of(2026, 2, 1);
    private static final LocalDate MONTH_END = LocalDate.of(2026, 2, 28);

    @Test
    void constructorRejectsNullDependencies() {
        Collaborators collaborators = collaborators();

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new SummaryService(
                        null,
                        collaborators.scheduleService(),
                        collaborators.studyPlanService(),
                        collaborators.financeService(),
                        collaborators.noteService(),
                        collaborators.timeProvider())),
                () -> assertThrows(NullPointerException.class, () -> new SummaryService(
                        collaborators.taskService(),
                        null,
                        collaborators.studyPlanService(),
                        collaborators.financeService(),
                        collaborators.noteService(),
                        collaborators.timeProvider())),
                () -> assertThrows(NullPointerException.class, () -> new SummaryService(
                        collaborators.taskService(),
                        collaborators.scheduleService(),
                        null,
                        collaborators.financeService(),
                        collaborators.noteService(),
                        collaborators.timeProvider())),
                () -> assertThrows(NullPointerException.class, () -> new SummaryService(
                        collaborators.taskService(),
                        collaborators.scheduleService(),
                        collaborators.studyPlanService(),
                        null,
                        collaborators.noteService(),
                        collaborators.timeProvider())),
                () -> assertThrows(NullPointerException.class, () -> new SummaryService(
                        collaborators.taskService(),
                        collaborators.scheduleService(),
                        collaborators.studyPlanService(),
                        collaborators.financeService(),
                        null,
                        collaborators.timeProvider())),
                () -> assertThrows(NullPointerException.class, () -> new SummaryService(
                        collaborators.taskService(),
                        collaborators.scheduleService(),
                        collaborators.studyPlanService(),
                        collaborators.financeService(),
                        collaborators.noteService(),
                        null)));
    }

    @Test
    void getDashboardSummaryQueriesServicesWithExpectedDateBoundaries() {
        Collaborators collaborators = collaboratorsWithSuccess();
        SummaryService service = collaborators.newSummaryService();

        OperationResult<DashboardSummary> result = service.getDashboardSummary();

        assertTrue(result.isSuccess());
        DashboardSummary summary = result.getPayload();
        assertAll(
                () -> assertEquals(TODAY, summary.today()),
                () -> assertEquals(WEEK_START, summary.weekStart()),
                () -> assertEquals(WEEK_END, summary.weekEnd()),
                () -> assertEquals(MONTH_START, summary.monthStart()),
                () -> assertEquals(MONTH_END, summary.monthEnd()));
        verify(collaborators.taskService()).listTasks(TaskQuery.byDueDate(TODAY));
        verify(collaborators.scheduleService()).listSchedulesByDate(TODAY);
        verify(collaborators.studyPlanService()).listStudyPlans(StudyPlanQuery.byPeriod(new DateRange(WEEK_START, WEEK_END)));
        TransactionQuery monthQuery = TransactionQuery.byDateRange(new DateRange(MONTH_START, MONTH_END));
        verify(collaborators.financeService()).calculateStatistics(monthQuery);
        verify(collaborators.financeService()).listTransactions(monthQuery);
        verify(collaborators.noteService()).listNotes();
    }

    @Test
    void getDashboardSummaryAggregatesNoteTagsInFirstSeenOrder() {
        Collaborators collaborators = collaboratorsWithSuccess();
        when(collaborators.noteService().listNotes()).thenReturn(OperationResult.success(List.of(
                note(1, "First", Tag.of("java"), Tag.of("test")),
                note(2, "Second", Tag.of("java"), Tag.of("design")),
                note(3, "Third", Tag.of("test")))));

        DashboardSummary summary = collaborators.newSummaryService().getDashboardSummary().getPayload();

        assertEquals(
                List.of(Tag.of("java"), Tag.of("test"), Tag.of("design")),
                List.copyOf(summary.noteTagDistribution().keySet()));
        assertEquals(List.of(2, 2, 1), List.copyOf(summary.noteTagDistribution().values()));
    }

    @Test
    void getDashboardSummaryReturnsSnapshotsUnaffectedByLaterServiceChanges() {
        Collaborators collaborators = collaboratorsWithSuccess();
        java.util.ArrayList<TaskView> tasks = new java.util.ArrayList<>(List.of(task(1, "Review")));
        java.util.ArrayList<NoteView> notes = new java.util.ArrayList<>(List.of(note(2, "First", Tag.of("java"))));
        when(collaborators.taskService().listTasks(TaskQuery.byDueDate(TODAY))).thenReturn(OperationResult.success(tasks));
        when(collaborators.noteService().listNotes()).thenReturn(OperationResult.success(notes));

        DashboardSummary summary = collaborators.newSummaryService().getDashboardSummary().getPayload();
        tasks.add(task(3, "Later"));
        notes.add(note(4, "Second", Tag.of("test")));

        assertAll(
                () -> assertEquals(1, summary.todayTasks().size()),
                () -> assertEquals(1, summary.noteCount()),
                () -> assertEquals(List.of(Tag.of("java")), List.copyOf(summary.noteTagDistribution().keySet())));
    }

    @Test
    void getDashboardSummaryPropagatesFirstDependencyFailure() {
        assertFirstFailure(
                c -> when(c.taskService().listTasks(TaskQuery.byDueDate(TODAY)))
                        .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "task failed")),
                "task failed",
                c -> {
                    verify(c.scheduleService(), never()).listSchedulesByDate(any());
                    verify(c.studyPlanService(), never()).listStudyPlans(any());
                    verify(c.financeService(), never()).calculateStatistics(any());
                    verify(c.financeService(), never()).listTransactions(any());
                    verify(c.noteService(), never()).listNotes();
                });
        assertFirstFailure(
                c -> when(c.scheduleService().listSchedulesByDate(TODAY))
                        .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "schedule failed")),
                "schedule failed",
                c -> {
                    verify(c.studyPlanService(), never()).listStudyPlans(any());
                    verify(c.financeService(), never()).calculateStatistics(any());
                    verify(c.financeService(), never()).listTransactions(any());
                    verify(c.noteService(), never()).listNotes();
                });
        assertFirstFailure(
                c -> when(c.studyPlanService().listStudyPlans(StudyPlanQuery.byPeriod(new DateRange(WEEK_START, WEEK_END))))
                        .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "study failed")),
                "study failed",
                c -> {
                    verify(c.financeService(), never()).calculateStatistics(any());
                    verify(c.financeService(), never()).listTransactions(any());
                    verify(c.noteService(), never()).listNotes();
                });
        TransactionQuery monthQuery = TransactionQuery.byDateRange(new DateRange(MONTH_START, MONTH_END));
        assertFirstFailure(
                c -> when(c.financeService().calculateStatistics(monthQuery))
                        .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "statistics failed")),
                "statistics failed",
                c -> {
                    verify(c.financeService(), never()).listTransactions(any());
                    verify(c.noteService(), never()).listNotes();
                });
        assertFirstFailure(
                c -> when(c.financeService().listTransactions(monthQuery))
                        .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "transactions failed")),
                "transactions failed",
                c -> verify(c.noteService(), never()).listNotes());
        assertFirstFailure(
                c -> when(c.noteService().listNotes())
                        .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "notes failed")),
                "notes failed",
                c -> {
                });
    }

    @Test
    void getDashboardSummaryUsesStableFallbackWhenDependencyFailureMessageIsBlank() {
        Collaborators collaborators = collaboratorsWithSuccess();
        OperationResult<List<TaskView>> blankFailure = mock(OperationResult.class);
        when(blankFailure.isFailure()).thenReturn(true);
        when(blankFailure.getErrorCode()).thenReturn(ErrorCode.VALIDATION_ERROR);
        when(blankFailure.getMessage()).thenReturn(" \t");
        when(collaborators.taskService().listTasks(TaskQuery.byDueDate(TODAY))).thenReturn(blankFailure);

        OperationResult<DashboardSummary> result = collaborators.newSummaryService().getDashboardSummary();

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("summary service dependency failed", result.getMessage()));
    }

    @Test
    void buildLocalContextPropagatesSummaryFailure() {
        Collaborators collaborators = collaboratorsWithSuccess();
        when(collaborators.taskService().listTasks(TaskQuery.byDueDate(TODAY)))
                .thenReturn(OperationResult.failure(ErrorCode.VALIDATION_ERROR, "task failed"));

        OperationResult<LocalContext> result = collaborators.newSummaryService().buildLocalContext();

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals("task failed", result.getMessage()));
    }

    @Test
    void buildLocalContextReturnsLocalContextFromSuccessfulSummary() {
        Collaborators collaborators = collaboratorsWithSuccess();

        OperationResult<LocalContext> result = collaborators.newSummaryService().buildLocalContext();

        assertTrue(result.isSuccess());
        LocalContext context = result.getPayload();
        assertEquals(
                "今日任务1项，今日日程1项，本周学习计划2项（已完成1项，未完成1项），本月收入0.00，支出0.00，结余0.00，笔记1篇，标签1个。",
                context.overviewText());
        assertAll(
                () -> assertEquals(1, context.dashboardSummary().todayTasks().size()),
                () -> assertEquals(1, context.dashboardSummary().todaySchedules().size()),
                () -> assertEquals(2, context.dashboardSummary().weekStudyPlans().size()),
                () -> assertEquals(1, context.dashboardSummary().monthTransactions().size()),
                () -> assertEquals(List.of(Tag.of("java")), List.copyOf(context.dashboardSummary().noteTagDistribution().keySet())),
                () -> assertEquals(
                        List.of("任务：Review｜优先级：HIGH｜状态：TODO｜截止：2026-02-18"),
                        context.todayTaskLines()),
                () -> assertEquals(
                        List.of("日程：Standup｜状态：UPCOMING｜时间：2026-02-18T09:00~2026-02-18T10:00｜地点：Room A"),
                        context.todayScheduleLines()),
                () -> assertEquals(
                        List.of(
                                "学习：Java｜状态：COMPLETED｜进度：100%｜周期：2026-02-16~2026-02-22",
                                "学习：Mockito｜状态：IN_PROGRESS｜进度：40%｜周期：2026-02-16~2026-02-22"),
                        context.weekStudyPlanLines()),
                () -> assertEquals(
                        List.of("收支：EXPENSE｜金额：8.50｜类别：Food｜日期：2026-02-18"),
                        context.monthTransactionLines()),
                () -> assertEquals(
                        List.of("标签：java｜数量：1"),
                        context.noteTagLines()));
    }

    @Test
    void weekStudyPlanCountsUseOnlyWeekStudyPlansSnapshot() {
        Collaborators collaborators = collaboratorsWithSuccess();
        when(collaborators.studyPlanService().listStudyPlans(StudyPlanQuery.byPeriod(new DateRange(WEEK_START, WEEK_END))))
                .thenReturn(OperationResult.success(List.of(
                        plan(1, "Weekly done", 100, StudyPlanStatus.COMPLETED),
                        plan(2, "Weekly todo", 40, StudyPlanStatus.IN_PROGRESS))));
        when(collaborators.studyPlanService().countCompletedPlans()).thenReturn(OperationResult.success(2));
        when(collaborators.studyPlanService().countIncompletePlans()).thenReturn(OperationResult.success(1));

        OperationResult<LocalContext> result = collaborators.newSummaryService().buildLocalContext();

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(1, result.getPayload().dashboardSummary().completedWeekStudyPlanCount()),
                () -> assertEquals(1, result.getPayload().dashboardSummary().incompleteWeekStudyPlanCount()),
                () -> assertTrue(result.getPayload().overviewText().contains("已完成1项，未完成1项")));
        verify(collaborators.studyPlanService(), never()).countCompletedPlans();
        verify(collaborators.studyPlanService(), never()).countIncompletePlans();
    }

    @Test
    void summaryDoesNotUseRealCurrentDate() {
        Collaborators collaborators = collaboratorsWithSuccess();

        DashboardSummary summary = collaborators.newSummaryService().getDashboardSummary().getPayload();

        assertEquals(TODAY, summary.today());
        verify(collaborators.timeProvider(), times(1)).today();
    }

    @Test
    void getDashboardSummaryUsesSingleStableTodaySnapshotForAllDateBoundariesAndQueries() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        when(timeProvider.today()).thenReturn(TODAY, LocalDate.of(2026, 3, 1));
        Collaborators collaborators = collaboratorsWithSuccess(timeProvider);

        OperationResult<DashboardSummary> result = collaborators.newSummaryService().getDashboardSummary();

        assertTrue(result.isSuccess());
        DashboardSummary summary = result.getPayload();
        assertAll(
                () -> assertEquals(TODAY, summary.today()),
                () -> assertEquals(WEEK_START, summary.weekStart()),
                () -> assertEquals(WEEK_END, summary.weekEnd()),
                () -> assertEquals(MONTH_START, summary.monthStart()),
                () -> assertEquals(MONTH_END, summary.monthEnd()));
        verify(collaborators.timeProvider(), times(1)).today();
        verify(collaborators.taskService()).listTasks(TaskQuery.byDueDate(TODAY));
        verify(collaborators.scheduleService()).listSchedulesByDate(TODAY);
        verify(collaborators.studyPlanService()).listStudyPlans(StudyPlanQuery.byPeriod(new DateRange(WEEK_START, WEEK_END)));
        TransactionQuery monthQuery = TransactionQuery.byDateRange(new DateRange(MONTH_START, MONTH_END));
        verify(collaborators.financeService()).calculateStatistics(monthQuery);
        verify(collaborators.financeService()).listTransactions(monthQuery);
    }

    private static void assertFirstFailure(
            java.util.function.Consumer<Collaborators> override,
            String expectedMessage,
            java.util.function.Consumer<Collaborators> verifyShortCircuit) {
        Collaborators collaborators = collaboratorsWithSuccess();
        override.accept(collaborators);

        OperationResult<DashboardSummary> result = collaborators.newSummaryService().getDashboardSummary();

        assertAll(
                () -> assertTrue(result.isFailure()),
                () -> assertEquals(ErrorCode.VALIDATION_ERROR, result.getErrorCode()),
                () -> assertEquals(expectedMessage, result.getMessage()));
        verifyShortCircuit.accept(collaborators);
    }

    private static Collaborators collaboratorsWithSuccess() {
        return collaboratorsWithSuccess(defaultTimeProvider());
    }

    private static Collaborators collaboratorsWithSuccess(TimeProvider timeProvider) {
        Collaborators collaborators = collaborators();
        collaborators = new Collaborators(
                collaborators.taskService(),
                collaborators.scheduleService(),
                collaborators.studyPlanService(),
                collaborators.financeService(),
                collaborators.noteService(),
                timeProvider);
        when(collaborators.taskService().listTasks(TaskQuery.byDueDate(TODAY)))
                .thenReturn(OperationResult.success(List.of(task(1, "Review"))));
        when(collaborators.scheduleService().listSchedulesByDate(TODAY))
                .thenReturn(OperationResult.success(List.of(schedule(2, "Standup"))));
        when(collaborators.studyPlanService().listStudyPlans(StudyPlanQuery.byPeriod(new DateRange(WEEK_START, WEEK_END))))
                .thenReturn(OperationResult.success(List.of(
                        plan(3, "Java", 100, StudyPlanStatus.COMPLETED),
                        plan(4, "Mockito", 40, StudyPlanStatus.IN_PROGRESS))));
        TransactionQuery monthQuery = TransactionQuery.byDateRange(new DateRange(MONTH_START, MONTH_END));
        when(collaborators.financeService().calculateStatistics(monthQuery))
                .thenReturn(OperationResult.success(FinanceStatistics.zero()));
        when(collaborators.financeService().listTransactions(monthQuery))
                .thenReturn(OperationResult.success(List.of(transaction(5, TransactionType.EXPENSE))));
        when(collaborators.noteService().listNotes())
                .thenReturn(OperationResult.success(List.of(note(6, "Note", Tag.of("java")))));
        return collaborators;
    }

    private static Collaborators collaborators() {
        TimeProvider timeProvider = defaultTimeProvider();
        return new Collaborators(
                mock(TaskService.class),
                mock(ScheduleService.class),
                mock(StudyPlanService.class),
                mock(FinanceService.class),
                mock(NoteService.class),
                timeProvider);
    }

    private static TimeProvider defaultTimeProvider() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        when(timeProvider.today()).thenReturn(TODAY);
        return timeProvider;
    }

    private record Collaborators(
            TaskService taskService,
            ScheduleService scheduleService,
            StudyPlanService studyPlanService,
            FinanceService financeService,
            NoteService noteService,
            TimeProvider timeProvider) {
        SummaryService newSummaryService() {
            return new SummaryService(
                    taskService,
                    scheduleService,
                    studyPlanService,
                    financeService,
                    noteService,
                    timeProvider);
        }
    }

    private static TaskView task(long id, String title) {
        return new TaskView(new EntityId(id), title, "Description", TaskPriority.HIGH, TODAY, TaskStatus.TODO);
    }

    private static ScheduleView schedule(long id, String name) {
        DateTimeRange range = new DateTimeRange(
                TODAY.atTime(9, 0),
                TODAY.atTime(10, 0));
        return new ScheduleView(
                new EntityId(id),
                name,
                range,
                range.startDateTime(),
                range.endDateTime(),
                "Room A",
                "Note",
                ScheduleStatus.UPCOMING);
    }

    private static StudyPlanView plan(long id, String goal, int progress, StudyPlanStatus status) {
        DateRange period = new DateRange(WEEK_START, WEEK_END);
        return new StudyPlanView(
                new EntityId(id),
                goal,
                period,
                period.startDate(),
                period.endDate(),
                10,
                Progress.of(progress),
                status);
    }

    private static TransactionView transaction(long id, TransactionType type) {
        return new TransactionView(
                new EntityId(id),
                type,
                TransactionAmount.of("8.50"),
                "Food",
                TODAY,
                "Note");
    }

    private static NoteView note(long id, String title, Tag... tags) {
        LinkedHashSet<Tag> orderedTags = new LinkedHashSet<>(List.of(tags));
        return new NoteView(
                new EntityId(id),
                title,
                "Content",
                TODAY,
                orderedTags);
    }
}
