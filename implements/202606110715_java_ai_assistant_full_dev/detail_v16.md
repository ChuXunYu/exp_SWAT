# 详细设计（v16）

## 概述

本轮设计目标是在 `assistant.note` 包中补齐个人笔记模块的查询条件、只读视图、仓储契约、内存仓储和应用服务，使笔记能力具备新增、查看、修改、删除、列表、关键字查询、标签查询和组合查询的完整应用层入口。

本轮实现范围：

- `NoteQuery`：表达全部、关键字、标签和关键字加标签组合过滤；关键字匹配只委托 `NoteSearchPolicy.matchesKeyword(...)`，标签过滤只委托 `Note.hasTag(Tag)`。
- `NoteView`：从 `Note` 投影不可变只读视图，并复制标签集合。
- `NoteRepository`：定义笔记仓储契约。
- `InMemoryNoteRepository`：基于 `LinkedHashMap<EntityId, Note>` 的内存仓储，保存和返回均使用实体快照隔离。
- `NoteService`：提供笔记 CRUD 与查询应用服务，统一返回 `OperationResult`，将输入校验异常转换为 `VALIDATION_ERROR`，并确保失败路径不改变仓储状态。
- 新增上述类型的 JUnit Jupiter 单元测试。

本轮不实现 AI 摘要、汇总统计、控制台入口或文件持久化。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/note/NoteQuery.java` | 新建 | 定义笔记查询条件和值语义匹配方法。 |
| `java-ai-assistant/src/main/java/assistant/note/NoteView.java` | 新建 | 定义笔记只读 DTO/record，隔离标签集合快照。 |
| `java-ai-assistant/src/main/java/assistant/note/NoteRepository.java` | 新建 | 定义笔记仓储接口。 |
| `java-ai-assistant/src/main/java/assistant/note/InMemoryNoteRepository.java` | 新建 | 实现单线程内存笔记仓储，保持插入顺序和实体快照隔离。 |
| `java-ai-assistant/src/main/java/assistant/note/NoteService.java` | 新建 | 实现笔记应用服务，负责输入转换、编号/日期注入、错误映射和视图返回。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteQueryTest.java` | 新建 | 覆盖查询工厂、匹配组合、空输入拒绝和关键字策略委托。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteViewTest.java` | 新建 | 覆盖视图投影、构造校验、标签复制和不可修改快照。 |
| `java-ai-assistant/src/test/java/assistant/note/InMemoryNoteRepositoryTest.java` | 新建 | 覆盖保存、替换、顺序、查询、删除、空参数拒绝和快照隔离。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteServiceTest.java` | 新建 | 覆盖创建、查看、列表、查询、修改、删除、错误映射和失败原子性。 |

## 类型定义

### `NoteQuery`

**形态**：`record`

**包路径**：`assistant.note`

**职责**：表达笔记关键字过滤、标签过滤和二者组合过滤，并以组合谓词判断 `Note` 是否匹配。

