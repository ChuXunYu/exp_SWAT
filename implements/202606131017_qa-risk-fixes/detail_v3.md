# 详细设计（v3）

## 概述
本轮修复摘要页紧急事项覆盖不足。设计范围限定在摘要数据结构、摘要服务过滤规则、AI 本地上下文、AI prompt 出口、控制台汇总展示、对应测试和受影响文档。

目标行为：
- 保留现有今日任务、今日日程、本周学习计划、本月收支、笔记标签摘要能力。
- 新增“逾期未完成任务”和“未来7天高优先级任务”两个紧急任务视图。
- AI 本地上下文不仅包含两个视图的计数，还包含可被 `PromptBuilder` 写入 AI 请求的任务明细。
- 控制台汇总页展示两个新增栏目计数，并按现有简洁 CLI 风格展示任务标题。

本轮不修改 `TaskQuery` 结构，不引入日期范围查询；`SummaryService` 通过 `taskService.listTasks(TaskQuery.all())` 获取任务快照后在内存中过滤两个新增视图。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/summary/DashboardSummary.java` | 修改 | 新增逾期未完成任务和未来7天高优先级任务列表字段，并保持空值、元素空值和不可变快照校验 |
| `java-ai-assistant/src/main/java/assistant/summary/SummaryService.java` | 修改 | 查询全部任务并按固定规则生成新增紧急任务视图，保持依赖失败传播策略 |
| `java-ai-assistant/src/main/java/assistant/summary/LocalContext.java` | 修改 | 新增两个任务行列表，overview 文本加入新增计数，并从摘要生成新增任务明细 |
| `java-ai-assistant/src/main/java/assistant/ai/PromptBuilder.java` | 修改 | 在 AI user message 中输出“逾期未完成任务”和“未来7天高优先级任务”段落 |
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 汇总页展示新增紧急任务计数和任务标题列表 |
| `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java` | 修改 | 覆盖新增过滤规则、查询顺序、失败传播和本地上下文输出 |
| `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java` | 修改 | 覆盖新增列表的构造校验、不可变快照和空摘要行为 |
| `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java` | 修改 | 覆盖 overview 新计数、新增任务行、空列表、构造校验和不可变快照 |
| `java-ai-assistant/src/test/java/assistant/ai/PromptBuilderTest.java` | 修改 | 必须覆盖新增 prompt 段落的非空和空列表输出 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 覆盖汇总输出包含逾期未完成任务和未来7天高优先级任务的计数及标题 |
| `java-ai-assistant/src/test/java/assistant/ai/AiAssistantServiceTest.java` | 必要时修改 | 如直接构造 `DashboardSummary` 或 `LocalContext`，同步新增构造参数 |
| `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionDraftServiceTest.java` | 必要时修改 | 如直接构造 `DashboardSummary` 或 `LocalContext`，同步新增构造参数 |
| `java-ai-assistant/docs/test-plan.md` | 修改 | 更新汇总统计和 AI 上下文测试重点 |
| `java-ai-assistant/docs/test-cases.md` | 修改 | 新增或更新 SUMMARY 用例，引用真实存在的新增测试方法 |
| `java-ai-assistant/docs/coverage/README.md` | 修改 | 更新 `SummaryService.getDashboardSummary()` 控制流、独立路径和圈复杂度说明 |
| `java-ai-assistant/docs/defect-regression.md` | 可选修改 | 如记录本轮风险修复，增加摘要紧急事项回归条目 |
| `java-ai-assistant/README.md` | 可选修改 | 如 README 的 summary/AI context 简述需要同步，可补充紧急任务摘要说明 |

## 类型定义

### DashboardSummary
**形态**：`record`
**包路径**：`assistant.summary`
**职责**：承载仪表盘摘要的不可变快照。

**记录签名调整为**：
```java
public record DashboardSummary(
        LocalDate today,
        LocalDate weekStart,
        LocalDate weekEnd,
        LocalDate monthStart,
        LocalDate monthEnd,
        List<TaskView> todayTasks,
        List<TaskView> overdueTasks,
        List<TaskView> upcomingHighPriorityTasks,
        List<ScheduleView> todaySchedules,
        List<StudyPlanView> weekStudyPlans,
        int completedWeekStudyPlanCount,
        int incompleteWeekStudyPlanCount,
        FinanceStatistics monthFinanceStatistics,
        List<TransactionView> monthTransactions,
        int noteCount,
        Map<Tag, Integer> noteTagDistribution)
