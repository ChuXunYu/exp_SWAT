# 详细设计（v27）

## 概述

本轮设计目标是扩展 `assistant.app.ConsoleApplication` 的个人笔记入口：主菜单命令 `6` 不再执行一次性 `showNotes()`，而是进入可循环笔记子菜单。笔记子菜单通过既有 `NoteService` 完成列表、新增、查看、关键字搜索、标签搜索、关键字与标签组合筛选、修改、删除、帮助、返回主菜单和 EOF 稳定退出。

本轮实现范围：

- 修改 `ConsoleApplication`：主菜单笔记命令接入笔记子菜单；新增笔记命令分发、字段读取、正整数 id 解析、标签列表解析、`NoteQuery` 构造、列表和详情输出。
- 修改 `ConsoleApplicationTest`：更新主菜单笔记入口断言，新增笔记子菜单成功路径、搜索筛选、标签解析、控制台校验、服务失败、帮助、返回和 EOF 测试。

本轮不修改：

- `NoteService`、`NoteQuery`、`NoteView`、`Tag` 的公开契约。
- 笔记实体、仓储、搜索策略、汇总服务、AI 服务、应用装配或其他业务模块。
- 独立 CLI 框架或第三方命令解析库。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 将主菜单命令 `6` 接入笔记子菜单，新增笔记命令处理、字段读取解析、查询构造、列表和详情输出。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 更新笔记入口断言，新增笔记子菜单成功路径、搜索筛选、标签解析、校验失败、服务失败、帮助/返回和 EOF 测试。 |

## 类型定义

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：控制台主循环与各核心功能子菜单交互层。笔记相关代码只负责读取输入、解析控制台字段、调用 `NoteService` 和展示 `OperationResult`，不承载标题、内容、标签业务语义、搜索匹配或仓储访问等业务规则。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.common.Tag` | 组合筛选两字段均非空时构造 `NoteQuery.of(keyword, Tag.of(tagText))`；列表和详情输出时调用 `displayName()`。 |
| `assistant.note.NoteQuery` | 构造关键字与标签组合筛选查询。 |
| `java.util.LinkedHashSet` | 解析控制台逗号分隔标签列表，保持输入顺序并去重。 |
| `java.util.Set` | 表达传给 `NoteService` 的标签文本集合。 |
| `java.util.stream.Collectors` | 可选，用于把 `Set<Tag>` 格式化为稳定展示文本；若不用 stream，可不新增该导入。 |

既有 `EntityId`、`OperationResult`、`NoteView`、`Locale`、`List`、`Objects` 等依赖继续复用。

**现有字段保持不变**：

| 字段签名 | 约束 |
|----------|------|
| `private final ApplicationServices services` | 构造时非空。 |
| `private final BufferedReader input` | 构造时非空；测试通过 `StringReader` 输入。 |
| `private final PrintWriter output` | 构造时非空；测试通过 `StringWriter` 输出。 |
| `private boolean running` | 主菜单与各子菜单共用的程序运行标记。 |

**公开接口保持不变**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ConsoleApplication(ApplicationServices services, Reader input, Writer output)` | 构造器 | 维持既有空依赖防御和 `BufferedReader` / `PrintWriter` 包装行为。 |
| `public void run()` | `void` | 维持欢迎语、主菜单循环、EOF 正常退出和每轮输出刷新行为。 |

