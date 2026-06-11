# 测试报告（v7）

## 概述

已按详细设计 v7 和 `test_review_v7_r1.md` 的 REJECTED 反馈修订任务待办模块 JUnit Jupiter 单元测试。

测试继续围绕公开行为契约编写，不验证私有实现细节；本次修订重点补齐 `TaskItem` 两个公开创建入口在字段规范化和空白标题拒绝上的直接覆盖，避免完整构造器与 `createTodo(...)` 后续出现分叉实现时测试仍然通过。

## 测试文件

| 文件路径 | 覆盖内容 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/task/TaskPriorityTest.java` | 覆盖任务优先级枚举固定取值顺序、默认优先级、`valueOf` 和 `name` 稳定性。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskStatusTest.java` | 覆盖任务状态枚举固定取值顺序、完成状态语义、`valueOf` 和 `name` 稳定性。 |
| `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java` | 覆盖任务实体构造、工厂方法、字段规范化、非法输入、更新原子性、状态迁移、状态冲突和冲突后的状态保持。 |

## 针对审查反馈的修订

### 1. 完整构造器字段规范化覆盖不足

已在 `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java` 补充直接调用 `new TaskItem(...)` 的构造期规范化用例：

| 测试方法 | 覆盖契约 |
|---------|----------|
| `constructorNormalizesTitleAndDescription()` | 完整构造器对标题和描述首尾 ASCII/Unicode 空白执行 `strip()` 后保存。 |
| `constructorConvertsNullDescriptionToEmptyString()` | 完整构造器接收 `description == null` 时保存为 `""`。 |
| `constructorAllowsBlankDescriptionAsEmptyString()` | 完整构造器接收清理后为空的描述时合法，保存为 `""`。 |

这些断言均直接调用完整构造器，不再只通过 `TaskItem.createTodo(...)` 间接覆盖。

### 2. 纯空白标题拒绝矩阵不完整

已扩展 `rejectsBlankTitle()`，覆盖两个公开创建入口对以下标题输入均抛出 `IllegalArgumentException`：

| 输入类别 | 示例 | 覆盖入口 |
|---------|------|----------|
| 空字符串 | `""` | `new TaskItem(...)`、`TaskItem.createTodo(...)` |
| ASCII 纯空白 | `" \t\n"` | `new TaskItem(...)`、`TaskItem.createTodo(...)` |
| Unicode 纯空白 | `"\u2003"` | `new TaskItem(...)`、`TaskItem.createTodo(...)` |

## 已保留覆盖

- `TaskPriorityTest` 覆盖固定优先级顺序、默认优先级、`valueOf` 和 `name` 稳定性。
- `TaskStatusTest` 覆盖固定状态顺序、完成状态语义、`valueOf` 和 `name` 稳定性。
- `TaskItemTest` 保留构造器字段保存、`createTodo(...)` 初始状态、工厂方法规范化、内部空白保留、必填字段空值拒绝、`updateDetails(...)` 更新和原子性、完成/撤销完成状态迁移、重复状态迁移业务冲突及冲突后字段保持不变等覆盖。

## 验证说明

根据 verifier 指令，本环节只负责编写测试，不负责运行测试；未执行 `mvn test`。
