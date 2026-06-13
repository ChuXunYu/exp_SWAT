package assistant.summary;

import assistant.common.Tag;
import assistant.finance.FinanceStatistics;
import assistant.finance.TransactionView;
import assistant.schedule.ScheduleView;
import assistant.study.StudyPlanView;
import assistant.task.TaskView;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record LocalContext(
        DashboardSummary dashboardSummary,
        String overviewText,
        List<String> todayTaskLines,
        List<String> overdueTaskLines,
        List<String> upcomingHighPriorityTaskLines,
        List<String> todayScheduleLines,
        List<String> weekStudyPlanLines,
        List<String> monthTransactionLines,
        List<String> noteTagLines) {
    public LocalContext {
        Objects.requireNonNull(dashboardSummary, "dashboardSummary");
        overviewText = Objects.requireNonNull(overviewText, "overviewText").strip();
        if (overviewText.isBlank()) {
            throw new IllegalArgumentException("overviewText must not be blank");
        }
        todayTaskLines = copyLines(todayTaskLines, "todayTaskLines");
        overdueTaskLines = copyLines(overdueTaskLines, "overdueTaskLines");
        upcomingHighPriorityTaskLines = copyLines(upcomingHighPriorityTaskLines, "upcomingHighPriorityTaskLines");
        todayScheduleLines = copyLines(todayScheduleLines, "todayScheduleLines");
        weekStudyPlanLines = copyLines(weekStudyPlanLines, "weekStudyPlanLines");
        monthTransactionLines = copyLines(monthTransactionLines, "monthTransactionLines");
        noteTagLines = copyLines(noteTagLines, "noteTagLines");
    }

    public static LocalContext from(DashboardSummary dashboardSummary) {
        Objects.requireNonNull(dashboardSummary, "dashboardSummary");
        return new LocalContext(
                dashboardSummary,
                buildOverviewText(dashboardSummary),
                dashboardSummary.todayTasks().stream().map(LocalContext::taskLine).toList(),
                dashboardSummary.overdueTasks().stream().map(LocalContext::taskLine).toList(),
                dashboardSummary.upcomingHighPriorityTasks().stream().map(LocalContext::taskLine).toList(),
                dashboardSummary.todaySchedules().stream().map(LocalContext::scheduleLine).toList(),
                dashboardSummary.weekStudyPlans().stream().map(LocalContext::studyPlanLine).toList(),
                dashboardSummary.monthTransactions().stream().map(LocalContext::transactionLine).toList(),
                dashboardSummary.noteTagDistribution().entrySet().stream().map(LocalContext::tagLine).toList());
    }

    private static String buildOverviewText(DashboardSummary summary) {
        FinanceStatistics statistics = summary.monthFinanceStatistics();
        return "今日任务" + summary.todayTasks().size()
                + "项，逾期未完成任务" + summary.overdueTasks().size()
                + "项，未来7天高优先级任务" + summary.upcomingHighPriorityTasks().size()
                + "项，今日日程" + summary.todaySchedules().size()
                + "项，本周学习计划" + summary.weekStudyPlans().size()
                + "项（已完成" + summary.completedWeekStudyPlanCount()
                + "项，未完成" + summary.incompleteWeekStudyPlanCount()
                + "项），本月收入" + statistics.totalIncome().value().toPlainString()
                + "，支出" + statistics.totalExpense().value().toPlainString()
                + "，结余" + statistics.balance().value().toPlainString()
                + "，笔记" + summary.noteCount()
                + "篇，标签" + summary.noteTagDistribution().size()
                + "个。";
    }

    private static String taskLine(TaskView task) {
        return "任务：" + task.title()
                + "｜优先级：" + task.priority().name()
                + "｜状态：" + task.status().name()
                + "｜截止：" + task.dueDate();
    }

    private static String scheduleLine(ScheduleView schedule) {
        return "日程：" + schedule.name()
                + "｜状态：" + schedule.status().name()
                + "｜时间：" + schedule.startDateTime()
                + "~" + schedule.endDateTime()
                + "｜地点：" + schedule.location();
    }

    private static String studyPlanLine(StudyPlanView plan) {
        return "学习：" + plan.goalName()
                + "｜状态：" + plan.status().name()
                + "｜进度：" + plan.progress().value()
                + "%｜周期：" + plan.startDate()
                + "~" + plan.endDate();
    }

    private static String transactionLine(TransactionView transaction) {
        return "收支：" + transaction.type().name()
                + "｜金额：" + transaction.amount().value().toPlainString()
                + "｜类别：" + transaction.category()
                + "｜日期：" + transaction.date();
    }

    private static String tagLine(Map.Entry<Tag, Integer> entry) {
        return "标签：" + entry.getKey().displayName() + "｜数量：" + entry.getValue();
    }

    private static List<String> copyLines(List<String> lines, String name) {
        Objects.requireNonNull(lines, name);
        return lines.stream().map(LocalContext::requireLine).toList();
    }

    private static String requireLine(String line) {
        String requiredLine = Objects.requireNonNull(line, "line").strip();
        if (requiredLine.isBlank()) {
            throw new IllegalArgumentException("line must not be blank");
        }
        return requiredLine;
    }
}
