# 详细设计（v2）

## 概述

本轮设计目标是在既有 `java-ai-assistant/` Maven 单模块工程中新增跨业务实体编号基础能力，为后续任务、日程、学习计划、收支记录、笔记和 AI 草稿提供统一、可排序、可断言的正整数标识语义。

范围仅包含 `assistant.common.EntityId` 值对象、`assistant.testability.IdGenerator` 编号生成接口、`assistant.testability.IncrementalIdGenerator` 默认递增编号生成器，以及对应 JUnit Jupiter 单元测试。本轮不实现任何具体业务实体、仓储、服务、AI 草稿或控制台菜单。

设计沿用上一轮 Java 17、JUnit Jupiter、Maven Surefire 和 `assistant.common` / `assistant.testability` 包约定。普通单元测试不得读取真实环境变量、访问网络、依赖真实 DeepSeek API Key 或真实当前时间。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/common/EntityId.java` | 新建 | 不可变编号值对象，封装大于 0 的 `long`，提供值访问、相等性、哈希、稳定字符串和升序比较语义。 |
| `java-ai-assistant/src/main/java/assistant/testability/IdGenerator.java` | 新建 | 编号生成接口，供后续业务服务通过依赖注入获取新的 `EntityId`。 |
| `java-ai-assistant/src/main/java/assistant/testability/IncrementalIdGenerator.java` | 新建 | 默认单线程递增编号生成器，支持默认起点和自定义起点。 |
| `java-ai-assistant/src/test/java/assistant/common/EntityIdTest.java` | 新建 | 覆盖正整数校验、访问器、相等性、`hashCode`、`toString` 和排序语义。 |
| `java-ai-assistant/src/test/java/assistant/testability/IncrementalIdGeneratorTest.java` | 新建 | 覆盖默认起点、指定起点、连续递增、非法起点拒绝和生成结果类型。 |

## 类型定义

### `EntityId`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表示所有可修改记录的统一正整数编号，避免同名记录无法定位，并为后续仓储、服务、草稿导入和测试断言提供稳定标识。

**类型签名定义**：`public record EntityId(long value) implements Comparable<EntityId>`

**组件**：

| 组件签名 | 约束 |
|----------|------|
| `long value` | 必须大于 `0`；`0` 或负数在构造阶段拒绝。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public EntityId` | 构造器 | Java record compact constructor；`value <= 0` 时抛出 `IllegalArgumentException`。 |
| `public long value()` | `long` | 返回构造时传入的正整数编号。 |
| `public int compareTo(EntityId other)` | `int` | 按 `value` 升序比较；`other == null` 时抛出 `NullPointerException`。 |
| `public String toString()` | `String` | 返回稳定格式 `EntityId[value={value}]`，例如 `EntityId[value=42]`。 |
| `public boolean equals(Object obj)` | `boolean` | Java record 值相等语义；相同 `value` 的 `EntityId` 相等。 |
| `public int hashCode()` | `int` | Java record 哈希语义；相同 `value` 的 `EntityId` 哈希一致。 |

**构造方式**：调用 `new EntityId(long value)` 直接创建。

**类型关系**：实现 `Comparable<EntityId>`；后续任务、日程、学习计划、收支记录、笔记和 AI 草稿实体组合持有该类型；`IncrementalIdGenerator` 创建该类型。

### `IdGenerator`

**形态**：`interface`

**包路径**：`assistant.testability`

**职责**：抽象实体编号生成能力，使业务服务不直接依赖具体递增策略，便于单元测试使用固定或可控编号生成器。

**类型签名定义**：`public interface IdGenerator`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `EntityId nextId()` | `EntityId` | 返回一个新的合法 `EntityId`；具体唯一性和递增策略由实现类保证。 |

**构造方式**：接口无构造器，由实现类提供实例。

**类型关系**：依赖 `assistant.common.EntityId`；被后续业务服务通过构造器注入或装配注入使用。

### `IncrementalIdGenerator`

**形态**：`final class`

**包路径**：`assistant.testability`

**职责**：提供默认递增编号生成策略，从默认起点或指定起点开始，每次生成后内部游标加 1。

**类型签名定义**：`public final class IncrementalIdGenerator implements IdGenerator`

**字段**：

