# 详细设计（v7）

## 概述

本轮设计目标是在既有 `java-ai-assistant/` Maven 单模块工程中新增任务待办模块的核心领域模型：`assistant.task.TaskPriority`、`assistant.task.TaskStatus` 和 `assistant.task.TaskItem`，并补充对应 JUnit Jupiter 单元测试规格。

`TaskPriority` 和 `TaskStatus` 使用 Java enum 固定任务优先级与完成状态取值。`TaskItem` 使用普通 Java class 表达一条可修改、可完成、可撤销完成的待办任务，组合既有 `assistant.common.EntityId` 作为不可变唯一标识，并持有标题、描述、优先级、截止日期和状态。实体自身负责保护基础不变量：编号非空、标题清理后非空、描述规范化为非空字符串、优先级非空、截止日期非空、状态非空，以及重复完成和重复撤销时状态保持不变。

本轮范围仅包含 `assistant.task` 包下的领域模型基础和单元测试，不实现 `TaskService`、`TaskQuery`、任务仓储、汇总服务、控制台交互、AI 草稿解析或 AI 草稿导入。后续服务层可以在创建、修改、删除、筛选和草稿导入时复用本轮实体契约，并将实体抛出的状态冲突转换为稳定应用结果。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/task/TaskPriority.java` | 新建 | 定义任务优先级枚举，固定低、中、高三档取值并提供默认优先级入口。 |
| `java-ai-assistant/src/main/java/assistant/task/TaskStatus.java` | 新建 | 定义任务完成状态枚举，固定未完成和已完成取值并提供完成状态语义判断。 |
| `java-ai-assistant/src/main/java/assistant/task/TaskItem.java` | 新建 | 定义任务领域实体，封装任务字段、输入规范化、基础信息修改和完成状态迁移。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskPriorityTest.java` | 新建 | 覆盖优先级枚举固定取值、默认优先级和 enum 字符串稳定性。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskStatusTest.java` | 新建 | 覆盖任务状态固定取值、完成状态语义和 enum 字符串稳定性。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java` | 新建 | 覆盖任务实体构造、字段规范化、非法输入、基础信息修改、状态迁移、重复状态迁移冲突和状态不变。 |

## 类型定义

### `TaskPriority`

**形态**：`enum`

**包路径**：`assistant.task`

**职责**：限定待办任务优先级的固定业务取值，避免后续服务层、查询条件和 AI 草稿导入流程使用裸字符串表达优先级。

**类型签名定义**：`public enum TaskPriority`

**枚举常量**：

| 常量 | 语义 |
|------|------|
| `LOW` | 低优先级任务。 |
| `MEDIUM` | 中优先级任务，也是本轮默认优先级。 |
| `HIGH` | 高优先级任务。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static TaskPriority defaultPriority()` | `TaskPriority` | 返回 `TaskPriority.MEDIUM`；后续 `TaskService` 在用户未显式选择优先级且业务允许默认值时可调用。 |
| `public static TaskPriority valueOf(String name)` | `TaskPriority` | Java enum 自动提供；只接受精确枚举名称，非法字符串抛出 `IllegalArgumentException`。本轮不包装解析错误。 |
| `public static TaskPriority[] values()` | `TaskPriority[]` | Java enum 自动提供；返回声明顺序 `LOW`、`MEDIUM`、`HIGH`。 |
| `public String name()` | `String` | Java enum 自动提供；返回常量名称。 |

**构造方式**：

- 业务代码直接引用 `TaskPriority.LOW`、`TaskPriority.MEDIUM` 或 `TaskPriority.HIGH`。
- 默认优先级通过 `TaskPriority.defaultPriority()` 获取。
- 本轮不提供 `fromString`、本地化展示名或排序权重；控制台输入解析和非法优先级转换属于后续应用服务或 UI 边界职责。

**类型关系**：被 `assistant.task.TaskItem` 组合持有；后续 `TaskQuery` 可用它表达按优先级筛选条件。

### `TaskStatus`

**形态**：`enum`

**包路径**：`assistant.task`

**职责**：限定待办任务状态的固定业务取值，并提供稳定完成判断语义，避免后续汇总统计或查询逻辑重复比较状态细节。

**类型签名定义**：`public enum TaskStatus`

**枚举常量**：

| 常量 | 语义 |
|------|------|
| `TODO` | 未完成任务。 |
| `COMPLETED` | 已完成任务。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public boolean isCompleted()` | `boolean` | 当且仅当当前枚举值为 `COMPLETED` 时返回 `true`；`TODO` 返回 `false`。 |
| `public static TaskStatus valueOf(String name)` | `TaskStatus` | Java enum 自动提供；只接受精确枚举名称，非法字符串抛出 `IllegalArgumentException`。 |
| `public static TaskStatus[] values()` | `TaskStatus[]` | Java enum 自动提供；返回声明顺序 `TODO`、`COMPLETED`。 |
| `public String name()` | `String` | Java enum 自动提供；返回常量名称。 |

