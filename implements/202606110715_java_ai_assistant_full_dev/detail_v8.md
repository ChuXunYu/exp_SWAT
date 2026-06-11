# 详细设计（v8）

## 概述

本轮设计目标是在既有 `assistant.task` 任务待办领域模型基础上，补齐任务模块的查询条件、只读查询快照、仓储契约、内存仓储实现和应用服务，形成任务核心功能的服务层闭环。

新增 `TaskQuery` 用于表达按状态、优先级、截止日期及其组合筛选任务；新增 `TaskView` 作为服务查询与写操作成功结果的只读快照 DTO，避免外部调用方拿到仓储内部保存的可变 `TaskItem` 实例；新增 `TaskRepository` 和 `InMemoryTaskRepository` 封装内存数据边界；新增 `TaskService` 作为外部读写任务数据的唯一应用服务入口，统一返回 `OperationResult`，并将输入校验错误、记录不存在和重复状态迁移转换为稳定 `ErrorCode`。

本轮范围仅包含 `assistant.task` 包内的任务查询、任务仓储和任务服务，不实现汇总统计、AI 草稿导入、日程模块、学习计划模块、控制台菜单或真实 DeepSeek 调用。新增单元测试必须使用固定编号和固定日期，不依赖真实当前时间、网络、API Key 或外部文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/task/TaskQuery.java` | 新建 | 定义任务筛选条件，支持状态、优先级、截止日期及组合筛选。 |
| `java-ai-assistant/src/main/java/assistant/task/TaskView.java` | 新建 | 定义任务只读快照 DTO，作为 `TaskService` 对外返回的查询和写操作成功载荷。 |
| `java-ai-assistant/src/main/java/assistant/task/TaskRepository.java` | 新建 | 定义任务仓储契约，封装按编号保存、查询、删除和按条件筛选。 |
| `java-ai-assistant/src/main/java/assistant/task/InMemoryTaskRepository.java` | 新建 | 使用 `LinkedHashMap<EntityId, TaskItem>` 实现默认内存仓储，保持插入顺序和列表快照不可修改。 |
| `java-ai-assistant/src/main/java/assistant/task/TaskService.java` | 新建 | 定义任务应用服务，提供创建、查看、列表、筛选、修改、删除、标记完成和撤销完成。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskQueryTest.java` | 新建 | 覆盖查询条件构造、匹配语义和组合筛选条件。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskViewTest.java` | 新建 | 覆盖只读快照字段、从实体生成快照、快照不随实体后续变化而变化。 |
| `java-ai-assistant/src/test/java/assistant/task/InMemoryTaskRepositoryTest.java` | 新建 | 覆盖保存、查找、列表、筛选、删除、插入顺序和不可修改列表快照。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java` | 新建 | 覆盖任务服务创建、查看、列表、筛选、修改、删除、完成、撤销完成、错误转换和只读查询边界。 |

## 类型定义

### `TaskQuery`

**形态**：`record`

**包路径**：`assistant.task`

**职责**：表达任务筛选条件。每个记录组件为 `null` 时表示不启用该筛选条件；多个非空条件之间使用逻辑与语义，任务必须同时满足所有已启用条件才匹配。

**类型签名定义**：`public record TaskQuery(TaskStatus status, TaskPriority priority, LocalDate dueDate)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `TaskStatus status` | 可为 `null`；非空时只匹配相同任务状态。 |
| `TaskPriority priority` | 可为 `null`；非空时只匹配相同任务优先级。 |
| `LocalDate dueDate` | 可为 `null`；非空时只匹配截止日期等于该日期的任务。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TaskQuery(TaskStatus status, TaskPriority priority, LocalDate dueDate)` | 构造器 | 创建组合查询条件；任一组件为 `null` 表示对应条件不启用；构造器不读取系统日期。 |
| `public static TaskQuery all()` | `TaskQuery` | 返回不包含任何筛选条件的查询，匹配所有任务。 |
| `public static TaskQuery byStatus(TaskStatus status)` | `TaskQuery` | 创建仅按状态筛选的查询；`status == null` 时抛出 `NullPointerException`。 |
| `public static TaskQuery byPriority(TaskPriority priority)` | `TaskQuery` | 创建仅按优先级筛选的查询；`priority == null` 时抛出 `NullPointerException`。 |
| `public static TaskQuery byDueDate(LocalDate dueDate)` | `TaskQuery` | 创建仅按截止日期筛选的查询；`dueDate == null` 时抛出 `NullPointerException`。 |
| `public static TaskQuery of(TaskStatus status, TaskPriority priority, LocalDate dueDate)` | `TaskQuery` | 语义化组合查询工厂；等价于调用规范构造器，允许任一参数为 `null`。 |
| `public boolean hasStatusFilter()` | `boolean` | 当且仅当 `status() != null` 时返回 `true`。 |
| `public boolean hasPriorityFilter()` | `boolean` | 当且仅当 `priority() != null` 时返回 `true`。 |
| `public boolean hasDueDateFilter()` | `boolean` | 当且仅当 `dueDate() != null` 时返回 `true`。 |
| `public boolean matches(TaskItem task)` | `boolean` | `task == null` 时抛出 `NullPointerException`；所有启用条件均匹配时返回 `true`，否则返回 `false`。 |
| `public TaskStatus status()` | `TaskStatus` | record 自动提供；返回状态筛选条件，可能为 `null`。 |
| `public TaskPriority priority()` | `TaskPriority` | record 自动提供；返回优先级筛选条件，可能为 `null`。 |
| `public LocalDate dueDate()` | `LocalDate` | record 自动提供；返回截止日期筛选条件，可能为 `null`。 |

