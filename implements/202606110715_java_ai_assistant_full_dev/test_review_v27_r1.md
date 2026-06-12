# 测试审查报告（v27 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` — `noteMenuCreatesEmptyTagListAndParsesCommaSeparatedTags()` 只通过真实服务后的列表展示断言标签为 `study, life`，无法验证控制台层是否按详细设计把新增/修改标签解析为原始 `LinkedHashSet<String>` 后传给 `NoteService`。如果控制台层错误地在传参前做大小写归一化、改变顺序，或只依赖服务层归一化，该测试仍可能通过，不能覆盖“控制台层不得对新增/修改标签执行大小写归一，按英文逗号分割、strip、丢弃空白片段、保持首次出现顺序并去重后传给服务”的关键契约。

## 修改要求（仅 REJECTED 时）
- 在 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 的笔记标签解析相关测试中使用 mock `NoteService` 验证 `createNote(...)` 和/或 `updateNote(...)` 接收到的 `Set<String>` 参数。断言应能区分控制台层是否保留原始大小写、是否按逗号拆分、是否丢弃空白片段、是否按首次出现顺序去重；例如输入 ` Study, , life, study, LIFE ` 时，传给服务的集合应是按迭代顺序可验证的 `["Study", "life", "study", "LIFE"]`，而不是仅断言服务归一化后的展示文本。
