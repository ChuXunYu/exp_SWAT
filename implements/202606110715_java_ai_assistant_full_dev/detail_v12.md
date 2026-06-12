# 详细设计（v12）

## 概述

本轮设计目标是在 `java-ai-assistant` Maven 工程中补齐 `assistant.study` 学习计划模块的查询、只读视图、仓储与应用服务闭环，使学习计划能够通过稳定公开 API 完成创建、查看、列表、组合筛选、修改详情、更新进度、删除，以及已完成/未完成数量统计。

本轮沿用 v11 已完成的 `StudyPlan`、`StudyPlanStatus` 和 `StudyPlanAnalysisService` 作为领域基础，并新增以下约束：

- 动态状态筛选统一通过显式时间上下文完成，`StudyPlanQuery`、`StudyPlanRepository.findBy(...)`、`StudyPlanView.from(...)` 和 `StudyPlanService` 全链路禁止直接读取系统时间。
- 仓储层必须建立只读快照边界，禁止把内部可变 `StudyPlan` 实体引用直接暴露给调用方。
- 服务层公开写接口统一接收原始 `LocalDate startDate`、`LocalDate endDate` 和原始整数进度值，由服务内部构造 `DateRange` 与 `Progress`，并统一映射为 `OperationResult`。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/study/StudyPlanQuery.java` | 新建 | 定义学习计划组合查询条件，支持动态状态筛选与计划周期重叠筛选，并要求调用方显式提供状态分析依赖和当前日期。 |
| `java-ai-assistant/src/main/java/assistant/study/StudyPlanView.java` | 新建 | 定义学习计划只读 DTO/record，对外暴露稳定快照字段和动态状态。 |
| `java-ai-assistant/src/main/java/assistant/study/StudyPlanRepository.java` | 新建 | 定义学习计划仓储契约，包含保存、按编号查询、全量查询、组合筛选与删除。 |
| `java-ai-assistant/src/main/java/assistant/study/InMemoryStudyPlanRepository.java` | 新建 | 基于 `LinkedHashMap<EntityId, StudyPlan>` 的内存仓储实现，负责插入顺序和快照隔离。 |
| `java-ai-assistant/src/main/java/assistant/study/StudyPlanService.java` | 新建 | 定义学习计划应用服务，负责输入校验映射、时间上下文注入、仓储协作和视图投影。 |
| `java-ai-assistant/src/test/java/assistant/study/StudyPlanQueryTest.java` | 新建 | 覆盖查询条件构造、筛选标记、动态状态匹配、周期重叠匹配与空参数快速失败。 |
| `java-ai-assistant/src/test/java/assistant/study/StudyPlanViewTest.java` | 新建 | 覆盖视图构造约束、字段映射和动态状态投影。 |
| `java-ai-assistant/src/test/java/assistant/study/InMemoryStudyPlanRepositoryTest.java` | 新建 | 覆盖仓储插入顺序、组合筛选、快照隔离、集合不可修改和空参数快速失败。 |
| `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` | 新建 | 覆盖服务创建、查看、列表、组合筛选、修改、更新进度、删除、统计、错误映射和失败后仓储不变。 |

## 类型定义

### `StudyPlanQuery`

**形态**：`record`

**包路径**：`assistant.study`

**职责**：表达学习计划只读查询条件。查询条件本身不持有时间源，也不复制动态状态推导逻辑；动态状态筛选必须依赖调用方显式传入的 `StudyPlanAnalysisService` 和 `LocalDate currentDate`。

**类型签名定义**：`public record StudyPlanQuery(StudyPlanStatus status, DateRange period)`

**字段定义**：

| 字段签名 | 可空性 | 语义 |
|----------|--------|------|
| `StudyPlanStatus status` | 可空 | 为空表示不过滤动态状态；非空表示仅匹配 `analysisService.analyzeStatus(plan, currentDate)` 等于该值的计划。 |
| `DateRange period` | 可空 | 为空表示不过滤周期；非空表示仅匹配与该区间存在重叠的计划。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static StudyPlanQuery all()` | `StudyPlanQuery` | 返回无条件查询，`status == null && period == null`。 |
| `public static StudyPlanQuery byStatus(StudyPlanStatus status)` | `StudyPlanQuery` | `status == null` 时抛出 `NullPointerException`。 |
| `public static StudyPlanQuery byPeriod(DateRange period)` | `StudyPlanQuery` | `period == null` 时抛出 `NullPointerException`；语义为“计划周期与该区间重叠”。 |
| `public static StudyPlanQuery of(StudyPlanStatus status, DateRange period)` | `StudyPlanQuery` | 允许任一字段为空，用于组合查询。 |
| `public boolean hasStatusFilter()` | `boolean` | 当且仅当 `status != null` 时返回 `true`。 |
| `public boolean hasPeriodFilter()` | `boolean` | 当且仅当 `period != null` 时返回 `true`。 |
| `public boolean matches(StudyPlan plan, StudyPlanAnalysisService analysisService, LocalDate currentDate)` | `boolean` | `plan`、`analysisService` 或 `currentDate` 为空时抛出 `NullPointerException`；按动态状态和周期重叠两个条件组合筛选。 |

