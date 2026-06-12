# 详细设计（v28）

## 概述

本轮设计目标是扩展 `assistant.app.ConsoleApplication` 的 AI 草稿入口：主菜单命令 `8` 不再执行一次性 `showDrafts()`，而是进入可循环 AI 草稿子菜单。子菜单通过既有 `DraftLifecycleService` 完成列出全部草稿、查看草稿详情、确认导入、取消草稿、帮助、返回主菜单和 EOF 稳定退出。

本轮实现范围：

- 修改 `ConsoleApplication`：主菜单 AI 草稿命令接入子菜单；新增 AI 草稿命令分发、草稿 id 读取解析、列表展示、详情展示、确认导入和取消展示。
- 修改 `ConsoleApplicationTest`：更新主菜单草稿入口断言，新增 AI 草稿子菜单成功路径、超过 10 条列表、非法 id、服务失败、终态冲突、帮助、返回和 EOF 测试。

本轮不修改：

- `DraftLifecycleService`、`DraftImportService`、`SuggestionDraftView`、`SuggestionDraft`、`TaskDraftItem`、`StudyPlanDraftContent` 的公开契约。
- AI 草稿仓储、导入服务、正式任务仓储、学习计划仓储、AI 解析或 HTTP 客户端。
- 独立 CLI 框架或第三方命令解析库。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 将主菜单命令 `8` 接入 AI 草稿子菜单，新增草稿命令处理、id 解析、列表和详情输出。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 覆盖 AI 草稿子菜单列表、查看、确认、取消、失败、校验、帮助、返回和 EOF 行为。 |

