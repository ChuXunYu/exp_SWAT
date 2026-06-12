# 详细设计（v21）

## 概述

本轮设计目标是在 `assistant.ai` 包中新增草稿导入服务与草稿生命周期服务，补齐 AI 结构化建议从“可确认草稿”到“用户取消或确认导入正式记录”的服务闭环。

本轮实现范围：

- `DraftImportService`：只负责把一个仍处于可导入语义的 `SuggestionDraft` 转换为正式任务或正式学习计划。正式数据写入必须通过 `TaskService` 或 `StudyPlanService`，不得直接访问任务或学习计划仓储。
- `DraftLifecycleService`：负责按草稿 id 查询、列表、取消和确认导入；统一处理草稿不存在、终态重复操作、导入成功后标记和保存草稿、导入失败时保持草稿状态。
- 对应单元测试覆盖查询快照、列表不可修改、取消、确认成功、重复操作、导入失败状态保持、任务导入预校验和回滚。

职责边界：

- `DraftImportService.importDraft(...)` 导入成功只表示正式业务数据已成功写入，返回 `OperationResult<Void>`；它不修改或保存草稿状态。
- `DraftLifecycleService.confirmDraft(...)` 在导入成功后调用 `SuggestionDraft.markImported()` 并保存草稿，再返回导入后 `SuggestionDraftView`。
- 导入失败、校验失败或运行时异常路径均不得调用 `markImported()`，草稿保持 `CONFIRMABLE`。

本轮不实现：

- AI 服务自动生成并保存草稿。
- 控制台菜单、用户输入解析或展示文本。
- 真实 DeepSeek 调用、网络访问、数据库、文件持久化或跨进程草稿持久化。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/ai/DraftImportService.java` | 新建 | 根据草稿类型调用正式任务或学习计划服务导入数据，并处理任务批量导入回滚。 |
| `java-ai-assistant/src/main/java/assistant/ai/DraftLifecycleService.java` | 新建 | 提供草稿查看、列表、取消和确认导入入口，返回只读视图和统一失败结果。 |
| `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java` | 新建 | 覆盖任务和学习计划导入成功、校验失败、目标服务失败、异常回滚和 null 防御路径。 |
| `java-ai-assistant/src/test/java/assistant/ai/DraftLifecycleServiceTest.java` | 新建 | 覆盖查询、列表、取消、确认、重复操作、缺失草稿和视图快照不可变。 |

## 类型定义

### `DraftImportService`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：把 `SuggestionDraft` 内容导入正式业务模块，并保证任务批量导入失败时正式任务数据不出现本次半写入。

**类型签名定义**：

```java
public final class DraftImportService
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final TaskService taskService` | 构造时非空；任务草稿导入唯一正式写入口。 |
| `private final StudyPlanService studyPlanService` | 构造时非空；学习计划草稿导入唯一正式写入口。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DraftImportService(TaskService taskService, StudyPlanService studyPlanService)` | 构造器 | `taskService == null` 抛 `NullPointerException("taskService")`；`studyPlanService == null` 抛 `NullPointerException("studyPlanService")`。 |
| `public OperationResult<Void> importDraft(SuggestionDraft draft)` | `OperationResult<Void>` | `draft == null` 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "draft must not be null")`；`TASK_DRAFT` 委托任务导入；`STUDY_PLAN_DRAFT` 委托学习计划导入；捕获服务边界可预期异常并返回失败结果。 |

**私有辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private OperationResult<Void> importTasks(SuggestionDraft draft)` | `OperationResult<Void>` | 导入 `draft.getTasks()` 中所有任务；导入前完整校验所有 `TaskDraftItem.dueDate()` 非空；成功返回 `OperationResult.success()`。 |
| `private OperationResult<Void> validateTaskDueDates(List<TaskDraftItem> tasks)` | `OperationResult<Void>` | 任一任务截止日期为空时返回 `VALIDATION_ERROR`，消息固定为 `"task draft dueDate must not be null"`；调用方在该失败结果下不得调用 `TaskService.createTask(...)`。 |
| `private OperationResult<Void> createTasks(List<TaskDraftItem> tasks)` | `OperationResult<Void>` | 逐项调用 `taskService.createTask(title, description, priority, dueDate)`；记录本次已成功创建的 `EntityId`；任一创建失败时回滚已创建任务并返回原失败错误码和消息。 |
| `private OperationResult<Void> importStudyPlan(SuggestionDraft draft)` | `OperationResult<Void>` | 读取 `draft.getStudyPlan()`，调用 `studyPlanService.createStudyPlan(goalName, startDate, endDate, expectedHours, initialProgress.value())`；失败时透传错误码和消息，成功返回空成功结果。 |
| `private void rollbackCreatedTasks(List<EntityId> createdIds)` | `void` | 对本次成功创建的任务 id 逐个调用 `taskService.deleteTask(id)`；忽略删除失败结果，不覆盖主失败原因。 |
| `private OperationResult<Void> toFailure(OperationResult<?> result)` | `OperationResult<Void>` | 将其他载荷类型的失败结果转换为 `OperationResult<Void>`，保留 `errorCode` 与 `message`。 |
| `private OperationResult<Void> businessFailure(BusinessException exception)` | `OperationResult<Void>` | 返回 `OperationResult.failure(exception.getErrorCode(), exception.getMessage())`。 |
| `private OperationResult<Void> systemFailure(RuntimeException exception)` | `OperationResult<Void>` | 返回 `OperationResult.failure(ErrorCode.SYSTEM_ERROR, "failed to import suggestion draft")`。 |

