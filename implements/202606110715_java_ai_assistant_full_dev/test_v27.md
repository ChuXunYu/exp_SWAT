# 测试报告（v27）

## 测试文件

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 已编写 | 覆盖 `ConsoleApplication` 笔记子菜单入口、命令分发、增删改查、搜索筛选、标签解析、校验失败、服务失败、帮助、返回和 EOF 行为。 |

## 覆盖要点

- 主菜单命令 `6` 进入笔记子菜单，返回主菜单后其他命令仍可继续执行。
- 笔记列表、新增、查看、修改、删除的成功路径，并验证列表和详情输出格式。
- 关键字搜索、标签搜索、关键字与标签组合筛选。
- 组合筛选单字段场景委托等价搜索，双字段场景使用 `NoteQuery`。
- 逗号分隔标签解析、空标签列表、按展示名稳定输出标签。
- 使用 mock `NoteService` 捕获新增和修改传参，验证控制台层传给服务的标签集合保留原始大小写、按英文逗号拆分、丢弃空白片段、按首次出现顺序去重。
- 非法笔记 id、空关键字、空标签、空组合筛选在控制台层拦截且不调用 `NoteService`。
- 服务层校验失败后仍停留在笔记子菜单。
- 未知命令、空命令、帮助、返回主菜单和 EOF 稳定退出。
- 笔记列表超过 10 条时完整展示，不截断。

## 审查修订

- 采纳 `test_review_v27_r1.md` 的审查意见，在 `ConsoleApplicationTest` 新增 `noteMenuPassesParsedTagsToNoteServiceWithoutNormalizingCase()`。
- 新测试通过 `ArgumentCaptor<Set<String>>` 分别验证 `createNote(...)` 和 `updateNote(...)` 接收到的标签集合迭代顺序为 `["Study", "life", "study", "LIFE"]`。
- 保留原真实服务测试 `noteMenuCreatesEmptyTagListAndParsesCommaSeparatedTags()`，继续覆盖服务归一化后的展示结果和空标签展示。

## 验证

已执行：

```bash
mvn test -Dtest=ConsoleApplicationTest
```

结果：BUILD SUCCESS；`ConsoleApplicationTest` 运行 100 个测试，Failures: 0，Errors: 0，Skipped: 0。