## 类型定义

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：控制台主循环与各核心功能子菜单交互层。AI 草稿相关代码只负责读取输入、解析控制台字段、调用 `DraftLifecycleService` 和展示 `OperationResult`，不直接操作草稿仓储、任务仓储、学习计划仓储或可变实体。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.ai.StudyPlanDraftContent` | 打印学习计划草稿详情。 |
| `assistant.ai.TaskDraftItem` | 打印任务草稿详情。 |

既有 `SuggestionDraftView`、`EntityId`、`OperationResult`、`List`、`Locale`、`Objects` 等依赖继续复用。

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
| `private void dispatch(String rawCommand)` | `void` | 主菜单命令 `8` 改为调用 `runDraftMenu()`；其他主菜单命令保持既有行为。 |
| `private void showDrafts()` | `void` | 删除，或改名为 `listDrafts()` 后仅供 AI 草稿子菜单调用；主菜单不得再直接调用一次性列表入口。 |
| `private void printHelp()` | `void` | 主菜单帮助保留 `8 AI 草稿` 入口说明；无需列出草稿子菜单全部命令。 |

**新增 AI 草稿子菜单私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void runDraftMenu()` | `void` | 进入时先调用 `printDraftMenu()`；随后在 `running == true` 且未返回主菜单时循环读取草稿命令。EOF 或读取失败时设置 `running = false` 并结束程序；命令 `b` / `back` 结束子菜单并返回主菜单；每次命令处理后刷新输出。 |
| `private void printDraftMenu()` | `void` | 输出 AI 草稿子菜单命令说明，至少包含 `l/list`、`v/view`、`c/confirm`、`x/cancel`、`b/back`、`h/help`。 |
| `private boolean dispatchDraftCommand(String rawCommand)` | `boolean` | 返回 `true` 表示继续留在 AI 草稿子菜单，返回 `false` 表示返回主菜单或程序已结束。空命令输出 `请输入 AI 草稿命令。` 并返回 `true`；未知命令输出 `未知 AI 草稿命令，请输入 h 查看帮助。` 后展示草稿帮助并返回 `true`。 |
| `private void listDrafts()` | `void` | 调用 `DraftLifecycleService.listDrafts()`；成功后调用 `printDraftList(result.getPayload())`；失败复用 `printResult(...)`。列表必须展示全部草稿，不得使用 `limit(10)` 或其他截断。 |
| `private void viewDraft()` | `void` | 读取草稿 id；`INVALID` 时不调用服务并留在草稿子菜单；`EOF` 时结束程序；`VALUE` 时调用 `DraftLifecycleService.getDraft(id)` 并通过 `printDraftResult(result)` 展示。 |
| `private void confirmDraft()` | `void` | 读取草稿 id；`INVALID` 时不调用服务；`EOF` 时结束程序；`VALUE` 时调用 `DraftLifecycleService.confirmDraft(id)` 并通过 `printDraftResult(result)` 展示。成功后必须打印返回视图详情，使用户看到 `IMPORTED` 状态；失败时只打印错误，不打印旧详情。 |
| `private void cancelDraft()` | `void` | 读取草稿 id；`INVALID` 时不调用服务；`EOF` 时结束程序；`VALUE` 时调用 `DraftLifecycleService.cancelDraft(id)` 并通过 `printDraftResult(result)` 展示。成功后必须打印返回视图详情，使用户看到 `CANCELLED` 状态；失败时只打印错误，不打印旧详情。 |
| `private void printDraftResult(OperationResult<SuggestionDraftView> result)` | `void` | 先调用 `printResult(result)`；失败时只输出 `失败: {ErrorCode} - {message}`；成功时调用 `printDraftDetail(result.getPayload())`。 |
| `private void printDraftList(List<SuggestionDraftView> drafts)` | `void` | 输出 `AI 草稿列表`；空列表输出 `暂无 AI 草稿`；非空逐行输出全部草稿，格式为 `{id} | {type} | {status} | 任务 {tasksCount} | 学习计划 {hasStudyPlan}`。`tasksCount` 使用 `draft.tasks().size()`；`hasStudyPlan` 使用 `draft.studyPlan().isPresent()`。 |
| `private void printDraftDetail(SuggestionDraftView draft)` | `void` | 输出单条草稿完整详情，至少包含 `AI 草稿详情`、`ID: {id}`、`类型: {type}`、`状态: {status}`、任务草稿区和学习计划草稿区。 |
| `private void printTaskDraftItems(List<TaskDraftItem> tasks)` | `void` | 输出任务草稿条目。空列表输出稳定文本 `任务草稿: 无`；非空先输出 `任务草稿:`，再逐条输出标题、优先级、截止日期和描述。 |
| `private void printTaskDraftItem(int index, TaskDraftItem item)` | `void` | 输出单个任务草稿。格式至少包含 `任务 {index}`、`标题: {title}`、`优先级: {priority}`、`截止日期: {dueDateOrUnset}`、`描述: {description}`；缺失截止日期使用 `未设置`。 |
| `private void printStudyPlanDraft(SuggestionDraftView draft)` | `void` | 若 `draft.studyPlan().isEmpty()` 输出 `学习计划草稿: 无`；否则输出 `学习计划草稿:` 并委托 `printStudyPlanDraftContent(...)`。 |
| `private void printStudyPlanDraftContent(StudyPlanDraftContent content)` | `void` | 输出目标名称、开始日期、截止日期、预期小时、初始进度和拆解条目。初始进度使用 `content.initialProgress().toPercentageString()`。 |
| `private void printStudyPlanDraftBreakdown(List<String> breakdown)` | `void` | 空列表输出 `拆解: 无`；非空先输出 `拆解:`，再按输入顺序逐条输出 `{index}. {item}`。 |
| `private ParsedInput<EntityId> readDraftId(String prompt)` | `ParsedInput<EntityId>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字、小数或超出 `long` 范围时输出 `失败: VALIDATION_ERROR - AI 草稿 id 必须是正整数` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(new EntityId(value))`。 |
| `private EntityId parseDraftId(String rawValue)` | `EntityId` 或 `null` | 只做语法解析与正数约束；成功返回 `new EntityId(value)`；失败输出草稿 id 验证错误并返回 `null`。 |
| `private String formatDraftDueDate(TaskDraftItem item)` | `String` | `item.hasDueDate()` 为 `true` 时返回 `item.dueDate().toString()`；否则返回 `未设置`。 |

