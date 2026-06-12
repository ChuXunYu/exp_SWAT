# 测试审查报告（v12 r2）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — `countMethodsUseInjectedCurrentDate()`（约第 529-538 行）并没有真正锁住“统计逻辑必须把 `timeProvider.today()` 返回的日期传给动态状态分析”这一契约。该用例只放入了一个已完成计划，`countCompletedPlans()` / `countIncompletePlans()` 的结果对日期天然不敏感，再加上断言只检查 `todayCalls()` 次数，导致实现即使只是“表面上调用了 `timeProvider.today()`，实际统计时改用 `LocalDate.now()`、常量日期，或传错日期给 `analysisService`”也仍然会通过。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanViewTest.java` — `StudyPlanView.from(...)` 的测试（约第 18-43 行）只使用真实 `StudyPlanAnalysisService` 校验最终状态，没有验证它是否真的调用了“调用方传入的分析服务和日期”。如果实现改成在 `from(...)` 内部自行 `new StudyPlanAnalysisService()`、复制状态推导逻辑，甚至忽略传入依赖，现有测试依旧会通过，覆盖不到设计里“显式依赖分析组件、禁止复制动态状态逻辑”的约束。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — `listStudyPlansWithQueryRejectsNullQuery()`（约第 249-255 行）仍然只断言了 `ErrorCode`，没有断言 `OperationResult.getMessage()`。这使得 `listStudyPlans(null)` 这条公开校验失败路径即使返回了错误的消息文本，也不会被发现，和本轮补强“服务层错误映射要连同消息一起校验”的目标不一致。

## 修改要求（仅 REJECTED 时）
1. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 的 `countMethodsUseInjectedCurrentDate()` 附近重写统计类测试：不要再用日期无关的数据集只看 `todayCalls()`。应注入可观察参数的 `StudyPlanAnalysisService`（mock / spy / 自定义 stub 均可），直接断言 `countCompletedPlans()` 与 `countIncompletePlans()` 调用分析服务时使用的就是 `timeProvider.today()` 返回值。
2. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanViewTest.java` 中为 `StudyPlanView.from(...)` 增加依赖传递测试：使用 mock 的 `StudyPlanAnalysisService` 返回一个有辨识度的状态，并验证 `analyzeStatus(plan, currentDate)` 被以传入的 `plan` 和 `currentDate` 精确调用，且返回状态被原样投影到 `StudyPlanView.status()`。
3. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 的 `listStudyPlansWithQueryRejectsNullQuery()` 中补充消息断言，至少精确校验 `"query must not be null"`，使该公开失败路径与其余已补强的错误映射测试保持同一严格度。
