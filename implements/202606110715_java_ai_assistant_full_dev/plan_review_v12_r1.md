# 计划审查报告（v12 r1）

## 审查结果
REJECTED

## 发现
- **[严重]** — 学习计划的状态筛选契约没有定义完整。`StudyPlanStatus` 是依赖 `StudyPlanAnalysisService` 和“当前日期”的动态状态，但 `task_v12.md` 同时要求 `StudyPlanQuery` 提供 `matches(...)`、`StudyPlanRepository` 提供 `findBy(...)`，却没有明确这两个接口如何拿到 `currentDate` / `TimeProvider.today()` / `StudyPlanAnalysisService`。这会让后续实现出现分叉：有人可能在 `query` 或仓储里直接调用 `LocalDate.now()`，有人可能复制一套状态判断逻辑，有人可能绕过 `repository.findBy(...)` 改成服务层手工筛选。现有 `schedule` 模块已经因为动态状态查询显式把 `currentDateTime` 传入仓储和 query，这里若不补清，学习计划模块很容易与既有模式脱节，并引入不可测的时间依赖。
- **[严重]** — 创建学习计划时“初始进度”的契约缺失。需求、OOD 和技术方案都明确提到创建计划时需要处理完成进度及其边界，且 AI 草稿导入也不能绕过进度校验；但 `task_v12.md` 只要求 `StudyPlanService` 实现“创建、修改、更新进度”，没有说明创建接口是否允许传入初始进度、默认是否强制为 `0`、以及创建时对 `0`、`100`、`-1`、`101` 的校验与错误映射。这样后续设计/编码很可能只做“创建默认 0，更新时才校验进度”，从而遗漏需求里的创建场景和后续草稿导入入口。

## 修改要求（仅 REJECTED 时）
- 对动态状态筛选补齐唯一且可执行的契约：明确 `StudyPlanQuery.matches(...)` 的签名和 `StudyPlanRepository.findBy(...)` 的职责边界，说明动态状态筛选到底是在仓储层还是服务层完成；如果在仓储层完成，必须显式传入 `StudyPlanAnalysisService` 与 `LocalDate currentDate`（或等价上下文）；如果在服务层完成，也要明确仓储只负责静态数据读取，避免实现者在 query/仓储中直接读取系统时间或复制状态推导逻辑。同时补充相应测试要求，验证状态筛选在固定日期下稳定可测。
- 对创建学习计划补齐初始进度规则：明确创建接口是否接收初始进度参数，默认行为是什么，哪些输入应映射为 `VALIDATION_ERROR`，并把 `0`、`100`、`-1`、`101` 的创建路径纳入本轮测试范围。还应说明该契约需要支持后续 AI 学习计划草稿导入复用，避免后续模块不得不绕开当前服务接口。