**构造方式**：

- 应用装配时传入已有 `TaskService` 与 `StudyPlanService`。
- 单元测试使用真实本地服务、内存仓储和递增编号生成器覆盖成功路径。
- 任务导入失败和运行时异常回滚测试使用真实 `TaskService` 配合测试内可控失败 `TaskRepository`，通过 `TaskService.listTasks()` 断言正式任务状态无本次半写入。
- 学习计划创建失败测试可使用 Mockito mock `StudyPlanService` 返回失败结果。

**类型关系**：

- 依赖 `assistant.task.TaskService`、`assistant.task.TaskView`、`assistant.study.StudyPlanService`、`assistant.study.StudyPlanView`。
- 依赖 `assistant.common.OperationResult`、`ErrorCode`、`BusinessException`、`EntityId`。
- 使用 `SuggestionDraft`、`SuggestionDraftType`、`TaskDraftItem`、`StudyPlanDraftContent`。

### `DraftLifecycleService`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：作为控制台层可调用的草稿生命周期入口，通过草稿 id 查询、取消或确认草稿，并保证外部只获得 `SuggestionDraftView` 快照。

**类型签名定义**：

```java
public final class DraftLifecycleService
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final SuggestionDraftRepository repository` | 构造时非空；草稿读取和状态保存唯一边界。 |
| `private final DraftImportService importService` | 构造时非空；确认导入唯一委托边界。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DraftLifecycleService(SuggestionDraftRepository repository, DraftImportService importService)` | 构造器 | `repository == null` 抛 `NullPointerException("repository")`；`importService == null` 抛 `NullPointerException("importService")`。 |
| `public OperationResult<SuggestionDraftView> getDraft(EntityId id)` | `OperationResult<SuggestionDraftView>` | `id == null` 返回 `VALIDATION_ERROR`、消息 `"id must not be null"`；草稿不存在返回 `NOT_FOUND`；存在时返回 `SuggestionDraftView.from(draft)`。 |
| `public OperationResult<List<SuggestionDraftView>> listDrafts()` | `OperationResult<List<SuggestionDraftView>>` | 返回当前仓储中所有草稿的视图快照列表；成功载荷列表不可修改；不得暴露 `SuggestionDraft` 引用。 |
| `public OperationResult<SuggestionDraftView> cancelDraft(EntityId id)` | `OperationResult<SuggestionDraftView>` | `id == null` 返回 `VALIDATION_ERROR`；草稿不存在返回 `NOT_FOUND`；草稿非 `CONFIRMABLE` 返回 `STATE_CONFLICT`；成功时只调用 `draft.cancel()` 与 `repository.save(draft)`，不调用导入服务或正式业务服务。 |
| `public OperationResult<SuggestionDraftView> confirmDraft(EntityId id)` | `OperationResult<SuggestionDraftView>` | `id == null` 返回 `VALIDATION_ERROR`；草稿不存在返回 `NOT_FOUND`；草稿非 `CONFIRMABLE` 返回 `STATE_CONFLICT`；导入失败时返回导入失败结果且不改变草稿状态；导入成功后调用 `draft.markImported()`、`repository.save(draft)` 并返回导入后视图。 |

**私有辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private OperationResult<SuggestionDraft> findDraft(EntityId id)` | `OperationResult<SuggestionDraft>` | 统一处理空 id 与不存在草稿；不存在消息固定为 `"suggestion draft not found: " + id.value()`。 |
| `private OperationResult<SuggestionDraftView> ensureConfirmable(SuggestionDraft draft)` | `OperationResult<SuggestionDraftView>` | `draft.isConfirmable() == false` 时返回 `STATE_CONFLICT`、消息 `"suggestion draft is not confirmable"`；可确认时返回成功视图或由调用方继续。 |
| `private SuggestionDraftView toView(SuggestionDraft draft)` | `SuggestionDraftView` | 委托 `SuggestionDraftView.from(draft)` 创建快照。 |
| `private OperationResult<SuggestionDraftView> toViewFailure(OperationResult<?> result)` | `OperationResult<SuggestionDraftView>` | 转换失败结果载荷类型，保留错误码和消息。 |
| `private OperationResult<SuggestionDraftView> businessFailure(BusinessException exception)` | `OperationResult<SuggestionDraftView>` | 返回 `OperationResult.failure(exception.getErrorCode(), exception.getMessage())`。 |

