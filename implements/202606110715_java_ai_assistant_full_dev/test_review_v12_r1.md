# 测试审查报告（v12 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — 错误映射相关断言只检查了 `ErrorCode`，没有校验设计契约要求的错误消息内容。`assertFailure(...)`（约第 557-560 行）忽略了 `OperationResult.getMessage()`，因此 `getStudyPlanReturnsNotFoundForMissingPlan()`、`deleteStudyPlanReturnsNotFoundForMissingPlan()` 等用例即使实现返回了错误的 `"study plan not found: ..."` 文本也会通过；创建、修改、进度更新等校验失败路径即使丢失了底层异常消息、改成了泛化文案，也同样不会被发现。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — 单计划成功路径没有真正锁住“动态状态必须来自 `timeProvider.today()`”这一公开契约。`createStudyPlanWithoutInitialProgressDefaultsToZeroAndReturnsView()`（约第 35-52 行）、`getStudyPlanReturnsViewForExistingPlan()`（约第 132-139 行）、`updateStudyPlanDetailsPersistsChangedFieldsAndReturnsView()`（约第 276-293 行）、`updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus()` / `updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus()`（约第 379-400 行）要么选择了与当前真实日期可能得到相同状态的测试数据，要么根本没有断言返回视图状态，因此这些路径如果偷偷读取 `LocalDate.now()` 而不是注入的 `TimeProvider`，现有测试仍有漏检空间。

## 修改要求（仅 REJECTED 时）
1. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 的错误映射用例中，补充对 `OperationResult.getMessage()` 的断言；至少覆盖 `NOT_FOUND` 的精确消息 `"study plan not found: " + id.value()`，以及若干代表性的 `VALIDATION_ERROR` 场景，确认服务层确实透传了设计要求的异常消息。
2. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 中补强 `createStudyPlan(...)`、`getStudyPlan(...)`、`updateStudyPlanDetails(...)`、`updateStudyPlanProgress(...)` 的成功路径测试：使用与运行时真实日期明显不同、且会产生不同动态状态的固定日期，直接断言返回 `StudyPlanView.status()`；必要时引入计数型 `TimeProvider`，确认这些路径确实读取了 `timeProvider.today()`。
