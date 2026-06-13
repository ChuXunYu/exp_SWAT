# 任务指令（v4）

## 动作
NEW

## 任务描述
在 `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 中改善中文控制台枚举输入和输出体验：

- 为任务优先级、任务状态、日程状态、学习计划状态、收支类型补齐中文别名输入。
- 控制台列表、详情、摘要紧急任务明细、草稿详情和错误提示优先显示中文枚举含义。
- 保留现有英文枚举输入兼容，不改变服务层类型安全枚举和业务规则。
- 更新 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 中相关断言，并补充中文输入、英文兼容、非法输入中文错误提示测试。
- 同步 `java-ai-assistant/docs/test-plan.md`、`java-ai-assistant/docs/test-cases.md` 或受影响文档，确保文档引用真实测试方法和新行为。

## 选择理由
v1 至 v3 已分别完成并通过验证：AI 结构化草稿生成入口与 dueDate 保存前一致性、学习计划草稿 breakdown 导入落地、摘要页紧急任务视图。本轮需求列入“必须修复”的剩余真实风险是中文 CLI 暴露英文枚举。该问题主要位于控制台展示和输入解析层，单独实现可以避免扩大服务层或领域模型改动范围。

## 任务上下文
来自本轮修复需求的约束：

- 用户可以用中文输入任务优先级、任务状态、日程状态、学习计划状态和收支类型等常见枚举条件。
- 控制台列表、详情和错误提示优先显示中文含义，而不是内部英文枚举名。
- 既有英文枚举输入作为兼容别名继续可用。
- 非法输入时，错误提示列出中文可选值，并可附带英文别名。
- 不引入复杂国际化框架，不修改业务服务层枚举建模。

建议映射：

- `TaskPriority`: `LOW=低`、`MEDIUM=中`、`HIGH=高`；输入支持 `低/中/高` 与 `LOW/MEDIUM/HIGH`。
- `TaskStatus`: `TODO=未完成`、`COMPLETED=已完成`；输入支持中文与英文。
- `ScheduleStatus`: `UPCOMING=即将开始`、`ONGOING=进行中`、`EXPIRED=已过期`；输入支持中文与英文。
- `StudyPlanStatus`: `NOT_STARTED=未开始`、`IN_PROGRESS=进行中`、`COMPLETED=已完成`、`OVERDUE_INCOMPLETE=逾期未完成`；输入支持中文与英文。
- `TransactionType`: `INCOME=收入`、`EXPENSE=支出`；输入支持中文与英文。

## 已有代码上下文
当前 `ConsoleApplication` 中直接暴露英文枚举的位置包括：

- 任务新增/修改/筛选提示和错误：`优先级(LOW/MEDIUM/HIGH)`、`状态(TODO/COMPLETED)`，以及 `parseTaskPriority`、`parseTaskStatus`。
- 任务列表/详情、摘要紧急任务明细和任务草稿详情直接输出 `task.priority()`、`task.status()` 或 `item.priority()`。
- 日程筛选提示和错误使用 `UPCOMING/ONGOING/EXPIRED`，列表/详情直接输出 `schedule.status()`。
- 学习计划筛选提示和错误使用 `NOT_STARTED/IN_PROGRESS/COMPLETED/OVERDUE_INCOMPLETE`，列表/详情直接输出 `plan.status()`。
- 收支修改/筛选提示和错误使用 `INCOME/EXPENSE`，列表/详情直接输出 `transaction.type()`。
- 草稿详情目前直接输出 `draft.type()`、`draft.status()`；如本轮选择覆盖草稿状态/类型展示，应保证不破坏既有 AI 草稿生命周期测试。

实现建议：

- 在 `ConsoleApplication` 内新增或复用小型私有 helper，集中完成枚举 parse 和 displayName，避免在各菜单分支散落硬编码。
- read prompt 文案改为中文优先，例如 `优先级(低/中/高，可输入 LOW/MEDIUM/HIGH):`。
- 列表和详情输出用 display helper，例如任务列表输出 `高 | 未完成` 而不是 `HIGH | TODO`。
- 英文兼容测试必须证明旧输入仍可创建、筛选或修改成功。
- 文档一致性测试若检查测试用例表，需要同步新增 CLI 中文枚举用例编号或说明。