**构造方式**：

- 查询全部任务时调用 `TaskQuery.all()`。
- 单条件筛选优先使用 `TaskQuery.byStatus(...)`、`TaskQuery.byPriority(...)` 或 `TaskQuery.byDueDate(...)`。
- 组合筛选调用 `TaskQuery.of(status, priority, dueDate)` 或直接调用构造器；未使用的条件传入 `null`。

**类型关系**：

- 依赖 `assistant.task.TaskStatus`、`assistant.task.TaskPriority`、`assistant.task.TaskItem` 和 `java.time.LocalDate`。
- 被 `TaskRepository.findBy(TaskQuery query)` 和 `TaskService.listTasks(TaskQuery query)` 使用。
- 不依赖 `OperationResult`、仓储实现、系统时间、控制台或 AI 模块。

### `TaskView`

**形态**：`record`

**包路径**：`assistant.task`

**职责**：表达任务的只读快照，作为 `TaskService` 对外返回的任务查询和写操作成功载荷，防止调用方通过返回对象修改仓储内部 `TaskItem`。

**类型签名定义**：`public record TaskView(EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate, TaskStatus status)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `EntityId id` | 必须非空；值对象自身不可变。 |
| `String title` | 必须非空且 `strip()` 后不得为空；保存入参文本本身，不额外规范化。 |
| `String description` | 必须非空；允许为空字符串；保存入参文本本身。 |
| `TaskPriority priority` | 必须非空。 |
| `LocalDate dueDate` | 必须非空；`LocalDate` 不可变。 |
| `TaskStatus status` | 必须非空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TaskView(EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate, TaskStatus status)` | 构造器 | 创建只读任务快照；必填引用为 `null` 时抛出 `NullPointerException`；标题清理后为空时抛出 `IllegalArgumentException`。 |
| `public static TaskView from(TaskItem task)` | `TaskView` | `task == null` 时抛出 `NullPointerException`；读取当前 `TaskItem` 字段并创建独立快照。 |
| `public boolean isCompleted()` | `boolean` | 等价于 `status().isCompleted()`。 |
| `public EntityId id()` | `EntityId` | record 自动提供；返回任务编号。 |
| `public String title()` | `String` | record 自动提供；返回快照标题。 |
| `public String description()` | `String` | record 自动提供；返回快照描述。 |
| `public TaskPriority priority()` | `TaskPriority` | record 自动提供；返回快照优先级。 |
| `public LocalDate dueDate()` | `LocalDate` | record 自动提供；返回快照截止日期。 |
| `public TaskStatus status()` | `TaskStatus` | record 自动提供；返回快照状态。 |

**构造方式**：

- `TaskService` 必须通过 `TaskView.from(task)` 将 `TaskItem` 转换为只读快照后再返回。
- 单元测试可直接调用构造器构造期望快照。
- `TaskView` 不提供任何修改方法；调用方如需修改任务，必须再次调用 `TaskService.updateTask(...)`、`markTaskCompleted(...)` 或 `reopenTask(...)`。

**类型关系**：

- 组合 `assistant.common.EntityId`、`assistant.task.TaskPriority`、`assistant.task.TaskStatus` 和 `java.time.LocalDate`。
- 从 `assistant.task.TaskItem` 生成，但不持有 `TaskItem` 引用。
- 被 `TaskService` 的创建、查看、列表、筛选、修改、完成和撤销完成成功结果返回。