**复用的私有接口**：

| 方法签名 | 返回类型 | 复用契约 |
|----------|----------|----------|
| `private void printValidationError(String message)` | `void` | 继续输出固定格式 `失败: VALIDATION_ERROR - {message}`。草稿 id 校验错误复用该输出入口。 |
| `private <T> boolean printResult(OperationResult<T> result)` | `boolean` | 服务失败输出 `失败: {ErrorCode} - {message}`；成功且空载荷输出 `操作成功`。草稿服务成功均带载荷，因此成功后由 `printDraftResult(...)` 打印详情。 |
| `private String readLine(String prompt)` | `String` 或 `null` | EOF 返回 `null` 且自身不修改 `running`；`IOException` 输出 `输入读取失败，程序退出。`、设置 `running = false` 并返回 `null`。 |
| `private record ParsedInput<T>(State state, T value)` | 私有嵌套类型 | 继续表达 `VALUE`、`EMPTY`、`INVALID`、`EOF` 四种字段解析状态；草稿读取方法必须复用该类型，不新增并行状态类型。 |

**命令分发契约**：

| 命令 | 私有方法 | 服务调用 |
|------|----------|----------|
| `l` / `list` | `listDrafts()` | `DraftLifecycleService.listDrafts()` |
| `v` / `view` | `viewDraft()` | `DraftLifecycleService.getDraft(EntityId)` |
| `c` / `confirm` | `confirmDraft()` | `DraftLifecycleService.confirmDraft(EntityId)` |
| `x` / `cancel` | `cancelDraft()` | `DraftLifecycleService.cancelDraft(EntityId)` |
| `b` / `back` | 子菜单返回 | 不调用服务 |
| `h` / `help` | `printDraftMenu()` | 不调用服务 |

**字段读取顺序**：

| 操作 | 字段顺序 |
|------|----------|
| 查看草稿 | 草稿 id |
| 确认导入草稿 | 草稿 id |
| 取消草稿 | 草稿 id |

**输出格式契约**：

| 方法 | 输出要求 |
|------|----------|
| `printDraftList(List<SuggestionDraftView> drafts)` | 先输出 `AI 草稿列表`；空列表输出 `暂无 AI 草稿`；非空每行至少包含 `{id} | {type} | {status} | 任务 {tasksCount} | 学习计划 {hasStudyPlan}`，不限制数量。 |
| `printDraftDetail(SuggestionDraftView draft)` | 输出 `AI 草稿详情`、`ID: {id}`、`类型: {type}`、`状态: {status}`；任务草稿和学习计划草稿均需有稳定区块，缺失时输出 `无`。 |
| `printTaskDraftItem(int index, TaskDraftItem item)` | 输出每条任务草稿的标题、优先级、截止日期和描述；`dueDate == null` 时固定展示 `未设置`。 |
| `printStudyPlanDraftContent(StudyPlanDraftContent content)` | 输出目标名称、开始日期、截止日期、预期小时、初始进度和拆解。 |
| `printDraftResult(OperationResult<SuggestionDraftView> result)` | 查看、确认、取消成功均通过统一详情展示；失败复用 `printResult` 的 `失败: {ErrorCode} - {message}`。 |

**构造方式**：

- 仍由 `Main` 和测试通过 `new ConsoleApplication(services, input, output)` 构造。

**类型关系**：

- `ConsoleApplication` 依赖既有 `ApplicationServices.draftLifecycleService()` 获取 `DraftLifecycleService`。
- `ConsoleApplication` 依赖 `EntityId` 表达经过控制台语法校验的正整数草稿 id。
- `ConsoleApplication` 依赖 `SuggestionDraftView`、`TaskDraftItem`、`StudyPlanDraftContent` 只读取不可变视图数据并格式化输出。
- `ConsoleApplication` 不依赖 `SuggestionDraftRepository`、`SuggestionDraft`、`DraftImportService`、`TaskService`、`StudyPlanService` 或任何仓储集合。

### `ConsoleApplication.ParsedInput<T>`