**匹配规则**：

1. 若无状态过滤条件，则状态部分视为匹配。
2. 若有状态过滤条件，则使用 `analysisService.analyzeStatus(plan, currentDate) == status` 判断。
3. 若无周期过滤条件，则周期部分视为匹配。
4. 若有周期过滤条件，则使用 `plan.getPeriod().overlaps(period)` 判断。
5. 最终结果为上述条件的逻辑与。

**构造方式**：

- 与 `TaskQuery`、`ScheduleQuery` 保持一致，优先通过静态工厂创建。
- 不引入额外上下文对象，直接复用显式方法参数，与 `schedule` 模块的时间上下文模式保持一致。

**类型关系**：

- 依赖 `assistant.common.DateRange`、`assistant.study.StudyPlanStatus`、`assistant.study.StudyPlanAnalysisService`。
- 被 `StudyPlanRepository.findBy(...)` 和 `StudyPlanService.listStudyPlans(...)` 复用。

### `StudyPlanView`

**形态**：`record`

**包路径**：`assistant.study`

**职责**：作为学习计划服务的统一只读投影载荷，对外暴露稳定快照字段和动态状态，避免服务层向调用方暴露可变 `StudyPlan` 实体引用。

**类型签名定义**：

`public record StudyPlanView(EntityId id, String goalName, DateRange period, LocalDate startDate, LocalDate endDate, int expectedHours, Progress progress, StudyPlanStatus status)`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `EntityId id` | 非空。 |
| `String goalName` | 非空，`strip()` 后不得为空。 |
| `DateRange period` | 非空。 |
| `LocalDate startDate` | 非空，且必须等于 `period.startDate()`。 |
| `LocalDate endDate` | 非空，且必须等于 `period.endDate()`。 |
| `int expectedHours` | 必须大于 0。 |
| `Progress progress` | 非空。 |
| `StudyPlanStatus status` | 非空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static StudyPlanView from(StudyPlan plan, StudyPlanAnalysisService analysisService, LocalDate currentDate)` | `StudyPlanView` | `plan`、`analysisService` 或 `currentDate` 为空时抛出 `NullPointerException`；使用分析组件和显式日期推导动态状态。 |

**记录紧凑构造器约束**：

- `id`、`goalName`、`period`、`startDate`、`endDate`、`progress`、`status` 必须非空。
- `goalName.strip().isEmpty()` 时抛出 `IllegalArgumentException`。
- `expectedHours <= 0` 时抛出 `IllegalArgumentException`。
- `!startDate.equals(period.startDate())` 时抛出 `IllegalArgumentException("startDate must match period")`。
- `!endDate.equals(period.endDate())` 时抛出 `IllegalArgumentException("endDate must match period")`。

**构造方式**：

- 服务层和测试优先使用 `from(...)` 工厂。
- 工厂内部直接复用 `plan.getId()`、`plan.getGoalName()`、`plan.getPeriod()`、`plan.getStartDate()`、`plan.getEndDate()`、`plan.getExpectedHours()`、`plan.getProgress()` 和 `analysisService.analyzeStatus(plan, currentDate)`。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.DateRange`、`assistant.common.Progress`。
- 被 `StudyPlanService` 作为所有“返回学习计划快照”的成功载荷类型复用。

