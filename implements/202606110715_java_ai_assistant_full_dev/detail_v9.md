# 详细设计（v9）

## 概述

本轮设计目标是在独立 Maven 工程 `java-ai-assistant` 中新增 `assistant.schedule` 日程提醒核心领域模型：动态状态枚举 `ScheduleStatus`、可变日程实体 `ScheduleItem` 和无状态冲突判断策略 `ScheduleConflictPolicy`。

本轮只覆盖日程基础领域类型和冲突策略，不实现 `ScheduleService`、日程仓储、日程查询 DTO、控制台菜单、汇总统计或 AI 日程建议。设计复用既有 `assistant.common.EntityId` 和 `assistant.common.DateTimeRange`：日程时间范围沿用左闭右开语义，结束时间必须晚于开始时间；冲突判断集中委托 `DateTimeRange.overlaps(...)`，首尾相接不冲突；状态不持久化，由调用方传入的 `LocalDateTime` 动态推导，不读取真实系统时间。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleStatus.java` | 新建 | 定义日程动态展示状态枚举，包含即将开始、进行中和已过期语义。 |
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleItem.java` | 新建 | 定义日程领域实体，封装编号、名称、时间范围、地点、备注及字段不变量。 |
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleConflictPolicy.java` | 新建 | 定义日程冲突判断策略，集中判断两个日程或候选日程与既有集合是否存在非空时间重叠。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleStatusTest.java` | 新建 | 覆盖状态枚举常量、展示文本和语义方法。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleItemTest.java` | 新建 | 覆盖日程实体构造、字段规范化、修改失败不变性、动态状态和日期覆盖。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleConflictPolicyTest.java` | 新建 | 覆盖冲突、非冲突、首尾相接、跨日期覆盖、集合扫描和空参数拒绝。 |

## 类型定义

### `ScheduleStatus`

**形态**：`enum`

**包路径**：`assistant.schedule`

**职责**：表达日程在指定当前时间下的动态展示状态。该状态不保存在 `ScheduleItem` 字段中，只由 `ScheduleItem.statusAt(LocalDateTime currentDateTime)` 推导。

**类型签名定义**：`public enum ScheduleStatus`

**枚举常量**：