**构造方式**：

- 业务代码直接引用 `TaskStatus.TODO` 或 `TaskStatus.COMPLETED`。
- 新建普通未完成任务时由 `TaskItem.createTodo(...)` 使用 `TaskStatus.TODO`。
- 任务状态变更必须通过 `TaskItem.markCompleted()` 和 `TaskItem.reopen()`，后续服务层不得直接绕过实体状态迁移规则。

**类型关系**：被 `assistant.task.TaskItem` 组合持有；后续 `TaskQuery` 可用它表达按状态筛选条件；后续 `SummaryService` 可通过 `TaskStatus.isCompleted()` 或 `TaskItem.isCompleted()` 判断完成状态。

### `TaskItem`

**形态**：`class`

**包路径**：`assistant.task`

**职责**：表达一条可跟踪完成状态的待办任务实体，集中维护任务字段不变量、基础信息修改规则和完成状态迁移冲突规则。

**类型签名定义**：`public class TaskItem`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final EntityId id` | 构造后不可变 | 必须非空；使用 `Objects.requireNonNull(id, "id")` 校验。 |
| `private String title` | 可通过 `updateDetails` 修改 | 必须非空；调用 `strip()` 去除首尾 Unicode 空白后不得为空；保存规范化后文本。 |
| `private String description` | 可通过 `updateDetails` 修改 | 允许输入 `null`；`null` 规范化为空字符串；非空输入调用 `strip()` 后保存，允许清理后为空。 |
| `private TaskPriority priority` | 可通过 `updateDetails` 修改 | 必须非空；使用 `Objects.requireNonNull(priority, "priority")` 校验。 |
| `private LocalDate dueDate` | 可通过 `updateDetails` 修改 | 必须非空；使用 `Objects.requireNonNull(dueDate, "dueDate")` 校验；本轮不比较当前日期。 |
| `private TaskStatus status` | 只能通过状态方法修改 | 必须非空；使用 `Objects.requireNonNull(status, "status")` 校验。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TaskItem(EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate, TaskStatus status)` | 构造器 | 创建任务实体；校验并规范化所有字段；任一必填引用为 `null` 时抛出 `NullPointerException`；标题清理后为空时抛出 `IllegalArgumentException`。 |
| `public static TaskItem createTodo(EntityId id, String title, String description, TaskPriority priority, LocalDate dueDate)` | `TaskItem` | 创建初始状态为 `TaskStatus.TODO` 的任务；其他字段校验与构造器一致。 |
| `public EntityId getId()` | `EntityId` | 返回任务编号；编号对象不可变，实体生命周期内引用不变。 |
| `public String getTitle()` | `String` | 返回规范化后的标题；返回值非空、非空白且无首尾空白。 |
| `public String getDescription()` | `String` | 返回规范化后的描述；返回值非空，可能为空字符串。 |
| `public TaskPriority getPriority()` | `TaskPriority` | 返回当前优先级。 |
| `public LocalDate getDueDate()` | `LocalDate` | 返回当前截止日期。 |
| `public TaskStatus getStatus()` | `TaskStatus` | 返回当前任务状态。 |
| `public boolean isCompleted()` | `boolean` | 等价于 `status.isCompleted()`；用于后续查询、统计和测试断言。 |
| `public void updateDetails(String title, String description, TaskPriority priority, LocalDate dueDate)` | `void` | 更新标题、描述、优先级和截止日期；复用构造期字段校验和规范化；不修改 `id` 和 `status`。若任一新值非法，方法抛出异常，并且实体全部字段保持调用前状态。 |
| `public void markCompleted()` | `void` | 当前状态为 `TODO` 时切换为 `COMPLETED`；当前状态已为 `COMPLETED` 时抛出 `BusinessException(ErrorCode.STATE_CONFLICT, ...)`，状态保持 `COMPLETED`。 |
| `public void reopen()` | `void` | 当前状态为 `COMPLETED` 时切换为 `TODO`；当前状态已为 `TODO` 时抛出 `BusinessException(ErrorCode.STATE_CONFLICT, ...)`，状态保持 `TODO`。 |

