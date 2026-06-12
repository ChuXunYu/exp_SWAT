# 测试审查报告（v12 r7）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — `updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus()` 与 `updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus()`（约第 519-563 行）只校验了返回 `StudyPlanView` 的进度与状态，没有校验仓储中的计划是否真的被持久化更新。由于 `InMemoryStudyPlanRepository.findById(...)` 返回的是实体副本，如果实现退化为“仅修改取出的副本并返回视图，但遗漏 `repository.save(plan)`”，这两条成功路径测试仍会通过，而“更新进度成功后必须持久化”的核心契约已经失效。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — 多个用例依赖 `CountingTimeProvider.todayCalls()` 证明服务“只调用一次 `today()` 并使用该日期”，但测试替身 `CountingTimeProvider.now()`（约第 835-838 行）内部直接调用了 `today()`。这使得相关断言无法区分实现到底调用的是设计要求的 `timeProvider.today()`，还是 `timeProvider.now().toLocalDate()`。因此，诸如创建、查询筛选和统计路径中“当前日期只能经由 `today()` 提供”的显式设计约束并未被测试真正锁住。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`：在 `updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus()` 和 `updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus()` 成功断言后，补充对仓储最终状态的检查，例如重新从 `repository.findById(new EntityId(100))` 读取并断言 `progress` 已分别变为 `0` / `100`。问题在于当前测试只能证明返回值正确，不能证明服务完成了持久化；修正方向是把“返回值正确”和“仓储状态已更新”同时锁住。
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`：重做 `CountingTimeProvider` 或新增专用测试替身，使 `today()` 与 `now()` 的调用可被独立计数并可返回可区分结果；随后把依赖“只调用一次 today”的用例改为同时断言 `today()` 调用次数符合预期且 `now()` 未被调用。问题在于当前替身把 `now()` 伪装成 `today()`，导致测试无法验证设计要求的 API 使用约束；修正方向是让两条时间读取路径在测试里可观测、可区分。
