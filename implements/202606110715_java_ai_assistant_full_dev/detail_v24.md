# 详细设计（v24）

## 概述

本轮设计目标是扩展 `assistant.app.ConsoleApplication` 的日程提醒入口：主菜单命令 `3` 不再只执行一次日程列表，而是进入可循环日程子菜单。日程子菜单通过既有 `ScheduleService` 完成日程列表、新增、查看、筛选、修改、删除、帮助、返回主菜单和 EOF 稳定退出。

本轮实现范围：

- 修改 `ConsoleApplication`：主菜单日程命令接入日程子菜单；新增日程命令分发、字段读取、日期时间解析、日程范围构造、列表与详情展示。
- 修改 `ConsoleApplicationTest`：扩展基于 `StringReader` / `StringWriter` 的交互测试，覆盖日程子菜单成功路径、验证失败、冲突拒绝、筛选、服务失败、未知命令、帮助、返回主菜单和 EOF 场景。

本轮不修改：

- `ScheduleService`、`ScheduleQuery`、`ScheduleView`、`ScheduleStatus`、`DateTimeRange` 的公开契约。
- 日程仓储、冲突策略、领域实体、汇总服务、应用装配、AI、任务或其他业务模块。
- 独立 CLI 框架或第三方命令解析库。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 将主菜单日程命令接入日程子菜单；新增日程命令分发、字段读取、解析校验、服务调用、列表与详情输出。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 调整既有日程入口断言以适配命令 `3` 行为变化；新增日程子菜单交互测试。 |

## 类型定义

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：控制台主循环、任务子菜单与日程子菜单交互层。日程相关代码只负责读取输入、解析控制台字段、调用 `ScheduleService` 和展示 `OperationResult`，不承载日程冲突、状态推导、字段业务校验或仓储访问规则。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.common.DateTimeRange` | 根据开始/结束时间构造日程时间区间。 |
| `assistant.schedule.ScheduleQuery` | 构造日程筛选条件。 |
| `assistant.schedule.ScheduleStatus` | 解析可选日程状态筛选条件。 |
| `java.time.LocalDateTime` | 解析日程开始/结束时间。 |

现有 `EntityId`、`OperationResult`、`LocalDate`、`DateTimeParseException`、`Locale`、`List`、`Objects` 等依赖继续复用。

**现有字段保持不变**：

| 字段签名 | 约束 |
|----------|------|
| `private final ApplicationServices services` | 构造时非空。 |
| `private final BufferedReader input` | 构造时非空；测试通过 `StringReader` 输入。 |
| `private final PrintWriter output` | 构造时非空；测试通过 `StringWriter` 输出。 |
| `private boolean running` | 主菜单、任务子菜单与日程子菜单共用的程序运行标记。 |

**公开接口保持不变**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ConsoleApplication(ApplicationServices services, Reader input, Writer output)` | 构造器 | 维持既有空依赖防御和 `BufferedReader` / `PrintWriter` 包装行为。 |
| `public void run()` | `void` | 维持欢迎语、主菜单循环、EOF 正常退出和每轮输出刷新行为。 |