### `StudyPlanRepository`

**形态**：`interface`

**包路径**：`assistant.study`

**职责**：定义学习计划聚合的持久化契约，同时明确动态状态筛选的时间上下文和快照边界要求。

**类型签名定义**：`public interface StudyPlanRepository`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `void save(StudyPlan plan)` | `void` | `plan == null` 时抛出 `NullPointerException`；保存时必须复制输入实体快照，禁止把调用方传入的 `StudyPlan` 引用原样保存在内部。 |
| `Optional<StudyPlan> findById(EntityId id)` | `Optional<StudyPlan>` | `id == null` 时抛出 `NullPointerException`；若存在则返回与仓储内部状态隔离的实体副本。 |
| `List<StudyPlan> findAll()` | `List<StudyPlan>` | 返回插入顺序的不可修改列表，列表元素均为与仓储内部状态隔离的实体副本。 |
| `List<StudyPlan> findBy(StudyPlanQuery query, StudyPlanAnalysisService analysisService, LocalDate currentDate)` | `List<StudyPlan>` | `query`、`analysisService` 或 `currentDate` 为空时抛出 `NullPointerException`；基于调用方传入的上下文筛选，仓储自身不得读取系统时间或复制状态推导逻辑。 |
| `boolean deleteById(EntityId id)` | `boolean` | `id == null` 时抛出 `NullPointerException`；存在则删除并返回 `true`，否则返回 `false`。 |

**快照边界契约**：

- `save(...)` 后调用方继续修改原始 `StudyPlan`，不得影响仓储内部状态。
- `findById(...)`、`findAll()`、`findBy(...)` 返回的 `StudyPlan` 被调用方执行 `updateDetails(...)` 或 `updateProgress(...)` 后，不得影响仓储内部状态。
- 集合返回值除元素快照外，还必须是结构不可修改快照。

**类型关系**：

- 被 `StudyPlanService` 依赖。
- 由 `InMemoryStudyPlanRepository` 实现。

### `InMemoryStudyPlanRepository`

**形态**：`final class`

**包路径**：`assistant.study`

**职责**：提供学习计划仓储的内存实现，负责插入顺序、按条件筛选和快照隔离。

**类型签名定义**：`public final class InMemoryStudyPlanRepository implements StudyPlanRepository`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final Map<EntityId, StudyPlan> plans = new LinkedHashMap<>();` | 使用 `LinkedHashMap` 保持首次插入顺序；相同 `EntityId` 覆盖时保留原位置。 |

**公开接口行为**：

- `save(...)`：将 `copyOf(plan)` 存入 `plans`，键为 `plan.getId()`。
- `findById(...)`：若命中，返回 `Optional.of(copyOf(storedPlan))`。
- `findAll()`：对 `plans.values()` 逐个 `copyOf(...)` 后返回 `List.copyOf(...)`。
- `findBy(...)`：对 `plans.values()` 依次调用 `query.matches(plan, analysisService, currentDate)`；命中后返回副本列表，保持插入顺序。
- `deleteById(...)`：从 `plans` 中移除对应键。

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static StudyPlan copyOf(StudyPlan source)` | `StudyPlan` | `source == null` 时抛出 `NullPointerException`；通过现有公开构造或工厂按字段重建实体副本。 |

**副本构建策略**：

- 使用 `new StudyPlan(source.getId(), source.getGoalName(), source.getPeriod(), source.getExpectedHours(), source.getProgress())` 或等价工厂重建。
- `EntityId`、`DateRange` 和 `Progress` 均为不可变值对象，可安全复用其引用；真正需要隔离的是可变 `StudyPlan` 实体自身。
- 不修改现有 `StudyPlan` 类对外接口，不新增仓储专用暴露方法。

**类型关系**：

- 依赖 `java.util.LinkedHashMap`、`java.util.List`、`java.util.Optional`、`java.time.LocalDate`。
- 依赖 `StudyPlanQuery` 和 `StudyPlanAnalysisService` 实现动态状态筛选。

### `StudyPlanService`

**形态**：`final class`

**包路径**：`assistant.study`

**职责**：作为学习计划应用服务，负责对外暴露唯一稳定公开 API，统一处理原始输入校验、时间上下文注入、仓储读写、动态状态投影与错误映射。