```

**公开接口**：
- record accessor 全部公开，包括：
  - `public List<TaskView> overdueTasks()`
  - `public List<TaskView> upcomingHighPriorityTasks()`

**构造方式**：
- 继续使用 canonical constructor。
- `todayTasks`、`overdueTasks`、`upcomingHighPriorityTasks`、`todaySchedules`、`weekStudyPlans`、`monthTransactions` 都通过现有 `copyList(List<T> values, String name)` 复制。
- 新增字段校验消息名分别为 `"overdueTasks"` 和 `"upcomingHighPriorityTasks"`；元素空值仍由 `copyList` 抛出 `NullPointerException("element")`。

**类型关系**：
- 继续组合 `TaskView`、`ScheduleView`、`StudyPlanView`、`TransactionView`、`FinanceStatistics`、`Tag`。
- 不新增继承或接口实现。

### SummaryService
**形态**：`final class`
**包路径**：`assistant.summary`
**职责**：聚合各领域服务输出，生成摘要和 AI 本地上下文。

**公开接口保持不变**：
```java
public OperationResult<DashboardSummary> getDashboardSummary()

public OperationResult<LocalContext> buildLocalContext()
```

**新增私有方法签名**：
```java
private static List<TaskView> filterTodayTasks(List<TaskView> tasks, LocalDate today)

private static List<TaskView> filterOverdueTasks(List<TaskView> tasks, LocalDate today)

private static List<TaskView> filterUpcomingHighPriorityTasks(List<TaskView> tasks, LocalDate today)

private static boolean isIncomplete(TaskView task)
```

**修改的依赖调用**：
```java
OperationResult<List<TaskView>> tasksResult = taskService.listTasks(TaskQuery.all());
```

**过滤定义**：
- `filterTodayTasks`：`task.dueDate().equals(today)`；不按状态或优先级过滤，保持原“今日任务”语义。
- `filterOverdueTasks`：`task.dueDate().isBefore(today) && isIncomplete(task)`。
- `filterUpcomingHighPriorityTasks`：`!task.dueDate().isBefore(today) && !task.dueDate().isAfter(today.plusDays(7)) && task.priority() == TaskPriority.HIGH && isIncomplete(task)`。
- `isIncomplete`：`task.status() != TaskStatus.COMPLETED`，不要依赖标题、描述或其它字段。
- 三个过滤方法均保持输入列表顺序。

**类型关系**：
- 新增 import：`assistant.task.TaskPriority`、`assistant.task.TaskStatus`。
- 继续组合 `TaskService` 等已有服务。

### LocalContext
**形态**：`record`
**包路径**：`assistant.summary`
**职责**：将摘要转换为 AI 可消费的本地上下文文本和明细行。

**记录签名调整为**：
```java
public record LocalContext(
        DashboardSummary dashboardSummary,
        String overviewText,
        List<String> todayTaskLines,
        List<String> overdueTaskLines,
        List<String> upcomingHighPriorityTaskLines,
        List<String> todayScheduleLines,
        List<String> weekStudyPlanLines,
        List<String> monthTransactionLines,
        List<String> noteTagLines)
```

**公开接口**：
```java
public static LocalContext from(DashboardSummary dashboardSummary)

public List<String> overdueTaskLines()

public List<String> upcomingHighPriorityTaskLines()
```

**构造方式**：
- 继续通过 canonical constructor 和 `LocalContext.from(...)` 构造。
- 新增两个列表字段调用现有 `copyLines(...)`，空列表允许，null 列表拒绝，null 或 blank 行拒绝。

**私有方法保持或复用**：
```java
private static String buildOverviewText(DashboardSummary summary)

