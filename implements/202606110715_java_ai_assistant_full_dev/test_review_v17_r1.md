# 测试审查报告（v17 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java:51` — 固定时间源使用的日期 `2026-06-12` 与当前真实日期相同，`summaryDoesNotUseRealCurrentDate()` 中的 `LocalDate.now()` 断言也无法在当天识别错误实现。若 `SummaryService` 违背设计直接调用 `LocalDate.now()`，这些测试在 2026-06-12 仍可能全部通过，导致“所有日期只来自 TimeProvider”的核心契约测试不可靠。
- **[一般]** `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java:263` — `assertFirstFailure()` 对依赖失败消息只断言 `endsWith("failed")`，没有验证返回消息等于协作服务原始非空消息。设计要求非空失败消息必须原样传播；错误实现若统一返回 `"summary service dependency failed"` 或替换成其他 `failed` 结尾文本，当前测试仍可通过。
- **[一般]** `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java:75` — 多模块顺序测试构造了两条日程，但未断言 `todayScheduleLines()`。这遗漏了设计要求的“今日日程明细按 `todaySchedules` 当前顺序生成且格式固定”，错误实现若在多日程场景中漏掉、反转或误格式化日程行，当前用例不会失败。
- **[一般]** `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java:109` — `constructorCopiesInputListsAsUnmodifiableSnapshots()` 只修改了 `todayTaskLines` 的源列表，且只验证了 `todayTaskLines` 与 `noteTagLines` 的不可修改性。设计要求五类明细列表都必须复制为不可修改快照；错误实现若仅对部分列表做防御性复制或不可修改包装，当前测试覆盖不足。

## 修改要求

- `SummaryServiceTest.java:51`：将固定测试日期改为与真实运行日期无关且不等于当前日期的值，或使用 Mockito mock `TimeProvider` 并验证 `today()` 调用；同时删除或改写依赖 `LocalDate.now()` 的断言。关键目标是让直接调用真实日期的错误实现稳定失败。
- `SummaryServiceTest.java:263`：调整 `assertFirstFailure()` 接收期望消息，分别对 `"task failed"`、`"schedule failed"`、`"study failed"`、`"statistics failed"`、`"transactions failed"`、`"notes failed"` 做精确相等断言；保留空白消息 fallback 的单独测试。
- `LocalContextTest.java:75`：在 `fromBuildsLinesInSourceOrderForMultiModuleData()` 中补充 `todayScheduleLines()` 精确断言，覆盖两条日程的固定格式和源顺序。
- `LocalContextTest.java:109`：为 `todayScheduleLines`、`weekStudyPlanLines`、`monthTransactionLines`、`noteTagLines` 都使用可变输入列表并在构造后修改源列表；同时对五类访问器均断言修改会抛出 `UnsupportedOperationException`。