**修改的私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void dispatch(String rawCommand)` | `void` | 主菜单命令 `3` 改为调用 `runScheduleMenu()`；命令 `2` 继续调用任务子菜单，其他命令保持既有行为。 |
| `private void printHelp()` | `void` | 主菜单帮助保留日程入口说明；无需列出日程子菜单全部命令。 |

**替换/新增日程子菜单私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void runScheduleMenu()` | `void` | 进入时先调用 `printScheduleMenu()`；随后在 `running == true` 且未返回主菜单时循环读取日程命令。EOF 或读取失败时设置 `running = false` 并结束程序；命令 `b` / `back` 结束子菜单并返回主菜单；每次命令处理后刷新输出。 |
| `private void printScheduleMenu()` | `void` | 输出日程子菜单命令说明，至少包含 `l/list`、`a/add`、`v/view`、`f/filter`、`u/update`、`d/delete`、`b/back`、`h/help`。 |
| `private boolean dispatchScheduleCommand(String rawCommand)` | `boolean` | 返回 `true` 表示继续留在日程子菜单，返回 `false` 表示返回主菜单或程序已结束。空命令输出 `请输入日程命令。` 并返回 `true`；未知命令输出 `未知日程命令，请输入 h 查看帮助。` 后展示日程帮助并返回 `true`。 |
| `private void listSchedules()` | `void` | 调用 `services.scheduleService().listSchedules()`；成功时复用 `printScheduleList("日程列表", schedules)`。 |
| `private void addSchedule()` | `void` | 依次读取名称、开始时间、结束时间、地点、备注。名称、地点、备注作为原始字段传给服务；开始/结束时间为必填 ISO 本地日期时间；成功构造 `DateTimeRange` 后调用 `ScheduleService.createSchedule(name, timeRange, location, note)` 并通过 `printScheduleResult(result)` 展示。 |
| `private void viewSchedule()` | `void` | 读取日程 id；`INVALID` 时不调用服务并留在日程子菜单；`EOF` 时结束程序；`VALUE` 时调用 `ScheduleService.getSchedule(id)` 并通过 `printScheduleResult(result)` 展示。 |
| `private void filterSchedules()` | `void` | 依次读取可选日期、可选状态。任一字段返回 `EOF` 时结束程序；任一字段返回 `INVALID` 时不调用服务并留在日程子菜单；`EMPTY` 转换为 `ScheduleQuery` 参数 `null`；全部合法后构造 `ScheduleQuery.of(dateOrNull, statusOrNull)` 并调用 `ScheduleService.listSchedules(query)`，成功时复用 `printScheduleList("日程筛选结果", schedules)`。 |
| `private void updateSchedule()` | `void` | 依次读取日程 id、名称、开始时间、结束时间、地点、备注。id 或时间字段返回 `INVALID` 时不调用服务并留在日程子菜单；任一读取返回 EOF 状态或原始字段 `null` 时结束程序；成功构造 `DateTimeRange` 后调用 `ScheduleService.updateSchedule(id, name, timeRange, location, note)` 并通过 `printScheduleResult(result)` 展示。 |
| `private void deleteSchedule()` | `void` | 读取日程 id；`INVALID` 时不调用服务并留在日程子菜单；`EOF` 时结束程序；`VALUE` 时调用 `ScheduleService.deleteSchedule(id)` 并通过既有 `printResult(result)` 展示；成功空载荷输出 `操作成功`。 |
| `private void printScheduleResult(OperationResult<ScheduleView> result)` | `void` | 先调用 `printResult(result)`；失败时只输出错误码和消息；成功时调用 `printScheduleDetail(result.getPayload())`。 |
| `private void printScheduleList(String heading, List<ScheduleView> schedules)` | `void` | 输出标题；空列表输出 `暂无日程`；非空逐行输出全部日程，不限制 10 条。逐行格式为 `id | name | status | startDateTime ~ endDateTime | location`。 |
| `private void printScheduleDetail(ScheduleView schedule)` | `void` | 输出单条日程详情，至少包含 `日程详情`、`ID: {id}`、`名称: {name}`、`状态: {status}`、`开始时间: {startDateTime}`、`结束时间: {endDateTime}`、`地点: {location}`、`备注: {note}`。 |
| `private String readScheduleRawField(String prompt)` | `String` 或 `null` | 调用 `readLine(prompt)`；读取到 EOF 时显式设置 `running = false` 并返回 `null`；读取到真实空行时返回空字符串。用于名称、地点、备注等不由控制台层做业务校验的原始字段。 |
| `private ParsedInput<EntityId> readScheduleId(String prompt)` | `ParsedInput<EntityId>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字或超出 `long` 范围时输出 `失败: VALIDATION_ERROR - 日程 id 必须是正整数` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(new EntityId(value))`。 |
| `private ParsedInput<LocalDateTime> readRequiredScheduleDateTime(String prompt)` | `ParsedInput<LocalDateTime>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空值或解析失败输出 `失败: VALIDATION_ERROR - 日程时间格式必须是 yyyy-MM-ddTHH:mm` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(dateTime)`。 |
| `private ParsedInput<LocalDate> readOptionalScheduleDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()` 作为无日期筛选；非空输入使用 `LocalDate.parse(raw.strip())` 解析；解析失败输出 `失败: VALIDATION_ERROR - 日程日期格式必须是 yyyy-MM-dd` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<ScheduleStatus> readOptionalScheduleStatus(String prompt)` | `ParsedInput<ScheduleStatus>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()` 作为无状态筛选；非空输入大小写不敏感匹配 `UPCOMING`、`ONGOING`、`EXPIRED`；非法值输出 `失败: VALIDATION_ERROR - 状态必须是 UPCOMING、ONGOING 或 EXPIRED` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<DateTimeRange> readScheduleTimeRange(String startPrompt, String endPrompt)` | `ParsedInput<DateTimeRange>` | 先后读取必填开始时间和结束时间。任一读取未得到 `VALUE` 时原样返回 `EOF` 或 `INVALID`；两个时间都合法后构造 `new DateTimeRange(start, end)`；若构造抛出 `IllegalArgumentException`，输出 `失败: VALIDATION_ERROR - 结束时间必须晚于开始时间` 并返回 `ParsedInput.invalid()`。 |
| `private EntityId parseScheduleId(String rawValue)` | `EntityId` 或 `null` | 只做语法解析与正数约束；成功返回 `new EntityId(value)`；失败输出日程 id 验证错误并返回 `null`。 |
| `private LocalDateTime parseScheduleDateTime(String rawValue)` | `LocalDateTime` 或 `null` | 使用 `LocalDateTime.parse(rawValue.strip())` 解析 ISO 本地日期时间；成功返回日期时间；失败输出日程时间格式验证错误并返回 `null`。 |
| `private LocalDate parseScheduleDate(String rawValue)` | `LocalDate` 或 `null` | 使用 `LocalDate.parse(rawValue.strip())` 解析 ISO 日期；成功返回日期；失败输出日程日期格式验证错误并返回 `null`。 |
| `private ScheduleStatus parseScheduleStatus(String rawValue)` | `ScheduleStatus` 或 `null` | 输入大小写不敏感、前后空白忽略；成功返回枚举；失败输出状态验证错误并返回 `null`。 |

