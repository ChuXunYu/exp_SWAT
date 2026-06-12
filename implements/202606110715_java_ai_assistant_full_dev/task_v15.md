# 任务指令（v15）

## 动作
NEW

## 任务描述
新增个人笔记模块的核心领域实体和关键字搜索策略，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/note/Note.java`
- `java-ai-assistant/src/main/java/assistant/note/NoteSearchPolicy.java`
- `java-ai-assistant/src/test/java/assistant/note/NoteTest.java`
- `java-ai-assistant/src/test/java/assistant/note/NoteSearchPolicyTest.java`

本轮只实现笔记领域模型与搜索策略基础，不实现 `NoteQuery`、`NoteView`、`NoteRepository`、`InMemoryNoteRepository` 或 `NoteService`。

## 选择理由
任务、日程、学习计划和收支四个本地核心模块已经形成服务闭环，可以进入 8 个核心功能中的个人笔记或日记管理。既有 `Tag`、`EntityId` 和时间抽象已经可支撑笔记编号、标签语义和后续服务创建日期；先实现领域实体和搜索策略，可以把标题/内容校验、创建日期、标签集合快照、标签增删和关键字匹配规则集中固定在可测对象中，避免后续服务层重复实现文本清理和标签匹配逻辑。

## 任务上下文
需求要求个人笔记或日记管理支持新增、查看、修改、删除文本笔记；笔记应包含标题、内容、创建日期和标签；程序应支持按关键字或标签查询笔记，并可由 AI 对指定笔记或查询结果生成摘要；该功能应处理空标题、空内容、关键字无匹配、关键字为空、修改不存在记录、删除不存在记录等情况。

OOD 要求：

- `Note` 是个人笔记领域实体，表示带标题、内容、创建日期和标签的文本记录，并负责维护自身内容和标签集合有效性。
- `Tag` 是笔记标签值对象，统一处理空标签、大小写或前后空白等标签语义。
- `NoteSearchPolicy` 负责定义关键字匹配标题、内容或标签的规则，使“关键字为空”“无匹配”“多字段匹配”等分支可独立测试。
- AI 摘要由 AI 应用服务编排，笔记服务只提供上下文，不直接调用外部 API。

技术方案要求：

- 笔记实体持有标题、内容、创建日期和标签集合。
- 标题和内容不能为空。
- 标签通过 `Tag` 值对象统一清理和校验。
- 关键字查询由 `NoteSearchPolicy` 处理。
- 关键字为空属于输入错误，后续服务应返回 `VALIDATION_ERROR`；本轮策略层应拒绝空关键字，不把空关键字解释为匹配全部。
- 无匹配返回空集合不作为错误；本轮策略层只返回 `false`。
- 关键字匹配标题和内容，标签查询按 `Tag` 的统一语义比较。
- 普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## 已有代码上下文
已完成的相关基础：

- `assistant.common.EntityId`：正整数唯一编号值对象，可作为笔记编号。
- `assistant.common.Tag`：标签值对象，已实现首尾空白清理、空标签拒绝、`Locale.ROOT` 小写归一、展示文本和集合/映射键语义。
- `assistant.common.BusinessException`、`ErrorCode`、`OperationResult`：后续服务层错误映射基础。本轮领域对象可继续使用 `IllegalArgumentException` / `NullPointerException` 表达构造与方法参数错误，服务层后续再统一转换。
- 既有任务、日程、学习计划、收支模块均采用“可变领域实体 + 只读视图/仓储/服务后续闭环”的拆分方式；本轮应与这些包的风格一致。

建议接口边界：

- `Note` 使用普通 `final class`，包路径 `assistant.note`。
- `Note` 字段至少包括 `EntityId id`、`String title`、`String content`、`LocalDate createdDate`、`Set<Tag> tags`。
- `Note` 构造器拒绝空编号、空标题、空内容、空创建日期、空标签集合引用和标签集合中的空元素；标题和内容执行 `strip()` 后不得为空；标签集合保存为脱离调用方输入集合的副本。
- `Note` 对外返回不可修改的标签快照，调用方不得通过返回集合修改内部状态。
- `Note` 支持修改标题和内容，建议方法名为 `updateContent(String title, String content)` 或等价清晰命名；非法输入必须保持原状态不变。
- `Note` 支持替换全部标签、添加标签、移除标签；空标签参数拒绝；重复添加同一归一标签后集合不重复；移除不存在标签返回 `false` 或等价稳定语义但不得报错。
- `NoteSearchPolicy` 使用普通 `final class`，包路径 `assistant.note`。
- `NoteSearchPolicy.matchesKeyword(Note note, String keyword)` 或等价公开方法必须拒绝空 `note`、空 `keyword` 和 `strip()` 后为空的关键字。
- 关键字匹配标题和内容必须采用大小写不敏感包含匹配，并使用 `Locale.ROOT` 做大小写归一，作为后续 `NoteQuery` / `NoteService` 的唯一公开语义；不得实现为大小写敏感匹配。
- 标签匹配必须使用 `Tag.of(keyword)` 与笔记标签集合做归一语义下的精确匹配，从而复用 `Tag` 的大小写和空白处理规则。若关键字无法形成合法 `Tag`，标题/内容仍可匹配，标签分支视为不匹配。
- 替换全部标签的方法必须先完整校验输入集合引用、集合元素非空和所有标签有效性，再替换内部状态；任何非法输入都必须保持原标签集合不变，不得先清空或部分写入后再失败。
- 本轮测试应覆盖标题/内容/日期/标签校验、文本规范化、标签集合不可修改和快照隔离、更新原子性、标签替换失败后状态不变、标签增删、关键字匹配标题、内容、标签、多字段、`Locale.ROOT` 大小写不敏感行为、空关键字拒绝和无匹配返回 `false`。

## 修订说明（v15 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| `NoteSearchPolicy` 的标题和内容关键字匹配大小写规则只写为“建议”，与测试覆盖大小写行为之间存在公开语义分叉。 | 将大小写不敏感包含匹配改为强制契约，明确必须使用 `Locale.ROOT` 归一，且不得实现为大小写敏感匹配；同步要求测试断言该唯一行为。 |
| 标签替换操作没有明确失败原子性，包含 `null` 或非法标签时可能先清空或部分写入内部集合。 | 明确替换全部标签必须先完整校验输入集合引用、元素非空和所有标签有效性，再替换内部状态；任何非法输入必须保持原标签集合不变，并要求测试覆盖失败后状态不变。 |