**形态**：`private record`

**包路径**：`assistant.app`，作为 `ConsoleApplication` 私有嵌套类型

**职责**：继续表达控制台字段解析结果，任务、日程、学习计划、收支、笔记与 AI 草稿子菜单共同复用，避免用 `null` 同时代表非法输入和 EOF。

**状态定义保持不变**：

| 状态 | 含义 | AI 草稿调用方行为 |
|------|------|------------------|
| `VALUE` | 字段存在且解析成功，`value()` 非空。 | 继续当前草稿操作并使用值。 |
| `EMPTY` | 可选字段为空。 | 本轮 AI 草稿不使用该状态。 |
| `INVALID` | 用户输入不合法，且已输出 `VALIDATION_ERROR`。 | 立即中止当前草稿操作，不调用服务，保持在草稿子菜单。 |
| `EOF` | 读取到 EOF，且字段读取方法已设置 `running = false`。 | 立即中止当前草稿操作和草稿子菜单，程序正常结束。 |

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

**职责**：通过完整控制台输入输出验证 AI 草稿子菜单，不读取真实环境变量、不访问真实网络、不依赖真实 API Key 或真实系统时间。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.ai.DraftLifecycleService` | 构造 mock 服务验证控制台校验与服务失败路径。 |
| `assistant.ai.StudyPlanDraftContent` | 构造学习计划草稿测试视图。 |
| `assistant.ai.SuggestionDraftStatus` | 构造不同状态的草稿视图。 |
| `assistant.ai.SuggestionDraftType` | 构造任务或学习计划草稿视图。 |
| `assistant.ai.SuggestionDraftView` | 构造 mock 返回的草稿视图。 |
| `assistant.ai.TaskDraftItem` | 构造任务草稿测试视图。 |
| `assistant.common.Progress` | 构造学习计划草稿初始进度。 |
| `assistant.task.TaskPriority` | 构造任务草稿优先级。 |

既有 `ApplicationServices`、`servicesWithDemoData()`、`servicesWithoutDemoData()`、`runWithInput(...)`、`between(...)`、`assertContains(...)`、`assertNotContains(...)` 等测试辅助继续复用。

**保留并复用的辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static ApplicationServices servicesWithDemoData()` | `ApplicationServices` | 保持固定时间与演示数据装配。 |
| `private static ApplicationServices servicesWithoutDemoData()` | `ApplicationServices` | 保持固定时间 `2026-01-15T09:00` 与空数据装配。 |
| `private static String runWithInput(ApplicationServices services, String input)` | `String` | 保持 `StringReader` / `StringWriter` 运行方式。 |
| `private static void assertContains(String text, String expected)` | `void` | 保持输出包含断言。 |
| `private static void assertNotContains(String text, String unexpected)` | `void` | 继续断言指定输出片段不包含非匹配项。 |
| `private static String between(String text, String startInclusive, String endExclusive)` | `String` | 继续用于截取输出段。 |
| `private static void assertNullRejected(String expectedMessage, Executable executable)` | `void` | 保持空依赖断言。 |

