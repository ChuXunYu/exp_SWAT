# 测试审查报告（v7 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java` — `TaskItem` 完整构造器的字段规范化契约覆盖不足。当前规范化相关测试主要通过 `TaskItem.createTodo(...)` 覆盖标题/描述 `strip()`、`null` 描述转空字符串和空白描述转空字符串，未独立断言公开完整构造器在这些输入下也满足同一契约。
- **[一般]** `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java` — 纯空白标题拒绝矩阵不完整。设计要求构造器和 `createTodo(...)` 都拒绝空字符串、ASCII 纯空白和 Unicode 纯空白标题；当前测试仅覆盖完整构造器拒绝空字符串，以及 `createTodo(...)` 拒绝 ASCII/Unicode 纯空白，未覆盖两个公开创建入口的全部必测输入类别。

## 修改要求
- 在 `java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java` 的构造/规范化测试附近补充完整构造器用例：标题和描述带首尾 Unicode/ASCII 空白时应保存为 `strip()` 后结果；描述为 `null` 时应保存为 `""`；描述清理后为空时应保存为 `""`。这些断言必须直接调用 `new TaskItem(...)`，不能只通过 `createTodo(...)` 间接覆盖。
- 扩展 `rejectsBlankTitle` 或拆分为参数化/辅助断言，确保 `new TaskItem(...)` 和 `TaskItem.createTodo(...)` 分别对空字符串、ASCII 纯空白、Unicode 纯空白标题抛出 `IllegalArgumentException`。这样才能防止两个公开创建入口出现分叉实现时测试仍然通过。