**类型签名定义**：`public record NoteQuery(String keyword, Tag tag)`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `String keyword` | 可为 `null` 表示无关键字过滤；非空时在规范构造器中执行 `strip()`，规范化后不得为空。 |
| `Tag tag` | 可为 `null` 表示无标签过滤；非空时按 `Tag` 值语义比较。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public NoteQuery` 规范构造器 | `NoteQuery` | `keyword != null` 时执行 `strip()`；`strip()` 后为空抛出 `IllegalArgumentException("keyword must not be blank")`；`tag` 可为 `null`。 |
| `public static NoteQuery all()` | `NoteQuery` | 返回无任何过滤条件的查询。 |
| `public static NoteQuery byKeyword(String keyword)` | `NoteQuery` | `keyword == null` 抛出 `NullPointerException("keyword")`；空白关键字由规范构造器拒绝。 |
| `public static NoteQuery byTag(Tag tag)` | `NoteQuery` | `tag == null` 抛出 `NullPointerException("tag")`。 |
| `public static NoteQuery of(String keyword, Tag tag)` | `NoteQuery` | `keyword == null` 抛出 `NullPointerException("keyword")`；`tag == null` 抛出 `NullPointerException("tag")`；创建组合查询。 |
| `public boolean hasKeywordFilter()` | `boolean` | `keyword != null` 返回 `true`。 |
| `public boolean hasTagFilter()` | `boolean` | `tag != null` 返回 `true`。 |
| `public boolean matches(Note note, NoteSearchPolicy searchPolicy)` | `boolean` | `note == null` 抛出 `NullPointerException("note")`；`searchPolicy == null` 抛出 `NullPointerException("searchPolicy")`；无关键字过滤或 `searchPolicy.matchesKeyword(note, keyword)` 为真，且无标签过滤或 `note.hasTag(tag)` 为真时返回 `true`。 |

**构造方式**：

- 调用方优先使用 `all()`、`byKeyword(...)`、`byTag(...)`、`of(...)`。
- 服务层的 `searchByKeyword(...)` 使用 `byKeyword(...)`；`searchByTag(...)` 先构造 `Tag`，再使用 `byTag(...)`。

**类型关系**：

- 依赖 `assistant.common.Tag`、`assistant.note.Note`、`assistant.note.NoteSearchPolicy`、`java.util.Objects`。
- 被 `NoteRepository.findBy(...)` 和 `NoteService.listNotes(NoteQuery)` 使用。

### `NoteView`

**形态**：`record`

**包路径**：`assistant.note`

**职责**：作为笔记对服务调用方暴露的只读 DTO，避免返回可变领域实体。

**类型签名定义**：`public record NoteView(EntityId id, String title, String content, LocalDate createdDate, Set<Tag> tags)`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `EntityId id` | 非空。 |
| `String title` | 非空；执行 `strip()`；规范化后不得为空。 |
| `String content` | 非空；执行 `strip()`；规范化后不得为空。 |
| `LocalDate createdDate` | 非空。 |
| `Set<Tag> tags` | 非空；元素非空；构造时复制为 `LinkedHashSet` 后再用 `Collections.unmodifiableSet(...)` 包装。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public NoteView` 规范构造器 | `NoteView` | `id`、`title`、`content`、`createdDate`、`tags` 为空分别抛出 `NullPointerException`，消息为参数名；标题或内容经 `strip()` 后为空分别抛出 `IllegalArgumentException("title must not be blank")`、`IllegalArgumentException("content must not be blank")`；任一标签元素为空抛出 `NullPointerException("tag")`；字段 `tags` 保存不可修改的 `LinkedHashSet` 副本。 |
| `public static NoteView from(Note note)` | `NoteView` | `note == null` 抛出 `NullPointerException("note")`；从 `Note` getter 投影视图，标签通过构造器再次复制。 |

**构造方式**：