| 字段签名 | 约束 |
|----------|------|
| `private long nextValue` | 保存下一次要生成的编号值；构造后必须保持大于 `0`，直到编号空间耗尽。 |
| `private boolean exhausted` | 标记 `Long.MAX_VALUE` 已生成后的耗尽状态；普通路径下为 `false`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public IncrementalIdGenerator()` | 构造器 | 创建从 `1` 开始的生成器。 |
| `public IncrementalIdGenerator(long startInclusive)` | 构造器 | 创建从 `startInclusive` 开始的生成器；`startInclusive <= 0` 时抛出 `IllegalArgumentException`。 |
| `public EntityId nextId()` | `EntityId` | 返回当前 `nextValue` 对应的 `EntityId`，随后将下一次值递增 1；若编号空间已耗尽，抛出 `IllegalStateException`。 |

**构造方式**：调用无参构造器使用默认起点，或调用一参构造器指定包含式起点。

**类型关系**：实现 `IdGenerator`；组合并创建 `assistant.common.EntityId`；不依赖业务服务、仓储、JUnit、Mockito、AI、环境变量或系统时间。

## 单元测试规格

### `EntityIdTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void acceptsPositiveValueAndExposesAccessor()` | `new EntityId(42)` 成功，`value()` 返回 `42`。 |
| `void rejectsZeroValue()` | `new EntityId(0)` 抛出 `IllegalArgumentException`。 |
| `void rejectsNegativeValue()` | `new EntityId(-1)` 抛出 `IllegalArgumentException`。 |
| `void equalityUsesUnderlyingValue()` | 相同 `value` 的两个实例相等，不同 `value` 的实例不相等。 |
| `void hashCodeUsesUnderlyingValue()` | 相同 `value` 的两个实例 `hashCode()` 一致。 |
| `void toStringUsesStableReadableFormat()` | `new EntityId(42).toString()` 返回 `EntityId[value=42]`。 |
| `void compareToSortsByNumericValueAscending()` | 多个 `EntityId` 排序后按底层 `long` 升序排列。 |
| `void compareToRejectsNull()` | `compareTo(null)` 抛出 `NullPointerException`。 |

### `IncrementalIdGeneratorTest`

**包路径**：`assistant.testability`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void defaultConstructorStartsAtOne()` | 无参构造器第一次 `nextId()` 返回 `new EntityId(1)`。 |
| `void customStartIsInclusive()` | `new IncrementalIdGenerator(10)` 第一次 `nextId()` 返回 `new EntityId(10)`。 |
| `void nextIdReturnsSequentialValues()` | 连续调用返回 `1`、`2`、`3` 或指定起点后的连续值。 |
| `void rejectsZeroStart()` | `new IncrementalIdGenerator(0)` 抛出 `IllegalArgumentException`。 |
| `void rejectsNegativeStart()` | `new IncrementalIdGenerator(-5)` 抛出 `IllegalArgumentException`。 |
| `void nextIdReturnsEntityIdInstances()` | `nextId()` 返回对象类型为 `EntityId`，并具备合法正整数值。 |
| `void independentGeneratorsKeepIndependentSequences()` | 两个生成器实例互不共享游标，分别按自身起点递增。 |

## 错误处理

`EntityId` 与 `IncrementalIdGenerator` 属于基础值对象和基础生成器，本轮直接使用 Java 标准异常表达参数错误，不引入 `BusinessException` 或 `OperationResult`。

| 场景 | 异常类型 | 说明 |
|------|----------|------|
| `EntityId` 构造参数 `value <= 0` | `IllegalArgumentException` | 正整数编号语义被破坏，属于调用方输入错误。 |
| `IncrementalIdGenerator` 构造参数 `startInclusive <= 0` | `IllegalArgumentException` | 起点必须能创建合法 `EntityId`。 |
| `EntityId.compareTo(null)` | `NullPointerException` | 遵循 Java `Comparable` 对空比较对象的常规拒绝语义。 |
| `IncrementalIdGenerator.nextId()` 在编号空间耗尽后继续调用 | `IllegalStateException` | 生成器状态已无法再产生合法递增编号；普通业务和本轮测试不依赖该边界。 |

异常消息只需简短、可读，不作为本轮测试的强约束。测试断言异常类型和状态不变，不断言完整异常文本。

## 行为契约

1. `EntityId` 创建后不可变；`value()`、`equals()`、`hashCode()`、`compareTo()` 和 `toString()` 不得改变对象状态。
2. `EntityId` 只接受正整数 `long`；`0`、负数必须在构造阶段拒绝。
3. `EntityId` 相等性和哈希语义只由 `value` 决定，可安全作为 `Map` key。
4. `EntityId.compareTo(EntityId)` 必须使用 `Long.compare(this.value, other.value())` 等价语义，保证升序、传递性和与 `equals()` 一致。
5. `EntityId.toString()` 必须是稳定可读格式 `EntityId[value={value}]`，不得使用对象地址格式。
6. `IdGenerator.nextId()` 是业务服务获取编号的唯一抽象方法；后续业务服务不得直接自行维护裸 `long` 编号游标。
7. `IncrementalIdGenerator()` 默认从 `1` 开始，第一次调用返回 `EntityId(1)`。
8. `IncrementalIdGenerator(long startInclusive)` 使用包含式起点，第一次调用返回 `EntityId(startInclusive)`。
9. `IncrementalIdGenerator.nextId()` 每次成功调用都返回新的 `EntityId` 实例，编号值相对上一次成功调用递增 `1`。
10. 两个 `IncrementalIdGenerator` 实例之间没有共享状态，互不影响。
11. 本轮不要求线程安全并发语义；默认单用户命令行顺序执行。后续若引入并发写入，应另行设计同步或线程安全生成器。
12. 本轮新增生产代码不得读取环境变量、访问网络、读取真实当前时间或依赖 DeepSeek 配置。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.common.EntityId` | Java 标准库 `Comparable`、`Objects` 或等价空值校验工具；不依赖项目其他类型。 |
| `assistant.testability.IdGenerator` | `assistant.common.EntityId`。 |
| `assistant.testability.IncrementalIdGenerator` | `assistant.common.EntityId`、`assistant.testability.IdGenerator`。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `assistant.common.EntityIdTest` | JUnit Jupiter 断言 API、`assistant.common.EntityId`、Java 集合排序 API。 |
| `assistant.testability.IncrementalIdGeneratorTest` | JUnit Jupiter 断言 API、`assistant.common.EntityId`、`assistant.testability.IdGenerator`、`assistant.testability.IncrementalIdGenerator`。 |

后续任务、日程、学习计划、收支、笔记和 AI 草稿服务应通过构造器接收 `IdGenerator`，并在创建正式记录或草稿时调用 `nextId()`。本轮只提供该公开契约，不引入任何后续业务类型。