**类型签名定义**：`public final class StudyPlanService`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final StudyPlanRepository repository;` | 非空。 |
| `private final IdGenerator idGenerator;` | 非空。 |
| `private final TimeProvider timeProvider;` | 非空；仅通过 `today()` 提供当前日期。 |
| `private final StudyPlanAnalysisService analysisService;` | 非空。 |

**构造器**：

| 方法签名 | 契约 |
|----------|------|
| `public StudyPlanService(StudyPlanRepository repository, IdGenerator idGenerator, TimeProvider timeProvider, StudyPlanAnalysisService analysisService)` | 任一依赖为空时抛出 `NullPointerException`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<StudyPlanView> createStudyPlan(String goalName, LocalDate startDate, LocalDate endDate, int expectedHours)` | `OperationResult<StudyPlanView>` | 便捷重载，等价于把初始进度设为 `0`。 |
| `public OperationResult<StudyPlanView> createStudyPlan(String goalName, LocalDate startDate, LocalDate endDate, int expectedHours, int initialProgress)` | `OperationResult<StudyPlanView>` | 由服务内部构造 `DateRange` 和 `Progress`，成功后保存并返回只读视图。 |
| `public OperationResult<StudyPlanView> getStudyPlan(EntityId id)` | `OperationResult<StudyPlanView>` | 查看单个计划；成功返回只读视图。 |
| `public OperationResult<List<StudyPlanView>> listStudyPlans()` | `OperationResult<List<StudyPlanView>>` | 返回全部计划的不可修改视图列表。 |
| `public OperationResult<List<StudyPlanView>> listStudyPlans(StudyPlanQuery query)` | `OperationResult<List<StudyPlanView>>` | 组合查询；成功返回不可修改视图列表。 |
| `public OperationResult<StudyPlanView> updateStudyPlanDetails(EntityId id, String goalName, LocalDate startDate, LocalDate endDate, int expectedHours)` | `OperationResult<StudyPlanView>` | 公开接口固定接收原始日期和原始小时数，由服务内部完成 `DateRange` 构造和校验。 |
| `public OperationResult<StudyPlanView> updateStudyPlanProgress(EntityId id, int progressValue)` | `OperationResult<StudyPlanView>` | 公开接口固定接收原始整数进度值，由服务内部完成 `Progress.zero()` / `Progress.of(...)` 转换。 |
| `public OperationResult<Void> deleteStudyPlan(EntityId id)` | `OperationResult<Void>` | 删除成功返回无载荷成功结果。 |
| `public OperationResult<Integer> countCompletedPlans()` | `OperationResult<Integer>` | 统计动态状态为 `COMPLETED` 的全量计划数量。 |
| `public OperationResult<Integer> countIncompletePlans()` | `OperationResult<Integer>` | 统计动态状态不为 `COMPLETED` 的全量计划数量。 |

**公开接口行为契约**：

1. 创建、查看、列表、组合筛选、修改详情、更新进度成功时，统一返回 `StudyPlanView` 或不可修改的 `List<StudyPlanView>`。
2. 删除固定返回 `OperationResult<Void>`；统计固定返回 `OperationResult<Integer>`。
3. 服务内部不得向调用方暴露 `StudyPlan` 实体引用，也不得要求调用方先构造 `DateRange` 或 `Progress`。
4. 所有动态状态计算统一使用 `timeProvider.today()` 和 `analysisService`，不得在服务中调用 `LocalDate.now()`、`LocalDateTime.now()` 或 `System.currentTimeMillis()`。
5. 对同一公开方法中的筛选和视图投影必须使用同一个 `currentDate`，避免查询结果与返回状态不一致。

**创建与修改详情行为**：

- `createStudyPlan(..., int initialProgress)`：
  1. 调用 `idGenerator.nextId()` 生成编号。
  2. 依次构造 `DateRange(startDate, endDate)` 和 `Progress`。
  3. 使用 `StudyPlan.create(id, goalName, period, expectedHours, progress)` 创建实体。
  4. `repository.save(plan)` 后，使用 `timeProvider.today()` 得到 `currentDate`，返回 `StudyPlanView.from(plan, analysisService, currentDate)`。