**修改的私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void dispatch(String rawCommand)` | `void` | 主菜单命令 `6` 改为调用 `runNoteMenu()`；其他主菜单命令保持既有行为。 |
| `private void printHelp()` | `void` | 主菜单帮助保留笔记入口说明；无需列出笔记子菜单全部命令。 |
| `private void showNotes()` | `void` | 删除，或改为由 `listNotes()` 覆盖；主菜单不得再直接调用一次性列表入口。 |

**新增笔记子菜单私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void runNoteMenu()` | `void` | 进入时先调用 `printNoteMenu()`；随后在 `running == true` 且未返回主菜单时循环读取笔记命令。EOF 或读取失败时设置 `running = false` 并结束程序；命令 `b` / `back` 结束子菜单并返回主菜单；每次命令处理后刷新输出。 |
| `private void printNoteMenu()` | `void` | 输出笔记子菜单命令说明，至少包含 `l/list`、`a/add`、`v/view`、`k/keyword`、`t/tag`、`f/filter`、`u/update`、`d/delete`、`b/back`、`h/help`。 |
| `private boolean dispatchNoteCommand(String rawCommand)` | `boolean` | 返回 `true` 表示继续留在笔记子菜单，返回 `false` 表示返回主菜单或程序已结束。空命令输出 `请输入笔记命令。` 并返回 `true`；未知命令输出 `未知笔记命令，请输入 h 查看帮助。` 后展示笔记帮助并返回 `true`。 |
| `private void listNotes()` | `void` | 调用 `NoteService.listNotes()`；成功后调用 `printNoteList("笔记列表", notes)`；失败复用 `printResult(...)`。列表不得限制为前 10 条。 |
| `private void addNote()` | `void` | 依次读取标题、内容、标签列表；标题和内容原始字符串传给服务；标签列表由控制台层解析为 `LinkedHashSet<String>`；调用 `NoteService.createNote(title, content, tags)` 并通过 `printNoteResult(result)` 展示。 |
| `private void viewNote()` | `void` | 读取笔记 id；`INVALID` 时不调用服务并留在笔记子菜单；`EOF` 时结束程序；`VALUE` 时调用 `NoteService.getNote(id)` 并通过 `printNoteResult(result)` 展示。 |
| `private void searchNotesByKeyword()` | `void` | 读取一个必填关键字；空白输出 `失败: VALIDATION_ERROR - 关键字不能为空` 并不调用服务；合法时调用 `NoteService.searchByKeyword(keyword)`，成功后调用 `printNoteList("笔记关键字搜索结果", notes)`。 |
| `private void searchNotesByTag()` | `void` | 读取一个必填标签文本；空白输出 `失败: VALIDATION_ERROR - 标签不能为空` 并不调用服务；合法时调用 `NoteService.searchByTag(tagText)`，成功后调用 `printNoteList("笔记标签搜索结果", notes)`。 |
| `private void filterNotes()` | `void` | 依次读取可选关键字、可选标签。两者均为空白时输出 `失败: VALIDATION_ERROR - 关键字和标签至少填写一个` 并不调用服务。仅关键字非空时调用 `NoteService.searchByKeyword(keyword)` 或 `NoteService.listNotes(NoteQuery.byKeyword(keyword))`；仅标签非空时调用 `NoteService.searchByTag(tagText)` 或 `NoteService.listNotes(NoteQuery.byTag(Tag.of(tagText)))`；两者均非空时构造 `NoteQuery.of(keyword, Tag.of(tagText))` 并调用 `NoteService.listNotes(query)`。成功后调用 `printNoteList("笔记筛选结果", notes)`。 |
| `private void updateNote()` | `void` | 依次读取笔记 id、标题、内容、标签列表。id 返回 `INVALID` 时不调用服务并留在笔记子菜单；任一读取返回 EOF 或原始字段 `null` 时结束程序；全部读取成功后调用 `NoteService.updateNote(id, title, content, tags)` 并通过 `printNoteResult(result)` 展示。控制台层不预先校验标题、内容或标签业务语义。 |
| `private void deleteNote()` | `void` | 读取笔记 id；`INVALID` 时不调用服务；`EOF` 时结束程序；`VALUE` 时调用 `NoteService.deleteNote(id)` 并通过既有 `printResult(result)` 展示；成功空载荷输出 `操作成功`。 |
| `private void printNoteResult(OperationResult<NoteView> result)` | `void` | 先调用 `printResult(result)`；失败时只输出错误码和消息；成功时调用 `printNoteDetail(result.getPayload())`。 |
| `private void printNoteList(String heading, List<NoteView> notes)` | `void` | 输出标题；空列表输出 `暂无笔记`；非空逐行输出全部笔记，不限制 10 条。逐行格式为 `id | title | createdDate | tags`，标签文本必须使用 `formatTags(note.tags())`，不得直接依赖 `Set.toString()`。 |
| `private void printNoteDetail(NoteView note)` | `void` | 输出单条笔记详情，至少包含 `笔记详情`、`ID: {id}`、`标题: {title}`、`内容: {content}`、`创建日期: {createdDate}`、`标签: {tags}`。标签文本使用 `formatTags(note.tags())`。 |
| `private String readNoteRawField(String prompt)` | `String` 或 `null` | 调用 `readLine(prompt)`；读取到 EOF 时显式设置 `running = false` 并返回 `null`；读取到真实空行时返回空字符串。用于标题、内容、标签列表、搜索字段等由服务层或当前控制台契约处理的原始字段。 |
| `private ParsedInput<EntityId> readNoteId(String prompt)` | `ParsedInput<EntityId>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字、小数或超出 `long` 范围时输出 `失败: VALIDATION_ERROR - 笔记 id 必须是正整数` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(new EntityId(value))`。 |
| `private EntityId parseNoteId(String rawValue)` | `EntityId` 或 `null` | 只做语法解析与正数约束；成功返回 `new EntityId(value)`；失败输出笔记 id 验证错误并返回 `null`。 |
| `private Set<String> parseNoteTags(String rawValue)` | `Set<String>` | 以英文逗号 `,` 分割；每段执行 `strip()`；丢弃空白片段；使用 `LinkedHashSet<String>` 保持首次出现顺序并去重；空输入返回空 `LinkedHashSet`。不得执行大小写归一，也不得拒绝非空标签业务语义。 |
| `private String formatTags(Set<Tag> tags)` | `String` | 按集合迭代顺序读取 `Tag.displayName()`，用 `", "` 连接；空集合返回空字符串或稳定空展示文本。调用方和测试不得依赖 `Set.toString()`。 |

