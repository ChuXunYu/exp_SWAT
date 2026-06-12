# 详细设计（v17）

## 概述

本轮设计目标是在 `assistant.summary` 包中新增跨模块汇总统计能力，形成“今日任务与日程、本周学习计划、本月收支、笔记标签分布、AI 本地上下文”的基础闭环。

本轮实现范围：

- `DashboardSummary`：不可变汇总快照，保存本次汇总的时间边界、只读业务视图、学习计划本周完成/未完成数量、收支统计、笔记数量和标签分布。
- `LocalContext`：不可变 AI 本地上下文快照，保存 `DashboardSummary` 和固定格式的总览文本、任务/日程/学习计划/收支/标签明细行。
- `SummaryService`：只读协作 `TaskService`、`ScheduleService`、`StudyPlanService`、`FinanceService`、`NoteService` 和 `TimeProvider`，即时生成摘要和本地上下文。
- 新增上述类型的 JUnit Jupiter 单元测试，重点覆盖空数据、单模块数据、多模块数据、快照不可变、错误传播和本周学习计划统计口径。

本轮不实现 `assistant.ai`、DeepSeek HTTP 客户端、提示词构造器、控制台首页、草稿导入或文件持久化。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/summary/DashboardSummary.java` | 新建 | 定义跨模块仪表盘汇总结果的不可变 record。 |
| `java-ai-assistant/src/main/java/assistant/summary/LocalContext.java` | 新建 | 定义后续 AI 提示词可直接消费的不可变本地上下文 record。 |
| `java-ai-assistant/src/main/java/assistant/summary/SummaryService.java` | 新建 | 实现跨模块只读查询、日期边界计算、汇总统计和上下文行生成。 |
| `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java` | 新建 | 覆盖摘要记录构造校验、集合复制和不可修改快照。 |
| `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java` | 新建 | 覆盖固定文本/明细行生成、构造校验和列表快照隔离。 |
| `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java` | 新建 | 覆盖服务协作、时间边界、错误传播、统计口径和上下文构建。 |

## 类型定义

### `DashboardSummary`

**形态**：`record`

**包路径**：`assistant.summary`

**职责**：保存一次汇总统计的只读结果快照，避免向调用方暴露可变领域实体或可变集合。

**类型签名定义**：

```java
public record DashboardSummary(
        LocalDate today,
        LocalDate weekStart,
        LocalDate weekEnd,
        LocalDate monthStart,
        LocalDate monthEnd,
        List<TaskView> todayTasks,
        List<ScheduleView> todaySchedules,
        List<StudyPlanView> weekStudyPlans,
        int completedWeekStudyPlanCount,
        int incompleteWeekStudyPlanCount,
        FinanceStatistics monthFinanceStatistics,
        List<TransactionView> monthTransactions,
        int noteCount,
        Map<Tag, Integer> noteTagDistribution)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `LocalDate today` | 非空；来自 `TimeProvider.today()`。 |
