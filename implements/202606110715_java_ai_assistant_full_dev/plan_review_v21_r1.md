# 计划审查报告（v21 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** — `TASK_DRAFT` 的导入契约没有处理草稿任务 `dueDate` 可空但正式 `TaskService.createTask(...)` 必须传入非空截止日期的冲突。v20 已实现的 `TaskDraftItem` 明确允许 `dueDate == null`，解析器也会在任务 JSON 缺省 `dueDate` 时创建可确认草稿；但现有 `TaskItem` / `TaskView` 均要求 `dueDate` 非空，`TaskService.createTask(..., null)` 会返回 `VALIDATION_ERROR`。当前 v21 任务要求任务草稿逐项导入正式任务，却没有规定无截止日期草稿是导入前拒绝、自动填充某个日期、还是调整正式任务模型。因此同一个合法草稿可能在后续设计中被实现为失败、补默认日期或修改既有任务模型，行为口径不唯一，并且可能导致 AI 生成的缺省任务草稿无法导入。

- **[一般]** — `DraftImportService` 要求覆盖“未知或不匹配状态防御路径”，但没有给出可执行语义。当前 `SuggestionDraftType` 是封闭枚举，`SuggestionDraft` 工厂已保护类型与内容匹配；正常代码路径下无法构造未知类型或类型内容不匹配的草稿。若该防御路径指通过 mock/null/反射/未来枚举扩展触发，任务未明确应返回 `VALIDATION_ERROR`、`AI_MALFORMED_RESPONSE`、`SYSTEM_ERROR` 还是抛编程异常。这个测试要求会迫使设计者和测试者自行发明不可达场景与错误码，容易产生与服务边界不一致的实现。

## 修改要求（仅 REJECTED 时）

- 对 `TASK_DRAFT` 无截止日期导入给出唯一契约。建议在本轮任务中明确其中一种做法：要么要求导入时将任一 `TaskDraftItem.dueDate() == null` 视为草稿不满足正式任务约束，返回指定错误码且回滚已创建任务、草稿保持 `CONFIRMABLE`；要么明确一个不依赖真实当前时间的默认截止日期来源；要么将前置任务调整为正式任务支持可空截止日期。必须同步补充 `DraftImportServiceTest` 对该边界的断言。

- 删除或收束“未知或不匹配状态防御路径”的测试要求。若仍要求防御，应明确可达输入、触发方式和返回语义，例如 `importDraft(null)` 返回 `VALIDATION_ERROR` 或抛 `NullPointerException("draft")`，`STUDY_PLAN_DRAFT` 缺失内容在当前聚合根不可构造因此不列为本轮测试要求；不要要求测试正常类型系统下不可达的未知枚举或内容不匹配状态。
