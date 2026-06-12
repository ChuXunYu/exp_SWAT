# 测试报告（v16）

## 概述

已根据 `detail_v16.md` 的行为契约核对并补充 `assistant.note` 包单元测试。测试覆盖查询条件、只读视图、内存仓储和应用服务的正常路径、边界条件、错误路径与快照隔离。

本轮根据 `test_review_v16_r1.md` 的审查意见，在既有 v16 测试基础上补强了关键字策略委托相关用例：

- `NoteQueryTest.byKeywordDelegatesToSearchPolicy()`：改为构造“自然命中但 mock 策略返回 false”和“自然不命中但 mock 策略返回 true”的反向场景，断言 `NoteQuery.matches(...)` 完全跟随 `NoteSearchPolicy.matchesKeyword(...)` 返回值，并验证传入策略的是规范化后的关键字。
- `NoteServiceTest.searchByKeywordUsesInjectedSearchPolicy()`：新增注入 mock `NoteSearchPolicy` 的服务层查询用例，让 mock 策略返回与真实关键字匹配相反的结果，断言 `searchByKeyword(...)` 结果跟随构造器注入策略并验证策略被调用。

此前已补强的服务层空结果、失败消息和列表快照行为仍保留：

- `createNoteReturnsStableValidationMessages()`：验证创建失败时返回稳定的 `VALIDATION_ERROR` 消息。
- `listNotesReturnsSuccessEmptyListWhenRepositoryIsEmpty()`：验证空仓储列表为成功空列表且不可修改。
- `listNotesWithQueryReturnsSuccessEmptyListWhenNoMatch()`：验证组合/过滤查询无匹配时返回成功空列表。
- `listNotesWithQueryReturnsSnapshotUnaffectedByLaterChanges()`：验证过滤列表和其中视图不受后续更新、新增影响。
- `getNoteReturnsViewOrNotFound()` 增强 `NOT_FOUND` 消息断言。

## 测试文件

| 文件路径 | 覆盖范围 |
|----------|----------|
| `java-ai-assistant/src/test/java/assistant/note/NoteQueryTest.java` | 查询工厂、关键字策略委托、标签语义、组合匹配、空参数和空白关键字拒绝。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteViewTest.java` | 视图投影、字段规范化、空参数/空白拒绝、标签复制和不可修改快照。 |
| `java-ai-assistant/src/test/java/assistant/note/InMemoryNoteRepositoryTest.java` | 保存、替换、插入顺序、查询、删除、空参数拒绝、保存和读取快照隔离。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteServiceTest.java` | 创建、查看、列表、条件查询、关键字查询、标签查询、修改、删除、错误映射、失败原子性和返回视图隔离。 |

## 验证命令

已执行 note 包定向测试：

```bash
mvn -q -Dtest='assistant.note.*Test' test
```

结果：通过。

已执行全量单元测试：

```bash
mvn test
```

结果：构建成功，测试通过。共执行 672 个测试，失败 0，错误 0，跳过 0。

## 结论

v16 设计要求的新增类型公开行为均已有对应单元测试覆盖；针对 r1 审查意见新增/修订的补强用例已验证 `NoteQuery` 和 `NoteService` 的关键字匹配路径确实使用注入策略。
