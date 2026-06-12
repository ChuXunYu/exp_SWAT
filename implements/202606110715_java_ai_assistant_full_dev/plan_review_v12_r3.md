# 计划审查报告（v12 r3）

## 审查结果
REJECTED

## 发现
- **[严重]** `task_v12.md` 对 `StudyPlanService` 的成功返回契约自相矛盾。一方面要求“所有成功查询和写操作载荷必须返回 `StudyPlanView` 或不可修改的 `List<StudyPlanView>`”，另一方面又在同一文件中强制规定统计接口公开签名为 `OperationResult<Integer> countCompletedPlans()` 与 `OperationResult<Integer> countIncompletePlans()`。统计本身属于查询操作，这两条要求不能同时严格成立，后续 coder 无法判断通用返回规则是否应排除统计接口。
- **[一般]** 创建接口的初始进度契约仍未收束到唯一可执行路径。当前文本同时要求创建方法“接收 `Progress initialProgress` 或等价参数”，又要求对初始进度 `-1`、`101` 在服务边界映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。如果公开接口实际接收 `Progress`，非法数值通常会在调用方构造 `Progress` 前就失败，无法稳定落到服务层错误映射；如果公开接口接收原始整数，则又与“接收 `Progress initialProgress`”的表述不一致。该处仍会导致 coder 自行选择 API 形态。

## 修改要求（仅 REJECTED 时）
- 对返回契约冲突：明确“`StudyPlanView` / `List<StudyPlanView>` 规则”究竟适用于哪些服务方法。建议显式限定为创建、查看、列表、筛选、修改、更新进度这类返回计划快照的方法；删除维持 `OperationResult<Void>`，统计维持 `OperationResult<Integer>`，避免与固定统计签名冲突。
- 对初始进度契约：明确唯一的公开创建接口形态，以及非法初始进度由谁负责接收并转换。建议直接固定为接收原始 `Integer/int` 进度值并在服务内转换为 `Progress`，同时保留默认 0 的便捷重载；或者明确分成“外部服务接口接收原始数值、内部领域构造接收 `Progress`”两层，不再混用“`Progress` 或等价参数”的模糊表述。
