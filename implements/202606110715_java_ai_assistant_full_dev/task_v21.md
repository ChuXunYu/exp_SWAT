# 任务指令（v21）

## 动作
NEW

## 任务描述
新增 AI 草稿生命周期服务和正式导入服务，补齐结构化建议“确认/取消/导入正式记录”的服务闭环。预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/ai/DraftImportService.java`
- `java-ai-assistant/src/main/java/assistant/ai/DraftLifecycleService.java`
- `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/DraftLifecycleServiceTest.java`

本轮实现范围：

- `DraftImportService`：根据 `SuggestionDraftType` 将 `TASK_DRAFT` 导入正式任务，将 `STUDY_PLAN_DRAFT` 导入正式学习计划；所有正式数据写入必须调用既有 `TaskService` 或 `StudyPlanService`，不得直接操作任务或学习计划仓储。
- `DraftLifecycleService`：提供草稿查看、列表、取消和确认导入入口；统一处理不存在草稿、终态草稿重复操作、导入成功状态保存和导入失败状态保持。

建议公开契约收束如下，若设计阶段发现需微调签名，必须保持返回语义等价：

- `DraftImportService.importDraft(SuggestionDraft draft)` 返回 `OperationResult<Void>`。
- `DraftLifecycleService.getDraft(EntityId id)` 返回 `OperationResult<SuggestionDraftView>`。
- `DraftLifecycleService.listDrafts()` 返回 `OperationResult<List<SuggestionDraftView>>`，成功载荷列表不可修改且为视图快照。
- `DraftLifecycleService.cancelDraft(EntityId id)` 返回 `OperationResult<SuggestionDraftView>`。
- `DraftLifecycleService.confirmDraft(EntityId id)` 返回 `OperationResult<SuggestionDraftView>`。

## 选择理由
v20 已完成 AI 结构化建议草稿类型、草稿状态、任务草稿项、学习计划草稿内容、草稿聚合根、只读视图、仓储和严格 JSON 解析器。技术方案中结构化建议下一步是“草稿-确认-导入”的独立状态流；控制台菜单依赖该服务闭环，否则重复确认、防半写入、取消和导入失败处理会被迫散落到交互层。

## 任务上下文
必须满足以下行为边界：

- 查看或确认不存在草稿返回 `OperationResult.failure(ErrorCode.NOT_FOUND, ...)`。
- 取消不存在草稿返回 `OperationResult.failure(ErrorCode.NOT_FOUND, ...)`。
- 取消成功只修改草稿状态为 `CANCELLED`，不得调用任务或学习计划服务，不得修改正式业务数据。
- 确认导入前必须检查草稿仍为 `CONFIRMABLE`；重复确认、取消后确认、已导入后确认、取消后再次取消、导入后取消均返回 `STATE_CONFLICT`，不得新增正式记录。
- `TASK_DRAFT` 导入时逐项调用 `TaskService.createTask(title, description, priority, dueDate)`；若任一创建返回失败，必须删除本次已创建的任务并返回该失败结果的错误码和消息，草稿状态保持 `CONFIRMABLE`。
- `TASK_DRAFT` 导入正式任务前必须先完整校验所有 `TaskDraftItem.dueDate()` 均非空；任一任务草稿缺失截止日期时，必须返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不得调用 `TaskService.createTask(...)`，不得新增正式任务，草稿状态保持 `CONFIRMABLE`。本轮不自动填充默认截止日期，也不修改正式 `TaskItem` / `TaskView` 的非空截止日期模型。
- `TASK_DRAFT` 导入若在部分创建成功后出现不可预期运行时异常，必须尽力调用 `TaskService.deleteTask(createdId)` 回滚本次已创建任务，返回 `SYSTEM_ERROR`，草稿状态保持 `CONFIRMABLE`。
- `STUDY_PLAN_DRAFT` 导入时调用 `StudyPlanService.createStudyPlan(goalName, startDate, endDate, expectedHours, initialProgress.value())` 或既有等价接口；创建失败返回该失败结果，草稿状态保持 `CONFIRMABLE`。
- 导入成功后必须调用 `SuggestionDraft.markImported()` 并保存草稿，返回导入后 `SuggestionDraftView`，重复确认不得重复新增正式记录。
- 所有服务成功查询和写操作返回 `SuggestionDraftView` 或不可修改的 `List<SuggestionDraftView>`，不得暴露内部可变 `SuggestionDraft` 引用。
- 服务边界捕获 `BusinessException` 并转换为对应 `OperationResult.failure(...)`；本轮固定公开契约为：`DraftImportService.importDraft(null)` 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "draft must not be null")`；`DraftLifecycleService` 的 `getDraft(null)`、`cancelDraft(null)`、`confirmDraft(null)` 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "id must not be null")`；服务构造参数空引用抛出参数名明确的 `NullPointerException`。

本轮不实现：

- AI 服务自动生成并保存草稿。
- 控制台菜单、用户输入解析或展示文本。
- 真实 DeepSeek 调用、集成测试或网络访问。
- 数据库、文件持久化或跨进程草稿持久化。

## 已有代码上下文
已存在并应复用：

- `assistant.ai.SuggestionDraft`：提供 `getId()`、`getType()`、`getTasks()`、`getStudyPlan()`、`isConfirmable()`、`cancel()`、`markImported()`；终态重复迁移抛出 `BusinessException(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable")`。
- `assistant.ai.SuggestionDraftView.from(SuggestionDraft)`：创建草稿视图快照。
- `assistant.ai.SuggestionDraftRepository` 与 `InMemorySuggestionDraftRepository`：提供 `save`、`findById`、`findAll`、`deleteById`。
- `assistant.ai.TaskDraftItem`：持有任务草稿 `title`、`description`、`TaskPriority priority`、`LocalDate dueDate`。
- `assistant.ai.StudyPlanDraftContent`：持有学习计划草稿 `goalName`、`startDate`、`endDate`、`expectedHours`、`Progress initialProgress`、`breakdown`。
- `assistant.task.TaskService`：公开 `createTask(...)`、`deleteTask(...)` 等服务入口，成功创建返回 `TaskView`，其中包含正式任务 `EntityId`。
- `assistant.study.StudyPlanService`：公开带原始日期和初始进度整数的 `createStudyPlan(...)` 服务入口。
- `assistant.common.OperationResult`、`ErrorCode`、`BusinessException`：用于服务边界成功/失败语义。

测试要求：

- 使用内存仓储、固定或递增编号生成器和真实本地服务/fake 失败路径组合，不访问真实网络、真实 DeepSeek、真实 API Key、真实当前时间或外部文件。
- 覆盖 `DraftLifecycleService` 的查看、列表、取消、确认成功、确认不存在、取消不存在、重复确认、取消后确认、导入后取消和视图快照不可变。
- 覆盖 `DraftImportService` 的任务草稿全部成功、任务草稿缺失截止日期时校验失败且不创建正式任务、任务草稿中途失败回滚、任务草稿异常回滚、学习计划成功、学习计划失败、`importDraft(null)` 可达防御路径。
- 回滚测试必须断言正式 `TaskService.listTasks()` 或 `StudyPlanService.listStudyPlans()` 的状态未出现半写入。

## 修订说明（v21 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| `TASK_DRAFT` 草稿允许 `dueDate == null`，但正式 `TaskService.createTask(...)` / `TaskView` 要求非空截止日期，导入行为未唯一。 | 固定本轮契约为导入前完整校验所有任务草稿截止日期；任一缺失返回 `VALIDATION_ERROR`，不调用正式任务创建接口，不新增正式任务，草稿保持 `CONFIRMABLE`；不引入默认日期，也不修改正式任务模型。 |
| “未知或不匹配状态防御路径”在当前封闭枚举和聚合根工厂保护下不可达，测试要求不可执行。 | 删除不可达未知枚举/内容不匹配测试要求，改为覆盖可达的 `importDraft(null)` 防御路径，并固定返回 `VALIDATION_ERROR`；同时固定生命周期服务空 id 与构造参数空引用契约。 |
