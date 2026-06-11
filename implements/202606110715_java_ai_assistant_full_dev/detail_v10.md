# 详细设计（v10）

## 概述

本轮设计目标是在 `java-ai-assistant` Maven 工程中补齐 `assistant.schedule` 日程提醒模块的应用服务闭环：查询条件 `ScheduleQuery`、只读投影视图 `ScheduleView`、仓储契约 `ScheduleRepository`、内存仓储 `InMemoryScheduleRepository` 和应用服务 `ScheduleService`。

设计沿用 v9 已完成的 `ScheduleItem`、`ScheduleStatus` 和 `ScheduleConflictPolicy`。日程实体继续负责字段不变量和动态状态推导；查询对象只表达筛选条件；仓储只保存内存数据并返回不可修改快照；服务层负责编号生成、当前时间注入、冲突拒绝、错误分类和只读 DTO 投影。生产代码不得读取真实系统时间，状态筛选和状态展示均基于调用方传入的 `LocalDateTime` 或服务注入的 `TimeProvider.now()`。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleQuery.java` | 新建 | 定义日程筛选条件，支持全量、按自然日覆盖、按动态状态以及日期加状态组合筛选。 |
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleView.java` | 新建 | 定义日程只读 DTO/record，按指定当前时间从 `ScheduleItem` 生成状态快照。 |
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleRepository.java` | 新建 | 定义日程仓储契约，提供保存、按编号查找、全量快照、条件筛选和删除。 |
| `java-ai-assistant/src/main/java/assistant/schedule/InMemoryScheduleRepository.java` | 新建 | 基于 `LinkedHashMap<EntityId, ScheduleItem>` 的内存仓储实现，保持插入顺序并返回不可修改快照。 |
| `java-ai-assistant/src/main/java/assistant/schedule/ScheduleService.java` | 新建 | 日程应用服务，提供创建、查看、列表、筛选、按日期查询、修改和删除入口，并统一返回 `OperationResult`。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleQueryTest.java` | 新建 | 覆盖查询工厂、筛选标记、日期覆盖、动态状态、组合筛选和空参数拒绝。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleViewTest.java` | 新建 | 覆盖视图字段投影、状态快照、构造校验、空参数拒绝和实体后续修改不影响既有视图。 |
| `java-ai-assistant/src/test/java/assistant/schedule/InMemoryScheduleRepositoryTest.java` | 新建 | 覆盖保存覆盖、按编号查找、插入顺序、不可修改快照、查询筛选、删除和空参数拒绝。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleServiceTest.java` | 新建 | 覆盖服务创建、查看、列表、筛选、按日期、修改、删除、冲突拒绝、错误分类、只读结果和仓储不变性。 |

## 类型定义

### `ScheduleQuery`

**形态**：`record`

**包路径**：`assistant.schedule`

**职责**：表达日程列表查询条件。日期条件按 `DateTimeRange.coversDate(...)` 语义匹配覆盖该自然日的日程；状态条件按调用方传入的 `currentDateTime` 动态计算，不读取真实系统时间。

**类型签名定义**：`public record ScheduleQuery(LocalDate date, ScheduleStatus status)`

**字段定义**：

