package assistant.summary;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import assistant.common.DateRange;
import assistant.common.DateTimeRange;
import assistant.common.EntityId;
import assistant.common.Progress;
import assistant.common.Tag;
import assistant.common.TransactionAmount;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionType;
import assistant.finance.TransactionView;
import assistant.schedule.ScheduleStatus;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanStatus;
import assistant.study.StudyPlanView;
import assistant.task.TaskPriority;
import assistant.task.TaskStatus;
import assistant.task.TaskView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DashboardSummaryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);
    private static final LocalDate WEEK_START = LocalDate.of(2026, 6, 8);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 6, 14);
    private static final LocalDate MONTH_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate MONTH_END = LocalDate.of(2026, 6, 30);

    @Test
    void constructorCopiesListsAndMapAsUnmodifiableSnapshots() {
        ArrayList<TaskView> tasks = new ArrayList<>(List.of(task(1, "Review")));
        ArrayList<ScheduleView> schedules = new ArrayList<>(List.of(schedule(2, "Standup")));
        ArrayList<StudyPlanView> plans = new ArrayList<>(List.of(plan(3, "Java", 100, StudyPlanStatus.COMPLETED)));
        ArrayList<TransactionView> transactions = new ArrayList<>(List.of(transaction(4, TransactionType.INCOME)));
        LinkedHashMap<Tag, Integer> tags = new LinkedHashMap<>();
        tags.put(Tag.of("java"), 2);
        tags.put(Tag.of("test"), 1);

        DashboardSummary summary = new DashboardSummary(
                TODAY,
                WEEK_START,
                WEEK_END,
                MONTH_START,
                MONTH_END,
                tasks,
                schedules,
                plans,
                1,
                0,
                FinanceStatistics.zero(),
                transactions,
                3,
                tags);

        tasks.clear();
        schedules.clear();
        plans.clear();
        transactions.clear();
        tags.put(Tag.of("later"), 1);

        assertAll(
                () -> assertEquals(1, summary.todayTasks().size()),
                () -> assertEquals(1, summary.todaySchedules().size()),
                () -> assertEquals(1, summary.weekStudyPlans().size()),
                () -> assertEquals(1, summary.monthTransactions().size()),
                () -> assertEquals(List.of(Tag.of("java"), Tag.of("test")), List.copyOf(summary.noteTagDistribution().keySet())),
                () -> assertThrows(UnsupportedOperationException.class, () -> summary.todayTasks().add(task(9, "Later"))),
                () -> assertThrows(UnsupportedOperationException.class, () -> summary.todaySchedules().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> summary.weekStudyPlans().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> summary.monthTransactions().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> summary.noteTagDistribution().clear()));
    }

    @Test
    void constructorRejectsNullRequiredFieldsAndElements() {
        DashboardSummary valid = emptySummary();
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                null,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                null,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                null,
                                MONTH_START,
                                MONTH_END,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                null,
                                MONTH_END,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                null,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                null,
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                null,
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                valid.monthTransactions(),
                                0,
                                valid.noteTagDistribution())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                listWithNull(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                valid.monthTransactions(),
                                0,
                                valid.noteTagDistribution())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                null,
                                0,
                                0,
                                FinanceStatistics.zero(),
                                valid.monthTransactions(),
                                0,
                                valid.noteTagDistribution())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                listWithNull(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                Map.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                null,
                                valid.monthTransactions(),
                                0,
                                valid.noteTagDistribution())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                null,
                                0,
                                valid.noteTagDistribution())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                listWithNull(),
                                0,
                                valid.noteTagDistribution())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                valid.monthTransactions(),
                                0,
                                null)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                valid.monthTransactions(),
                                0,
                                mapWithNullKey())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                valid.todayTasks(),
                                valid.todaySchedules(),
                                valid.weekStudyPlans(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                valid.monthTransactions(),
                                0,
                                mapWithNullValue())));
    }

    @Test
    void constructorRejectsInvalidDateBoundariesAndCounts() {
        List<StudyPlanView> plans = List.of(
                plan(1, "Done", 100, StudyPlanStatus.COMPLETED),
                plan(2, "Doing", 40, StudyPlanStatus.IN_PROGRESS));

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> summaryWith(WEEK_END, WEEK_START, MONTH_START, MONTH_END, plans, 1, 1)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> summaryWith(WEEK_START, WEEK_END, MONTH_END, MONTH_START, plans, 1, 1)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> summaryWith(WEEK_START, WEEK_END, MONTH_START, MONTH_END, plans, -1, 1)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> summaryWith(WEEK_START, WEEK_END, MONTH_START, MONTH_END, plans, 2, 0)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> summaryWith(WEEK_START, WEEK_END, MONTH_START, MONTH_END, plans, 1, -1)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> summaryWith(WEEK_START, WEEK_END, MONTH_START, MONTH_END, plans, 1, 0)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                -1,
                                Map.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new DashboardSummary(
                                TODAY,
                                WEEK_START,
                                WEEK_END,
                                MONTH_START,
                                MONTH_END,
                                List.of(),
                                List.of(),
                                List.of(),
                                0,
                                0,
                                FinanceStatistics.zero(),
                                List.of(),
                                0,
                                mapWithCount(0))));
    }

    @Test
    void constructorAllowsEmptySuccessfulSummary() {
        DashboardSummary summary = emptySummary();

        assertAll(
                () -> assertTrue(summary.todayTasks().isEmpty()),
                () -> assertTrue(summary.todaySchedules().isEmpty()),
                () -> assertTrue(summary.weekStudyPlans().isEmpty()),
                () -> assertEquals(FinanceStatistics.zero(), summary.monthFinanceStatistics()),
                () -> assertTrue(summary.monthTransactions().isEmpty()),
                () -> assertEquals(0, summary.noteCount()),
                () -> assertTrue(summary.noteTagDistribution().isEmpty()));
    }

    private static DashboardSummary emptySummary() {
        return new DashboardSummary(
                TODAY,
                WEEK_START,
                WEEK_END,
                MONTH_START,
                MONTH_END,
                List.of(),
                List.of(),
                List.of(),
                0,
                0,
                FinanceStatistics.zero(),
                List.of(),
                0,
                Map.of());
    }

    private static DashboardSummary summaryWith(
            LocalDate weekStart,
            LocalDate weekEnd,
            LocalDate monthStart,
            LocalDate monthEnd,
            List<StudyPlanView> plans,
            int completed,
            int incomplete) {
        return new DashboardSummary(
                TODAY,
                weekStart,
                weekEnd,
                monthStart,
                monthEnd,
                List.of(),
                List.of(),
                plans,
                completed,
                incomplete,
                FinanceStatistics.zero(),
                List.of(),
                0,
                Map.of());
    }

    private static TaskView task(long id, String title) {
        return new TaskView(new EntityId(id), title, "Description", TaskPriority.HIGH, TODAY, TaskStatus.TODO);
    }

    private static ScheduleView schedule(long id, String name) {
        DateTimeRange range = new DateTimeRange(
                LocalDateTime.of(2026, 6, 12, 9, 0),
                LocalDateTime.of(2026, 6, 12, 10, 0));
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
                TransactionAmount.of("10.00"),
                "Salary",
                TODAY,
                "Note");
    }

    private static <T> List<T> listWithNull() {
        ArrayList<T> values = new ArrayList<>();
        values.add(null);
        return values;
    }

    private static Map<Tag, Integer> mapWithNullValue() {
        LinkedHashMap<Tag, Integer> values = new LinkedHashMap<>();
        values.put(Tag.of("java"), null);
        return values;
    }

    private static Map<Tag, Integer> mapWithNullKey() {
        LinkedHashMap<Tag, Integer> values = new LinkedHashMap<>();
        values.put(null, 1);
        return values;
    }

    private static Map<Tag, Integer> mapWithCount(int count) {
        LinkedHashMap<Tag, Integer> values = new LinkedHashMap<>();
        values.put(Tag.of("java"), count);
        return values;
    }
}
