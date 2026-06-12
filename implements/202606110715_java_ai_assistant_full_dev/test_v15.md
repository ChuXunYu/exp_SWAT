# 测试报告（v15）

## 概述

根据 `detail_v15.md` 的行为契约，为个人笔记模块补充并校准 JUnit Jupiter 单元测试。测试覆盖 `Note` 领域实体的构造校验、文本规范化、标签快照隔离、更新原子性、标签增删替换行为，以及 `NoteSearchPolicy` 的关键字校验、文本大小写不敏感匹配、标签精确匹配和 Locale.ROOT 语义。

## 测试文件变更

| 文件路径 | 操作 | 覆盖范围 |
|---------|------|----------|
| `java-ai-assistant/src/test/java/assistant/note/NoteTest.java` | 新增/修订 | 覆盖 `Note` 构造、工厂方法、字段规范化、异常消息、空标签集合、标签副本隔离、不可修改快照、标签迭代顺序、内容更新原子性、标签替换原子性、标签增删查。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteSearchPolicyTest.java` | 新增/修订 | 覆盖 `NoteSearchPolicy.matchesKeyword(...)` 空输入拒绝、空白关键字拒绝、异常消息、标题/内容大小写不敏感匹配、标签语义精确匹配、标签子串不匹配、无匹配返回 `false`、默认 Locale 不影响匹配、非法标签关键字不遮蔽文本匹配。 |

## 覆盖契约

### `NoteTest`

- 构造成功后保存编号、规范化标题、规范化内容、创建日期和去重标签集合。
- `Note.create(...)` 与公开构造器行为一致。
- `id`、`title`、`content`、`createdDate`、`tags` 为空时抛出带参数名消息的 `NullPointerException`。
- 标题或内容经 `strip()` 后为空时抛出带契约消息的 `IllegalArgumentException`。
- 标签集合包含 `null` 时抛出 `NullPointerException("tag")`。
- 空标签集合允许创建，返回标签快照不可修改。
- 构造器和 `replaceTags(...)` 均复制输入集合，调用方后续修改不影响实体状态。
- `getTags()` 返回不可修改快照，旧快照不随后续实体标签变更而变化，并保持当前 `LinkedHashSet` 迭代顺序。
- `updateContent(...)` 成功时只修改标题和内容；非法标题或内容会保持原标题和原内容不变。
- `replaceTags(...)` 遇到空集合引用或空标签元素时保持原标签集合不变。
- `addTag(...)`、`removeTag(...)`、`hasTag(...)` 使用 `Tag` 值语义，并拒绝空标签参数。

### `NoteSearchPolicyTest`

- `note` 或 `keyword` 为空时分别抛出带参数名消息的 `NullPointerException`。
- 关键字经 `strip()` 后为空时抛出 `IllegalArgumentException("keyword must not be blank")`。
- 标题和内容匹配使用大小写不敏感包含语义。
- 标签匹配复用 `Tag.of(...)` 归一语义，并且只做精确标签匹配，不做标签子串匹配。
- 关键字命中多个字段时返回 `true`；全部字段不命中时返回 `false`。
- 临时切换默认 Locale 为土耳其语后，文本匹配仍按 `Locale.ROOT` 语义工作。
- 关键字能命中文本字段时，即使标签集合不包含该标签也返回 `true`；标签构造失败路径不遮蔽文本匹配结果。

## 验证

已执行测试源码编译检查：

```bash
mvn -q -DskipTests test-compile
```

结果：通过。

按 verifier 角色约束，本轮只负责编写测试，不负责运行完整测试套件。

## 设计偏差

无测试侧设计偏差。测试均基于公开接口和行为契约编写，未断言私有方法或内部字段。