### `TaskRepository`

**形态**：`interface`

**包路径**：`assistant.task`

**职责**：定义任务实体的数据访问契约，隔离任务服务和具体内存存储实现。仓储返回的任务列表必须是不可修改的集合快照，不得暴露仓储内部可修改集合对象。

**类型签名定义**：`public interface TaskRepository`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `void save(TaskItem task)` | `void` | 保存任务实体；`task == null` 时抛出 `NullPointerException`；同一 `EntityId` 已存在时替换现有任务。 |
| `Optional<TaskItem> findById(EntityId id)` | `Optional<TaskItem>` | 按编号查找任务；`id == null` 时抛出 `NullPointerException`；不存在时返回 `Optional.empty()`。 |
| `List<TaskItem> findAll()` | `List<TaskItem>` | 返回当前全部任务的不可修改列表快照；顺序由具体实现定义，本轮内存实现使用插入顺序。 |
| `List<TaskItem> findBy(TaskQuery query)` | `List<TaskItem>` | 按查询条件返回不可修改列表快照；`query == null` 时抛出 `NullPointerException`。 |
| `boolean deleteById(EntityId id)` | `boolean` | 按编号删除任务；`id == null` 时抛出 `NullPointerException`；删除到记录返回 `true`，记录不存在返回 `false`。 |

**构造方式**：

