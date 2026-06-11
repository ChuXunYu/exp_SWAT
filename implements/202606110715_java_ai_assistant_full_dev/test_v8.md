# 测试报告（v8）

## 概述

已按详细设计 v8、实现报告 v8、`test_review_v8_r1.md` 和 `test_review_v8_r2.md` 的 REJECTED 意见修订任务查询、任务只读快照、任务仓储和任务服务的 JUnit Jupiter 单元测试。

测试基于公开行为契约编写，不断言私有辅助方法或内部集合实现；重点覆盖正常路径、边界条件、错误路径、状态迁移和服务返回只读快照边界。

## 测试文件

| 文件路径 | 覆盖内容 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/task/TaskQueryTest.java` | 覆盖查询条件构造、状态/优先级/截止日期筛选、组合筛选、空组件通配语义、筛选启用标记和空参数拒绝。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskViewTest.java` | 覆盖快照构造校验、字段原样保存、从实体复制、完成状态判断和实体后续变化不影响既有快照。 |
| `java-ai-assistant/src/test/java/assistant/task/InMemoryTaskRepositoryTest.java` | 覆盖保存、按编号查找、同编号替换、插入顺序、筛选、删除、空参数拒绝和不可修改列表快照。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java` | 覆盖任务服务创建、查看、列表、筛选、修改、删除、完成、撤销完成、错误转换、仓储不变性、只读返回边界和服务依赖校验。 |

## 审查反馈修订

| 测试方法 | 覆盖契约 |
|---------|----------|
| `TaskServiceTest.createTaskRejectsNullTitleAndDoesNotStoreTask()` | `createTask(...)` 遇到 `title == null` 时不抛出异常，返回 `VALIDATION_ERROR`，且仓储仍为空。 |
| `TaskServiceTest.updateTaskRejectsNullTitleAndKeepsStoredTaskUnchanged()` | `updateTask(...)` 遇到 `title == null` 时不抛出异常，返回 `VALIDATION_ERROR`，并保持原标题、描述、优先级、截止日期和状态不变。 |
| `TaskServiceTest.updateTaskPersistsChangesWhenRepositoryReturnsDetachedCopies()` | 使用测试专用 `CopyingTaskRepository` 保存和返回实体副本，验证 `updateTask(...)` 成功后再次查询能看到标题、描述、优先级、截止日期变更且状态保持不变；若服务省略 `repository.save(task)`，该用例会失败。 |
| `TaskServiceTest.markTaskCompletedPersistsStatusWhenRepositoryReturnsDetachedCopies()` | 使用测试专用 `CopyingTaskRepository` 验证 `markTaskCompleted(...)` 成功后再次查询能看到 `COMPLETED` 状态；若服务省略 `repository.save(task)`，该用例会失败。 |
| `TaskServiceTest.reopenTaskPersistsStatusWhenRepositoryReturnsDetachedCopies()` | 使用测试专用 `CopyingTaskRepository` 验证 `reopenTask(...)` 成功后再次查询能看到 `TODO` 状态；若服务省略 `repository.save(task)`，该用例会失败。 |
| `TaskServiceTest.missingTaskMutationsReturnNotFoundWithoutChangingExistingTask()` | 预置一条既有任务，对另一个不存在编号分别调用 `updateTask(...)`、`deleteTask(...)`、`markTaskCompleted(...)` 和 `reopenTask(...)`，断言均返回 `NOT_FOUND` 且既有任务的标题、描述、优先级、截止日期和状态保持不变。 |
| `TaskViewTest.constructorPreservesProvidedTitleAndDescriptionText()` | `TaskView` 构造器只校验标题非空白，保存带前后空白的标题和描述文本本身，不额外 `strip` 或规范化。 |

## 测试辅助对象

| 类型 | 用途 |
|------|------|
| `TaskServiceTest.CopyingTaskRepository` | 测试专用 `TaskRepository` fake；内部保存任务副本，`findById(...)`、`findAll()` 和 `findBy(...)` 返回新的副本，只有调用 `save(...)` 才会更新内部存储。该 fake 用于覆盖详细设计中“成功变更后必须调用 `repository.save(task)`，即使未来仓储返回非托管实体副本也能持久化变更”的服务层契约。 |

## 既有补充

| 测试方法 | 覆盖契约 |
|---------|----------|
| `TaskServiceTest.constructorRejectsNullDependencies()` | `TaskService` 构造器拒绝空仓储或空编号生成器。 |
| `TaskServiceTest.createTaskAllowsNullDescriptionAsEmptyString()` | 创建任务时 `description == null` 通过实体契约规范化为 `""`，并可通过服务再次读取到。 |
| `TaskServiceTest.updateTaskAllowsNullDescriptionAsEmptyStringAndPersistsChange()` | 修改任务时 `description == null` 规范化为 `""`，成功结果和再次查询结果一致。 |
| `TaskServiceTest.markTaskCompletedChangesTodoTaskToCompleted()` | 标记完成成功后，除返回快照为 `COMPLETED` 外，再次查询同一任务也为 `COMPLETED`。 |
| `TaskServiceTest.reopenTaskChangesCompletedTaskToTodo()` | 撤销完成成功后，除返回快照为 `TODO` 外，再次查询同一任务也为 `TODO`。 |
| `TaskServiceTest.returnedListSnapshotDoesNotChangeWhenStoredTasksChangeLater()` | 服务返回的 `List<TaskView>` 是只读快照；后续修改任务、状态迁移和新增任务不改变既有列表大小和既有快照字段。 |

## 已覆盖设计用例

- `TaskQueryTest` 覆盖设计规划中的全部查询条件用例，包括 `all()`、单条件筛选、组合筛选、空组件通配、筛选标记、单条件工厂空值拒绝和 `matches(null)` 拒绝。
- `TaskViewTest` 覆盖设计规划中的全部快照用例，包括构造器字段、带空白文本原样保存、必填字段空值拒绝、空白标题拒绝、`from(...)` 复制、`from(null)` 拒绝、完成状态语义和快照独立性。
- `InMemoryTaskRepositoryTest` 覆盖设计规划中的全部仓储用例，包括保存查找、缺失查找、同编号替换、插入顺序、不可修改快照、按状态/优先级/截止日期/组合筛选、删除和空参数拒绝。
- `TaskServiceTest` 覆盖设计规划中的服务用例，包括创建、查看、列表、筛选、修改、删除、完成、撤销完成、空标题/空优先级/空截止日期错误转换、`VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT`、只读列表、`TaskView` 快照边界、成功变更后的显式保存边界，以及不存在记录失败不影响既有任务。

## 验证说明

根据 verifier 指令，本环节只负责编写测试，不负责运行测试；未执行 `mvn test`。
