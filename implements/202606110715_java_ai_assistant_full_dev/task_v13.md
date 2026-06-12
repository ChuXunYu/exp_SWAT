# 任务指令（v13）

## 动作
NEW

## 任务描述
新增收支记录模块的核心领域实体、类型枚举和统计结果值对象，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/finance/TransactionType.java`
- `java-ai-assistant/src/main/java/assistant/finance/TransactionRecord.java`
- `java-ai-assistant/src/main/java/assistant/finance/FinanceStatistics.java`
- `java-ai-assistant/src/test/java/assistant/finance/TransactionTypeTest.java`
- `java-ai-assistant/src/test/java/assistant/finance/TransactionRecordTest.java`
- `java-ai-assistant/src/test/java/assistant/finance/FinanceStatisticsTest.java`

本轮只实现收支领域模型与统计结果基础，不实现 `TransactionQuery`、`TransactionRepository`、`FinanceService` 或 `FinanceStatisticsService`。后续轮次再基于本轮类型补齐查询、仓储、服务闭环和跨记录统计计算。

## 选择理由
任务待办、日程提醒和学习计划三个核心功能已经形成服务闭环，可以进入 8 个核心功能中的收支记录管理。既有 `TransactionAmount` 和 `MoneyValue` 已提供金额基础，本轮先固定收支方向、单条交易记录不变量和统计结果的收入/支出/结余语义，能避免后续服务层重复处理类别清理、日期校验、备注规范化和结余计算规则。

## 任务上下文
来自技术方案和 OOD 的直接约束：

- 收支记录持有类型、`TransactionAmount`、类别、日期和备注。
- 收入与支出使用 `TransactionType` enum。
- 新增收支记录时，金额必须大于零、类型必须为收入或支出、日期必须合法。
- `FinanceStatistics` 表示收入总额、支出总额和结余等汇总信息，适合作为不可变值对象。
- 空记录统计后续应返回三个零值；只有支出无收入时结余允许为负。
- 统计计算使用 `BigDecimal.add` 或既有金额值对象能力，禁止使用 `double`。

本轮建议接口边界：

- `TransactionType` 至少包含 `INCOME` 与 `EXPENSE`，并提供清晰的方向判断方法，例如 `isIncome()`、`isExpense()`。
- `TransactionRecord` 作为可修改领域实体，持有 `EntityId id`、`TransactionType type`、`TransactionAmount amount`、`String category`、`LocalDate date`、`String note`。
- `TransactionRecord` 构造和修改详情时应拒绝空编号、空类型、空金额、空日期和空类别；类别使用 `strip()` 规范化后不得为空；备注允许为空并规范化为 `""`，非空备注使用 `strip()`。
- `TransactionRecord` 应提供基础读取方法，以及 `updateDetails(TransactionType type, TransactionAmount amount, String category, LocalDate date, String note)` 或等价方法，修改时先完成所有输入校验再改变对象状态，避免部分更新。
- `FinanceStatistics` 作为不可变 record 或等价不可变类型，持有 `MoneyValue totalIncome`、`MoneyValue totalExpense`、`MoneyValue balance`。
- `FinanceStatistics` 应提供 `zero()` 和基于收入/支出总额构造统计结果的工厂方法，例如 `of(MoneyValue totalIncome, MoneyValue totalExpense)`，由工厂统一计算 `balance = totalIncome.subtract(totalExpense)`。
- `FinanceStatistics` 应拒绝空金额；收入总额和支出总额不得为负，结余可为负。

## 已有代码上下文
项目根目录下已有独立 Maven 工程 `java-ai-assistant/`，当前主要相关类型如下：

- `assistant.common.EntityId`：正整数编号值对象，所有可修改记录使用它作为稳定唯一标识。
- `assistant.common.TransactionAmount`：单笔收支金额值对象，使用 `BigDecimal`，必须大于 0，最多两位小数，构造后规范为两位小数。
- `assistant.common.MoneyValue`：统计金额值对象，允许 0 和负数，支持 `zero()`、`from(TransactionAmount)`、`add(...)`、`subtract(...)` 和两位小数展示。
- `assistant.common.DateRange`：日期左右闭区间，后续收支查询日期范围会复用。
- 已有 `task`、`schedule`、`study` 模块均采用“领域实体保护不变量 + 测试覆盖边界 + 后续服务层转换 `OperationResult`”的风格，本轮应保持一致。

## 测试要求
新增 JUnit Jupiter 单元测试应覆盖：

- `TransactionType` 的枚举取值和方向判断，确保收入/支出互斥。
- `TransactionRecord` 创建成功路径：字段保存、类别和备注首尾空白清理、空备注转为空字符串。
- `TransactionRecord` 创建失败路径：空编号、空类型、空金额、空日期、空类别、空白类别。
- `TransactionRecord.updateDetails(...)` 成功更新所有可变字段。
- `TransactionRecord.updateDetails(...)` 任一输入非法时对象保持原状态，避免部分更新。
- `FinanceStatistics.zero()` 返回收入、支出、结余均为 `MoneyValue.zero()`。
- `FinanceStatistics.of(...)` 正确计算正结余、零结余和负结余。
- `FinanceStatistics` 拒绝空收入、空支出、负收入总额和负支出总额。

普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。