**构造方式**：

- 应用装配时传入 `SuggestionDraftRepository` 与 `DraftImportService`。
- 单元测试使用 `InMemorySuggestionDraftRepository` 保存草稿；确认导入成功路径使用真实 `DraftImportService` 或 mock import service，重复操作路径断言不会重复调用导入服务。

**类型关系**：

- 依赖 `SuggestionDraftRepository` 保存和查询草稿。
- 依赖 `DraftImportService` 执行正式业务导入。
- 依赖 `SuggestionDraftView` 作为唯一成功返回 DTO。
- 依赖 `OperationResult`、`ErrorCode`、`BusinessException`、`EntityId`。

### `DraftImportServiceTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.ai`

**职责**：验证草稿导入到正式任务或学习计划的服务契约、失败传播和回滚。

**类型签名定义**：

```java
class DraftImportServiceTest
```

**测试夹具规划**：

| 辅助成员 | 形态 | 职责 |
|----------|------|------|
| `private InMemoryTaskRepository taskRepository` | 字段或局部变量 | 观察正式任务写入结果。 |
| `private FailingTaskRepository failingTaskRepository` | 测试内私有静态类或局部 fake | 包装 `InMemoryTaskRepository`，按启用后的第 N 次 `save(TaskItem)` 抛出指定运行时异常，其余 `findById`、`findAll`、`findBy`、`deleteById` 委托给内存仓储。 |
| `private InMemoryStudyPlanRepository studyPlanRepository` | 字段或局部变量 | 观察正式学习计划写入结果。 |
| `private TaskService taskService` | 字段或局部变量 | 真实任务服务成功路径。 |
| `private StudyPlanService studyPlanService` | 字段或局部变量 | 真实学习计划服务成功路径。 |
| `private DraftImportService importService` | 字段或局部变量 | 被测服务。 |

**测试内 fake 仓储契约**：