private static String taskLine(TaskView task)
```

**overview 文本调整**：
- 在“今日任务{n}项”之后加入：
  - `，逾期未完成任务{summary.overdueTasks().size()}项`
  - `，未来7天高优先级任务{summary.upcomingHighPriorityTasks().size()}项`
- 完整文本模式为：
  - `今日任务{n}项，逾期未完成任务{x}项，未来7天高优先级任务{y}项，今日日程{n}项，本周学习计划...`

**类型关系**：
- 从 `DashboardSummary.overdueTasks()` 和 `DashboardSummary.upcomingHighPriorityTasks()` 生成新增任务行。
- 新增任务行复用 `taskLine(TaskView task)`，格式与今日任务完全一致。

### PromptBuilder
**形态**：`final class`
**包路径**：`assistant.ai`
**职责**：构建 AI 请求，将用户问题和本地上下文写入 prompt。

**公开接口保持不变**：
```java
public OperationResult<AiRequest> build(
        AiScenario scenario,
        String userQuestion,
        AiConfiguration configuration,
        LocalContext localContext)
```

**修改的私有方法签名保持不变**：
```java
private static String buildUserMessage(String userQuestion, LocalContext localContext)
```

**buildUserMessage 输出顺序调整为**：
1. 用户问题。
2. 本地总览。
3. `section("今日任务：", localContext.todayTaskLines())`。
4. `section("逾期未完成任务：", localContext.overdueTaskLines())`。
5. `section("未来7天高优先级任务：", localContext.upcomingHighPriorityTaskLines())`。
6. 今日日程。
7. 本周学习计划。
8. 本月收支。
9. 笔记标签。

**空列表语义**：
- 复用现有 `section(...)`，输出：
  - `逾期未完成任务：\n（无）`
  - `未来7天高优先级任务：\n（无）`

**类型关系**：
- 消费 `LocalContext.overdueTaskLines()` 和 `LocalContext.upcomingHighPriorityTaskLines()`。
- 不修改 AI scenario、配置、client 或结构化 JSON 系统指令。

### ConsoleApplication
**形态**：`final class`
**包路径**：`assistant.app`
**职责**：控制台交互入口。

**修改方法**：
```java
private void showSummary()
```

**新增私有方法签名**：
```java
private void printTaskTitles(String heading, List<TaskView> tasks)
```

**showSummary 输出调整**：
- 在 `今日任务数: ...` 后新增：
  - `逾期未完成任务数: {summary.overdueTasks().size()}`
  - `未来7天高优先级任务数: {summary.upcomingHighPriorityTasks().size()}`
- 随后调用：
  - `printTaskTitles("逾期未完成任务", summary.overdueTasks())`
  - `printTaskTitles("未来7天高优先级任务", summary.upcomingHighPriorityTasks())`
- 其它现有行保持不变。

**printTaskTitles 行为**：
- 当 `tasks.isEmpty()` 时不输出明细行，避免空摘要变冗长。
- 非空时先输出 `{heading}:`，再逐项输出 `- {task.title()} | 截止 {task.dueDate()} | {task.priority()} | {task.status()}`。
- 保持任务列表源顺序。

**类型关系**：
- 新增 import：`assistant.task.TaskView`、`java.util.List`，如文件已有则复用。

## 错误处理
- `SummaryService.getDashboardSummary()` 对任务服务的失败传播仍使用 `propagateFailure(...)`。
- 由于任务查询从 `TaskQuery.byDueDate(today)` 改为 `TaskQuery.all()`，只发生一次任务查询；该查询失败时必须在调用日程、学习、收支、笔记服务前立即返回失败。
- `propagateFailure(...)` 和 `stableMessage(...)` 行为不变：保留原错误码，空白消息映射为 `"summary service dependency failed"`。
- 新增过滤方法不吞异常；`TaskView` 构造已保证 `dueDate`、`priority`、`status` 非空。
- `DashboardSummary` 和 `LocalContext` 新增字段沿用已有 null/blank 校验风格，不新增自定义错误类型。
- `PromptBuilder` 的空问题校验、null 依赖校验和 `OperationResult` 错误语义不变。
- `ConsoleApplication.showSummary()` 仍通过 `printResult(result)` 处理摘要失败，失败时不输出摘要详情。

## 行为契约
- `SummaryService.getDashboardSummary()` 调用顺序：
  1. 获取一次 `today = timeProvider.today()`。
  2. 计算 `weekStart`、`weekEnd`、`monthStart`、`monthEnd`。
  3. 调用 `taskService.listTasks(TaskQuery.all())`。
  4. 任务查询失败则立即传播失败。
  5. 从同一个任务快照生成 `todayTasks`、`overdueTasks`、`upcomingHighPriorityTasks`。
  6. 继续按现有顺序查询今日日程、本周学习计划、本月收支统计、本月交易、笔记。
  7. 组装 `DashboardSummary` 时传入三个任务列表。
- 日期窗口契约：
  - 逾期未完成任务：`dueDate < today && status != COMPLETED`。
  - 未来7天高优先级任务：`today <= dueDate <= today.plusDays(7) && priority == HIGH && status != COMPLETED`。
  - 未来7天窗口包含今天。
  - 未来7天窗口不包含 `dueDate < today` 的任务。
  - 同一任务可以同时出现在 `todayTasks` 和 `upcomingHighPriorityTasks`。
  - 已完成任务不出现在 `overdueTasks` 或 `upcomingHighPriorityTasks`。
  - 低/中优先级未来任务不出现在 `upcomingHighPriorityTasks`。
- 列表顺序契约：
  - 新增两个紧急任务列表保持 `TaskQuery.all()` 返回顺序，不额外排序。
  - `LocalContext` 新增行保持对应摘要列表顺序。
  - `PromptBuilder` 和 `ConsoleApplication` 展示保持对应 `LocalContext` 或 `DashboardSummary` 顺序。
- `DashboardSummary` 快照契约：
  - 构造后外部原列表变化不影响摘要。
  - 返回的新增列表不可修改。
- `LocalContext` 快照契约：
  - 构造后外部原行列表变化不影响上下文。
  - 返回的新增行列表不可修改。
- `PromptBuilder` 契约：
  - AI user message 必须包含两个新增段落标题和明细行。
  - 空列表必须显示 `（无）`，不能省略段落。
- `ConsoleApplication` 契约：
  - 汇总页必须总是显示新增两个计数行。
  - 非空新增列表显示任务标题；空列表可只显示计数。

## 依赖关系
- 依赖已有类型：
  - `assistant.ai.PromptBuilder`
  - `assistant.common.DateRange`
  - `assistant.common.OperationResult`
  - `assistant.summary.DashboardSummary`
  - `assistant.summary.LocalContext`
  - `assistant.task.TaskPriority`
  - `assistant.task.TaskQuery`
  - `assistant.task.TaskService`
  - `assistant.task.TaskStatus`
  - `assistant.task.TaskView`
  - `assistant.testability.TimeProvider`
- 暴露给后续任务的公开接口：
  - `DashboardSummary.overdueTasks()`
  - `DashboardSummary.upcomingHighPriorityTasks()`
  - `LocalContext.overdueTaskLines()`
  - `LocalContext.upcomingHighPriorityTaskLines()`
- 不修改：
  - `TaskQuery` 字段和匹配语义。
  - `TaskService` 公开接口。
  - `TaskView`、`TaskStatus`、`TaskPriority`。
  - AI client、AI scenario、草稿生成、草稿导入和学习计划 breakdown 逻辑。

## 测试设计

### SummaryServiceTest
**修改测试类**：`assistant.summary.SummaryServiceTest`

**helper 调整**：
- `collaboratorsWithSuccess()` 改为 mock：
  - `when(taskService.listTasks(TaskQuery.all()))`
  - 不再 mock `TaskQuery.byDueDate(TODAY)` 作为摘要入口。
- `task(long id, String title)` 可保留为今日 HIGH TODO helper。
- 新增 helper：
```java
private static TaskView task(long id, String title, TaskPriority priority, LocalDate dueDate, TaskStatus status)
```

**既有测试调整**：
- `getDashboardSummaryQueriesServicesWithExpectedDateBoundaries`：
  - verify `taskService.listTasks(TaskQuery.all())`。
  - 不再 verify `TaskQuery.byDueDate(TODAY)`。
- `getDashboardSummaryReturnsSnapshotsUnaffectedByLaterServiceChanges`：
  - mock `TaskQuery.all()`。
  - 可断言 `todayTasks`、`overdueTasks`、`upcomingHighPriorityTasks` 都不受后续列表变化影响。
- `getDashboardSummaryPropagatesFirstDependencyFailure`、`getDashboardSummaryUsesStableFallbackWhenDependencyFailureMessageIsBlank`、`buildLocalContextPropagatesSummaryFailure`：
  - 任务失败 override 改为 `TaskQuery.all()`。
- `buildLocalContextReturnsLocalContextFromSuccessfulSummary`：
  - overview 期望值加入 `逾期未完成任务0项，未来7天高优先级任务1项`。
  - 断言 `upcomingHighPriorityTaskLines()` 包含今日 HIGH TODO 任务。
  - 断言 `overdueTaskLines()` 为空。

**新增用例**：
- `getDashboardSummaryBuildsOverdueIncompleteTasks`：
  - 任务快照包含昨天 TODO、昨天 IN_PROGRESS、昨天 COMPLETED、今天 TODO。
  - 断言 `overdueTasks()` 只包含昨天 TODO 和昨天 IN_PROGRESS，排除 COMPLETED 与今天任务。
- `getDashboardSummaryBuildsUpcomingHighPriorityIncompleteTasksWithinInclusiveWindow`：
  - 任务快照包含今天 HIGH TODO、`today.plusDays(7)` HIGH IN_PROGRESS、`today.plusDays(8)` HIGH TODO、明天 MEDIUM TODO、昨天 HIGH TODO、明天 HIGH COMPLETED。
  - 断言 `upcomingHighPriorityTasks()` 只包含今天 HIGH TODO 和第7天 HIGH IN_PROGRESS。
- `getDashboardSummaryAllowsTodayHighPriorityTaskInTodayAndUpcomingViews`：
  - 单个今天 HIGH TODO。
  - 断言同一 id 同时出现在 `todayTasks()` 和 `upcomingHighPriorityTasks()`。
- `getDashboardSummaryUsesTaskAllFailureAsFirstDependencyFailure`：
  - `TaskQuery.all()` 返回失败。
  - 断言不调用 schedule、study、finance、note 依赖。
- `buildLocalContextIncludesUrgentTaskCountsAndLines`：
  - 摘要包含一个逾期任务和一个未来7天 HIGH 任务。
  - 断言 overview 和两个新增行列表都包含对应标题、优先级、状态、截止日期。

### DashboardSummaryTest
**修改测试类**：`assistant.summary.DashboardSummaryTest`

**构造调用调整**：
- 所有 `new DashboardSummary(...)` 在 `todayTasks` 后插入：
  - `List<TaskView> overdueTasks`
  - `List<TaskView> upcomingHighPriorityTasks`
- `emptySummary()` 传入两个 `List.of()`。
- `summaryWith(...)` helper 增加两个列表参数，或提供默认空列表重载减少重复。

**新增或调整断言**：
- `constructorCopiesListsAndMapAsUnmodifiableSnapshots`：
  - 准备 `overdueTasks` 和 `upcomingHighPriorityTasks` mutable lists。
  - 构造后清空原列表。
  - 断言 summary 中两个新增列表仍有原元素。
  - 断言两个新增列表不可修改。
- `constructorRejectsNullRequiredFieldsAndElements`：
  - 增加 null `overdueTasks`、null `upcomingHighPriorityTasks`。
  - 增加新增列表内 null 元素场景。
- `emptySummaryReturnsEmptyCollectionsAndZeroCounts`：
  - 断言 `overdueTasks().isEmpty()` 和 `upcomingHighPriorityTasks().isEmpty()`。

### LocalContextTest
**修改测试类**：`assistant.summary.LocalContextTest`

**构造调用调整**：
- 直接 `new LocalContext(...)` 的位置在 `todayTaskLines` 后插入 `overdueTaskLines`、`upcomingHighPriorityTaskLines`。
- `summaryWith(...)` helper 增加 `overdueTasks` 和 `upcomingHighPriorityTasks` 参数，或提供默认空列表重载。

**既有测试调整**：
- `fromBuildsStableOverviewAndEmptyLinesForEmptySummary`：
  - overview 期望值加入 `逾期未完成任务0项，未来7天高优先级任务0项`。
  - 断言两个新增行列表为空。
- `fromBuildsOnlyCorrespondingLinesForSingleModuleData`：
  - 增加两个 assert：
    - 只传 overdue tasks 时 `overdueTaskLines()` 生成任务行。
    - 只传 upcoming tasks 时 `upcomingHighPriorityTaskLines()` 生成任务行。
- `fromBuildsLinesInSourceOrderForMultiModuleData`：
  - 传入两个逾期任务和两个未来高优先级任务。
  - 断言新增行列表保持源顺序。
- `constructorCopiesInputListsAsUnmodifiableSnapshots`：
  - 增加两个 mutable line lists，对应快照与不可修改断言。
- `constructorRejectsNullsAndBlankOverviewOrLines`：
  - 增加新增列表 null、列表元素 null、blank 行场景。

### PromptBuilderTest
**修改测试类**：`assistant.ai.PromptBuilderTest`

**helper 调整**：
- `summaryWith(...)` 增加 overdue/upcoming 两个任务列表参数，或提供默认空列表重载。
- `populatedContext()` 必须包含：
  - 今日任务：`Review`
  - 逾期未完成任务：例如 `Overdue`
  - 未来7天高优先级任务：例如 `Upcoming`

**必须调整用例**：
- `buildIncludesOverviewAndAllContextSections`：
  - 断言 `userMessage` 包含 `逾期未完成任务：\n- 任务：Overdue`。
  - 断言 `userMessage` 包含 `未来7天高优先级任务：\n- 任务：Upcoming`。
  - 保留已有今日任务、日程、学习、收支、标签断言。
- `buildUsesStableEmptyMarkerForEmptyDetailLists`：
  - 断言包含：
    - `逾期未完成任务：\n（无）`
    - `未来7天高优先级任务：\n（无）`

### ConsoleApplicationTest
**修改测试类**：`assistant.app.ConsoleApplicationTest`

**既有测试调整**：
- `summaryCommandDisplaysDashboardSummary`：
  - 在 demo data 或专门服务数据下断言输出包含：
    - `逾期未完成任务数:`
    - `未来7天高优先级任务数:`

**新增用例**：
- `summaryCommandDisplaysUrgentTaskCountsAndTitles`：
  - 使用固定日期服务，先创建：
    - 逾期 HIGH/TODO 任务，标题 `过期报告`，dueDate 为 today minus 1。
    - 未来7天 HIGH/TODO 任务，标题 `准备答辩`，dueDate 为 today plus 7。
    - 可选创建一个未来 MEDIUM/TODO 任务，标题 `普通事项`，验证不会出现在未来高优先级明细。
  - 输入 `1\nq\n`。
  - 断言输出包含：
    - `逾期未完成任务数: 1`
    - `未来7天高优先级任务数: 1`
    - `过期报告`
    - `准备答辩`
  - 断言如果测试框架已有 `assertNotContains`，则 `普通事项` 不在紧急明细；若没有，不强行新增全局 helper，可通过服务层测试覆盖排除规则。

### 其它 AI 测试
- `AiAssistantServiceTest`、`StructuredSuggestionDraftServiceTest` 中若存在直接构造 `DashboardSummary` 或 `LocalContext` 的 helper，只做构造参数同步。
- 不需要为这些类新增紧急任务业务断言，避免重复覆盖。

### 文档一致性测试
- 文档中新增或修改的测试方法名必须真实存在，满足 `DocumentationDeliveryTest.documentationReferencesExistingJUnitTests`。
- 如果 `docs/test-cases.md` 新增 SUMMARY 编号，需同步保证 `DocumentationDeliveryTest` 对 SUMMARY 编号集合的断言仍通过；优先更新已有 `SUMMARY-05` 或新增 `SUMMARY-07` 并确认测试允许。

## 文档更新设计
- `docs/test-plan.md`：
  - 汇总统计范围从“空数据、单模块、多模块组合、本周/本月范围、笔记标签、AI 本地上下文”扩展为包含“逾期未完成任务、未来7天高优先级任务、AI prompt 上下文段落”。
  - AI 问答覆盖重点中补充 prompt 会注入紧急任务明细。
- `docs/test-cases.md`：
  - 更新 `AI-04`：预期从“system prompt 包含本地上下文”修正为“user message 包含本地上下文”，并加入逾期和未来高优先级段落。
  - 更新 `SUMMARY-05`：说明 AI 本地上下文包含今日任务、逾期未完成任务和未来7天高优先级任务。
  - 新增或更新 SUMMARY 用例，引用：
    - `SummaryServiceTest.getDashboardSummaryBuildsOverdueIncompleteTasks`
    - `SummaryServiceTest.getDashboardSummaryBuildsUpcomingHighPriorityIncompleteTasksWithinInclusiveWindow`
    - `ConsoleApplicationTest.summaryCommandDisplaysUrgentTaskCountsAndTitles`
    - `PromptBuilderTest.buildIncludesOverviewAndAllContextSections`
- `docs/coverage/README.md`：
  - `SummaryService.getDashboardSummary()` 控制流第3步改为查询全部任务。
  - 在任务查询后新增今日、逾期未完成、未来7天高优先级过滤步骤。
  - 独立路径新增：
    - 逾期未完成任务包含 TODO/IN_PROGRESS 并排除 COMPLETED。
    - 未来7天高优先级任务包含 today 和 today+7，排除低/中优先级、已完成、窗口外和逾期任务。
    - `buildLocalContext` 和 `PromptBuilder` 携带新增紧急任务明细。
  - 圈复杂度估算按最终实现增加任务过滤判定后更新，不保留旧数值 9。
- `docs/defect-regression.md`：
  - 可新增风险条目：摘要紧急事项覆盖不足，修复证据指向新增 SUMMARY/AI/CONSOLE 测试。
- `README.md`：
  - 如更新功能列表，可在 summary 或 AI Q&A 描述中说明本地上下文包含逾期未完成任务和未来7天高优先级任务。

## 修订说明（v3 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| 设计没有把新增 `LocalContext.overdueTaskLines()` 和 `LocalContext.upcomingHighPriorityTaskLines()` 接入 `PromptBuilder`，AI prompt 只能看到计数而没有明细。 | 新增 `PromptBuilder` 类型设计和文件规划，明确 `buildUserMessage(...)` 必须输出 `逾期未完成任务：` 与 `未来7天高优先级任务：` 段落，并复用 `section(...)` 的空列表 `（无）` 语义。 |
| `PromptBuilderTest.buildIncludesOverviewAndAllContextSections` 和空上下文用例应成为必须覆盖项。 | 在测试设计中将 `PromptBuilderTest` 单独列为必须修改，要求非空上下文断言两个新增段落明细，空上下文断言两个新增段落的 `（无）`。 |
| 文件规划、行为契约和文档更新需同步列出 `PromptBuilder.java` 与对应测试/文档影响，避免遗漏 AI prompt 出口。 | 文件规划新增 `PromptBuilder.java`、`PromptBuilderTest.java`；行为契约新增 prompt 段落必须存在和空列表不可省略；文档更新新增 AI-04、test-plan 和 coverage 中对 prompt 注入紧急任务明细的要求。 |