- 服务层只通过 `NoteView.from(note)` 生成返回载荷。
- 直接构造仅用于测试或未来汇总模块构造只读数据。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.Tag`、`java.time.LocalDate`、`java.util.Set`、`java.util.LinkedHashSet`、`java.util.Collections`、`java.util.Objects`。
- 由 `NoteService` 对外返回，不反向依赖仓储或服务。

### `NoteRepository`

**形态**：`interface`

**包路径**：`assistant.note`

**职责**：定义笔记数据访问边界，隔离服务层与内存存储实现。

**类型签名定义**：`public interface NoteRepository`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `void save(Note note);` | `void` | 保存或按相同 `EntityId` 替换笔记；实现应拒绝空参数。 |
| `Optional<Note> findById(EntityId id);` | `Optional<Note>` | 按编号查找；实现应拒绝空 `id`。 |
| `List<Note> findAll();` | `List<Note>` | 返回当前全部笔记快照，保持仓储定义的顺序。 |
| `List<Note> findBy(NoteQuery query, NoteSearchPolicy searchPolicy);` | `List<Note>` | 根据查询条件与搜索策略过滤；实现应拒绝空 `query` 或空 `searchPolicy`。 |
| `boolean deleteById(EntityId id);` | `boolean` | 删除存在编号返回 `true`；不存在返回 `false`；实现应拒绝空 `id`。 |

**构造方式**：

- 接口不能实例化；默认实现为 `InMemoryNoteRepository`。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`java.util.Optional`、`java.util.List`。
- 被 `NoteService` 依赖；未来文件仓储可实现同一接口。

### `InMemoryNoteRepository`

**形态**：`final class`

**包路径**：`assistant.note`

**职责**：提供单线程、可测试的进程内笔记仓储，实现保存和返回实体的快照隔离。

**类型签名定义**：`public final class InMemoryNoteRepository implements NoteRepository`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final Map<EntityId, Note> notes = new LinkedHashMap<>();` | 按首次插入顺序保存笔记；相同 `EntityId` 替换值但不改变已有键的顺序。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public void save(Note note)` | `void` | `note == null` 抛出 `NullPointerException("note")`；通过 `copyOf(note)` 保存副本或等价隔离结果，调用方保存后继续修改原对象不得影响仓储内部状态。 |
| `public Optional<Note> findById(EntityId id)` | `Optional<Note>` | `id == null` 抛出 `NullPointerException("id")`；存在时返回内部对象副本；不存在返回 `Optional.empty()`。 |
| `public List<Note> findAll()` | `List<Note>` | 按插入顺序返回每个内部对象的副本；返回列表为不可修改快照。 |
| `public List<Note> findBy(NoteQuery query, NoteSearchPolicy searchPolicy)` | `List<Note>` | `query == null` 抛出 `NullPointerException("query")`；`searchPolicy == null` 抛出 `NullPointerException("searchPolicy")`；在内部存储对象上调用 `query.matches(note, searchPolicy)` 过滤，再返回匹配对象副本；返回列表为不可修改快照。 |
| `public boolean deleteById(EntityId id)` | `boolean` | `id == null` 抛出 `NullPointerException("id")`；存在并删除返回 `true`，不存在返回 `false`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static Note copyOf(Note source)` | `Note` | `source == null` 抛出 `NullPointerException("source")`；调用 `new Note(source.getId(), source.getTitle(), source.getContent(), source.getCreatedDate(), source.getTags())` 重建实体。 |
| `private static List<Note> copyList(Collection<Note> sources)` | `List<Note>` | 将来源集合逐一 `copyOf(...)` 后使用 `toList()` 返回不可修改列表。 |

**构造方式**：

- 通过 `new InMemoryNoteRepository()` 创建空仓储。
- 不支持多线程并发写入；满足本项目单用户命令行和单元测试场景。

**类型关系**：

- 实现 `NoteRepository`。
- 依赖 `assistant.common.EntityId`、`java.util.LinkedHashMap`、`java.util.Map`、`java.util.Optional`、`java.util.List`、`java.util.Collection`、`java.util.Objects`。

### `NoteService`

**形态**：`final class`

**包路径**：`assistant.note`

**职责**：提供笔记应用用例入口，负责把原始输入转换为领域对象和值对象，调用仓储并返回只读视图或明确失败结果。

