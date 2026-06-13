# 覆盖证据说明

## JaCoCo 报告生成

以下命令均从 `java-ai-assistant/` 目录执行：

```bash
mvn clean verify
mvn jacoco:report
```

JaCoCo HTML 报告输出到 `target/site/jacoco/index.html`。本文件只记录生成方式、路径分析和用例映射，不提交 `target/site/jacoco/` 生成物，也不填写未实际采集的覆盖率百分比。

## 覆盖目标

覆盖重点是核心业务包中的服务、策略、值对象和 AI 模块，包括 `assistant.task`、`assistant.schedule`、`assistant.study`、`assistant.finance`、`assistant.note`、`assistant.summary`、`assistant.ai` 和 `assistant.common`。控制台层作为补充，用于验证用户输入解析、菜单循环、错误提示和跨模块场景链路。

## 重点方法路径分析

### `assistant.finance.FinanceStatisticsService.calculate(List<TransactionRecord>)`

文字化控制流：

1. 校验 `records` 非空。
2. 初始化收入合计和支出合计为 0。
3. 遍历交易列表。
4. 对每条记录校验非空并转换金额。
5. 若交易类型为收入，则累加收入；否则累加支出。
6. 遍历结束后返回 `FinanceStatistics`。

圈复杂度估算：按主要判定节点估算为 3，包含循环判定和收入/支出分支。

独立路径：

| 路径 | 条件 | 预期 | 映射用例 |
|------|------|------|----------|
| F-P1 | 空集合 | 收入 0、支出 0、结余 0 | FINANCE-06；`FinanceStatisticsServiceTest.calculateReturnsZeroForEmptyRecords` |
| F-P2 | 仅收入记录 | 只累计收入 | FINANCE-01, FINANCE-06；`FinanceStatisticsServiceTest` 收入统计场景 |
| F-P3 | 仅支出记录 | 只累计支出 | FINANCE-01, FINANCE-06；`FinanceStatisticsServiceTest` 支出统计场景 |
| F-P4 | 收入和支出混合 | 分别累计并计算结余 | FINANCE-06；`FinanceStatisticsServiceTest.calculateAccumulatesIncomeAndExpenseSeparately` |
| F-P5 | 支出大于收入 | 结余为负 | FINANCE-06；`FinanceStatisticsServiceTest.calculateAllowsNegativeBalanceWhenExpenseExceedsIncome` |

覆盖结论：路径覆盖空集合、收入分支、支出分支、混合统计和负结余语义。

### `assistant.ai.DraftImportService.importDraft(SuggestionDraft)`

文字化控制流：

1. 若草稿为 `null`，返回校验失败。
2. 判断草稿类型是否为任务草稿。
3. 任务草稿先遍历校验每个任务是否有 dueDate。
4. dueDate 缺失时立即返回校验失败，不创建任何任务。
5. dueDate 全部有效时逐条调用 `TaskService.createTask`。
6. 任一任务创建返回失败时，回滚已创建任务并返回失败。
7. 任一任务创建抛出运行时异常时，回滚已创建任务并映射为系统失败。
8. 学习计划草稿读取内容并调用 `StudyPlanService.createStudyPlan`。
9. 学习计划创建失败时传播失败，不创建 breakdown 任务。
10. 学习计划创建成功后将 breakdown 转为任务列表。
11. breakdown 为空时直接返回成功。
12. breakdown 非空时逐条创建正式任务。
13. 任一 breakdown 任务创建返回失败时，回滚已创建 breakdown 任务并补偿删除本次学习计划。
14. 任一 breakdown 任务创建抛出运行时异常时，回滚已创建 breakdown 任务、补偿删除本次学习计划并映射为系统失败。
15. 捕获 `BusinessException` 和其他 `RuntimeException` 并映射为 `OperationResult` 失败。

圈复杂度估算：按主要判定节点估算为 12，包含空草稿、类型分支、dueDate 校验循环、任务创建循环、创建失败、运行时异常、学习计划失败、breakdown 空分支和学习计划补偿分支。

独立路径：

