# 详细设计（v2）

## 概述
本轮实现学习计划草稿 `breakdown` 的确认导入落地。设计范围限定在 `DraftImportService` 的学习计划导入路径、对应服务层/查询测试和受影响测试文档，不修改正式 `StudyPlan` 模型，不扩展控制台菜单，不改变 v1 已完成的结构化草稿生成入口和任务草稿 `dueDate` 保存前校验。

核心策略：
- 学习计划草稿导入成功创建正式学习计划后，将非空 `StudyPlanDraftContent.breakdown()` 中的每个项转换为正式 TODO 任务。
- 转换任务字段固定为：标题 = breakdown 项原文；描述 = `来自学习计划：{goalName}`；优先级 = `TaskPriority.MEDIUM`；`dueDate` = 学习计划草稿 `endDate`。
- `StudyPlanDraftContent` 构造器已经 strip 并过滤空白 breakdown；导入服务信任该清洗结果，不在导入时生成空任务。
- 若学习计划创建失败，直接传播失败，不创建 breakdown 任务。
- 若任一 breakdown 任务创建失败或导入过程中抛出运行时异常，回滚本次已创建的 breakdown 任务，并补偿删除本次创建的学习计划，避免用户无感知的部分落地。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/ai/DraftImportService.java` | 修改 | 在学习计划草稿导入后落地 breakdown 任务，并增加学习计划创建后的失败补偿 |
| `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java` | 修改 | 覆盖 breakdown 转任务、空 breakdown、学习计划创建失败不创建任务、breakdown 任务失败补偿 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 必要时修改 | 如现有确认导入学习计划控制台链路可查询正式任务，则补充用户确认后任务列表可见 breakdown 的场景；若服务层覆盖已足够，可不改 |
| `java-ai-assistant/docs/test-plan.md` | 修改 | 更新 AI 结构化建议确认导入覆盖重点，说明 breakdown 确认导入后转正式任务 |
| `java-ai-assistant/docs/test-cases.md` | 修改 | 更新 DRAFT 用例和跨模块链路，加入学习计划 breakdown 导入到任务服务的验证 |
| `java-ai-assistant/docs/coverage/README.md` | 修改 | 更新 `DraftImportService.importDraft` 控制流、独立路径、映射用例和圈复杂度说明 |
| `java-ai-assistant/README.md` | 可选修改 | 若 README 的 AI drafts 简述需要更精确，可补充学习计划 breakdown 确认导入会创建待办步骤 |

## 类型定义

### DraftImportService
**形态**：`final class`
**包路径**：`assistant.ai`
**职责**：导入确认后的 AI 草稿到正式任务/学习计划模块，并对批量/跨模块写入失败执行补偿。

**现有字段保持不变**：
```java
private final TaskService taskService;
private final StudyPlanService studyPlanService;
```

**构造方式保持不变**：
```java
public DraftImportService(TaskService taskService, StudyPlanService studyPlanService)
```

**公开接口保持不变**：
```java
public OperationResult<Void> importDraft(SuggestionDraft draft)
```

**修改的私有方法签名**：
```java
private OperationResult<Void> importStudyPlan(SuggestionDraft draft)
```

**新增私有方法签名**：
```java
private List<TaskDraftItem> toBreakdownTasks(StudyPlanDraftContent content)

private TaskDraftItem toBreakdownTask(StudyPlanDraftContent content, String breakdownItem)

private OperationResult<Void> createBreakdownTasks(StudyPlanView studyPlan, List<TaskDraftItem> tasks)

private void rollbackStudyPlan(EntityId id)
```

**复用的现有私有方法**：
```java
private OperationResult<Void> createTasks(List<TaskDraftItem> tasks)

private void rollbackCreatedTasks(List<EntityId> createdIds)

private OperationResult<Void> toFailure(OperationResult<?> result)

private OperationResult<Void> businessFailure(BusinessException exception)

