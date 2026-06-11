# 详细设计（v5）

## 概述

本轮设计目标是在既有 `java-ai-assistant/` Maven 单模块工程中新增两个通用金额值对象：`assistant.common.TransactionAmount` 和 `assistant.common.MoneyValue`，并补充对应 JUnit Jupiter 单元测试规格。

`TransactionAmount` 表达单笔收入或支出的正金额，底层使用 `BigDecimal`，构造阶段拒绝 `null`、零值、负数和超过两位小数的输入。`MoneyValue` 表达统计金额，底层同样使用 `BigDecimal`，允许零值和负数，用于收入总额、支出总额和结余等统计场景，并对外提供稳定的两位小数金额表达。

两个类型都保持不可变值对象语义，沿用当前 `assistant.common.EntityId`、`DateRange`、`DateTimeRange` 的 record 风格、构造阶段校验和 Java 标准异常语义。本轮范围仅包含通用金额基础类型及其单元测试，不实现 `finance` 包、收支记录实体、收支服务、收支查询、统计服务、控制台交互或 DeepSeek 接入。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/common/TransactionAmount.java` | 新建 | 定义单笔收支金额值对象，集中处理正金额、最多两位小数、字符串解析和两位小数规范化。 |
| `java-ai-assistant/src/main/java/assistant/common/MoneyValue.java` | 新建 | 定义统计金额值对象，集中处理允许负数和零值的金额规范化、两位小数字符串表达、加法和减法。 |
| `java-ai-assistant/src/test/java/assistant/common/TransactionAmountTest.java` | 新建 | 覆盖 `TransactionAmount` 的构造、字符串工厂、空参数、零值、负数、超过两位小数、规范化和值对象语义。 |
| `java-ai-assistant/src/test/java/assistant/common/MoneyValueTest.java` | 新建 | 覆盖 `MoneyValue` 的零值、正数、负数、尾随零、两位小数表达、加减结果、空参数和不可变值对象语义。 |

## 类型定义

### `TransactionAmount`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表达一笔收入或支出的不可变正金额，避免后续收支记录、收支服务和统计逻辑直接使用裸 `BigDecimal` 或浮点类型。

**类型签名定义**：`public record TransactionAmount(BigDecimal value)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `BigDecimal value` | 必须非空；必须严格大于 `BigDecimal.ZERO`；原始输入的 `scale()` 不得大于 `2`；构造成功后统一保存为 scale 为 `2` 的 `BigDecimal`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TransactionAmount(BigDecimal value)` | 构造器 | 创建单笔收支金额；`value == null` 时抛出 `NullPointerException`；`value.compareTo(BigDecimal.ZERO) <= 0` 时抛出 `IllegalArgumentException`；`value.scale() > 2` 时抛出 `IllegalArgumentException`；合法输入统一规范化为两位小数。 |
| `public static TransactionAmount of(String text)` | `TransactionAmount` | 从用户输入或测试字面量创建金额；`text == null` 时抛出 `NullPointerException`；去除首尾空白后交由 `BigDecimal(String)` 解析；空白、非数字或不符合构造器金额约束时抛出 `IllegalArgumentException` 或其子类。 |
| `public BigDecimal value()` | `BigDecimal` | 返回规范化后的金额；由 record 自动提供；构造成功后返回值非空且 `scale() == 2`。 |
| `public boolean equals(Object other)` | `boolean` | 由 record 自动提供；由于构造期已规范化，`new TransactionAmount(new BigDecimal("1.2"))` 与 `new TransactionAmount(new BigDecimal("1.20"))` 相等。 |
| `public int hashCode()` | `int` | 由 record 自动提供；与 `equals` 使用同一组记录组件。 |
| `public String toString()` | `String` | 由 record 自动提供稳定可读格式，不作为控制台金额展示文本依赖。 |

**构造方式**：

- 业务代码可调用 `new TransactionAmount(BigDecimal value)` 创建。
- 控制台解析或测试字面量可调用 `TransactionAmount.of(String text)` 创建。
- 不提供 `double`、`float` 或接收浮点类型的构造/工厂方法；调用方如需从文本输入创建金额，必须使用字符串或 `BigDecimal`。

**小数位规则**：

- `new TransactionAmount(new BigDecimal("1"))` 成功，组件值规范化为 `1.00`。
- `new TransactionAmount(new BigDecimal("1.2"))` 成功，组件值规范化为 `1.20`。
- `new TransactionAmount(new BigDecimal("1.23"))` 成功，组件值保持为 `1.23`。
- `new TransactionAmount(new BigDecimal("1.230"))` 因原始 scale 大于 `2` 被拒绝，即使多出的位是尾随零；这是单笔交易输入校验语义。
- `new TransactionAmount(new BigDecimal("1.234"))` 被拒绝。

**类型关系**：依赖 Java 标准库 `java.math.BigDecimal` 和 `java.util.Objects`；不继承自定义基类，不实现自定义接口；后续 `finance.TransactionRecord` 可组合持有该值对象。

### `MoneyValue`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表达统计结果中的不可变金额，支持收入总额、支出总额和结余计算；允许零值和负数，并提供统一两位小数字符串表达。

**类型签名定义**：`public record MoneyValue(BigDecimal value)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `BigDecimal value` | 必须非空；允许小于、等于或大于 `BigDecimal.ZERO`；构造成功后统一保存为 scale 为 `2` 的 `BigDecimal`；若输入需要舍入才能变成两位小数则拒绝。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public MoneyValue(BigDecimal value)` | 构造器 | 创建统计金额；`value == null` 时抛出 `NullPointerException`；使用无舍入语义规范化为两位小数；当输入存在超过两位且非零的小数位时抛出 `IllegalArgumentException`。 |
| `public static MoneyValue zero()` | `MoneyValue` | 返回表示 `0.00` 的统计金额。 |
| `public static MoneyValue of(String text)` | `MoneyValue` | 从测试字面量或内部文本创建统计金额；`text == null` 时抛出 `NullPointerException`；去除首尾空白后交由 `BigDecimal(String)` 解析；空白、非数字或不符合构造器金额约束时抛出 `IllegalArgumentException` 或其子类。 |
| `public static MoneyValue from(TransactionAmount amount)` | `MoneyValue` | 将单笔收支金额转换为统计金额；`amount == null` 时抛出 `NullPointerException`；返回值金额与 `amount.value()` 数值一致且 scale 为 `2`。 |
| `public BigDecimal value()` | `BigDecimal` | 返回规范化后的统计金额；由 record 自动提供；构造成功后返回值非空且 `scale() == 2`。 |
| `public MoneyValue add(MoneyValue other)` | `MoneyValue` | 返回当前金额与 `other` 相加后的新 `MoneyValue`；`other == null` 时抛出 `NullPointerException`；不修改当前对象或 `other`。 |
| `public MoneyValue subtract(MoneyValue other)` | `MoneyValue` | 返回当前金额减去 `other` 后的新 `MoneyValue`；`other == null` 时抛出 `NullPointerException`；结果允许为零或负数；不修改当前对象或 `other`。 |
| `public String toPlainString()` | `String` | 返回用于展示和测试断言的纯金额文本，始终包含两位小数，不使用科学计数法。 |
| `public boolean equals(Object other)` | `boolean` | 由 record 自动提供；由于构造期已规范化，`new MoneyValue(new BigDecimal("1.2"))`、`new MoneyValue(new BigDecimal("1.20"))` 和 `new MoneyValue(new BigDecimal("1.2000"))` 相等。 |
| `public int hashCode()` | `int` | 由 record 自动提供；与 `equals` 使用同一组记录组件。 |
| `public String toString()` | `String` | 由 record 自动提供稳定可读格式，不作为业务金额展示文本依赖；业务展示使用 `toPlainString()`。 |

**构造方式**：

- 统计服务可调用 `MoneyValue.zero()` 获取空统计金额。
- 统计服务可调用 `MoneyValue.from(TransactionAmount amount)` 将单笔金额纳入统计。
- 统计计算可使用 `add(MoneyValue other)` 和 `subtract(MoneyValue other)` 返回新的统计金额。
- 测试或内部文本解析可调用 `MoneyValue.of(String text)`。
- 不提供 `double`、`float` 或接收浮点类型的构造/工厂方法。

**两位小数表达规则**：

- `new MoneyValue(new BigDecimal("0")).toPlainString()` 返回 `0.00`。
- `new MoneyValue(new BigDecimal("1.2")).toPlainString()` 返回 `1.20`。
- `new MoneyValue(new BigDecimal("-3.4")).toPlainString()` 返回 `-3.40`。
- `new MoneyValue(new BigDecimal("1.2000")).toPlainString()` 返回 `1.20`。
- `new MoneyValue(new BigDecimal("1.234"))` 被拒绝，因为规范化为两位小数需要舍入。

**类型关系**：依赖 Java 标准库 `java.math.BigDecimal`、`java.math.RoundingMode` 和 `java.util.Objects`；依赖同包 `assistant.common.TransactionAmount` 用于 `from(TransactionAmount amount)` 转换；后续 `finance.FinanceStatisticsService` 和统计结果对象可组合使用该值对象。

## 单元测试规格

### `TransactionAmountTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructsPositiveIntegerAmountAndNormalizesScale()` | `new TransactionAmount(new BigDecimal("12"))` 成功，`value()` 为 `12.00` 且 `scale() == 2`。 |
| `void constructsPositiveOneDecimalAmountAndNormalizesScale()` | `new TransactionAmount(new BigDecimal("12.3"))` 成功，`value()` 为 `12.30`。 |
| `void constructsPositiveTwoDecimalAmount()` | `new TransactionAmount(new BigDecimal("12.34"))` 成功，`value()` 为 `12.34`。 |
| `void rejectsNullValue()` | 构造参数 `null` 时抛出 `NullPointerException`。 |
| `void rejectsZeroValue()` | 金额为 `0` 或 `0.00` 时抛出 `IllegalArgumentException`。 |
| `void rejectsNegativeValue()` | 金额为负数时抛出 `IllegalArgumentException`。 |
| `void rejectsMoreThanTwoDecimalPlaces()` | `1.234` 等 scale 大于 `2` 的输入抛出 `IllegalArgumentException`。 |
| `void rejectsMoreThanTwoDecimalPlacesEvenWhenTrailingZeros()` | `1.230` 等原始 scale 大于 `2` 的单笔交易输入抛出 `IllegalArgumentException`。 |
| `void createsAmountFromStringAndTrimsWhitespace()` | `TransactionAmount.of(" 12.30 ")` 成功，`value()` 为 `12.30`。 |
| `void stringFactoryRejectsNullText()` | `TransactionAmount.of(null)` 抛出 `NullPointerException`。 |
| `void stringFactoryRejectsBlankText()` | `TransactionAmount.of("   ")` 抛出 `IllegalArgumentException` 或其子类。 |
| `void stringFactoryRejectsInvalidNumberText()` | `TransactionAmount.of("abc")` 抛出 `IllegalArgumentException` 或其子类。 |
| `void equalityAndHashCodeUseNormalizedAmount()` | `1.2` 与 `1.20` 构造出的金额相等且哈希一致。 |
| `void toStringUsesRecordComponentNameAndNormalizedValue()` | record 默认 `toString()` 包含类型名、组件名和规范化后的两位小数金额。 |

### `MoneyValueTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructsZeroValueAndFormatsWithTwoDecimals()` | `new MoneyValue(BigDecimal.ZERO)` 成功，`toPlainString()` 返回 `0.00`。 |
| `void zeroFactoryReturnsZeroWithTwoDecimals()` | `MoneyValue.zero()` 返回 `0.00` 且 `value().scale() == 2`。 |
| `void constructsPositiveValueAndFormatsWithTwoDecimals()` | `1.2` 规范化为 `1.20`。 |
| `void constructsNegativeValueAndFormatsWithTwoDecimals()` | `-3.4` 规范化为 `-3.40`。 |
| `void preservesTwoDecimalInput()` | `5.67` 保持为 `5.67`。 |
| `void normalizesTrailingZerosToTwoDecimals()` | `1.2000` 规范化为 `1.20`。 |
| `void rejectsNullValue()` | 构造参数 `null` 时抛出 `NullPointerException`。 |
| `void rejectsValueThatRequiresRounding()` | `1.234` 抛出 `IllegalArgumentException`，不进行静默舍入。 |
| `void createsMoneyValueFromStringAndTrimsWhitespace()` | `MoneyValue.of(" -3.40 ")` 成功并返回 `-3.40`。 |
| `void stringFactoryRejectsNullText()` | `MoneyValue.of(null)` 抛出 `NullPointerException`。 |
| `void stringFactoryRejectsBlankText()` | `MoneyValue.of("   ")` 抛出 `IllegalArgumentException` 或其子类。 |
| `void createsMoneyValueFromTransactionAmount()` | `MoneyValue.from(new TransactionAmount(new BigDecimal("8.50")))` 返回 `8.50`。 |
| `void fromTransactionAmountRejectsNull()` | `MoneyValue.from(null)` 抛出 `NullPointerException`。 |
| `void addReturnsNewMoneyValueWithTwoDecimalResult()` | `1.20 + 2.30` 返回 `3.50`，原对象不变。 |
| `void addAllowsNegativeOperand()` | `5.00 + -2.25` 返回 `2.75`。 |
| `void addRejectsNullOther()` | `add(null)` 抛出 `NullPointerException`。 |
| `void subtractReturnsNewMoneyValueWithTwoDecimalResult()` | `5.00 - 1.25` 返回 `3.75`，原对象不变。 |
| `void subtractAllowsNegativeResult()` | `1.00 - 3.40` 返回 `-2.40`。 |
| `void subtractRejectsNullOther()` | `subtract(null)` 抛出 `NullPointerException`。 |
| `void equalityAndHashCodeUseNormalizedAmount()` | `1.2`、`1.20` 和 `1.2000` 构造出的统计金额相等且哈希一致。 |
| `void toStringUsesRecordComponentNameAndNormalizedValue()` | record 默认 `toString()` 包含类型名、组件名和规范化后的两位小数金额。 |

## 错误处理

两个值对象都属于 `assistant.common` 底层基础类型，直接使用 Java 标准异常表达调用方输入错误，不引入 `BusinessException`、`OperationResult` 或新的 `ErrorCode`。后续应用服务边界如需面向控制台返回统一错误分类，可捕获这些标准异常并转换为 `OperationResult`。

| 场景 | 异常类型 | 说明 |
|------|----------|------|
| `TransactionAmount` 构造参数 `value == null` | `NullPointerException` | 单笔金额是必需状态。 |
| `TransactionAmount` 构造参数 `value <= 0` | `IllegalArgumentException` | 单笔收入或支出金额必须严格大于零。 |
| `TransactionAmount` 构造参数 `value.scale() > 2` | `IllegalArgumentException` | 单笔交易输入最多允许两位小数。 |
| `TransactionAmount.of(null)` | `NullPointerException` | 输入文本必须明确。 |
| `TransactionAmount.of(blankOrInvalidText)` | `IllegalArgumentException` 或其子类 | 空白、非数字或非法金额输入属于调用方输入错误。 |
| `MoneyValue` 构造参数 `value == null` | `NullPointerException` | 统计金额是必需状态。 |
| `MoneyValue` 构造参数存在超过两位且非零的小数位 | `IllegalArgumentException` | 统计金额不进行静默舍入。 |
| `MoneyValue.of(null)` | `NullPointerException` | 输入文本必须明确。 |
| `MoneyValue.of(blankOrInvalidText)` | `IllegalArgumentException` 或其子类 | 空白、非数字或非法金额输入属于调用方输入错误。 |
| `MoneyValue.from(null)` | `NullPointerException` | 单笔金额参数必须明确。 |
| `MoneyValue.add(null)` | `NullPointerException` | 加数必须明确。 |
| `MoneyValue.subtract(null)` | `NullPointerException` | 减数必须明确。 |

异常消息保持简短、可读即可，不作为本轮测试强约束。测试断言异常类型，不断言完整异常文本。

## 行为契约

1. `TransactionAmount` 是不可变值对象；record 组件为不可变的 `BigDecimal` 引用语义，构造后不会暴露可变状态。
2. `TransactionAmount` 只表示单笔收入或支出的正金额，不承担加减、结余或展示统计职责。
3. `TransactionAmount` 的构造器和字符串工厂都禁止零值、负数和超过两位小数的输入。
4. `TransactionAmount` 构造成功后，`value()` 必须始终返回 scale 为 `2` 的 `BigDecimal`，用于避免 `BigDecimal.equals` 因 scale 差异破坏值对象相等性。
5. `TransactionAmount` 的小数位校验以传入 `BigDecimal` 的原始 `scale()` 为准；scale 大于 `2` 的单笔交易输入直接拒绝。
6. `MoneyValue` 是不可变值对象；所有加减操作返回新对象，不修改参与运算的对象。
7. `MoneyValue` 允许零值和负数，支持空统计、支出大于收入时的负结余等场景。
8. `MoneyValue` 构造成功后，`value()` 必须始终返回 scale 为 `2` 的 `BigDecimal`。
9. `MoneyValue` 不进行静默舍入；输入如果无法在不舍入的情况下表达为两位小数，应作为输入校验错误。
10. `MoneyValue.toPlainString()` 是后续统计展示和单元测试断言金额文本的稳定 API，必须始终输出两位小数且不使用科学计数法。
11. `MoneyValue.add(other)` 和 `MoneyValue.subtract(other)` 只接收 `MoneyValue`，不接收裸 `BigDecimal` 或浮点类型，避免统计服务绕过统一金额语义。
12. 两个值对象都不提供 `double` 或 `float` 构造、工厂或运算接口。
13. 两个值对象的 `equals`、`hashCode` 使用 Java record 默认语义；构造期规范化保证常见金额文本如 `1.2` 与 `1.20` 能按值对象语义相等。
14. 两个值对象的 record 默认 `toString()` 只用于调试和日志可读性；用户界面金额展示应使用 `MoneyValue.toPlainString()`，单笔金额展示可在后续控制台层读取 `TransactionAmount.value().toPlainString()` 或先转换为 `MoneyValue`。
15. 本轮新增生产代码不得读取系统当前时间、环境变量、文件、网络、AI 配置或 DeepSeek API。
16. 本轮新增单元测试必须使用固定字面量金额，不能依赖真实当前时间、网络、API Key 或外部文件。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.common.TransactionAmount` | Java 标准库 `java.math.BigDecimal`、`java.util.Objects`。 |
| `assistant.common.MoneyValue` | Java 标准库 `java.math.BigDecimal`、`java.math.RoundingMode`、`java.util.Objects`；同包 `assistant.common.TransactionAmount`。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `assistant.common.TransactionAmountTest` | JUnit Jupiter 断言 API、Java 标准库 `java.math.BigDecimal`、`assistant.common.TransactionAmount`。 |
| `assistant.common.MoneyValueTest` | JUnit Jupiter 断言 API、Java 标准库 `java.math.BigDecimal`、`assistant.common.TransactionAmount`、`assistant.common.MoneyValue`。 |

后续任务中的 `finance.TransactionRecord` 应使用 `TransactionAmount` 表达单笔收入或支出金额；`FinanceStatisticsService` 应使用 `MoneyValue.zero()` 初始化收入总额、支出总额和结余，并通过 `MoneyValue.from(TransactionAmount)`、`add`、`subtract` 完成统计计算，不在 finance 模块内重复散落 `BigDecimal` 小数位、舍入或展示规则。
