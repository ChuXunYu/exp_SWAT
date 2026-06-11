# 任务指令（v8）

## 动作
NEW

## 任务描述
实现任务待办模块的查询条件、只读查询快照、仓储契约、内存仓储和应用服务，形成任务核心功能的服务层闭环，并明确服务查询结果不得暴露仓储内部可变实体。

预期新增或更新文件包括：

- `java-ai-assistant/src/main/java/assistant/task/TaskQuery.java`
- `java-ai-assistant/src/main/java/assistant/task/TaskView.java`
- `java-ai-assistant/src/main/java/assistant/task/TaskRepository.java`
- `java-ai-assistant/src/main/java/assistant/task/InMemoryTaskRepository.java`
- `java-ai-assistant/src/main/java/assistant/task/TaskService.java`
- `java-ai-assistant/src/test/java/assistant/task/TaskQueryTest.java`
- `java-ai-assistant/src/test/java/assistant/task/TaskViewTest.java`
- `java-ai-assistant/src/test/java/assistant/task/InMemoryTaskRepositoryTest.java`
- `java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java`

本轮应支持的服务行为包括：创建任务、按编号查看任务、列出任务、按状态筛选、按优先级筛选、按截止日期筛选或组合筛选、修改任务基础信息、删除任务、标记完成、撤销完成。服务层应对外返回 `OperationResult`，并将记录不存在、重复状态迁移和输入校验错误转换为稳定错误分类。

服务层查询载荷应使用只读 DTO/record（建议命名为 `TaskView`）表达任务快照，字段至少包括 `EntityId id`、`String title`、`String description`、`TaskPriority priority`、`LocalDate dueDate`、`TaskStatus status`，并提供从 `TaskItem` 生成快照的工厂方法。`TaskService` 的创建、查看、列表和组合筛选成功结果均应返回 `TaskView` 或不可修改的 `List<TaskView>`，不得把仓储内部保存的可变 `TaskItem` 实例直接返回给外部调用方。

## 选择理由
v7 已完成 `TaskPriority`、`TaskStatus` 和 `TaskItem`，任务待办模块已有领域模型与实体内部状态流转规则，但还没有任务待办管理功能所需的服务入口和数据存取边界。

先实现 `TaskQuery`、`TaskView`、`TaskRepository`、`InMemoryTaskRepository` 和 `TaskService`，可以让任务模块从领域对象推进到可被后续汇总统计、AI 草稿导入和控制台菜单复用的应用服务闭环。该任务仍局限于任务模块内部，不引入日程、学习计划、汇总、AI 或控制台层，风险和验证范围可控。

## 任务上下文
需求要求任务待办管理支持新增、查看、修改、删除待办任务；每条任务至少包含标题、描述、优先级、截止日期和完成状态；支持将任务标记为已完成或未完成，并能按状态或优先级筛选任务；需要处理标题为空、优先级非法、删除不存在任务、重复标记完成、撤销完成等异常情况。

技术方案要求：

- 任务实体持有标题、描述、优先级、截止日期、完成状态和编号。
- 新增和修改任务时由服务集中校验标题非空、优先级合法、日期合法。
- 标记完成和撤销完成先查找记录，再检查状态迁移。
- 重复完成、重复撤销统一返回 `STATE_CONFLICT`，任务状态不变。
- 查询条件由 `TaskQuery` 表达，支持按状态、优先级和截止日期筛选。
- 仓储默认使用内存实现，仓储可在模块内部返回实体快照集合供服务编排，但不得把可修改内部集合暴露出去。
- `TaskService` 是外部读写任务数据的边界；服务的查看、列表、筛选和创建结果必须返回 `TaskView` 只读快照或不可修改的 `List<TaskView>`，不得返回仓储内的 `TaskItem` 引用。
- 外部调用方拿到 `TaskView` 或 `List<TaskView>` 后，不能通过返回对象修改仓储内部任务的标题、描述、优先级、截止日期或状态；列表本身也必须不可修改。
- 删除不存在记录由服务层转换为 `NOT_FOUND`。
- 普通单元测试不得依赖真实当前时间、真实网络、API Key 或外部文件。

本轮不实现汇总统计、AI 草稿导入、日程模块、学习计划模块、控制台菜单或真实 DeepSeek 调用。

## 已有代码上下文
已完成的相关基础包括：

- `assistant.common.EntityId`：正整数编号值对象。
- `assistant.testability.IdGenerator` 与 `IncrementalIdGenerator`：可替换编号生成基础。
- `assistant.common.OperationResult<T>`：应用服务成功/失败返回语义。
- `assistant.common.ErrorCode`：已包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT` 等错误分类。
- `assistant.common.BusinessException`：领域状态冲突异常载体。
- `assistant.task.TaskPriority`：固定 `LOW`、`MEDIUM`、`HIGH` 优先级。
- `assistant.task.TaskStatus`：固定 `TODO`、`COMPLETED` 状态，并提供 `isCompleted()`。
- `assistant.task.TaskItem`：封装任务编号、标题、描述、优先级、截止日期和状态；支持 `createTodo(...)`、`updateDetails(...)`、`markCompleted()`、`reopen()`；重复完成或重复撤销抛出 `BusinessException(ErrorCode.STATE_CONFLICT, ...)`。

实现时应优先复用 `TaskItem` 的公开方法，不应在服务层绕过实体直接修改状态字段；仓储和服务对外返回任务列表时应避免暴露可修改内部集合。

本轮测试除覆盖创建、查看、筛选、修改、删除、完成和撤销完成等功能外，还必须覆盖服务查询只读边界：

- `TaskService` 查看、列表和组合筛选结果的载荷类型为 `TaskView` 或 `List<TaskView>`，不是 `TaskItem`。
- 服务返回的列表不可被调用方增删改。
- 调用方拿到查询结果后，无法通过返回对象改变仓储中对应任务的状态或基础信息；如后续通过服务再次查询同一任务，应仍以仓储内部状态为准。

## 修订说明（v8 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| 服务和仓储列表虽然要求不可修改集合，但未明确避免直接暴露可变 `TaskItem` 实例，可能让外部调用方绕过 `TaskService` 修改内部状态。 | 将本轮任务修订为新增 `TaskView` 只读 DTO/record；明确 `TaskService` 的创建、查看、列表和组合筛选成功载荷返回 `TaskView` 或不可修改的 `List<TaskView>`，不得返回仓储内部 `TaskItem` 引用。 |
| 缺少验证外部查询结果无法影响仓储内部状态的测试要求。 | 在任务文件中新增 `TaskViewTest` 预期文件，并补充服务查询只读边界测试要求：载荷类型不是 `TaskItem`、列表不可修改、通过服务再次查询时仓储内部状态不受返回对象影响。 |
