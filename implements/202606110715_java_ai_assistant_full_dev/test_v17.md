# 测试报告（v17）

## 概述

已根据 `detail_v17.md` 的行为契约核对并补充 `assistant.summary` 包单元测试。测试覆盖跨模块摘要快照、本地上下文固定文本、服务查询边界、错误传播、标签聚合顺序和本周学习计划统计口径。

本轮在既有 v17 测试基础上补强了以下边界断言，并处理 `test_review_v17_r1.md` 的全部反馈：

- `DashboardSummaryTest.constructorRejectsNullRequiredFieldsAndElements()`：补齐周/月日期、各列表、列表元素、标签映射、标签 key/value 为空时的拒绝路径。
- `DashboardSummaryTest.constructorRejectsInvalidDateBoundariesAndCounts()`：补齐未完成计数为负、标签计数非正数时的拒绝路径。
- `LocalContextTest.constructorRejectsNullsAndBlankOverviewOrLines()`：补齐五类明细列表为空引用、元素为空引用、行文本为空白时的拒绝路径。
- `SummaryServiceTest.getDashboardSummaryUsesStableFallbackWhenDependencyFailureMessageIsBlank()`：验证依赖失败消息为空白时返回稳定消息 `summary service dependency failed`，避免二次构造失败。
- `SummaryServiceTest`：固定日期改为 `2026-02-18`，不再与当前真实日期 `2026-06-12` 重合；使用 Mockito mock `TimeProvider` 并验证 `today()` 被调用，使直接读取真实日期的错误实现稳定失败。
- `SummaryServiceTest.getDashboardSummaryPropagatesFirstDependencyFailure()`：对 `task failed`、`schedule failed`、`study failed`、`statistics failed`、`transactions failed`、`notes failed` 做精确消息断言，覆盖非空失败消息原样传播。
- `LocalContextTest.fromBuildsLinesInSourceOrderForMultiModuleData()`：补充两条今日日程明细的固定格式和源顺序断言。
- `LocalContextTest.constructorCopiesInputListsAsUnmodifiableSnapshots()`：五类明细列表均使用可变输入列表，构造后修改源列表并逐一断言访问器不可修改。

针对 `test_review_v17_r2.md` 的反馈，本轮继续补强：

- `SummaryServiceTest.summaryDoesNotUseRealCurrentDate()`：将 `TimeProvider.today()` 验证收紧为 `times(1)`，覆盖单次稳定时间快照约束。
- `SummaryServiceTest.getDashboardSummaryUsesSingleStableTodaySnapshotForAllDateBoundariesAndQueries()`：使用第二次调用会返回不同日期的 `TimeProvider` mock，断言摘要日期边界和全部服务查询参数均来自第一次稳定日期快照，并验证 `today()` 只调用一次。
- `SummaryServiceTest.getDashboardSummaryPropagatesFirstDependencyFailure()`：对任务、日程、学习计划、收支统计、收支列表、笔记失败场景分别补充后续依赖 `never()` 验证，确保首个依赖失败后立即短路。
- `SummaryServiceTest.buildLocalContextReturnsLocalContextFromSuccessfulSummary()`：补充 `dashboardSummary()` 中任务、日程、学习计划、收支、标签快照存在性断言，并精确断言日程、学习计划、收支、标签明细行的格式和顺序。

## 测试文件

| 文件路径 | 覆盖范围 |
|----------|----------|
| `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java` | 摘要 record 构造校验、日期边界、学习计划计数一致性、集合复制、不可修改快照、标签顺序和空摘要。 |
| `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java` | 固定总览文本、任务/日程/学习/收支/标签明细格式、来源顺序、输入快照隔离、不可修改列表和非法输入拒绝。 |
| `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java` | 服务依赖非空校验、固定时间边界、跨模块查询参数、标签聚合、快照隔离、依赖失败传播、本周学习计划统计口径和本地上下文构建。 |

## 验证命令

已执行 summary 包定向测试：

```bash
mvn -q -Dtest='assistant.summary.*Test' test
```

结果：通过。

已执行全量单元测试：

```bash
mvn -q test
```

结果：通过。

## 结论

v17 设计要求的新增 `assistant.summary` 公开行为均已有对应单元测试覆盖；新增测试均基于公开接口和行为契约，不依赖实现细节，不修改编码 Agent 的生产源码文件。
