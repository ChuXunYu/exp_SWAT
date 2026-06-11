# 任务指令（v7）

## 动作
NEW

## 任务描述
新增任务待办模块的核心领域实体与枚举，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/task/TaskPriority.java`
- `java-ai-assistant/src/main/java/assistant/task/TaskStatus.java`
- `java-ai-assistant/src/main/java/assistant/task/TaskItem.java`
- `java-ai-assistant/src/test/java/assistant/task/TaskPriorityTest.java`
- `java-ai-assistant/src/test/java/assistant/task/TaskStatusTest.java`
- `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java`

本轮实现范围限定为 `assistant.task` 的领域模型基础，不实现 `TaskService`、`TaskQuery`、任务仓储、汇总服务、控制台交互或 AI 草稿导入。`TaskItem` 应作为普通 Java class 封装任务自身状态变化，组合使用已存在的 `assistant.common.EntityId`，并持有标题、描述、优先级、截止日期和状态。`TaskPriority` 与 `TaskStatus` 使用 enum 表达固定业务取值。

## 选择理由
通用编号、日期、时间、金额、进度和标签基础已经完成，可以开始进入 8 个核心功能中的任务待办管理。任务实体、优先级和状态是后续 `TaskService`、`TaskQuery`、任务仓储、汇总统计和 AI 任务草稿导入的直接依赖。

先实现任务领域模型可把标题校验、描述规范化、截止日期持有、状态流转和重复状态变更冲突等规则固定在可单独测试的对象中，避免后续服务层同时承担实体行为、仓储编排和结果转换，降低单轮实现风险。

## 任务上下文
完整需求要求任务待办管理支持新增、查看、修改、删除待办任务；每条任务至少包含标题、描述、优先级、截止日期和完成状态；支持标记完成、撤销完成，并能按状态或优先级筛选。需要明确处理标题为空、优先级非法、删除不存在任务、重复标记完成、撤销完成等情况。

技术方案要求：

- 任务实体持有标题、描述、优先级、截止日期、完成状态和编号。
- 优先级、状态使用 enum。
- 新增和修改任务时由服务集中校验标题非空、优先级合法、日期合法；本轮实体仍需保护自身不进入非法状态。
- 标记完成和撤销完成先查找记录，再检查状态迁移。
- 重复完成、重复撤销统一返回或表达 `STATE_CONFLICT`，任务状态不变。
- 查询条件由后续 `TaskQuery` 表达，本轮不实现筛选逻辑。

本轮建议契约：

- `TaskPriority` 至少提供 `LOW`、`MEDIUM`、`HIGH` 三档固定优先级；可提供 `defaultPriority()` 返回默认优先级。
- `TaskStatus` 至少提供 `TODO`、`COMPLETED` 两种状态；可提供 `isCompleted()` 语义方法。
- `TaskItem` 构造参数包括 `EntityId id`、`String title`、`String description`、`TaskPriority priority`、`LocalDate dueDate` 和初始 `TaskStatus status`，也可提供创建未完成任务的静态工厂。
- `TaskItem` 必须拒绝空编号、空标题、空优先级、空截止日期和空状态。
- 标题应使用 `strip()` 去除首尾空白后保存，清理后为空时拒绝。
- 描述允许为空值输入但应规范化为非空字符串；建议 `null` 作为空描述处理，非空输入去除首尾空白。
- 截止日期使用 `LocalDate` 持有，本轮不基于真实当前时间判断逾期。
- `markCompleted()` 在 `TODO` 时切换为 `COMPLETED`；已完成时应抛出带 `ErrorCode.STATE_CONFLICT` 的 `BusinessException` 或等价稳定业务错误。
- `reopen()` 或 `markTodo()` 在 `COMPLETED` 时切换为 `TODO`；未完成时应抛出带 `ErrorCode.STATE_CONFLICT` 的 `BusinessException` 或等价稳定业务错误。
- 修改基础信息的方法应能更新标题、描述、优先级和截止日期，并复用构造期校验；状态不应被该方法直接覆盖。
- 对外返回的字符串字段应是规范化后的值，实体编号不可变，状态只能通过显式状态方法变化。

普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## 已有代码上下文
当前工程位于 `java-ai-assistant/`，已经具备 Maven 单模块构建、Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe 基线。

已完成的相关生产类型：

- `assistant.common.EntityId`：正整数编号 record，可作为任务唯一标识。
- `assistant.common.ErrorCode`：已包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT` 等错误分类。
- `assistant.common.BusinessException`：持有 `ErrorCode` 的运行时业务异常，可用于表达重复完成、重复撤销等状态冲突。
- `assistant.common.OperationResult<T>`：后续服务层可用的统一返回结果，本轮实体模型不需要直接依赖。
- `assistant.testability.IdGenerator` 与 `IncrementalIdGenerator`：后续 `TaskService` 创建任务时使用，本轮不需要生成编号。
- `assistant.testability.TimeProvider`、`FixedTimeProvider`、`SystemTimeProvider`：后续汇总和日期状态使用，本轮不要直接读取当前时间。
- `assistant.common.DateRange`、`DateTimeRange`、`Progress`、`Tag`、金额值对象：本轮无需直接依赖，但需保持同类值对象和实体的校验风格一致。

已有测试集中使用 JUnit Jupiter，测试文件按包路径放在 `java-ai-assistant/src/test/java/assistant/...`。本轮应新增 `assistant.task` 包测试，覆盖枚举语义、合法构造、字段规范化、非法输入、修改基础信息、完成状态迁移、重复完成冲突、撤销完成和重复撤销冲突，并断言失败时任务状态保持不变。
