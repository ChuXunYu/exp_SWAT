# 任务指令（v14）

## 动作
NEW

## 任务描述
实现收支记录模块的查询、只读视图、仓储、服务与统计闭环。新增文件预期包括：

- `java-ai-assistant/src/main/java/assistant/finance/TransactionQuery.java`
- `java-ai-assistant/src/main/java/assistant/finance/TransactionView.java`
- `java-ai-assistant/src/main/java/assistant/finance/TransactionRepository.java`
- `java-ai-assistant/src/main/java/assistant/finance/InMemoryTransactionRepository.java`
- `java-ai-assistant/src/main/java/assistant/finance/FinanceService.java`
- `java-ai-assistant/src/main/java/assistant/finance/FinanceStatisticsService.java`
- `java-ai-assistant/src/test/java/assistant/finance/TransactionQueryTest.java`
- `java-ai-assistant/src/test/java/assistant/finance/TransactionViewTest.java`
- `java-ai-assistant/src/test/java/assistant/finance/InMemoryTransactionRepositoryTest.java`
- `java-ai-assistant/src/test/java/assistant/finance/FinanceServiceTest.java`
- `java-ai-assistant/src/test/java/assistant/finance/FinanceStatisticsServiceTest.java`

本轮必须完成：

1. `TransactionQuery` 表达组合查询条件，支持按 `TransactionType`、规范化后的类别和 `DateRange` 筛选；空条件表示全部记录；日期范围使用左右闭语义。
2. `TransactionView` 作为只读 DTO/record，从 `TransactionRecord` 投影编号、类型、金额、类别、日期和备注，服务层成功查询和写操作返回视图或不可修改的 `List<TransactionView>`，不得返回内部 `TransactionRecord` 引用。
3. `TransactionRepository` 与 `InMemoryTransactionRepository` 提供 `save`、`findById`、`findAll`、`findBy`、`deleteById`；内存实现使用 `LinkedHashMap<EntityId, TransactionRecord>` 保持插入顺序，并对保存和读取都做实体快照隔离，禁止外部通过保存后仍持有的对象或仓储返回值绕过服务修改内部状态。
4. `FinanceStatisticsService` 从 `List<TransactionRecord>` 或等价只读记录集合计算 `FinanceStatistics`，按 `TransactionType.INCOME` 累加收入、按 `TransactionType.EXPENSE` 累加支出，空集合返回 `FinanceStatistics.zero()`，统计计算禁止使用 `double`。
5. `FinanceService` 负责记录收入、记录支出、查看、列表、组合筛选、修改和删除收支记录，并提供基于当前仓储状态和可选查询条件的统计入口。

服务接口边界需要收束为唯一可执行形态：

- 创建接口至少提供 `recordIncome(String amountText, String category, LocalDate date, String note)` 与 `recordExpense(String amountText, String category, LocalDate date, String note)`，服务内部将原始金额文本转换为 `TransactionAmount`，非法金额、空类别、空日期统一映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。
- 修改接口固定为 `updateTransaction(EntityId id, TransactionType type, String amountText, String category, LocalDate date, String note)` 或唯一等价命名，服务内部转换金额并调用实体更新；`id == null` 映射为 `VALIDATION_ERROR`，不存在编号映射为 `NOT_FOUND`，`type == null` 作为未知或空收支类型的唯一可执行表现，必须由 `FinanceService` 映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不得向调用方泄漏 `NullPointerException` 或 `IllegalArgumentException`；非法输入失败后仓储状态不变。
- 删除接口返回 `OperationResult<Void>`；`id == null` 映射为 `VALIDATION_ERROR`，不存在编号映射为 `NOT_FOUND`。
- 查询接口包括查看单条、列出全部、按 `TransactionQuery` 组合筛选；`query == null` 映射为 `VALIDATION_ERROR`。
- 统计接口包括全量统计和按查询条件统计，返回 `OperationResult<FinanceStatistics>`；`query == null` 映射为 `VALIDATION_ERROR`，统计不修改仓储。

