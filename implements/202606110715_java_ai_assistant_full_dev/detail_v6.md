# 详细设计（v6）

## 概述

本轮设计目标是在既有 `java-ai-assistant/` Maven 单模块工程中新增两个通用值对象：`assistant.common.Progress` 和 `assistant.common.Tag`，并补充对应 JUnit Jupiter 单元测试规格。

`Progress` 表达学习计划完成进度，底层使用 `int`，构造阶段集中封装 0 到 100 的合法边界，并提供 `isComplete()` 作为后续学习计划状态分析可直接复用的完成判断语义。`Tag` 表达笔记标签，底层使用 `String`，构造阶段统一去除首尾空白、拒绝空标签，并将标签文本归一为小写，使笔记服务、标签查询、标签分布统计和 AI 本地上下文都能复用同一相等性语义。

两个类型都保持不可变值对象语义，沿用当前 `assistant.common.EntityId`、`DateRange`、`DateTimeRange`、`TransactionAmount` 和 `MoneyValue` 的 record 风格、构造阶段校验和 Java 标准异常语义。本轮范围仅包含 `assistant.common` 中的两个基础值对象及其单元测试，不实现 `study`、`note`、`summary` 或 `ai` 业务包，不实现学习计划服务、笔记服务、标签统计服务、控制台交互或 DeepSeek 接入。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/common/Progress.java` | 新建 | 定义学习计划进度值对象，集中处理 0 到 100 边界、默认进度、完成进度和完成判断。 |
| `java-ai-assistant/src/main/java/assistant/common/Tag.java` | 新建 | 定义笔记标签值对象，集中处理去除首尾空白、拒绝空标签、大小写归一和展示文本。 |
| `java-ai-assistant/src/test/java/assistant/common/ProgressTest.java` | 新建 | 覆盖 `Progress` 的 0、100、中间值、小于 0、大于 100、工厂、完成判断、相等性和 record 默认字符串格式。 |
| `java-ai-assistant/src/test/java/assistant/common/TagTest.java` | 新建 | 覆盖 `Tag` 的去除空白、拒绝空值和空标签、大小写归一相等性、展示文本、集合去重和 record 默认字符串格式。 |

## 类型定义

### `Progress`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表达学习计划完成进度的不可变百分比值对象，避免后续 `study` 模块在实体、服务和状态分析组件中重复判断进度边界。

**类型签名定义**：`public record Progress(int value)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `int value` | 必须大于等于 `0` 且小于等于 `100`；构造成功后原样保存。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public Progress(int value)` | 构造器 | 创建进度值对象；`value < 0` 或 `value > 100` 时抛出 `IllegalArgumentException`；`0`、`100` 和区间内整数均合法。 |
| `public static Progress zero()` | `Progress` | 返回表示初始进度 `0` 的值对象，用于后续学习计划创建默认值。 |
| `public static Progress complete()` | `Progress` | 返回表示完成进度 `100` 的值对象，用于后续完成状态测试和服务调用。 |
| `public static Progress of(int value)` | `Progress` | 语义化工厂方法，等价于构造器校验，用于后续服务层和测试代码在不暴露构造语义细节时创建进度。 |
| `public int value()` | `int` | 返回进度百分比整数；由 record 自动提供。 |
| `public boolean isComplete()` | `boolean` | 当且仅当 `value == 100` 时返回 `true`；后续 `StudyPlanAnalysisService` 必须优先使用该语义判断已完成状态。 |
| `public String toPercentageString()` | `String` | 返回稳定展示文本，格式为 `{value}%`，例如 `0%`、`75%`、`100%`。 |
| `public boolean equals(Object other)` | `boolean` | 由 record 自动提供；进度值相同即相等。 |
| `public int hashCode()` | `int` | 由 record 自动提供；与 `equals` 使用同一记录组件。 |
| `public String toString()` | `String` | 由 record 自动提供稳定可读格式，不作为控制台展示文本依赖；业务展示使用 `toPercentageString()`。 |

**构造方式**：

- 业务代码可调用 `new Progress(int value)` 创建。
- 后续学习计划创建默认初始进度可调用 `Progress.zero()`。
- 后续测试或服务表达完成进度可调用 `Progress.complete()`。
- 后续服务层可调用 `Progress.of(int value)` 作为语义化入口。
- 不提供接收 `double`、`float`、`BigDecimal` 或 `String` 的工厂方法；控制台输入解析属于 app 层或后续服务边界职责，解析后传入整数。

**边界规则**：

- `new Progress(0)` 成功，表示未完成或刚开始。
- `new Progress(1)` 到 `new Progress(99)` 成功，表示部分完成。
- `new Progress(100)` 成功，且 `isComplete()` 返回 `true`。
- `new Progress(-1)` 被拒绝。
- `new Progress(101)` 被拒绝。

**类型关系**：依赖 Java 语言基础类型 `int`；不继承自定义基类，不实现自定义接口；后续 `assistant.study.StudyPlan` 可组合持有该值对象，`assistant.study.StudyPlanAnalysisService` 可使用 `isComplete()` 作为状态推导最高优先级条件。

### `Tag`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表达笔记标签的不可变值对象，集中封装标签文本清理、空标签拒绝和大小写归一后的相等性语义，避免后续笔记服务、标签查询、标签分布统计和 AI 本地上下文重复处理标签规范化。

**类型签名定义**：`public record Tag(String value)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `String value` | 必须非空；调用 `strip()` 去除首尾 Unicode 空白后不得为空；构造成功后统一保存为 `Locale.ROOT` 小写文本。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public Tag(String value)` | 构造器 | 创建标签值对象；`value == null` 时抛出 `NullPointerException`；`value.strip()` 为空时抛出 `IllegalArgumentException`；合法输入保存为去除首尾空白并按 `Locale.ROOT` 转小写后的文本。 |
| `public static Tag of(String value)` | `Tag` | 语义化工厂方法，等价于构造器校验和规范化，用于后续服务层、查询条件和测试代码创建标签。 |
| `public String value()` | `String` | 返回规范化后的标签文本；由 record 自动提供；构造成功后返回值非空、非空白、无首尾空白且为 `Locale.ROOT` 小写。 |
| `public String displayName()` | `String` | 返回用于控制台、统计结果和 AI 本地上下文的稳定标签展示文本；本轮设计中与 `value()` 相同。 |
| `public boolean equals(Object other)` | `boolean` | 由 record 自动提供；由于构造期已去除首尾空白并小写归一，`new Tag(" Java ")`、`new Tag("java")` 和 `new Tag("JAVA")` 相等。 |
| `public int hashCode()` | `int` | 由 record 自动提供；与 `equals` 使用同一规范化记录组件，可直接用于 `Set<Tag>` 去重和 `Map<Tag, Integer>` 标签分布统计。 |
| `public String toString()` | `String` | 由 record 自动提供稳定可读格式，不作为用户界面标签展示文本依赖；业务展示使用 `displayName()`。 |

**构造方式**：

- 业务代码可调用 `new Tag(String value)` 创建。
- 后续 `NoteService`、`NoteQuery`、标签查询和测试代码可调用 `Tag.of(String value)` 创建。
- 后续笔记实体应持有 `Set<Tag>` 或不可变标签集合，不应持有裸 `String` 标签集合。
- 后续标签查询和标签分布统计必须按 `Tag` 的 `equals` 和 `hashCode` 比较，不在服务层重复执行 `trim`、`strip` 或大小写转换。

**规范化规则**：

- `new Tag("java")` 成功，组件值为 `java`。
- `new Tag(" Java ")` 成功，组件值为 `java`。
- `new Tag("JAVA")` 成功，组件值为 `java`。
- `new Tag("  Review Notes  ")` 成功，组件值为 `review notes`；本轮不拆分标签内部空白。
- `new Tag(null)` 被拒绝。
- `new Tag("")`、`new Tag("   ")` 和仅包含可被 `strip()` 去除的空白文本被拒绝。

**类型关系**：依赖 Java 标准库 `java.util.Locale` 和 `java.util.Objects`；不继承自定义基类，不实现自定义接口；后续 `assistant.note.Note` 可组合持有 `Set<Tag>`，`assistant.note.NoteSearchPolicy` 可按 `Tag` 比较执行标签查询，`assistant.summary.SummaryService` 可使用 `Map<Tag, Integer>` 或等价只读 DTO 表达标签分布。

## 单元测试规格

### `ProgressTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void acceptsZeroProgress()` | `new Progress(0)` 成功，`value()` 返回 `0`，`isComplete()` 返回 `false`，`toPercentageString()` 返回 `0%`。 |
| `void acceptsMiddleProgress()` | `new Progress(75)` 成功，`value()` 返回 `75`，`isComplete()` 返回 `false`，`toPercentageString()` 返回 `75%`。 |
| `void acceptsCompleteProgress()` | `new Progress(100)` 成功，`value()` 返回 `100`，`isComplete()` 返回 `true`，`toPercentageString()` 返回 `100%`。 |
| `void rejectsNegativeProgress()` | `new Progress(-1)` 抛出 `IllegalArgumentException`。 |
| `void rejectsProgressGreaterThanOneHundred()` | `new Progress(101)` 抛出 `IllegalArgumentException`。 |
| `void zeroFactoryReturnsZeroProgress()` | `Progress.zero()` 返回 `0`，且未完成。 |
| `void completeFactoryReturnsCompleteProgress()` | `Progress.complete()` 返回 `100`，且已完成。 |
| `void ofFactoryUsesSameValidationAsConstructor()` | `Progress.of(50)` 成功；`Progress.of(-1)` 和 `Progress.of(101)` 抛出 `IllegalArgumentException`。 |
| `void equalityAndHashCodeUseProgressValue()` | 相同数值的 `Progress` 相等且哈希一致，不同数值不相等。 |
| `void toStringUsesRecordComponentNameAndValue()` | record 默认 `toString()` 返回 `Progress[value=75]`。 |

### `TagTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void createsTagWithNormalizedLowercaseValue()` | `new Tag("Java")` 成功，`value()` 和 `displayName()` 均返回 `java`。 |
| `void trimsLeadingAndTrailingWhitespace()` | `new Tag("  java  ")` 成功，组件值为 `java`。 |
| `void normalizesUppercaseAndMixedCaseForEquality()` | `new Tag("JAVA")`、`new Tag("Java")` 与 `new Tag("java")` 相等且哈希一致。 |
| `void preservesInternalWhitespace()` | `new Tag(" Review Notes ")` 成功，组件值为 `review notes`，不拆分或压缩内部空白。 |
| `void factoryCreatesNormalizedTag()` | `Tag.of(" Study ")` 成功，`value()` 返回 `study`。 |
| `void rejectsNullValue()` | `new Tag(null)` 和 `Tag.of(null)` 抛出 `NullPointerException`。 |
| `void rejectsEmptyValue()` | `new Tag("")` 抛出 `IllegalArgumentException`。 |
| `void rejectsBlankValue()` | `new Tag("   ")` 抛出 `IllegalArgumentException`。 |
| `void normalizedTagsCanBeUsedAsSetKeys()` | `Set<Tag>` 中加入 `Tag.of("Java")` 和 `Tag.of(" java ")` 后只保留一个元素。 |
| `void normalizedTagsCanBeUsedAsMapKeysForDistribution()` | `Map<Tag, Integer>` 使用 `Tag.of("AI")` 写入后，可通过 `Tag.of(" ai ")` 读取同一计数。 |
| `void toStringUsesRecordComponentNameAndNormalizedValue()` | record 默认 `toString()` 返回 `Tag[value=java]`。 |

## 错误处理

两个值对象都属于 `assistant.common` 底层基础类型，直接使用 Java 标准异常表达调用方输入错误，不引入 `BusinessException`、`OperationResult` 或新的 `ErrorCode`。后续应用服务边界如需面向控制台返回统一错误分类，可捕获这些标准异常并转换为 `OperationResult`。

| 场景 | 异常类型 | 说明 |
|------|----------|------|
| `Progress` 构造参数 `value < 0` | `IllegalArgumentException` | 进度不能小于 0。 |
| `Progress` 构造参数 `value > 100` | `IllegalArgumentException` | 进度不能大于 100。 |
| `Progress.of(value)` 参数越界 | `IllegalArgumentException` | 工厂方法复用构造器校验。 |
| `Tag` 构造参数 `value == null` | `NullPointerException` | 标签文本必须明确。 |
| `Tag` 构造参数清理后为空 | `IllegalArgumentException` | 空标签或纯空白标签不具备业务意义。 |
| `Tag.of(null)` | `NullPointerException` | 工厂方法复用构造器空值校验。 |
| `Tag.of(blank)` | `IllegalArgumentException` | 工厂方法复用构造器空标签校验。 |

异常消息保持简短、可读即可，不作为本轮测试强约束。测试断言异常类型，不断言完整异常文本。

## 行为契约

1. `Progress` 是不可变值对象，构造成功后 `value()` 必须始终处于闭区间 `[0, 100]`。
2. `Progress` 只表达百分比整数进度，不负责学习计划日期判断、逾期判断、完成数量统计或 AI 拆解建议。
3. `Progress.isComplete()` 是后续学习计划状态分析判断完成状态的稳定 API；当进度为 `100` 时应优先视为已完成。
4. `Progress.zero()` 和 `Progress.complete()` 每次可返回新实例或等价实例；调用方不得依赖对象引用相同，只能依赖值对象相等性。
5. `Progress.toPercentageString()` 是后续展示和测试断言百分比文本的稳定 API，必须返回整数加 `%`，不增加小数位。
6. `Tag` 是不可变值对象，构造成功后 `value()` 必须非空、非空白、无首尾空白并按 `Locale.ROOT` 小写归一。
7. `Tag` 的大小写归一由值对象集中完成；后续笔记服务、标签查询、标签分布统计和 AI 本地上下文不得重复维护独立大小写规则。
8. `Tag` 不压缩、不拆分、不删除内部空白；标签内部格式是否进一步限制留给后续业务需求，不在本轮增加。
9. `Tag.displayName()` 是后续控制台展示、统计展示和 AI 上下文输出标签文本的稳定 API；本轮返回值与规范化后的 `value()` 相同。
10. `Tag` 的 `equals` 和 `hashCode` 使用 Java record 默认语义；构造期规范化保证大小写和首尾空白差异不会破坏标签值对象相等性。
11. 后续 `Note` 和 `NoteQuery` 应使用 `Tag` 而不是裸 `String` 表达标签；如需接收用户字符串输入，应在服务边界立即转换为 `Tag`。
12. 后续标签分布统计应优先使用 `Tag` 作为 key；如果输出 DTO 需要字符串标签名，应通过 `displayName()` 转换。
13. 两个值对象的 record 默认 `toString()` 只用于调试和日志可读性；用户界面展示应使用 `Progress.toPercentageString()` 和 `Tag.displayName()`。
14. 本轮新增生产代码不得读取系统当前时间、环境变量、文件、网络、AI 配置或 DeepSeek API。
15. 本轮新增单元测试必须使用固定字面量进度和标签文本，不能依赖真实当前时间、网络、API Key 或外部文件。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.common.Progress` | Java 语言基础类型 `int`。 |
| `assistant.common.Tag` | Java 标准库 `java.util.Locale`、`java.util.Objects`。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `assistant.common.ProgressTest` | JUnit Jupiter 断言 API、`assistant.common.Progress`。 |
| `assistant.common.TagTest` | JUnit Jupiter 断言 API、Java 标准库 `java.util.HashMap`、`java.util.HashSet`、`java.util.Map`、`java.util.Set`、`assistant.common.Tag`。 |

后续任务中的 `assistant.study.StudyPlan` 应使用 `Progress` 表达计划进度，并由 `StudyPlanAnalysisService` 调用 `isComplete()` 确保进度达到 `100` 时优先视为已完成。后续 `assistant.note.Note`、`NoteQuery`、`NoteSearchPolicy` 和 `assistant.summary.SummaryService` 应使用 `Tag` 表达、比较和统计标签，避免在业务服务中散落字符串清理、空标签校验和大小写转换规则。