- `createStudyPlan(...不带 initialProgress)` 委托给五参数重载，固定传 `0`。
- `updateStudyPlanDetails(...)`：
  1. `id == null` 时直接返回 `VALIDATION_ERROR`。
  2. 从仓储读取快照；不存在返回 `NOT_FOUND`。
  3. 由服务内部构造 `DateRange(startDate, endDate)`。
  4. 对取回的实体副本调用 `updateDetails(...)`。
  5. `repository.save(updatedPlan)` 后返回新视图。

**更新进度行为**：

- `progressValue == 0` 时使用 `Progress.zero()`。
- `progressValue == 100` 时允许使用 `Progress.complete()` 或 `Progress.of(100)`，优先复用 `Progress.complete()`。
- 其他值使用 `Progress.of(progressValue)`。
- 若 `Progress` 构造失败，则映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，并保持仓储状态不变。

**统计行为**：

- `countCompletedPlans()`：
  1. 读取 `LocalDate currentDate = timeProvider.today()`。
  2. 遍历 `repository.findAll()`。
  3. 使用 `analysisService.analyzeStatus(plan, currentDate) == StudyPlanStatus.COMPLETED` 计数。
- `countIncompletePlans()`：
  1. 使用同样的 `currentDate`。
  2. 统计 `analyzeStatus(...) != StudyPlanStatus.COMPLETED` 的计划数量。
  3. 结果包含 `NOT_STARTED`、`IN_PROGRESS`、`OVERDUE_INCOMPLETE`。

**错误映射**：