| 类型/方法签名 | 契约 |
|---------------|------|
| `private static final class FailingTaskRepository implements TaskRepository` | 测试专用；持有 `private final InMemoryTaskRepository delegate`、`private boolean failureEnabled`、`private int saveCallsAfterEnable`、`private int failOnSaveCall`、`private RuntimeException failure`。 |
| `void failOnSave(int saveCallAfterEnable, RuntimeException failure)` | 启用失败注入；`saveCallAfterEnable` 必须大于 `0`；启用后重置计数；参数空引用抛 `NullPointerException("failure")`。 |
| `void save(TaskItem task)` | 若失败已启用且本次为启用后的第 `failOnSaveCall` 次保存，则抛出 `failure` 且不委托保存；否则委托 `delegate.save(task)`。 |
| `Optional<TaskItem> findById(EntityId id)` | 委托 `delegate.findById(id)`。 |
| `List<TaskItem> findAll()` | 委托 `delegate.findAll()`。 |
| `List<TaskItem> findBy(TaskQuery query)` | 委托 `delegate.findBy(query)`。 |
| `boolean deleteById(EntityId id)` | 委托 `delegate.deleteById(id)`，用于验证 `DraftImportService` 通过真实 `TaskService.deleteTask(...)` 清除本次已创建任务。 |

**测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `importsAllTaskDraftItems()` | 多个 `TASK_DRAFT` 均有截止日期时逐项创建正式任务；返回成功；`TaskService.listTasks()` 中存在全部新任务。 |
| `rejectsTaskDraftMissingDueDateBeforeCreatingAnyTask()` | 任一任务草稿 `dueDate == null` 时返回 `VALIDATION_ERROR`；不调用正式任务创建；`TaskService.listTasks()` 为空；草稿仍 `CONFIRMABLE`。 |
| `rollsBackCreatedTasksWhenTaskCreationFails()` | 使用真实 `TaskService`、递增 id 生成器和 `FailingTaskRepository`；先创建一条导入前已有任务作为基线，再启用 `failOnSave(2, new IllegalArgumentException("planned task creation failure"))`，使第一条草稿任务保存成功、第二条草稿任务由 `TaskService.createTask(...)` 转为失败结果；断言返回该失败结果的 `VALIDATION_ERROR` 和消息；通过 `TaskService.listTasks()` 断言列表只剩导入前基线任务，不包含第一条已创建草稿任务；草稿仍 `CONFIRMABLE`。 |
| `rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException()` | 使用真实 `TaskService`、递增 id 生成器和 `FailingTaskRepository`；先创建一条导入前已有任务作为基线，再启用 `failOnSave(2, new IllegalStateException("planned task repository failure"))`，使第一条草稿任务保存成功、第二条草稿任务抛不可预期运行时异常；断言返回 `SYSTEM_ERROR`；通过 `TaskService.listTasks()` 断言列表只剩导入前基线任务，不包含第一条已创建草稿任务；草稿仍 `CONFIRMABLE`。 |
| `importsStudyPlanDraft()` | `STUDY_PLAN_DRAFT` 调用 `StudyPlanService.createStudyPlan(goalName, startDate, endDate, expectedHours, initialProgress.value())`；返回成功；正式学习计划列表新增一条。 |
| `propagatesStudyPlanCreationFailure()` | 使用 mock `StudyPlanService` 返回失败；断言错误码和消息透传；草稿仍 `CONFIRMABLE`。 |
| `importDraftRejectsNullDraft()` | `importDraft(null)` 返回 `VALIDATION_ERROR`、消息 `"draft must not be null"`。 |
| `constructorRejectsNullDependencies()` | 构造参数空引用抛出参数名明确的 `NullPointerException`。 |

### `DraftLifecycleServiceTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.ai`

**职责**：验证草稿生命周期入口的查询、取消、确认、重复操作和视图快照契约。

**类型签名定义**：

```java
class DraftLifecycleServiceTest
```

**测试夹具规划**：

| 辅助成员 | 形态 | 职责 |
|----------|------|------|
| `private InMemorySuggestionDraftRepository repository` | 字段或局部变量 | 保存并观察草稿状态。 |
| `private DraftImportService importService` | 字段或 mock | 成功确认路径可用真实导入服务；失败和“不得调用”路径使用 mock 验证。 |
| `private DraftLifecycleService lifecycleService` | 字段或局部变量 | 被测服务。 |

