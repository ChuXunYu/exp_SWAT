# 任务指令（v4）

## 动作
NEW

## 任务描述
新增跨业务日期区间和日期时间区间值对象，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/common/DateRange.java`
- `java-ai-assistant/src/main/java/assistant/common/DateTimeRange.java`
- `java-ai-assistant/src/test/java/assistant/common/DateRangeTest.java`
- `java-ai-assistant/src/test/java/assistant/common/DateTimeRangeTest.java`

`DateRange` 表达 `LocalDate` 左右闭区间，服务于收支日期筛选、学习计划周期、本周统计和本月统计。它至少应封装开始日期、结束日期、开始日期不得晚于结束日期的校验、单日包含判断、与另一日期区间的非空重叠判断，并保持不可变值对象语义。

`DateTimeRange` 表达 `LocalDateTime` 左闭右开区间，服务于日程时间合法性、日程冲突识别和按日期查询。它至少应封装开始时间、结束时间、结束时间必须晚于开始时间的校验、时刻包含判断、与另一日期时间区间的非空重叠判断、是否覆盖指定 `LocalDate` 的判断，并保持不可变值对象语义。

新增单元测试需覆盖成功构造、空参数、非法边界、包含边界、重叠边界和首尾相接边界；测试必须可重复运行，不依赖真实当前时间、网络、API Key 或外部文件。

## 选择理由
日期/时间区间是后续多个业务模块的共同底层依赖：

- 收支记录查询需要按 `DateRange` 闭区间筛选日期。
- 学习计划需要用 `DateRange` 表达开始日期到截止日期，并拒绝开始晚于截止。
- 本周、本月统计需要稳定复用闭区间包含语义。
- 日程提醒需要用 `DateTimeRange` 校验开始/结束时间、识别时间冲突，并支持“首尾相接不冲突”的边界。
- 日程按日期查询需要判断跨日安排是否覆盖目标日期。

先实现这两个值对象，可以避免后续 `task`、`schedule`、`study`、`finance`、`summary` 模块重复散落日期比较逻辑，并让高风险边界行为先由独立单元测试锁定。

## 任务上下文
必须依据以下输入约束实现：

- `/root/exp_SWAT/designs-tech/202606111332_java-ai-assistant-tech/tech_v2.md`
- `/root/exp_SWAT/docs/2 design-oo.md`
- `/root/exp_SWAT/docs/1 requirement.md`

相关需求与技术约束：

- `DateRange` 使用左右闭区间语义，适用于收支日期筛选、学习计划周期、本周和本月统计。
- `DateRange` 开始日期晚于结束日期时直接作为输入校验错误。
- `DateTimeRange` 使用左闭右开区间语义，适用于日程。
- `DateTimeRange` 当前时间等于开始时间视为包含，等于结束时间视为不包含。
- 两个日程首尾相接不冲突，存在非空重叠才冲突。
- 日程按日期查询需要返回该日期开始或覆盖该日期的日程快照，因此 `DateTimeRange` 应能判断是否覆盖指定日期。
- 普通单元测试不得依赖真实 DeepSeek、网络、API Key 或真实当前时间。
- 生产代码应使用 Java 17、Maven 标准目录和现有 `assistant.common` 包风格。

建议错误语义：

- 构造参数为 `null` 时使用 `NullPointerException` 或与现有值对象一致的 JDK 空参数异常。
- 日期/时间边界非法时使用 `IllegalArgumentException`，由后续应用服务边界统一转换为 `OperationResult` 和 `ErrorCode.VALIDATION_ERROR`。

## 已有代码上下文
当前项目已建立独立 Maven 工程 `java-ai-assistant/`：

- `pom.xml` 已配置 Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe。
- `assistant.common` 已包含：
  - `EntityId`：不可变 record，正整数校验，非法值抛出 `IllegalArgumentException`。
  - `BusinessException`：携带 `ErrorCode`。
  - `ErrorCode`：包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT`、`SCHEDULE_CONFLICT` 和 AI 错误分类。
  - `OperationResult<T>`：统一成功/失败返回语义。
- `assistant.testability` 已包含：
  - `IdGenerator`、`IncrementalIdGenerator`
  - `TimeProvider`、`SystemTimeProvider`、`FixedTimeProvider`
- 已有测试位于 `java-ai-assistant/src/test/java/assistant/common/` 和 `java-ai-assistant/src/test/java/assistant/testability/`，当前 `mvn clean test` 通过 43 个测试。

实现风格应延续已有基础类型：

- 值对象优先使用 Java record。
- 构造阶段集中校验不可接受状态。
- 对外方法命名应清楚表达闭区间或左闭右开语义。
- 单元测试使用 JUnit Jupiter，覆盖正常、异常和边界分支。

## RETRY 说明（仅 RETRY 时）
不适用。