- `NullPointerException`、`IllegalArgumentException` 统一映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, exception.getMessage())`。
- `getStudyPlan(...)`、`updateStudyPlanDetails(...)`、`updateStudyPlanProgress(...)`、`deleteStudyPlan(...)` 对不存在编号统一返回 `OperationResult.failure(ErrorCode.NOT_FOUND, "study plan not found: " + id.value())`。
- 统计方法无业务异常分支；依赖已在构造阶段保证非空。

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private OperationResult<StudyPlanView> validationFailure(String message)` | `OperationResult<StudyPlanView>` | 生成学习计划视图失败结果。 |
| `private OperationResult<List<StudyPlanView>> validationFailureList(String message)` | `OperationResult<List<StudyPlanView>>` | 生成列表失败结果。 |
| `private OperationResult<Void> validationFailureVoid(String message)` | `OperationResult<Void>` | 生成删除失败结果。 |
| `private OperationResult<Integer> validationFailureCount(String message)` | `OperationResult<Integer>` | 生成统计失败结果；本轮仅保留设计对称性，正常实现中通常不会被调用。 |
| `private OperationResult<StudyPlanView> notFound(EntityId id)` | `OperationResult<StudyPlanView>` | 生成单计划未找到结果。 |
| `private OperationResult<Void> notFoundVoid(EntityId id)` | `OperationResult<Void>` | 生成删除未找到结果。 |
| `private StudyPlanView toView(StudyPlan plan, LocalDate currentDate)` | `StudyPlanView` | 使用 `StudyPlanView.from(...)` 投影。 |
| `private List<StudyPlanView> toUnmodifiableViews(List<StudyPlan> plans, LocalDate currentDate)` | `List<StudyPlanView>` | 将实体快照列表映射为不可修改视图列表。 |
| `private DateRange toDateRange(LocalDate startDate, LocalDate endDate)` | `DateRange` | 统一构造并抛出原始日期相关异常。 |
| `private Progress toProgress(int progressValue)` | `Progress` | 统一处理 `0`、`100` 和其他范围值。 |

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.DateRange`、`assistant.common.Progress`、`assistant.common.OperationResult`、`assistant.common.ErrorCode`。
- 依赖 `assistant.testability.IdGenerator` 和 `assistant.testability.TimeProvider`。
- 依赖 `StudyPlanRepository`、`StudyPlanQuery`、`StudyPlanView` 和 `StudyPlanAnalysisService`。

## 错误处理

- `StudyPlanQuery`、`StudyPlanView`、`StudyPlanRepository` 和 `InMemoryStudyPlanRepository` 遵循现有模块风格，对编程错误快速抛出 `NullPointerException` 或 `IllegalArgumentException`。
- `StudyPlanService` 作为应用层边界，捕获由 `DateRange`、`Progress`、`StudyPlan` 和查询参数触发的 `NullPointerException` / `IllegalArgumentException`，统一映射为 `ErrorCode.VALIDATION_ERROR`。
- 创建、修改详情和更新进度失败时，必须保证仓储状态保持不变；设计上通过“先构造/校验，再修改实体并保存”的顺序保证这一点。
- 仓储读取不存在编号不抛异常，使用 `Optional.empty()` 或 `boolean false` 表达未命中；服务层再映射为 `NOT_FOUND`。

## 行为契约

- 学习计划动态状态不持久化在仓储中；任何状态筛选、视图展示和统计都通过 `StudyPlanAnalysisService` + 显式 `currentDate` 即时计算。
- `StudyPlanQuery.matches(...)`、`StudyPlanRepository.findBy(...)` 和 `StudyPlanView.from(...)` 必须显式接收 `LocalDate currentDate`，禁止内部读取系统时间。
- `StudyPlanRepository` 的所有读取方法都必须返回实体副本，防止仓储内部状态被外部修改穿透。
- `StudyPlanService` 的写接口不公开 `DateRange` 或 `Progress` 参数；原始输入到值对象的转换责任在服务层。
- `StudyPlanService.listStudyPlans(query)` 必须在单次调用内只读取一次 `timeProvider.today()`，并把同一日期同时传给仓储筛选和视图投影，保证动态状态筛选结果与返回视图状态一致。
- `countCompletedPlans()` 和 `countIncompletePlans()` 只做全量统计，不新增统计 DTO，不支持按查询条件或日期范围过滤的统计重载。

## 依赖关系

- 新增生产代码全部位于 `assistant.study` 包，不新增外部 Maven 依赖。
- 复用已有通用类型：`EntityId`、`DateRange`、`Progress`、`OperationResult`、`ErrorCode`、`IdGenerator`、`TimeProvider`。
- 复用 v11 已完成类型：`StudyPlan`、`StudyPlanStatus`、`StudyPlanAnalysisService`。
- 测试继续使用 JUnit Jupiter；所有动态状态相关测试使用固定日期或固定 `TimeProvider`，不访问真实系统时间、网络或外部文件。

## 单元测试规格

### `StudyPlanQueryTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void allCreatesQueryWithoutFilters()` | `all()` 返回双空字段，两个 `has...Filter()` 均为 `false`。 |
| `void byStatusCreatesStatusOnlyFilter()` | `byStatus(...)` 设置状态过滤且不设置周期过滤。 |
| `void byPeriodCreatesPeriodOnlyFilter()` | `byPeriod(...)` 设置周期过滤且不设置状态过滤。 |
| `void ofAllowsCombinedFilters()` | `of(...)` 同时设置状态和周期过滤。 |
| `void matchesReturnsTrueWhenNoFilterIsConfigured()` | 无条件查询对任意合法计划返回 `true`。 |
| `void matchesFiltersByDynamicStatusUsingProvidedDate()` | 在固定 `currentDate` 下验证同一计划可因日期不同而命中/不命中状态过滤。 |
| `void matchesDelegatesStatusAnalysisToAnalysisService()` | 通过自定义分析组件或固定断言验证 query 不复制状态推导逻辑。 |
| `void matchesFiltersByOverlappingPeriod()` | 覆盖重叠、相交边界和不重叠三类周期关系。 |
| `void matchesRequiresBothFiltersWhenCombined()` | 组合查询必须同时满足状态和周期条件。 |
| `void factoryMethodsRejectNullRequiredArguments()` | `byStatus(null)`、`byPeriod(null)` 抛出 `NullPointerException`。 |
| `void matchesRejectsNullArguments()` | `plan`、`analysisService`、`currentDate` 任何为空都抛出 `NullPointerException`。 |

### `StudyPlanViewTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void fromMapsAllFieldsFromPlan()` | 验证 `id`、`goalName`、`period`、`startDate`、`endDate`、`expectedHours`、`progress` 和 `status` 完整投影。 |
| `void fromComputesDynamicStatusWithProvidedCurrentDate()` | 固定两个不同日期，验证视图状态随 `currentDate` 变化。 |
| `void compactConstructorRejectsInvalidFieldValues()` | 覆盖空字段、空白目标、非正小时数、起止日期与 `period` 不一致。 |
| `void fromRejectsNullArguments()` | `plan`、`analysisService`、`currentDate` 为空时抛出 `NullPointerException`。 |

### `InMemoryStudyPlanRepositoryTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试数据约定**：