**复用的私有接口**：

| 方法签名 | 返回类型 | 复用契约 |
|----------|----------|----------|
| `private void printValidationError(String message)` | `void` | 继续输出固定格式 `失败: VALIDATION_ERROR - {message}`。任务与日程解析错误均复用该输出入口。 |
| `private <T> boolean printResult(OperationResult<T> result)` | `boolean` | 服务失败输出 `失败: {ErrorCode} - {message}`；成功且空载荷输出 `操作成功`。 |
| `private String readLine(String prompt)` | `String` 或 `null` | EOF 返回 `null` 且自身不修改 `running`；`IOException` 输出 `输入读取失败，程序退出。`、设置 `running = false` 并返回 `null`。 |
| `private record ParsedInput<T>(State state, T value)` | 私有嵌套类型 | 继续表达 `VALUE`、`EMPTY`、`INVALID`、`EOF` 四种字段解析状态；日程读取方法必须复用该类型，不新增并行状态类型。 |

**命令分发契约**：

| 命令 | 私有方法 | 服务调用 |
|------|----------|----------|
| `l` / `list` | `listSchedules()` | `ScheduleService.listSchedules()` |
| `a` / `add` | `addSchedule()` | `ScheduleService.createSchedule(String, DateTimeRange, String, String)` |
| `v` / `view` | `viewSchedule()` | `ScheduleService.getSchedule(EntityId)` |
| `f` / `filter` | `filterSchedules()` | `ScheduleService.listSchedules(ScheduleQuery.of(date, status))` |
| `u` / `update` | `updateSchedule()` | `ScheduleService.updateSchedule(EntityId, String, DateTimeRange, String, String)` |
| `d` / `delete` | `deleteSchedule()` | `ScheduleService.deleteSchedule(EntityId)` |
| `b` / `back` | 子菜单返回 | 不调用服务 |
| `h` / `help` | `printScheduleMenu()` | 不调用服务 |

**字段读取顺序**：

| 操作 | 字段顺序 |
|------|----------|
| 新增日程 | 名称、开始时间、结束时间、地点、备注 |
| 查看日程 | 日程 id |
| 筛选日程 | 日期、状态 |
| 修改日程 | 日程 id、名称、开始时间、结束时间、地点、备注 |
| 删除日程 | 日程 id |

**构造方式**：

- 仍由 `Main` 和测试通过 `new ConsoleApplication(services, input, output)` 构造。

**类型关系**：