**复用的私有接口**：

| 方法签名 | 返回类型 | 复用契约 |
|----------|----------|----------|
| `private void printValidationError(String message)` | `void` | 继续输出固定格式 `失败: VALIDATION_ERROR - {message}`。笔记 id、必填关键字、必填标签和组合筛选全空错误复用该输出入口。 |
| `private <T> boolean printResult(OperationResult<T> result)` | `boolean` | 服务失败输出 `失败: {ErrorCode} - {message}`；成功且空载荷输出 `操作成功`。 |
| `private String readLine(String prompt)` | `String` 或 `null` | EOF 返回 `null` 且自身不修改 `running`；`IOException` 输出 `输入读取失败，程序退出。`、设置 `running = false` 并返回 `null`。 |
| `private record ParsedInput<T>(State state, T value)` | 私有嵌套类型 | 继续表达 `VALUE`、`EMPTY`、`INVALID`、`EOF` 四种字段解析状态；笔记读取方法必须复用该类型，不新增并行状态类型。 |

**命令分发契约**：

| 命令 | 私有方法 | 服务调用 |
|------|----------|----------|
| `l` / `list` | `listNotes()` | `NoteService.listNotes()` |
| `a` / `add` | `addNote()` | `NoteService.createNote(String, String, Set<String>)` |
| `v` / `view` | `viewNote()` | `NoteService.getNote(EntityId)` |
| `k` / `keyword` | `searchNotesByKeyword()` | `NoteService.searchByKeyword(String)` |
| `t` / `tag` | `searchNotesByTag()` | `NoteService.searchByTag(String)` |
| `f` / `filter` | `filterNotes()` | `NoteService.searchByKeyword(String)`、`NoteService.searchByTag(String)` 或 `NoteService.listNotes(NoteQuery)` |
| `u` / `update` | `updateNote()` | `NoteService.updateNote(EntityId, String, String, Set<String>)` |
| `d` / `delete` | `deleteNote()` | `NoteService.deleteNote(EntityId)` |
| `b` / `back` | 子菜单返回 | 不调用服务 |
| `h` / `help` | `printNoteMenu()` | 不调用服务 |