| 常量 | 语义 |
|------|------|
| `UPCOMING` | 当前时间早于日程开始时间，表示即将开始。 |
| `ONGOING` | 当前时间大于等于开始时间且小于结束时间，表示正在进行。 |
| `EXPIRED` | 当前时间大于等于结束时间，表示已过期。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public String displayName()` | `String` | 返回稳定中文展示文本；`UPCOMING` 返回 `"即将开始"`，`ONGOING` 返回 `"进行中"`，`EXPIRED` 返回 `"已过期"`。 |
| `public boolean isUpcoming()` | `boolean` | 当且仅当当前枚举值为 `UPCOMING` 时返回 `true`。 |
| `public boolean isOngoing()` | `boolean` | 当且仅当当前枚举值为 `ONGOING` 时返回 `true`。 |
| `public boolean isExpired()` | `boolean` | 当且仅当当前枚举值为 `EXPIRED` 时返回 `true`。 |
| `public static ScheduleStatus valueOf(String name)` | `ScheduleStatus` | enum 自动提供；按常量名解析，未知名称抛出 `IllegalArgumentException`。 |
| `public static ScheduleStatus[] values()` | `ScheduleStatus[]` | enum 自动提供；返回声明顺序为 `UPCOMING`、`ONGOING`、`EXPIRED` 的数组副本。 |

**构造方式**：

- 调用方不直接构造枚举实例，只使用固定常量。
- 状态应通过 `ScheduleItem.statusAt(...)` 得到，不应作为 `ScheduleItem` 持久字段保存。

**类型关系**：

- 被 `ScheduleItem.statusAt(...)` 返回。
- 后续 `ScheduleView` 或 `ScheduleService` 可使用 `displayName()` 展示状态文本。
- 不依赖 `TimeProvider`、系统时间、仓储、控制台或 AI 模块。

### `ScheduleItem`

**形态**：`class`

**包路径**：`assistant.schedule`

**职责**：表示课程、会议、考试或个人安排等单条日程，集中保护名称、时间范围、地点和备注字段不变量，并提供基于传入当前时间的动态状态推导。

**类型签名定义**：`public class ScheduleItem`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final EntityId id` | 构造后不可变 | 必须非空；用于后续仓储保存、修改和删除定位。 |
| `private String name` | 可变 | 必须非空，`strip()` 后不得为空；保存规范化后的名称。 |
| `private DateTimeRange timeRange` | 可变 | 必须非空；由 `DateTimeRange` 保证开始和结束非空且结束晚于开始。 |
| `private String location` | 可变 | 允许入参为 `null`；保存为非空字符串；非空入参经 `strip()` 后保存。 |
| `private String note` | 可变 | 允许入参为 `null`；保存为非空字符串；非空入参经 `strip()` 后保存。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ScheduleItem(EntityId id, String name, DateTimeRange timeRange, String location, String note)` | 构造器 | 创建日程实体；`id == null`、`name == null` 或 `timeRange == null` 时抛出 `NullPointerException`；`name.strip().isEmpty()` 时抛出 `IllegalArgumentException`；`location == null` 和 `note == null` 分别规范化为 `""`。 |
| `public static ScheduleItem create(EntityId id, String name, DateTimeRange timeRange, String location, String note)` | `ScheduleItem` | 语义化工厂方法；等价于调用构造器，方便后续服务层表达创建新日程。 |
| `public EntityId getId()` | `EntityId` | 返回日程编号。 |
| `public String getName()` | `String` | 返回规范化后的日程名称。 |
| `public DateTimeRange getTimeRange()` | `DateTimeRange` | 返回当前时间范围值对象。 |
| `public LocalDateTime getStartDateTime()` | `LocalDateTime` | 返回 `timeRange.startDateTime()`。 |
| `public LocalDateTime getEndDateTime()` | `LocalDateTime` | 返回 `timeRange.endDateTime()`。 |
| `public String getLocation()` | `String` | 返回规范化后的地点；永不为 `null`。 |
| `public String getNote()` | `String` | 返回规范化后的备注；永不为 `null`。 |
| `public void updateDetails(String name, DateTimeRange timeRange, String location, String note)` | `void` | 修改名称、时间范围、地点和备注；新名称或新时间范围非法时抛出对应异常且既有字段保持不变；成功时不改变 `id`。 |
| `public ScheduleStatus statusAt(LocalDateTime currentDateTime)` | `ScheduleStatus` | `currentDateTime == null` 时抛出 `NullPointerException`；当前时间早于开始时间返回 `UPCOMING`；当前时间大于等于开始且小于结束返回 `ONGOING`；当前时间大于等于结束返回 `EXPIRED`。 |
| `public boolean coversDate(LocalDate date)` | `boolean` | `date == null` 时抛出 `NullPointerException`；委托 `timeRange.coversDate(date)` 判断日程是否覆盖该自然日，支持跨日期日程。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String normalizeName(String name)` | `String` | `name == null` 时抛出 `NullPointerException`；返回 `name.strip()`；清理后为空抛出 `IllegalArgumentException`。 |
| `private static String normalizeOptionalText(String value)` | `String` | `value == null` 时返回 `""`；否则返回 `value.strip()`；清理后为空字符串允许保存。 |

私有辅助方法仅为实现复用设计，不作为公开 API。测试通过公开构造器、工厂和修改方法覆盖其行为。

**构造方式**：

- 生产和测试代码可直接调用 `new ScheduleItem(...)`。
- 后续 `ScheduleService` 创建日程时优先调用 `ScheduleItem.create(...)`，并由 `IdGenerator` 生成 `EntityId`、由调用方构造 `DateTimeRange`。
- `ScheduleItem` 不接收 `TimeProvider`，不读取 `LocalDateTime.now()`；当前时间必须由服务层或测试显式传入 `statusAt(...)`。

**类型关系**：

- 组合 `assistant.common.EntityId` 和 `assistant.common.DateTimeRange`。
- 依赖 `assistant.schedule.ScheduleStatus`、`java.time.LocalDate` 和 `java.time.LocalDateTime`。
- 被 `ScheduleConflictPolicy` 读取 `getTimeRange()` 进行冲突判断。
- 后续 `ScheduleRepository`、`ScheduleService` 和查询 DTO 可复用该实体；本轮不新增这些类型。

### `ScheduleConflictPolicy`

**形态**：`class`

