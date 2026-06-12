# 测试审查报告（v12 r6）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — 创建与更新成功路径没有锁住“返回视图状态必须来自注入的 `StudyPlanAnalysisService`”这一契约。`createStudyPlanWithoutInitialProgressDefaultsToZeroAndReturnsView()`、`createStudyPlanAcceptsExplicitCompleteInitialProgress()`、`updateStudyPlanDetailsPersistsChangedFieldsAndReturnsView()`、`updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus()`、`updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus()` 都使用真实分析器并断言默认状态；如果实现退化为在这些写接口里直接新建分析器或内联状态判断，这些测试仍会通过，无法发现服务绕过注入依赖的问题。
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — `listStudyPlans(StudyPlanQuery)` 路径仍未验证“查询筛选和视图投影都复用注入的 `analysisService`”。现有 `listStudyPlansWithQueryFiltersByStatusPeriodAndCombination()` 与 `listStudyPlansWithQueryUsesOneCurrentDateForFilteringAndProjection()` 只用真实分析器检查结果集合与 `today()` 调用次数；如果实现只在仓储筛选阶段使用注入分析器，而在结果投影阶段复制状态逻辑或改用其他分析器，测试仍会通过，无法守住设计要求的统一委托边界。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`：在创建、修改详情、更新进度的成功用例附近补充或改造测试，使用 mock `StudyPlanAnalysisService` 返回与默认规则可区分的状态，并断言返回 `StudyPlanView.status()` 严格等于 mock 返回值，同时校验 `timeProvider.today()` 的使用符合设计。这样才能覆盖 `StudyPlanService.createStudyPlan(...)`、`updateStudyPlanDetails(...)`、`updateStudyPlanProgress(...)` 是否真的经由注入分析器完成视图投影。
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`：为 `listStudyPlans(StudyPlanQuery)` 增加针对注入分析器的委托测试。建议使用 mock `StudyPlanAnalysisService` 和固定 `CountingTimeProvider`，让筛选命中与投影视图状态都依赖 mock 返回值，并显式断言仓储筛选和 `StudyPlanView` 投影收到的是同一个 `currentDate`。这样才能防止查询路径在筛选与投影之间出现两套状态推导逻辑。