- 固定周期使用 `2026-06-08` 到 `2026-06-14` 及相邻区间。
- 固定动态状态判断日期使用 `2026-06-10`、`2026-06-15` 等显式 `LocalDate`。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void saveAndFindByIdReturnsDetachedSnapshot()` | `findById(...)` 能取回已保存计划，但返回对象不是调用方原始实例。 |
| `void saveCopiesInputPlanSoLaterCallerMutationsDoNotAffectRepository()` | `save(...)` 后继续修改原始 `StudyPlan`，仓储内部状态不变。 |
| `void findByIdReturnsEmptyWhenPlanDoesNotExist()` | 不存在编号返回空。 |
| `void saveReplacesPlanWithSameIdAndKeepsInsertionOrder()` | 同编号覆盖后仍保留原插入位置。 |
| `void findAllReturnsPlansInInsertionOrder()` | 全量查询保持插入顺序。 |
| `void findAllReturnsUnmodifiableDetachedSnapshotList()` | 列表不可修改，且修改列表中实体不会影响仓储内部状态。 |
| `void findByFiltersByDynamicStatusAtProvidedCurrentDate()` | 在固定日期下按状态筛选，验证命中编号稳定。 |
| `void findByFiltersByOverlappingPeriod()` | 按周期重叠筛选。 |
| `void findByAppliesCombinedQueryInInsertionOrder()` | 组合筛选同时应用状态和周期，并保持插入顺序。 |
| `void findByReturnsUnmodifiableDetachedSnapshotList()` | `findBy(...)` 返回列表不可修改且元素为副本。 |
| `void mutatingPlanReturnedFromFindByIdDoesNotAffectStoredState()` | 修改 `findById(...)` 返回的计划后再次读取，仓储状态不变。 |
| `void mutatingPlanReturnedFromFindAllDoesNotAffectStoredState()` | 修改 `findAll()` 返回元素后再次读取，仓储状态不变。 |
| `void mutatingPlanReturnedFromFindByDoesNotAffectStoredState()` | 修改 `findBy(...)` 返回元素后再次读取，仓储状态不变。 |
| `void deleteByIdRemovesExistingPlan()` | 删除成功。 |
| `void deleteByIdReturnsFalseWhenPlanDoesNotExist()` | 删除不存在编号返回 `false`。 |
| `void methodsRejectNullArguments()` | 覆盖所有空参数快速失败。 |

### `StudyPlanServiceTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试数据约定**：

