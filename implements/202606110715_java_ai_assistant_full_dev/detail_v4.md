# 详细设计（v4）

## 概述

本轮设计目标是在既有 `java-ai-assistant/` Maven 单模块工程中新增两个跨业务日期区间值对象：`assistant.common.DateRange` 和 `assistant.common.DateTimeRange`，并补充对应 JUnit Jupiter 单元测试规格。

`DateRange` 表达 `LocalDate` 左右闭区间，用于收支日期筛选、学习计划周期、本周统计和本月统计。`DateTimeRange` 表达 `LocalDateTime` 左闭右开区间，用于日程时间合法性、日程冲突识别和按日期查询。两个类型都保持不可变值对象语义，沿用当前 `assistant.common.EntityId` 的 record 风格、构造阶段校验和 JDK 标准异常语义。

本轮范围仅包含两个通用值对象及其单元测试，不实现日程、学习计划、收支查询、汇总统计或服务层错误转换。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/common/DateRange.java` | 新建 | 定义 `LocalDate` 左右闭区间值对象，集中处理日期区间边界校验、日期包含和闭区间非空重叠判断。 |
| `java-ai-assistant/src/main/java/assistant/common/DateTimeRange.java` | 新建 | 定义 `LocalDateTime` 左闭右开区间值对象，集中处理日期时间边界校验、时刻包含、非空重叠和覆盖指定日期判断。 |
| `java-ai-assistant/src/test/java/assistant/common/DateRangeTest.java` | 新建 | 覆盖 `DateRange` 构造、空参数、非法边界、闭区间包含、闭区间重叠和不可变值对象语义。 |
| `java-ai-assistant/src/test/java/assistant/common/DateTimeRangeTest.java` | 新建 | 覆盖 `DateTimeRange` 构造、空参数、非法边界、左闭右开包含、首尾相接重叠边界、覆盖日期和不可变值对象语义。 |

## 类型定义

### `DateRange`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表达由开始日期和结束日期组成的不可变 `LocalDate` 闭区间，并为跨业务日期筛选与统计提供统一边界语义。

**类型签名定义**：`public record DateRange(LocalDate startDate, LocalDate endDate)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `LocalDate startDate` | 必须非空；闭区间起点，包含在区间内。 |
| `LocalDate endDate` | 必须非空；闭区间终点，包含在区间内；不得早于 `startDate`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DateRange(LocalDate startDate, LocalDate endDate)` | 构造器 | 创建闭区间；任一参数为 `null` 时抛出 `NullPointerException`；`startDate.isAfter(endDate)` 时抛出 `IllegalArgumentException`。 |
| `public LocalDate startDate()` | `LocalDate` | 返回闭区间起点；由 record 自动提供；不得返回 `null`。 |
| `public LocalDate endDate()` | `LocalDate` | 返回闭区间终点；由 record 自动提供；不得返回 `null`。 |
| `public boolean contains(LocalDate date)` | `boolean` | 判断 `date` 是否位于 `[startDate, endDate]` 内；`date == null` 时抛出 `NullPointerException`。 |
| `public boolean overlaps(DateRange other)` | `boolean` | 判断当前闭区间与 `other` 是否存在至少一个共同日期；`other == null` 时抛出 `NullPointerException`。 |
| `public boolean equals(Object other)` | `boolean` | 由 record 自动提供；两个 `DateRange` 的 `startDate` 和 `endDate` 均相等时相等。 |
| `public int hashCode()` | `int` | 由 record 自动提供；与 `equals` 使用同一组记录组件。 |
| `public String toString()` | `String` | 由 record 自动提供稳定可读格式，不作为业务展示文本依赖。 |

**构造方式**：调用 `new DateRange(LocalDate startDate, LocalDate endDate)` 创建；允许 `startDate.equals(endDate)` 表示单日闭区间。

**类型关系**：依赖 Java 标准库 `java.time.LocalDate` 和 `java.util.Objects`；不继承自定义基类，不实现自定义接口；后续 `finance.TransactionQuery`、`study.StudyPlan`、`summary.SummaryService` 可组合持有或接收该值对象。

### `DateTimeRange`

**形态**：`record`

**包路径**：`assistant.common`

**职责**：表达由开始日期时间和结束日期时间组成的不可变 `LocalDateTime` 左闭右开区间，并为日程时间校验、冲突识别和按日期查询提供统一边界语义。

**类型签名定义**：`public record DateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime)`

**记录组件**：

| 组件签名 | 约束 |
|----------|------|
| `LocalDateTime startDateTime` | 必须非空；左闭右开区间起点，包含在区间内。 |
| `LocalDateTime endDateTime` | 必须非空；左闭右开区间终点，不包含在区间内；必须晚于 `startDateTime`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime)` | 构造器 | 创建左闭右开区间；任一参数为 `null` 时抛出 `NullPointerException`；`!endDateTime.isAfter(startDateTime)` 时抛出 `IllegalArgumentException`。 |
| `public LocalDateTime startDateTime()` | `LocalDateTime` | 返回区间起点；由 record 自动提供；不得返回 `null`。 |
| `public LocalDateTime endDateTime()` | `LocalDateTime` | 返回区间终点；由 record 自动提供；不得返回 `null`。 |
| `public boolean contains(LocalDateTime dateTime)` | `boolean` | 判断 `dateTime` 是否位于 `[startDateTime, endDateTime)` 内；等于起点返回 `true`，等于终点返回 `false`；`dateTime == null` 时抛出 `NullPointerException`。 |
| `public boolean overlaps(DateTimeRange other)` | `boolean` | 判断当前区间与 `other` 是否存在非空日期时间重叠；两个区间仅首尾相接时返回 `false`；`other == null` 时抛出 `NullPointerException`。 |
| `public boolean coversDate(LocalDate date)` | `boolean` | 判断当前左闭右开区间是否覆盖指定自然日的任意时刻；`date == null` 时抛出 `NullPointerException`。 |
| `public boolean equals(Object other)` | `boolean` | 由 record 自动提供；两个 `DateTimeRange` 的 `startDateTime` 和 `endDateTime` 均相等时相等。 |
| `public int hashCode()` | `int` | 由 record 自动提供；与 `equals` 使用同一组记录组件。 |
| `public String toString()` | `String` | 由 record 自动提供稳定可读格式，不作为业务展示文本依赖。 |

