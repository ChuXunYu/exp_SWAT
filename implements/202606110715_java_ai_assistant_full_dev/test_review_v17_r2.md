# 测试审查报告（v17 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java:257` — `summaryDoesNotUseRealCurrentDate()` 只验证 `timeProvider.today()` 至少被调用过，无法约束设计要求的“读取 `TimeProvider.today()` 一次或等价稳定快照”。当前 mock 始终返回同一天，错误实现若在同一次汇总中多次调用 `today()` 分别计算 today、周边界、月边界或查询条件，现有测试仍会通过，无法识别跨午夜或可变时间源导致的边界不一致风险。
- **[一般]** `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java:162` — 依赖失败传播测试只断言最终失败码和消息，没有验证“任一协作服务返回失败时立即返回”。例如 `taskService.listTasks(...)` 已失败后，错误实现仍继续调用日程、学习、收支和笔记服务，最后再返回任务失败，当前 `assertFirstFailure()` 仍会通过。这遗漏了设计中的短路行为和“不返回半成品摘要”的关键副作用约束。
- **[一般]** `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java:222` — `buildLocalContextReturnsLocalContextFromSuccessfulSummary()` 只断言 `overviewText` 和 `todayTaskLines()`，没有断言日程、学习计划、收支和标签四类明细行。设计要求 `buildLocalContext()` 返回由成功摘要生成的完整 `LocalContext`；错误实现若只填充任务明细、丢弃其他模块明细，仍可通过该服务级测试。`LocalContextTest` 覆盖的是 `LocalContext.from(...)` 自身，不能替代 `SummaryService.buildLocalContext()` 对完整上下文装配的断言。

## 修改要求（仅 REJECTED 时）

- `SummaryServiceTest.java:257`：新增或改写固定时间源测试，使用会在多次调用时返回不同日期的 mock/fake `TimeProvider`，或显式 `verify(timeProvider, times(1)).today()`；断言同一次摘要中的 `today`、周/月边界和所有服务查询参数均来自同一个稳定日期快照。
- `SummaryServiceTest.java:162`：在每个失败场景中补充后续依赖不被调用的 Mockito 验证。任务失败时不应调用日程、学习、收支、笔记服务；日程失败时不应调用学习、收支、笔记服务；依此类推，确保首个失败立即短路。
- `SummaryServiceTest.java:222`：补充 `todayScheduleLines()`、`weekStudyPlanLines()`、`monthTransactionLines()`、`noteTagLines()` 的精确格式和顺序断言，最好同时断言 `dashboardSummary()` 中对应快照数据存在，确保 `buildLocalContext()` 没有丢失非任务模块明细。
