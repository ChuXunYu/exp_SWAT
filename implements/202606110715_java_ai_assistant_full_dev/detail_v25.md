# 详细设计（v25）

## 概述

本轮设计目标是扩展 `assistant.app.ConsoleApplication` 的学习计划入口：主菜单命令 `4` 不再执行一次性 `showStudyPlans()`，而是进入可循环学习计划子菜单。学习计划子菜单通过既有 `StudyPlanService` 完成学习计划列表、新增、查看、筛选、修改详情、更新进度、删除、帮助、返回主菜单和 EOF 稳定退出。

本轮实现范围：

- 修改 `ConsoleApplication`：主菜单学习计划命令接入学习计划子菜单；新增学习计划命令分发、字段读取、输入解析校验、`DateRange` 与 `StudyPlanQuery` 构造、列表与详情输出。
- 修改 `ConsoleApplicationTest`：扩展基于 `StringReader` / `StringWriter` 的交互测试，覆盖学习计划子菜单成功路径、验证失败、服务失败、长命令别名、大小写不敏感状态筛选、未知命令、帮助、返回主菜单和 EOF 场景。

本轮不修改：

- `StudyPlanService`、`StudyPlanQuery`、`StudyPlanView`、`StudyPlanStatus` 的公开契约。
- 学习计划实体、仓储、分析服务、汇总服务、AI 草稿导入、应用装配或其他业务模块。
- 独立 CLI 框架或第三方命令解析库。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 将主菜单命令 `4` 接入学习计划子菜单，新增学习计划命令处理、字段读取解析、筛选构造、列表与详情展示。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 调整学习计划入口断言，新增学习计划子菜单交互测试与服务失败测试。 |

## 类型定义

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：控制台主循环、任务子菜单、日程子菜单与学习计划子菜单交互层。学习计划相关代码只负责读取输入、解析控制台字段、调用 `StudyPlanService` 和展示 `OperationResult`，不承载学习计划业务校验、状态推导或仓储访问规则。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.common.DateRange` | 根据开始/截止日期构造学习计划筛选周期。 |
| `assistant.study.StudyPlanQuery` | 构造学习计划筛选条件。 |
| `assistant.study.StudyPlanStatus` | 解析可选学习计划状态筛选条件。 |

现有 `EntityId`、`OperationResult`、`StudyPlanView`、`LocalDate`、`DateTimeParseException`、`Locale`、`List`、`Objects` 等依赖继续复用。

**现有字段保持不变**：

| 字段签名 | 约束 |
|----------|------|
| `private final ApplicationServices services` | 构造时非空。 |
| `private final BufferedReader input` | 构造时非空；测试通过 `StringReader` 输入。 |
| `private final PrintWriter output` | 构造时非空；测试通过 `StringWriter` 输出。 |
| `private boolean running` | 主菜单、任务子菜单、日程子菜单与学习计划子菜单共用的程序运行标记。 |

**公开接口保持不变**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ConsoleApplication(ApplicationServices services, Reader input, Writer output)` | 构造器 | 维持既有空依赖防御和 `BufferedReader` / `PrintWriter` 包装行为。 |
| `public void run()` | `void` | 维持欢迎语、主菜单循环、EOF 正常退出和每轮输出刷新行为。 |

