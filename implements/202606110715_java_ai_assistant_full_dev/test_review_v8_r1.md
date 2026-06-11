# 测试审查报告（v8 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java` — `TaskService` 的标题空值校验转换缺少测试覆盖。详细设计要求 `createTask(...)` 和 `updateTask(...)` 将标题非法等输入校验错误转换为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，且仓储状态不变；当前测试只覆盖空白标题，未覆盖 `title == null`。如果服务层遗漏 `NullPointerException` 捕获或对空标题直接抛异常，现有测试仍可能通过。
- **[轻微]** `java-ai-assistant/src/test/java/assistant/task/TaskViewTest.java` — `TaskView` 构造器“保存入参文本本身，不额外规范化”的细节没有用带前后空白的标题或描述验证；当前普通字符串用例无法区分“原样保存”和“被 strip 后保存”。

## 修改要求

- 在 `TaskServiceTest` 的创建任务校验区域补充 `createTaskRejectsNullTitleAndDoesNotStoreTask()`，调用 `createTask(null, "Scope", TaskPriority.MEDIUM, JUNE_30)`，断言返回 `VALIDATION_ERROR`、不抛出异常、仓储仍为空。
- 在 `TaskServiceTest` 的修改任务校验区域补充 `updateTaskRejectsNullTitleAndKeepsStoredTaskUnchanged()`，先创建任务，再调用 `updateTask(existingId, null, "Changed", TaskPriority.HIGH, JULY_15)`，断言返回 `VALIDATION_ERROR`，并通过 `getTask(existingId)` 验证原标题、描述、优先级、截止日期和状态均保持调用前值。