**构造方式**：调用 `new DateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime)` 创建；不允许零长度区间，即 `startDateTime.equals(endDateTime)` 非法。

**类型关系**：依赖 Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime` 和 `java.util.Objects`；不继承自定义基类，不实现自定义接口；后续 `schedule.ScheduleItem`、`schedule.ScheduleConflictPolicy`、`schedule.ScheduleService` 可组合持有或接收该值对象。

## 单元测试规格

### `DateRangeTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructsClosedDateRangeAndExposesBounds()` | 构造合法区间后，`startDate()` 与 `endDate()` 返回构造参数。 |
| `void allowsSingleDayRange()` | `startDate.equals(endDate)` 时构造成功，并且该日期被 `contains` 包含。 |
| `void rejectsNullStartDate()` | `startDate == null` 时构造抛出 `NullPointerException`。 |
| `void rejectsNullEndDate()` | `endDate == null` 时构造抛出 `NullPointerException`。 |
| `void rejectsStartDateAfterEndDate()` | 起点晚于终点时构造抛出 `IllegalArgumentException`。 |
| `void containsIncludesStartAndEndDates()` | 闭区间包含起点和终点。 |
| `void containsIncludesInteriorDate()` | 闭区间包含中间日期。 |
| `void containsExcludesDatesOutsideRange()` | 闭区间不包含早于起点或晚于终点的日期。 |
| `void containsRejectsNullDate()` | `contains(null)` 抛出 `NullPointerException`。 |
| `void overlapsWhenRangesShareInteriorDates()` | 两个闭区间存在多个共同日期时返回 `true`。 |
| `void overlapsWhenRangesTouchAtBoundaryDate()` | 两个闭区间仅共享一个边界日期时返回 `true`，因为重叠非空。 |
| `void doesNotOverlapWhenRangesAreSeparated()` | 两个闭区间之间至少间隔一天时返回 `false`。 |
| `void overlapsRejectsNullOtherRange()` | `overlaps(null)` 抛出 `NullPointerException`。 |
| `void equalityAndHashCodeUseBothBounds()` | 相同起止日期的区间相等且哈希一致，不同边界不相等。 |

### `DateTimeRangeTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructsDateTimeRangeAndExposesBounds()` | 构造合法区间后，`startDateTime()` 与 `endDateTime()` 返回构造参数。 |
| `void rejectsNullStartDateTime()` | `startDateTime == null` 时构造抛出 `NullPointerException`。 |
| `void rejectsNullEndDateTime()` | `endDateTime == null` 时构造抛出 `NullPointerException`。 |
| `void rejectsEndDateTimeEqualToStartDateTime()` | 终点等于起点时构造抛出 `IllegalArgumentException`。 |
| `void rejectsEndDateTimeBeforeStartDateTime()` | 终点早于起点时构造抛出 `IllegalArgumentException`。 |
| `void containsIncludesStartDateTime()` | 左闭右开区间包含起点。 |
| `void containsIncludesInteriorDateTime()` | 左闭右开区间包含内部时刻。 |
| `void containsExcludesEndDateTime()` | 左闭右开区间不包含终点。 |
| `void containsExcludesDateTimesOutsideRange()` | 区间不包含早于起点或晚于等于终点的时刻。 |
| `void containsRejectsNullDateTime()` | `contains(null)` 抛出 `NullPointerException`。 |
| `void overlapsWhenRangesShareInteriorDateTimes()` | 两个区间存在非空时间交集时返回 `true`。 |
| `void doesNotOverlapWhenRangesTouchAtBoundary()` | 一个区间终点等于另一个区间起点时返回 `false`，支持日程首尾相接不冲突。 |
| `void doesNotOverlapWhenRangesAreSeparated()` | 两个区间之间存在时间间隔时返回 `false`。 |
| `void overlapsRejectsNullOtherRange()` | `overlaps(null)` 抛出 `NullPointerException`。 |
| `void coversDateWhenRangeStartsOnDate()` | 区间起点落在目标日期内时，`coversDate(date)` 返回 `true`。 |
| `void coversDateWhenRangeSpansAcrossDate()` | 跨日区间覆盖目标日期任意时刻时返回 `true`。 |
| `void coversDateExcludesDateAtExclusiveEndBoundary()` | 区间终点正好等于目标日期零点且区间没有覆盖该日期时返回 `false`。 |
| `void coversDateRejectsNullDate()` | `coversDate(null)` 抛出 `NullPointerException`。 |
| `void equalityAndHashCodeUseBothBounds()` | 相同起止日期时间的区间相等且哈希一致，不同边界不相等。 |

## 错误处理

两个值对象都属于 `assistant.common` 底层基础类型，直接使用 Java 标准异常表达调用方输入错误，不引入 `BusinessException`、`OperationResult` 或新的 `ErrorCode`。

| 场景 | 异常类型 | 说明 |
|------|----------|------|
| `DateRange` 构造参数 `startDate == null` | `NullPointerException` | 起点日期是必需状态。 |
| `DateRange` 构造参数 `endDate == null` | `NullPointerException` | 终点日期是必需状态。 |
| `DateRange` 构造参数 `startDate.isAfter(endDate)` | `IllegalArgumentException` | 日期闭区间边界非法。 |
| `DateRange.contains(null)` | `NullPointerException` | 被判断日期必须明确。 |
| `DateRange.overlaps(null)` | `NullPointerException` | 被比较区间必须明确。 |
| `DateTimeRange` 构造参数 `startDateTime == null` | `NullPointerException` | 起点日期时间是必需状态。 |
| `DateTimeRange` 构造参数 `endDateTime == null` | `NullPointerException` | 终点日期时间是必需状态。 |
| `DateTimeRange` 构造参数 `!endDateTime.isAfter(startDateTime)` | `IllegalArgumentException` | 左闭右开日期时间区间必须具有正长度。 |
| `DateTimeRange.contains(null)` | `NullPointerException` | 被判断时刻必须明确。 |
| `DateTimeRange.overlaps(null)` | `NullPointerException` | 被比较区间必须明确。 |
| `DateTimeRange.coversDate(null)` | `NullPointerException` | 被判断日期必须明确。 |

异常消息保持简短、可读即可，不作为本轮测试强约束。测试断言异常类型，不断言完整异常文本。

## 行为契约

1. `DateRange` 是不可变值对象；record 组件均为不可变的 `LocalDate`，创建后不能修改。
2. `DateRange` 允许单日区间，即 `startDate.equals(endDate)` 合法。
3. `DateRange.contains(date)` 采用左右闭区间语义：`date.equals(startDate)` 和 `date.equals(endDate)` 均返回 `true`。
4. `DateRange.overlaps(other)` 判断非空日期交集；两个闭区间仅在边界日期相接且共享该日期时返回 `true`。
5. `DateRange` 不关心时区、当前时间或日期格式解析；调用方必须传入已经解析完成的 `LocalDate`。
6. `DateTimeRange` 是不可变值对象；record 组件均为不可变的 `LocalDateTime`，创建后不能修改。
7. `DateTimeRange` 不允许零长度或反向区间；`endDateTime` 必须严格晚于 `startDateTime`。
8. `DateTimeRange.contains(dateTime)` 采用左闭右开语义：等于起点返回 `true`，等于终点返回 `false`。
9. `DateTimeRange.overlaps(other)` 判断非空日期时间交集；一个区间的终点等于另一个区间的起点时返回 `false`，后续日程冲突策略可直接据此支持“首尾相接不冲突”。
10. `DateTimeRange.coversDate(date)` 判断区间与目标自然日 `[date.atStartOfDay(), date.plusDays(1).atStartOfDay())` 是否存在非空交集。
11. `DateTimeRange.coversDate(date)` 对跨日安排返回稳定结果：只要该区间包含目标日期内任一时刻即返回 `true`；如果区间仅在目标日期零点的排他终点结束，则返回 `false`。
12. 两个值对象的 `equals`、`hashCode` 和 `toString` 使用 Java record 默认语义。
13. 本轮新增生产代码不得读取系统当前时间、环境变量、文件、网络、AI 配置或 DeepSeek API。
14. 本轮新增单元测试必须使用固定字面量日期和日期时间，不能依赖真实当前时间、网络、API Key 或外部文件。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.common.DateRange` | Java 标准库 `java.time.LocalDate`、`java.util.Objects`。 |
| `assistant.common.DateTimeRange` | Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`、`java.util.Objects`。 |

本轮测试代码依赖关系如下：

| 测试类 | 依赖 |
|--------|------|
| `assistant.common.DateRangeTest` | JUnit Jupiter 断言 API、Java 标准库 `java.time.LocalDate`、`assistant.common.DateRange`。 |
| `assistant.common.DateTimeRangeTest` | JUnit Jupiter 断言 API、Java 标准库 `java.time.LocalDate`、`java.time.LocalDateTime`、`assistant.common.DateTimeRange`。 |

后续任务中的收支查询、学习计划、汇总统计和日程冲突识别应复用本轮公开接口，不在各业务服务内重复散落日期或日期时间区间比较逻辑。
