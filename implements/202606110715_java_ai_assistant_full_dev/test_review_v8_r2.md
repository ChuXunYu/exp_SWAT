# 测试审查报告（v8 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java` — 服务层成功修改、标记完成和撤销完成的测试均只使用 `InMemoryTaskRepository`，无法验证详细设计要求的 `TaskService` 在成功变更后必须调用 `repository.save(task)`。当前内存仓储返回托管的同一个 `TaskItem` 实例，即使生产代码省略 `repository.save(task)`，现有 `updateTaskChangesEditableFieldsAndKeepsStatus()`、`markTaskCompletedChangesTodoTaskToCompleted()`、`reopenTaskChangesCompletedTaskToTodo()` 仍会通过，不能防止违反设计中的“即使未来仓储返回非托管实体副本也能持久化变更”契约。
- **[一般]** `java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java` — `NOT_FOUND` 路径大多在空仓储上断言错误码，没有验证“记录不存在失败不得改变仓储中已有任务状态或基础字段”的设计契约。例如 `updateTaskReturnsNotFoundForMissingTask()`、`deleteTaskReturnsNotFoundForMissingTask()`、`markTaskCompletedReturnsNotFoundForMissingTask()` 和 `reopenTaskReturnsNotFoundForMissingTask()` 都未先准备一条无关任务并在失败后确认其仍存在且字段不变。

## 修改要求（仅 REJECTED 时）

1. 在 `TaskServiceTest` 中补充可观察持久化边界的测试。建议加入一个测试专用 `TaskRepository` fake：内部保存任务副本，`findById`/`findAll`/`findBy` 返回新的副本，只有调用 `save` 才更新内部存储。用它分别覆盖 `updateTask(...)`、`markTaskCompleted(...)`、`reopenTask(...)` 成功后再次查询能看到变更，从而保证省略 `repository.save(task)` 的实现会失败。
2. 在 `TaskServiceTest` 的不存在记录路径中，先创建一条已有任务，再对另一个不存在的 `EntityId` 调用 `updateTask(...)`、`deleteTask(...)`、`markTaskCompleted(...)` 和 `reopenTask(...)`，断言返回 `NOT_FOUND` 后已有任务的标题、描述、优先级、截止日期和状态保持不变。可合并为一个聚焦“不存在记录不影响其他任务”的测试，也可分别增强现有测试方法。
