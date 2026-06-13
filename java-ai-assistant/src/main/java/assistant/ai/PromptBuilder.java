package assistant.ai;

import assistant.common.ErrorCode;
import assistant.common.OperationResult;
import assistant.summary.LocalContext;
import java.util.List;
import java.util.Objects;

public final class PromptBuilder {
    public OperationResult<AiRequest> build(
            AiScenario scenario,
            String userQuestion,
            AiConfiguration configuration,
            LocalContext localContext) {
        Objects.requireNonNull(scenario, "scenario");
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(localContext, "localContext");
        if (userQuestion == null || userQuestion.isBlank()) {
            return OperationResult.failure(ErrorCode.VALIDATION_ERROR, "user question must not be blank");
        }

        return OperationResult.success(AiRequest.nonStreaming(
                configuration.model(),
                List.of(
                        new AiMessage(AiRole.SYSTEM, buildSystemMessage(scenario)),
                        new AiMessage(AiRole.USER, buildUserMessage(userQuestion.strip(), localContext)))));
    }

    private static String buildSystemMessage(AiScenario scenario) {
        StringBuilder builder = new StringBuilder()
                .append("你是一个本地个人助手，只能基于用户提供的问题和本地上下文回答。")
                .append("不得编造本地数据；上下文缺失时要明确说明。")
                .append(scenario.systemInstruction());
        if (scenario.requiresStructuredJson()) {
            builder.append(" 只返回单个 JSON 对象。");
            scenario.targetType().ifPresent(targetType -> builder.append("目标类型：").append(targetType).append("。"));
        }
        return builder.toString();
    }

    private static String buildUserMessage(String userQuestion, LocalContext localContext) {
        return "用户问题：\n" + userQuestion
                + "\n\n本地总览：\n" + localContext.overviewText()
                + "\n\n" + section("今日任务：", localContext.todayTaskLines())
                + "\n\n" + section("逾期未完成任务：", localContext.overdueTaskLines())
                + "\n\n" + section("未来7天高优先级任务：", localContext.upcomingHighPriorityTaskLines())
                + "\n\n" + section("今日日程：", localContext.todayScheduleLines())
                + "\n\n" + section("本周学习计划：", localContext.weekStudyPlanLines())
                + "\n\n" + section("本月收支：", localContext.monthTransactionLines())
                + "\n\n" + section("笔记标签：", localContext.noteTagLines());
    }

    private static String section(String title, List<String> lines) {
        StringBuilder builder = new StringBuilder(title);
        if (lines.isEmpty()) {
            return builder.append("\n（无）").toString();
        }
        for (String line : lines) {
            builder.append("\n- ").append(line);
        }
        return builder.toString();
    }
}