| 组件签名 | 约束 |
|----------|------|
| `LocalDate date` | 可为 `null`；`null` 表示不按自然日过滤。 |
| `ScheduleStatus status` | 可为 `null`；`null` 表示不按动态状态过滤。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static ScheduleQuery all()` | `ScheduleQuery` | 返回无筛选条件查询，等价于 `new ScheduleQuery(null, null)`。 |
| `public static ScheduleQuery byDate(LocalDate date)` | `ScheduleQuery` | `date == null` 时抛出 `NullPointerException`；返回仅按自然日覆盖筛选的查询。 |
| `public static ScheduleQuery byStatus(ScheduleStatus status)` | `ScheduleQuery` | `status == null` 时抛出 `NullPointerException`；返回仅按动态状态筛选的查询。 |
| `public static ScheduleQuery of(LocalDate date, ScheduleStatus status)` | `ScheduleQuery` | 返回组合查询；任一参数允许为 `null` 并表示对应维度通配。 |
| `public boolean hasDateFilter()` | `boolean` | 当且仅当 `date != null` 时返回 `true`。 |
| `public boolean hasStatusFilter()` | `boolean` | 当且仅当 `status != null` 时返回 `true`。 |
| `public boolean matches(ScheduleItem item, LocalDateTime currentDateTime)` | `boolean` | `item == null` 或 `currentDateTime == null` 时抛出 `NullPointerException`；日期条件存在时要求 `item.coversDate(date)` 为 `true`；状态条件存在时要求 `item.statusAt(currentDateTime) == status`；多个条件按 AND 组合。 |

**构造方式**：

- 推荐通过 `all()`、`byDate(...)`、`byStatus(...)` 和 `of(...)` 创建查询。
- record 自动提供规范构造器 `public ScheduleQuery(LocalDate date, ScheduleStatus status)`；构造器本身不拒绝 `null`，以支持通配筛选。

**类型关系**：

- 依赖 `java.time.LocalDate`、`java.time.LocalDateTime`、`java.util.Objects`。
- 依赖 `assistant.schedule.ScheduleItem` 和 `assistant.schedule.ScheduleStatus`。
- 被 `ScheduleRepository.findBy(...)`、`InMemoryScheduleRepository.findBy(...)` 和 `ScheduleService.listSchedules(ScheduleQuery query)` 使用。

### `ScheduleView`

**形态**：`record`

**包路径**：`assistant.schedule`

**职责**：作为服务层对外返回的日程只读快照，包含日程基础字段、时间范围端点和基于指定当前时间计算出的动态状态，避免暴露内部可变 `ScheduleItem` 引用。

**类型签名定义**：

`public record ScheduleView(EntityId id, String name, DateTimeRange timeRange, LocalDateTime startDateTime, LocalDateTime endDateTime, String location, String note, ScheduleStatus status)`

**字段定义**：

| 组件签名 | 约束 |
|----------|------|
| `EntityId id` | 必须非空。 |
| `String name` | 必须非空，`strip()` 后不得为空；构造器保存调用方提供的原字符串，不做规范化。 |
| `DateTimeRange timeRange` | 必须非空。 |
| `LocalDateTime startDateTime` | 必须非空，并应等于 `timeRange.startDateTime()`。 |
| `LocalDateTime endDateTime` | 必须非空，并应等于 `timeRange.endDateTime()`。 |
| `String location` | 必须非空；允许为空字符串；构造器保存调用方提供的原字符串。 |
| `String note` | 必须非空；允许为空字符串；构造器保存调用方提供的原字符串。 |
| `ScheduleStatus status` | 必须非空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ScheduleView { ... }` | compact constructor | 对所有必填组件执行非空校验；`name.strip().isEmpty()` 时抛出 `IllegalArgumentException`；`startDateTime` 与 `timeRange.startDateTime()` 不一致或 `endDateTime` 与 `timeRange.endDateTime()` 不一致时抛出 `IllegalArgumentException`。 |
| `public static ScheduleView from(ScheduleItem item, LocalDateTime currentDateTime)` | `ScheduleView` | `item == null` 或 `currentDateTime == null` 时抛出 `NullPointerException`；读取 `item` 当前字段并用 `item.statusAt(currentDateTime)` 计算状态；返回与后续实体修改相互独立的快照。 |
| `public boolean isUpcoming()` | `boolean` | 返回 `status.isUpcoming()`。 |
| `public boolean isOngoing()` | `boolean` | 返回 `status.isOngoing()`。 |
| `public boolean isExpired()` | `boolean` | 返回 `status.isExpired()`。 |

**构造方式**：

- 服务层统一使用 `ScheduleView.from(scheduleItem, currentDateTime)`。
- 测试可直接调用 record 构造器验证只读字段和构造校验。

**类型关系**：

- 组合 `assistant.common.EntityId`、`assistant.common.DateTimeRange`、`java.time.LocalDateTime` 和 `assistant.schedule.ScheduleStatus`。
- 从 `assistant.schedule.ScheduleItem` 投影生成。
- 被 `ScheduleService` 作为所有成功载荷的唯一日程返回类型。

### `ScheduleRepository`

**形态**：`interface`

**包路径**：`assistant.schedule`

**职责**：定义日程实体仓储边界，供服务层保存、查找、筛选和删除日程。

