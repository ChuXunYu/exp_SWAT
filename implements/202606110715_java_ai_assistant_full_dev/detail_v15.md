# 详细设计（v15）

## 概述

本轮设计目标是在 `assistant.note` 包中新增个人笔记模块的核心领域实体和关键字搜索策略，为后续 `NoteQuery`、只读视图、仓储和服务层提供稳定领域基础。

本轮实现范围：

- `Note`：表示带编号、标题、内容、创建日期和标签集合的文本笔记，集中维护标题、内容、创建日期和标签集合不变量。
- `NoteSearchPolicy`：定义关键字匹配标题、内容或标签的公开语义，拒绝空关键字，大小写不敏感匹配文本，标签按 `Tag` 归一语义精确匹配。
- 对上述两个类型新增 JUnit Jupiter 单元测试。

本轮不实现 `NoteQuery`、`NoteView`、`NoteRepository`、`InMemoryNoteRepository` 或 `NoteService`，不接入 AI 摘要。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/note/Note.java` | 新建 | 定义个人笔记领域实体，维护标题、内容、创建日期和标签集合有效性。 |
| `java-ai-assistant/src/main/java/assistant/note/NoteSearchPolicy.java` | 新建 | 定义关键字匹配标题、内容或标签的无状态策略。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteTest.java` | 新建 | 覆盖笔记构造校验、文本规范化、标签快照隔离、更新原子性和标签增删替换行为。 |
| `java-ai-assistant/src/test/java/assistant/note/NoteSearchPolicyTest.java` | 新建 | 覆盖关键字匹配标题、内容、标签、多字段、大小写归一、空关键字拒绝和无匹配返回 `false`。 |

## 类型定义

### `Note`

**形态**：`final class`

**包路径**：`assistant.note`

**职责**：表示个人笔记或日记文本记录，持有稳定编号、规范化标题、规范化内容、创建日期和去重标签集合；对外只暴露不可修改标签快照。

**类型签名定义**：`public final class Note`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final EntityId id;` | 构造时非空；创建后不可变。 |
| `private String title;` | 非空；保存 `strip()` 后文本；规范化后不得为空。 |
| `private String content;` | 非空；保存 `strip()` 后文本；规范化后不得为空。 |
| `private final LocalDate createdDate;` | 构造时非空；创建后不可变。 |
| `private Set<Tag> tags;` | 内部使用 `LinkedHashSet<Tag>` 保存；集合引用非空；元素非空；按 `Tag.equals(...)` 去重；允许空集合。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public Note(EntityId id, String title, String content, LocalDate createdDate, Set<Tag> tags)` | `Note` | `id == null` 抛出 `NullPointerException("id")`；`title == null` 抛出 `NullPointerException("title")`；`content == null` 抛出 `NullPointerException("content")`；`createdDate == null` 抛出 `NullPointerException("createdDate")`；`tags == null` 抛出 `NullPointerException("tags")`；任一标签元素为 `null` 抛出 `NullPointerException("tag")`；标题和内容分别执行 `strip()`，规范化后为空分别抛出 `IllegalArgumentException("title must not be blank")`、`IllegalArgumentException("content must not be blank")`；内部保存脱离调用方输入集合的 `LinkedHashSet` 副本。 |
| `public static Note create(EntityId id, String title, String content, LocalDate createdDate, Set<Tag> tags)` | `Note` | 等价于调用公开构造器，作为后续服务层表达创建意图的工厂。 |
| `public EntityId getId()` | `EntityId` | 返回笔记编号。 |
| `public String getTitle()` | `String` | 返回已规范化标题。 |
| `public String getContent()` | `String` | 返回已规范化内容。 |
| `public LocalDate getCreatedDate()` | `LocalDate` | 返回创建日期。 |
| `public Set<Tag> getTags()` | `Set<Tag>` | 返回基于当前内部标签新建的不可修改 `LinkedHashSet` 快照；调用方无法通过返回集合修改内部状态。 |
| `public void updateContent(String title, String content)` | `void` | 先完整规范化并校验标题和内容，再一次性赋值；任一参数非法时抛出对应异常且原标题和原内容均保持不变。 |
| `public void replaceTags(Set<Tag> tags)` | `void` | 先完整校验集合引用和所有元素，再用新的 `LinkedHashSet` 副本替换内部集合；任一输入非法时原标签集合保持不变。 |
| `public boolean addTag(Tag tag)` | `boolean` | `tag == null` 抛出 `NullPointerException("tag")`；新增归一后不存在的标签返回 `true`；重复添加相同 `Tag` 语义标签返回 `false` 且集合不重复。 |
| `public boolean removeTag(Tag tag)` | `boolean` | `tag == null` 抛出 `NullPointerException("tag")`；存在并删除返回 `true`；不存在返回 `false`，不得抛出业务异常。 |
| `public boolean hasTag(Tag tag)` | `boolean` | `tag == null` 抛出 `NullPointerException("tag")`；存在相同 `Tag` 语义标签返回 `true`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String normalizeTitle(String title)` | `String` | `title == null` 抛出 `NullPointerException("title")`；执行 `strip()` 后为空抛出 `IllegalArgumentException("title must not be blank")`；返回规范化标题。 |
| `private static String normalizeContent(String content)` | `String` | `content == null` 抛出 `NullPointerException("content")`；执行 `strip()` 后为空抛出 `IllegalArgumentException("content must not be blank")`；返回规范化内容。 |
| `private static LinkedHashSet<Tag> copyTags(Set<Tag> tags)` | `LinkedHashSet<Tag>` | `tags == null` 抛出 `NullPointerException("tags")`；先遍历校验所有元素非空，再返回按输入迭代顺序复制的 `LinkedHashSet`；校验阶段不得修改任何已有实体状态。 |

**构造方式**：

- 编码时为 `assistant.note` 新建目录。
- 外部可直接调用构造器或 `Note.create(...)`。
- 标签由调用方以 `Tag.of(...)` 创建，`Note` 只接受 `Set<Tag>`，不接受裸字符串标签。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.Tag`、`java.time.LocalDate`、`java.util.Set`、`java.util.LinkedHashSet`、`java.util.Collections`、`java.util.Objects`。
- 后续 `NoteRepository` 保存实体时可通过构造器重建快照。
- 后续 `NoteView` 可从 `Note` getter 投影只读字段。