**字段读取顺序**：

| 操作 | 字段顺序 |
|------|----------|
| 新增笔记 | 标题、内容、标签列表 |
| 查看笔记 | 笔记 id |
| 关键字搜索 | 关键字 |
| 标签搜索 | 标签 |
| 组合筛选 | 可选关键字、可选标签 |
| 修改笔记 | 笔记 id、标题、内容、标签列表 |
| 删除笔记 | 笔记 id |

**输出格式契约**：

| 方法 | 输出要求 |
|------|----------|
| `printNoteList(String heading, List<NoteView> notes)` | 先输出 `heading`；空列表输出 `暂无笔记`；非空每行至少包含 `{id} | {title} | {createdDate} | {tags}`。 |
| `printNoteDetail(NoteView note)` | 输出 `笔记详情`、`ID: {id}`、`标题: {title}`、`内容: {content}`、`创建日期: {createdDate}`、`标签: {tags}`。 |
| `printNoteResult(OperationResult<NoteView> result)` | 新增、查看、修改成功均通过统一详情展示；失败复用 `printResult` 的 `失败: {ErrorCode} - {message}`。 |
| `deleteNote()` | 删除成功复用 `printResult` 输出 `操作成功`。 |

**构造方式**：

- 仍由 `Main` 和测试通过 `new ConsoleApplication(services, input, output)` 构造。

**类型关系**：

- `ConsoleApplication` 依赖既有 `ApplicationServices.noteService()` 获取 `NoteService`。
- `ConsoleApplication` 依赖 `EntityId` 表达经过控制台语法校验的正整数 id。
- `ConsoleApplication` 依赖 `Tag.of(tagText)` 和 `NoteQuery.of(keyword, tag)` 仅用于组合筛选两字段均非空的查询构造；`Tag` 抛出的非法标签语义由 `filterNotes()` 捕获并转成稳定 `VALIDATION_ERROR` 输出，或通过服务调用结果表达。为了满足“不得自行拒绝标签业务语义”，新增和修改的标签合法性不得在控制台层提前拦截。
- `ConsoleApplication` 不依赖 `NoteRepository`、`Note`、`NoteSearchPolicy` 或任何仓储集合。

### `ConsoleApplication.ParsedInput<T>`

**形态**：`private record`

**包路径**：`assistant.app`，作为 `ConsoleApplication` 私有嵌套类型

**职责**：继续表达控制台字段解析结果，任务、日程、学习计划、收支与笔记子菜单共同复用，避免用 `null` 同时代表合法空筛选、非法输入和 EOF。

**状态定义保持不变**：

| 状态 | 含义 | 笔记调用方行为 |
|------|------|----------------|
| `VALUE` | 字段存在且解析成功，`value()` 非空。 | 继续当前笔记操作并使用值。 |
| `EMPTY` | 可用于后续抽取的可选字段。 | 本轮笔记查询可不使用该状态；组合筛选可直接通过原始字符串空白判断。 |
| `INVALID` | 用户输入不合法，且已输出 `VALIDATION_ERROR`。 | 立即中止当前笔记操作，不调用服务，保持在笔记子菜单。 |
| `EOF` | 读取到 EOF，且字段读取方法已设置 `running = false`。 | 立即中止当前笔记操作和笔记子菜单，程序正常结束。 |

**公开给外部**：无。该类型为 `ConsoleApplication` 私有实现细节。

