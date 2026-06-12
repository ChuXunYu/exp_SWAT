# 测试审查报告（v16 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/note/NoteQueryTest.java:37` — `byKeywordDelegatesToSearchPolicy()` 只验证了 mock 策略被调用，没有验证关键字匹配结果完全由 `NoteSearchPolicy.matchesKeyword(...)` 决定。当前断言使用的 `"review"` 本身命中笔记标题，`"missing"` 本身也不命中任何字段，因此如果 `NoteQuery.matches(...)` 错误地在策略之外额外实现标题/内容/标签匹配，并把本地匹配结果与策略结果组合，测试仍可能通过。设计契约明确要求 `NoteQuery.byKeyword(...)` 只通过 `NoteSearchPolicy.matchesKeyword(...)` 判断命中。
- **[一般]** `java-ai-assistant/src/test/java/assistant/note/NoteServiceTest.java:164`、`java-ai-assistant/src/test/java/assistant/note/NoteServiceTest.java:207` — 服务层查询测试均使用真实 `NoteSearchPolicy`，没有用可控策略验证 `NoteService` 确实把构造器注入的 `searchPolicy` 传入仓储查询路径。若实现错误地在 `listNotes(NoteQuery)` 或 `searchByKeyword(...)` 中新建 `NoteSearchPolicy`、绕过注入策略，或在服务层自行实现关键字过滤，只要自然匹配结果相同，现有测试仍会通过。设计契约要求 `NoteService` 依赖构造器注入的 `NoteSearchPolicy`，且所有关键字匹配经由此策略。

## 修改要求（仅 REJECTED 时）

- 在 `NoteQueryTest.byKeywordDelegatesToSearchPolicy()` 或新增测试中构造“笔记内容自然命中但 mock 策略返回 `false`”以及“笔记内容自然不命中但 mock 策略返回 `true`”的场景，断言 `NoteQuery.matches(...)` 完全跟随策略返回值，并继续验证传入策略的参数为规范化后的关键字。
- 在 `NoteServiceTest` 中为查询路径增加使用构造器注入 mock `NoteSearchPolicy` 的用例。至少覆盖 `listNotes(NoteQuery.byKeyword(...))` 或 `searchByKeyword(...)`：让 mock 策略返回与真实关键词匹配相反的结果，并断言服务结果跟随 mock 策略，同时验证策略被调用。这样可以防止服务层绕过注入策略或自行实现关键字匹配。