**修改的私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void dispatch(String rawCommand)` | `void` | 主菜单命令 `4` 改为调用 `runStudyPlanMenu()`；命令 `2`、`3` 和其他主菜单命令保持既有行为。 |
| `private void printHelp()` | `void` | 主菜单帮助保留学习计划入口说明；无需列出学习计划子菜单全部命令。 |
| `private void showStudyPlans()` | `void` | 删除或改为仅由新 `listStudyPlans()` 覆盖；主菜单不得再直接调用一次性列表入口。 |

**新增学习计划子菜单私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void runStudyPlanMenu()` | `void` | 进入时先调用 `printStudyPlanMenu()`；随后在 `running == true` 且未返回主菜单时循环读取学习计划命令。EOF 或读取失败时设置 `running = false` 并结束程序；命令 `b` / `back` 结束子菜单并返回主菜单；每次命令处理后刷新输出。 |
| `private void printStudyPlanMenu()` | `void` | 输出学习计划子菜单命令说明，至少包含 `l/list`、`a/add`、`v/view`、`f/filter`、`u/update`、`p/progress`、`d/delete`、`b/back`、`h/help`。 |
| `private boolean dispatchStudyPlanCommand(String rawCommand)` | `boolean` | 返回 `true` 表示继续留在学习计划子菜单，返回 `false` 表示返回主菜单或程序已结束。空命令输出 `请输入学习计划命令。` 并返回 `true`；未知命令输出 `未知学习计划命令，请输入 h 查看帮助。` 后展示学习计划帮助并返回 `true`。 |
| `private void listStudyPlans()` | `void` | 调用 `services.studyPlanService().listStudyPlans()`；成功时复用 `printStudyPlanList("学习计划列表", plans)`。 |
| `private void addStudyPlan()` | `void` | 依次读取目标名称、开始日期、截止日期、预期投入小时数、初始进度。目标名称作为原始字段传给服务；日期为必填 `yyyy-MM-dd`；小时数为正整数；初始进度空行时调用 `StudyPlanService.createStudyPlan(goalName, startDate, endDate, expectedHours)`，非空合法时调用 `StudyPlanService.createStudyPlan(goalName, startDate, endDate, expectedHours, initialProgress)`；结果通过 `printStudyPlanResult(result)` 展示。 |
| `private void viewStudyPlan()` | `void` | 读取学习计划 id；`INVALID` 时不调用服务并留在学习计划子菜单；`EOF` 时结束程序；`VALUE` 时调用 `StudyPlanService.getStudyPlan(id)` 并通过 `printStudyPlanResult(result)` 展示。 |
| `private void filterStudyPlans()` | `void` | 依次读取可选状态、可选开始日期、可选截止日期。状态为空表示不筛选状态；开始日期与截止日期必须同时为空或同时合法填写；只填一个日期输出指定验证错误并不调用服务；两日期合法后构造 `DateRange`，若结束日期早于开始日期则输出指定验证错误并不调用服务；最终调用 `StudyPlanService.listStudyPlans(StudyPlanQuery.of(statusOrNull, periodOrNull))`，成功时复用 `printStudyPlanList("学习计划筛选结果", plans)`。 |
| `private void updateStudyPlan()` | `void` | 依次读取学习计划 id、目标名称、开始日期、截止日期、预期投入小时数。id、日期或小时数字段返回 `INVALID` 时不调用服务并留在学习计划子菜单；任一读取返回 EOF 或原始字段 `null` 时结束程序；全部合法后调用 `StudyPlanService.updateStudyPlanDetails(id, goalName, startDate, endDate, expectedHours)` 并通过 `printStudyPlanResult(result)` 展示。 |
| `private void updateStudyPlanProgress()` | `void` | 依次读取学习计划 id、进度。id 或进度返回 `INVALID` 时不调用服务并留在学习计划子菜单；`EOF` 时结束程序；全部合法后调用 `StudyPlanService.updateStudyPlanProgress(id, progress)` 并通过 `printStudyPlanResult(result)` 展示。 |
| `private void deleteStudyPlan()` | `void` | 读取学习计划 id；`INVALID` 时不调用服务并留在学习计划子菜单；`EOF` 时结束程序；`VALUE` 时调用 `StudyPlanService.deleteStudyPlan(id)` 并通过既有 `printResult(result)` 展示；成功空载荷输出 `操作成功`。 |
| `private void printStudyPlanResult(OperationResult<StudyPlanView> result)` | `void` | 先调用 `printResult(result)`；失败时只输出错误码和消息；成功时调用 `printStudyPlanDetail(result.getPayload())`。 |
| `private void printStudyPlanList(String heading, List<StudyPlanView> plans)` | `void` | 输出标题；空列表输出 `暂无学习计划`；非空逐行输出全部学习计划，不限制 10 条。逐行格式为 `id | goalName | status | 进度 {progress}% | {startDate} ~ {endDate} | 预期 {expectedHours} 小时`。 |
| `private void printStudyPlanDetail(StudyPlanView plan)` | `void` | 输出单条学习计划详情，至少包含 `学习计划详情`、`ID: {id}`、`目标: {goalName}`、`状态: {status}`、`进度: {progress}%`、`开始日期: {startDate}`、`截止日期: {endDate}`、`预期投入小时数: {expectedHours}`。 |
| `private String readStudyPlanRawField(String prompt)` | `String` 或 `null` | 调用 `readLine(prompt)`；读取到 EOF 时显式设置 `running = false` 并返回 `null`；读取到真实空行时返回空字符串。用于目标名称等不由控制台层做业务校验的原始字段。 |
| `private ParsedInput<EntityId> readStudyPlanId(String prompt)` | `ParsedInput<EntityId>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字、小数或超出 `long` 范围时输出 `失败: VALIDATION_ERROR - 学习计划 id 必须是正整数` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(new EntityId(value))`。 |
| `private ParsedInput<LocalDate> readRequiredStudyPlanDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空值或解析失败输出 `失败: VALIDATION_ERROR - 学习计划日期格式必须是 yyyy-MM-dd` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(date)`。 |
| `private ParsedInput<LocalDate> readOptionalStudyPlanDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()` 作为无周期筛选字段；非空输入使用 `LocalDate.parse(raw.strip())` 解析；解析失败输出 `失败: VALIDATION_ERROR - 学习计划日期格式必须是 yyyy-MM-dd` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<Integer> readRequiredStudyPlanExpectedHours(String prompt)` | `ParsedInput<Integer>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字、小数或超出 `int` 范围时输出 `失败: VALIDATION_ERROR - 预期投入小时数必须是正整数` 并返回 `ParsedInput.invalid()`；成功返回正整数。 |
| `private ParsedInput<Integer> readRequiredStudyPlanProgress(String prompt)` | `ParsedInput<Integer>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非整数、空值、小数或不在 `0..100` 范围时输出 `失败: VALIDATION_ERROR - 进度必须是 0 到 100 的整数` 并返回 `ParsedInput.invalid()`；成功返回进度整数。 |
| `private ParsedInput<Integer> readOptionalStudyPlanInitialProgress(String prompt)` | `ParsedInput<Integer>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()`，表示创建时使用默认 0；非空输入复用进度解析规则，非法时输出 `进度必须是 0 到 100 的整数` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<StudyPlanStatus> readOptionalStudyPlanStatus(String prompt)` | `ParsedInput<StudyPlanStatus>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()`；非空输入大小写不敏感匹配 `NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`OVERDUE_INCOMPLETE`；非法值输出 `失败: VALIDATION_ERROR - 状态必须是 NOT_STARTED、IN_PROGRESS、COMPLETED 或 OVERDUE_INCOMPLETE` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<DateRange> readOptionalStudyPlanPeriod(String startPrompt, String endPrompt)` | `ParsedInput<DateRange>` | 先后读取可选开始日期和可选截止日期。任一读取为 `EOF` 或 `INVALID` 时原样返回对应状态；两者均 `EMPTY` 返回 `ParsedInput.empty()`；仅一者为空时输出 `失败: VALIDATION_ERROR - 学习计划开始日期和截止日期必须同时填写或同时为空` 并返回 `ParsedInput.invalid()`；两者均合法后构造 `new DateRange(start, end)`；若构造抛出 `IllegalArgumentException`，输出 `失败: VALIDATION_ERROR - 学习计划结束日期不能早于开始日期` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<DateRange> readRequiredStudyPlanPeriod(String startPrompt, String endPrompt)` | `ParsedInput<DateRange>` | 先后读取必填开始日期和截止日期。任一读取未得到 `VALUE` 时返回 `EOF` 或 `INVALID`；两个日期都合法后构造 `new DateRange(start, end)`；若构造抛出 `IllegalArgumentException`，输出 `失败: VALIDATION_ERROR - 学习计划结束日期不能早于开始日期` 并返回 `ParsedInput.invalid()`。新增与修改可用该方法统一拒绝日期倒置。 |
| `private EntityId parseStudyPlanId(String rawValue)` | `EntityId` 或 `null` | 只做语法解析与正数约束；成功返回 `new EntityId(value)`；失败输出学习计划 id 验证错误并返回 `null`。 |
| `private LocalDate parseStudyPlanDate(String rawValue)` | `LocalDate` 或 `null` | 使用 `LocalDate.parse(rawValue.strip())` 解析 ISO 日期；成功返回日期；失败输出学习计划日期格式验证错误并返回 `null`。 |
| `private Integer parsePositiveInt(String rawValue, String validationMessage)` | `Integer` 或 `null` | 使用 `Integer.parseInt(rawValue.strip())` 解析；值必须 `> 0`；失败输出传入验证消息并返回 `null`。仅用于预期投入小时数。 |
| `private Integer parseStudyPlanProgress(String rawValue)` | `Integer` 或 `null` | 使用 `Integer.parseInt(rawValue.strip())` 解析；值必须在 `0..100`；失败输出进度验证错误并返回 `null`。 |
| `private StudyPlanStatus parseStudyPlanStatus(String rawValue)` | `StudyPlanStatus` 或 `null` | 输入大小写不敏感、前后空白忽略；成功返回枚举；失败输出状态验证错误并返回 `null`。 |

**复用的私有接口**：

| 方法签名 | 返回类型 | 复用契约 |
|----------|----------|----------|
| `private void printValidationError(String message)` | `void` | 继续输出固定格式 `失败: VALIDATION_ERROR - {message}`。任务、日程与学习计划解析错误均复用该输出入口。 |
| `private <T> boolean printResult(OperationResult<T> result)` | `boolean` | 服务失败输出 `失败: {ErrorCode} - {message}`；成功且空载荷输出 `操作成功`。 |
| `private String readLine(String prompt)` | `String` 或 `null` | EOF 返回 `null` 且自身不修改 `running`；`IOException` 输出 `输入读取失败，程序退出。`、设置 `running = false` 并返回 `null`。 |
| `private record ParsedInput<T>(State state, T value)` | 私有嵌套类型 | 继续表达 `VALUE`、`EMPTY`、`INVALID`、`EOF` 四种字段解析状态；学习计划读取方法必须复用该类型，不新增并行状态类型。 |

**命令分发契约**：

| 命令 | 私有方法 | 服务调用 |
|------|----------|----------|
| `l` / `list` | `listStudyPlans()` | `StudyPlanService.listStudyPlans()` |
| `a` / `add` | `addStudyPlan()` | `StudyPlanService.createStudyPlan(String, LocalDate, LocalDate, int)` 或 `StudyPlanService.createStudyPlan(String, LocalDate, LocalDate, int, int)` |
| `v` / `view` | `viewStudyPlan()` | `StudyPlanService.getStudyPlan(EntityId)` |
| `f` / `filter` | `filterStudyPlans()` | `StudyPlanService.listStudyPlans(StudyPlanQuery.of(status, period))` |
| `u` / `update` | `updateStudyPlan()` | `StudyPlanService.updateStudyPlanDetails(EntityId, String, LocalDate, LocalDate, int)` |
| `p` / `progress` | `updateStudyPlanProgress()` | `StudyPlanService.updateStudyPlanProgress(EntityId, int)` |
| `d` / `delete` | `deleteStudyPlan()` | `StudyPlanService.deleteStudyPlan(EntityId)` |
| `b` / `back` | 子菜单返回 | 不调用服务 |
| `h` / `help` | `printStudyPlanMenu()` | 不调用服务 |

**字段读取顺序**：

| 操作 | 字段顺序 |
|------|----------|
| 新增学习计划 | 目标名称、开始日期、截止日期、预期投入小时数、初始进度 |
| 查看学习计划 | 学习计划 id |
| 筛选学习计划 | 状态、开始日期、截止日期 |
| 修改学习计划 | 学习计划 id、目标名称、开始日期、截止日期、预期投入小时数 |
| 更新进度 | 学习计划 id、进度 |
| 删除学习计划 | 学习计划 id |

**构造方式**：

- 仍由 `Main` 和测试通过 `new ConsoleApplication(services, input, output)` 构造。

**类型关系**：

- `ConsoleApplication` 依赖既有 `ApplicationServices.studyPlanService()` 获取 `StudyPlanService`。
- `ConsoleApplication` 依赖 `DateRange` 构造学习计划周期，并捕获可预期的日期倒置错误。
- `ConsoleApplication` 依赖 `StudyPlanQuery.of(StudyPlanStatus, DateRange)` 表达状态与周期组合筛选。
- `ConsoleApplication` 不依赖 `StudyPlanRepository`、`StudyPlan`、`StudyPlanAnalysisService` 或任何仓储集合。

### `ConsoleApplication.ParsedInput<T>`

**形态**：`private record`

**包路径**：`assistant.app`，作为 `ConsoleApplication` 私有嵌套类型

**职责**：继续表达控制台字段解析结果，任务子菜单、日程子菜单与学习计划子菜单共同复用，避免用 `null` 同时代表合法空筛选、非法输入和 EOF。

**状态定义保持不变**：

| 状态 | 含义 | 学习计划调用方行为 |
|------|------|----------------|
| `VALUE` | 字段存在且解析成功，`value()` 非空。 | 继续当前学习计划操作并使用值。 |
| `EMPTY` | 仅用于筛选字段和创建时初始进度空输入。 | 筛选时把对应 `StudyPlanQuery` 参数设为 `null`；创建时调用默认进度重载。 |
| `INVALID` | 用户输入不合法，且已输出 `VALIDATION_ERROR`。 | 立即中止当前学习计划操作，不调用服务，保持在学习计划子菜单。 |
| `EOF` | 读取到 EOF，且字段读取方法已设置 `running = false`。 | 立即中止当前学习计划操作和学习计划子菜单，程序正常结束。 |

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

**职责**：通过完整控制台输入输出验证学习计划子菜单，不读取真实环境变量、不访问真实网络、不依赖真实系统时间。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.study.StudyPlanService` | Mockito mock 学习计划服务失败和服务未调用路径。 |
| `assistant.study.StudyPlanView` | 构造 mock 返回值时需要的载荷类型。 |
| `assistant.study.StudyPlanStatus` | 构造 mock 返回值或断言状态时按需使用。 |
| `assistant.common.DateRange` | 构造 mock `StudyPlanView` 或验证筛选查询时按需使用。 |
| `assistant.common.EntityId` | 构造 mock `StudyPlanView` 时按需使用。 |
| `assistant.common.Progress` | 构造 mock `StudyPlanView` 时按需使用。 |
| `java.time.LocalDate` | 构造学习计划日期或 mock 数据时按需使用。 |

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
| `listCommandsDisplayEachCoreEntry()` | 输入 `2\nb\n3\nb\n4\nb\n5\n6\n8\nq\n`；断言任务入口展示任务子菜单，日程入口展示日程子菜单，学习计划入口展示学习计划子菜单，其他主菜单入口仍可用。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 输入 `2\nl\nb\n3\nl\nb\n4\nl\nb\n5\n6\n8\nq\n`；断言任务、日程、学习计划等空状态均可通过对应入口展示。 |
| `studyPlanCommandDisplaysFailureCodeAndMessage()` | 使用 mock `StudyPlanService.listStudyPlans()` 返回失败；输入 `4\nl\nb\nq\n`；断言输出服务失败错误码和消息。 |
| `studyPlanMenuAddsPlanAndSummaryReflectsWeekStudyPlanCount()` | 空数据服务；输入进入学习计划菜单新增本周学习计划、列表、查看、返回主菜单、汇总；断言新增计划在列表和详情中可见，且汇总输出 `本周学习计划数: 1`。 |
| `studyPlanMenuViewsUpdatesProgressAndDeletesPlan()` | 新增学习计划后查看、修改详情、更新进度、删除，再查看；断言查看含旧目标，修改后详情含新目标/日期/小时数，更新进度后详情含新进度，删除成功输出 `操作成功`，删除后查看输出 `NOT_FOUND`。 |
| `studyPlanMenuFiltersByStatusAndPeriod()` | 固定当前日期 `2026-01-15`；新增一个匹配状态和周期的计划，以及一个非匹配状态或非重叠周期计划；执行状态+周期筛选；使用 `between(...)` 截取 `学习计划筛选结果` 到 `主菜单`，断言匹配项出现且非匹配项不出现。 |
| `studyPlanMenuFilterEmptyFieldsListAllPlans()` | 新增至少两个学习计划；执行筛选时状态、开始日期和截止日期均输入空行；断言筛选结果包含全部学习计划。 |
| `studyPlanMenuCreatesDefaultInitialProgressWhenBlank()` | 新增学习计划时初始进度输入空行；查看或列表断言进度为 `0%`，并确保创建成功。 |
| `studyPlanMenuCreatesCompletedPlanWithExplicitProgress100()` | 新增学习计划时初始进度输入 `100`；查看或列表断言进度为 `100%`，状态为 `COMPLETED`。 |
| `studyPlanMenuAcceptsLongCommandAliasesAndCaseInsensitiveStatus()` | 输入 `add/view/update/progress/filter/list/delete/back/quit`；筛选状态输入小写或混合大小写如 `completed`；断言各长命令生效、筛选结果包含匹配计划、删除成功。 |
| `studyPlanMenuRejectsInvalidIdWithoutWriteOperation()` | 输入非法 id 执行查看、修改、进度更新或删除之一；断言输出 `VALIDATION_ERROR` 和 `学习计划 id 必须是正整数`，随后列表仍保持已有学习计划状态不变。 |
| `studyPlanMenuRejectsInvalidDateWithoutWriteOperation()` | 新增或修改时输入非法开始/截止日期；断言输出 `VALIDATION_ERROR` 和 `学习计划日期格式必须是 yyyy-MM-dd`，随后列表不包含本次待新增目标或原计划未被修改。 |
| `studyPlanMenuRejectsFilterSingleDateWithoutServiceCall()` | 使用 mock `StudyPlanService`；筛选时仅填写开始日期或仅填写截止日期；断言输出 `学习计划开始日期和截止日期必须同时填写或同时为空`，并验证不调用 `studyPlanService`。 |
| `studyPlanMenuRejectsFilterEndBeforeStartWithoutServiceCall()` | 使用 mock `StudyPlanService`；筛选时结束日期早于开始日期；断言输出 `学习计划结束日期不能早于开始日期`，并验证不调用 `studyPlanService`。 |
| `studyPlanMenuRejectsInvalidHoursWithoutWriteOperation()` | 新增或修改时输入 `0`、负数或非整数小时数；断言输出 `预期投入小时数必须是正整数`，随后列表不包含本次待新增目标或原计划未被修改。 |
| `studyPlanMenuRejectsInvalidProgressWithoutWriteOperation()` | 新增初始进度或更新进度时输入非整数、小于 `0` 或大于 `100`；断言输出 `进度必须是 0 到 100 的整数`，随后列表或详情确认数据未被写入或未被更新。 |
| `studyPlanMenuRejectsInvalidStatusBeforeCallingStudyPlanService()` | 使用 mock `StudyPlanService`；筛选时输入非法状态；断言输出 `状态必须是 NOT_STARTED、IN_PROGRESS、COMPLETED 或 OVERDUE_INCOMPLETE`，并验证不调用 `studyPlanService`。 |
| `studyPlanMenuUnknownHelpBackAndMainMenuContinuation()` | 输入未知学习计划命令、帮助、返回主菜单、汇总、退出；断言未知命令提示、学习计划帮助、主菜单汇总均出现。 |
| `studyPlanMenuBlankCommandPromptsAgainAndStaysInStudyPlanMenu()` | 输入空学习计划命令后执行列表；断言输出 `请输入学习计划命令。` 且随后仍展示 `学习计划列表`。 |
| `studyPlanMenuExitsOnEofDuringCommandRead()` | 输入只包含 `4\n`；断言进入学习计划子菜单后 EOF 正常结束，不抛异常。 |
| `studyPlanMenuExitsOnEofDuringAddFields()` | 输入 `4\na\n半成品学习计划\n` 等中途 EOF；断言程序正常结束，不抛异常，未输出 `学习计划详情`，未创建半成品学习计划。 |