**既有接口保持不变**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `static <T> ParsedInput<T> value(T value)` | `ParsedInput<T>` | `value == null` 抛 `NullPointerException("value")`；状态为 `VALUE`。 |
| `static <T> ParsedInput<T> empty()` | `ParsedInput<T>` | 状态为 `EMPTY`，载荷为 `null`。 |
| `static <T> ParsedInput<T> invalid()` | `ParsedInput<T>` | 状态为 `INVALID`，载荷为 `null`。 |
| `static <T> ParsedInput<T> eof()` | `ParsedInput<T>` | 状态为 `EOF`，载荷为 `null`。 |
| `boolean hasValue()` | `boolean` | 仅 `VALUE` 返回 `true`。 |
| `boolean isEmpty()` | `boolean` | 仅 `EMPTY` 返回 `true`。 |
| `boolean isInvalid()` | `boolean` | 仅 `INVALID` 返回 `true`。 |
| `boolean isEof()` | `boolean` | 仅 `EOF` 返回 `true`。 |
| `T value()` | `T` | 仅在 `VALUE` 状态下调用；其他状态不得调用。 |

### `ConsoleApplicationTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.app`

**职责**：通过完整控制台输入输出验证笔记子菜单，不读取真实环境变量、不访问真实网络、不依赖真实系统时间。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.note.NoteService` | 构造 mock 服务验证控制台前置校验不调用服务。 |
| `assistant.note.NoteQuery` | Mockito 验证组合筛选服务调用时按需使用。 |
| `assistant.note.NoteView` | 构造超过 10 条或 mock 返回的笔记视图。 |
| `assistant.common.Tag` | 构造 `NoteView` 标签集合或断言标签格式。 |
| `java.util.LinkedHashSet` | 构造有序标签集合辅助方法。 |
| `java.util.Set` | 构造标签集合辅助方法与 Mockito 参数匹配。 |

既有 `ApplicationServices`、`servicesWithDemoData()`、`servicesWithoutDemoData()`、`runWithInput(...)`、`between(...)`、`assertContains(...)`、`assertNotContains(...)` 等测试辅助继续复用。

**保留并复用的辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static ApplicationServices servicesWithDemoData()` | `ApplicationServices` | 保持固定时间与演示数据装配。 |
| `private static ApplicationServices servicesWithoutDemoData()` | `ApplicationServices` | 保持固定时间 `2026-01-15T09:00` 与空数据装配。 |
| `private static String runWithInput(ApplicationServices services, String input)` | `String` | 保持 `StringReader` / `StringWriter` 运行方式。 |
| `private static void assertContains(String text, String expected)` | `void` | 保持输出包含断言。 |
| `private static String between(String text, String startInclusive, String endExclusive)` | `String` | 继续用于筛选测试断言匹配项出现且非匹配项不出现在筛选输出段中。 |
| `private static void assertNotContains(String text, String unexpected)` | `void` | 继续断言指定输出片段不包含非匹配项。 |
| `private static void assertNullRejected(String expectedMessage, Executable executable)` | `void` | 保持空依赖断言。 |