- 接口不可直接构造。
- 默认生产和测试实现使用 `InMemoryTaskRepository`。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.task.TaskItem` 和 `assistant.task.TaskQuery`。
- 被 `TaskService` 组合持有。
- 仓储方法面向任务模块内部编排返回 `TaskItem`，但 `TaskService` 不得把这些 `TaskItem` 直接返回给外部调用方。

### `InMemoryTaskRepository`

**形态**：`class`

**包路径**：`assistant.task`

**职责**：使用内存 `LinkedHashMap` 保存任务实体，按 `EntityId` 支持保存、查找、筛选和删除，并保持列表查询顺序稳定。

**类型签名定义**：`public final class InMemoryTaskRepository implements TaskRepository`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final Map<EntityId, TaskItem> tasks` | 集合引用不可变，内容可变 | 构造时初始化为 `LinkedHashMap<>`；key 为任务编号，value 为任务实体；不允许保存 `null` value。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public InMemoryTaskRepository()` | 构造器 | 创建空仓储。 |
| `public void save(TaskItem task)` | `void` | 使用 `task.getId()` 作为 key 保存任务；同一编号重复保存时替换 value，保留 `LinkedHashMap` 对既有 key 的顺序语义。 |
| `public Optional<TaskItem> findById(EntityId id)` | `Optional<TaskItem>` | 返回编号对应任务；不存在时返回空。 |
| `public List<TaskItem> findAll()` | `List<TaskItem>` | 按插入顺序返回不可修改列表快照；调用方不能增删该列表；后续仓储新增或删除不改变已返回列表的元素数量。 |
| `public List<TaskItem> findBy(TaskQuery query)` | `List<TaskItem>` | 按插入顺序筛选 `query.matches(task)` 为 `true` 的任务并返回不可修改列表快照。 |
| `public boolean deleteById(EntityId id)` | `boolean` | 删除指定编号任务；存在返回 `true`，不存在返回 `false`。 |

**构造方式**：

- `new InMemoryTaskRepository()` 创建空仓储。
- 每个单元测试应重新创建实例，避免测试之间共享状态。

**类型关系**：

- 实现 `TaskRepository`。
- 组合 `java.util.LinkedHashMap`、`java.util.Map`。
- 依赖 `assistant.common.EntityId`、`assistant.task.TaskItem` 和 `assistant.task.TaskQuery`。
- 不依赖 `OperationResult`、`IdGenerator`、系统时间、文件系统、网络或 AI 客户端。

### `TaskService`

**形态**：`class`

**包路径**：`assistant.task`

**职责**：作为任务待办模块对外应用服务边界，集中处理任务创建、查看、列表、筛选、修改、删除、完成和撤销完成，并将领域异常和输入校验错误转换为 `OperationResult`。

**类型签名定义**：`public final class TaskService`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final TaskRepository repository` | 构造后不可变 | 构造入参必须非空；所有任务读写通过该仓储执行。 |
| `private final IdGenerator idGenerator` | 构造后不可变 | 构造入参必须非空；仅创建任务时生成编号。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TaskService(TaskRepository repository, IdGenerator idGenerator)` | 构造器 | 注入仓储和编号生成器；任一依赖为 `null` 时抛出 `NullPointerException`。 |
| `public OperationResult<TaskView> createTask(String title, String description, TaskPriority priority, LocalDate dueDate)` | `OperationResult<TaskView>` | 生成编号并创建初始 `TODO` 任务；成功时保存任务并返回 `TaskView`；标题非法、优先级为空或截止日期为空时返回 `VALIDATION_ERROR`，仓储不新增记录。 |
| `public OperationResult<TaskView> getTask(EntityId id)` | `OperationResult<TaskView>` | 按编号查看任务；`id == null` 返回 `VALIDATION_ERROR`；记录不存在返回 `NOT_FOUND`；成功返回 `TaskView`。 |
| `public OperationResult<List<TaskView>> listTasks()` | `OperationResult<List<TaskView>>` | 返回全部任务的不可修改 `List<TaskView>`，按仓储列表顺序排列。 |
| `public OperationResult<List<TaskView>> listTasks(TaskQuery query)` | `OperationResult<List<TaskView>>` | 按查询条件返回不可修改 `List<TaskView>`；`query == null` 返回 `VALIDATION_ERROR`；成功结果不得包含 `TaskItem`。 |
| `public OperationResult<TaskView> updateTask(EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate)` | `OperationResult<TaskView>` | 修改任务标题、描述、优先级和截止日期；`id == null` 返回 `VALIDATION_ERROR`；记录不存在返回 `NOT_FOUND`；新字段非法返回 `VALIDATION_ERROR` 且原任务状态和基础信息不变；成功返回更新后的 `TaskView`。 |
| `public OperationResult<Void> deleteTask(EntityId id)` | `OperationResult<Void>` | 删除任务；`id == null` 返回 `VALIDATION_ERROR`；记录不存在返回 `NOT_FOUND`；成功返回 `OperationResult.success()`。 |
| `public OperationResult<TaskView> markTaskCompleted(EntityId id)` | `OperationResult<TaskView>` | 将 `TODO` 任务标记为 `COMPLETED`；`id == null` 返回 `VALIDATION_ERROR`；记录不存在返回 `NOT_FOUND`；重复完成返回 `STATE_CONFLICT` 且状态保持不变；成功返回更新后的 `TaskView`。 |
| `public OperationResult<TaskView> reopenTask(EntityId id)` | `OperationResult<TaskView>` | 将 `COMPLETED` 任务撤销为 `TODO`；`id == null` 返回 `VALIDATION_ERROR`；记录不存在返回 `NOT_FOUND`；重复撤销返回 `STATE_CONFLICT` 且状态保持不变；成功返回更新后的 `TaskView`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private OperationResult<TaskView> validationFailure(String message)` | `OperationResult<TaskView>` | 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, message)`；消息必须非空非空白。 |
| `private OperationResult<TaskView> notFound(EntityId id)` | `OperationResult<TaskView>` | 返回 `ErrorCode.NOT_FOUND` 失败结果；消息表达任务不存在。 |
| `private OperationResult<Void> validationFailureVoid(String message)` | `OperationResult<Void>` | 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, message)`。 |
| `private OperationResult<Void> notFoundVoid(EntityId id)` | `OperationResult<Void>` | 返回 `ErrorCode.NOT_FOUND` 失败结果。 |
| `private static TaskView toView(TaskItem task)` | `TaskView` | 调用 `TaskView.from(task)` 创建只读快照。 |
| `private static List<TaskView> toUnmodifiableViews(List<TaskItem> tasks)` | `List<TaskView>` | 将任务实体列表映射为 `TaskView` 列表，并返回不可修改列表。 |

私有辅助方法仅为实现复用设计，不作为公开 API。测试通过公开方法覆盖其行为。

**构造方式**：

- 生产或后续控制台装配时调用 `new TaskService(new InMemoryTaskRepository(), new IncrementalIdGenerator())`。
- 单元测试可传入新的 `InMemoryTaskRepository` 和起始值固定的 `IncrementalIdGenerator`，确保编号和仓储状态可预测。

**类型关系**：

- 组合 `TaskRepository` 和 `assistant.testability.IdGenerator`。
- 依赖 `assistant.common.OperationResult`、`assistant.common.ErrorCode`、`assistant.common.BusinessException`、`assistant.common.EntityId`。
- 复用 `TaskItem.createTodo(...)`、`TaskItem.updateDetails(...)`、`TaskItem.markCompleted()` 和 `TaskItem.reopen()`，不得绕过实体直接修改状态字段。
- 对外仅返回 `TaskView`、不可修改 `List<TaskView>` 或 `OperationResult<Void>`，不得返回 `TaskItem`。

