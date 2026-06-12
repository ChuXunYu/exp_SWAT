# 详细设计（v23）

## 概述

本轮设计目标是扩展 `assistant.app.ConsoleApplication` 的任务待办入口：主菜单命令 `2` 不再只执行一次任务列表，而是进入可循环任务子菜单。任务子菜单通过既有 `TaskService` 完成任务新增、查看、列表、筛选、修改、删除、标记完成和撤销完成，并在 EOF、输入解析失败、服务失败、返回主菜单等路径保持控制台行为稳定。

本轮实现范围：

- 修改 `ConsoleApplication`：新增任务子菜单循环、命令帮助、任务输入解析、任务详情展示和任务操作分发。
- 修改 `ConsoleApplicationTest`：扩展基于 `StringReader` / `StringWriter` 的交互测试，覆盖任务子菜单成功路径、失败路径、筛选、EOF、帮助、未知命令和返回主菜单。

本轮不修改：

- `TaskService`、`TaskRepository`、`TaskItem`、`TaskView`、`TaskQuery` 的公开契约。
- 应用装配、AI 配置、网络客户端、持久化或其他业务模块子菜单。
- 独立 CLI 框架或第三方命令解析库。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 将主菜单任务命令接入任务子菜单；新增任务命令分发、字段读取、输入解析、服务调用、列表与详情展示。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 补充任务子菜单交互测试，并调整既有主菜单列表入口断言以适配命令 `2` 行为变化。 |

## 类型定义

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：控制台主循环与任务子菜单交互层，只负责读取输入、解析控制台字段、调用服务和展示 `OperationResult`，不承载任务业务规则。

**现有字段保持不变**：

| 字段签名 | 约束 |
|----------|------|
| `private final ApplicationServices services` | 构造时非空。 |
| `private final BufferedReader input` | 构造时非空；测试通过 `StringReader` 输入。 |
| `private final PrintWriter output` | 构造时非空；测试通过 `StringWriter` 输出。 |
| `private boolean running` | 主菜单与任务子菜单共用的程序运行标记。 |

**公开接口保持不变**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ConsoleApplication(ApplicationServices services, Reader input, Writer output)` | 构造器 | 维持既有空依赖防御和 `BufferedReader` / `PrintWriter` 包装行为。 |
| `public void run()` | `void` | 维持欢迎语、主菜单循环、EOF 正常退出和每轮输出刷新行为。 |

**修改的私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void dispatch(String rawCommand)` | `void` | 主菜单命令 `2` 改为调用 `runTaskMenu()`；其他命令保持既有行为。 |
| `private void printHelp()` | `void` | 主菜单帮助保留任务入口说明；无需列出任务子菜单全部命令。 |
| `private String readLine(String prompt)` | `String` 或 `null` | 保持既有读取入口：输出 prompt 并读取一行；EOF 返回 `null` 且自身不修改 `running`；`IOException` 输出 `输入读取失败，程序退出。`、设置 `running = false` 并返回 `null`。主菜单通过 `dispatch(null)` 退出；任务字段读取辅助方法必须在 EOF 时显式设置 `running = false`。 |