private OperationResult<Void> systemFailure(RuntimeException exception)
```

**类型关系**：
- 继续组合 `TaskService`：创建 breakdown 对应的正式 TODO 任务，并复用 `deleteTask(EntityId)` 做任务补偿。
- 继续组合 `StudyPlanService`：创建正式学习计划，并用 `deleteStudyPlan(EntityId)` 做学习计划补偿。
- 新增依赖 import：`assistant.task.TaskPriority`。
- 继续使用已有 `TaskDraftItem` 作为内部转换中间结构，避免新增 DTO。

### TaskDraftItem
**形态**：`record`
**包路径**：`assistant.ai`
**职责**：表示草稿任务项；本轮仅作为 breakdown 转正式任务的内部中间结构使用。

**构造方式**：
```java
new TaskDraftItem(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate)
```

**本轮生成规则**：
- `title`：使用清洗后的 breakdown 项原文。
- `description`：固定为 `来自学习计划：` + `content.goalName()`。
- `priority`：固定为 `TaskPriority.MEDIUM`。
- `dueDate`：固定为 `content.endDate()`。

## 错误处理
- `importDraft(null)` 的现有 `VALIDATION_ERROR / "draft must not be null"` 行为保持不变。
- 任务草稿导入的 dueDate 前置校验、批量创建、失败回滚和错误传播保持不变。
- 学习计划创建失败时：
  - 返回 `toFailure(studyPlanResult)`。
  - 不调用 `toBreakdownTasks(...)`。
  - 不创建任何 breakdown 任务。
- 学习计划创建成功且 breakdown 为空时：
  - 不创建任务。
  - 直接返回 `OperationResult.success()`。
- 学习计划创建成功且 breakdown 非空时：
  - 将 breakdown 转换为任务列表后调用 `createBreakdownTasks(studyPlanView, tasks)`。
  - 任一任务创建返回失败时，`createTasks(...)` 先回滚本次已创建任务；随后 `createBreakdownTasks(...)` 调用 `rollbackStudyPlan(studyPlan.id())`；最终返回原任务创建失败的 `ErrorCode` 和 message。
  - 任一任务创建抛出 `RuntimeException` 时，`createTasks(...)` 先回滚本次已创建任务并重新抛出；`createBreakdownTasks(...)` 必须在 `catch (RuntimeException exception)` 中调用 `rollbackStudyPlan(studyPlan.id())` 后再次抛出，让外层 `importDraft(...)` 映射为 `SYSTEM_ERROR / "failed to import suggestion draft"`。
- `rollbackStudyPlan(EntityId id)` 为 best-effort：
  - 调用 `studyPlanService.deleteStudyPlan(id)`。
  - 吞掉运行时异常和失败结果，不覆盖原始导入失败。
- 若补偿删除学习计划失败，仍返回原始任务创建失败或系统失败；不新增错误类型。

## 行为契约
- `importStudyPlan(draft)` 调用顺序：
  1. 读取 `StudyPlanDraftContent content = draft.getStudyPlan().orElseThrow(...)`。
  2. 调用 `studyPlanService.createStudyPlan(content.goalName(), content.startDate(), content.endDate(), content.expectedHours(), content.initialProgress().value())`。
  3. 若学习计划创建失败，返回原失败。
  4. 将 `content.breakdown()` 转为 `List<TaskDraftItem>`。
  5. 若任务列表为空，返回成功。
  6. 调用 `createBreakdownTasks(studyPlanResult.getPayload(), tasks)`。
- `toBreakdownTasks(content)` 必须保持列表顺序，逐个映射 `content.breakdown()`。
- `toBreakdownTask(content, breakdownItem)` 不做随机或时间相关推导；所有生成字段只来自 `content` 和固定常量。
- 导入成功后正式数据状态：
  - `studyPlanService.listStudyPlans()` 可查询到 1 个新学习计划。
  - `taskService.listTasks()` 或 `taskService.listTasks(TaskQuery.byDueDate(content.endDate()))` 可查询到与 breakdown 数量一致的新任务。
  - 新任务均为 TODO，优先级为 MEDIUM，dueDate 为学习计划 endDate，描述包含来源学习计划目标。
- 导入失败后正式数据状态：
  - 学习计划创建失败：学习计划和任务均不增加。
  - breakdown 任务创建失败或异常：本次学习计划和本次已创建 breakdown 任务均被补偿删除；导入前已存在的学习计划/任务不受影响。
- `DraftLifecycleService.confirmDraft(...)` 行为不变：只有 `importDraft(...)` 成功后才标记草稿为 `IMPORTED`；导入失败时草稿仍保持 `CONFIRMABLE`。

## 依赖关系
- 依赖已有类型：
  - `assistant.ai.SuggestionDraft`
  - `assistant.ai.StudyPlanDraftContent`
  - `assistant.ai.TaskDraftItem`
  - `assistant.common.EntityId`
  - `assistant.common.ErrorCode`
  - `assistant.common.OperationResult`
  - `assistant.study.StudyPlanService`
  - `assistant.study.StudyPlanView`
  - `assistant.task.TaskPriority`
  - `assistant.task.TaskService`
  - `assistant.task.TaskView`
- 暴露给后续任务的公开接口：
  - 无新增公开接口。
  - `DraftImportService.importDraft(SuggestionDraft draft)` 的学习计划草稿语义扩展为“创建学习计划，并将 breakdown 转为正式任务”。
- 不修改：
  - `StudyPlan` / `StudyPlanView` 模型。
  - `StudyPlanDraftContent` 清洗规则。
  - `StructuredSuggestionParser.parseBreakdown(...)`。
  - `ConsoleApplication.printStudyPlanDraftContent(...)`。
  - `ApplicationServices` 和 `ApplicationFactory` 装配签名。

## 测试设计

### DraftImportServiceTest
**修改测试类**：`assistant.ai.DraftImportServiceTest`

**新增或调整支撑类型**：
```java
private static StudyPlanDraftContent studyPlanWithBreakdown(List<String> breakdown)

