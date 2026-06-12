# 任务指令（v16）

## 动作
NEW

## 任务描述
新增个人笔记模块的查询条件、只读视图、仓储契约、内存仓储和应用服务，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/note/NoteQuery.java`
- `java-ai-assistant/src/main/java/assistant/note/NoteView.java`
- `java-ai-assistant/src/main/java/assistant/note/NoteRepository.java`
- `java-ai-assistant/src/main/java/assistant/note/InMemoryNoteRepository.java`
- `java-ai-assistant/src/main/java/assistant/note/NoteService.java`
- `java-ai-assistant/src/test/java/assistant/note/NoteQueryTest.java`
- `java-ai-assistant/src/test/java/assistant/note/NoteViewTest.java`
- `java-ai-assistant/src/test/java/assistant/note/InMemoryNoteRepositoryTest.java`
- `java-ai-assistant/src/test/java/assistant/note/NoteServiceTest.java`

本轮固定以下公开契约，避免实现分叉：

1. `NoteQuery` 表达可选关键字过滤、可选标签过滤和二者组合过滤；关键字过滤必须复用 `NoteSearchPolicy.matchesKeyword(...)`，标签过滤必须复用 `Note.hasTag(Tag)` / `Tag` 值语义，不在查询对象或服务层重新实现文本大小写折叠规则。
2. `NoteQuery` 工厂方法至少覆盖 `all()`、`byKeyword(String keyword)`、`byTag(Tag tag)`、`of(String keyword, Tag tag)`；`keyword == null` 或 `strip()` 后为空时拒绝，`tag == null` 时拒绝。`matches(Note note, NoteSearchPolicy searchPolicy)` 必须拒绝空 `note` 或空 `searchPolicy`。
3. `NoteView` 是只读 DTO/record，字段包括 `EntityId id`、`String title`、`String content`、`LocalDate createdDate`、`Set<Tag> tags`；`from(Note note)` 从实体投影视图；构造和投影必须复制标签集合并返回不可修改标签快照，调用方不能通过视图标签集合影响实体或仓储内部状态。
4. `NoteRepository` 固定包含 `save(Note note)`、`Optional<Note> findById(EntityId id)`、`List<Note> findAll()`、`List<Note> findBy(NoteQuery query, NoteSearchPolicy searchPolicy)`、`boolean deleteById(EntityId id)`。
5. `InMemoryNoteRepository` 使用 `LinkedHashMap<EntityId, Note>` 保持插入顺序；`save(...)` 必须保存调用参数的副本或等价隔离结果；`findById(...)`、`findAll()`、`findBy(...)` 均返回脱离内部存储状态的 `Note` 快照，列表为不可修改快照，禁止外部通过仓储返回值或保存后仍持有的对象引用绕过 `NoteService` 修改内部状态。
6. `NoteService` 构造依赖固定为 `NoteRepository`、`IdGenerator`、`TimeProvider`、`NoteSearchPolicy`。创建笔记必须使用 `idGenerator.nextId()` 生成编号、`timeProvider.today()` 作为创建日期。
7. `NoteService` 写接口固定接收原始输入：`createNote(String title, String content, Set<String> tagTexts)`、`updateNote(EntityId id, String title, String content, Set<String> tagTexts)`；服务内部负责把原始标签文本完整转换为 `Set<Tag>`，并将空标题、空内容、空标签集合引用、空标签元素、空白标签和重复归一标签等非法或可归一输入按既有 `Tag`/`Note` 语义处理。重复归一标签应去重并保持首次出现顺序；任何非法输入返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不得向调用方泄漏运行时异常，失败后仓储状态保持不变。
8. `NoteService` 查询接口至少覆盖 `getNote(EntityId id)`、`listNotes()`、`listNotes(NoteQuery query)`、`searchByKeyword(String keyword)`、`searchByTag(String tagText)`；成功载荷统一返回 `NoteView` 或不可修改的 `List<NoteView>`，不得返回内部 `Note` 引用。`id == null`、`query == null`、空关键字和非法标签文本必须映射为 `VALIDATION_ERROR`；无匹配返回成功的空列表。
9. `NoteService.deleteNote(EntityId id)` 固定返回 `OperationResult<Void>`；`id == null` 返回 `VALIDATION_ERROR`，不存在返回 `NOT_FOUND`，存在则删除并返回成功。修改不存在笔记返回 `NOT_FOUND`。

## 选择理由
任务、日程、学习计划和收支四个本地核心模块已形成服务闭环，v15 又完成了个人笔记的实体与搜索策略基础。补齐笔记查询、仓储和服务后，个人笔记核心管理能力才具备新增、查看、修改、删除、关键字查询和标签查询的完整可测入口，也能为后续汇总统计、AI 笔记摘要上下文和控制台入口提供只读快照 API。

## 任务上下文
需求要求个人笔记或日记管理支持新增、查看、修改、删除文本笔记；笔记包含标题、内容、创建日期和标签；支持按关键字或标签查询；需要处理空标题、空内容、关键字无匹配、关键字为空、修改不存在记录、删除不存在记录等情况。技术方案要求关键字查询由 `NoteSearchPolicy` 处理，关键字为空属于输入错误并由服务返回 `VALIDATION_ERROR`，无匹配返回空集合，不作为错误；标签查询按 `Tag` 的统一语义比较；AI 摘要由 AI 服务编排，笔记服务只提供本地笔记上下文，不直接调用外部 API。

普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。创建日期必须通过可注入 `TimeProvider.today()` 控制。

## 已有代码上下文
已完成的相关代码包括：

- `assistant.note.Note`：持有 `EntityId id`、标题、内容、`LocalDate createdDate` 和 `Set<Tag>`；构造、内容更新、标签替换和标签增删查已经集中维护不变量；`getTags()` 返回不可修改快照。
- `assistant.note.NoteSearchPolicy`：`matchesKeyword(Note note, String keyword)` 会拒绝空关键字，标题和内容使用 `Locale.ROOT` 大小写不敏感包含匹配，标签通过 `Tag.of(keyword)` 与笔记标签集合精确匹配。
- `assistant.common.Tag`：原始标签文本执行 `strip()` 和 `Locale.ROOT` 小写归一，空标签拒绝；`displayName()` 返回归一值。
- `assistant.common.EntityId`、`assistant.testability.IdGenerator`、`assistant.testability.TimeProvider`、`assistant.common.OperationResult`、`assistant.common.ErrorCode` 已在任务、日程、学习计划、收支模块中形成稳定服务层模式。
- 既有 `TaskService`、`FinanceService` 等服务成功查询和写操作只返回 View，仓储实现保存和返回实体副本；本轮笔记模块应沿用该边界，避免暴露可变领域实体。

测试需覆盖：

- `NoteQuery` 的 all、关键字、标签和组合匹配；空关键字、空标签、空 note、空 searchPolicy 拒绝；关键字匹配复用 `NoteSearchPolicy`。
- `NoteView` 的投影、标签复制、不可修改标签快照和空字段拒绝。
- `InMemoryNoteRepository` 的保存、同 ID 替换、插入顺序、按查询过滤、删除、空参数拒绝、保存后调用方修改不影响仓储、返回实体或列表后修改不影响仓储。
- `NoteService` 的依赖空值拒绝、创建使用可控编号和可控日期、原始标签文本转换与去重、查看/列表/关键字搜索/标签搜索/组合查询、修改成功、删除成功、空标题/空内容/空标签输入/空关键字/非法标签/id 为空/query 为空/不存在记录错误映射、无匹配成功返回空列表、所有失败路径保持仓储状态不变。
