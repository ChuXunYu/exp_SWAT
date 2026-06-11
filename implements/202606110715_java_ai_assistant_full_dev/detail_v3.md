# 详细设计（v3）

## 概述

本轮设计目标是在既有 `java-ai-assistant/` Maven 单模块工程中新增跨业务可替换时间提供基础，为后续日程状态、学习计划逾期、本周统计、本月统计、今日摘要和 AI 本地上下文生成提供统一、可测试的当前时间来源。

范围仅包含 `assistant.testability.TimeProvider` 时间提供接口、`assistant.testability.SystemTimeProvider` 生产系统时间实现、`assistant.testability.FixedTimeProvider` 固定时间实现，以及对应 JUnit Jupiter 单元测试。本轮不实现日程、学习计划、汇总统计、日期区间、日期时间区间、时区配置、可变时钟或业务服务注入改造。

设计沿用现有 `assistant.testability` 包风格：生产和测试均可依赖的简单抽象及基础实现放在 `src/main/java`；JUnit 专用 fake、stub 和断言辅助放在 `src/test/java`。普通单元测试不得读取真实环境变量、访问网络、依赖真实 DeepSeek API Key，且不得对真实当前时间做固定日期或固定时刻断言。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/testability/TimeProvider.java` | 新建 | 定义当前日期和当前日期时间的统一抽象，供后续业务服务通过构造器注入依赖。 |
| `java-ai-assistant/src/main/java/assistant/testability/SystemTimeProvider.java` | 新建 | 生产用系统时间实现，委托 Java `java.time` 获取真实当前日期和日期时间。 |
| `java-ai-assistant/src/main/java/assistant/testability/FixedTimeProvider.java` | 新建 | 固定时间实现，构造后始终返回同一个 `LocalDateTime` 及其日期部分，用于单元测试、演示数据和可重复边界场景。 |
| `java-ai-assistant/src/test/java/assistant/testability/FixedTimeProviderTest.java` | 新建 | 覆盖固定日期时间返回、`today()` 与 `now()` 一致性、构造参数空值拒绝和独立实例行为。 |
| `java-ai-assistant/src/test/java/assistant/testability/SystemTimeProviderTest.java` | 新建 | 覆盖系统时间实现的非空、接近调用时刻、日期合理性等轻量契约，不断言固定真实日期或固定真实时刻。 |

## 类型定义

### `TimeProvider`

**形态**：`interface`

**包路径**：`assistant.testability`

**职责**：抽象“当前日期”和“当前日期时间”读取能力，使日程状态、学习计划逾期、本周统计、本月统计、今日摘要和 AI 本地上下文生成不直接依赖 `LocalDate.now()` 或 `LocalDateTime.now()`。

**类型签名定义**：`public interface TimeProvider`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `LocalDate today()` | `LocalDate` | 返回调用方应视为“今天”的日期；不得返回 `null`。 |
| `LocalDateTime now()` | `LocalDateTime` | 返回调用方应视为“当前时刻”的本地日期时间；不得返回 `null`。 |

**构造方式**：接口无构造器，由实现类提供实例。

**类型关系**：由 `SystemTimeProvider` 和 `FixedTimeProvider` 实现；后续 `ScheduleService`、`StudyPlanAnalysisService`、`SummaryService`、AI 本地上下文生成等服务通过构造器组合持有该接口。

### `SystemTimeProvider`

**形态**：`final class`

**包路径**：`assistant.testability`

**职责**：生产环境默认时间提供者，直接读取 JVM 默认时区下的系统当前日期和日期时间。

**类型签名定义**：`public final class SystemTimeProvider implements TimeProvider`

**字段**：无实例字段。

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public SystemTimeProvider()` | 构造器 | 创建无状态系统时间提供者。 |
| `public LocalDate today()` | `LocalDate` | 返回 `LocalDate.now()` 等价结果；不得返回 `null`。 |
| `public LocalDateTime now()` | `LocalDateTime` | 返回 `LocalDateTime.now()` 等价结果；不得返回 `null`。 |

**构造方式**：调用 `new SystemTimeProvider()` 创建。

**类型关系**：实现 `TimeProvider`；依赖 Java 标准库 `java.time.LocalDate` 和 `java.time.LocalDateTime`；不依赖业务服务、仓储、JUnit、Mockito、AI、环境变量或网络。

### `FixedTimeProvider`

**形态**：`final class`

**包路径**：`assistant.testability`

**职责**：提供不可变固定时间来源，使普通单元测试、演示数据和边界场景能够稳定覆盖“未开始、进行中、已过期、逾期、本周、本月、今日”等路径。

**类型签名定义**：`public final class FixedTimeProvider implements TimeProvider`

**字段**：