| `LocalDate weekStart` | 非空；ISO 周语义下 `today` 所在周周一。 |
| `LocalDate weekEnd` | 非空；ISO 周语义下 `today` 所在周周日。 |
| `LocalDate monthStart` | 非空；`today.withDayOfMonth(1)`。 |
| `LocalDate monthEnd` | 非空；`today.withDayOfMonth(today.lengthOfMonth())`。 |
| `List<TaskView> todayTasks` | 非空；元素非空；构造时复制为不可修改列表。 |
| `List<ScheduleView> todaySchedules` | 非空；元素非空；构造时复制为不可修改列表。 |
| `List<StudyPlanView> weekStudyPlans` | 非空；元素非空；构造时复制为不可修改列表。 |
| `int completedWeekStudyPlanCount` | 不得小于 0；必须等于本次 `weekStudyPlans` 快照中 `status() == StudyPlanStatus.COMPLETED` 的数量。 |
| `int incompleteWeekStudyPlanCount` | 不得小于 0；必须等于本次 `weekStudyPlans` 快照中非 `COMPLETED` 状态的数量。 |
| `FinanceStatistics monthFinanceStatistics` | 非空；来自 `FinanceService.calculateStatistics(monthQuery)`。 |
| `List<TransactionView> monthTransactions` | 非空；元素非空；构造时复制为不可修改列表。 |
| `int noteCount` | 不得小于 0；必须等于本次 `NoteService.listNotes()` 快照大小。 |
| `Map<Tag, Integer> noteTagDistribution` | 非空；key 和 value 非空；value 必须大于 0；构造时按来源迭代顺序复制为 `LinkedHashMap` 后包装为不可修改映射。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DashboardSummary` 规范构造器 | `DashboardSummary` | 校验全部非空字段、数量非负、周/月边界顺序、学习计划统计数量与 `weekStudyPlans` 快照一致、`noteCount >= noteTagDistribution.values().sum()` 不做强制要求但 `noteCount` 不得小于 0；所有列表和映射复制为不可修改快照。 |

**构造方式**：

- 生产路径只由 `SummaryService.getDashboardSummary()` 构造。
- 测试可直接构造，用于验证快照隔离和 `LocalContext.from(...)`。

**类型关系**：

- 依赖 `assistant.task.TaskView`、`assistant.schedule.ScheduleView`、`assistant.study.StudyPlanView`、`assistant.study.StudyPlanStatus`、`assistant.finance.FinanceStatistics`、`assistant.finance.TransactionView`、`assistant.common.Tag`。
- 不依赖任何仓储或可变领域实体。

### `LocalContext`

**形态**：`record`

**包路径**：`assistant.summary`

**职责**：保存供后续 AI 提示词构造使用的确定性本地上下文片段。

**类型签名定义**：

```java
public record LocalContext(
        DashboardSummary dashboardSummary,
        String overviewText,
        List<String> todayTaskLines,
        List<String> todayScheduleLines,
        List<String> weekStudyPlanLines,
        List<String> monthTransactionLines,
        List<String> noteTagLines)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `DashboardSummary dashboardSummary` | 非空；直接保存本次 `getDashboardSummary()` 生成的不可变摘要快照。 |
| `String overviewText` | 非空；执行 `strip()`；规范化后不得为空白；格式固定见行为契约。 |
| `List<String> todayTaskLines` | 非空；元素非空且不得为空白；构造时复制为不可修改列表。 |
| `List<String> todayScheduleLines` | 非空；元素非空且不得为空白；构造时复制为不可修改列表。 |
| `List<String> weekStudyPlanLines` | 非空；元素非空且不得为空白；构造时复制为不可修改列表。 |
| `List<String> monthTransactionLines` | 非空；元素非空且不得为空白；构造时复制为不可修改列表。 |
| `List<String> noteTagLines` | 非空；元素非空且不得为空白；构造时复制为不可修改列表。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public LocalContext` 规范构造器 | `LocalContext` | `dashboardSummary` 为空抛出 `NullPointerException("dashboardSummary")`；`overviewText` 为空抛出 `NullPointerException("overviewText")`，空白抛出 `IllegalArgumentException("overviewText must not be blank")`；任一列表为空引用抛出对应参数名 `NullPointerException`；任一行为空引用抛出 `NullPointerException("line")`，空白抛出 `IllegalArgumentException("line must not be blank")`；所有列表复制为不可修改快照。 |
| `public static LocalContext from(DashboardSummary dashboardSummary)` | `LocalContext` | `dashboardSummary == null` 抛出 `NullPointerException("dashboardSummary")`；按固定格式从摘要快照生成 `overviewText` 和五类明细行。 |

**构造方式**：

- `SummaryService.buildLocalContext()` 调用 `LocalContext.from(summary)`。
- 测试可直接调用构造器验证输入拒绝和快照隔离。

**类型关系**：

- 依赖 `DashboardSummary` 及其内部视图类型的访问器。
- 不依赖任何业务服务、仓储或 AI 客户端。

### `SummaryService`

**形态**：`final class`

**包路径**：`assistant.summary`

**职责**：作为跨模块只读应用服务，即时读取现有业务服务并生成摘要和 AI 本地上下文。

**类型签名定义**：`public final class SummaryService`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final TaskService taskService;` | 构造时非空。 |
| `private final ScheduleService scheduleService;` | 构造时非空。 |
| `private final StudyPlanService studyPlanService;` | 构造时非空。 |
| `private final FinanceService financeService;` | 构造时非空。 |
| `private final NoteService noteService;` | 构造时非空。 |
| `private final TimeProvider timeProvider;` | 构造时非空；所有日期边界只通过该依赖获得当前日期。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public SummaryService(TaskService taskService, ScheduleService scheduleService, StudyPlanService studyPlanService, FinanceService financeService, NoteService noteService, TimeProvider timeProvider)` | `SummaryService` | 任一依赖为空抛出 `NullPointerException`，消息为参数名。 |
| `public OperationResult<DashboardSummary> getDashboardSummary()` | `OperationResult<DashboardSummary>` | 读取 `timeProvider.today()` 一次或等价稳定快照；计算今日、ISO 本周和本月边界；依次调用各业务服务获取视图和统计；任一协作服务返回失败时立即返回同一 `ErrorCode` 的失败结果和稳定消息；全部成功时返回不可变 `DashboardSummary`。 |
| `public OperationResult<LocalContext> buildLocalContext()` | `OperationResult<LocalContext>` | 调用 `getDashboardSummary()`；摘要失败时返回同一 `ErrorCode` 和稳定消息；摘要成功时返回 `LocalContext.from(summary)`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static LocalDate weekStartOf(LocalDate today)` | `LocalDate` | `today == null` 抛出 `NullPointerException("today")`；返回 `today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))`。 |
| `private static LocalDate weekEndOf(LocalDate today)` | `LocalDate` | `today == null` 抛出 `NullPointerException("today")`；返回 `today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))`。 |
| `private static LocalDate monthStartOf(LocalDate today)` | `LocalDate` | `today == null` 抛出 `NullPointerException("today")`；返回 `today.withDayOfMonth(1)`。 |
| `private static LocalDate monthEndOf(LocalDate today)` | `LocalDate` | `today == null` 抛出 `NullPointerException("today")`；返回 `today.withDayOfMonth(today.lengthOfMonth())`。 |
| `private static int countCompletedWeekPlans(List<StudyPlanView> weekStudyPlans)` | `int` | 只统计 `status() == StudyPlanStatus.COMPLETED` 的元素数量；不得调用 `StudyPlanService.countCompletedPlans()`。 |
| `private static int countIncompleteWeekPlans(List<StudyPlanView> weekStudyPlans)` | `int` | 只统计 `status() != StudyPlanStatus.COMPLETED` 的元素数量；不得调用 `StudyPlanService.countIncompletePlans()`。 |
| `private static Map<Tag, Integer> countNoteTags(List<NoteView> notes)` | `Map<Tag, Integer>` | 按 `notes` 顺序遍历，再按每个 `NoteView.tags()` 迭代顺序遍历；使用 `LinkedHashMap<Tag, Integer>` 按标签值语义累加并保留首次出现标签顺序；返回不可修改或仅供 `DashboardSummary` 再复制的稳定映射。 |
| `private static <T> OperationResult<T> propagateFailure(OperationResult<?> failure)` | `OperationResult<T>` | `failure == null` 抛出 `NullPointerException("failure")`；要求 `failure.isFailure()`；返回 `OperationResult.failure(failure.getErrorCode(), stableMessage(failure.getMessage()))`。 |
| `private static String stableMessage(String message)` | `String` | 若 `message == null || message.isBlank()` 返回 `"summary service dependency failed"`；否则返回原消息。 |