**新增任务子菜单私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void runTaskMenu()` | `void` | 进入时先调用 `printTaskMenu()`；随后在 `running == true` 且未返回主菜单时循环读取任务命令。EOF 或读取失败时设置 `running = false` 并结束程序；命令 `b` / `back` 结束子菜单并返回主菜单；每次命令处理后刷新输出；留在子菜单时不打印主菜单。 |
| `private void printTaskMenu()` | `void` | 输出任务子菜单命令说明，至少包含 `l/list`、`a/add`、`v/view`、`f/filter`、`u/update`、`c/complete`、`r/reopen`、`d/delete`、`b/back`、`h/help`。 |
| `private boolean dispatchTaskCommand(String rawCommand)` | `boolean` | 返回 `true` 表示继续留在任务子菜单，返回 `false` 表示返回主菜单或程序已结束。空命令输出 `请输入任务命令。` 并返回 `true`；未知命令输出 `未知任务命令，请输入 h 查看帮助。` 后展示任务帮助并返回 `true`。 |
| `private void listTasks()` | `void` | 调用 `services.taskService().listTasks()`；成功时复用 `printTaskList("任务列表", tasks)`。 |
| `private void addTask()` | `void` | 依次读取标题、描述、必填优先级、必填截止日期。标题和描述通过 `readTaskRawField(...)` 读取，收到 `null` 表示 EOF 且直接结束程序；优先级或日期返回 `INVALID` 时不调用服务并留在任务子菜单，返回 `EOF` 时结束程序；成功解析后调用 `TaskService.createTask(title, description, priority, dueDate)` 并通过 `printTaskResult(result)` 展示。 |
| `private void viewTask()` | `void` | 读取任务 id；`INVALID` 时不调用服务并留在任务子菜单；`EOF` 时结束程序；`VALUE` 时调用 `TaskService.getTask(id)` 并通过 `printTaskResult(result)` 展示。 |
| `private void filterTasks()` | `void` | 依次读取可选状态、可选优先级、可选截止日期。任一字段返回 `EOF` 时结束程序；任一字段返回 `INVALID` 时不调用服务并留在任务子菜单；`EMPTY` 转换为对应 `TaskQuery` 参数 `null`；`VALUE` 转换为对应枚举或日期；全部合法后构造 `TaskQuery.of(statusOrNull, priorityOrNull, dueDateOrNull)` 并调用 `TaskService.listTasks(query)`，成功时复用 `printTaskList("任务筛选结果", tasks)`。 |
| `private void updateTask()` | `void` | 依次读取任务 id、标题、描述、必填优先级、必填截止日期。id、优先级或日期返回 `INVALID` 时不调用服务并留在任务子菜单；任一读取返回 EOF 状态或原始字段 `null` 时结束程序；标题和描述原样传给服务；成功后调用 `TaskService.updateTask(id, title, description, priority, dueDate)` 并通过 `printTaskResult(result)` 展示。 |
| `private void completeTask()` | `void` | 读取任务 id；`INVALID` 时不调用服务并留在任务子菜单；`EOF` 时结束程序；`VALUE` 时调用 `TaskService.markTaskCompleted(id)` 并通过 `printTaskResult(result)` 展示。 |
| `private void reopenTask()` | `void` | 读取任务 id；`INVALID` 时不调用服务并留在任务子菜单；`EOF` 时结束程序；`VALUE` 时调用 `TaskService.reopenTask(id)` 并通过 `printTaskResult(result)` 展示。 |
| `private void deleteTask()` | `void` | 读取任务 id；`INVALID` 时不调用服务并留在任务子菜单；`EOF` 时结束程序；`VALUE` 时调用 `TaskService.deleteTask(id)` 并通过既有 `printResult(result)` 展示；成功空载荷输出 `操作成功`。 |
| `private void printTaskResult(OperationResult<TaskView> result)` | `void` | 先调用 `printResult(result)`；失败时只输出错误码和消息；成功时调用 `printTaskDetail(result.getPayload())`。 |
| `private void printTaskList(String heading, List<TaskView> tasks)` | `void` | 输出标题；空列表输出 `暂无任务`；非空时按既有任务列表格式逐行输出，格式为 `id | title | priority | status | 截止 dueDate`。不再限制为 10 条，保证测试可见全部交互新增数据。 |
| `private void printTaskDetail(TaskView task)` | `void` | 输出单条任务详情，至少包含 `任务详情`、`ID: {id}`、`标题: {title}`、`优先级: {priority}`、`状态: {status}`、`截止日期: {dueDate}`、`描述: {description}`。 |
| `private String readTaskRawField(String prompt)` | `String` 或 `null` | 调用 `readLine(prompt)`；读取到 EOF 时显式设置 `running = false` 并返回 `null`；读取到真实空行时返回空字符串。用于标题、描述等不由控制台层校验的原始字段，调用方收到 `null` 必须立即停止当前操作。 |
| `private ParsedInput<EntityId> readTaskId(String prompt)` | `ParsedInput<EntityId>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字或超出 `long` 范围时输出包含 `VALIDATION_ERROR` 和 `任务 id 必须是正整数` 的提示，并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(new EntityId(value))`。 |
| `private ParsedInput<TaskPriority> readRequiredTaskPriority(String prompt)` | `ParsedInput<TaskPriority>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空值或非法值输出包含 `VALIDATION_ERROR` 和 `优先级必须是 LOW、MEDIUM 或 HIGH` 的提示，并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(priority)`。 |
| `private ParsedInput<LocalDate> readRequiredTaskDueDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空值或解析失败输出包含 `VALIDATION_ERROR` 和 `截止日期格式必须是 yyyy-MM-dd` 的提示，并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(date)`。 |
| `private ParsedInput<TaskStatus> readOptionalTaskStatus(String prompt)` | `ParsedInput<TaskStatus>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()` 作为无状态筛选；非空输入大小写不敏感匹配 `TODO`、`COMPLETED`；非法值输出包含 `VALIDATION_ERROR` 和 `状态必须是 TODO 或 COMPLETED` 的提示，并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<TaskPriority> readOptionalTaskPriority(String prompt)` | `ParsedInput<TaskPriority>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()` 作为无优先级筛选；非空输入执行 `strip().toUpperCase(Locale.ROOT)` 后匹配 `LOW`、`MEDIUM`、`HIGH`；非法值输出包含 `VALIDATION_ERROR` 和 `优先级必须是 LOW、MEDIUM 或 HIGH` 的提示，并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<LocalDate> readOptionalTaskDueDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()` 作为无截止日期筛选；非空输入使用 `LocalDate.parse(raw.strip())` 解析 ISO `yyyy-MM-dd`；解析失败输出包含 `VALIDATION_ERROR` 和 `截止日期格式必须是 yyyy-MM-dd` 的提示，并返回 `ParsedInput.invalid()`。 |
| `private EntityId parseTaskId(String rawValue)` | `EntityId` 或 `null` | 只做语法解析与正数约束；成功返回 `new EntityId(value)`；失败输出验证错误并返回 `null`。 |
| `private TaskPriority parseTaskPriority(String rawValue)` | `TaskPriority` 或 `null` | 输入大小写不敏感、前后空白忽略；成功返回枚举；失败输出验证错误并返回 `null`。 |
| `private TaskStatus parseTaskStatus(String rawValue)` | `TaskStatus` 或 `null` | 输入大小写不敏感、前后空白忽略；成功返回枚举；失败输出验证错误并返回 `null`。 |
| `private LocalDate parseTaskDueDate(String rawValue)` | `LocalDate` 或 `null` | 使用 ISO 本地日期解析；成功返回日期；失败输出验证错误并返回 `null`。 |
| `private void printValidationError(String message)` | `void` | 输出格式固定为 `失败: VALIDATION_ERROR - {message}`，与 `printResult(...)` 的服务失败风格一致。 |

**新增输入解析辅助类型**：

### `ConsoleApplication.ParsedInput<T>`

**形态**：`private static final class` 或 `private record`

**包路径**：`assistant.app`，作为 `ConsoleApplication` 私有嵌套类型

**职责**：表达控制台字段解析结果，避免用 `null` 同时代表合法空筛选、非法输入和 EOF。

**状态定义**：

| 状态 | 含义 | 调用方行为 |
|------|------|------------|
| `VALUE` | 字段存在且解析成功，`value()` 非空。 | 继续当前操作并使用值。 |
| `EMPTY` | 仅用于筛选字段，用户输入空值，表示该条件不筛选。 | 继续当前操作，并把对应 `TaskQuery` 参数设为 `null`。 |
| `INVALID` | 用户输入不合法，且已输出 `VALIDATION_ERROR`。 | 立即中止当前任务操作，不调用服务，保持在任务子菜单。 |
| `EOF` | 读取到 EOF，且字段读取方法已设置 `running = false`。 | 立即中止当前任务操作和任务子菜单，程序正常结束。 |

**公开给外部**：无。该类型为 `ConsoleApplication` 私有实现细节。

**私有/包内接口**：

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

**命令分发契约**：

| 命令 | 私有方法 | 服务调用 |
|------|----------|----------|
| `l` / `list` | `listTasks()` | `TaskService.listTasks()` |
| `a` / `add` | `addTask()` | `TaskService.createTask(String, String, TaskPriority, LocalDate)` |
| `v` / `view` | `viewTask()` | `TaskService.getTask(EntityId)` |
| `f` / `filter` | `filterTasks()` | `TaskService.listTasks(TaskQuery.of(status, priority, dueDate))` |
| `u` / `update` | `updateTask()` | `TaskService.updateTask(EntityId, String, String, TaskPriority, LocalDate)` |
| `c` / `complete` | `completeTask()` | `TaskService.markTaskCompleted(EntityId)` |
| `r` / `reopen` | `reopenTask()` | `TaskService.reopenTask(EntityId)` |
| `d` / `delete` | `deleteTask()` | `TaskService.deleteTask(EntityId)` |
| `b` / `back` | 子菜单返回 | 不调用服务 |
| `h` / `help` | `printTaskMenu()` | 不调用服务 |

**构造方式**：

- 仍由 `Main` 和测试通过 `new ConsoleApplication(services, input, output)` 构造。

**类型关系**：

- 新增依赖 `assistant.common.EntityId`、`assistant.common.ErrorCode`、`assistant.task.TaskPriority`、`assistant.task.TaskQuery`、`assistant.task.TaskStatus`。
- 新增 Java 标准库依赖 `java.time.LocalDate`、`java.time.format.DateTimeParseException`、`java.util.Locale`。
- 不依赖任何仓储、实体构造器或 HTTP 类型。

### `ConsoleApplicationTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.app`

**职责**：通过完整控制台输入输出验证任务子菜单，不读取真实环境变量、不访问真实网络、不依赖真实 API Key。

**保留并调整的辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static ApplicationServices servicesWithDemoData()` | `ApplicationServices` | 保持固定时间与演示数据装配。 |
| `private static ApplicationServices servicesWithoutDemoData()` | `ApplicationServices` | 保持固定时间与空数据装配。 |
| `private static String runWithInput(ApplicationServices services, String input)` | `String` | 保持 `StringReader` / `StringWriter` 运行方式。 |
| `private static void assertContains(String text, String expected)` | `void` | 保持输出包含断言。 |
| `private static void assertNullRejected(String expectedMessage, Executable executable)` | `void` | 保持空依赖断言。 |