- `ConsoleApplication` 依赖既有 `ApplicationServices.scheduleService()` 获取 `ScheduleService`。
- `ConsoleApplication` 依赖 `DateTimeRange` 构造左闭右开时间区间，但只捕获并展示可预期的输入范围错误。
- `ConsoleApplication` 依赖 `ScheduleQuery.of(LocalDate, ScheduleStatus)` 表达筛选条件。
- `ConsoleApplication` 不依赖 `ScheduleRepository`、`ScheduleItem`、`ScheduleConflictPolicy` 或任何仓储集合。

### `ConsoleApplication.ParsedInput<T>`

**形态**：`private record`

**包路径**：`assistant.app`，作为 `ConsoleApplication` 私有嵌套类型

**职责**：继续表达控制台字段解析结果，日程子菜单与任务子菜单共同复用，避免用 `null` 同时代表合法空筛选、非法输入和 EOF。

**状态定义保持不变**：

| 状态 | 含义 | 日程调用方行为 |
|------|------|----------------|
| `VALUE` | 字段存在且解析成功，`value()` 非空。 | 继续当前日程操作并使用值。 |
| `EMPTY` | 仅用于筛选字段，用户输入空值，表示该条件不筛选。 | 继续当前操作，并把对应 `ScheduleQuery` 参数设为 `null`。 |
| `INVALID` | 用户输入不合法，且已输出 `VALIDATION_ERROR`。 | 立即中止当前日程操作，不调用服务，保持在日程子菜单。 |
| `EOF` | 读取到 EOF，且字段读取方法已设置 `running = false`。 | 立即中止当前日程操作和日程子菜单，程序正常结束。 |

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