**构造方式**：

- 生产装配传入既有业务服务实例和 `SystemTimeProvider`。
- 单元测试可传入 Mockito mock 服务与 `FixedTimeProvider`，或传入真实内存服务组合验证跨模块即时读取。

**类型关系**：

- 依赖 `TaskService`、`ScheduleService`、`StudyPlanService`、`FinanceService`、`NoteService`、`TimeProvider`。
- 依赖查询类型 `TaskQuery`、`StudyPlanQuery`、`TransactionQuery` 和 `DateRange`。
- 不依赖 `TaskRepository`、`ScheduleRepository`、`StudyPlanRepository`、`TransactionRepository`、`NoteRepository` 或任何领域实体。

## 错误处理

- `DashboardSummary` 和 `LocalContext` 是值对象/DTO 边界，空引用使用 `NullPointerException`，空白文本或非法计数使用 `IllegalArgumentException`，与既有 record 风格保持一致。
- `SummaryService` 是应用服务边界，统一返回 `OperationResult`，不对调用方抛出协作服务的业务失败。
- `SummaryService.getDashboardSummary()` 中任一协作服务返回失败时，必须返回同一 `ErrorCode`，消息使用协作服务原消息；若原消息为空或空白，使用 `"summary service dependency failed"`，避免构造失败结果时再次抛异常。
- `SummaryService` 本身构造的查询条件在正常路径不应失败；如 `TimeProvider.today()` 返回空引用，允许通过 `DateRange` 或 `Objects.requireNonNull` 抛出运行时异常，因为这是依赖实现违约而非用户输入错误。
- 空数据属于成功摘要：列表为空、标签分布为空、`FinanceStatistics.zero()` 或由 `FinanceService.calculateStatistics(monthQuery)` 返回的零统计均为正常结果。
- `buildLocalContext()` 不吞掉 `getDashboardSummary()` 的失败，不返回半成品上下文。

