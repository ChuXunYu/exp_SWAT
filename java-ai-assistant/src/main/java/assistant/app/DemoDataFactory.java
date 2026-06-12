package assistant.app;

import assistant.common.DateTimeRange;
import assistant.common.OperationResult;
import assistant.task.TaskPriority;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class DemoDataFactory {
    public DemoDataFactory() {
    }

    public void load(ApplicationServices services) {
        Objects.requireNonNull(services, "services");
        LocalDate today = services.timeProvider().today();

        requireSuccess(services.taskService().createTask(
                "完成今日重点工作", "梳理今日必须完成的事项", TaskPriority.HIGH, today));
        requireSuccess(services.taskService().createTask(
                "准备明日计划", "列出明天的普通待办", TaskPriority.MEDIUM, today.plusDays(1)));

        LocalDateTime morningStart = today.atTime(9, 0);
        requireSuccess(services.scheduleService().createSchedule(
                "上午例会",
                new DateTimeRange(morningStart, morningStart.plusHours(1)),
                "会议室 A",
                "同步今日安排"));
        LocalDateTime afternoonStart = today.atTime(14, 0);
        requireSuccess(services.scheduleService().createSchedule(
                "下午复盘",
                new DateTimeRange(afternoonStart, afternoonStart.plusHours(1)),
                "线上",
                "检查任务进展"));

        requireSuccess(services.studyPlanService().createStudyPlan(
                "Java 服务层实践", today.minusDays(2), today.plusDays(4), 10, 40));
        requireSuccess(services.studyPlanService().createStudyPlan(
                "AI 助手需求整理", today.minusDays(7), today, 6, 100));

        requireSuccess(services.financeService().recordIncome(
                "8000.00", "工资", today.withDayOfMonth(1), "本月收入"));
        requireSuccess(services.financeService().recordExpense(
                "128.50", "学习", today, "课程资料"));

        requireSuccess(services.noteService().createNote(
                "今日工作记录", "记录今日任务与日程重点", tags("work", "daily")));
        requireSuccess(services.noteService().createNote(
                "学习笔记", "整理 Java AI Assistant 学习内容", tags("study", "daily")));
    }

    private void requireSuccess(OperationResult<?> result) {
        Objects.requireNonNull(result, "result");
        if (result.isFailure()) {
            throw new IllegalStateException(
                    "failed to load demo data: " + result.getErrorCode() + " - " + result.getMessage());
        }
    }

    private Set<String> tags(String... values) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        for (String value : values) {
            tags.add(value);
        }
        return tags;
    }
}
