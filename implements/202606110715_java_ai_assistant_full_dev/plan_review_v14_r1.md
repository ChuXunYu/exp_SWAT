# 计划审查报告（v14 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** — `updateTransaction(EntityId id, TransactionType type, String amountText, String category, LocalDate date, String note)` 的 `type` 参数错误映射未收束。任务上下文明确要求拒绝“未知收支类型”，但服务接口边界只明确了 `id == null`、不存在编号、非法金额、空类别、空日期等映射，没有明确 `type == null` 或等价未知类型应由 `FinanceService` 捕获并返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。由于 `TransactionType` 是 enum，后续实现中“未知类型”最可能表现为 `null`；若不固定该契约，coder 可能让 `Objects.requireNonNull(type, "type")` 从实体层直接抛出运行时异常，导致服务层错误返回语义不稳定，也无法形成明确的白盒测试边界。

## 修改要求（仅 REJECTED 时）
- 明确 `updateTransaction(...)` 中 `type == null`（或唯一等价请求对象中的未知/空类型）必须映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不得向调用方泄漏 `NullPointerException` 或 `IllegalArgumentException`；同时要求补充对应单元测试，并验证失败后仓储状态不变。