**包路径**：`assistant.schedule`

**职责**：作为日程冲突判断策略，集中定义“非空时间重叠即冲突，首尾相接不冲突”的规则，避免后续服务层、汇总层或测试中重复散落时间重叠判断。

**类型签名定义**：`public final class ScheduleConflictPolicy`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| 无字段 | 无状态 | 策略实例不保存任何日程集合或时间状态；可在服务层长期复用同一实例。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ScheduleConflictPolicy()` | 构造器 | 创建无状态冲突策略实例。 |
| `public boolean conflicts(ScheduleItem left, ScheduleItem right)` | `boolean` | `left == null` 或 `right == null` 时抛出 `NullPointerException`；返回 `left.getTimeRange().overlaps(right.getTimeRange())` 的结果；不因编号相同而特殊放行。 |
| `public boolean hasConflict(ScheduleItem candidate, Collection<ScheduleItem> existingSchedules)` | `boolean` | `candidate == null` 或 `existingSchedules == null` 时抛出 `NullPointerException`；集合中任一元素为 `null` 时抛出 `NullPointerException`；任一既有日程与候选日程冲突则返回 `true`；空集合返回 `false`。 |
| `public Optional<ScheduleItem> findFirstConflict(ScheduleItem candidate, Collection<ScheduleItem> existingSchedules)` | `Optional<ScheduleItem>` | 参数空值规则同 `hasConflict(...)`；按 `existingSchedules` 迭代顺序返回第一个与候选日程冲突的既有日程；无冲突返回 `Optional.empty()`。 |

**构造方式**：

- 后续 `ScheduleService` 可通过构造器注入 `new ScheduleConflictPolicy()`。
- 单元测试每个测试方法可直接创建新实例，策略无内部状态，测试之间不会共享业务数据。

**类型关系**：

- 依赖 `assistant.schedule.ScheduleItem`。
- 通过 `ScheduleItem.getTimeRange()` 复用 `assistant.common.DateTimeRange.overlaps(...)` 的左闭右开重叠规则。
- 不依赖 `OperationResult`、`BusinessException`、`ErrorCode`、仓储、系统时间、网络或 AI 客户端。
- 后续 `ScheduleService` 在创建或修改日程时可调用 `findFirstConflict(...)` 或 `hasConflict(...)`，并将冲突结果转换为 `OperationResult.failure(ErrorCode.SCHEDULE_CONFLICT, ...)`；本轮策略自身不负责返回错误分类。

## 错误处理

- `ScheduleItem` 对必填引用字段采用 `Objects.requireNonNull(...)` 风格，空编号、空名称、空时间范围和空当前时间均抛出 `NullPointerException`。
- `ScheduleItem` 对空白名称抛出 `IllegalArgumentException`，与既有 `TaskItem` 标题校验风格保持一致。
- `DateTimeRange` 已负责校验开始时间、结束时间和结束晚于开始；`ScheduleItem` 不重复实现日期时间范围校验。
- `ScheduleItem.updateDetails(...)` 必须先计算新名称、新地点、新备注并校验新时间范围，再写入字段，保证任一校验失败时原名称、原时间范围、原地点和原备注全部保持不变。
- `ScheduleConflictPolicy` 对任一空入参均抛出 `NullPointerException`；对集合中的空元素也抛出 `NullPointerException`，避免静默跳过污染数据。
- 本轮领域类型不直接抛出 `BusinessException`，也不直接使用 `ErrorCode.SCHEDULE_CONFLICT`；后续应用服务边界负责把冲突判断结果转换为稳定错误分类。

## 行为契约

- 日程名称使用 `String.strip()` 清理首尾空白后保存，清理后为空必须拒绝；名称内部空白保持原样。
- 地点和备注允许 `null`，并统一保存为 `""`；非空地点和备注使用 `strip()` 清理首尾空白，内部空白保持原样；清理后为空字符串允许保存。
- `ScheduleItem` 的 `id` 一经构造不得改变；`updateDetails(...)` 只能修改名称、时间范围、地点和备注。
- `ScheduleItem` 不保存状态字段；每次调用 `statusAt(...)` 都基于当前 `timeRange` 和传入 `currentDateTime` 动态计算。
- `statusAt(...)` 使用左闭右开边界：当前时间等于开始时间为 `ONGOING`，当前时间等于结束时间为 `EXPIRED`。
- `coversDate(...)` 必须复用 `DateTimeRange.coversDate(...)`，因此跨日期日程能覆盖开始日、中间日和结束日前的有效日期，结束时间正好在某日零点时不覆盖该结束日。
- `ScheduleConflictPolicy.conflicts(...)` 只以 `DateTimeRange.overlaps(...)` 为准：两个时间段存在非空交集返回 `true`；一个日程结束时间等于另一个日程开始时间返回 `false`；两个跨日期日程只要时间范围重叠也返回 `true`。
- `ScheduleConflictPolicy` 不按 `EntityId` 排除候选自身；后续服务层执行修改场景时如需排除同一编号，应在调用策略前过滤既有集合。

## 依赖关系

- 新增生产包：`assistant.schedule`。
- 复用已有通用类型：`assistant.common.EntityId`、`assistant.common.DateTimeRange`。
- 复用 Java 标准库：`java.time.LocalDate`、`java.time.LocalDateTime`、`java.util.Collection`、`java.util.Objects`、`java.util.Optional`。
- 复用测试框架：JUnit Jupiter 5.14.4。
- 不新增 Maven 依赖，不修改 `pom.xml`。
- 不依赖真实当前时间、网络、API Key、外部文件或系统通知。

## 单元测试规格

### `ScheduleStatusTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void exposesFixedStatusValuesInDeclaredOrder()` | `ScheduleStatus.values()` 顺序固定为 `UPCOMING`、`ONGOING`、`EXPIRED`。 |
| `void displayNameReturnsStableChineseText()` | 三个状态分别返回 `"即将开始"`、`"进行中"`、`"已过期"`。 |
| `void upcomingSemanticFlagsMatchOnlyUpcoming()` | `UPCOMING.isUpcoming()` 为 `true`，其他状态为 `false`。 |
| `void ongoingSemanticFlagsMatchOnlyOngoing()` | `ONGOING.isOngoing()` 为 `true`，其他状态为 `false`。 |
| `void expiredSemanticFlagsMatchOnlyExpired()` | `EXPIRED.isExpired()` 为 `true`，其他状态为 `false`。 |
| `void valueOfParsesDeclaredStatusName()` | `ScheduleStatus.valueOf("ONGOING")` 返回 `ONGOING`。 |
| `void valueOfRejectsUnknownStatusName()` | `ScheduleStatus.valueOf("ACTIVE")` 抛出 `IllegalArgumentException`。 |
| `void nameUsesStableEnumConstantName()` | `ScheduleStatus.UPCOMING.name()` 返回 `"UPCOMING"`。 |

### `ScheduleItemTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter 5.14.4