- 使用 `IncrementalIdGenerator` 生成稳定编号。
- 使用 `FixedTimeProvider` 或等价计数型测试替身固定 `today()`。
- 所有动态状态断言都基于显式固定日期，例如 `2026-06-11`。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorRejectsNullDependencies()` | 任一依赖为空抛出 `NullPointerException`。 |
| `void createStudyPlanWithoutInitialProgressDefaultsToZeroAndReturnsView()` | 便捷重载默认进度为 `0`，返回 `StudyPlanView`。 |
| `void createStudyPlanAcceptsExplicitZeroInitialProgress()` | 显式 `0` 成功。 |
| `void createStudyPlanAcceptsExplicitCompleteInitialProgress()` | 显式 `100` 成功，视图状态为 `COMPLETED`。 |
| `void createStudyPlanRejectsNegativeInitialProgressAndKeepsRepositoryUnchanged()` | `-1` 返回 `VALIDATION_ERROR`，仓储不变。 |
| `void createStudyPlanRejectsProgressGreaterThanHundredAndKeepsRepositoryUnchanged()` | `101` 返回 `VALIDATION_ERROR`，仓储不变。 |
| `void createStudyPlanRejectsInvalidDateRangeAndKeepsRepositoryUnchanged()` | `startDate` 晚于 `endDate` 返回 `VALIDATION_ERROR`，仓储不变。 |
| `void createStudyPlanRejectsNullDatesBlankGoalAndNonPositiveExpectedHours()` | 空日期、空白目标、非正小时数统一映射为 `VALIDATION_ERROR`。 |
| `void getStudyPlanReturnsViewForExistingPlan()` | 查看已存在计划成功。 |
| `void getStudyPlanReturnsNotFoundForMissingPlan()` | 查看不存在编号返回 `NOT_FOUND`。 |
| `void getStudyPlanRejectsNullId()` | 空编号返回 `VALIDATION_ERROR`。 |
| `void listStudyPlansReturnsUnmodifiableViewsInInsertionOrder()` | 列表保持插入顺序、元素为视图、集合不可修改。 |
| `void listStudyPlansComputesStatusesWithInjectedCurrentDate()` | 列表状态通过 `timeProvider.today()` 推导。 |
| `void listStudyPlansWithQueryFiltersByStatusPeriodAndCombination()` | 组合筛选覆盖状态、周期和二者叠加。 |
| `void listStudyPlansWithQueryUsesOneCurrentDateForFilteringAndProjection()` | 验证同一 `today()` 同时用于仓储筛选和视图状态投影。 |
| `void listStudyPlansWithQueryRejectsNullQuery()` | 空 query 返回 `VALIDATION_ERROR`。 |
| `void queryRepositoryAndServiceReturnConsistentResultsForSameCurrentDate()` | 在相同固定日期下，对同一批计划断言 `StudyPlanQuery.matches(...)`、`repository.findBy(...)` 和 `service.listStudyPlans(query)` 的命中编号一致。 |
| `void updateStudyPlanDetailsPersistsChangedFieldsAndReturnsView()` | 修改目标、日期和小时数成功并持久化。 |
| `void updateStudyPlanDetailsRejectsInvalidDateRangeAndKeepsRepositoryState()` | 非法日期范围返回 `VALIDATION_ERROR`，仓储状态不变。 |
| `void updateStudyPlanDetailsRejectsInvalidFieldsAndKeepsRepositoryState()` | 空字段、非正小时数失败后仓储状态不变。 |
| `void updateStudyPlanDetailsReturnsNotFoundForMissingPlan()` | 修改不存在编号返回 `NOT_FOUND`。 |
| `void updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus()` | 更新为 `0` 成功。 |
| `void updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus()` | 更新为 `100` 成功，视图状态刷新为 `COMPLETED`。 |
| `void updateStudyPlanProgressRejectsNegativeProgressAndKeepsRepositoryState()` | `-1` 返回 `VALIDATION_ERROR`，仓储不变。 |
| `void updateStudyPlanProgressRejectsProgressGreaterThanHundredAndKeepsRepositoryState()` | `101` 返回 `VALIDATION_ERROR`，仓储不变。 |
| `void updateStudyPlanProgressReturnsNotFoundForMissingPlan()` | 更新不存在编号返回 `NOT_FOUND`。 |
| `void deleteStudyPlanRemovesExistingPlan()` | 删除成功并返回 `OperationResult<Void>`。 |
| `void deleteStudyPlanReturnsNotFoundForMissingPlan()` | 删除不存在编号返回 `NOT_FOUND`。 |
| `void deleteStudyPlanRejectsNullId()` | 空编号返回 `VALIDATION_ERROR`。 |
| `void countCompletedPlansCountsOnlyCompletedStatusAtCurrentDate()` | 固定日期下只统计 `COMPLETED`。 |
| `void countIncompletePlansCountsAllNonCompletedStatusesAtCurrentDate()` | 固定日期下统计 `NOT_STARTED`、`IN_PROGRESS`、`OVERDUE_INCOMPLETE`。 |
| `void countMethodsUseInjectedCurrentDate()` | 使用计数型 `TimeProvider` 验证每次统计只读取一次 `today()`。 |

## 实现提示

- 本轮不修改 `StudyPlanAnalysisService` 的公开接口；动态状态逻辑继续集中在该类型中。
- 仓储快照隔离采用“保存时复制、读取时再复制”的保守策略，优先保证语义正确，再保持实现简单。
- `StudyPlanService` 与 `ScheduleService`、`TaskService` 保持相同的异常映射风格，但学习计划服务额外承担原始日期和原始整数进度到值对象的转换责任。