## 选择理由
v13 已完成 `TransactionType`、`TransactionRecord` 和 `FinanceStatistics`，固定了收支方向、单条记录校验、文本规范化、详情更新和统计结果一致性。本轮在这些领域基础上补齐服务闭环，使收支记录核心功能具备创建、读取、筛选、修改、删除和即时统计能力，并为后续汇总服务、本月收支统计、AI 本地上下文和控制台入口提供稳定公开 API。

## 任务上下文
需求和技术方案要求：

- 用户可以记录收入和支出，字段包括金额、类别、日期和备注。
- 程序应支持查看全部记录、按类别或日期范围查询记录，并计算收入总额、支出总额和结余。
- 程序应拒绝负金额、零金额、非法日期、未知收支类型和开始日期晚于结束日期的查询。
- 服务层修改收支记录时，未知收支类型在当前 enum 接口形态下固定表现为 `type == null`，必须返回 `VALIDATION_ERROR` 且保持原记录不变。
- `TransactionQuery` 组合支持类型、类别和 `DateRange`。
- `FinanceStatisticsService` 从查询结果计算收入总额、支出总额和结余，空集合返回三个零值。
- 只有支出无收入时结余允许为负，统计计算使用 `BigDecimal.add` 或既有金额值对象，禁止使用 `double`。
- 删除记录后，后续统计必须基于当前记录集合重新计算。
- 普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## 已有代码上下文
已存在的相关类型：

- `assistant.common.EntityId`：正整数编号值对象。
- `assistant.testability.IdGenerator`：服务层创建记录时生成可预测编号。
- `assistant.common.OperationResult`、`ErrorCode`：服务层返回成功或失败分类。
- `assistant.common.TransactionAmount`：单笔金额值对象，要求金额大于 0 且最多两位小数，支持 `TransactionAmount.of(String)`。
- `assistant.common.MoneyValue`：统计金额值对象，允许零值和负值，支持 `MoneyValue.from(TransactionAmount)`、`add(...)`、`subtract(...)`。
- `assistant.common.DateRange`：左右闭日期区间，开始日期晚于结束日期时抛出 `IllegalArgumentException`，支持 `contains(LocalDate)`。
- `assistant.finance.TransactionType`：已有 `INCOME`、`EXPENSE` 及 `isIncome()`、`isExpense()`。
- `assistant.finance.TransactionRecord`：已有 `create(...)`、getter 和 `updateDetails(...)`；类别会 `strip()` 且不能为空，备注允许 `null` 并规范化为 `""`。
- `assistant.finance.FinanceStatistics`：已有 `zero()` 和 `of(MoneyValue totalIncome, MoneyValue totalExpense)`，会校验收入/支出非负并计算结余。

可参考既有模块模式：

- `assistant.task.TaskQuery`、`TaskView`、`TaskRepository`、`InMemoryTaskRepository`、`TaskService`
- `assistant.schedule.ScheduleQuery`、`ScheduleView`、`ScheduleRepository`、`InMemoryScheduleRepository`、`ScheduleService`
- `assistant.study.StudyPlanQuery`、`StudyPlanView`、`StudyPlanRepository`、`InMemoryStudyPlanRepository`、`StudyPlanService`

本轮需要特别采用 `study` 模块的仓储快照隔离做法，而不是暴露内部可变实体引用。

## 修订说明（v14 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| `updateTransaction(...)` 未明确 `type == null` 或未知收支类型应由服务层捕获并映射为稳定失败结果，可能泄漏实体层运行时异常。 | 在服务接口边界中明确 `type == null` 是未知或空收支类型的唯一可执行表现，必须由 `FinanceService` 映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不得泄漏 `NullPointerException` 或 `IllegalArgumentException`。 |
| 缺少对应白盒测试边界，无法验证非法类型失败后仓储状态不变。 | 在任务上下文中补充：服务层修改收支记录时 `type == null` 必须返回 `VALIDATION_ERROR` 且保持原记录不变，后续 `FinanceServiceTest` 需覆盖该断言。 |
