# 任务指令（v17）

## 动作
NEW

## 任务描述
新增 `assistant.summary` 汇总统计模块的摘要结果、AI 本地上下文和汇总服务，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/summary/DashboardSummary.java`
- `java-ai-assistant/src/main/java/assistant/summary/LocalContext.java`
- `java-ai-assistant/src/main/java/assistant/summary/SummaryService.java`
- `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java`
- `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java`
- `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java`

本轮实现第 8 个核心功能“数据查询与汇总统计”的基础闭环：查看今日待办和日程、本周学习计划统计、本月收支统计、笔记数量与标签分布，并生成供后续 AI 提示词使用的本地上下文。汇总模块必须只读协作现有应用服务，不直接依赖各业务仓储，也不得暴露可变领域实体引用。

建议的公开类型边界：

- `DashboardSummary`：只读 record，保存 `LocalDate today`、`LocalDate weekStart`、`LocalDate weekEnd`、`LocalDate monthStart`、`LocalDate monthEnd`、`List<TaskView> todayTasks`、`List<ScheduleView> todaySchedules`、`List<StudyPlanView> weekStudyPlans`、`int completedWeekStudyPlanCount`、`int incompleteWeekStudyPlanCount`、`FinanceStatistics monthFinanceStatistics`、`List<TransactionView> monthTransactions`、`int noteCount`、`Map<Tag, Integer> noteTagDistribution`。
- `LocalContext`：只读 record，公开形态固定为 `LocalContext(DashboardSummary dashboardSummary, String overviewText, List<String> todayTaskLines, List<String> todayScheduleLines, List<String> weekStudyPlanLines, List<String> monthTransactionLines, List<String> noteTagLines)`。除 `dashboardSummary` 外，其余字段均为面向后续 AI 提示词的确定性本地上下文片段；字符串不得为 `null` 或空白，列表不得为 `null`，列表必须复制为不可修改快照。
- `SummaryService`：应用服务，构造时接收 `TaskService`、`ScheduleService`、`StudyPlanService`、`FinanceService`、`NoteService`、`TimeProvider`，提供 `OperationResult<DashboardSummary> getDashboardSummary()` 与 `OperationResult<LocalContext> buildLocalContext()`（方法名可等价但语义必须唯一清晰）。

服务行为要求：

- 今日任务通过 `TaskService.listTasks(TaskQuery.byDueDate(today))` 获取。
- 今日日程通过 `ScheduleService.listSchedulesByDate(today)` 获取。
- 本周学习计划采用 ISO 周语义，周一到周日；通过 `DateRange(weekStart, weekEnd)` 和 `StudyPlanQuery.byPeriod(...)` 获取与本周有重叠的计划。
- 本周学习计划完成/未完成数量必须基于本次 `StudyPlanService.listStudyPlans(StudyPlanQuery.byPeriod(weekRange))` 返回的 `weekStudyPlans` 快照计算：`completedWeekStudyPlanCount` 统计 `StudyPlanView.status() == StudyPlanStatus.COMPLETED` 的本周计划数量，`incompleteWeekStudyPlanCount` 统计同一 `weekStudyPlans` 快照中其余状态的计划数量。`SummaryService` 不得调用 `StudyPlanService.countCompletedPlans()` 或 `countIncompletePlans()` 来填充本周统计，避免把全量计划数量误写成本周完成情况；也不得重新推导学习计划动态状态，只能使用 `StudyPlanView.status()`。
- 本月收支采用当月第一天到最后一天；通过 `TransactionQuery.byDateRange(monthRange)` 获取本月记录，并通过 `FinanceService.calculateStatistics(monthQuery)` 获取本月统计。
- 笔记数量来自 `NoteService.listNotes()` 的当前快照；标签分布按 `NoteView.tags()` 中的 `Tag` 值语义聚合，使用 `LinkedHashMap` 或等价稳定顺序保留首次出现标签顺序。
- 返回的列表和映射必须是不可修改快照；后续业务数据变化不能改变已经返回的 `DashboardSummary` 或 `LocalContext`。
- 如果任一被协作服务返回失败，`SummaryService` 必须返回同一 `ErrorCode` 的失败结果和稳定消息，不得吞掉错误或返回半成品成功摘要。
- 汇总服务不得调用 `LocalDate.now()`、`LocalDateTime.now()` 或读取真实环境；所有日期边界只来自 `TimeProvider.today()`。

`LocalContext` 的字段生成口径必须固定如下，后续设计和实现不得另行选择字段或自由拼接：

- `dashboardSummary`：直接保存本次 `getDashboardSummary()` 生成的不可变摘要快照，构造时必须非空。
- `overviewText`：固定为一行总览文本，格式为 `今日任务{todayTasks.size()}项，今日日程{todaySchedules.size()}项，本周学习计划{weekStudyPlans.size()}项（已完成{completedWeekStudyPlanCount}项，未完成{incompleteWeekStudyPlanCount}项），本月收入{monthFinanceStatistics.totalIncome().toPlainString()}，支出{monthFinanceStatistics.totalExpense().toPlainString()}，结余{monthFinanceStatistics.balance().toPlainString()}，笔记{noteCount}篇，标签{noteTagDistribution.size()}个。`。不得使用地区化格式、真实时间或随机顺序。
- `todayTaskLines`：按 `todayTasks` 当前顺序生成，一条任务一行，格式为 `任务：{title}｜优先级：{priority.name()}｜状态：{status.name()}｜截止：{dueDate}`；无今日任务时返回空列表，不填充占位字符串。
- `todayScheduleLines`：按 `todaySchedules` 当前顺序生成，一条日程一行，格式为 `日程：{name}｜状态：{status.name()}｜时间：{startDateTime}~{endDateTime}｜地点：{location}`；无今日日程时返回空列表。
- `weekStudyPlanLines`：按 `weekStudyPlans` 当前顺序生成，一条学习计划一行，格式为 `学习：{goalName}｜状态：{status.name()}｜进度：{progress.value()}%｜周期：{startDate}~{endDate}`；无本周学习计划时返回空列表。
- `monthTransactionLines`：按 `monthTransactions` 当前顺序生成，一条收支记录一行，格式为 `收支：{type.name()}｜金额：{amount.value().toPlainString()}｜类别：{category}｜日期：{date}`；无本月收支记录时返回空列表。
- `noteTagLines`：按 `noteTagDistribution` 的稳定迭代顺序生成，一条标签一行，格式为 `标签：{tag.displayName()}｜数量：{count}`；无标签时返回空列表。
- `LocalContext` 构造器必须复制所有列表并包装为不可修改列表；即使后续业务服务、输入集合或 `DashboardSummary` 来源数据发生变化，已返回的 `LocalContext` 字段也不能变化。
- `LocalContextTest` 至少覆盖：空数据时 `overviewText` 稳定且各明细列表为空；单模块有数据时仅对应明细列表有行；多模块数据时各行按上述格式和源列表顺序生成；构造后修改输入列表不能影响 `LocalContext`；任一字段传 `null` 或空白 `overviewText` 会被拒绝。
- `SummaryServiceTest` 必须覆盖：本周计划数量与全量计划数量不同的场景下，`DashboardSummary.completedWeekStudyPlanCount`、`incompleteWeekStudyPlanCount` 和 `LocalContext.overviewText` 只反映 `weekStudyPlans` 快照中的完成/未完成数量；全量 `StudyPlanService.countCompletedPlans()` / `countIncompletePlans()` 不得被用于本周摘要。测试中至少包含一个本周外已完成计划，防止再次把全量完成数混入本周统计。

## 选择理由
v16 已完成个人笔记服务闭环，至此任务、日程、学习计划、收支和笔记五个本地数据模块都已经有稳定的只读视图服务。需求中的第 8 个核心功能要求跨模块汇总统计，AI 问答与建议也要求结合本地记录生成上下文。先实现 `assistant.summary`，可以为后续 `assistant.ai` 的提示词构造、AI 摘要、结构化建议和控制台首页展示提供统一、可测试的数据入口。

该任务风险集中在跨模块查询口径、时间边界和快照隔离，适合在 AI 客户端与控制台之前完成并用固定时间单元测试压实。

## 任务上下文
需求要求数据查询与汇总统计至少覆盖：

- 今日待办和日程。
- 本周学习计划完成情况。
- 本月收支统计。
- 笔记数量或标签分布。
- 生成可供 AI 总结的本地上下文。

技术方案要求：

- `SummaryService` 每次即时读取任务、日程、学习计划、收支和笔记服务，不维护冗余缓存。
- 今日摘要基于 `TimeProvider.today()`。
- 本周统计采用 ISO 周一到周日。
- 本月收支统计采用当前月第一天到最后一天。
- `LocalContext` 是 AI 提示词输入的摘要对象，不暴露领域实体内部可变结构。
- 空数据、单模块数据和多模块组合数据都应有稳定表达，便于单元测试断言提示词包含必要上下文。
- 普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## 已有代码上下文
当前工程已经具备以下可复用服务和只读视图：

- `assistant.task.TaskService`：`listTasks()`、`listTasks(TaskQuery)`，其中 `TaskQuery.byDueDate(LocalDate)` 可筛选今日任务，返回 `OperationResult<List<TaskView>>`。
- `assistant.schedule.ScheduleService`：`listSchedulesByDate(LocalDate)` 和 `listSchedules(ScheduleQuery)`，返回带动态 `ScheduleStatus` 的 `ScheduleView`。
- `assistant.study.StudyPlanService`：`listStudyPlans(StudyPlanQuery)`、`countCompletedPlans()`、`countIncompletePlans()`；`StudyPlanQuery.byPeriod(DateRange)` 使用日期区间重叠筛选。
- `assistant.finance.FinanceService`：`listTransactions(TransactionQuery)` 和 `calculateStatistics(TransactionQuery)`；`TransactionQuery.byDateRange(DateRange)` 可筛选本月交易。
- `assistant.note.NoteService`：`listNotes()` 返回 `List<NoteView>`；`NoteView.tags()` 已是不可修改标签集合快照。
- `assistant.common.DateRange` 表示左右闭日期区间；`assistant.common.Tag` 可作为标签分布 Map 的 key；`assistant.common.OperationResult` 和 `ErrorCode` 是应用服务统一返回语义。
- `assistant.testability.TimeProvider` 已提供可注入当前日期，测试可用 `FixedTimeProvider` 控制本周、本月和跨月边界。

本轮不得实现 `assistant.ai`、真实 DeepSeek HTTP 客户端、草稿导入、控制台菜单或文档报告；这些应在后续任务基于 `LocalContext` 和 `SummaryService` 继续推进。

## 修订说明（v17 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| `LocalContext` 公开结构未收束，仍使用“可包含”“简短文本字段或结构化字段”等开放描述，后续 AI 提示词构造和测试无法稳定依赖。 | 固定 `LocalContext` record 字段为 `dashboardSummary`、`overviewText` 和五类模块明细行列表；逐项规定总览文本、任务、日程、学习计划、收支和标签行的生成格式、空数据表达、快照不可变要求和测试覆盖点。 |

## 修订说明（v17 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| `DashboardSummary` 和 `overviewText` 将本周学习计划列表与 `StudyPlanService.countCompletedPlans()` / `countIncompletePlans()` 的全量完成/未完成数量混用，会生成“本周学习计划 N 项（已完成全量 X 项，未完成全量 Y 项）”的误导性上下文。 | 将摘要字段改为 `completedWeekStudyPlanCount` / `incompleteWeekStudyPlanCount`，要求 `SummaryService` 仅基于 `weekStudyPlans` 快照中的 `StudyPlanView.status()` 计算本周完成/未完成数量，不调用全量统计接口填充本周摘要；同步更新 `overviewText` 格式和 `SummaryServiceTest` 覆盖“本周计划数量与全量计划数量不同”的场景。 |