## 行为契约

- `SummaryService` 不直接访问任何仓储，不返回任何领域实体，只依赖各模块现有应用服务返回的只读视图。
- `getDashboardSummary()` 使用 `TimeProvider.today()` 作为唯一日期来源，不调用 `LocalDate.now()`、`LocalDateTime.now()`、`System.currentTimeMillis()` 或读取真实环境。
- 今日任务查询必须调用 `taskService.listTasks(TaskQuery.byDueDate(today))`。
- 今日日程查询必须调用 `scheduleService.listSchedulesByDate(today)`。
- 本周学习计划查询必须创建 `DateRange weekRange = new DateRange(weekStart, weekEnd)`，并调用 `studyPlanService.listStudyPlans(StudyPlanQuery.byPeriod(weekRange))`。
- 本周学习计划统计必须只基于本次 `weekStudyPlans` 快照：`completedWeekStudyPlanCount` 统计 `StudyPlanView.status() == StudyPlanStatus.COMPLETED`；`incompleteWeekStudyPlanCount` 统计同一列表中其余状态。`SummaryService` 不得调用 `StudyPlanService.countCompletedPlans()` 或 `StudyPlanService.countIncompletePlans()` 填充本周摘要，也不得重新推导学习计划动态状态。
- 本月收支必须创建 `DateRange monthRange = new DateRange(monthStart, monthEnd)`，并基于同一个 `TransactionQuery monthQuery = TransactionQuery.byDateRange(monthRange)` 调用 `financeService.calculateStatistics(monthQuery)` 和 `financeService.listTransactions(monthQuery)`。
- 笔记数量来自 `noteService.listNotes()` 返回快照的 `size()`。
- 标签分布按 `NoteView.tags()` 中 `Tag` 值语义聚合，使用 `LinkedHashMap` 保留首次出现标签顺序；同一标签多次出现时计数累加。
- `DashboardSummary` 构造后，调用方尝试修改 `todayTasks`、`todaySchedules`、`weekStudyPlans`、`monthTransactions` 或 `noteTagDistribution` 必须抛出 `UnsupportedOperationException`；后续服务数据变化不得改变已返回摘要。
- `LocalContext` 构造后，调用方尝试修改任一明细列表必须抛出 `UnsupportedOperationException`；构造后修改输入列表不得改变上下文。
- `LocalContext.from(summary)` 的 `overviewText` 格式固定为：`今日任务{todayTasks.size()}项，今日日程{todaySchedules.size()}项，本周学习计划{weekStudyPlans.size()}项（已完成{completedWeekStudyPlanCount}项，未完成{incompleteWeekStudyPlanCount}项），本月收入{monthFinanceStatistics.totalIncome().value().toPlainString()}，支出{monthFinanceStatistics.totalExpense().value().toPlainString()}，结余{monthFinanceStatistics.balance().value().toPlainString()}，笔记{noteCount}篇，标签{noteTagDistribution.size()}个。`
- `todayTaskLines` 按 `todayTasks` 当前顺序生成，格式固定为：`任务：{title}｜优先级：{priority.name()}｜状态：{status.name()}｜截止：{dueDate}`；无今日任务时为空列表。
- `todayScheduleLines` 按 `todaySchedules` 当前顺序生成，格式固定为：`日程：{name}｜状态：{status.name()}｜时间：{startDateTime}~{endDateTime}｜地点：{location}`；无今日日程时为空列表。
- `weekStudyPlanLines` 按 `weekStudyPlans` 当前顺序生成，格式固定为：`学习：{goalName}｜状态：{status.name()}｜进度：{progress.value()}%｜周期：{startDate}~{endDate}`；无本周学习计划时为空列表。
- `monthTransactionLines` 按 `monthTransactions` 当前顺序生成，格式固定为：`收支：{type.name()}｜金额：{amount.value().toPlainString()}｜类别：{category}｜日期：{date}`；无本月收支记录时为空列表。
- `noteTagLines` 按 `noteTagDistribution` 的稳定迭代顺序生成，格式固定为：`标签：{tag.displayName()}｜数量：{count}`；无标签时为空列表。
- 上述中文分隔符使用全角冒号 `：` 和竖线 `｜`，范围分隔使用半角 `~`，文本不得额外添加地区化格式、真实时间、随机顺序或占位字符串。