**类型签名定义**：`public final class NoteService`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final NoteRepository repository;` | 构造时非空。 |
| `private final IdGenerator idGenerator;` | 构造时非空；创建笔记时调用 `nextId()`。 |
| `private final TimeProvider timeProvider;` | 构造时非空；创建笔记时调用 `today()`。 |
| `private final NoteSearchPolicy searchPolicy;` | 构造时非空；所有关键字匹配经由此策略。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public NoteService(NoteRepository repository, IdGenerator idGenerator, TimeProvider timeProvider, NoteSearchPolicy searchPolicy)` | `NoteService` | 任一依赖为空抛出 `NullPointerException`，消息为参数名。 |
| `public OperationResult<NoteView> createNote(String title, String content, Set<String> tagTexts)` | `OperationResult<NoteView>` | 先通过 `toTags(tagTexts)` 完整转换原始标签集合，再调用 `Note.create(idGenerator.nextId(), title, content, timeProvider.today(), tags)`；成功后保存并返回 `NoteView`；捕获 `NullPointerException`、`IllegalArgumentException` 并返回 `VALIDATION_ERROR`；失败不得保存任何笔记。 |
| `public OperationResult<NoteView> updateNote(EntityId id, String title, String content, Set<String> tagTexts)` | `OperationResult<NoteView>` | `id == null` 返回 `VALIDATION_ERROR`；不存在返回 `NOT_FOUND`；存在时先完整转换和校验标题、内容、标签，再更新副本并保存；成功返回更新后视图；任一输入非法返回 `VALIDATION_ERROR` 且仓储状态不变。 |
| `public OperationResult<NoteView> getNote(EntityId id)` | `OperationResult<NoteView>` | `id == null` 返回 `VALIDATION_ERROR`；存在返回视图；不存在返回 `NOT_FOUND`。 |
| `public OperationResult<List<NoteView>> listNotes()` | `OperationResult<List<NoteView>>` | 返回当前全部笔记视图列表；列表为不可修改快照；空仓储返回成功空列表。 |
| `public OperationResult<List<NoteView>> listNotes(NoteQuery query)` | `OperationResult<List<NoteView>>` | `query == null` 返回 `VALIDATION_ERROR`；否则调用 `repository.findBy(query, searchPolicy)` 并返回视图列表；无匹配返回成功空列表。 |
| `public OperationResult<List<NoteView>> searchByKeyword(String keyword)` | `OperationResult<List<NoteView>>` | 使用 `NoteQuery.byKeyword(keyword)` 构造查询；空引用或空白关键字映射为 `VALIDATION_ERROR`；无匹配返回成功空列表。 |
| `public OperationResult<List<NoteView>> searchByTag(String tagText)` | `OperationResult<List<NoteView>>` | 使用 `Tag.of(tagText)` 构造标签，再用 `NoteQuery.byTag(tag)` 查询；空引用、空白或非法标签文本映射为 `VALIDATION_ERROR`；无匹配返回成功空列表。 |
| `public OperationResult<Void> deleteNote(EntityId id)` | `OperationResult<Void>` | `id == null` 返回 `VALIDATION_ERROR`；`repository.deleteById(id)` 为 `false` 返回 `NOT_FOUND`；存在并删除返回 `OperationResult.success()`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private Set<Tag> toTags(Set<String> tagTexts)` | `Set<Tag>` | `tagTexts == null` 抛出 `NullPointerException("tagTexts")`；按输入迭代顺序遍历，每个元素用 `Tag.of(tagText)` 转换；空元素抛出 `NullPointerException("tagText")`；空白标签由 `Tag` 抛出 `IllegalArgumentException`；重复归一标签通过 `LinkedHashSet` 去重并保持首次出现顺序；返回不可修改或仅内部使用的 `LinkedHashSet` 副本均可，但不得返回调用方集合。 |
| `private static NoteView toView(Note note)` | `NoteView` | 委托 `NoteView.from(note)`。 |
| `private static List<NoteView> toUnmodifiableViews(List<Note> notes)` | `List<NoteView>` | 将笔记列表投影为视图并用 `Stream.toList()` 返回不可修改列表。 |
| `private OperationResult<NoteView> validationFailure(String message)` | `OperationResult<NoteView>` | 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, stableMessage(message))`。 |
| `private OperationResult<List<NoteView>> validationFailureList(String message)` | `OperationResult<List<NoteView>>` | 返回列表型校验失败结果。 |
| `private OperationResult<Void> validationFailureVoid(String message)` | `OperationResult<Void>` | 返回空载荷校验失败结果。 |
| `private OperationResult<NoteView> notFound(EntityId id)` | `OperationResult<NoteView>` | 返回 `OperationResult.failure(ErrorCode.NOT_FOUND, "note not found: " + id.value())`。 |
| `private OperationResult<Void> notFoundVoid(EntityId id)` | `OperationResult<Void>` | 返回空载荷未找到结果。 |
| `private static String stableMessage(String message)` | `String` | 若异常消息为空或空白，返回 `"invalid note input"`；否则返回原消息，避免 `OperationResult.failure(...)` 因空消息再次抛异常。 |

