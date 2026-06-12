# 任务指令（v27）

## 动作
NEW

## 任务描述
扩展 `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 的个人笔记菜单交互，并补充 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 覆盖；必要时可在 `assistant.app` 包内复用或抽取小型输入解析辅助方法，但不得修改 `NoteService`、`NoteQuery`、`NoteView`、`Tag` 的公开契约。

本轮主菜单命令 `6` 必须从一次性 `showNotes()` 改为进入可循环笔记子菜单。笔记子菜单至少支持：

- `l/list`：列出全部笔记。
- `a/add`：新增笔记。
- `v/view`：查看单条笔记。
- `k/keyword`：按关键字搜索。
- `t/tag`：按标签搜索。
- `f/filter`：按关键字和标签组合筛选。
- `u/update`：修改笔记标题、内容和标签集合。
- `d/delete`：删除笔记。
- `b/back`：返回主菜单。
- `h/help`：展示笔记子菜单帮助。

字段读取和服务调用契约：

- 新增笔记字段顺序固定为：标题、内容、标签列表。
- 修改笔记字段顺序固定为：笔记 id、标题、内容、标签列表。
- 查看和删除只读取笔记 id。
- 关键字搜索读取一个必填关键字，调用 `NoteService.searchByKeyword(keyword)`。
- 标签搜索读取一个必填标签文本，调用 `NoteService.searchByTag(tagText)`。
- 组合筛选字段顺序固定为：可选关键字、可选标签。两者至少填写一个；全部为空时输出 `VALIDATION_ERROR` 且不得调用服务。仅关键字非空时可以调用 `NoteService.searchByKeyword(keyword)` 或等价 `listNotes(NoteQuery.byKeyword(keyword))`；仅标签非空时可以调用 `NoteService.searchByTag(tagText)` 或等价 `listNotes(NoteQuery.byTag(Tag.of(tagText)))`；两者均非空时构造 `NoteQuery.of(keyword, Tag.of(tagText))` 并调用 `NoteService.listNotes(query)`。
- 标签列表输入由控制台层按英文逗号 `,` 分割；空输入表示空标签集合；非空分段先 `strip()`，丢弃分割后为空白的片段，再以 `LinkedHashSet<String>` 保持输入顺序并去重后传给 `NoteService`。控制台层不得提前执行大小写归一、不得自行拒绝标签业务语义，标签合法性由 `Tag` / `NoteService` 返回结果表达。
- 笔记 id 必须由控制台层做正整数语法解析。空值、非数字、小数、非正数或超出 `long` 范围时输出 `失败: VALIDATION_ERROR - 笔记 id 必须是正整数`，不得调用服务。
- 关键字搜索和标签搜索的必填输入若为空白，可由控制台层输出稳定验证错误并不调用服务；组合筛选中单个可选字段允许为空。
- 标题、内容和标签列表的原始输入传递给服务，由服务负责标题/内容空白、标签非法、状态不变性和错误分类。
- EOF 或输入读取失败行为必须与任务、日程、学习计划、收支子菜单一致：EOF 正常结束程序，读取失败输出既有读取失败提示并结束程序。

输出展示契约：

- `printNoteList(String heading, List<NoteView> notes)` 输出标题；空列表输出 `暂无笔记`；非空时展示全部笔记，不限制前 10 条。逐行至少包含 `id | title | createdDate | tags`，标签展示使用 `Tag.displayName()` 或等价稳定文本，不依赖 `Set.toString()` 的实现细节。
- `printNoteDetail(NoteView note)` 输出单条详情，至少包含 `笔记详情`、`ID: {id}`、`标题: {title}`、`内容: {content}`、`创建日期: {createdDate}`、`标签: {tags}`。
- 成功新增、查看、修改通过统一笔记详情展示；删除成功复用既有 `printResult`，输出 `操作成功`。
- 服务失败必须复用既有 `printResult(...)` 风格输出 `失败: {ErrorCode} - {message}`。

测试要求：

- 更新既有主菜单入口测试：进入命令 `6` 后应能进入笔记子菜单并通过 `b/back` 返回主菜单；空数据列表通过 `6\nl\nb\n...` 展示。
- 覆盖新增、列表、查看、修改、删除的成功路径，并断言删除后再次查看返回 `NOT_FOUND`。
- 覆盖关键字搜索大小写不敏感行为、标签搜索按 `Tag` 归一语义匹配、组合筛选只返回同时满足关键字与标签的笔记。
- 覆盖空标签列表创建、逗号分隔标签解析、空白标签片段丢弃和重复标签去重的可观察输出。
- 覆盖非法 id、空关键字搜索、空标签搜索、组合筛选全空、服务层标题/内容验证失败，并断言这些失败不会导致程序退出且仍停留在笔记子菜单。
- 覆盖未知笔记命令、空笔记命令、`h/help`、`b/back` 和 EOF 处理。

## 选择理由
v26 已完成控制台收支记录完整交互入口并通过 918 个测试。至此任务待办、日程提醒、学习计划和收支记录四个本地控制台入口已具备完整可输入闭环；个人笔记入口仍只是一次性列表，不能满足“每个核心功能可演示、可输入、可产生明确结果”的验收口径。笔记数据还会影响汇总中的笔记数量、标签分布和 AI 本地上下文，因此应在继续增强 AI 菜单或最终文档前先补齐笔记交互闭环。

## 任务上下文
完整需求要求完成 Java AI 个人学习与生活助手，覆盖任务、日程、学习计划、收支、笔记、汇总、AI 问答和 AI 结构化建议等 8 个核心功能，并提供可运行命令行程序、源码、单元测试、必要文档和构建配置。技术方案要求控制台层只处理菜单、输入解析和展示，业务逻辑全部下沉到服务、领域对象和值对象；控制台层不得直接访问仓储集合或承载业务判断。普通单元测试不得读取真实环境变量、访问真实网络、依赖真实 API Key 或真实当前时间。

笔记管理要求：新增或修改笔记时，服务校验标题和内容非空，并通过 `Tag` 值对象处理标签语义；关键字查询为空属于输入错误，无匹配返回空集合；关键字匹配标题和内容，标签查询按 `Tag` 统一语义比较。AI 摘要由 AI 服务编排，笔记服务只提供上下文，不直接调用外部 API。

## 已有代码上下文
`assistant.app.ConsoleApplication` 已有主菜单、汇总入口、AI 问答入口、AI 草稿列表入口，以及任务、日程、学习计划、收支四个可循环子菜单。当前主菜单命令 `6` 仍调用 `showNotes()`，该方法只执行 `services.noteService().listNotes()`，输出 `笔记列表`，空列表输出 `暂无笔记`，非空时仅展示前 10 条并直接使用 `note.tags()` 输出。

既有 `NoteService` 公开接口包括：

- `OperationResult<NoteView> createNote(String title, String content, Set<String> tagTexts)`
- `OperationResult<NoteView> updateNote(EntityId id, String title, String content, Set<String> tagTexts)`
- `OperationResult<NoteView> getNote(EntityId id)`
- `OperationResult<List<NoteView>> listNotes()`
- `OperationResult<List<NoteView>> listNotes(NoteQuery query)`
- `OperationResult<List<NoteView>> searchByKeyword(String keyword)`
- `OperationResult<List<NoteView>> searchByTag(String tagText)`
- `OperationResult<Void> deleteNote(EntityId id)`

既有 `NoteQuery` 支持 `all()`、`byKeyword(String)`、`byTag(Tag)`、`of(String, Tag)` 和 `matches(Note, NoteSearchPolicy)`；其中 `of(String, Tag)` 要求关键字与标签均非空。既有 `Tag` 会 `strip()`、拒绝空值并使用 `Locale.ROOT` 小写归一，`displayName()` 返回归一后的标签文本。既有 `NoteView` 是只读 record，包含 `EntityId id`、`String title`、`String content`、`LocalDate createdDate`、`Set<Tag> tags`，并已复制为不可修改标签集合。