**测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `getDraftReturnsViewSnapshot()` | 存在草稿时返回成功视图；返回的 `SuggestionDraftView` 是创建时刻快照，后续修改草稿状态不改变旧 view。 |
| `getDraftReturnsNotFoundForMissingDraft()` | 查看不存在草稿返回 `NOT_FOUND`。 |
| `getDraftRejectsNullId()` | `getDraft(null)` 返回 `VALIDATION_ERROR`、消息 `"id must not be null"`。 |
| `listDraftsReturnsUnmodifiableViewSnapshots()` | 列表返回所有草稿视图；载荷列表不可修改；后续仓储新增或草稿状态变化不改变旧列表中已有 view。 |
| `cancelDraftMarksDraftCancelledAndSaves()` | 取消成功返回 `CANCELLED` 视图；仓储中草稿状态为 `CANCELLED`；不调用导入服务。 |
| `cancelDraftReturnsNotFoundForMissingDraft()` | 取消不存在草稿返回 `NOT_FOUND`。 |
| `cancelDraftRejectsTerminalDrafts()` | 已取消后再次取消、已导入后取消均返回 `STATE_CONFLICT`；不新增正式记录；不调用导入服务。 |
| `confirmDraftImportsAndMarksDraftImported()` | 可确认草稿确认成功时先委托导入服务；导入成功后草稿状态为 `IMPORTED` 并保存；返回 `IMPORTED` 视图。 |
| `confirmDraftReturnsNotFoundForMissingDraft()` | 确认不存在草稿返回 `NOT_FOUND`。 |
| `confirmDraftRejectsTerminalDraftsWithoutImporting()` | 重复确认、取消后确认、已导入后确认返回 `STATE_CONFLICT`；导入服务不被调用；正式业务数据不新增。 |
| `confirmDraftKeepsDraftConfirmableWhenImportFails()` | 导入服务返回失败时透传错误码和消息；草稿状态保持 `CONFIRMABLE`；仓储仍可再次确认。 |
| `confirmDraftRejectsNullId()` | `confirmDraft(null)` 返回 `VALIDATION_ERROR`、消息 `"id must not be null"`。 |
| `cancelDraftRejectsNullId()` | `cancelDraft(null)` 返回 `VALIDATION_ERROR`、消息 `"id must not be null"`。 |
| `constructorRejectsNullDependencies()` | 构造参数空引用抛出参数名明确的 `NullPointerException`。 |

## 错误处理

- 服务公开入口返回 `OperationResult`，不向控制台层传播可预期业务失败。
- `DraftImportService.importDraft(null)` 固定返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "draft must not be null")`。
- `DraftLifecycleService.getDraft(null)`、`cancelDraft(null)`、`confirmDraft(null)` 固定返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "id must not be null")`。
- `DraftLifecycleService` 查询不存在草稿返回 `OperationResult.failure(ErrorCode.NOT_FOUND, "suggestion draft not found: " + id.value())`。
- 终态草稿重复取消或确认返回 `OperationResult.failure(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable")`。
- `BusinessException` 在服务边界捕获并转换为 `OperationResult.failure(exception.getErrorCode(), exception.getMessage())`。
- 任务导入中 `TaskService.createTask(...)` 返回失败时，`DraftImportService` 回滚本次已创建任务并透传该失败结果的错误码和消息。
- 任务导入中出现不可预期 `RuntimeException` 时，`DraftImportService` 回滚本次已创建任务并返回 `OperationResult.failure(ErrorCode.SYSTEM_ERROR, "failed to import suggestion draft")`。
- 回滚中 `TaskService.deleteTask(...)` 返回失败或抛异常时，不覆盖主失败原因；服务仍返回原始创建失败或系统错误结果。
- 学习计划导入不需要批量回滚；`StudyPlanService.createStudyPlan(...)` 返回失败时直接透传错误码和消息，草稿状态由生命周期服务保持不变。

## 行为契约

