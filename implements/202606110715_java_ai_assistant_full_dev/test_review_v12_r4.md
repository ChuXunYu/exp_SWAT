# 测试审查报告（v12 r4）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/InMemoryStudyPlanRepositoryTest.java` — `findBy(...)` 的测试只用真实 `StudyPlanAnalysisService` 验证筛选结果，没有锁定仓储必须使用调用方传入的 `analysisService` 与 `currentDate`。如果实现回退为在仓储内部自行 new 分析服务、复制状态推导逻辑，或绕过注入依赖但碰巧得到相同结果，现有测试仍会通过，覆盖不到设计中“基于调用方传入上下文筛选”的契约。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — `countMethodsPassInjectedCurrentDateToAnalysisService()` 把 `countCompletedPlans()` 和 `countIncompletePlans()` 合并到一次断言里，只校验两次调用后的总 `todayCalls()==2`。这不能分别证明“每个统计公开方法各自只读取一次 `timeProvider.today()`”；若一个方法错误地读取两次、另一个方法零次，总数仍然可能为 2，测试会误通过，未覆盖详细设计中该单方法级别契约。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/src/test/java/assistant/study/InMemoryStudyPlanRepositoryTest.java`
  在 `findBy` 相关用例附近补一个显式委托测试：使用 mock/stub `StudyPlanAnalysisService` 和固定 `currentDate`，让返回值明显不同于默认分析逻辑，并断言仓储筛选结果与该 mock 返回值一致，同时校验 `analyzeStatus(plan, currentDate)` 被按预期调用。这样才能锁定仓储没有绕过注入依赖或自行复制状态逻辑。
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`
  将统计方法的时间读取约束拆成可独立失败的断言：分别验证 `countCompletedPlans()` 与 `countIncompletePlans()` 各自单次调用时只读取一次 `timeProvider.today()`，并继续校验传给 `analysisService` 的 `currentDate` 是该次调用读取到的日期。不要只依赖两个方法合并后的总调用次数。