### `NoteSearchPolicy`

**形态**：`final class`

**包路径**：`assistant.note`

**职责**：集中定义笔记关键字匹配规则，使关键字为空、无匹配、标题匹配、内容匹配、标签匹配和多字段匹配可独立测试。

**类型签名定义**：`public final class NoteSearchPolicy`

**字段定义**：

无实例字段；策略无状态。

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public NoteSearchPolicy()` | `NoteSearchPolicy` | 公开无参构造器，与现有无状态策略类风格一致。 |
| `public boolean matchesKeyword(Note note, String keyword)` | `boolean` | `note == null` 抛出 `NullPointerException("note")`；`keyword == null` 抛出 `NullPointerException("keyword")`；关键字执行 `strip()` 后为空抛出 `IllegalArgumentException("keyword must not be blank")`；标题和内容采用大小写不敏感包含匹配；标签分支使用 `Tag.of(normalizedKeyword)` 与 `note.getTags()` 做精确集合包含判断；任一字段匹配返回 `true`，全部不匹配返回 `false`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String normalizeKeyword(String keyword)` | `String` | `keyword == null` 抛出 `NullPointerException("keyword")`；执行 `strip()` 后为空抛出 `IllegalArgumentException("keyword must not be blank")`；返回规范化关键字。 |
| `private static boolean containsIgnoreCase(String text, String keyword)` | `boolean` | `text` 和 `keyword` 均由调用方保证非空；两者使用 `toLowerCase(Locale.ROOT)` 后执行包含匹配。 |
| `private static boolean matchesTag(Note note, String keyword)` | `boolean` | 尝试以 `Tag.of(keyword)` 构造标签；构造成功则判断 `note.getTags().contains(tag)`；若 `Tag.of(...)` 因关键字不能形成合法标签抛出 `IllegalArgumentException`，本方法返回 `false`，标题和内容匹配结果不受影响。 |

**构造方式**：

- 调用方通过 `new NoteSearchPolicy()` 创建。
- 后续 `NoteQuery` 或 `NoteService` 应复用此策略作为唯一关键字语义来源，不在服务层重复实现大小写折叠或标签匹配。

**类型关系**：

- 依赖 `assistant.common.Tag`、`java.util.Locale`、`java.util.Objects`。
- 被后续 `NoteQuery`、`NoteRepository.findBy(...)` 或 `NoteService.search...` 组合调用。

## 错误处理

- 本轮属于领域实体和策略基础，不引入 `OperationResult` 或 `BusinessException`。
- 构造器、实体方法和策略方法使用 `NullPointerException` 表达空引用参数错误，异常消息采用参数名。
- 标题、内容、关键字等文本经 `strip()` 后为空时使用 `IllegalArgumentException` 表达输入不合法。
- `NoteSearchPolicy.matchesKeyword(...)` 中空关键字是输入错误，必须抛出异常；不得解释为匹配全部。
- 无匹配是正常业务结果，`matchesKeyword(...)` 返回 `false`，不抛异常。
- `replaceTags(...)` 和 `updateContent(...)` 必须先完整校验所有新值，再修改内部字段，失败时保持原状态不变。

## 行为契约

- `Note` 标题和内容始终保存为 `strip()` 后文本，不保存调用方传入的首尾空白。
- `Note` 创建日期不可修改，后续修改内容或标签不得改变 `createdDate`。
- `Note` 内部标签集合允许为空；重复标签按 `Tag` 归一后的相等性去重。
- 构造器和 `replaceTags(...)` 均保存标签集合副本；调用方创建后继续修改传入集合不得影响笔记内部状态。
- `getTags()` 每次返回新的不可修改快照；调用方不能通过返回集合修改内部标签，且旧快照不随实体后续标签变更而变化。
- `updateContent(...)` 只修改标题和内容，不修改编号、创建日期或标签。
- `addTag(...)` 和 `removeTag(...)` 只影响标签集合，不修改标题、内容或创建日期。
- `NoteSearchPolicy` 文本匹配对标题和内容使用 `Locale.ROOT` 大小写归一后的包含匹配，必须覆盖非英语大小写特殊字符行为，例如土耳其语大写 `İ` 不应受默认 JVM Locale 影响。
- `NoteSearchPolicy` 标签匹配必须通过 `Tag.of(keyword)` 复用标签的 `strip()` 与小写归一语义，匹配方式为精确标签匹配，不做标签子串匹配。
- 若关键字同时匹配标题、内容和标签中的多个字段，`matchesKeyword(...)` 返回 `true`；策略无需暴露具体命中字段。