- 生命周期查询和写操作成功返回 `SuggestionDraftView` 或不可修改的 `List<SuggestionDraftView>`，不得返回 `SuggestionDraft`。
- `listDrafts()` 的列表是调用时刻快照；列表本身不可修改；每个元素由 `SuggestionDraftView.from(...)` 创建。
- `cancelDraft(id)` 成功只执行草稿状态迁移和草稿保存，不调用 `DraftImportService`、`TaskService` 或 `StudyPlanService`。
- `confirmDraft(id)` 必须在调用 `DraftImportService.importDraft(...)` 前检查草稿存在且 `draft.isConfirmable()` 为 `true`。
- `confirmDraft(id)` 导入成功后才允许调用 `draft.markImported()`；标记后必须调用 `repository.save(draft)`。
- `confirmDraft(id)` 导入失败时不得调用 `draft.markImported()` 或 `repository.save(draft)` 改写终态；草稿保持原 `CONFIRMABLE` 状态。
- `DraftImportService` 不检查或改变草稿生命周期状态；终态保护统一由 `DraftLifecycleService` 完成。
- `TASK_DRAFT` 导入前必须完整扫描所有任务草稿，任一 `dueDate == null` 立即返回 `VALIDATION_ERROR`，且不得调用 `TaskService.createTask(...)`。
- `TASK_DRAFT` 导入按 `draft.getTasks()` 顺序创建任务；每项调用 `taskService.createTask(item.title(), item.description(), item.priority(), item.dueDate())`。
- 任务批量导入中任一创建失败或抛运行时异常时，必须对本次已成功创建的任务 id 尽力调用 `taskService.deleteTask(id)`；不得删除本次导入前已存在的任务；测试必须通过真实 `TaskService.listTasks()` 断言最终正式任务列表只保留导入前基线数据，不出现本次已创建草稿任务半写入。
- `STUDY_PLAN_DRAFT` 导入调用 `studyPlanService.createStudyPlan(content.goalName(), content.startDate(), content.endDate(), content.expectedHours(), content.initialProgress().value())`。
- 重复确认、取消后确认、导入后确认、取消后再次取消、导入后取消均返回 `STATE_CONFLICT`，不得新增正式记录。

## 依赖关系

- 复用 `assistant.ai.SuggestionDraft`、`SuggestionDraftType`、`SuggestionDraftStatus`、`TaskDraftItem`、`StudyPlanDraftContent`、`SuggestionDraftView`、`SuggestionDraftRepository`、`InMemorySuggestionDraftRepository`。
- 复用 `assistant.task.TaskService` 的 `createTask(String title, String description, TaskPriority priority, LocalDate dueDate)`、`deleteTask(EntityId id)`、`listTasks()`。
- 复用 `assistant.study.StudyPlanService` 的 `createStudyPlan(String goalName, LocalDate startDate, LocalDate endDate, int expectedHours, int initialProgress)`、`listStudyPlans()`。
- 复用 `assistant.common.OperationResult`、`ErrorCode`、`BusinessException`、`EntityId`、`Progress`。
- 测试复用 `assistant.task.InMemoryTaskRepository`、`assistant.task.TaskRepository`、`assistant.task.TaskItem`、`assistant.task.TaskQuery`、`assistant.study.InMemoryStudyPlanRepository`、`assistant.testability.IncrementalIdGenerator`、`assistant.testability.FixedTimeProvider`、`assistant.study.StudyPlanAnalysisService`。
- 任务导入回滚失败与异常路径不得只使用 mock `TaskService` 调用验证；必须使用真实 `TaskService` 与可控失败 `TaskRepository`，并通过 `TaskService.listTasks()` 验证无半写入。
- 学习计划失败路径以及 `DraftLifecycleService` 对导入服务的调用次数验证可复用现有 Mockito 依赖模拟 `StudyPlanService`、`DraftImportService` 的返回失败、抛异常和调用次数。

## 修订说明（v21 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| `DraftImportServiceTest` 的任务回滚测试只使用 mock `TaskService` 验证调用，未满足任务要求中通过正式 `TaskService.listTasks()` 断言无半写入的硬性要求。 | 将任务创建失败和运行时异常两条回滚测试改为使用真实 `TaskService`、递增 id 生成器和测试内 `FailingTaskRepository`；两条测试都先创建导入前基线任务，再制造第一条草稿保存成功、第二条保存失败/抛异常，最后通过 `TaskService.listTasks()` 断言仅保留基线任务，确保本次已创建草稿任务被真实删除且未误删既有任务。 |