**职责**：通过完整控制台输入输出验证日程子菜单，不读取真实环境变量、不访问真实网络、不依赖真实系统时间。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.schedule.ScheduleService` | Mockito mock 日程服务失败路径。 |
| `assistant.schedule.ScheduleView` | 构造 mock 返回值时需要的载荷类型。 |
| `java.time.LocalDate` | 构造筛选或 mock 验证数据时按需使用。 |
| `assistant.common.DateTimeRange` | 构造 mock `ScheduleView` 时按需使用。 |
| `assistant.common.EntityId` | 构造 mock `ScheduleView` 时按需使用。 |
| `assistant.schedule.ScheduleStatus` | 构造 mock `ScheduleView` 或断言状态时按需使用。 |

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

**新增或调整的测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `listCommandsDisplayEachCoreEntry()` | 输入 `2\nb\n3\nb\n4\n5\n6\n8\nq\n`；断言任务入口展示任务子菜单，日程入口展示日程子菜单，其他主菜单入口仍可用。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 输入 `2\nl\nb\n3\nl\nb\n4\n5\n6\n8\nq\n`；断言任务空状态和日程空状态均可通过子菜单列表展示。 |
| `scheduleCommandDisplaysFailureCodeAndMessage()` | 使用 mock `ScheduleService.listSchedules()` 返回失败；输入 `3\nl\nb\nq\n`；断言输出服务失败错误码和消息。 |
| `scheduleMenuAddsScheduleAndSummaryReflectsTodayScheduleCount()` | 空数据服务；输入进入日程菜单新增今日日程、列表、返回主菜单、汇总；断言新增日程在列表中可见，且汇总输出 `今日日程数: 1`。 |
| `scheduleMenuViewsUpdatesDeletesSchedule()` | 新增日程后查看、修改、删除，再查看；断言查看含旧名称，修改后详情含新名称/新地点/新备注，删除成功输出 `操作成功`，删除后查看输出 `NOT_FOUND`。 |
| `scheduleMenuRejectsOverlappingScheduleAndAllowsAdjacentSchedule()` | 新增 `09:30-10:00` 日程，再新增 `09:45-10:15` 返回 `SCHEDULE_CONFLICT`，再新增 `10:00-10:30` 成功；断言冲突项不出现在列表中，首尾相接项出现在列表中。 |
| `scheduleMenuFiltersByDateAndStatus()` | 固定当前时间 `2026-01-15T09:00`；新增一个进行中日程和一个非匹配日期或非匹配状态日程；执行日期+状态筛选；使用 `between(...)` 截取 `日程筛选结果` 到 `主菜单`，断言匹配项出现且非匹配项不出现。 |
| `scheduleMenuFilterEmptyFieldsListAllSchedules()` | 新增至少两个日程；执行筛选时日期和状态均输入空行；断言筛选结果包含全部日程。 |
| `scheduleMenuRejectsInvalidIdWithoutWriteOperation()` | 输入非法 id 执行查看、修改或删除之一；断言输出 `VALIDATION_ERROR` 和 `日程 id 必须是正整数`，随后列表仍保持已有日程状态不变。 |
| `scheduleMenuRejectsInvalidDateTimeWithoutWriteOperation()` | 新增或修改时输入非法开始/结束时间；断言输出 `VALIDATION_ERROR` 和 `日程时间格式必须是 yyyy-MM-ddTHH:mm`，随后列表不包含本次待新增名称或原日程未被修改。 |
| `scheduleMenuRejectsInvalidDateWithoutServiceCall()` | 筛选时输入非法日期；断言输出 `VALIDATION_ERROR` 和 `日程日期格式必须是 yyyy-MM-dd`，随后有效列表确认数据未变化。 |
| `scheduleMenuRejectsInvalidStatusWithoutServiceCall()` | 筛选时输入非法状态；断言输出 `VALIDATION_ERROR` 和 `状态必须是 UPCOMING、ONGOING 或 EXPIRED`，随后有效列表确认数据未变化。 |
| `scheduleMenuRejectsEndNotAfterStartWithoutWriteOperation()` | 新增或修改时结束时间早于或等于开始时间；断言输出 `VALIDATION_ERROR` 和 `结束时间必须晚于开始时间`，随后列表不包含本次待新增名称或原日程未被修改。 |
| `scheduleMenuUnknownHelpBackAndMainMenuContinuation()` | 输入未知日程命令、帮助、返回主菜单、汇总、退出；断言未知日程命令提示、日程帮助、主菜单汇总均出现。 |
| `scheduleMenuBlankCommandPromptsAgainAndStaysInScheduleMenu()` | 输入空日程命令后执行列表；断言输出 `请输入日程命令。` 且随后仍展示 `日程列表`。 |
| `scheduleMenuExitsOnEofDuringCommandRead()` | 输入只包含 `3\n`；断言进入日程子菜单后 EOF 正常结束，不抛异常。 |
| `scheduleMenuExitsOnEofDuringAddFields()` | 输入 `3\na\n半成品日程\n` 等中途 EOF；断言程序正常结束，不抛异常，未输出 `日程详情`，未创建半成品日程。 |

**测试数据约束**：

- 固定当前时间由 `servicesWithoutDemoData()` 提供：`2026-01-15T09:00`。
- 日程新增输入字段顺序必须与实现一致：名称、开始时间、结束时间、地点、备注。
- 修改输入字段顺序必须与实现一致：日程 id、名称、开始时间、结束时间、地点、备注。
- 使用 ISO 本地日期时间输入，例如 `2026-01-15T09:30`、`2026-01-15T10:00`。
- 进行中状态示例可使用 `2026-01-15T08:30` 到 `2026-01-15T09:30`；即将开始状态示例可使用 `2026-01-15T10:00` 到 `2026-01-15T10:30`；已过期状态示例可使用 `2026-01-15T07:00` 到 `2026-01-15T08:00`。
- 冲突测试中重叠区间应断言 `SCHEDULE_CONFLICT`，首尾相接区间应断言成功详情或列表可见。
- 所有测试通过 `ApplicationFactory.create(Map.of(), new FixedTimeProvider(...))` 或局部 Mockito mock 装配，不设置真实 API Key，不触发真实 AI 请求。

## 错误处理

- 服务层失败继续统一使用 `printResult(OperationResult<T>)` 输出：`失败: {ErrorCode} - {message}`。
- 控制台解析失败不构造业务服务调用，直接输出 `失败: VALIDATION_ERROR - {清晰提示}`，随后留在日程子菜单。
- 日程 id 非正整数、空值、非数字、小数或超出 `long` 范围统一输出 `日程 id 必须是正整数`。
- 必填开始/结束时间为空或格式错误统一输出 `日程时间格式必须是 yyyy-MM-ddTHH:mm`。
- `DateTimeRange` 构造时若结束时间不晚于开始时间，控制台层捕获 `IllegalArgumentException` 并统一输出 `结束时间必须晚于开始时间`，不让运行时异常泄漏到 `run()` 调用方。
- 筛选日期为空属于合法空筛选；非空格式错误输出 `日程日期格式必须是 yyyy-MM-dd`。
- 筛选状态为空属于合法空筛选；非空非法输出 `状态必须是 UPCOMING、ONGOING 或 EXPIRED`。
- 名称、地点、备注作为原始字段传给服务，不在控制台层做业务校验，不做 `strip()` 后传递；字段为空、空白或 `null` 之外的业务错误由 `ScheduleService` 返回 `OperationResult` 并由控制台展示。
- `readLine(...)` 读取到 EOF 返回 `null` 且不自行设置 `running = false`；主菜单 EOF 继续由 `dispatch(null)` 处理。
- 日程子菜单命令读取到 EOF 时，`runScheduleMenu()` 显式设置 `running = false` 并结束程序。
- 日程子菜单字段读取中，`readScheduleRawField(...)` 收到 EOF 时设置 `running = false` 并返回 `null`；日程 id、必填日期时间和可选筛选读取方法收到 EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`。调用方收到原始字段 `null` 或 `ParsedInput.eof()` 后不再继续解析或调用服务。
- 可选筛选字段的合法空输入必须返回 `ParsedInput.empty()`；非法输入必须返回 `ParsedInput.invalid()`；二者不得都用 `null` 表达。`EMPTY` 继续构造 `ScheduleQuery`，对应参数为 `null`；`INVALID` 不调用服务并留在日程子菜单。
- `IOException` 仍由既有 `readLine(...)` 处理，输出 `输入读取失败，程序退出。` 并设置 `running = false`。
- 删除成功使用 `OperationResult<Void>` 的既有空载荷成功展示，输出 `操作成功`。