**新增测试辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String between(String text, String startInclusive, String endExclusive)` | `String` | 返回指定输出片段，用于筛选测试断言匹配项出现且非匹配项不出现在筛选输出段中；未找到边界时抛断言失败。 |
| `private static void assertNotContains(String text, String unexpected)` | `void` | 断言指定输出片段不包含非匹配项。 |

**新增或调整的测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `listCommandsDisplayEachCoreEntry()` | 输入 `2\nb\n3\n4\n5\n6\n8\nq\n`；断言任务入口展示任务子菜单或任务命令说明，其他主菜单入口仍可用。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 输入 `2\nl\nb\n3\n4\n5\n6\n8\nq\n`；断言任务空状态仍为 `暂无任务`。 |
| `taskMenuAddsTaskAndSummaryReflectsTodayTaskCount()` | 空数据服务；输入进入任务菜单新增今日任务、列表、返回主菜单、汇总；断言新增任务在列表中可见，且汇总输出 `今日任务数: 1`。 |
| `taskMenuViewsUpdatesDeletesTask()` | 新增任务后查看、修改、删除，再列表或查看；断言查看含原标题，修改后详情含新标题，删除成功输出 `操作成功`，删除后列表不再含新标题或查看输出 `NOT_FOUND`。 |
| `taskMenuCompletesReportsConflictAndReopensTask()` | 新增任务后完成、重复完成、撤销完成；断言首次完成详情状态 `COMPLETED`，重复完成输出 `STATE_CONFLICT`，撤销完成详情状态 `TODO`。 |
| `taskMenuFiltersByStatusPriorityAndDueDate()` | 新增至少两个不同状态、优先级、日期的任务；执行组合筛选；使用 `between(...)` 截取筛选输出段，断言匹配项出现且非匹配项不出现。 |
| `taskMenuRejectsInvalidIdWithoutWriteOperation()` | 输入非法 id 执行查看、更新、完成、撤销或删除之一；断言输出 `VALIDATION_ERROR`，随后列表仍保持已有任务状态不变。 |
| `taskMenuRejectsInvalidDateWithoutWriteOperation()` | 新增或修改时输入非法日期；断言输出 `VALIDATION_ERROR`，随后列表不包含本次待新增标题或原任务未被修改。 |
| `taskMenuRejectsInvalidPriorityWithoutWriteOperation()` | 新增或修改时输入非法优先级；断言输出 `VALIDATION_ERROR`，随后列表不包含本次待新增标题或原任务未被修改。 |
| `taskMenuRejectsInvalidStatusWithoutServiceCall()` | 筛选时输入非法状态；断言输出 `VALIDATION_ERROR`，并通过随后有效列表确认数据未变化。 |
| `taskMenuUnknownHelpBackAndMainMenuContinuation()` | 输入未知任务命令、帮助、返回主菜单、汇总、退出；断言未知任务命令提示、任务帮助、主菜单汇总均出现。 |
| `taskMenuExitsOnEofDuringCommandRead()` | 输入只包含 `2\n`；断言进入任务子菜单后 EOF 正常结束，不抛异常，不要求输出 `已退出。`。 |
| `taskMenuExitsOnEofDuringAddFields()` | 输入 `2\na\n标题\n` 等中途 EOF；断言程序正常结束，不抛异常，未创建半成品任务。 |

**测试数据约束**：

- 任务新增输入字段顺序必须与实现一致：标题、描述、优先级、截止日期。
- 修改输入字段顺序必须与实现一致：任务 id、标题、描述、优先级、截止日期。
- 使用固定日期 `2026-01-15` 作为今日任务，另用 `2026-01-16` 或 `2026-02-01` 构造非匹配项。
- 所有测试通过 `ApplicationFactory.create(Map.of(), new FixedTimeProvider(...))` 装配，不设置真实 API Key，不触发真实 AI 请求。

## 错误处理

- 服务层失败继续统一使用 `printResult(OperationResult<T>)` 输出：`失败: {ErrorCode} - {message}`。
- 控制台解析失败不构造业务服务调用，直接输出 `失败: VALIDATION_ERROR - {清晰提示}`，随后留在任务子菜单。
- `readLine(...)` 读取到 EOF 返回 `null` 且不自行设置 `running = false`；主菜单 EOF 继续由 `dispatch(null)` 结束程序。
- 任务子菜单命令读取到 EOF 时，`runTaskMenu()` 显式设置 `running = false` 并结束程序。
- 任务子菜单字段读取中，`readTaskRawField(...)` 收到 EOF 时设置 `running = false` 并返回 `null`；`readTaskId(...)`、必填枚举/日期读取方法和可选筛选读取方法收到 EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`。调用方收到原始字段 `null` 或 `ParsedInput.eof()` 后不再继续解析或调用服务。
- 可选筛选字段的合法空输入必须返回 `ParsedInput.empty()`；非法输入必须返回 `ParsedInput.invalid()`；二者不得都用 `null` 表达。`EMPTY` 继续构造 `TaskQuery`，对应参数为 `null`；`INVALID` 不调用服务并留在任务子菜单。
- `IOException` 仍由既有 `readLine(...)` 处理，输出 `输入读取失败，程序退出。` 并设置 `running = false`。
- 标题、描述等非解析字段不在控制台层校验，不做 `strip()` 后传递；业务失败由 `TaskService` 返回 `OperationResult` 并由控制台展示。
- 删除成功使用 `OperationResult<Void>` 的既有空载荷成功展示，输出 `操作成功`。