**新增测试辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static SuggestionDraftView taskDraftView(long id, SuggestionDraftStatus status, String title)` | `SuggestionDraftView` | 使用 `SuggestionDraftType.TASK_DRAFT`、传入状态、一个 `TaskDraftItem` 和空学习计划构造任务草稿视图。任务截止日期可固定为 `LocalDate.of(2026, 1, 20)`。 |
| `private static SuggestionDraftView taskDraftViewWithoutDueDate(long id, SuggestionDraftStatus status, String title)` | `SuggestionDraftView` | 构造 `dueDate == null` 的任务草稿视图，用于断言 `未设置`。 |
| `private static SuggestionDraftView studyPlanDraftView(long id, SuggestionDraftStatus status, String goalName)` | `SuggestionDraftView` | 使用 `SuggestionDraftType.STUDY_PLAN_DRAFT`、空任务列表和固定 `StudyPlanDraftContent` 构造学习计划草稿视图。 |
| `private static TaskDraftItem taskDraftItem(String title, LocalDate dueDate)` | `TaskDraftItem` | 使用固定描述 `description`、`TaskPriority.MEDIUM` 和传入截止日期构造任务草稿条目。 |
| `private static StudyPlanDraftContent studyPlanDraftContent(String goalName)` | `StudyPlanDraftContent` | 使用固定开始日期 `2026-01-15`、结束日期 `2026-02-15`、预期小时 `20`、`Progress.of(10)`、拆解 `List.of("阶段一", "阶段二")` 构造学习计划草稿内容。 |
| `private static ApplicationServices withDraftLifecycleService(ApplicationServices baseServices, DraftLifecycleService draftLifecycleService)` | `ApplicationServices` | 基于既有 `ApplicationServices` 构造器替换草稿生命周期服务，其余服务沿用 base。 |

**新增或调整的测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `listCommandsDisplayEachCoreEntry()` | 输入改为 `2\nb\n3\nb\n4\nb\n5\nb\n6\nb\n8\nb\nq\n`；断言 AI 草稿入口进入 `AI 草稿菜单`，不再依赖一次性列表。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 输入改为 `2\nl\nb\n3\nl\nb\n4\nl\nb\n5\nl\nb\n6\nl\nb\n8\nl\nb\nq\n`；断言 AI 草稿空状态通过子菜单列表展示 `暂无 AI 草稿`。 |
| `draftMenuListsAllDraftsWithoutTruncation()` | mock `DraftLifecycleService.listDrafts()` 返回 11 条以上草稿；输入 `8\nl\nb\nq\n`；断言第一条和第十一条均出现在 `AI 草稿列表` 输出段。 |
| `draftMenuViewsTaskDraftDetail()` | mock `getDraft(new EntityId(1))` 返回含任务草稿的 `SuggestionDraftView`；输入 `8\nv\n1\nb\nq\n`；断言输出 `AI 草稿详情`、`ID: 1`、`类型: TASK_DRAFT`、`状态: CONFIRMABLE`、任务标题、优先级、截止日期和描述。 |
| `draftMenuViewsStudyPlanDraftDetail()` | mock `getDraft(new EntityId(2))` 返回含学习计划草稿的 `SuggestionDraftView`；输入 `8\nv\n2\nb\nq\n`；断言输出目标名称、开始日期、截止日期、预期小时、`10%` 初始进度和拆解条目。 |
| `draftMenuDisplaysUnsetDueDateForTaskDraft()` | mock `getDraft(...)` 返回 `dueDate == null` 的任务草稿；断言详情输出 `截止日期: 未设置`。 |
| `draftMenuConfirmsDraftAndDisplaysImportedStatus()` | mock `confirmDraft(new EntityId(1))` 返回 `SuggestionDraftStatus.IMPORTED` 的视图；输入 `8\nc\n1\nb\nq\n`；断言调用确认服务且输出详情包含 `状态: IMPORTED`。 |
| `draftMenuCancelsDraftAndDisplaysCancelledStatus()` | mock `cancelDraft(new EntityId(1))` 返回 `SuggestionDraftStatus.CANCELLED` 的视图；输入 `8\nx\n1\nb\nq\n`；断言调用取消服务且输出详情包含 `状态: CANCELLED`。 |
| `draftMenuDisplaysNotFoundAndStateConflictFailuresWithoutDetail()` | mock `getDraft(...)` 返回 `NOT_FOUND`，`confirmDraft(...)` 或 `cancelDraft(...)` 返回 `STATE_CONFLICT`；断言输出对应错误码和消息，失败输出段不包含成功详情标题 `AI 草稿详情`。 |
| `draftMenuDisplaysImportFailureWithoutOldDetail()` | mock `confirmDraft(...)` 返回 `VALIDATION_ERROR` 或其他导入失败错误；输入 `8\nc\n1\nb\nq\n`；断言只输出失败信息，不输出 `状态: CONFIRMABLE` 或旧详情。 |
| `draftMenuRejectsInvalidIdBeforeCallingDraftLifecycleService()` | 使用 mock `DraftLifecycleService`；分别覆盖查看、确认、取消的非法 id，例如空值、`abc`、`1.5`、`0`、超出 `long` 范围；断言输出 `失败: VALIDATION_ERROR - AI 草稿 id 必须是正整数`，且对应服务方法未被调用。 |
| `draftMenuHandlesUnknownBlankHelpBackAndEof()` | 覆盖未知草稿命令、空草稿命令、`h/help`、`b/back` 和进入草稿菜单后 EOF；断言未知命令和空命令提示稳定，帮助展示 `AI 草稿菜单`，返回后主菜单可继续运行，EOF 正常结束程序。 |
| `draftMenuAcceptsLongCommandAliases()` | mock 生命周期服务；输入 `8\nlist\nview\n1\nconfirm\n1\ncancel\n1\nback\nq\n`；验证长命令别名会分发到对应服务方法。 |

## 错误处理

- 控制台读取 EOF：与其他子菜单一致，草稿字段读取方法设置 `running = false` 并返回 `ParsedInput.eof()`，程序正常结束，不输出额外错误。
- 控制台读取 `IOException`：继续由 `readLine(...)` 输出 `输入读取失败，程序退出。` 并设置 `running = false`。
- 草稿 id 语法错误：控制台层负责拦截，输出 `失败: VALIDATION_ERROR - AI 草稿 id 必须是正整数`，不得调用 `DraftLifecycleService`。
- 不存在草稿：由 `DraftLifecycleService` 返回 `OperationResult.failure(ErrorCode.NOT_FOUND, ...)`，控制台复用 `printResult(...)` 输出。
- 终态草稿重复确认、取消后确认、已导入后确认或终态取消：由 `DraftLifecycleService` 返回 `STATE_CONFLICT`，控制台复用 `printResult(...)` 输出。
- 导入失败：由 `DraftLifecycleService.confirmDraft(...)` 返回失败结果，控制台复用 `printResult(...)` 输出；不得打印旧详情或自行改写状态。
- 服务失败：所有 `OperationResult` 失败都复用 `printResult(...)` 风格输出 `失败: {ErrorCode} - {message}`。

## 行为契约

- 主菜单命令 `8` 必须进入 AI 草稿子菜单；除 EOF、读取失败、退出命令外，用户通过 `b/back` 返回主菜单。
- AI 草稿子菜单每个命令处理完成后仍停留在子菜单，除非命令为 `b/back` 或发生 EOF/读取失败。
- `l/list` 必须调用 `DraftLifecycleService.listDrafts()` 并展示全部返回草稿，不得截断前 10 条。
- `v/view` 必须调用 `DraftLifecycleService.getDraft(id)`；成功后展示当前服务返回视图详情。
- `c/confirm` 必须调用 `DraftLifecycleService.confirmDraft(id)`；成功后展示当前服务返回视图详情，详情状态应来自返回视图。
- `x/cancel` 必须调用 `DraftLifecycleService.cancelDraft(id)`；成功后展示当前服务返回视图详情，详情状态应来自返回视图。
- 失败结果不得打印详情，避免把旧状态误展示为操作结果。
- 控制台层不得直接操作草稿仓储、任务仓储、学习计划仓储、`SuggestionDraft` 可变实体或正式业务服务。
- 草稿详情同时打印任务草稿区和学习计划草稿区；某一区块不存在时输出稳定 `无` 文本。

## 依赖关系

- `ConsoleApplication` 依赖 `ApplicationServices.draftLifecycleService()`、`DraftLifecycleService`、`SuggestionDraftView`、`TaskDraftItem`、`StudyPlanDraftContent`、`EntityId`、`OperationResult`。
- `ConsoleApplicationTest` 可通过 `withDraftLifecycleService(...)` 注入 mock `DraftLifecycleService`，不触发真实网络、真实环境变量或真实 API Key。
- 本轮向后续任务暴露的公开接口无新增；所有新增方法均为 `ConsoleApplication` 私有实现细节或测试私有辅助。
