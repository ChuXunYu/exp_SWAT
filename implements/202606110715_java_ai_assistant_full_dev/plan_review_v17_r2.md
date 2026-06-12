# 计划审查报告（v17 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** — `DashboardSummary` 和 `overviewText` 中的学习计划统计口径自相矛盾。任务要求本轮实现“本周学习计划统计”，并把 `DashboardSummary.weekStudyPlans` 定义为通过 `StudyPlanQuery.byPeriod(DateRange)` 得到的本周相关计划；但同一摘要又要求 `completedStudyPlanCount` 和 `incompleteStudyPlanCount` 直接使用 `StudyPlanService.countCompletedPlans()` / `countIncompletePlans()` 的现有全量统计。这样会生成形如“本周学习计划 1 项（已完成 3 项，未完成 4 项）”的上下文，完成/未完成数量并不属于本周计划集合，误导后续 AI 提示词和汇总展示，也无法满足需求中“查看本周学习计划完成情况”的语义。由于 v12 已明确这两个服务接口只做全量统计且不提供按条件过滤重载，当前 v17 计划必须显式补足本周口径，而不是把全量计数嵌入本周摘要。

## 修改要求

- 对学习计划统计口径作出唯一、可执行的修正：`DashboardSummary` 和 `LocalContext.overviewText` 若表达“本周学习计划统计”，其完成/未完成数量必须基于 `weekStudyPlans` 这一本周快照按 `StudyPlanView.status()` 计算，或另行把字段和文案明确改名为“全量计划已完成/未完成”并避免称其为本周完成情况。建议优先保持需求语义，新增或改写为本周计数字段，例如 `completedWeekStudyPlanCount` / `incompleteWeekStudyPlanCount`，由 `SummaryService` 基于 `weekStudyPlans` 计算；同时可继续调用 `StudyPlanService.countCompletedPlans()` / `countIncompletePlans()` 仅用于全量计划统计时使用。任务文件需同步固定 `overviewText` 格式和 `SummaryServiceTest` 断言，覆盖“本周计划数量与全量计划数量不同”的场景，防止再次混淆。