**构造方式**：

- 生产装配使用 `new NoteService(new InMemoryNoteRepository(), idGenerator, timeProvider, new NoteSearchPolicy())`。
- 测试可传入固定编号生成器、固定时间提供者和空仓储。

**类型关系**：

- 依赖 `NoteRepository`、`IdGenerator`、`TimeProvider`、`NoteSearchPolicy`。
- 依赖通用类型 `EntityId`、`Tag`、`OperationResult`、`ErrorCode`。
- 对外仅暴露 `NoteView` 和 `List<NoteView>`，不暴露 `Note`。

## 错误处理

- `NoteQuery`、`NoteView`、`InMemoryNoteRepository` 属于领域/基础设施边界，空引用参数使用 `NullPointerException`，空白文本使用 `IllegalArgumentException`，与既有模块风格保持一致。
- `NoteService` 是应用服务边界，必须捕获来自 `Tag`、`Note`、`NoteQuery` 和 `NoteView` 的 `NullPointerException`、`IllegalArgumentException`，并转换为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。
- `NoteService` 不捕获仓储实现的不可预期运行时异常；本轮内存仓储不设计系统错误路径。
- `id == null`、`query == null`、空关键字、非法标签文本、空标签集合引用、空标签元素、空标题和空内容均返回 `VALIDATION_ERROR`。
- 查询无匹配属于正常结果，返回成功的空列表。
- `getNote(...)`、`updateNote(...)`、`deleteNote(...)` 对不存在编号返回 `NOT_FOUND`。
- 修改路径必须先完成所有新输入校验，再修改从仓储取回的实体副本并保存，保证校验失败时仓储状态保持不变。

## 行为契约

- `NoteQuery.all()` 匹配所有非空笔记，不调用关键字策略或标签判断。
- `NoteQuery.byKeyword(...)` 只通过 `NoteSearchPolicy.matchesKeyword(...)` 判断关键字命中，不在查询对象中重新实现标题、内容或标签关键字规则。
- `NoteQuery.byTag(...)` 只通过 `Note.hasTag(Tag)` 判断标签命中，不在查询对象或服务层重新实现 `Tag` 大小写折叠语义。
- `NoteQuery.of(...)` 同时满足关键字和标签过滤才匹配。
- `NoteView` 构造和投影都复制标签集合；修改原始集合、实体标签或尝试修改视图标签集合均不能改变视图状态。
- `InMemoryNoteRepository` 保存时复制传入实体；保存后调用方继续调用 `updateContent(...)`、`replaceTags(...)`、`addTag(...)` 或 `removeTag(...)` 不影响仓储。
- `InMemoryNoteRepository.findById(...)`、`findAll()`、`findBy(...)` 返回的实体均为副本；调用方修改返回实体不影响仓储。
- `InMemoryNoteRepository.findAll()` 和 `findBy(...)` 返回列表不可修改，且保持 `LinkedHashMap` 插入顺序；相同编号替换不改变该编号原有位置。
- `NoteService.createNote(...)` 的创建日期必须来自 `timeProvider.today()`，编号必须来自 `idGenerator.nextId()`。
- `NoteService` 将原始标签文本转换为 `Tag` 时保持首次出现顺序，并按 `Tag.equals(...)` 去重。
- `NoteService` 任意成功查询返回的 `List<NoteView>` 不可修改；调用方不能通过返回值影响仓储或实体。
- `NoteService` 不调用外部 AI、网络、文件系统或真实当前日期。

## 依赖关系