| 路径 | 条件 | 预期 | 映射用例 |
|------|------|------|----------|
| D-P1 | `draft == null` | 返回 `VALIDATION_ERROR` | DRAFT-09；`DraftImportServiceTest.importDraftRejectsNullDraft` |
| D-P2 | 任务草稿全部有效 | 批量创建任务成功 | DRAFT-07；`DraftImportServiceTest.importsAllTaskDraftItems` |
| D-P3 | 任务草稿缺失 dueDate | 创建前失败，不写入任务 | DRAFT-08；`DraftImportServiceTest.rejectsTaskDraftMissingDueDateBeforeCreatingAnyTask` |
| D-P4 | 任务创建返回失败 | 回滚已创建任务并返回失败 | DRAFT-08；`DraftImportServiceTest.rollsBackCreatedTasksWhenTaskCreationFails` |
| D-P5 | 任务创建抛运行时异常 | 回滚已创建任务并返回系统失败 | DRAFT-08；`DraftImportServiceTest.rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException` |
| D-P6 | 学习计划草稿含 breakdown | 创建学习计划并按顺序创建正式任务 | DRAFT-09；`DraftImportServiceTest.importsStudyPlanDraftCreatesTasksForBreakdown` |
| D-P7 | 学习计划草稿无 breakdown | 只创建学习计划 | DRAFT-09；`DraftImportServiceTest.importsStudyPlanDraftWithoutBreakdownCreatesOnlyStudyPlan` |
| D-P8 | 学习计划服务返回失败 | 传播目标服务失败且不创建任务 | DRAFT-09；`DraftImportServiceTest.propagatesStudyPlanCreationFailureWithoutCreatingBreakdownTasks` |
| D-P9 | breakdown 任务创建返回失败 | 回滚已创建 breakdown 任务并补偿删除学习计划 | DRAFT-16；`DraftImportServiceTest.rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationFails` |
| D-P10 | breakdown 任务创建抛运行时异常 | 回滚已创建 breakdown 任务、补偿删除学习计划并返回系统失败 | DRAFT-16；`DraftImportServiceTest.rollsBackStudyPlanAndCreatedBreakdownTasksWhenBreakdownTaskCreationThrowsRuntimeException` |

覆盖结论：路径覆盖草稿类型分支、任务批量先校验后写入、回滚、学习计划导入、breakdown 转正式任务、跨模块补偿和失败传播。

### `assistant.summary.SummaryService.getDashboardSummary()`

文字化控制流：

1. 从 `TimeProvider` 获取今日日期。
2. 计算本周开始、本周结束、本月开始和本月结束。
3. 通过 `TaskQuery.all()` 查询一次全量任务快照，失败则立即传播失败。
4. 从同一任务快照中过滤今日任务。
5. 从同一任务快照中过滤逾期未完成任务。
6. 从同一任务快照中过滤未来 7 天高优先级未完成任务。
7. 查询今日日程，失败则立即传播失败。
8. 查询本周学习计划，失败则立即传播失败。
9. 查询本月收支统计，失败则立即传播失败。
10. 查询本月交易列表，失败则立即传播失败。
11. 查询笔记列表，失败则立即传播失败。
12. 统计本周已完成和未完成学习计划。
13. 统计笔记标签分布。
14. 组装并返回 `DashboardSummary`。

圈复杂度估算：按主要判定节点估算为 14，包含 6 个依赖失败早返回、3 个任务过滤条件组合、学习计划状态过滤和笔记标签嵌套循环。

独立路径：

