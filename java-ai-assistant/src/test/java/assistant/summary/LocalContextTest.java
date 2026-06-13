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

class LocalContextTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);
    private static final LocalDate WEEK_START = LocalDate.of(2026, 6, 8);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 6, 14);
    private static final LocalDate MONTH_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate MONTH_END = LocalDate.of(2026, 6, 30);

    @Test
    void fromBuildsStableOverviewAndEmptyLinesForEmptySummary() {
        LocalContext context = LocalContext.from(emptySummary());

        assertAll(
                () -> assertEquals(
                        "今日任务0项，逾期未完成任务0项，未来7天高优先级任务0项，今日日程0项，本周学习计划0项（已完成0项，未完成0项），本月收入0.00，支出0.00，结余0.00，笔记0篇，标签0个。",
                        context.overviewText()),
                () -> assertTrue(context.todayTaskLines().isEmpty()),
                () -> assertTrue(context.overdueTaskLines().isEmpty()),
                () -> assertTrue(context.upcomingHighPriorityTaskLines().isEmpty()),
                () -> assertTrue(context.todayScheduleLines().isEmpty()),
                () -> assertTrue(context.weekStudyPlanLines().isEmpty()),
                () -> assertTrue(context.monthTransactionLines().isEmpty()),
                () -> assertTrue(context.noteTagLines().isEmpty()));
    }

    @Test
    void fromBuildsOnlyCorrespondingLinesForSingleModuleData() {
        assertAll(
                () -> assertEquals(List.of("任务：Review｜优先级：HIGH｜状态：TODO｜截止：2026-06-12"),
                        LocalContext.from(summaryWithTasks(List.of(task(1, "Review")), List.of(), List.of()))
                                .todayTaskLines()),
                () -> assertEquals(List.of("任务：Overdue｜优先级：HIGH｜状态：TODO｜截止：2026-06-12"),
                        LocalContext.from(summaryWithTasks(List.of(), List.of(task(2, "Overdue")), List.of()))
                                .overdueTaskLines()),
                () -> assertEquals(List.of("任务：Urgent｜优先级：HIGH｜状态：TODO｜截止：2026-06-12"),
                        LocalContext.from(summaryWithTasks(List.of(), List.of(), List.of(task(3, "Urgent"))))
                                .upcomingHighPriorityTaskLines()),
                () -> assertEquals(List.of("日程：Standup｜状态：UPCOMING｜时间：2026-06-12T09:00~2026-06-12T10:00｜地点：Room A"),
                        LocalContext.from(summaryWith(List.of(), List.of(), List.of(), List.of(schedule(4, "Standup")), List.of(), List.of(), Map.of()))
                                .todayScheduleLines()),
                () -> assertEquals(List.of("学习：Java｜状态：COMPLETED｜进度：100%｜周期：2026-06-08~2026-06-14"),
                        LocalContext.from(summaryWith(List.of(), List.of(), List.of(), List.of(), List.of(plan(5, "Java", 100, StudyPlanStatus.COMPLETED)), List.of(), Map.of()))
                                .weekStudyPlanLines()),
                () -> assertEquals(List.of("收支：EXPENSE｜金额：8.50｜类别：Food｜日期：2026-06-12"),
                        LocalContext.from(summaryWith(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(transaction(6, TransactionType.EXPENSE)), Map.of()))
                                .monthTransactionLines()),
                () -> assertEquals(List.of("标签：java｜数量：2"),
                        LocalContext.from(summaryWith(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), linkedTags()))
                                .noteTagLines()));
    }

    @Test
    void fromBuildsLinesInSourceOrderForMultiModuleData() {
        LinkedHashMap<Tag, Integer> tags = new LinkedHashMap<>();
        tags.put(Tag.of("java"), 2);
        tags.put(Tag.of("test"), 1);

        LocalContext context = LocalContext.from(summaryWith(
                List.of(task(1, "Review"), task(2, "Write")),
                List.of(task(3, "Overdue"), task(4, "Blocked")),
                List.of(task(5, "Urgent"), task(6, "Risk")),
                List.of(schedule(7, "Standup"), schedule(8, "Retro")),
                List.of(
                        plan(9, "Java", 100, StudyPlanStatus.COMPLETED),
                        plan(10, "Mockito", 40, StudyPlanStatus.IN_PROGRESS)),
                List.of(transaction(11, TransactionType.INCOME), transaction(12, TransactionType.EXPENSE)),
                tags));

        assertAll(
                () -> assertEquals(
                        List.of(
                                "任务：Review｜优先级：HIGH｜状态：TODO｜截止：2026-06-12",
                                "任务：Write｜优先级：HIGH｜状态：TODO｜截止：2026-06-12"),
                        context.todayTaskLines()),
                () -> assertEquals(
                        List.of(
                                "任务：Overdue｜优先级：HIGH｜状态：TODO｜截止：2026-06-12",
                                "任务：Blocked｜优先级：HIGH｜状态：TODO｜截止：2026-06-12"),
                        context.overdueTaskLines()),
                () -> assertEquals(
                        List.of(
                                "任务：Urgent｜优先级：HIGH｜状态：TODO｜截止：2026-06-12",
                                "任务：Risk｜优先级：HIGH｜状态：TODO｜截止：2026-06-12"),
                        context.upcomingHighPriorityTaskLines()),
                () -> assertEquals(
                        List.of(
                                "日程：Standup｜状态：UPCOMING｜时间：2026-06-12T09:00~2026-06-12T10:00｜地点：Room A",
                                "日程：Retro｜状态：UPCOMING｜时间：2026-06-12T09:00~2026-06-12T10:00｜地点：Room A"),
                        context.todayScheduleLines()),
                () -> assertEquals(
                        List.of(
                                "学习：Java｜状态：COMPLETED｜进度：100%｜周期：2026-06-08~2026-06-14",
                                "学习：Mockito｜状态：IN_PROGRESS｜进度：40%｜周期：2026-06-08~2026-06-14"),
                        context.weekStudyPlanLines()),
                () -> assertEquals(
                        List.of(
                                "收支：INCOME｜金额：8.50｜类别：Food｜日期：2026-06-12",
                                "收支：EXPENSE｜金额：8.50｜类别：Food｜日期：2026-06-12"),
                        context.monthTransactionLines()),
                () -> assertEquals(List.of("标签：java｜数量：2", "标签：test｜数量：1"), context.noteTagLines()));
    }

    @Test
    void constructorCopiesInputListsAsUnmodifiableSnapshots() {
        ArrayList<String> taskLines = new ArrayList<>(List.of("任务：Review"));
        ArrayList<String> overdueTaskLines = new ArrayList<>(List.of("任务：Overdue"));
        ArrayList<String> upcomingHighPriorityTaskLines = new ArrayList<>(List.of("任务：Urgent"));
        ArrayList<String> scheduleLines = new ArrayList<>(List.of("日程：Standup"));
        ArrayList<String> studyPlanLines = new ArrayList<>(List.of("学习：Java"));
        ArrayList<String> transactionLines = new ArrayList<>(List.of("收支：INCOME"));
        ArrayList<String> noteTagLines = new ArrayList<>(List.of("标签：java"));
        LocalContext context = new LocalContext(
                emptySummary(),
                " Overview ",
                taskLines,
                overdueTaskLines,
                upcomingHighPriorityTaskLines,
                scheduleLines,
                studyPlanLines,
                transactionLines,
                noteTagLines);

        taskLines.set(0, "任务：Later");
        overdueTaskLines.set(0, "任务：Later");
        upcomingHighPriorityTaskLines.set(0, "任务：Later");
        scheduleLines.set(0, "日程：Later");
        studyPlanLines.set(0, "学习：Later");
        transactionLines.set(0, "收支：Later");
        noteTagLines.set(0, "标签：later");

        assertAll(
                () -> assertEquals("Overview", context.overviewText()),
                () -> assertEquals(List.of("任务：Review"), context.todayTaskLines()),
                () -> assertEquals(List.of("任务：Overdue"), context.overdueTaskLines()),
                () -> assertEquals(List.of("任务：Urgent"), context.upcomingHighPriorityTaskLines()),
                () -> assertEquals(List.of("日程：Standup"), context.todayScheduleLines()),
                () -> assertEquals(List.of("学习：Java"), context.weekStudyPlanLines()),
                () -> assertEquals(List.of("收支：INCOME"), context.monthTransactionLines()),
                () -> assertEquals(List.of("标签：java"), context.noteTagLines()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.todayTaskLines().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.overdueTaskLines().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.upcomingHighPriorityTaskLines().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.todayScheduleLines().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.weekStudyPlanLines().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.monthTransactionLines().clear()),
                () -> assertThrows(UnsupportedOperationException.class, () -> context.noteTagLines().add("标签：test")));
    }

    @Test
    void constructorRejectsNullsAndBlankOverviewOrLines() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(null, "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), " \t\n", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), null, List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), null, List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), null, List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", listWithNull(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), listWithNull(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), listWithNull(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), listWithNull(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), listWithNull(), List.of(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), listWithNull(), List.of())),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), listWithNull())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(" \t"), List.of(), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(" \t"), List.of(), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(" \t"), List.of(), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(" \t"), List.of(), List.of(), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(" \t"), List.of(), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(" \t"), List.of())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> context(emptySummary(), "Overview", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(" \t"))));
    }

    private static LocalContext context(
            DashboardSummary summary,
            String overview,
            List<String> todayTaskLines,
            List<String> overdueTaskLines,
            List<String> upcomingHighPriorityTaskLines,
            List<String> scheduleLines,
            List<String> studyPlanLines,
            List<String> transactionLines,
            List<String> noteTagLines) {
        return new LocalContext(
                summary,
                overview,
                todayTaskLines,
                overdueTaskLines,
                upcomingHighPriorityTaskLines,
                scheduleLines,
                studyPlanLines,
                transactionLines,
                noteTagLines);
    }

    private static DashboardSummary emptySummary() {
        return summaryWith(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), Map.of());
    }

    private static DashboardSummary summaryWithTasks(
            List<TaskView> todayTasks,
            List<TaskView> overdueTasks,
            List<TaskView> upcomingHighPriorityTasks) {
        return summaryWith(todayTasks, overdueTasks, upcomingHighPriorityTasks, List.of(), List.of(), List.of(), Map.of());
    }

    private static DashboardSummary summaryWith(
            List<TaskView> todayTasks,
            List<TaskView> overdueTasks,
            List<TaskView> upcomingHighPriorityTasks,
            List<ScheduleView> schedules,
            List<StudyPlanView> plans,
            List<TransactionView> transactions,
            Map<Tag, Integer> tags) {
        int completed = (int) plans.stream().filter(plan -> plan.status() == StudyPlanStatus.COMPLETED).count();
        return new DashboardSummary(
                TODAY,
                WEEK_START,
                WEEK_END,
                MONTH_START,
                MONTH_END,
                todayTasks,
                overdueTasks,
                upcomingHighPriorityTasks,
                schedules,
                plans,
                completed,
                plans.size() - completed,
                FinanceStatistics.zero(),
                transactions,
                tags.values().stream().mapToInt(Integer::intValue).sum(),
                tags);
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
                TransactionAmount.of("8.50"),
                "Food",
                TODAY,
                "Note");
    }

    private static LinkedHashMap<Tag, Integer> linkedTags() {
        LinkedHashMap<Tag, Integer> tags = new LinkedHashMap<>();
        tags.put(Tag.of("java"), 2);
        return tags;
    }

    private static List<String> listWithNull() {
        ArrayList<String> values = new ArrayList<>();
        values.add(null);
        return values;
    }
}
