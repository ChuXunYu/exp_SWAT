# 任务指令（v3）

## 动作
NEW

## 任务描述
修复摘要页紧急事项覆盖不足：扩展摘要数据结构、摘要服务、AI 本地上下文和控制台汇总页，使系统在保留今日任务的基础上，能够表达并展示逾期未完成任务和未来 7 天高优先级未完成任务。预期涉及：

- `java-ai-assistant/src/main/java/assistant/summary/DashboardSummary.java`
- `java-ai-assistant/src/main/java/assistant/summary/SummaryService.java`
- `java-ai-assistant/src/main/java/assistant/summary/LocalContext.java`
- `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java`
- `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java`
- `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java`
- `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java`
- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`
- 受影响的 README、测试计划、测试用例或覆盖文档。

规则固定为：
- 逾期未完成任务：`dueDate` 早于 `today`，且 `status != COMPLETED`。
- 未来 7 天高优先级未完成任务：`dueDate` 在 `today` 到 `today.plusDays(7)` 闭区间内，`priority == HIGH`，且 `status != COMPLETED`。
- 未来 7 天窗口包含今天，但不包含已经逾期的任务。
- 同一任务如果同时是今日任务和未来高优先级任务，可以在两个栏目出现；栏目名称和计数必须清晰。

## 选择理由
v1 已完成 AI 结构化草稿生成入口和任务草稿 dueDate 保存前一致性，v2 已完成学习计划草稿 breakdown 导入落地，且验证报告通过。摘要漏掉逾期未完成任务和未来 7 天高优先级任务仍属于本轮必须修复的真实风险，会影响用户汇总页和 AI 本地上下文的紧急事项感知，优先级高于中文枚举体验。

## 任务上下文
来自需求文档的关键约束：
- 摘要应在保留今日任务的基础上，增加两个紧急视图：逾期未完成任务、未来 7 天内到期的高优先级未完成任务。
- 已完成的逾期任务不应出现在逾期未完成任务中。
- 低/中优先级的未来任务不应出现在未来高优先级任务中。
- CLI 汇总页能显示逾期未完成任务数量和未来 7 天高优先级任务数量；如项目已有列表展示习惯，也应显示任务标题或关键摘要。
- AI 本地上下文包含逾期和未来高优先级任务信息。
- 必须补充 `SummaryService`、CLI 汇总展示和 AI 本地上下文测试，并同步受影响文档。

## 已有代码上下文
当前实现状态：
- `SummaryService.getDashboardSummary()` 只调用 `taskService.listTasks(TaskQuery.byDueDate(today))` 获取今日任务。
- `DashboardSummary` 当前只包含 `todayTasks`，没有 `overdueTasks` 或 `upcomingHighPriorityTasks` 字段。
- `LocalContext.from(...)` 只把 `dashboardSummary.todayTasks()` 转成 `todayTaskLines`；`overviewText` 也只包含今日任务数。
- `ConsoleApplication.showSummary()` 只输出今日任务数、今日日程数、本周学习计划数、本月收支、笔记数和标签数。
- `TaskQuery` 支持 status、priority、dueDate 单日匹配，不支持日期范围。为降低改动范围，本轮可在 `SummaryService` 中通过 `taskService.listTasks(TaskQuery.all())` 取得任务后按上述规则过滤；若担心依赖失败传播，应保持任一任务查询失败时返回稳定失败结果。
- 任务领域已有 `TaskStatus.COMPLETED`、`TaskPriority.HIGH`、`TaskView.dueDate()`、`TaskView.status()`、`TaskView.priority()` 可用于过滤。

测试要求：
- `SummaryService` 测试覆盖逾期未完成任务纳入摘要、已完成逾期任务排除、未来 7 天 HIGH 未完成任务纳入摘要、非 HIGH 或超出窗口任务排除。
- `DashboardSummary` 或相关测试覆盖新增列表的空值、元素空值和不可变拷贝约束，保持已有构造校验风格。
- `LocalContext` 测试覆盖 overview 文本和新增紧急任务行包含逾期与未来高优先级任务信息。
- `ConsoleApplicationTest` 覆盖汇总输出包含逾期未完成任务数量和未来 7 天高优先级任务数量，必要时断言任务标题。
- 文档更新后必须满足现有 `DocumentationDeliveryTest` 文档引用一致性检查。
