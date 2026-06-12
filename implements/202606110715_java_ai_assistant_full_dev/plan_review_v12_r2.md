# 计划审查报告（v12 r2）

## 审查结果
REJECTED

## 发现
- **[严重]** — 任务要求 `StudyPlanRepository.findAll()` 和 `findBy(...)` 返回“不可修改快照”，但没有明确“快照”是只保护集合外壳，还是像 `task` 模块修订后那样也要避免把仓储内部可变 `StudyPlan` 实体直接暴露给调用方。由于本轮同时要求 `StudyPlanService` 统计和查询链路把 `StudyPlanAnalysisService`、`currentDate` 传给 repository/query，后续汇总服务、AI 上下文或其他调用方极可能直接复用仓储接口；如果仓储返回的只是 `List.copyOf(values())`，调用方仍可拿到内部实体引用并绕过 `StudyPlanService.updateDetails/updateProgress(...)` 直接修改计划，破坏“服务作为唯一写入口”和“失败后仓储状态不变”的测试前提。当前任务只约束服务成功载荷返回 `StudyPlanView`，但没有约束仓储层对外暴露的可变性边界，计划仍存在实现分叉，足以误导后续设计与编码。
- **[一般]** — 任务要求 `StudyPlanService` “统计已完成数量和未完成数量”，并在动态状态链路中统一使用 `timeProvider.today()`，但没有把统计接口形态说清楚：是分别提供两个方法、还是返回聚合结果对象、是否需要同时提供无条件统计与按查询条件统计。该缺口会直接影响服务 API 与测试设计，因为后续汇总服务和 AI 上下文要复用这些统计能力；如果 coder 仅实现两个裸 `int` 方法而不定义查询上下文，就可能与后续“本周学习计划统计”演进冲突；如果自行新增统计 DTO，又超出了当前任务授权。当前计划没有给出唯一可执行的接口边界。

## 修改要求（仅 REJECTED 时）
1. 明确仓储只读边界：说明 `StudyPlanRepository.findAll()` / `findBy(...)` 返回值除了集合不可修改外，是否允许暴露内部 `StudyPlan` 引用；若不允许，应像服务层一样补充只读快照策略（例如新增 repository 仅供内部使用的约束，或要求外部协作统一经 `StudyPlanService` / `StudyPlanView` 完成），并把“外部拿到仓储查询结果不能绕过服务修改内部状态”写入任务和测试范围。
2. 明确统计接口契约：指定 `StudyPlanService` 完成/未完成统计的公开方法签名与返回形态，以及是否仅针对全部计划统计，还是需要接受 `StudyPlanQuery` / 日期范围等上下文。保证后续设计、编码和测试都围绕同一 API 展开，避免实现者自行扩展或遗漏。
