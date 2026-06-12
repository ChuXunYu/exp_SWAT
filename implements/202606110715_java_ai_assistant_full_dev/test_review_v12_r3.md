# 测试审查报告（v12 r3）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java:145`, `:193`, `:294`, `:412`, `:427` — 服务层多个“返回 `StudyPlanView`”的公开方法只用真实 `StudyPlanAnalysisService` 校验最终状态值，没有锁定“必须把注入的 `analysisService` 传入视图投影”这一明确契约。当前若 `createStudyPlan(...)`、`getStudyPlan(...)`、`listStudyPlans()`、`updateStudyPlanDetails(...)`、`updateStudyPlanProgress(...)` 在内部复制状态推导逻辑或绕过注入依赖，只要结果恰好与真实分析服务一致，现有测试仍会通过。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java:214`, `:234` — `listStudyPlans(StudyPlanQuery)` 只校验筛选结果和日期调用次数，没有校验成功返回值仍然是不可修改列表。设计把“组合查询成功返回不可修改 `List<StudyPlanView>`”列为公开契约；当前即使实现回退为返回可变列表，测试也不会失败。

## 修改要求（仅 REJECTED 时）
1. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 中，为至少一个 `StudyPlanView` 返回路径增加基于 mock `StudyPlanAnalysisService` 的用例，显式断言服务层把注入的 `analysisService` 与同一个 `currentDate` 传入视图投影，而不是在服务内部复制状态逻辑。优先覆盖 `createStudyPlan(...)`、`getStudyPlan(...)`、`listStudyPlans()` 或更新接口中的代表性路径。
2. 在 `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 中补充 `listStudyPlans(StudyPlanQuery)` 成功结果的结构不可修改断言，例如对返回列表执行 `add/remove/clear` 并断言抛出 `UnsupportedOperationException`。