private static StudyPlanDraftContent studyPlanWithoutBreakdown()
```

如需让同一测试同时查询任务和学习计划，直接在测试内创建共享 `InMemoryTaskRepository`、`TaskService`、`InMemoryStudyPlanRepository`、`StudyPlanService`，不要通过只返回单服务的 helper 隐藏依赖。

**用例要求**：
- `importsStudyPlanDraftCreatesTasksForBreakdown`：
  - 输入：学习计划草稿 `goalName = "Learn Java"`，`endDate = JULY_31`，breakdown 为 `["Syntax", "Testing"]`。
  - 断言：导入成功；学习计划数量为 1；任务数量为 2；任务标题顺序为 `["Syntax", "Testing"]`；任务描述均为 `来自学习计划：Learn Java`；优先级均为 `TaskPriority.MEDIUM`；dueDate 均为 `JULY_31`；状态均为 TODO；草稿状态仍为 `CONFIRMABLE`。
- `importsStudyPlanDraftWithoutBreakdownCreatesOnlyStudyPlan`：
  - 输入：breakdown 为 `List.of()`。
  - 断言：导入成功；学习计划数量为 1；任务列表为空。
- `importsStudyPlanDraftIgnoresBlankBreakdownItemsCleanedByContent`：
  - 输入：构造 `StudyPlanDraftContent` 时 breakdown 为 `[" Syntax ", " ", "Testing"]`。
  - 断言：导入后只创建 `Syntax` 和 `Testing` 两个任务，不创建空标题任务。
- `propagatesStudyPlanCreationFailureWithoutCreatingBreakdownTasks`：
  - 使用 mock `StudyPlanService` 让 `createStudyPlan(...)` 返回失败。
  - 断言：返回原错误；`taskService.listTasks()` 为空。
- `rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationFails`：
  - 使用现有 `FailingTaskRepository`，让第二个 breakdown 任务保存返回/抛出可被 `TaskService.createTask(...)` 捕获为 `VALIDATION_ERROR` 的 `IllegalArgumentException`。
  - 断言：返回 `VALIDATION_ERROR` 和原 message；本次学习计划被删除；本次已创建 breakdown 任务被删除；导入前已存在任务仍保留。
- `rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationThrowsRuntimeException`：
  - 使用现有 `FailingTaskRepository`，让第二个 breakdown 任务保存抛 `IllegalStateException`。
  - 断言：返回 `SYSTEM_ERROR / "failed to import suggestion draft"`；学习计划列表为空；本次已创建 breakdown 任务被删除；导入前已存在任务仍保留。

**既有用例调整**：
- `importsStudyPlanDraft` 当前使用带 `["Syntax", "Testing"]` 的 helper 但只断言学习计划；应改为上述 `importsStudyPlanDraftCreatesTasksForBreakdown`，或将 helper 改成无 breakdown，避免测试名和断言不匹配。
- `constructorRejectsNullDependencies` 不变。
- 任务草稿导入相关测试不变。

### ConsoleApplicationTest
**可选增强测试**：如果实现者选择补充用户路径验证，可在已有草稿确认链路基础上增加：
- `generatedStudyPlanDraftConfirmCreatesBreakdownTasksVisibleInTaskList`：
  - fake AI 返回学习计划草稿 JSON，含 breakdown。
  - 用户在草稿菜单生成学习计划草稿并确认导入。
  - 再进入任务列表，断言输出包含 breakdown 任务标题。

若服务层测试已经覆盖正式模块查询，且控制台确认导入已有回归用例，本轮可不新增控制台测试，避免扩大 `ConsoleApplicationTest`。

### 文档一致性测试
如修改 `docs/test-cases.md` 或 `docs/coverage/README.md` 中引用的测试方法名，必须同步满足 `DocumentationDeliveryTest.documentationReferencesExistingJUnitTests`：
- 文档中新增的 `DraftImportServiceTest` 方法名必须真实存在。
- 覆盖文档中的独立路径编号和测试类名必须仍能被正则扫描到。

## 文档更新设计
- `docs/test-plan.md`：
  - 将 AI 结构化建议确认导入覆盖重点从“breakdown 保留”更新为“breakdown 保留并确认导入后转正式任务”。
- `docs/test-cases.md`：
  - 更新 `DRAFT-09` 或新增 `DRAFT-16`，说明学习计划草稿含 breakdown 时导入后正式任务服务可见拆解步骤。
  - 更新跨模块链路“AI 学习计划草稿确认导入到学习计划服务”为“确认导入到学习计划服务，并将 breakdown 同步为任务服务可见的待办步骤”。
- `docs/coverage/README.md`：
  - `DraftImportService.importDraft` 控制流新增：学习计划成功后判断 breakdown；breakdown 任务创建；任务失败时回滚任务并补偿删除学习计划。
  - 独立路径新增：
    - 学习计划无 breakdown 成功。
    - 学习计划含 breakdown 成功并创建任务。
    - breakdown 任务创建失败后补偿学习计划。
    - breakdown 任务创建抛运行时异常后补偿学习计划并映射系统失败。
  - 圈复杂度估算相应调高，具体数值由实现者按最终控制流更新。
- `README.md`：
  - 可将 AI drafts 描述调整为包含“study plan breakdown steps are imported as todo tasks”，但若不改 README，需确认现有文档测试不会要求该精度。