- `assistant.note.NoteQuery` 依赖 `Note`、`NoteSearchPolicy` 和 `Tag`。
- `assistant.note.NoteView` 依赖 `Note`、`EntityId`、`Tag` 和 `LocalDate`。
- `assistant.note.NoteRepository` 依赖 `Note`、`NoteQuery`、`NoteSearchPolicy` 和 `EntityId`。
- `assistant.note.InMemoryNoteRepository` 实现 `NoteRepository`，仅依赖 Java 集合和笔记领域对象。
- `assistant.note.NoteService` 依赖 `NoteRepository`、`IdGenerator`、`TimeProvider`、`NoteSearchPolicy`、`OperationResult`、`ErrorCode` 和通用值对象。
- 本轮测试仅依赖 JUnit Jupiter、Mockito（用于验证 `NoteQuery` 策略委托时可选）、Java 标准库和项目内测试友好接口，不依赖真实当前时间、网络、API Key 或外部文件。

## 测试设计

### `NoteQueryTest`

| 测试方法 | 覆盖契约 |
|----------|----------|
| `allMatchesEveryNoteWithoutDelegatingKeywordPolicy()` | `all()` 无过滤匹配所有笔记，并不调用关键字策略。 |
| `byKeywordDelegatesToSearchPolicy()` | 关键字过滤调用 `NoteSearchPolicy.matchesKeyword(note, keyword)` 决定结果。 |
| `byTagUsesNoteTagSemantics()` | 标签过滤通过 `Note.hasTag(Tag)` 使用 `Tag` 值语义匹配。 |
| `combinedQueryRequiresKeywordAndTag()` | 组合查询必须同时满足关键字和标签。 |
| `factoryMethodsRejectNullRequiredArguments()` | `byKeyword(null)`、`byTag(null)`、`of(null, tag)`、`of(keyword, null)` 拒绝空参数。 |
| `constructorRejectsBlankKeyword()` | 空字符串、普通空白或 Unicode 空白关键字抛出 `IllegalArgumentException`。 |
| `matchesRejectsNullNoteAndSearchPolicy()` | `matches(null, policy)` 和 `matches(note, null)` 抛出 `NullPointerException`。 |

### `NoteViewTest`

| 测试方法 | 覆盖契约 |
|----------|----------|
| `fromProjectsNoteFields()` | `from(Note)` 正确投影编号、标题、内容、创建日期和标签。 |
| `constructorNormalizesTitleAndContent()` | 直接构造视图时标题和内容执行 `strip()`。 |
| `constructorRejectsNullRequiredFields()` | `id`、`title`、`content`、`createdDate`、`tags` 为空分别拒绝。 |
| `constructorRejectsBlankTitleAndContent()` | 空白标题或内容抛出 `IllegalArgumentException`。 |
| `constructorRejectsNullTagElement()` | 标签集合包含空元素时抛出 `NullPointerException("tag")`。 |
| `tagsAreUnmodifiableSnapshot()` | 视图标签集合不可修改，构造后修改输入集合不影响视图。 |
| `fromCopiesTagsFromEntity()` | 投影后修改实体标签不影响已有视图。 |
| `fromRejectsNullNote()` | `from(null)` 抛出 `NullPointerException("note")`。 |

### `InMemoryNoteRepositoryTest`