**类型签名定义**：`public interface ScheduleRepository`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `void save(ScheduleItem schedule)` | `void` | 保存或覆盖同一编号日程；`schedule == null` 时具体实现应快速失败。 |
| `Optional<ScheduleItem> findById(EntityId id)` | `Optional<ScheduleItem>` | 按编号查找；`id == null` 时具体实现应快速失败；不存在返回 `Optional.empty()`。 |
| `List<ScheduleItem> findAll()` | `List<ScheduleItem>` | 返回当前全部日程的不可修改快照，顺序由具体实现定义。 |
| `List<ScheduleItem> findBy(ScheduleQuery query, LocalDateTime currentDateTime)` | `List<ScheduleItem>` | `query == null` 或 `currentDateTime == null` 时具体实现应快速失败；按查询条件返回不可修改快照。 |
| `boolean deleteById(EntityId id)` | `boolean` | 删除指定编号日程；`id == null` 时具体实现应快速失败；存在并删除返回 `true`，不存在返回 `false`。 |

**构造方式**：

- 接口不提供构造器。
- 生产和测试默认使用 `InMemoryScheduleRepository`。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.schedule.ScheduleItem`、`assistant.schedule.ScheduleQuery`、`java.time.LocalDateTime`、`java.util.List` 和 `java.util.Optional`。
- 被 `ScheduleService` 注入使用。

### `InMemoryScheduleRepository`

**形态**：`class`

**包路径**：`assistant.schedule`

**职责**：以内存 `LinkedHashMap` 保存日程实体，保持首次插入顺序，提供不可修改列表快照，适合作为普通单元测试和命令行演示默认仓储。

**类型签名定义**：`public final class InMemoryScheduleRepository implements ScheduleRepository`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final Map<EntityId, ScheduleItem> schedules = new LinkedHashMap<>();` | map 引用不可变，内容可变 | key 为日程编号；value 为日程实体引用；同编号保存会覆盖 value，并保留 `LinkedHashMap` 对既有 key 的原插入位置。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public void save(ScheduleItem schedule)` | `void` | `schedule == null` 时抛出 `NullPointerException`；执行 `schedules.put(schedule.getId(), schedule)`。 |
| `public Optional<ScheduleItem> findById(EntityId id)` | `Optional<ScheduleItem>` | `id == null` 时抛出 `NullPointerException`；按 key 返回实体引用或空 Optional。 |
| `public List<ScheduleItem> findAll()` | `List<ScheduleItem>` | 返回 `List.copyOf(schedules.values())` 语义的不可修改快照；保存新日程不会改变已返回列表长度或顺序。 |
| `public List<ScheduleItem> findBy(ScheduleQuery query, LocalDateTime currentDateTime)` | `List<ScheduleItem>` | `query == null` 或 `currentDateTime == null` 时抛出 `NullPointerException`；按 `query.matches(schedule, currentDateTime)` 筛选，结果保持仓储插入顺序并返回不可修改快照。 |
| `public boolean deleteById(EntityId id)` | `boolean` | `id == null` 时抛出 `NullPointerException`；删除成功返回 `true`，不存在返回 `false`。 |

**构造方式**：

- 使用默认无参构造器创建空仓储。
- 不提供线程安全保证；本项目命令行单用户和单元测试场景按单线程使用。

**类型关系**：

- 实现 `ScheduleRepository`。
- 组合 `assistant.common.EntityId` 和 `assistant.schedule.ScheduleItem`。
- 依赖 `java.util.LinkedHashMap`、`java.util.List`、`java.util.Map`、`java.util.Objects`、`java.util.Optional`。

### `ScheduleService`

**形态**：`class`

**包路径**：`assistant.schedule`

**职责**：日程应用服务边界。它协调仓储、编号生成器、时间提供者和冲突策略，向控制台层、汇总层和后续 AI 上下文模块提供稳定 API。所有可预期业务错误均转换为 `OperationResult.failure(...)`。

**类型签名定义**：`public final class ScheduleService`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final ScheduleRepository repository` | 构造后不可变 | 必须非空；负责日程保存和读取。 |
| `private final IdGenerator idGenerator` | 构造后不可变 | 必须非空；创建日程时生成 `EntityId`。 |
| `private final TimeProvider timeProvider` | 构造后不可变 | 必须非空；服务层所有状态展示和状态筛选均通过 `now()` 获取当前时间。 |
| `private final ScheduleConflictPolicy conflictPolicy` | 构造后不可变 | 必须非空；创建和修改时判断时间冲突。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ScheduleService(ScheduleRepository repository, IdGenerator idGenerator, TimeProvider timeProvider, ScheduleConflictPolicy conflictPolicy)` | 构造器 | 任一依赖为 `null` 时抛出 `NullPointerException`。 |
| `public OperationResult<ScheduleView> createSchedule(String name, DateTimeRange timeRange, String location, String note)` | `OperationResult<ScheduleView>` | 创建日程。字段非法时返回 `VALIDATION_ERROR` 且不保存；与既有日程存在非空重叠时返回 `SCHEDULE_CONFLICT` 且不保存；成功时保存实体并返回 `ScheduleView`。 |
| `public OperationResult<ScheduleView> getSchedule(EntityId id)` | `OperationResult<ScheduleView>` | `id == null` 返回 `VALIDATION_ERROR`；不存在返回 `NOT_FOUND`；存在返回按 `timeProvider.now()` 计算状态的 `ScheduleView`。 |
| `public OperationResult<List<ScheduleView>> listSchedules()` | `OperationResult<List<ScheduleView>>` | 返回全部日程的 `ScheduleView` 不可修改快照，顺序与仓储快照一致；所有视图使用同一次 `timeProvider.now()` 结果计算状态。 |
| `public OperationResult<List<ScheduleView>> listSchedules(ScheduleQuery query)` | `OperationResult<List<ScheduleView>>` | `query == null` 返回 `VALIDATION_ERROR`；否则使用同一次 `timeProvider.now()` 调用仓储筛选并投影为不可修改 `ScheduleView` 列表。 |
| `public OperationResult<List<ScheduleView>> listSchedulesByDate(LocalDate date)` | `OperationResult<List<ScheduleView>>` | `date == null` 返回 `VALIDATION_ERROR`；等价于按 `ScheduleQuery.byDate(date)` 筛选；日期匹配必须覆盖跨日期日程，并排除结束时间正好位于查询日零点的无实际覆盖日程。 |
| `public OperationResult<ScheduleView> updateSchedule(EntityId id, String name, DateTimeRange timeRange, String location, String note)` | `OperationResult<ScheduleView>` | `id == null` 返回 `VALIDATION_ERROR`；不存在返回 `NOT_FOUND`；字段非法返回 `VALIDATION_ERROR` 且既有日程不变；与其他日程冲突返回 `SCHEDULE_CONFLICT` 且既有日程不变；成功时修改并保存，返回新视图。 |
| `public OperationResult<Void> deleteSchedule(EntityId id)` | `OperationResult<Void>` | `id == null` 返回 `VALIDATION_ERROR`；不存在返回 `NOT_FOUND`；存在则删除并返回 `OperationResult.success()`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private OperationResult<ScheduleView> validationFailure(String message)` | `OperationResult<ScheduleView>` | 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, nonBlankMessage)`。 |
| `private OperationResult<List<ScheduleView>> validationFailureList(String message)` | `OperationResult<List<ScheduleView>>` | 返回列表载荷场景的校验失败。 |
| `private OperationResult<Void> validationFailureVoid(String message)` | `OperationResult<Void>` | 返回删除场景的校验失败。 |
| `private OperationResult<ScheduleView> notFound(EntityId id)` | `OperationResult<ScheduleView>` | 返回 `ErrorCode.NOT_FOUND`，消息格式为 `"schedule not found: " + id.value()`。 |
| `private OperationResult<Void> notFoundVoid(EntityId id)` | `OperationResult<Void>` | 返回删除场景的不存在失败，消息格式同上。 |
| `private OperationResult<ScheduleView> conflictFailure(ScheduleItem conflict)` | `OperationResult<ScheduleView>` | 返回 `ErrorCode.SCHEDULE_CONFLICT`，消息需非空并包含冲突日程编号。 |
| `private ScheduleView toView(ScheduleItem schedule, LocalDateTime currentDateTime)` | `ScheduleView` | 委托 `ScheduleView.from(schedule, currentDateTime)`。 |
| `private List<ScheduleView> toUnmodifiableViews(List<ScheduleItem> schedules, LocalDateTime currentDateTime)` | `List<ScheduleView>` | 将实体快照投影为 `ScheduleView`；返回 Java 17 `Stream.toList()` 语义的不可修改列表。 |
| `private List<ScheduleItem> schedulesExcept(EntityId id)` | `List<ScheduleItem>` | 从 `repository.findAll()` 中过滤 `!schedule.getId().equals(id)` 的日程；用于修改前排除当前日程自身。 |

**创建流程契约**：

1. 通过 `idGenerator.nextId()` 获取编号，并调用 `ScheduleItem.create(id, name, timeRange, location, note)` 构造候选实体。
2. 捕获候选构造过程中的 `NullPointerException` 和 `IllegalArgumentException`，转换为 `VALIDATION_ERROR`；此时不得调用 `repository.save(...)`。
3. 调用 `conflictPolicy.findFirstConflict(candidate, repository.findAll())` 判断与既有日程是否冲突。
4. 如存在冲突，返回 `SCHEDULE_CONFLICT`，不得保存候选日程。
5. 无冲突时保存候选日程，并使用 `timeProvider.now()` 生成 `ScheduleView` 成功载荷。

**修改流程契约**：

1. `id == null` 直接返回 `VALIDATION_ERROR`。
2. 先通过 `repository.findById(id)` 定位既有日程；不存在返回 `NOT_FOUND`。
3. 使用同一 `id` 构造候选 `ScheduleItem` 来复用字段校验和文本规范化；构造失败返回 `VALIDATION_ERROR`，不得修改既有实体。
4. 从 `repository.findAll()` 中排除 `getId().equals(id)` 的日程后调用 `conflictPolicy.findFirstConflict(...)`，避免修改自身不变时间范围时被误判为冲突。本轮不额外处理仓储污染导致的同编号多实体语义。
5. 如存在其他日程冲突，返回 `SCHEDULE_CONFLICT`，不得调用既有实体的 `updateDetails(...)`。
6. 无冲突时调用既有实体 `updateDetails(candidate.getName(), candidate.getTimeRange(), candidate.getLocation(), candidate.getNote())`，再调用 `repository.save(existing)`，保证对返回分离副本的仓储实现也能持久化修改。

**错误处理**：

- 服务构造器对空依赖抛出 `NullPointerException`，属于装配错误。
- 服务公开业务方法不得向外抛出可预期字段校验异常；`ScheduleItem`、`DateTimeRange` 或 `ScheduleView` 抛出的 `NullPointerException`、`IllegalArgumentException` 在创建和修改入口转换为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。
- `getSchedule(...)`、`updateSchedule(...)` 和 `deleteSchedule(...)` 中空编号返回 `VALIDATION_ERROR`。
- `listSchedules(ScheduleQuery query)` 中空查询返回 `VALIDATION_ERROR`。
- `listSchedulesByDate(LocalDate date)` 中空日期返回 `VALIDATION_ERROR`。
- 不存在日程统一返回 `NOT_FOUND`。
- 创建或修改存在非空时间重叠时统一返回 `SCHEDULE_CONFLICT`。
- 服务成功载荷只能为 `ScheduleView`、不可修改 `List<ScheduleView>` 或 `Void` 成功结果，不返回 `ScheduleItem`。

## 错误处理

- `ScheduleQuery` 单条件工厂对空条件抛出 `NullPointerException`；组合工厂允许 `null` 作为通配；`matches(...)` 对空日程或空当前时间抛出 `NullPointerException`。
- `ScheduleView` 构造器对空字段抛出 `NullPointerException`，对空白名称或不一致时间端点抛出 `IllegalArgumentException`。
- `InMemoryScheduleRepository` 对 `save(null)`、`findById(null)`、`findBy(null, ...)`、`findBy(..., null)` 和 `deleteById(null)` 抛出 `NullPointerException`，符合仓储快速失败原则。
- `ScheduleService` 将用户输入和业务状态导致的失败转换为 `OperationResult`，不要求控制台层捕获领域校验异常。
- `OperationResult.failure(...)` 消息必须非空非空白；服务私有错误方法需保证传入消息满足该约束。

## 行为契约

- 日期筛选必须委托 `ScheduleItem.coversDate(date)`，间接复用 `DateTimeRange.coversDate(date)`。因此跨日期日程覆盖开始日、中间日和结束日前的有效日期；若结束时间正好是某日 `00:00`，则不覆盖该结束日。
- 状态筛选必须委托 `ScheduleItem.statusAt(currentDateTime)`，不读取 `LocalDateTime.now()` 或 `LocalDate.now()`。
- `ScheduleService` 的列表类方法在一次调用内只读取一次 `timeProvider.now()`，并将同一个当前时间用于仓储状态筛选和视图状态投影，避免同次查询内状态不一致。
- `InMemoryScheduleRepository` 返回的是列表快照，不是深拷贝；服务层必须将实体投影为 `ScheduleView` 后再向外返回。
- `ScheduleView.from(...)` 是字段快照；创建视图后再修改原 `ScheduleItem`，既有视图字段和状态不发生变化。
- 创建日程冲突失败时仓储状态保持不变；修改日程字段校验失败或冲突失败时既有日程字段保持不变。
- 首尾相接的两个日程不冲突；存在非空时间重叠才冲突，该规则由 `ScheduleConflictPolicy` 和 `DateTimeRange.overlaps(...)` 承担。
- 修改日程时必须排除当前 `EntityId` 后再判断冲突，使“只改备注”或“不改变时间范围”的更新不会与自身冲突。
- 删除日程后，后续 `getSchedule(id)` 返回 `NOT_FOUND`，后续列表和筛选结果不再包含该编号。

## 依赖关系

- 新增生产包仍为 `assistant.schedule`，不新增 Maven 依赖。
- 复用已有通用类型：`assistant.common.EntityId`、`assistant.common.DateTimeRange`、`assistant.common.OperationResult`、`assistant.common.ErrorCode`。
- 复用已有可测试抽象：`assistant.testability.IdGenerator`、`assistant.testability.TimeProvider`。
- 复用已有日程领域类型：`assistant.schedule.ScheduleItem`、`assistant.schedule.ScheduleStatus`、`assistant.schedule.ScheduleConflictPolicy`。
- 复用 Java 标准库：`java.time.LocalDate`、`java.time.LocalDateTime`、`java.util.LinkedHashMap`、`java.util.List`、`java.util.Map`、`java.util.Objects`、`java.util.Optional`。
- 测试继续使用 JUnit Jupiter；普通单元测试不得访问网络、真实 API Key、真实系统时间或外部文件。

## 单元测试规格

### `ScheduleQueryTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter

**测试数据约定**：

- 使用固定时间 `2026-06-11T09:00`、`2026-06-11T10:00`、`2026-06-11T11:00` 等构造日程。
- 使用 `new EntityId(1)`、`new EntityId(2)` 等固定编号。
- 不调用真实当前时间。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void allQueryMatchesEverySchedule()` | 无条件查询匹配不同日期和不同状态的日程。 |
| `void dateQueryMatchesSchedulesCoveringNaturalDate()` | `byDate(...)` 使用 `coversDate(...)` 匹配覆盖指定自然日的日程。 |
| `void dateQueryMatchesCrossDateScheduleCoveringMiddleDate()` | 跨日期日程覆盖中间自然日。 |
| `void dateQueryExcludesExclusiveMidnightEndBoundary()` | 结束时间正好在查询日零点时不覆盖该日。 |
| `void statusQueryMatchesStatusAtProvidedCurrentDateTime()` | `byStatus(...)` 使用传入 `currentDateTime` 匹配 `UPCOMING`、`ONGOING` 或 `EXPIRED`。 |
| `void combinedQueryRequiresDateAndStatusToMatch()` | 日期和状态组合筛选按 AND 组合。 |
| `void ofAllowsNullComponentsAsWildcards()` | `of(null, status)` 或 `of(date, null)` 将空组件视为通配。 |
| `void exposesFilterPresenceFlags()` | `hasDateFilter()` 和 `hasStatusFilter()` 返回准确。 |
| `void singleCriterionFactoriesRejectNullCriterion()` | `byDate(null)` 和 `byStatus(null)` 抛出 `NullPointerException`。 |
| `void matchesRejectsNullArguments()` | `matches(null, current)` 和 `matches(schedule, null)` 抛出 `NullPointerException`。 |

### `ScheduleViewTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorStoresProvidedSnapshotFields()` | record 构造器保存编号、名称、时间范围、起止时间、地点、备注和状态。 |
| `void constructorPreservesProvidedText()` | 构造器不规范化名称、地点和备注，只校验名称非空白。 |
| `void constructorRejectsNullRequiredFields()` | 任一组件为空抛出 `NullPointerException`。 |
| `void constructorRejectsBlankName()` | 空白名称抛出 `IllegalArgumentException`。 |
| `void constructorRejectsInconsistentTimeEndpoints()` | `startDateTime` 或 `endDateTime` 与 `timeRange` 不一致时抛出 `IllegalArgumentException`。 |
| `void fromCopiesAllScheduleFieldsAndComputesStatus()` | `from(...)` 从实体投影所有字段，并按传入当前时间计算状态。 |
| `void fromComputesUpcomingOngoingAndExpiredSnapshots()` | 使用不同当前时间覆盖三种状态快照。 |
| `void semanticFlagsReflectSnapshotStatus()` | `isUpcoming()`、`isOngoing()`、`isExpired()` 委托快照状态。 |
| `void fromRejectsNullArguments()` | `from(null, current)` 和 `from(item, null)` 抛出 `NullPointerException`。 |
| `void fromCreatesSnapshotIndependentFromLaterScheduleMutation()` | 创建视图后修改实体，既有视图字段、时间范围和状态不变。 |

### `InMemoryScheduleRepositoryTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void saveAndFindByIdReturnsStoredSchedule()` | 保存后可按编号取回同一实体引用。 |
| `void findByIdReturnsEmptyWhenScheduleDoesNotExist()` | 不存在编号返回空 Optional。 |
| `void saveReplacesScheduleWithSameId()` | 同编号保存覆盖旧实体。 |
| `void replacingExistingScheduleKeepsOriginalInsertionPosition()` | 覆盖同编号日程不改变该 key 的原插入位置。 |
| `void findAllReturnsSchedulesInInsertionOrder()` | 全量快照保持插入顺序。 |
| `void findAllReturnsUnmodifiableSnapshotList()` | 全量快照不可修改，后续保存不改变已返回列表。 |
| `void findByFiltersByDate()` | 日期查询通过 `ScheduleQuery.byDate(...)` 筛选。 |
| `void findByFiltersByStatusAtProvidedCurrentTime()` | 状态查询使用传入 `currentDateTime`，不依赖真实时间。 |
| `void findByAppliesCombinedQueryInInsertionOrder()` | 组合查询结果按插入顺序返回。 |
| `void findByReturnsUnmodifiableSnapshotList()` | 条件查询快照不可修改，后续保存不改变已返回列表。 |
| `void deleteByIdRemovesExistingSchedule()` | 删除存在日程返回 `true` 并使查找为空。 |
| `void deleteByIdReturnsFalseWhenScheduleDoesNotExist()` | 删除不存在编号返回 `false`。 |
| `void methodsRejectNullArguments()` | `save`、`findById`、`findBy` 和 `deleteById` 空参数快速失败。 |

### `ScheduleServiceTest`

**包路径**：`assistant.schedule`

**测试框架**：JUnit Jupiter

**测试数据约定**：

- 使用 `InMemoryScheduleRepository`、`IncrementalIdGenerator`、`FixedTimeProvider` 和 `ScheduleConflictPolicy` 组装服务。
- 当前时间固定为可控 `LocalDateTime`，例如 `2026-06-11T09:30`。
- 测试每例重新创建仓储和服务，避免状态串扰。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorRejectsNullDependencies()` | 构造器拒绝空仓储、空编号生成器、空时间提供者和空冲突策略。 |
| `void createScheduleStoresScheduleAndReturnsScheduleView()` | 创建成功保存实体并返回 `ScheduleView`。 |
| `void createScheduleNormalizesEntityTextInReturnedView()` | 通过实体创建规则规范化名称、地点、备注，视图反映规范化结果。 |
| `void createScheduleAllowsNullLocationAndNoteAsEmptyStrings()` | 空地点和备注保存并投影为 `""`。 |
| `void createScheduleReturnsValidationErrorForInvalidFieldsAndDoesNotStore()` | 空名称、空时间范围等非法输入返回 `VALIDATION_ERROR` 且仓储为空。 |
| `void createScheduleRejectsOverlappingScheduleAndKeepsRepositoryUnchanged()` | 与既有日程非空重叠返回 `SCHEDULE_CONFLICT` 且不新增日程。 |
| `void createScheduleAllowsTouchingTimeRanges()` | 首尾相接的两个日程允许保存。 |
| `void getScheduleReturnsViewForExistingSchedule()` | 查看存在日程返回视图。 |
| `void getScheduleReturnsNotFoundForMissingSchedule()` | 查看不存在编号返回 `NOT_FOUND`。 |
| `void getScheduleRejectsNullId()` | 空编号返回 `VALIDATION_ERROR`。 |
| `void listSchedulesReturnsUnmodifiableViewsInInsertionOrder()` | 全量列表返回不可修改 `ScheduleView` 快照，顺序稳定。 |
| `void listSchedulesComputesStatusesWithInjectedTimeProvider()` | 列表状态使用固定 `TimeProvider.now()`。 |
| `void listSchedulesWithQueryFiltersByDateStatusAndCombination()` | 条件筛选覆盖日期、状态和组合条件。 |
| `void listSchedulesRejectsNullQuery()` | 空查询返回 `VALIDATION_ERROR`。 |
| `void listSchedulesByDateReturnsSchedulesCoveringDate()` | 按日期查询返回覆盖该自然日的日程。 |
| `void listSchedulesByDateIncludesCrossDateSchedule()` | 按日期查询包含跨日期覆盖日程。 |
| `void listSchedulesByDateExcludesExclusiveMidnightEndBoundary()` | 按日期查询排除结束在该日零点的日程。 |
| `void listSchedulesByDateRejectsNullDate()` | 空日期返回 `VALIDATION_ERROR`。 |
| `void updateScheduleChangesEditableFieldsAndPersists()` | 修改成功更新名称、时间范围、地点和备注并保存。 |
| `void updateScheduleAllowsNullLocationAndNoteAsEmptyStrings()` | 修改时空地点和备注保存为 `""`。 |
| `void updateScheduleReturnsNotFoundForMissingSchedule()` | 修改不存在编号返回 `NOT_FOUND`。 |
| `void updateScheduleRejectsNullId()` | 修改空编号返回 `VALIDATION_ERROR`。 |
| `void updateScheduleRejectsInvalidFieldsAndKeepsStoredScheduleUnchanged()` | 非法字段返回 `VALIDATION_ERROR` 且原日程字段不变。 |
| `void updateScheduleRejectsOverlappingOtherScheduleAndKeepsStoredScheduleUnchanged()` | 修改后与其他日程重叠返回 `SCHEDULE_CONFLICT` 且原日程字段不变。 |
| `void updateScheduleExcludesCurrentScheduleWhenCheckingConflict()` | 不改变自身时间范围或只改备注不会与自身冲突。 |
| `void updateScheduleAllowsTouchingOtherScheduleTimeRange()` | 修改为首尾相接时间范围允许成功。 |
| `void deleteScheduleRemovesExistingSchedule()` | 删除存在日程成功，后续查看返回 `NOT_FOUND`。 |
| `void deleteScheduleReturnsNotFoundForMissingSchedule()` | 删除不存在编号返回 `NOT_FOUND`。 |
| `void deleteScheduleRejectsNullId()` | 删除空编号返回 `VALIDATION_ERROR`。 |
| `void deleteScheduleRemovesScheduleFromLaterQueries()` | 删除后全量、日期和状态筛选均不再返回该日程。 |
| `void returnedScheduleViewDoesNotChangeWhenStoredScheduleIsUpdatedLater()` | 单个成功视图不暴露可变实体引用。 |
| `void returnedListSnapshotDoesNotChangeWhenStoredSchedulesChangeLater()` | 列表快照不随后续修改和新增变化。 |
| `void returnedListCannotModifyServiceStorage()` | 成功列表不可修改，外部修改尝试不会影响仓储。 |
| `void filteredResultsAreScheduleViewsAndDoNotExposeScheduleItems()` | 条件筛选成功载荷只包含 `ScheduleView`。 |

## 实现注意事项

- 本轮不修改 v9 已完成的 `ScheduleItem`、`ScheduleStatus` 和 `ScheduleConflictPolicy` 公开契约。
- `ScheduleService` 创建和修改时为了复用实体校验可先构造候选 `ScheduleItem`；冲突失败只要求仓储不变，不对编号生成器是否推进作业务契约。
- `Stream.toList()` 在 Java 17 中返回不可修改列表，可用于服务层和内存仓储条件查询快照；全量快照也可使用 `List.copyOf(...)`，两者需保持不可修改语义。
- 单元测试断言错误分类时优先断言 `ErrorCode`，错误消息只需验证非空或包含关键编号，避免与展示文案过度耦合。
