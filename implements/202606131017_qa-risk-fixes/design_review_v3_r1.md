# 设计审查报告（v3 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** — 设计没有把新增的 `LocalContext.overdueTaskLines()` 和 `LocalContext.upcomingHighPriorityTaskLines()` 接入 `PromptBuilder`。当前 `PromptBuilder.buildUserMessage(...)` 只输出 overview、今日任务、今日日程、本周学习计划、本月收支和笔记标签段。按本设计编码后，AI 本地上下文 record 虽然会持有逾期和未来 7 天高优先级任务行，但 AI 请求 prompt 仍不会包含这些明细，只能从 overview 中看到计数，不能让 AI 感知具体紧急任务信息。需求明确要求“AI 本地上下文包含逾期和未来高优先级任务信息”，且风险背景是 AI 建议要能感知这些紧急事项；只更新 `LocalContext` 而不更新 prompt 消费方会留下生产链路缺口。

## 修改要求

- 补充 `assistant.ai.PromptBuilder` 的生产代码设计：在 `buildUserMessage(...)` 中新增清晰的 `逾期未完成任务：` 和 `未来7天高优先级任务：` 段落，分别输出 `localContext.overdueTaskLines()` 与 `localContext.upcomingHighPriorityTaskLines()`，空列表沿用现有 `section(...)` 的 `（无）` 语义。
- 将 `PromptBuilderTest.buildIncludesOverviewAndAllContextSections` 和空上下文用例从“可顺手补充”调整为必须覆盖新增 prompt 段落，确保 AI 请求实际携带新增紧急任务信息。
- 在文件规划、行为契约和文档更新设计中同步列出 `java-ai-assistant/src/main/java/assistant/ai/PromptBuilder.java` 与对应测试/文档影响，避免后续编码只改 `LocalContext` 而遗漏 AI prompt 出口。