| 测试方法 | 覆盖契约 |
|----------|----------|
| `saveAndFindByIdReturnsDetachedSnapshot()` | 保存后按编号查找成功，返回对象不是原对象。 |
| `saveCopiesInputNoteSoLaterCallerMutationsDoNotAffectRepository()` | 保存后修改原对象标题、内容或标签不影响仓储状态。 |
| `findByIdReturnsEmptyWhenNoteDoesNotExist()` | 不存在编号返回空 `Optional`。 |
| `saveReplacesNoteWithSameIdAndKeepsInsertionOrder()` | 相同编号替换内容但保持首次插入顺序。 |
| `findAllReturnsNotesInInsertionOrder()` | `findAll()` 保持插入顺序。 |
| `findAllReturnsUnmodifiableDetachedSnapshotList()` | 列表不可修改，修改返回实体不影响仓储。 |
| `findByFiltersUsingQueryAndSearchPolicy()` | `findBy(...)` 按查询和策略过滤。 |
| `findByAppliesCombinedQueryInInsertionOrder()` | 组合查询结果保持插入顺序。 |
| `findByReturnsUnmodifiableDetachedSnapshotList()` | 查询列表不可修改，修改返回实体不影响仓储。 |
| `mutatingNoteReturnedFromFindByIdDoesNotAffectStoredState()` | 修改 `findById` 返回实体不影响仓储。 |
| `deleteByIdRemovesExistingNote()` | 删除存在编号返回 `true` 并移除记录。 |
| `deleteByIdReturnsFalseWhenNoteDoesNotExist()` | 删除不存在编号返回 `false`。 |
| `methodsRejectNullArguments()` | `save`、`findById`、`findBy`、`deleteById` 的空参数抛出 `NullPointerException`。 |

### `NoteServiceTest`

| 测试方法 | 覆盖契约 |
|----------|----------|
| `constructorRejectsNullDependencies()` | 四个构造依赖为空时分别抛出 `NullPointerException`。 |
| `createNoteUsesGeneratedIdAndCurrentDate()` | 创建使用 `idGenerator.nextId()` 和 `timeProvider.today()`，返回视图。 |
| `createNoteConvertsTagsDeduplicatesAndKeepsFirstOrder()` | 原始标签文本转换为 `Tag`，重复归一标签去重并保持首次出现顺序。 |
| `createNoteReturnsValidationErrorForInvalidInputsAndDoesNotSave()` | 空标题、空内容、空标签集合引用、空标签元素、空白标签均返回 `VALIDATION_ERROR` 且仓储不变。 |
| `getNoteReturnsViewOrNotFound()` | 存在返回视图，不存在返回 `NOT_FOUND`。 |
| `getNoteRejectsNullId()` | 空编号返回 `VALIDATION_ERROR`。 |
| `listNotesReturnsUnmodifiableViewsInRepositoryOrder()` | 全量列表返回不可修改视图列表且保持顺序。 |
| `listNotesWithQueryFiltersUsingSearchPolicy()` | 组合查询返回匹配视图。 |
| `listNotesRejectsNullQuery()` | 空查询返回 `VALIDATION_ERROR`。 |
| `searchByKeywordReturnsMatchesAndEmptyListWhenNoMatch()` | 关键字查询命中返回结果，无匹配返回成功空列表。 |
| `searchByKeywordRejectsNullOrBlankKeyword()` | 空引用或空白关键字返回 `VALIDATION_ERROR`。 |
| `searchByTagUsesTagSemantics()` | 标签查询通过 `Tag` 语义命中大小写或空白不同的标签。 |
| `searchByTagRejectsInvalidTagText()` | 空引用或空白标签文本返回 `VALIDATION_ERROR`。 |
| `updateNoteChangesContentAndTags()` | 修改存在笔记成功更新标题、内容和标签并返回视图。 |
| `updateNoteReturnsNotFoundForMissingId()` | 修改不存在编号返回 `NOT_FOUND`。 |
| `updateNoteRejectsInvalidInputsAndKeepsStoredState()` | 空编号、空标题、空内容、空标签集合引用、空标签元素、空白标签返回错误且原记录不变。 |
| `deleteNoteRemovesExistingNote()` | 删除存在编号返回成功并移除记录。 |
| `deleteNoteRejectsNullId()` | 空编号返回 `VALIDATION_ERROR`。 |
| `deleteNoteReturnsNotFoundForMissingId()` | 删除不存在编号返回 `NOT_FOUND`。 |
| `returnedViewsDoNotExposeMutableRepositoryState()` | 修改服务返回视图标签集合失败，后续仓储/服务状态不受影响。 |
