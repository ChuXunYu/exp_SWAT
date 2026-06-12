# 设计审查报告（v21 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** — `DraftImportServiceTest` 的任务回滚测试设计未满足任务文件对“无半写入”状态断言的硬性要求。`detail_v21.md` 第 171-172 行将中途失败和运行时异常回滚测试设计为使用 mock `TaskService`，只断言返回值、`deleteTask(...)` 调用和不调用后续创建；但 `task_v21.md` 第 69 行明确要求“回滚测试必须断言正式 `TaskService.listTasks()` 或 `StudyPlanService.listStudyPlans()` 的状态未出现半写入”。纯 mock 调用验证不能证明正式服务/仓储状态已经恢复，也不能覆盖删除失败、id 记录错误或回滚未真正清空正式任务列表等实现缺陷。

## 修改要求（仅 REJECTED 时）

- 修订 `DraftImportServiceTest` 的回滚测试设计，使任务创建失败和运行时异常两类路径都能断言正式任务状态未出现本次半写入。建议使用真实 `TaskService` + `InMemoryTaskRepository` + 可控失败的 `TaskRepository` 或其他 fake 依赖来制造“第一条保存成功、第二条失败/抛异常”的场景，然后通过 `TaskService.listTasks()` 断言本次已创建任务被删除且导入前已有任务不被删除。若仍保留 mock 用于调用顺序验证，也必须补充基于真实服务列表状态的断言，满足任务文件第 69 行要求。