| 路径 | 条件 | 预期 | 映射用例 |
|------|------|------|----------|
| S-P1 | 全部仓储为空 | 返回空仪表盘和 0 统计 | SUMMARY-01；`SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries` |
| S-P2 | 多模块均有数据 | 聚合今日任务、紧急任务视图、日程、本周计划、本月收支、笔记标签 | SUMMARY-02；`SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries`, `ConsoleApplicationTest.summaryCommandDisplaysDashboardSummary` |
| S-P3 | 固定日期跨周/月边界 | 使用周一到周日、本月首日至末日查询 | SUMMARY-03；`SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries`, `SummaryServiceTest.getDashboardSummaryUsesSingleStableTodaySnapshotForAllDateBoundariesAndQueries` |
| S-P4 | 多个学习计划含完成和未完成状态 | 已完成/未完成计数正确 | SUMMARY-02；`SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries` |
| S-P5 | 多条笔记含重复标签 | 标签分布合并计数 | SUMMARY-04；`SummaryServiceTest.getDashboardSummaryAggregatesNoteTagsInFirstSeenOrder` |
| S-P6 | 任一依赖服务失败 | 立即返回失败并使用稳定消息 | SUMMARY-06；`SummaryServiceTest.getDashboardSummaryPropagatesFirstDependencyFailure`, `SummaryServiceTest.getDashboardSummaryUsesStableFallbackWhenDependencyFailureMessageIsBlank` |
| S-P7 | 任务快照含逾期、已完成、非高优先级、今天、第 7 天和第 8 天任务 | 逾期和未来 7 天高优先级视图按规则过滤并保持源顺序 | SUMMARY-07；`SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries` |
| S-P8 | 调用 `buildLocalContext` | 基于仪表盘生成含紧急任务明细的 AI 本地上下文 | SUMMARY-05；`SummaryServiceTest.buildLocalContextReturnsLocalContextFromSuccessfulSummary`, `LocalContextTest.fromBuildsStableOverviewAndEmptyLinesForEmptySummary`, `LocalContextTest.fromBuildsLinesInSourceOrderForMultiModuleData` |

覆盖结论：路径覆盖空数据、单模块/多模块组合、今日任务、逾期未完成任务、未来 7 天高优先级任务、本周和本月范围、统计同步、标签聚合、依赖失败传播和 AI 本地上下文生成。

## 覆盖证据与用例映射

| 方法 | 独立路径 | 测试类/方法或用例编号 | 覆盖结论 |
|------|----------|------------------------|----------|
| `assistant.finance.FinanceStatisticsService.calculate(List<TransactionRecord>)` | F-P1 至 F-P5 | FINANCE-06；`FinanceStatisticsServiceTest.calculateReturnsZeroForEmptyRecords`, `FinanceStatisticsServiceTest.calculateAccumulatesIncomeAndExpenseSeparately`, `FinanceStatisticsServiceTest.calculateAllowsNegativeBalanceWhenExpenseExceedsIncome` | 覆盖空集合、收入、支出、混合和负结余 |
| `assistant.ai.DraftImportService.importDraft(SuggestionDraft)` | D-P1 至 D-P10 | DRAFT-07 至 DRAFT-09, DRAFT-16；`DraftImportServiceTest` 任务和学习计划导入场景 | 覆盖类型分支、校验、回滚、breakdown 任务同步和失败传播 |
| `assistant.summary.SummaryService.getDashboardSummary()` | S-P1 至 S-P7 | SUMMARY-01 至 SUMMARY-07；`SummaryServiceTest` 汇总场景 | 覆盖空数据、多模块、时间范围、紧急任务过滤、标签统计和依赖失败 |
| `assistant.summary.SummaryService.buildLocalContext()` | S-P8 | SUMMARY-05；`SummaryServiceTest.buildLocalContextReturnsLocalContextFromSuccessfulSummary`, `LocalContextTest.fromBuildsStableOverviewAndEmptyLinesForEmptySummary`, `LocalContextTest.fromBuildsLinesInSourceOrderForMultiModuleData` | 覆盖 AI 本地上下文链路 |

## 结果记录方式

JaCoCo 生成物位于 `target/site/jacoco/`，不提交到仓库。课程报告如需截图或百分比，应先在本地执行 `mvn clean verify` 或 `mvn jacoco:report`，再引用 `target/site/jacoco/index.html` 的实际结果。本文件不以覆盖率数字替代路径和用例映射。

相关文档：[`../test-plan.md`](../test-plan.md)、[`../test-cases.md`](../test-cases.md)、[`../defect-regression.md`](../defect-regression.md)。
