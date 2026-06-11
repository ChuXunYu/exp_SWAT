# 任务指令（v5）

## 动作
NEW

## 任务描述
新增单笔收支金额值对象和统计金额值对象，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/common/TransactionAmount.java`
- `java-ai-assistant/src/main/java/assistant/common/MoneyValue.java`
- `java-ai-assistant/src/test/java/assistant/common/TransactionAmountTest.java`
- `java-ai-assistant/src/test/java/assistant/common/MoneyValueTest.java`

本轮只实现通用金额基础，不实现收支记录实体、收支服务、收支查询或统计服务。

## 选择理由
收支记录管理与收支统计是需求中的白盒测试重点，后续 `finance.TransactionRecord`、`FinanceService`、`FinanceStatisticsService` 都依赖稳定的金额合法性、精度和加减语义。先实现两个底层金额值对象，可避免后续 finance 模块直接使用裸 `BigDecimal` 或 `double`，并集中覆盖零值、负数、小数位和统计结余等边界。

## 任务上下文
必须依据以下约束实现：

- `TransactionAmount` 用于单笔收入或支出金额，底层使用 `BigDecimal`。
- `TransactionAmount` 必须大于 0。
- `TransactionAmount` 最多允许两位小数，超过两位小数应作为输入校验错误。
- `MoneyValue` 用于统计金额，底层使用 `BigDecimal`。
- `MoneyValue` 允许 0 和负数，用于表达空统计、支出大于收入时的负结余等场景。
- `MoneyValue` 对外必须提供稳定的两位小数金额表达，确保 `0`、`1.2`、`-3.4` 等统计金额可统一展示或断言为两位小数形式。
- 金额计算必须使用 `BigDecimal`，禁止使用 `double`。
- `MoneyValue` 需要支持后续统计服务计算收入总额、支出总额和结余所需的基础加减能力。
- 两类金额值对象应保持不可变值对象语义，适合使用 `record` 或等价不可变实现。
- 普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。
- `MoneyValue` 单元测试需要覆盖零值、正数、负数、尾随零以及加减结果的两位小数行为。具体 API 名称，以及采用构造期规范化还是提供格式化方法，由详细设计阶段决定。

本轮不需要引入 `finance` 包，不需要实现控制台交互，不需要接入 DeepSeek。

## 已有代码上下文
当前 Maven 工程位于 `java-ai-assistant/`，已配置 Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe。已存在并通过测试的基础类型包括：

- `assistant.common.ErrorCode`
- `assistant.common.BusinessException`
- `assistant.common.OperationResult<T>`
- `assistant.common.EntityId`
- `assistant.common.DateRange`
- `assistant.common.DateTimeRange`
- `assistant.testability.IdGenerator`
- `assistant.testability.IncrementalIdGenerator`
- `assistant.testability.TimeProvider`
- `assistant.testability.SystemTimeProvider`
- `assistant.testability.FixedTimeProvider`

现有通用值对象倾向于构造阶段校验、不可变语义和清晰的边界测试。`DateRange`、`DateTimeRange` 等底层值对象直接使用 Java 标准异常表达调用方输入错误；本轮可沿用该风格，具体异常类型与消息由详细设计进一步明确，测试重点应断言稳定的行为和错误边界。

## 修订说明（v5 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| `task_v5.md` 遗漏技术方案中 `MoneyValue` 统一按两位小数展示的约束，可能导致后续统计、汇总或控制台层重复处理金额格式。 | 在任务上下文中补充 `MoneyValue` 对外提供稳定两位小数金额表达的要求，并明确测试需覆盖零值、正数、负数、尾随零和加减结果的两位小数行为；具体 API 和规范化方式交由详细设计阶段决定。 |