**构造方式**：

- 后续服务层需要精确指定初始状态时调用完整构造器。
- 新建普通待办任务时调用 `TaskItem.createTodo(id, title, description, priority, dueDate)`，避免服务层重复传入 `TaskStatus.TODO`。
- 任务编号由后续 `TaskService` 协作 `assistant.testability.IdGenerator` 生成后包装为 `EntityId`，本轮 `TaskItem` 不生成编号。

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String normalizeTitle(String title)` | `String` | `title == null` 时抛出 `NullPointerException`；调用 `strip()` 后为空时抛出 `IllegalArgumentException`；否则返回清理后标题。 |
| `private static String normalizeDescription(String description)` | `String` | `description == null` 时返回 `""`；否则返回 `description.strip()`。 |

私有辅助方法仅为编码复用设计，不作为公开 API，不需要单独测试私有方法；通过构造器和 `updateDetails` 的公开行为覆盖。

**类型关系**：

- 组合 `assistant.common.EntityId`、`assistant.task.TaskPriority`、`assistant.task.TaskStatus` 和 `java.time.LocalDate`。
- 状态冲突依赖 `assistant.common.BusinessException` 与 `assistant.common.ErrorCode.STATE_CONFLICT`。
- 不实现 `Comparable`、不覆盖 `equals` 和 `hashCode`，本轮不定义实体相等性；后续仓储按 `EntityId` 管理任务唯一性。
- 不依赖 `OperationResult`、`IdGenerator`、`TimeProvider`、AI 客户端或文件系统。

## 单元测试规格

### `TaskPriorityTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void exposesFixedPriorityValuesInDeclaredOrder()` | `TaskPriority.values()` 返回 `LOW`、`MEDIUM`、`HIGH`。 |
| `void defaultPriorityReturnsMedium()` | `TaskPriority.defaultPriority()` 返回 `TaskPriority.MEDIUM`。 |
| `void valueOfParsesDeclaredPriorityName()` | `TaskPriority.valueOf("HIGH")` 返回 `TaskPriority.HIGH`。 |
| `void valueOfRejectsUnknownPriorityName()` | `TaskPriority.valueOf("URGENT")` 抛出 `IllegalArgumentException`。 |
| `void nameUsesStableEnumConstantName()` | `TaskPriority.LOW.name()` 返回 `LOW`。 |

### `TaskStatusTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void exposesFixedStatusValuesInDeclaredOrder()` | `TaskStatus.values()` 返回 `TODO`、`COMPLETED`。 |
| `void todoIsNotCompleted()` | `TaskStatus.TODO.isCompleted()` 返回 `false`。 |
| `void completedIsCompleted()` | `TaskStatus.COMPLETED.isCompleted()` 返回 `true`。 |
| `void valueOfParsesDeclaredStatusName()` | `TaskStatus.valueOf("COMPLETED")` 返回 `TaskStatus.COMPLETED`。 |
| `void valueOfRejectsUnknownStatusName()` | `TaskStatus.valueOf("DONE")` 抛出 `IllegalArgumentException`。 |
| `void nameUsesStableEnumConstantName()` | `TaskStatus.TODO.name()` 返回 `TODO`。 |

### `TaskItemTest`

**包路径**：`assistant.task`

**测试框架**：JUnit Jupiter 5.14.4

**测试数据约定**：

- 使用 `new EntityId(1)`、`new EntityId(2)` 等固定编号。
- 使用固定 `LocalDate.of(2026, 6, 30)` 等字面量日期。
- 不读取真实当前日期、系统时间、网络、API Key 或外部文件。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorStoresProvidedFields()` | 完整构造器保存编号、标题、描述、优先级、截止日期和状态。 |
| `void createTodoCreatesTaskWithTodoStatus()` | `createTodo(...)` 创建 `TODO` 状态任务，`isCompleted()` 返回 `false`。 |
| `void normalizesTitleAndDescription()` | 标题和描述均调用 `strip()` 去除首尾 Unicode 空白后保存。 |
| `void convertsNullDescriptionToEmptyString()` | 描述入参为 `null` 时保存为空字符串。 |
| `void allowsBlankDescriptionAsEmptyString()` | 描述入参清理后为空时合法，保存为空字符串。 |
| `void rejectsNullId()` | 构造器和 `createTodo` 对 `id == null` 抛出 `NullPointerException`。 |
| `void rejectsNullTitle()` | 构造器和 `createTodo` 对 `title == null` 抛出 `NullPointerException`。 |
| `void rejectsBlankTitle()` | 构造器和 `createTodo` 对空字符串、ASCII 纯空白和 Unicode 纯空白标题抛出 `IllegalArgumentException`。 |
| `void rejectsNullPriority()` | 构造器、`createTodo` 和 `updateDetails` 对 `priority == null` 抛出 `NullPointerException`。 |
| `void rejectsNullDueDate()` | 构造器、`createTodo` 和 `updateDetails` 对 `dueDate == null` 抛出 `NullPointerException`。 |
| `void rejectsNullStatus()` | 完整构造器对 `status == null` 抛出 `NullPointerException`。 |
| `void updateDetailsChangesEditableFieldsOnly()` | `updateDetails(...)` 更新标题、描述、优先级和截止日期，保持 `id` 和 `status` 不变。 |
| `void updateDetailsNormalizesNewTitleAndDescription()` | `updateDetails(...)` 对新标题和新描述复用构造期规范化。 |
| `void updateDetailsLeavesTaskUnchangedWhenTitleIsInvalid()` | 标题非法时抛出异常，编号、原标题、原描述、原优先级、原截止日期和原状态全部保持调用前值。 |
| `void updateDetailsLeavesTaskUnchangedWhenPriorityIsNull()` | 优先级为空时抛出异常，全部字段保持调用前值。 |
| `void updateDetailsLeavesTaskUnchangedWhenDueDateIsNull()` | 截止日期为空时抛出异常，全部字段保持调用前值。 |
| `void markCompletedChangesTodoTaskToCompleted()` | `TODO` 任务调用 `markCompleted()` 后状态为 `COMPLETED`，`isCompleted()` 返回 `true`。 |
| `void markCompletedRejectsAlreadyCompletedTaskAndKeepsState()` | 已完成任务再次调用 `markCompleted()` 抛出 `BusinessException`，`getErrorCode()` 为 `STATE_CONFLICT`，状态仍为 `COMPLETED`。 |
| `void reopenChangesCompletedTaskToTodo()` | `COMPLETED` 任务调用 `reopen()` 后状态为 `TODO`，`isCompleted()` 返回 `false`。 |
| `void reopenRejectsTodoTaskAndKeepsState()` | 未完成任务调用 `reopen()` 抛出 `BusinessException`，`getErrorCode()` 为 `STATE_CONFLICT`，状态仍为 `TODO`。 |
| `void repeatedConflictDoesNotChangeEditableFields()` | 重复完成或重复撤销失败后，标题、描述、优先级和截止日期仍保持失败前值。 |