## 单元测试规格

### `TaskQueryTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试数据约定**：

- 使用 `new EntityId(1)`、`new EntityId(2)` 等固定编号。
- 使用 `LocalDate.of(2026, 6, 30)`、`LocalDate.of(2026, 7, 1)` 等固定日期。
- 通过 `TaskItem.createTodo(...)` 或完整构造器创建匹配与不匹配任务。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void allQueryMatchesEveryTask()` | `TaskQuery.all()` 不启用任何筛选条件，匹配任意任务。 |
| `void statusQueryMatchesOnlySameStatus()` | `TaskQuery.byStatus(TaskStatus.COMPLETED)` 只匹配已完成任务。 |
| `void priorityQueryMatchesOnlySamePriority()` | `TaskQuery.byPriority(TaskPriority.HIGH)` 只匹配高优先级任务。 |
| `void dueDateQueryMatchesOnlySameDueDate()` | `TaskQuery.byDueDate(date)` 只匹配相同截止日期任务。 |
| `void combinedQueryRequiresEveryProvidedFilterToMatch()` | 组合状态、优先级和截止日期时必须全部匹配才返回 `true`。 |
| `void ofAllowsNullComponentsAsWildcards()` | `TaskQuery.of(null, TaskPriority.HIGH, null)` 仅启用优先级筛选。 |
| `void exposesFilterPresenceFlags()` | `hasStatusFilter()`、`hasPriorityFilter()`、`hasDueDateFilter()` 反映对应组件是否非空。 |
| `void singleCriterionFactoriesRejectNullCriterion()` | `byStatus(null)`、`byPriority(null)`、`byDueDate(null)` 抛出 `NullPointerException`。 |
| `void matchesRejectsNullTask()` | `TaskQuery.all().matches(null)` 抛出 `NullPointerException`。 |

### `TaskViewTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorStoresProvidedSnapshotFields()` | 构造器保存编号、标题、描述、优先级、截止日期和状态。 |
| `void constructorRejectsNullRequiredFields()` | `id`、`title`、`description`、`priority`、`dueDate`、`status` 任一为空时抛出 `NullPointerException`。 |
| `void constructorRejectsBlankTitle()` | 标题清理后为空时抛出 `IllegalArgumentException`。 |
| `void fromCopiesAllTaskFields()` | `TaskView.from(task)` 复制 `TaskItem` 当前所有字段。 |
| `void fromRejectsNullTask()` | `TaskView.from(null)` 抛出 `NullPointerException`。 |
| `void isCompletedReflectsSnapshotStatus()` | `TaskView` 的 `isCompleted()` 与快照状态一致。 |
| `void fromCreatesSnapshotIndependentFromLaterTaskMutation()` | 从 `TaskItem` 生成 `TaskView` 后再修改实体，已生成快照字段保持旧值。 |

### `InMemoryTaskRepositoryTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试数据约定**：

- 每个测试重新创建 `InMemoryTaskRepository`。
- 使用固定 `EntityId` 和固定 `LocalDate`。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void saveAndFindByIdReturnsStoredTask()` | 保存任务后可按编号查回。 |
| `void findByIdReturnsEmptyWhenTaskDoesNotExist()` | 不存在编号返回 `Optional.empty()`。 |
| `void saveReplacesTaskWithSameId()` | 保存相同编号任务时替换旧记录，`findAll()` 只返回一条该编号记录。 |
| `void findAllReturnsTasksInInsertionOrder()` | 多个任务按首次插入顺序返回。 |
| `void findAllReturnsUnmodifiableSnapshotList()` | `findAll()` 返回列表不可增删，且后续仓储新增不改变已返回列表大小。 |
| `void findByFiltersByStatus()` | 按状态筛选返回匹配任务。 |
| `void findByFiltersByPriority()` | 按优先级筛选返回匹配任务。 |
| `void findByFiltersByDueDate()` | 按截止日期筛选返回匹配任务。 |
| `void findByAppliesCombinedQueryInInsertionOrder()` | 组合筛选只返回同时满足所有条件的任务，并保持插入顺序。 |
| `void findByReturnsUnmodifiableSnapshotList()` | `findBy(...)` 返回列表不可修改。 |
| `void deleteByIdRemovesExistingTask()` | 删除存在任务返回 `true`，后续查找为空。 |
| `void deleteByIdReturnsFalseWhenTaskDoesNotExist()` | 删除不存在编号返回 `false`。 |
| `void methodsRejectNullArguments()` | `save(null)`、`findById(null)`、`findBy(null)`、`deleteById(null)` 抛出 `NullPointerException`。 |

### `TaskServiceTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试数据约定**：