## 行为契约

- 主菜单命令 `3` 进入日程子菜单；日程子菜单命令 `b` / `back` 返回主菜单，返回后主菜单仍可执行汇总、任务、AI 问答、退出等既有命令。
- 进入日程子菜单时必须先展示日程子菜单命令说明。
- 日程子菜单中 `h` / `help` 展示同一日程子菜单命令说明。
- 日程子菜单中空命令输出 `请输入日程命令。` 并继续留在日程子菜单。
- 日程子菜单中未知命令输出 `未知日程命令，请输入 h 查看帮助。`，随后展示帮助并继续留在日程子菜单。
- 列表与筛选空结果均输出 `暂无日程`。
- 新增、查看、修改成功后输出单条日程详情，包含 id、名称、状态、开始时间、结束时间、地点和备注。
- 列表展示全部日程，不再限制为 10 条；格式保持可读，并包含 id、名称、状态、开始结束时间和地点。
- 筛选日期接受空值或 ISO `yyyy-MM-dd`；空值代表不按日期筛选。
- 筛选状态接受空值、`UPCOMING`、`ONGOING`、`EXPIRED`，大小写不敏感，前后空白忽略；空值代表不按状态筛选。
- 日程时间只接受 ISO 本地日期时间，交互提示和错误消息固定为 `yyyy-MM-ddTHH:mm`。
- 日程 id 只接受正整数；`0`、负数、空值、非数字、小数和超出 `long` 范围均为控制台验证错误。
- 新增或修改与已有日程存在非空时间重叠时，由 `ScheduleService` 返回 `SCHEDULE_CONFLICT`；控制台只展示失败结果，不自行修改或回滚仓储。
- 首尾相接的日程不应被控制台层拦截，必须交给服务层和冲突策略判断并允许成功。
- 主菜单汇总仍通过 `SummaryService` 读取日程服务，新增或删除日程后下一次汇总应反映当前数据。

## 依赖关系

- `ConsoleApplication` 依赖既有 `ApplicationServices.scheduleService()` 获取 `ScheduleService`。
- `ConsoleApplication` 只调用 `ScheduleService` 的公开方法：`createSchedule`、`getSchedule`、`listSchedules()`、`listSchedules(ScheduleQuery)`、`updateSchedule`、`deleteSchedule`。
- `ConsoleApplication` 使用 `ScheduleQuery.of(...)` 构造筛选条件，不访问仓储集合，不构造或修改 `ScheduleItem`。
- `ConsoleApplication` 使用 `DateTimeRange` 表达开始和结束时间区间，只在控制台层捕获构造输入非法错误并转换为稳定展示消息。
- `ConsoleApplicationTest` 依赖 `ApplicationFactory`、`FixedTimeProvider` 和既有测试辅助方法；不得读取真实环境变量或真实网络。
- Mockito 仅用于构造 `ScheduleService` 服务失败等控制台展示路径，不用于替代普通成功路径的真实服务集成行为。