## 依赖关系

- 新增包 `assistant.note` 不依赖任务、日程、学习计划或收支模块。
- `Note` 依赖 `EntityId` 与 `Tag` 两个通用值对象。
- `NoteSearchPolicy` 依赖 `Note` 与 `Tag`。
- 本轮测试仅依赖 JUnit Jupiter、通用值对象和 Java 标准库，不依赖真实当前时间、网络、API Key 或外部文件。

## 测试设计

### `NoteTest`

| 测试方法 | 覆盖契约 |
|----------|----------|
| `constructorStoresNormalizedFieldsAndTags()` | 构造成功后编号、标题、内容、创建日期和标签字段正确，标题与内容执行 `strip()`。 |
| `constructorRejectsNullRequiredFields()` | `id`、`title`、`content`、`createdDate`、`tags` 为空时分别抛出 `NullPointerException`。 |
| `constructorRejectsBlankTitleAndContent()` | 标题或内容为空字符串、普通空白、Unicode 空白时抛出 `IllegalArgumentException`。 |
| `constructorRejectsNullTagElement()` | 标签集合中包含 `null` 时抛出 `NullPointerException("tag")`。 |
| `constructorAllowsEmptyTagSet()` | 空标签集合可构造成功，`getTags()` 返回空不可修改集合。 |
| `constructorCopiesInputTagsAndDeduplicatesByTagSemantics()` | 构造后修改输入集合不影响实体；重复归一标签只保留一个。 |
| `getTagsReturnsUnmodifiableSnapshot()` | 返回集合不可修改；旧快照不随后续 `addTag(...)` 或 `removeTag(...)` 改变。 |
| `updateContentChangesTitleAndContentOnly()` | 更新成功后标题和内容规范化，编号、创建日期和标签保持不变。 |
| `updateContentRejectsInvalidInputAndKeepsOldState()` | 新标题或新内容非法时原标题和原内容均保持不变。 |
| `replaceTagsReplacesWithValidatedSnapshot()` | 替换成功后标签集合等于新集合副本，修改新输入集合不影响实体。 |
| `replaceTagsRejectsInvalidInputAndKeepsOldTags()` | 替换标签集合引用为空或包含空元素时抛出异常且原标签集合保持不变。 |
| `addTagAddsOnlyNewTagSemantics()` | 新标签返回 `true`；重复归一标签返回 `false` 且集合不重复。 |
| `addTagRejectsNullTag()` | 空标签参数抛出 `NullPointerException("tag")`。 |
| `removeTagRemovesExistingTagAndReturnsFalseForMissing()` | 删除存在标签返回 `true`；删除不存在标签返回 `false` 且不抛异常。 |
| `removeTagRejectsNullTag()` | 空标签参数抛出 `NullPointerException("tag")`。 |
| `hasTagUsesTagValueSemantics()` | 大小写和首尾空白不同但归一后相同的 `Tag` 可命中。 |

### `NoteSearchPolicyTest`

| 测试方法 | 覆盖契约 |
|----------|----------|
| `matchesKeywordRejectsNullNoteAndKeyword()` | 空 `note` 或空 `keyword` 分别抛出 `NullPointerException`。 |
| `matchesKeywordRejectsBlankKeyword()` | 空字符串、普通空白、Unicode 空白关键字抛出 `IllegalArgumentException`。 |
| `matchesTitleIgnoringCase()` | 关键字以不同大小写命中标题子串。 |
| `matchesContentIgnoringCase()` | 关键字以不同大小写命中内容子串。 |
| `matchesTagUsingTagSemantics()` | 关键字首尾空白和大小写不同但通过 `Tag.of(...)` 归一后命中标签。 |
| `doesNotMatchTagBySubstring()` | 标签分支为精确匹配，标签子串关键字若未命中文本字段则返回 `false`。 |
| `returnsTrueWhenMultipleFieldsMatch()` | 同一关键字同时命中标题、内容或标签时返回 `true`。 |
| `returnsFalseWhenNoFieldMatches()` | 标题、内容和标签均不匹配时返回 `false`。 |
| `textMatchingUsesLocaleRootInsteadOfDefaultLocale()` | 临时切换默认 Locale 后，文本大小写匹配结果仍由 `Locale.ROOT` 语义决定。 |
| `tagBranchDoesNotHideTextMatch()` | 关键字可匹配标题或内容时，即使标签集合不包含对应标签仍返回 `true`。 |