- 每个测试使用新的 `InMemoryTaskRepository` 和 `IncrementalIdGenerator`。
- 使用 `new IncrementalIdGenerator(100)` 等固定起始编号确保创建结果可预测。
- 日期使用 `LocalDate.of(...)` 字面量。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void createTaskStoresTodoTaskAndReturnsTaskView()` | 创建成功返回 `OperationResult<TaskView>`，编号来自生成器，状态为 `TODO`，载荷不是 `TaskItem`。 |
| `void createTaskRejectsBlankTitleAndDoesNotStoreTask()` | 空白标题返回 `VALIDATION_ERROR`，仓储仍为空。 |
| `void createTaskRejectsNullPriorityAndDoesNotStoreTask()` | `priority == null` 返回 `VALIDATION_ERROR`，仓储仍为空。 |
| `void createTaskRejectsNullDueDateAndDoesNotStoreTask()` | `dueDate == null` 返回 `VALIDATION_ERROR`，仓储仍为空。 |
| `void getTaskReturnsTaskViewForExistingTask()` | 查看存在任务成功返回 `TaskView`，载荷运行时类型不是 `TaskItem`。 |
| `void getTaskReturnsNotFoundForMissingTask()` | 查看不存在编号返回 `NOT_FOUND`。 |
| `void getTaskRejectsNullId()` | `id == null` 返回 `VALIDATION_ERROR`。 |
| `void listTasksReturnsUnmodifiableTaskViewListInCreationOrder()` | 列表成功返回不可修改 `List<TaskView>`，按创建顺序排列，元素不是 `TaskItem`。 |
| `void listTasksWithStatusQueryFiltersTasks()` | 按状态筛选返回匹配任务。 |
| `void listTasksWithPriorityQueryFiltersTasks()` | 按优先级筛选返回匹配任务。 |
| `void listTasksWithDueDateQueryFiltersTasks()` | 按截止日期筛选返回匹配任务。 |
| `void listTasksWithCombinedQueryFiltersTasks()` | 组合状态、优先级、截止日期筛选返回同时满足条件的任务。 |
| `void listTasksRejectsNullQuery()` | `listTasks(null)` 返回 `VALIDATION_ERROR`。 |
| `void updateTaskChangesEditableFieldsAndKeepsStatus()` | 修改成功更新标题、描述、优先级、截止日期，保持完成状态不被重置。 |
| `void updateTaskReturnsNotFoundForMissingTask()` | 修改不存在任务返回 `NOT_FOUND`。 |
| `void updateTaskRejectsInvalidFieldsAndKeepsStoredTaskUnchanged()` | 标题非法、优先级为空或截止日期为空返回 `VALIDATION_ERROR`，仓储中原任务字段保持不变。 |
| `void updateTaskRejectsNullId()` | `id == null` 返回 `VALIDATION_ERROR`。 |
| `void deleteTaskRemovesExistingTask()` | 删除存在任务成功返回 `OperationResult.success()`，后续查看返回 `NOT_FOUND`。 |
| `void deleteTaskReturnsNotFoundForMissingTask()` | 删除不存在任务返回 `NOT_FOUND`。 |
| `void deleteTaskRejectsNullId()` | `id == null` 返回 `VALIDATION_ERROR`。 |
| `void markTaskCompletedChangesTodoTaskToCompleted()` | 未完成任务标记完成成功，返回状态为 `COMPLETED` 的 `TaskView`。 |
| `void markTaskCompletedReturnsNotFoundForMissingTask()` | 完成不存在任务返回 `NOT_FOUND`。 |
| `void markTaskCompletedRejectsAlreadyCompletedTaskAndKeepsState()` | 重复完成返回 `STATE_CONFLICT`，后续查看仍为 `COMPLETED`。 |
| `void markTaskCompletedRejectsNullId()` | `id == null` 返回 `VALIDATION_ERROR`。 |
| `void reopenTaskChangesCompletedTaskToTodo()` | 已完成任务撤销完成成功，返回状态为 `TODO` 的 `TaskView`。 |
| `void reopenTaskReturnsNotFoundForMissingTask()` | 撤销不存在任务返回 `NOT_FOUND`。 |
| `void reopenTaskRejectsTodoTaskAndKeepsState()` | 重复撤销返回 `STATE_CONFLICT`，后续查看仍为 `TODO`。 |
| `void reopenTaskRejectsNullId()` | `id == null` 返回 `VALIDATION_ERROR`。 |
| `void returnedTaskViewDoesNotChangeWhenStoredTaskIsUpdatedLater()` | 调用方持有的旧 `TaskView` 不随后续服务修改或状态迁移变化。 |
| `void returnedListCannotModifyServiceStorage()` | 调用方对服务返回列表执行增删操作抛出 `UnsupportedOperationException`，再次查询仓储数据不受影响。 |
| `void filteredResultsAreTaskViewsAndDoNotExposeTaskItems()` | 组合筛选成功载荷为不可修改 `List<TaskView>`，元素不是仓储内部 `TaskItem`。 |

## 错误处理

本轮错误处理以 `TaskService` 作为应用边界统一转换为 `OperationResult`。领域实体和仓储仍使用 Java 标准异常或 `BusinessException` 表达自身契约，服务层负责将面向外部调用方的常见失败路径映射成稳定错误分类。

| 场景 | 发生层 | 对外结果 | 状态要求 |
|------|--------|----------|----------|
| 创建任务标题为空、标题清理后为空、优先级为空或截止日期为空 | `TaskService.createTask(...)` 捕获 `TaskItem` 校验异常 | `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)` | 仓储不新增任务。 |
| 查看、修改、删除、完成、撤销完成时 `id == null` | `TaskService` 入参校验 | `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)` | 仓储不变。 |
| `listTasks(TaskQuery query)` 的 `query == null` | `TaskService` 入参校验 | `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)` | 仓储不变。 |
| 查看不存在任务 | `TaskService.getTask(...)` | `OperationResult.failure(ErrorCode.NOT_FOUND, ...)` | 仓储不变。 |
| 修改不存在任务 | `TaskService.updateTask(...)` | `OperationResult.failure(ErrorCode.NOT_FOUND, ...)` | 仓储不变。 |
| 删除不存在任务 | `TaskService.deleteTask(...)` | `OperationResult.failure(ErrorCode.NOT_FOUND, ...)` | 仓储不变。 |
| 完成或撤销完成不存在任务 | `TaskService.markTaskCompleted(...)` / `reopenTask(...)` | `OperationResult.failure(ErrorCode.NOT_FOUND, ...)` | 仓储不变。 |
| 修改任务字段非法 | `TaskService.updateTask(...)` 捕获 `TaskItem.updateDetails(...)` 校验异常 | `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)` | `TaskItem` 已保证字段原子性，仓储中任务保持调用前状态。 |
| 重复完成 | `TaskItem.markCompleted()` 抛出 `BusinessException` | `OperationResult.failure(ErrorCode.STATE_CONFLICT, ...)` | 状态保持 `COMPLETED`，其他字段不变。 |
| 重复撤销完成 | `TaskItem.reopen()` 抛出 `BusinessException` | `OperationResult.failure(ErrorCode.STATE_CONFLICT, ...)` | 状态保持 `TODO`，其他字段不变。 |
| `TaskQuery` 单条件工厂参数为空 | `TaskQuery` | 抛出 `NullPointerException` | 无仓储状态变化。 |
| `TaskRepository` 方法参数为空 | 仓储契约 | 抛出 `NullPointerException` | 调用方错误，不由仓储转换为 `OperationResult`。 |

失败消息只要求简短可读，不作为单元测试强约束。单元测试断言 `isFailure()`、`getErrorCode()` 和关键状态不变；成功路径断言 `isSuccess()`、载荷类型和载荷字段。

## 行为契约

1. `TaskQuery` 中 `null` 组件表示未启用对应筛选条件；`TaskQuery.all()` 必须匹配所有任务。
2. `TaskQuery` 的状态、优先级和截止日期筛选均使用精确相等语义；本轮不设计截止日期范围筛选。
3. `TaskQuery.matches(task)` 不修改任务实体，不读取系统时间，不访问仓储。
4. `TaskView` 是只读快照，不持有 `TaskItem` 引用；从实体生成快照后，实体后续修改不得改变已生成快照字段。
5. `TaskView` 的所有组件都是不可变或按值使用的对象：`EntityId` record、`String`、enum 和 `LocalDate`。
6. `TaskRepository.findAll()` 和 `TaskRepository.findBy(...)` 返回的列表对象必须不可修改；外部对返回列表的增删操作必须失败。
7. `InMemoryTaskRepository` 使用 `LinkedHashMap` 保持任务首次保存顺序；替换同编号任务不应产生重复记录。
8. 仓储列表是集合快照，不是内部 `Map.values()` 视图；仓储后续新增或删除不改变已返回列表的元素数量。
9. 仓储为任务模块内部编排返回 `TaskItem`；服务层必须立即映射为 `TaskView`，不得将 `TaskItem` 或 `List<TaskItem>` 穿透到 `OperationResult` 载荷。
10. `TaskService` 是外部读写任务数据的边界；所有公开业务方法均返回 `OperationResult`。
11. 创建任务必须通过注入的 `IdGenerator.nextId()` 生成编号，并调用 `TaskItem.createTodo(...)` 创建 `TODO` 状态任务。
12. 修改任务必须复用 `TaskItem.updateDetails(...)`，不得在服务层直接改写实体字段。
13. 标记完成必须复用 `TaskItem.markCompleted()`；撤销完成必须复用 `TaskItem.reopen()`。
14. 修改、完成或撤销完成成功后，服务应调用 `repository.save(task)`，保证即使未来仓储返回非托管实体副本也能持久化变更。
15. 删除任务只删除任务记录，不对编号生成器回退，不影响已返回的 `TaskView` 快照。
16. 服务创建、查看、列表、筛选、修改、完成和撤销完成成功结果中的任务载荷必须是 `TaskView` 或不可修改 `List<TaskView>`。
17. 服务返回的 `List<TaskView>` 必须不可修改；调用方不能通过列表增删影响仓储。
18. 调用方拿到 `TaskView` 后无法通过该对象修改仓储中任务；如后续通过服务再次查询同一任务，应以仓储内部当前状态为准。
19. 输入校验失败、记录不存在和状态冲突失败都不得改变仓储中任务状态或基础字段。
20. 本轮生产代码不得读取真实当前时间、环境变量、文件、网络、API Key 或 DeepSeek API。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.task.TaskQuery` | `assistant.task.TaskStatus`、`assistant.task.TaskPriority`、`assistant.task.TaskItem`、`java.time.LocalDate`、`java.util.Objects`。 |
| `assistant.task.TaskView` | `assistant.common.EntityId`、`assistant.task.TaskItem`、`assistant.task.TaskPriority`、`assistant.task.TaskStatus`、`java.time.LocalDate`、`java.util.Objects`。 |
| `assistant.task.TaskRepository` | `assistant.common.EntityId`、`assistant.task.TaskItem`、`assistant.task.TaskQuery`、`java.util.List`、`java.util.Optional`。 |
| `assistant.task.InMemoryTaskRepository` | `TaskRepository`、`assistant.common.EntityId`、`TaskItem`、`TaskQuery`、`java.util.LinkedHashMap`、`java.util.List`、`java.util.Map`、`java.util.Objects`、`java.util.Optional`。 |
| `assistant.task.TaskService` | `assistant.common.BusinessException`、`assistant.common.EntityId`、`assistant.common.ErrorCode`、`assistant.common.OperationResult`、`assistant.testability.IdGenerator`、`TaskRepository`、`TaskItem`、`TaskQuery`、`TaskView`、`TaskPriority`、`java.time.LocalDate`、`java.util.List`、`java.util.Objects`。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `TaskQueryTest` | JUnit Jupiter、`EntityId`、`TaskItem`、`TaskPriority`、`TaskStatus`、`TaskQuery`、`LocalDate`。 |
| `TaskViewTest` | JUnit Jupiter、`EntityId`、`TaskItem`、`TaskPriority`、`TaskStatus`、`TaskView`、`LocalDate`。 |
| `InMemoryTaskRepositoryTest` | JUnit Jupiter、`EntityId`、`TaskItem`、`TaskPriority`、`TaskStatus`、`TaskQuery`、`InMemoryTaskRepository`、`LocalDate`、`List`。 |
| `TaskServiceTest` | JUnit Jupiter、`EntityId`、`ErrorCode`、`OperationResult`、`IncrementalIdGenerator`、`TaskService`、`InMemoryTaskRepository`、`TaskQuery`、`TaskView`、`TaskPriority`、`TaskStatus`、`LocalDate`、`List`。 |

后续任务中的汇总服务应只读调用 `TaskService.listTasks(...)` 获取 `TaskView` 快照，不直接持有 `TaskRepository` 或 `TaskItem`。后续 AI 草稿导入服务在用户确认任务草稿时应调用 `TaskService.createTask(...)`，复用本轮服务校验和错误分类，不能绕过服务直接写入仓储。