**测试数据约束**：

- 固定当前时间由 `servicesWithoutDemoData()` 提供：`2026-01-15T09:00`，因此当前日期为 `2026-01-15`。
- 学习计划新增输入字段顺序必须与实现一致：目标名称、开始日期、截止日期、预期投入小时数、初始进度。
- 学习计划修改输入字段顺序必须与实现一致：学习计划 id、目标名称、开始日期、截止日期、预期投入小时数。
- 使用 ISO 日期输入，例如 `2026-01-13`、`2026-01-15`、`2026-01-18`、`2026-02-01`。
- 状态推导示例：当前日期前开始且未完成可为 `IN_PROGRESS`；当前日期后开始可为 `NOT_STARTED`；进度 `100` 可为 `COMPLETED`；截止日期早于当前日期且未完成可为 `OVERDUE_INCOMPLETE`。
- 筛选周期使用 `DateRange` 重叠语义；匹配样例应与筛选周期有日期重叠，非匹配样例应完全不重叠或状态不匹配。
- 所有测试通过 `ApplicationFactory.create(Map.of(), new FixedTimeProvider(...))` 或局部 Mockito mock 装配，不设置真实 API Key，不触发真实 AI 请求。

## 错误处理

- 服务层失败继续统一使用 `printResult(OperationResult<T>)` 输出：`失败: {ErrorCode} - {message}`。
- 控制台解析失败不构造业务服务调用，直接输出 `失败: VALIDATION_ERROR - {清晰提示}`，随后留在学习计划子菜单。
- 学习计划 id 非正整数、空值、非数字、小数或超出 `long` 范围统一输出 `学习计划 id 必须是正整数`。
- 学习计划日期为空或格式错误统一输出 `学习计划日期格式必须是 yyyy-MM-dd`。创建、修改、筛选日期均使用同一日期格式提示。
- 预期投入小时数为空、非整数、小数、超出 `int` 范围或 `<= 0` 统一输出 `预期投入小时数必须是正整数`。
- 进度为空、非整数、小数、超出 `int` 范围、小于 `0` 或大于 `100` 统一输出 `进度必须是 0 到 100 的整数`。
- 筛选状态为空属于合法空筛选；非空非法输出 `状态必须是 NOT_STARTED、IN_PROGRESS、COMPLETED 或 OVERDUE_INCOMPLETE`。
- 筛选开始日期和截止日期均为空属于合法空周期筛选；仅填写一个日期输出 `学习计划开始日期和截止日期必须同时填写或同时为空`。
- `DateRange` 构造时若开始日期晚于结束日期，控制台层捕获 `IllegalArgumentException` 并统一输出 `学习计划结束日期不能早于开始日期`，不让运行时异常泄漏到 `run()` 调用方。新增、修改、筛选均适用该提示。
- 目标名称作为原始字段传给服务，不在控制台层做业务校验，不做 `strip()` 后传递；字段为空、空白或其他业务错误由 `StudyPlanService` 返回 `OperationResult` 并由控制台展示。
- `readLine(...)` 读取到 EOF 返回 `null` 且不自行设置 `running = false`；主菜单 EOF 继续由 `dispatch(null)` 处理。
- 学习计划子菜单命令读取到 EOF 时，`runStudyPlanMenu()` 显式设置 `running = false` 并结束程序。
- 学习计划子菜单字段读取中，`readStudyPlanRawField(...)` 收到 EOF 时设置 `running = false` 并返回 `null`；id、日期、小时数、进度和状态读取方法收到 EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`。调用方收到原始字段 `null` 或 `ParsedInput.eof()` 后不再继续解析或调用服务。
- 可选筛选字段的合法空输入必须返回 `ParsedInput.empty()`；非法输入必须返回 `ParsedInput.invalid()`；二者不得都用 `null` 表达。`EMPTY` 继续构造 `StudyPlanQuery`，对应参数为 `null`；`INVALID` 不调用服务并留在学习计划子菜单。
- 创建时初始进度空输入必须返回 `ParsedInput.empty()`，调用默认 0 的 `createStudyPlan` 重载；非空非法进度返回 `ParsedInput.invalid()`，不得调用服务。
- `IOException` 仍由既有 `readLine(...)` 处理，输出 `输入读取失败，程序退出。` 并设置 `running = false`。
- 删除成功使用 `OperationResult<Void>` 的既有空载荷成功展示，输出 `操作成功`。

## 行为契约

- 主菜单命令 `4` 进入学习计划子菜单；学习计划子菜单命令 `b` / `back` 返回主菜单，返回后主菜单仍可执行汇总、任务、日程、AI 问答、退出等既有命令。
- 进入学习计划子菜单时必须先展示学习计划子菜单命令说明。
- 学习计划子菜单中 `h` / `help` 展示同一学习计划子菜单命令说明。
- 学习计划子菜单中空命令输出 `请输入学习计划命令。` 并继续留在学习计划子菜单。
- 学习计划子菜单中未知命令输出 `未知学习计划命令，请输入 h 查看帮助。`，随后展示帮助并继续留在学习计划子菜单。
- 列表与筛选空结果均输出 `暂无学习计划`。
- 新增、查看、修改和更新进度成功后输出单条学习计划详情，包含 id、目标、状态、进度、开始日期、截止日期和预期投入小时数。
- 列表与筛选非空结果必须逐行输出全部学习计划，不得限制为前 10 条。
- 学习计划列表标题固定为 `学习计划列表`；学习计划筛选标题固定为 `学习计划筛选结果`；详情标题固定为 `学习计划详情`。
- 学习计划列表行至少包含 `id | goalName | status | 进度 {progress}% | {startDate} ~ {endDate} | 预期 {expectedHours} 小时`。允许追加字段，但不得破坏该最小可断言格式。
- 学习计划状态在输出中使用枚举名，例如 `NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`OVERDUE_INCOMPLETE`，与现有任务、日程列表的状态输出风格一致。
- 学习计划详情进度输出使用 `plan.progress().value()`，格式为 `进度: {value}%`。
- 新增学习计划初始进度为空行时，控制台必须调用不带 `initialProgress` 的 `createStudyPlan` 重载，由服务默认 0；非空合法时调用带 `initialProgress` 的重载。
- 筛选学习计划时，状态、开始日期、截止日期均为空应调用 `StudyPlanService.listStudyPlans(StudyPlanQuery.of(null, null))`，并展示全部学习计划。
- 筛选学习计划时，状态合法且周期为空应传 `StudyPlanQuery.of(status, null)`；状态为空且周期合法应传 `StudyPlanQuery.of(null, period)`；两者均合法应传 `StudyPlanQuery.of(status, period)`。
- 任一控制台验证失败都不得调用对应学习计划服务方法；操作中止后仍保持在学习计划子菜单，下一条命令可继续执行。
- EOF 出现在学习计划命令读取或任一字段读取期间时，程序正常结束，不输出异常堆栈，不创建或修改半成品数据。

## 依赖关系

- `ConsoleApplication` 新增依赖 `StudyPlanQuery` 与 `StudyPlanStatus`，继续依赖既有 `StudyPlanView`。
- `ConsoleApplication` 复用 `DateRange`、`EntityId`、`OperationResult`、`ParsedInput`、`printResult(...)`、`printValidationError(...)`、`readLine(...)`。
- `ConsoleApplicationTest` 复用现有 `servicesWithDemoData()`、`servicesWithoutDemoData()`、`runWithInput(...)`、`between(...)` 和断言辅助方法。
- 后续编码步骤不得改变学习计划服务、查询、视图和状态枚举的公开 API；若需要辅助解析，应限制在 `ConsoleApplication` 私有方法或 `assistant.app` 包内小型包私有工具，优先采用 `ConsoleApplication` 私有方法以贴合现有任务和日程实现风格。
