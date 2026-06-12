# 任务指令（v28）

## 动作
NEW

## 任务描述
扩展 `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 的 AI 草稿菜单交互，并补充 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 覆盖；必要时可在 `assistant.app` 包内复用或抽取小型输入解析、草稿展示辅助方法，但不得修改 `DraftLifecycleService`、`DraftImportService`、`SuggestionDraftView`、`SuggestionDraft`、`TaskDraftItem`、`StudyPlanDraftContent` 的公开契约。

本轮需要将主菜单命令 `8` 从一次性 `showDrafts()` 列表展示改为可循环 AI 草稿子菜单，支持：

- 列出全部 AI 草稿，不限制前 10 条。
- 查看单个草稿详情。
- 确认导入草稿，成功后展示导入后的草稿状态。
- 取消草稿，成功后展示取消后的草稿状态。
- 对不存在草稿、终态草稿重复确认/取消、导入失败、非法草稿 id、空命令、未知命令、帮助、返回主菜单和 EOF 做稳定展示。

预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java`
- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

## 选择理由
任务待办、日程提醒、学习计划、收支记录和个人笔记五个本地控制台入口已具备完整可输入、可演示、可产生明确结果的闭环；AI 草稿领域、解析、生命周期和正式导入服务也已完成，但主菜单命令 `8` 当前仍只展示草稿列表，无法通过控制台查看详情、确认导入或取消草稿。需求和技术方案明确要求 AI 结构化建议采用“草稿-确认-导入”流程，用户确认或取消后才影响正式任务/学习计划数据，因此下一步应补齐 AI 草稿完整交互入口，并沿用已验证的子菜单、id 解析、错误展示、EOF 和返回主菜单模式。

## 任务上下文
技术方案要求结构化建议流程分为生成草稿、校验草稿、用户确认或取消、导入正式记录四个阶段；AI 原始输出不得直接写入任务或学习计划仓储。取消草稿只改变草稿状态，不修改正式业务数据；确认草稿时生命周期服务先检查草稿存在且状态为 `CONFIRMABLE`，再委托 `DraftImportService` 导入正式任务或学习计划。导入成功后草稿进入 `IMPORTED`，取消后进入 `CANCELLED`，重复确认、取消后确认、已导入后确认或终态取消应返回 `STATE_CONFLICT`，不存在草稿返回 `NOT_FOUND`。控制台层必须只做菜单、输入解析、调用服务和结果展示，不直接操作草稿仓储、任务仓储、学习计划仓储或可变实体。

普通单元测试不得读取真实环境变量、访问真实网络、依赖真实 API Key 或真实当前时间；测试应通过可控输入输出流、固定服务装配或 mock 服务验证草稿菜单行为。

## 已有代码上下文
`ConsoleApplication` 主菜单命令 `8` 当前调用 `showDrafts()`，该方法仅执行 `services.draftLifecycleService().listDrafts()`，输出 `AI 草稿列表`，并对非空结果使用 `limit(10)` 输出 `id | type | status | 任务数量 | 学习计划是否存在`。需要改为 `runDraftMenu()` 子菜单入口，并可保留或改造 `showDrafts()` 为 `listDrafts()` 辅助方法。

既有 `DraftLifecycleService` 已提供：

- `OperationResult<SuggestionDraftView> getDraft(EntityId id)`
- `OperationResult<List<SuggestionDraftView>> listDrafts()`
- `OperationResult<SuggestionDraftView> cancelDraft(EntityId id)`
- `OperationResult<SuggestionDraftView> confirmDraft(EntityId id)`

既有 `SuggestionDraftView` 提供 `id()`、`type()`、`status()`、`tasks()`、`studyPlan()`、`isConfirmable()`；`TaskDraftItem` 提供标题、描述、优先级、可空截止日期和 `hasDueDate()`；`StudyPlanDraftContent` 提供目标名称、开始日期、结束日期、预期小时、初始进度和拆解列表。

控制台层已有可复用模式：

- 任务、日程、学习计划、收支、笔记子菜单均采用 `runXxxMenu()` + `printXxxMenu()` + `dispatchXxxCommand(...)`。
- id 解析错误统一输出 `失败: VALIDATION_ERROR - ... 必须是正整数`。
- 服务失败通过 `printResult(...)` 输出 `失败: {ErrorCode} - {message}`。
- 成功空载荷通过 `printResult(...)` 输出 `操作成功`。
- EOF 时设置 `running = false` 并正常结束。

本轮 AI 草稿子菜单建议命令：

- `l` / `list`：列出全部草稿，调用 `DraftLifecycleService.listDrafts()`。
- `v` / `view`：读取草稿 id，调用 `DraftLifecycleService.getDraft(id)`。
- `c` / `confirm`：读取草稿 id，调用 `DraftLifecycleService.confirmDraft(id)`。
- `x` / `cancel`：读取草稿 id，调用 `DraftLifecycleService.cancelDraft(id)`。
- `b` / `back`：返回主菜单。
- `h` / `help`：展示 AI 草稿子菜单帮助。

草稿列表输出需稳定展示全部草稿，不得截断为前 10 条。草稿详情至少展示：

- `AI 草稿详情`
- `ID: {id}`
- `类型: {type}`
- `状态: {status}`
- 任务草稿条目：标题、优先级、截止日期（缺失时用稳定文本如 `未设置`）、描述。
- 学习计划草稿内容：目标名称、开始日期、截止日期、预期小时、初始进度、拆解条目。

确认导入和取消成功后应复用详情展示，让用户能看到更新后的 `IMPORTED` 或 `CANCELLED` 状态；失败时不得打印旧详情。
