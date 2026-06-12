package assistant.summary;

import assistant.common.Tag;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionView;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanStatus;
import assistant.study.StudyPlanView;
import assistant.task.TaskView;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record DashboardSummary(
        LocalDate today,
        LocalDate weekStart,
        LocalDate weekEnd,
        LocalDate monthStart,
        LocalDate monthEnd,
        List<TaskView> todayTasks,
        List<ScheduleView> todaySchedules,
        List<StudyPlanView> weekStudyPlans,
        int completedWeekStudyPlanCount,
        int incompleteWeekStudyPlanCount,
        FinanceStatistics monthFinanceStatistics,
        List<TransactionView> monthTransactions,
        int noteCount,
        Map<Tag, Integer> noteTagDistribution) {
    public DashboardSummary {
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(weekStart, "weekStart");
        Objects.requireNonNull(weekEnd, "weekEnd");
        Objects.requireNonNull(monthStart, "monthStart");
        Objects.requireNonNull(monthEnd, "monthEnd");
        if (weekStart.isAfter(weekEnd)) {
            throw new IllegalArgumentException("weekStart must not be after weekEnd");
        }
        if (monthStart.isAfter(monthEnd)) {
            throw new IllegalArgumentException("monthStart must not be after monthEnd");
        }

        todayTasks = copyList(todayTasks, "todayTasks");
        todaySchedules = copyList(todaySchedules, "todaySchedules");
        weekStudyPlans = copyList(weekStudyPlans, "weekStudyPlans");
        if (completedWeekStudyPlanCount < 0) {
            throw new IllegalArgumentException("completedWeekStudyPlanCount must not be negative");
        }
        if (incompleteWeekStudyPlanCount < 0) {
            throw new IllegalArgumentException("incompleteWeekStudyPlanCount must not be negative");
        }
        int actualCompleted = (int) weekStudyPlans.stream()
                .filter(plan -> plan.status() == StudyPlanStatus.COMPLETED)
                .count();
        int actualIncomplete = weekStudyPlans.size() - actualCompleted;
        if (completedWeekStudyPlanCount != actualCompleted) {
            throw new IllegalArgumentException("completedWeekStudyPlanCount must match weekStudyPlans");
        }
        if (incompleteWeekStudyPlanCount != actualIncomplete) {
            throw new IllegalArgumentException("incompleteWeekStudyPlanCount must match weekStudyPlans");
        }

        Objects.requireNonNull(monthFinanceStatistics, "monthFinanceStatistics");
        monthTransactions = copyList(monthTransactions, "monthTransactions");
        if (noteCount < 0) {
            throw new IllegalArgumentException("noteCount must not be negative");
        }
        noteTagDistribution = copyTagDistribution(noteTagDistribution);
    }

    private static <T> List<T> copyList(List<T> values, String name) {
        Objects.requireNonNull(values, name);
        return values.stream()
                .map(value -> Objects.requireNonNull(value, "element"))
                .toList();
    }

    private static Map<Tag, Integer> copyTagDistribution(Map<Tag, Integer> distribution) {
        Objects.requireNonNull(distribution, "noteTagDistribution");
        LinkedHashMap<Tag, Integer> copy = new LinkedHashMap<>();
        for (Map.Entry<Tag, Integer> entry : distribution.entrySet()) {
            Tag tag = Objects.requireNonNull(entry.getKey(), "tag");
            Integer count = Objects.requireNonNull(entry.getValue(), "count");
            if (count <= 0) {
                throw new IllegalArgumentException("tag count must be positive");
            }
            copy.put(tag, count);
        }
        return Collections.unmodifiableMap(copy);
    }
}
