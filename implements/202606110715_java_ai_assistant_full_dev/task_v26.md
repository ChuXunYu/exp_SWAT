# 任务指令（v26）

## 动作
NEW

## 任务描述
扩展 `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 的收支记录菜单交互，并补充 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 覆盖；必要时可在 `assistant.app` 包内复用或抽取小型输入解析辅助方法，但不得修改 `FinanceService`、`TransactionQuery`、`TransactionView`、`TransactionType`、`FinanceStatistics` 的公开契约。

本轮应将主菜单命令 `5` 从一次性 `showTransactions()` 调整为可循环收支子菜单，至少支持：

- `l/list`：列出全部收支记录，并展示全量统计。
- `i/income`：记录收入。
- `e/expense`：记录支出。
- `v/view`：按 id 查看单条收支记录。
- `f/filter`：按类型、类别、日期范围组合筛选，并展示筛选结果统计。
- `u/update`：修改收支记录类型、金额、类别、日期和备注。
- `d/delete`：删除收支记录。
- `s/statistics`：展示全量或筛选统计。
- `b/back`：返回主菜单。
- `h/help`：展示收支子菜单帮助。

控制台输出应包含清晰的列表、详情和统计信息。列表不应限制为前 10 条；空列表输出 `暂无收支记录`。统计至少展示收入、支出和结余。所有服务失败统一通过既有 `printResult(...)` 风格展示错误码和消息。

## 选择理由
任务待办、日程提醒和学习计划三个控制台入口已完成完整可输入闭环；收支记录仍只有一次性列表与统计展示，不能满足“每个核心功能可演示、可输入、可产生明确结果”的验收口径。收支模块同时覆盖金额解析、收支类型、日期范围筛选、删除后统计重算和结余计算，是需求中的测试重点之一，应优先补齐控制台交互。

## 任务上下文
完整需求要求第 6 个核心功能“收支记录管理”：用户可以记录收入和支出，字段包括金额、类别、日期和备注；支持查看全部记录、按类别或日期范围查询记录，并计算收入总额、支出总额和结余；拒绝负金额、零金额、非法日期、未知收支类型和开始日期晚于结束日期的查询。

控制台层必须只负责菜单、输入解析、调用服务和结果展示，不直接访问收支仓储或可变实体。普通单元测试不得读取真实环境变量、访问真实网络、依赖真实 API Key 或真实当前时间。

本轮必须覆盖以下交互与边界：

- 收支子菜单进入、帮助、未知命令、空命令、返回主菜单和 EOF 稳定退出。
- 成功记录收入和支出后，列表与统计同步展示。
- 查看、修改、删除记录的成功路径和不存在记录失败路径。
- 筛选支持类型、类别、日期范围全部为空时列出全部；支持任意组合筛选。
- 类型输入大小写不敏感匹配 `INCOME` / `EXPENSE`；非法类型在控制台层输出 `VALIDATION_ERROR` 且不得调用服务。
- 日期输入格式为 `yyyy-MM-dd`；筛选日期范围必须同时为空或同时填写；结束日期早于开始日期时输出 `VALIDATION_ERROR` 且不得调用服务。
- 金额字段作为原始字符串传给 `FinanceService`；空金额、零金额、负金额或超过金额值对象约束的输入应通过服务失败结果稳定展示。
- 修改时 `TransactionType`、金额、类别、日期和备注均按字段顺序读取；校验失败不得写入。
- 删除成功的空载荷结果应输出 `操作成功`，随后统计基于当前仓储状态重新计算。

## 已有代码上下文
当前 `assistant.app.ConsoleApplication` 中主菜单命令 `5` 调用 `showTransactions()`，该方法只执行 `FinanceService.listTransactions()` 和 `calculateStatistics()`，展示统计和前 10 条记录。任务、日程、学习计划子菜单已经实现了可复用的菜单循环、字段读取、`ParsedInput<T>` 状态、`printValidationError(...)`、`printResult(...)`、id/date 解析和 EOF 处理模式。

既有 `assistant.finance.FinanceService` 公开接口包括：

- `recordIncome(String amountText, String category, LocalDate date, String note)`
- `recordExpense(String amountText, String category, LocalDate date, String note)`
- `getTransaction(EntityId id)`
- `listTransactions()`
- `listTransactions(TransactionQuery query)`
- `updateTransaction(EntityId id, TransactionType type, String amountText, String category, LocalDate date, String note)`
- `deleteTransaction(EntityId id)`
- `calculateStatistics()`
- `calculateStatistics(TransactionQuery query)`

既有 `TransactionQuery.of(TransactionType type, String category, DateRange dateRange)` 支持组合筛选；`TransactionType` 为 `INCOME` / `EXPENSE`；`TransactionView` 提供 `id()`、`type()`、`amount()`、`category()`、`date()`、`note()`；`FinanceStatistics` 提供 `totalIncome()`、`totalExpense()`、`balance()`。
