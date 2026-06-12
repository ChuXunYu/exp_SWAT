# 测试报告（v21）

## 概述

本轮围绕 `assistant.ai` 包新增的草稿导入服务与草稿生命周期服务补充单元测试。测试依据 `detail_v21.md` 的公开行为契约编写，验证 AI 结构化建议草稿从 `CONFIRMABLE` 状态到取消或确认导入正式任务/学习计划的服务闭环。

测试关注服务边界行为，不验证私有实现细节；正式任务与学习计划写入通过真实 `TaskService`、`StudyPlanService` 和内存仓储覆盖成功路径，失败路径使用 Mockito 或测试内 fake 仓储注入可控错误。

## 测试文件

| 文件路径 | 覆盖范围 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java` | 覆盖草稿导入正式任务/学习计划、任务导入预校验、目标服务失败透传、任务批量导入回滚、null 防御和构造依赖校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/DraftLifecycleServiceTest.java` | 覆盖草稿查询、列表快照与不可修改列表、取消、确认导入、终态重复操作、缺失草稿、导入失败状态保持和构造依赖校验。 |

## 本轮补充

- 新增 `DraftImportServiceTest`，共 8 个用例：
  - `importsAllTaskDraftItems`
  - `rejectsTaskDraftMissingDueDateBeforeCreatingAnyTask`
  - `rollsBackCreatedTasksWhenTaskCreationFails`
  - `rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException`
  - `importsStudyPlanDraft`
  - `propagatesStudyPlanCreationFailure`
  - `importDraftRejectsNullDraft`
  - `constructorRejectsNullDependencies`
- 新增 `DraftLifecycleServiceTest`，共 14 个用例：
  - `getDraftReturnsViewSnapshot`
  - `getDraftReturnsNotFoundForMissingDraft`
  - `getDraftRejectsNullId`
  - `listDraftsReturnsUnmodifiableViewSnapshots`
  - `cancelDraftMarksDraftCancelledAndSaves`
  - `cancelDraftReturnsNotFoundForMissingDraft`
  - `cancelDraftRejectsTerminalDrafts`
  - `confirmDraftImportsAndMarksDraftImported`
  - `confirmDraftReturnsNotFoundForMissingDraft`
  - `confirmDraftRejectsTerminalDraftsWithoutImporting`
  - `confirmDraftKeepsDraftConfirmableWhenImportFails`
  - `confirmDraftRejectsNullId`
  - `cancelDraftRejectsNullId`
  - `constructorRejectsNullDependencies`
- 在 `DraftImportServiceTest` 中新增测试专用 `FailingTaskRepository`，委托真实 `InMemoryTaskRepository`，仅在指定保存次数抛出可控异常，用于验证通过真实 `TaskService.deleteTask(...)` 回滚本次已创建任务。

## 设计契约覆盖

- `DraftImportService.importDraft(null)` 返回 `VALIDATION_ERROR`，消息为 `"draft must not be null"`。
- `TASK_DRAFT` 导入前完整校验所有任务草稿的 `dueDate`，任一为空时返回 `VALIDATION_ERROR`，不创建任何正式任务，草稿保持 `CONFIRMABLE`。
- `TASK_DRAFT` 导入按草稿任务顺序调用正式任务服务；全部成功时正式任务列表包含所有导入任务，草稿状态不由导入服务修改。
- 任务批量导入中出现 `TaskService.createTask(...)` 失败结果时，回滚本次已创建任务，保留导入前已有任务，并透传原失败错误码和消息。
- 任务批量导入中出现不可预期运行时异常时，回滚本次已创建任务，保留导入前已有任务，并返回 `SYSTEM_ERROR` 与固定消息 `"failed to import suggestion draft"`。
- `STUDY_PLAN_DRAFT` 导入调用正式学习计划服务创建学习计划；成功时正式学习计划列表新增记录，草稿状态不由导入服务修改。
- 学习计划创建失败时导入服务透传错误码和消息，草稿保持 `CONFIRMABLE`。
- `DraftLifecycleService.getDraft(null)`、`cancelDraft(null)`、`confirmDraft(null)` 均返回 `VALIDATION_ERROR`，消息为 `"id must not be null"`。
- 查询、取消、确认不存在草稿均返回 `NOT_FOUND`，消息为 `"suggestion draft not found: " + id.value()`。
- `getDraft(id)` 返回 `SuggestionDraftView` 快照；草稿后续状态变化不改变旧视图。
- `listDrafts()` 返回调用时刻的视图快照列表；列表本身不可修改，后续新增草稿或修改草稿状态不改变旧列表中已有视图。
- `cancelDraft(id)` 成功时只取消草稿并保存仓储，不调用导入服务；返回 `CANCELLED` 视图。
- 对已取消或已导入草稿再次取消返回 `STATE_CONFLICT`，不调用导入服务。
- `confirmDraft(id)` 在草稿可确认时调用导入服务；导入成功后标记 `IMPORTED` 并保存仓储，返回 `IMPORTED` 视图。
- 重复确认、取消后确认和已导入后确认均返回 `STATE_CONFLICT`，不调用导入服务，不新增正式记录。
- 导入失败时 `confirmDraft(id)` 透传导入失败错误码和消息，草稿保持 `CONFIRMABLE`，后续仍可再次确认。
- `DraftImportService` 与 `DraftLifecycleService` 构造器对 null 依赖抛出带参数名消息的 `NullPointerException`。

## 验证说明

按测试编写 Agent 指令，本轮职责是编写测试。为交付前自检，在子项目目录 `/root/exp_SWAT/java-ai-assistant` 执行：

```text
mvn -Dtest=DraftImportServiceTest,DraftLifecycleServiceTest test
```

结果：`BUILD SUCCESS`，共 `22` 个测试通过，`0` failures，`0` errors，`0` skipped。
