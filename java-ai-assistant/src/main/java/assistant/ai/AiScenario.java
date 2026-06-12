package assistant.ai;

import java.util.Optional;

public enum AiScenario {
    GENERAL_QA("你是个人学习与生活助手，请直接回答用户的问题。", null),
    STUDY_ADVICE("请基于本地学习计划、任务和日程给出可执行的学习建议。", null),
    NOTE_SUMMARY("请基于本地笔记标签和摘要上下文给出笔记总结建议。", null),
    STRUCTURED_TASK_SUGGESTION("请生成待办任务草稿建议。", "TASK_DRAFT"),
    STRUCTURED_STUDY_PLAN_SUGGESTION("请生成学习计划草稿建议。", "STUDY_PLAN_DRAFT");

    private final String systemInstruction;
    private final String targetType;

    AiScenario(String systemInstruction, String targetType) {
        this.systemInstruction = systemInstruction;
        this.targetType = targetType;
    }

    public String systemInstruction() {
        if (targetType == null) {
            return systemInstruction;
        }
        return systemInstruction + " 只返回单个 JSON 对象，目标类型为 " + targetType + "。";
    }

    public boolean requiresStructuredJson() {
        return targetType != null;
    }

    public Optional<String> targetType() {
        return Optional.ofNullable(targetType);
    }
}
