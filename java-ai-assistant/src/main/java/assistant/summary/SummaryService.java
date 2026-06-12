package assistant.summary;

import assistant.common.DateRange;
import assistant.common.OperationResult;
import assistant.common.Tag;
import assistant.finance.FinanceService;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionQuery;
import assistant.finance.TransactionView;
import assistant.note.NoteService;
import assistant.note.NoteView;
import assistant.schedule.ScheduleService;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanQuery;
import assistant.study.StudyPlanService;
import assistant.study.StudyPlanStatus;
import assistant.study.StudyPlanView;
import assistant.task.TaskQuery;
import assistant.task.TaskService;
import assistant.task.TaskView;
import assistant.testability.TimeProvider;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SummaryService {
    private final TaskService taskService;
    private final ScheduleService scheduleService;
    private final StudyPlanService studyPlanService;
    private final FinanceService financeService;
    private final NoteService noteService;
    private final TimeProvider timeProvider;

    public SummaryService(
            TaskService taskService,
            ScheduleService scheduleService,
            StudyPlanService studyPlanService,
            FinanceService financeService,
            NoteService noteService,
            TimeProvider timeProvider) {
        this.taskService = Objects.requireNonNull(taskService, "taskService");
        this.scheduleService = Objects.requireNonNull(scheduleService, "scheduleService");
        this.studyPlanService = Objects.requireNonNull(studyPlanService, "studyPlanService");
        this.financeService = Objects.requireNonNull(financeService, "financeService");
        this.noteService = Objects.requireNonNull(noteService, "noteService");
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider");
    }

    public OperationResult<DashboardSummary> getDashboardSummary() {
        LocalDate today = Objects.requireNonNull(timeProvider.today(), "today");
        LocalDate weekStart = weekStartOf(today);
        LocalDate weekEnd = weekEndOf(today);
        LocalDate monthStart = monthStartOf(today);
        LocalDate monthEnd = monthEndOf(today);

        OperationResult<List<TaskView>> tasksResult = taskService.listTasks(TaskQuery.byDueDate(today));
        if (tasksResult.isFailure()) {
            return propagateFailure(tasksResult);
        }

        OperationResult<List<ScheduleView>> schedulesResult = scheduleService.listSchedulesByDate(today);
        if (schedulesResult.isFailure()) {
            return propagateFailure(schedulesResult);
        }

        DateRange weekRange = new DateRange(weekStart, weekEnd);
        OperationResult<List<StudyPlanView>> studyPlansResult =
                studyPlanService.listStudyPlans(StudyPlanQuery.byPeriod(weekRange));
        if (studyPlansResult.isFailure()) {
            return propagateFailure(studyPlansResult);
        }

        DateRange monthRange = new DateRange(monthStart, monthEnd);
        TransactionQuery monthQuery = TransactionQuery.byDateRange(monthRange);
        OperationResult<FinanceStatistics> financeStatisticsResult = financeService.calculateStatistics(monthQuery);
        if (financeStatisticsResult.isFailure()) {
            return propagateFailure(financeStatisticsResult);
        }

        OperationResult<List<TransactionView>> transactionsResult = financeService.listTransactions(monthQuery);
        if (transactionsResult.isFailure()) {
            return propagateFailure(transactionsResult);
        }

        OperationResult<List<NoteView>> notesResult = noteService.listNotes();
        if (notesResult.isFailure()) {
            return propagateFailure(notesResult);
        }

        List<StudyPlanView> weekStudyPlans = studyPlansResult.getPayload();
        List<NoteView> notes = notesResult.getPayload();
        return OperationResult.success(new DashboardSummary(
                today,
                weekStart,
                weekEnd,
                monthStart,
                monthEnd,
                tasksResult.getPayload(),
                schedulesResult.getPayload(),
                weekStudyPlans,
                countCompletedWeekPlans(weekStudyPlans),
                countIncompleteWeekPlans(weekStudyPlans),
                financeStatisticsResult.getPayload(),
                transactionsResult.getPayload(),
                notes.size(),
                countNoteTags(notes)));
    }

    public OperationResult<LocalContext> buildLocalContext() {
        OperationResult<DashboardSummary> summaryResult = getDashboardSummary();
        if (summaryResult.isFailure()) {
            return propagateFailure(summaryResult);
        }
        return OperationResult.success(LocalContext.from(summaryResult.getPayload()));
    }

    private static LocalDate weekStartOf(LocalDate today) {
        Objects.requireNonNull(today, "today");
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static LocalDate weekEndOf(LocalDate today) {
        Objects.requireNonNull(today, "today");
        return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    private static LocalDate monthStartOf(LocalDate today) {
        Objects.requireNonNull(today, "today");
        return today.withDayOfMonth(1);
    }

    private static LocalDate monthEndOf(LocalDate today) {
        Objects.requireNonNull(today, "today");
        return today.withDayOfMonth(today.lengthOfMonth());
    }

    private static int countCompletedWeekPlans(List<StudyPlanView> weekStudyPlans) {
        Objects.requireNonNull(weekStudyPlans, "weekStudyPlans");
        return (int) weekStudyPlans.stream()
                .filter(plan -> plan.status() == StudyPlanStatus.COMPLETED)
                .count();
    }

    private static int countIncompleteWeekPlans(List<StudyPlanView> weekStudyPlans) {
        Objects.requireNonNull(weekStudyPlans, "weekStudyPlans");
        return (int) weekStudyPlans.stream()
                .filter(plan -> plan.status() != StudyPlanStatus.COMPLETED)
                .count();
    }

    private static Map<Tag, Integer> countNoteTags(List<NoteView> notes) {
        Objects.requireNonNull(notes, "notes");
        LinkedHashMap<Tag, Integer> distribution = new LinkedHashMap<>();
        for (NoteView note : notes) {
            for (Tag tag : Objects.requireNonNull(note, "note").tags()) {
                distribution.merge(tag, 1, Integer::sum);
            }
        }
        return distribution;
    }

    private static <T> OperationResult<T> propagateFailure(OperationResult<?> failure) {
        Objects.requireNonNull(failure, "failure");
        if (!failure.isFailure()) {
            throw new IllegalArgumentException("failure must be failed");
        }
        return OperationResult.failure(failure.getErrorCode(), stableMessage(failure.getMessage()));
    }

    private static String stableMessage(String message) {
        if (message == null || message.isBlank()) {
            return "summary service dependency failed";
        }
        return message;
    }
}