## 行为契约

- 主菜单命令 `2` 进入任务子菜单；任务子菜单命令 `b` / `back` 返回主菜单，返回后主菜单仍可执行汇总、AI 问答、退出等既有命令。
- 进入任务子菜单时必须先展示任务子菜单命令说明。
- 任务子菜单中 `h` / `help` 展示同一任务子菜单命令说明。
- 列表与筛选空结果均输出 `暂无任务`。
- 新增、查看、修改、完成、撤销完成成功后输出单条任务详情，包含 id、标题、优先级、状态、截止日期和描述。
- 列表展示格式复用当前 `showTasks()` 的逐行格式，避免破坏既有可读性。
- 筛选状态接受空值、`TODO`、`COMPLETED`；筛选优先级和截止日期均接受空值；空值代表对应字段不筛选。
- 优先级输入接受 `LOW`、`MEDIUM`、`HIGH`，大小写不敏感，前后空白忽略。
- 截止日期只接受 ISO `yyyy-MM-dd`。
- 任务 id 只接受正整数；`0`、负数、空值、非数字、小数和超出 `long` 范围均为控制台验证错误。

## 依赖关系

- `ConsoleApplication` 依赖既有 `ApplicationServices.taskService()` 获取 `TaskService`。
- `ConsoleApplication` 只调用 `TaskService` 的公开方法：`createTask`、`getTask`、`listTasks()`、`listTasks(TaskQuery)`、`updateTask`、`deleteTask`、`markTaskCompleted`、`reopenTask`。
- `ConsoleApplication` 使用 `TaskQuery.of(...)` 构造筛选条件，不访问仓储集合，不构造或修改 `TaskItem`。
- `ConsoleApplicationTest` 依赖 `ApplicationFactory`、`FixedTimeProvider` 和 `DemoDataFactory` 的既有测试夹具；不得读取真实环境变量或真实网络。

## 修订说明（v23 r2）

| 审查意见 | 修改措施 |
|---------|---------|
| 可选筛选字段用 `null` 同时表达合法空值、非法输入和 EOF，导致非法状态/优先级/日期可能被误当作空筛选条件并继续调用服务。 | 新增 `ConsoleApplication.ParsedInput<T>` 私有辅助类型，显式区分 `VALUE`、`EMPTY`、`INVALID`、`EOF` 四种状态；把可选状态、可选优先级、可选截止日期读取方法改为返回 `ParsedInput<T>`；明确 `EMPTY` 才转换为 `TaskQuery` 的 `null` 参数，`INVALID` 不调用服务并留在任务子菜单，`EOF` 结束程序。 |
| 字段读取 EOF 契约不一致，设计错误地写成依赖 `readLine(...)` 设置 `running = false`，但既有 `readLine(...)` 在 EOF 时只返回 `null`。 | 明确 `readLine(...)` EOF 不修改 `running`；主菜单 EOF 继续由 `dispatch(null)` 处理；任务命令读取、原始字段读取和解析字段读取辅助方法在收到 EOF 时显式设置 `running = false`，并由调用方立即中止当前操作。 |