**新增测试辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static NoteView noteView(long id, String title, String content, String... tags)` | `NoteView` | 使用 `new EntityId(id)`、固定 `LocalDate.of(2026, 1, 15)` 与按传入顺序构造的 `LinkedHashSet<Tag>` 创建测试笔记视图。 |
| `private static LinkedHashSet<Tag> tagSet(String... values)` | `LinkedHashSet<Tag>` | 对每个值调用 `Tag.of(value)` 并保持顺序。 |
| `private static ApplicationServices withNoteService(ApplicationServices baseServices, NoteService noteService)` | `ApplicationServices` | 基于既有 `ApplicationServices` 构造器替换笔记服务，其余服务沿用 base。 |

**新增或调整的测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `listCommandsDisplayEachCoreEntry()` | 输入改为 `2\nb\n3\nb\n4\nb\n5\nb\n6\nb\n8\nq\n`；断言任务、日程、学习计划、收支和笔记均进入对应子菜单，AI 草稿仍可用。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 输入改为 `2\nl\nb\n3\nl\nb\n4\nl\nb\n5\nl\nb\n6\nl\nb\n8\nq\n`；断言笔记空状态通过笔记子菜单列表展示。 |
| `noteMenuAddsListsViewsUpdatesDeletesNote()` | 空数据服务；新增笔记后列表、查看、修改、删除，再查看；断言新增和修改通过 `笔记详情` 展示，列表包含 `id | title | createdDate | tags`，删除成功输出 `操作成功`，删除后查看输出 `NOT_FOUND`。 |
| `noteMenuSearchesByKeywordCaseInsensitively()` | 新增两条笔记，其中一条标题或内容包含不同大小写关键字；执行 `k/keyword` 搜索；截取 `笔记关键字搜索结果` 输出段，断言匹配项出现且非匹配项不出现。 |
| `noteMenuSearchesByTagUsingNormalizedTagSemantics()` | 新增标签大小写不同但语义相同的笔记；以不同大小写标签执行 `t/tag` 搜索；断言输出使用 `Tag.displayName()` 的归一文本并匹配对应笔记。 |
| `noteMenuFiltersByKeywordAndTagTogether()` | 新增至少三条笔记，只有一条同时满足关键字和标签；执行 `f/filter` 并填写两个字段；截取 `笔记筛选结果`，断言只包含同时满足的笔记。 |
| `noteMenuCreatesEmptyTagListAndParsesCommaSeparatedTags()` | 新增一条空标签笔记和一条输入 ` Study, , life, study, LIFE ` 的笔记；列表或详情断言空标签稳定展示，非空标签按 `study, life` 顺序去重展示。 |
| `noteMenuRejectsInvalidIdBeforeCallingNoteService()` | 使用 mock `NoteService`；输入查看、修改、删除的非法 id，例如空值、`abc`、`1.5`、`0`、超出 `long` 范围；断言输出 `失败: VALIDATION_ERROR - 笔记 id 必须是正整数`，且对应服务方法未被调用。 |
| `noteMenuRejectsBlankRequiredKeywordAndTagBeforeCallingService()` | 使用 mock `NoteService`；输入 `k` 后空白、`t` 后空白；断言分别输出 `关键字不能为空`、`标签不能为空`，并验证 `searchByKeyword(...)`、`searchByTag(...)` 未调用。 |
| `noteMenuRejectsEmptyCombinedFilterBeforeCallingService()` | 使用 mock `NoteService`；输入 `f` 后关键字和标签均为空；断言输出 `关键字和标签至少填写一个`，且 `listNotes(NoteQuery)`、`searchByKeyword(...)`、`searchByTag(...)` 均未调用。 |
| `noteMenuDisplaysServiceValidationFailuresAndStaysInMenu()` | 空数据服务；新增空白标题或空白内容失败后继续执行 `l` 和 `b`；断言输出服务层 `VALIDATION_ERROR`，且后续 `笔记列表` 或 `暂无笔记` 出现，证明程序未退出且仍在笔记子菜单。 |
| `noteMenuHandlesUnknownBlankHelpBackAndEof()` | 覆盖未知笔记命令、空笔记命令、`h/help`、`b/back` 和进入笔记菜单后 EOF；断言未知命令和空命令提示稳定，帮助展示 `笔记菜单`，返回后主菜单可继续运行，EOF 正常结束程序。 |
| `noteMenuListsMoreThanTenNotesWithoutTruncation()` | mock `NoteService.listNotes()` 返回 11 条以上 `NoteView`；输入 `6\nl\nb\nq\n`；断言第一条和第十一条均出现在 `笔记列表` 输出段。 |
| `noteMenuCombinedFilterSingleFieldUsesEquivalentSearch()` | 可选加强测试：使用 mock `NoteService`；组合筛选仅关键字非空时验证调用 `searchByKeyword(keyword)` 或 `listNotes(NoteQuery.byKeyword(keyword))`；仅标签非空时验证调用 `searchByTag(tagText)` 或 `listNotes(NoteQuery.byTag(Tag.of(tagText)))`，且不会调用组合 `NoteQuery.of(...)`。 |

## 错误处理

- 控制台读取 EOF：与任务、日程、学习计划、收支子菜单一致，字段读取方法设置 `running = false` 并返回 `null` 或 `ParsedInput.eof()`，程序正常结束，不输出额外错误。
- 控制台读取 `IOException`：继续由 `readLine(...)` 输出 `输入读取失败，程序退出。` 并设置 `running = false`。
- id 语法错误：控制台层负责拦截，输出 `失败: VALIDATION_ERROR - 笔记 id 必须是正整数`，不得调用 `NoteService`。
- 关键字搜索和标签搜索必填字段为空白：控制台层输出稳定 `VALIDATION_ERROR`，不得调用 `NoteService`。
- 组合筛选两个可选字段均为空白：控制台层输出稳定 `VALIDATION_ERROR`，不得调用 `NoteService`。
- 标题、内容和新增/修改标签业务语义错误：原始输入传给 `NoteService`，由服务返回 `OperationResult.failure(...)`，控制台复用 `printResult(...)` 输出。
- 组合筛选中两字段均非空且 `Tag.of(tagText)` 抛出 `IllegalArgumentException` 或 `NullPointerException`：`filterNotes()` 捕获后输出 `失败: VALIDATION_ERROR - {message}`；`message` 为空或空白时输出稳定兜底 `失败: VALIDATION_ERROR - invalid note input`。
- 服务失败：所有 `OperationResult` 失败都复用 `printResult(...)` 风格输出 `失败: {ErrorCode} - {message}`。

## 行为契约

- 主菜单命令 `6` 必须进入笔记子菜单；除 EOF、读取失败、退出命令外，用户通过 `b/back` 返回主菜单。
- 笔记子菜单每个命令处理完成后仍停留在子菜单，除非命令为 `b/back` 或发生 EOF/读取失败。
- 新增笔记字段顺序固定为：标题、内容、标签列表。
- 修改笔记字段顺序固定为：笔记 id、标题、内容、标签列表。
- 查看和删除只读取笔记 id。
- 标签列表输入只按英文逗号分割；空输入表示空标签集合；非空分段先 `strip()`，丢弃空白片段，再以 `LinkedHashSet<String>` 保持输入顺序并去重后传给 `NoteService`。
- 控制台层不得对新增/修改标签执行大小写归一，大小写归一只能由 `Tag` / `NoteService` 完成。
- `printNoteList(...)` 必须展示全部笔记，不得使用 `limit(10)`。
- 标签输出必须使用 `Tag.displayName()` 或等价稳定文本，不得直接拼接 `note.tags()`。
- 新增、查看、修改成功统一通过 `printNoteDetail(...)` 展示。
- 删除成功复用 `printResult(...)` 输出 `操作成功`。
- 控制台前置校验失败不得导致程序退出，且仍留在笔记子菜单等待下一条笔记命令。

## 依赖关系

- 依赖既有 `assistant.note.NoteService` 公开接口：`createNote`、`updateNote`、`getNote`、`listNotes`、`listNotes(NoteQuery)`、`searchByKeyword`、`searchByTag`、`deleteNote`。
- 依赖既有 `assistant.note.NoteQuery` 静态工厂：`byKeyword(String)`、`byTag(Tag)`、`of(String, Tag)`。
- 依赖既有 `assistant.common.Tag` 的 `Tag.of(String)` 和 `displayName()`。
- 依赖既有 `assistant.note.NoteView` record 访问器：`id()`、`title()`、`content()`、`createdDate()`、`tags()`。
- 暴露给后续任务的行为是完整笔记控制台闭环；不新增跨模块公开 API。
