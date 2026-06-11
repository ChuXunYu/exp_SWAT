# 任务指令（v3）

## 动作
NEW

## 任务描述
实现跨业务可替换时间提供基础，为后续日程状态、学习计划逾期、本周和本月统计、今日摘要、AI 本地上下文生成提供统一且可测试的当前时间来源。

预期生产文件：

| 文件路径 | 目标 |
|---------|------|
| `java-ai-assistant/src/main/java/assistant/testability/TimeProvider.java` | 新增时间提供接口，统一暴露当前日期和当前日期时间。 |
| `java-ai-assistant/src/main/java/assistant/testability/SystemTimeProvider.java` | 新增生产用系统时间实现，委托 Java `java.time` 获取真实当前时间。 |
| `java-ai-assistant/src/main/java/assistant/testability/FixedTimeProvider.java` | 新增固定时间实现，用于单元测试、演示数据和可重复边界场景。 |

预期测试文件：

| 文件路径 | 目标 |
|---------|------|
| `java-ai-assistant/src/test/java/assistant/testability/FixedTimeProviderTest.java` | 覆盖固定日期时间返回、`today()` 与 `now()` 一致性、构造参数空值拒绝和独立实例行为。 |
| `java-ai-assistant/src/test/java/assistant/testability/SystemTimeProviderTest.java` | 如设计认为必要，仅做不依赖具体真实当前时间的轻量契约测试；不得断言固定日期或固定时刻。 |

## 选择理由
`TimeProvider` 是多个后续业务模块的底层依赖。需求和技术方案都强调日程状态、学习计划逾期、本周统计、本月统计、今日摘要和 AI 本地上下文必须通过可替换时间来源读取当前时间，普通单元测试不得依赖真实当前时间。

本任务先完成时间抽象与两个基础实现，可为后续业务服务统一构造器注入时间依赖，并让边界测试使用固定日期时间稳定覆盖“未开始、进行中、已过期、逾期、本周、本月、今日”等路径。任务粒度控制在 3 个紧密相关生产类型内，不引入日期区间、日期时间区间或具体业务服务。

## 任务上下文
来自需求、OOD 和技术方案的直接约束：

- 涉及当前日期、初始数据、网络请求、API Key、AI 客户端等外部状态的逻辑，应通过参数、接口或可替换对象传入，避免 JUnit 单元测试依赖不可控环境。
- 普通单元测试不得依赖真实 DeepSeek、网络、API Key 或真实当前时间。
- `TimeProvider` 返回当前 `LocalDate` 和 `LocalDateTime`。
- 生产实现读取系统时间，测试实现固定在指定日期时间。
- 日程状态、学习计划逾期、本周统计、本月统计和今日摘要都必须通过该接口读取时间，不直接调用 `LocalDate.now()` 或 `LocalDateTime.now()`。
- 生产代码只依赖 `assistant.testability` 中的接口或简单基础实现；JUnit 专用 fake、stub 和断言辅助放在 `src/test/java`。

建议行为契约：

- `TimeProvider` 位于 `assistant.testability` 包，接口方法建议为 `LocalDate today()` 与 `LocalDateTime now()`。
- `SystemTimeProvider` 位于 `assistant.testability` 包，实现 `TimeProvider`，`today()` 返回系统当前日期，`now()` 返回系统当前日期时间。
- `SystemTimeProvider` 生产实现可以直接委托 `LocalDate.now()` 和 `LocalDateTime.now()`；后续业务服务不得绕过 `TimeProvider` 直接调用这些静态方法。
- `FixedTimeProvider` 位于 `assistant.testability` 包，实现 `TimeProvider`，构造时接收一个非空 `LocalDateTime fixedDateTime`。
- `FixedTimeProvider.today()` 返回 `fixedDateTime.toLocalDate()`，`now()` 返回构造时的同一个日期时间值。
- `FixedTimeProvider` 创建后不可变；多次调用 `today()` 和 `now()` 返回一致结果，不随真实时间变化。
- `FixedTimeProvider` 构造参数为 `null` 时抛出 `NullPointerException` 或 `IllegalArgumentException`；设计阶段需固定一种异常类型，测试按该契约断言。
- 本轮不要求实现可变时间、时区配置或线程安全时钟推进器；后续若需要可在测试源码中增加专用 test double。

## 已有代码上下文
当前项目已存在 Maven 单模块工程 `java-ai-assistant/`：

- `java-ai-assistant/pom.xml` 已配置 Java 17、JUnit Jupiter、Mockito、Jackson、Surefire、Failsafe 和 JaCoCo。
- `assistant.common.ErrorCode` 已包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT`、`SCHEDULE_CONFLICT`、AI 相关错误和 `SYSTEM_ERROR`。
- `assistant.common.BusinessException` 已支持携带非空 `ErrorCode`。
- `assistant.common.OperationResult<T>` 已支持成功/失败返回语义。
- `assistant.common.EntityId` 已实现正整数编号值对象，支持访问、比较、相等性、哈希和稳定字符串格式。
- `assistant.testability.IdGenerator` 已定义编号生成接口。
- `assistant.testability.IncrementalIdGenerator` 已实现默认或指定起点的递增编号生成器。
- `assistant.testability` 包已用于放置生产和测试均可依赖的简单抽象及基础实现。

## RETRY 说明（仅 RETRY 时）
不适用。