**测试数据约定**：

- 使用 `new EntityId(1)`、`new EntityId(2)` 等固定编号。
- 使用固定 `LocalDateTime`，例如 `2026-06-11T09:00` 到 `2026-06-11T10:00`。
- 使用 `new DateTimeRange(start, end)` 构造时间范围；不依赖真实当前时间。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorStoresProvidedFields()` | 构造器保存编号、名称、时间范围、地点和备注字段。 |
| `void createFactoryCreatesEquivalentScheduleItem()` | `ScheduleItem.create(...)` 与构造器具有相同字段结果。 |
| `void constructorNormalizesNameLocationAndNote()` | 名称、地点、备注清理首尾空白后保存。 |
| `void constructorConvertsNullLocationAndNoteToEmptyString()` | `location == null` 和 `note == null` 保存为 `""`。 |
| `void constructorAllowsBlankLocationAndNoteAsEmptyString()` | 空白地点和备注清理后保存为空字符串。 |
| `void keepsInternalWhitespaceInTextFields()` | 名称、地点、备注内部空白不被折叠。 |
| `void rejectsNullRequiredFields()` | 空编号、空名称、空时间范围分别抛出 `NullPointerException`。 |
| `void rejectsBlankName()` | `""`、普通空白和 Unicode 空白名称抛出 `IllegalArgumentException`。 |
| `void exposesStartAndEndDateTimesFromRange()` | `getStartDateTime()` 和 `getEndDateTime()` 返回 `DateTimeRange` 两端。 |
| `void updateDetailsChangesEditableFieldsOnly()` | 修改名称、时间范围、地点和备注成功，编号保持不变。 |
| `void updateDetailsNormalizesNewTextFields()` | 修改时同样规范化名称、地点和备注。 |
| `void updateDetailsConvertsNullOptionalFieldsToEmptyString()` | 修改时空地点和空备注保存为 `""`。 |
| `void updateDetailsRejectsInvalidNameAndKeepsFieldsUnchanged()` | 新名称为空或空白时抛出异常，原字段全部不变。 |
| `void updateDetailsRejectsNullTimeRangeAndKeepsFieldsUnchanged()` | 新时间范围为空时抛出异常，原字段全部不变。 |
| `void statusAtReturnsUpcomingBeforeStart()` | 当前时间早于开始时间返回 `UPCOMING`。 |
| `void statusAtReturnsOngoingAtStartBoundary()` | 当前时间等于开始时间返回 `ONGOING`。 |
| `void statusAtReturnsOngoingInsideRange()` | 当前时间位于开始和结束之间返回 `ONGOING`。 |
| `void statusAtReturnsExpiredAtEndBoundary()` | 当前时间等于结束时间返回 `EXPIRED`。 |
| `void statusAtReturnsExpiredAfterEnd()` | 当前时间晚于结束时间返回 `EXPIRED`。 |
| `void statusAtRejectsNullCurrentDateTime()` | `statusAt(null)` 抛出 `NullPointerException`。 |
| `void coversDateWhenScheduleStartsOnDate()` | 日程开始所在日期返回 `true`。 |
| `void coversDateWhenScheduleSpansAcrossDate()` | 跨日期日程覆盖中间日期返回 `true`。 |
| `void coversDateExcludesExclusiveEndDateBoundary()` | 结束时间正好为某日零点时不覆盖该结束日。 |
| `void coversDateRejectsNullDate()` | `coversDate(null)` 抛出 `NullPointerException`。 |

### `ScheduleConflictPolicyTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter 5.14.4

