# 任务指令（v2）

## 动作
NEW

## 任务描述
实现学习计划草稿 `breakdown` 的确认导入落地：修改 `java-ai-assistant/src/main/java/assistant/ai/DraftImportService.java`，导入 `STUDY_PLAN_DRAFT` 时不能再静默丢弃非空 `StudyPlanDraftContent.breakdown()`；建议将每个 breakdown 项转换为正式 TODO 任务，并确保用户导入后可通过任务服务查询这些拆解步骤。

预期同步补充或更新：
- `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java`
- 必要时补充 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 或服务查询测试，证明确认导入后正式模块可见 breakdown 落地结果
- `java-ai-assistant/docs/test-plan.md`
- `java-ai-assistant/docs/test-cases.md`
- 其他受影响 README/验收说明，若行为描述发生变化

## 选择理由
v1 已完成 AI 结构化草稿生成入口和任务草稿 dueDate 保存前一致性，验证报告为 PASSED。剩余本轮必须修复项中，学习计划草稿 breakdown 导入丢失直接影响 AI 生成学习计划的核心价值，而且当前解析、保存、展示链路已经具备，只缺确认导入时的正式数据落地。

## 任务上下文
需求要求：
- 带有 breakdown 的学习计划草稿确认导入后，用户能在正式数据中看到这些拆解步骤的落地结果。
- breakdown 中的空字符串或空白项应被忽略或在解析阶段清洗，不应生成空任务或空步骤。
- 无 breakdown 的学习计划草稿仍可正常导入为正式学习计划。
- 如果 breakdown 落地过程中任一正式数据创建失败，导入结果必须可解释，不能出现学习计划已创建但步骤部分丢失且用户无感知。
- 若将 breakdown 转为任务，任务标题、描述、优先级和 dueDate 的生成规则必须稳定；dueDate 应遵守本轮 dueDate 规则。

建议实现策略：
- 采用“breakdown 转任务”而不是扩展 `StudyPlan` 模型，以降低模型变更范围并复用现有任务查询能力。
- 每个 breakdown 项创建一个 TODO 任务。
- 任务标题使用 breakdown 项原文。
- 任务描述稳定包含来源学习计划目标，例如 `来自学习计划：{goalName}`。
- 任务优先级使用固定默认值 `TaskPriority.MEDIUM`。
- 任务 dueDate 使用学习计划草稿 `endDate`，确保符合正式任务 dueDate 必填规则。
- 空 breakdown 继续只创建学习计划。
- 为避免静默丢失，导入时应对将要创建的任务先进行可预测校验；任务创建失败时应返回原错误或清晰错误，并补偿已创建的任务。若学习计划已创建后任务创建失败，还需要补偿学习计划，或调整写入顺序以保证不会留下用户无感知的部分结果。

## 已有代码上下文
- `StudyPlanDraftContent` 已在构造阶段 strip 并过滤空白 breakdown；`hasBreakdown()` 可判断是否存在有效拆解项。
- `StructuredSuggestionParser.parseBreakdown()` 已支持字符串数组并过滤空项。
- `StructuredSuggestionDraftService.generateStudyPlanDraft(...)` 已保存包含 breakdown 的学习计划草稿。
- `ConsoleApplication.printStudyPlanDraftContent(...)` 已展示草稿 breakdown。
- `DraftImportService.importStudyPlan(...)` 当前只调用：
  `studyPlanService.createStudyPlan(content.goalName(), content.startDate(), content.endDate(), content.expectedHours(), content.initialProgress().value())`
  因此不会落地 `content.breakdown()`。
- `DraftImportService.createTasks(...)` 已有批量任务创建和 best-effort rollback 逻辑，可复用或抽取给 breakdown 转任务使用。
- `StudyPlanService` 已有 `deleteStudyPlan(EntityId id)`，可用于学习计划创建后任务落地失败时的补偿。
- `TaskService.createTask(...)` 返回 `TaskView`，其 id 可用于失败回滚。
- 现有 `DraftImportServiceTest.importsStudyPlanDraft` 使用带 `List.of("Syntax", "Testing")` 的 studyPlan 草稿，但目前只断言学习计划被创建，未断言 breakdown 落地。

