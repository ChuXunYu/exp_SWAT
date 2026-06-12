# 测试审查报告（v12 r5）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` — `listStudyPlansComputesStatusesWithInjectedCurrentDate()` 只断言返回状态值与 `today()` 调用次数，没有验证 `StudyPlanService.listStudyPlans()` 是否真的把动态状态投影委托给注入的 `analysisService`。设计明确要求服务层所有动态状态计算统一使用注入的 `StudyPlanAnalysisService`；如果实现回退为在 `listStudyPlans()` 中自行复制状态推导逻辑，或者内部 `new StudyPlanAnalysisService()` 绕过注入依赖，当前测试仍会通过，导致关键契约失守。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java` 中与 `listStudyPlans()` 相关的测试位置（建议新增独立用例，或强化 `listStudyPlansComputesStatusesWithInjectedCurrentDate()`）：
  问题是当前只校验结果，不校验委托路径；这会放过“结果偶然正确但依赖注入契约被破坏”的错误实现。
  为什么是问题：`detail_v12.md` 明确要求服务层统一通过注入的 `analysisService` 和 `timeProvider.today()` 计算动态状态，测试必须在该依赖被绕过时可靠失败。
  期望修正方向：为 `listStudyPlans()` 增加 mock `StudyPlanAnalysisService` 的断言，验证每个计划的状态投影都调用了注入分析器，并且传入的 `currentDate` 就是本次 `timeProvider.today()` 返回值；同时让 mock 返回值与默认状态分析逻辑可区分，避免复制逻辑的错误实现继续假通过。
