# 测试审查报告（v2 r2）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java` — breakdown 任务失败/异常补偿用例只预置并验证了既有任务不受影响，未预置既有学习计划来验证“导入前已存在的学习计划不受影响”。详细设计的失败后正式数据状态明确要求“导入前已存在的学习计划/任务不受影响”。当前测试即使面对错误实现（例如补偿时删除所有学习计划，或删除错误的学习计划）也可能通过，导致跨模块补偿隔离契约覆盖不足。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java` 的 `rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationFails` 与 `rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationThrowsRuntimeException` 应在触发导入前先创建一个既有学习计划，并在断言中验证失败后该既有学习计划仍存在且只有本次创建的学习计划被补偿删除。可断言学习计划列表仅包含预置计划的目标名/id/progress，避免实现误删非本次数据仍通过测试。