## 依赖关系

- `assistant.summary` 是只读编排包，可依赖 `assistant.task`、`assistant.schedule`、`assistant.study`、`assistant.finance`、`assistant.note` 的应用服务与只读视图。
- `assistant.summary` 不向上述业务包反向暴露依赖；后续 `assistant.ai` 可以依赖 `LocalContext` 或 `SummaryService.buildLocalContext()`。
- 本轮不修改既有业务服务公开签名；若测试需要构造数据，优先使用既有内存仓储和服务，或使用 Mockito mock 服务返回 `OperationResult`。
- `SummaryServiceTest` 必须使用 Mockito `verify(studyPlanService, never()).countCompletedPlans()` 和 `verify(studyPlanService, never()).countIncompletePlans()` 覆盖本周统计口径，且测试数据至少包含一个本周外已完成计划造成全量完成数与本周完成数不同。

## 测试设计

### `DashboardSummaryTest`

| 用例 | 断言重点 |
|------|----------|
| `constructorCopiesListsAndMapAsUnmodifiableSnapshots()` | 构造后修改输入集合不影响摘要；访问器返回集合不可修改；`LinkedHashMap` 标签顺序保持。 |
| `constructorRejectsNullRequiredFieldsAndElements()` | 日期、列表、统计、映射、列表元素、映射 key/value 为空均被拒绝。 |
| `constructorRejectsInvalidDateBoundariesAndCounts()` | 周/月开始晚于结束、完成/未完成数量为负、数量与 `weekStudyPlans` 状态不一致时抛出 `IllegalArgumentException`。 |
| `constructorAllowsEmptySuccessfulSummary()` | 空列表、空标签分布、零笔记和零收支统计可成功构造。 |

### `LocalContextTest`

| 用例 | 断言重点 |
|------|----------|
| `fromBuildsStableOverviewAndEmptyLinesForEmptySummary()` | 空数据时 `overviewText` 精确等于固定格式，各明细列表为空。 |
| `fromBuildsOnlyCorrespondingLinesForSingleModuleData()` | 仅任务、仅日程、仅学习计划、仅收支、仅标签分布存在时，只有对应明细列表有行。 |
| `fromBuildsLinesInSourceOrderForMultiModuleData()` | 多模块数据时各行格式精确匹配任务指令，顺序跟随摘要列表和标签映射迭代顺序。 |
| `constructorCopiesInputListsAsUnmodifiableSnapshots()` | 构造后修改输入列表不影响上下文；访问器列表不可修改。 |
| `constructorRejectsNullsAndBlankOverviewOrLines()` | `dashboardSummary`、`overviewText`、任一列表、任一行为空引用或空白时被拒绝。 |

### `SummaryServiceTest`

| 用例 | 断言重点 |
|------|----------|
| `getDashboardSummaryQueriesServicesWithExpectedDateBoundaries()` | 固定日期为 2026-06-12 时，今日为 2026-06-12，ISO 周为 2026-06-08 到 2026-06-14，本月为 2026-06-01 到 2026-06-30；验证服务调用参数为对应查询对象。 |
| `getDashboardSummaryAggregatesNoteTagsInFirstSeenOrder()` | 多篇笔记和重复标签时，标签计数按值语义累加，顺序为首次出现顺序。 |
| `getDashboardSummaryReturnsSnapshotsUnaffectedByLaterServiceChanges()` | 使用真实内存服务或可变 fake 返回源数据，断言已返回摘要不随后续数据变化。 |
| `getDashboardSummaryPropagatesFirstDependencyFailure()` | 任务、日程、学习计划、收支统计、收支列表、笔记任一失败时返回同一 `ErrorCode` 和消息，并不返回半成品摘要。 |
| `buildLocalContextPropagatesSummaryFailure()` | 摘要失败时上下文失败结果保持同一错误码和消息。 |
| `buildLocalContextReturnsLocalContextFromSuccessfulSummary()` | 成功摘要生成固定 `overviewText` 和各类明细行。 |
| `weekStudyPlanCountsUseOnlyWeekStudyPlansSnapshot()` | 本周计划数量与全量计划数量不同；至少存在一个本周外已完成计划；断言摘要和 `overviewText` 只反映 `weekStudyPlans` 快照；验证 `countCompletedPlans()` 和 `countIncompletePlans()` 从未被调用。 |
| `summaryDoesNotUseRealCurrentDate()` | 使用 `FixedTimeProvider` 断言边界跟随固定日期；无需依赖真实系统日期。 |