| 字段签名 | 约束 |
|----------|------|
| `private final LocalDateTime fixedDateTime` | 构造时传入，必须非空；对象生命周期内不可变。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public FixedTimeProvider(LocalDateTime fixedDateTime)` | 构造器 | 创建固定时间提供者；`fixedDateTime == null` 时抛出 `NullPointerException`。 |
| `public LocalDate today()` | `LocalDate` | 返回 `fixedDateTime.toLocalDate()`；多次调用结果一致。 |
| `public LocalDateTime now()` | `LocalDateTime` | 返回构造时传入的同一个日期时间值；多次调用结果一致。 |

**构造方式**：调用 `new FixedTimeProvider(LocalDateTime fixedDateTime)` 创建。

**类型关系**：实现 `TimeProvider`；组合持有 `java.time.LocalDateTime`；不依赖业务服务、仓储、JUnit、Mockito、AI、环境变量、网络或系统时间。

## 单元测试规格

### `FixedTimeProviderTest`

**包路径**：`assistant.testability`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void nowReturnsFixedDateTime()` | `new FixedTimeProvider(LocalDateTime.of(2026, 6, 11, 9, 30))` 的 `now()` 返回同一个日期时间值。 |
| `void todayReturnsDatePartOfFixedDateTime()` | `today()` 返回固定日期时间的 `toLocalDate()`。 |
| `void todayAndNowStayConsistentAcrossCalls()` | 多次调用 `today()` 与 `now().toLocalDate()` 一致，且不随真实时间变化。 |
| `void rejectsNullFixedDateTime()` | 构造参数为 `null` 时抛出 `NullPointerException`。 |
| `void independentInstancesKeepIndependentFixedTimes()` | 两个 `FixedTimeProvider` 实例分别返回各自构造时传入的固定时间，互不共享状态。 |
| `void canBeUsedThroughTimeProviderInterface()` | 通过 `TimeProvider` 引用调用 `today()` 和 `now()` 时仍返回固定契约结果。 |

### `SystemTimeProviderTest`

**包路径**：`assistant.testability`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void nowReturnsNonNullDateTimeNearInvocationWindow()` | 调用前记录 `LocalDateTime before = LocalDateTime.now()`，调用 `provider.now()`，调用后记录 `LocalDateTime after = LocalDateTime.now()`；断言结果非空且不早于 `before`、不晚于 `after`。 |
| `void todayReturnsNonNullDateNearInvocationWindow()` | 调用前记录 `LocalDate before = LocalDate.now()`，调用 `provider.today()`，调用后记录 `LocalDate after = LocalDate.now()`；断言结果非空且等于 `before` 或 `after`，避免跨午夜脆弱断言。 |
| `void canBeUsedThroughTimeProviderInterface()` | 通过 `TimeProvider` 引用调用 `today()` 和 `now()`，断言二者均非空，不断言固定日期或固定时刻。 |

## 错误处理

`TimeProvider`、`SystemTimeProvider` 和 `FixedTimeProvider` 属于基础抽象与基础实现，本轮直接使用 Java 标准异常表达参数错误，不引入 `BusinessException`、`OperationResult` 或新的错误码。

| 场景 | 异常类型 | 说明 |
|------|----------|------|
| `FixedTimeProvider` 构造参数 `fixedDateTime == null` | `NullPointerException` | 固定时间是该实现的必需状态，空值属于调用方编程错误。 |

异常消息只需简短、可读，不作为本轮测试的强约束。测试断言异常类型，不断言完整异常文本。

## 行为契约

1. 后续业务服务读取当前日期或当前日期时间时，应依赖注入 `TimeProvider`，不得在业务逻辑中直接调用 `LocalDate.now()` 或 `LocalDateTime.now()`。
2. `TimeProvider.today()` 和 `TimeProvider.now()` 均不得返回 `null`。
3. `SystemTimeProvider` 无状态；多个实例之间没有共享可变状态。
4. `SystemTimeProvider.today()` 读取 JVM 默认时区下的系统当前日期，语义等价于 `LocalDate.now()`。
5. `SystemTimeProvider.now()` 读取 JVM 默认时区下的系统当前本地日期时间，语义等价于 `LocalDateTime.now()`。
6. `SystemTimeProvider` 单元测试只能验证非空和调用窗口内的合理性，不得断言固定日期、固定时刻或依赖测试执行当天日期。
7. `FixedTimeProvider` 创建后不可变；`fixedDateTime` 字段为 `final`，构造后不允许修改。
8. `FixedTimeProvider.now()` 每次返回构造时传入的固定 `LocalDateTime` 值。
9. `FixedTimeProvider.today()` 每次返回固定 `LocalDateTime` 的日期部分，与 `now().toLocalDate()` 一致。
10. 两个 `FixedTimeProvider` 实例之间没有共享状态，互不影响。
11. 本轮不要求实现可变时间、时区配置、线程安全时钟推进器或基于 `java.time.Clock` 的适配器；后续若需要可另行新增实现或测试专用 double。
12. 本轮新增生产代码不得读取环境变量、访问网络、读取 DeepSeek 配置、调用 AI 客户端或依赖 JUnit/Mockito。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.testability.TimeProvider` | Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`。 |
| `assistant.testability.SystemTimeProvider` | `assistant.testability.TimeProvider`、Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`。 |
| `assistant.testability.FixedTimeProvider` | `assistant.testability.TimeProvider`、Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`、`java.util.Objects` 或等价空值校验工具。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `assistant.testability.FixedTimeProviderTest` | JUnit Jupiter 断言 API、Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`、`assistant.testability.TimeProvider`、`assistant.testability.FixedTimeProvider`。 |
| `assistant.testability.SystemTimeProviderTest` | JUnit Jupiter 断言 API、Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`、`assistant.testability.TimeProvider`、`assistant.testability.SystemTimeProvider`。 |

后续日程、学习计划、汇总统计和 AI 本地上下文相关任务应通过构造器接收 `TimeProvider`，生产装配传入 `SystemTimeProvider`，单元测试和演示数据传入 `FixedTimeProvider`。本轮只提供该公开契约，不引入任何后续业务类型。