## 错误处理

本轮错误处理分为字段输入错误和业务状态冲突两类。

| 场景 | 异常类型 | 错误分类 | 状态要求 |
|------|----------|----------|----------|
| `TaskItem` 构造器或 `createTodo` 的 `id == null` | `NullPointerException` | 无 | 构造失败，无实体产生。 |
| `TaskItem` 构造器或 `createTodo` 的 `title == null` | `NullPointerException` | 无 | 构造失败，无实体产生。 |
| `TaskItem` 构造器或 `createTodo` 的标题清理后为空 | `IllegalArgumentException` | 无 | 构造失败，无实体产生。 |
| `TaskItem` 构造器或 `createTodo` 的 `priority == null` | `NullPointerException` | 无 | 构造失败，无实体产生。 |
| `TaskItem` 构造器或 `createTodo` 的 `dueDate == null` | `NullPointerException` | 无 | 构造失败，无实体产生。 |
| `TaskItem` 完整构造器的 `status == null` | `NullPointerException` | 无 | 构造失败，无实体产生。 |
| `updateDetails` 的标题非法、优先级为空或截止日期为空 | `NullPointerException` 或 `IllegalArgumentException` | 无 | 实体所有字段保持调用前值。 |
| `markCompleted` 作用于 `COMPLETED` 任务 | `BusinessException` | `ErrorCode.STATE_CONFLICT` | 状态保持 `COMPLETED`，其他字段不变。 |
| `reopen` 作用于 `TODO` 任务 | `BusinessException` | `ErrorCode.STATE_CONFLICT` | 状态保持 `TODO`，其他字段不变。 |