**测试数据约定**：

- 使用固定 `EntityId` 和固定 `DateTimeRange` 创建多个 `ScheduleItem`。
- 通过 `List.of(...)` 和空集合覆盖集合扫描语义。
- 不使用真实当前时间、网络、API Key 或外部文件。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void conflictsWhenRangesShareInteriorDateTimes()` | 两个日程存在内部重叠时返回 `true`。 |
| `void conflictsWhenCandidateContainsExistingRange()` | 候选日程完全覆盖既有日程时返回 `true`。 |
| `void conflictsWhenExistingContainsCandidateRange()` | 既有日程完全覆盖候选日程时返回 `true`。 |
| `void conflictsWhenCrossDateRangesOverlap()` | 跨日期日程与单日或另一个跨日期日程存在重叠时返回 `true`。 |
| `void conflictsIsSymmetric()` | `conflicts(left, right)` 与 `conflicts(right, left)` 结果一致。 |
| `void doesNotConflictWhenRangesTouchAtBoundary()` | 一个日程结束时间等于另一个日程开始时间时返回 `false`。 |
| `void doesNotConflictWhenRangesAreSeparated()` | 时间范围完全分离时返回 `false`。 |
| `void doesNotConflictWhenSchedulesAreOnDifferentDatesWithoutOverlap()` | 不同日期且时间范围不重叠时返回 `false`。 |
| `void conflictsRejectsNullArguments()` | `conflicts(null, item)` 和 `conflicts(item, null)` 抛出 `NullPointerException`。 |
| `void hasConflictReturnsTrueWhenAnyExistingScheduleConflicts()` | 既有集合中任一日程冲突时返回 `true`。 |
| `void hasConflictReturnsFalseForEmptyOrNonConflictingCollection()` | 空集合或全部不冲突时返回 `false`。 |
| `void findFirstConflictReturnsFirstConflictByIterationOrder()` | 按输入集合迭代顺序返回第一个冲突日程。 |
| `void findFirstConflictReturnsEmptyWhenNoScheduleConflicts()` | 无冲突时返回 `Optional.empty()`。 |
| `void collectionMethodsRejectNullCandidateOrCollection()` | 候选日程为空或集合为空时抛出 `NullPointerException`。 |
| `void collectionMethodsRejectNullElements()` | 既有集合包含 `null` 元素时抛出 `NullPointerException`。 |