字段输入错误沿用当前 `assistant.common` 基础类型的风格，直接使用 Java 标准异常，不引入新的 `ErrorCode`。重复完成和重复撤销属于业务状态错误，使用既有 `BusinessException` 携带 `ErrorCode.STATE_CONFLICT`，便于后续 `TaskService`、AI 草稿导入和控制台层转换为统一失败结果。

异常消息只要求简短可读，不作为本轮测试强约束。单元测试断言异常类型；对 `BusinessException` 额外断言 `getErrorCode()`。

## 行为契约

1. `TaskPriority` 的业务取值固定为 `LOW`、`MEDIUM`、`HIGH`，本轮默认优先级固定为 `MEDIUM`。
2. `TaskStatus` 的业务取值固定为 `TODO`、`COMPLETED`，`isCompleted()` 是后续查询和汇总判断完成状态的稳定 API。
3. `TaskItem` 的 `id` 在构造成功后不可变，且不允许通过任何公开方法替换。
4. `TaskItem` 的标题在构造和修改时都必须调用 `strip()` 去除首尾 Unicode 空白，清理后为空必须拒绝。
5. `TaskItem` 的描述允许 `null` 输入，构造和修改时都规范化为非空字符串；`null` 和清理后空白描述保存为 `""`。
6. `TaskItem` 不压缩、不拆分、不删除标题或描述内部空白。
7. `TaskItem` 的优先级、截止日期和状态必须始终非空。
8. `TaskItem` 本轮不基于真实当前日期判断截止日期是否逾期，任意非空 `LocalDate` 均可保存。
9. `updateDetails` 只能修改标题、描述、优先级和截止日期，不得修改编号和状态。
10. `updateDetails` 必须先完成全部新值校验和规范化，再写入字段；任一新值非法时，实体保持调用前完整状态。
11. `markCompleted()` 是从 `TODO` 到 `COMPLETED` 的唯一公开状态迁移入口。
12. `reopen()` 是从 `COMPLETED` 到 `TODO` 的唯一公开状态迁移入口。
13. 重复调用 `markCompleted()` 不得静默成功，不得重复改变统计依据；必须抛出 `BusinessException` 且错误分类为 `STATE_CONFLICT`。
14. 对 `TODO` 任务调用 `reopen()` 不得静默成功；必须抛出 `BusinessException` 且错误分类为 `STATE_CONFLICT`。
15. 任一状态冲突失败后，任务状态和基础信息都必须保持失败前值。
16. 本轮生产代码不得读取系统当前时间、环境变量、文件、网络、AI 配置或 DeepSeek API。
17. 本轮单元测试必须使用固定编号、固定日期和固定字符串，不依赖真实当前时间、网络、API Key 或外部文件。
18. 后续 `TaskService` 应负责仓储查找、编号生成、删除不存在任务、筛选任务、服务层统一结果转换和 AI 草稿导入协调；不得把这些职责提前放入本轮实体。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.task.TaskPriority` | Java enum 语言特性。 |
| `assistant.task.TaskStatus` | Java enum 语言特性。 |
| `assistant.task.TaskItem` | `assistant.common.EntityId`、`assistant.common.BusinessException`、`assistant.common.ErrorCode`、`assistant.task.TaskPriority`、`assistant.task.TaskStatus`、`java.time.LocalDate`、`java.util.Objects`。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `assistant.task.TaskPriorityTest` | JUnit Jupiter 断言 API、`assistant.task.TaskPriority`。 |
| `assistant.task.TaskStatusTest` | JUnit Jupiter 断言 API、`assistant.task.TaskStatus`。 |
| `assistant.task.TaskItemTest` | JUnit Jupiter 断言 API、`assistant.common.EntityId`、`assistant.common.BusinessException`、`assistant.common.ErrorCode`、`assistant.task.TaskItem`、`assistant.task.TaskPriority`、`assistant.task.TaskStatus`、`java.time.LocalDate`。 |

后续任务中的 `assistant.task.TaskService` 应组合 `TaskItem` 完成新增、修改、删除、标记完成、撤销完成和筛选用例；后续 `TaskQuery` 应使用 `TaskStatus` 与 `TaskPriority` 表达查询条件；后续 `SummaryService` 和 AI 本地上下文生成逻辑应通过服务层只读查询任务，不直接持有或修改任务仓储。
