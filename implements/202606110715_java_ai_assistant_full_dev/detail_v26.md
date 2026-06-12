# 详细设计（v26）

## 概述

本轮设计目标是扩展 `assistant.app.ConsoleApplication` 的收支入口：主菜单命令 `5` 不再执行一次性 `showTransactions()`，而是进入可循环收支子菜单。收支子菜单通过既有 `FinanceService` 完成全部记录列表、收入记录、支出记录、单条查看、组合筛选、修改、删除、统计、帮助、返回主菜单和 EOF 稳定退出。

本轮实现范围：

- 修改 `ConsoleApplication`：主菜单收支命令接入收支子菜单；新增收支命令分发、字段读取、输入解析校验、`DateRange` 与 `TransactionQuery` 构造、列表、详情和统计输出。
- 修改 `ConsoleApplicationTest`：更新主菜单收支入口断言，新增收支子菜单交互测试与服务失败、控制台校验失败测试。

本轮不修改：

- `FinanceService`、`TransactionQuery`、`TransactionView`、`TransactionType`、`FinanceStatistics` 的公开契约。
- 收支实体、仓储、金额值对象、统计服务、汇总服务、应用装配或其他业务模块。
- 独立 CLI 框架或第三方命令解析库。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 将主菜单命令 `5` 接入收支子菜单，新增收支命令处理、字段读取解析、`TransactionQuery` 构造、列表、详情和统计输出。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 更新收支入口断言，新增收支子菜单成功路径、边界校验、服务失败、帮助、返回和 EOF 测试。 |

## 类型定义

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：控制台主循环与各核心功能子菜单交互层。收支相关代码只负责读取输入、解析控制台字段、调用 `FinanceService` 和展示 `OperationResult`，不承载金额、类别、记录存在性或仓储访问等业务规则。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.finance.TransactionQuery` | 构造收支筛选条件。 |
| `assistant.finance.TransactionType` | 解析收入/支出类型。 |

既有 `DateRange`、`EntityId`、`FinanceStatistics`、`TransactionView`、`LocalDate`、`DateTimeParseException`、`Locale`、`List`、`Objects` 等依赖继续复用。

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
| `private void dispatch(String rawCommand)` | `void` | 主菜单命令 `5` 改为调用 `runFinanceMenu()`；其他主菜单命令保持既有行为。 |
| `private void printHelp()` | `void` | 主菜单帮助保留收支入口说明；无需列出收支子菜单全部命令。 |
| `private void showTransactions()` | `void` | 删除，或改为仅由 `listTransactions()` / `showFinanceStatistics()` 覆盖；主菜单不得再直接调用一次性列表入口。 |

**新增收支子菜单私有接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void runFinanceMenu()` | `void` | 进入时先调用 `printFinanceMenu()`；随后在 `running == true` 且未返回主菜单时循环读取收支命令。EOF 或读取失败时设置 `running = false` 并结束程序；命令 `b` / `back` 结束子菜单并返回主菜单；每次命令处理后刷新输出。 |
| `private void printFinanceMenu()` | `void` | 输出收支子菜单命令说明，至少包含 `l/list`、`i/income`、`e/expense`、`v/view`、`f/filter`、`u/update`、`d/delete`、`s/statistics`、`b/back`、`h/help`。 |
| `private boolean dispatchFinanceCommand(String rawCommand)` | `boolean` | 返回 `true` 表示继续留在收支子菜单，返回 `false` 表示返回主菜单或程序已结束。空命令输出 `请输入收支命令。` 并返回 `true`；未知命令输出 `未知收支命令，请输入 h 查看帮助。` 后展示收支帮助并返回 `true`。 |
| `private void listTransactions()` | `void` | 调用 `FinanceService.listTransactions()`；成功后调用 `FinanceService.calculateStatistics()`；任一服务失败通过 `printResult(...)` 输出并中止展示；均成功时输出全量统计和 `printTransactionList("收支记录列表", transactions)`。列表不得限制为前 10 条。 |
| `private void recordIncome()` | `void` | 依次读取金额、类别、日期、备注；金额、类别、备注作为原始字符串传给服务；日期为必填 `yyyy-MM-dd`；日期读取非法不调用服务；成功或失败结果通过 `printTransactionResult(result)` 展示。 |
| `private void recordExpense()` | `void` | 字段读取与 `recordIncome()` 相同，调用 `FinanceService.recordExpense(amountText, category, date, note)`。 |
| `private void viewTransaction()` | `void` | 读取收支记录 id；`INVALID` 时不调用服务并留在收支子菜单；`EOF` 时结束程序；`VALUE` 时调用 `FinanceService.getTransaction(id)` 并通过 `printTransactionResult(result)` 展示。 |
| `private void filterTransactions()` | `void` | 依次读取可选类型、可选类别、可选开始日期、可选结束日期。类型为空表示不筛选类型；类别为空表示不筛选类别；日期范围必须同时为空或同时合法填写；只填一个日期输出指定验证错误并不调用服务；两日期合法后构造 `DateRange`，若结束日期早于开始日期则输出指定验证错误并不调用服务；最终调用 `FinanceService.listTransactions(TransactionQuery.of(type, category, dateRange))` 和 `FinanceService.calculateStatistics(query)`，成功时输出筛选统计和 `printTransactionList("收支筛选结果", transactions)`。 |
| `private void updateTransaction()` | `void` | 依次读取收支记录 id、类型、金额、类别、日期、备注。id、类型或日期返回 `INVALID` 时不调用服务并留在收支子菜单；任一读取返回 EOF 或原始字段 `null` 时结束程序；全部合法后调用 `FinanceService.updateTransaction(id, type, amountText, category, date, note)` 并通过 `printTransactionResult(result)` 展示。控制台层不预先校验金额正负、零值、小数位或类别空白，这些业务错误由服务结果统一展示。 |
| `private void deleteTransaction()` | `void` | 读取收支记录 id；`INVALID` 时不调用服务；`EOF` 时结束程序；`VALUE` 时调用 `FinanceService.deleteTransaction(id)` 并通过既有 `printResult(result)` 展示；成功空载荷输出 `操作成功`。 |
| `private void showFinanceStatistics()` | `void` | 询问是否筛选统计：读取可选类型、类别、开始日期、结束日期，字段规则与 `filterTransactions()` 相同。若四个筛选字段均为空，调用 `FinanceService.calculateStatistics()`；否则构造 `TransactionQuery` 并调用 `FinanceService.calculateStatistics(query)`。成功时调用 `printFinanceStatistics("收支统计", statistics)`。 |
| `private void printTransactionResult(OperationResult<TransactionView> result)` | `void` | 先调用 `printResult(result)`；失败时只输出错误码和消息；成功时调用 `printTransactionDetail(result.getPayload())`。 |
| `private void printTransactionList(String heading, List<TransactionView> transactions)` | `void` | 输出标题；空列表输出 `暂无收支记录`；非空逐行输出全部记录，不限制 10 条。逐行格式为 `id | type | amount | category | date | note`，金额使用 `transaction.amount().value().toPlainString()`。 |
| `private void printTransactionDetail(TransactionView transaction)` | `void` | 输出单条收支记录详情，至少包含 `收支记录详情`、`ID: {id}`、`类型: {type}`、`金额: {amount}`、`类别: {category}`、`日期: {date}`、`备注: {note}`。 |
| `private void printFinanceStatistics(String heading, FinanceStatistics statistics)` | `void` | 输出标题；随后输出 `收入: {totalIncome}`、`支出: {totalExpense}`、`结余: {balance}`，金额均使用 `toPlainString()`。 |
| `private String readFinanceRawField(String prompt)` | `String` 或 `null` | 调用 `readLine(prompt)`；读取到 EOF 时显式设置 `running = false` 并返回 `null`；读取到真实空行时返回空字符串。用于金额、类别和备注等由服务层做业务校验或允许空备注的原始字段。 |
| `private ParsedInput<EntityId> readFinanceId(String prompt)` | `ParsedInput<EntityId>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；非正整数、空值、非数字、小数或超出 `long` 范围时输出 `失败: VALIDATION_ERROR - 收支记录 id 必须是正整数` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(new EntityId(value))`。 |
| `private ParsedInput<TransactionType> readRequiredTransactionType(String prompt)` | `ParsedInput<TransactionType>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空值或非法值输出 `失败: VALIDATION_ERROR - 收支类型必须是 INCOME 或 EXPENSE` 并返回 `ParsedInput.invalid()`；合法值大小写不敏感匹配 `INCOME` / `EXPENSE`。 |
| `private ParsedInput<TransactionType> readOptionalTransactionType(String prompt)` | `ParsedInput<TransactionType>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()`；非空输入复用类型解析规则，非法值输出 `收支类型必须是 INCOME 或 EXPENSE` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<LocalDate> readRequiredTransactionDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空值或解析失败输出 `失败: VALIDATION_ERROR - 收支日期格式必须是 yyyy-MM-dd` 并返回 `ParsedInput.invalid()`；成功返回 `ParsedInput.value(date)`。 |
| `private ParsedInput<LocalDate> readOptionalTransactionDate(String prompt)` | `ParsedInput<LocalDate>` | 调用 `readLine(prompt)`；EOF 时设置 `running = false` 并返回 `ParsedInput.eof()`；空输入返回 `ParsedInput.empty()`；非空输入使用 `LocalDate.parse(raw.strip())` 解析；解析失败输出 `失败: VALIDATION_ERROR - 收支日期格式必须是 yyyy-MM-dd` 并返回 `ParsedInput.invalid()`。 |
| `private ParsedInput<DateRange> readOptionalTransactionDateRange(String startPrompt, String endPrompt)` | `ParsedInput<DateRange>` | 先后读取可选开始日期和可选结束日期。任一读取为 `EOF` 或 `INVALID` 时原样返回对应状态；两者均 `EMPTY` 返回 `ParsedInput.empty()`；仅一者为空时输出 `失败: VALIDATION_ERROR - 收支开始日期和结束日期必须同时填写或同时为空` 并返回 `ParsedInput.invalid()`；两者均合法后构造 `new DateRange(start, end)`；若构造抛出 `IllegalArgumentException`，输出 `失败: VALIDATION_ERROR - 收支结束日期不能早于开始日期` 并返回 `ParsedInput.invalid()`。 |
| `private TransactionQuery buildTransactionQuery(TransactionType type, String category, DateRange dateRange)` | `TransactionQuery` | 传入的 `category` 若为 `null` 或 `isBlank()` 则作为 `null` 筛选；非空类别原值传给 `TransactionQuery.of(...)`，由其执行 `strip()` 与空白防御。 |
| `private ParsedInput<TransactionQuery> readOptionalTransactionQuery()` | `ParsedInput<TransactionQuery>` | 按顺序读取类型、类别、开始日期、结束日期，复用筛选和统计命令的输入规则；成功时返回 `TransactionQuery.of(...)`。 |
| `private EntityId parseFinanceId(String rawValue)` | `EntityId` 或 `null` | 只做语法解析与正数约束；成功返回 `new EntityId(value)`；失败输出收支记录 id 验证错误并返回 `null`。 |
| `private TransactionType parseTransactionType(String rawValue)` | `TransactionType` 或 `null` | 输入大小写不敏感、前后空白忽略；成功返回枚举；失败输出类型验证错误并返回 `null`。 |
| `private LocalDate parseTransactionDate(String rawValue)` | `LocalDate` 或 `null` | 使用 `LocalDate.parse(rawValue.strip())` 解析 ISO 日期；成功返回日期；失败输出收支日期格式验证错误并返回 `null`。 |

**复用的私有接口**：

| 方法签名 | 返回类型 | 复用契约 |
|----------|----------|----------|
| `private void printValidationError(String message)` | `void` | 继续输出固定格式 `失败: VALIDATION_ERROR - {message}`。收支解析错误复用该输出入口。 |
| `private <T> boolean printResult(OperationResult<T> result)` | `boolean` | 服务失败输出 `失败: {ErrorCode} - {message}`；成功且空载荷输出 `操作成功`。 |
| `private String readLine(String prompt)` | `String` 或 `null` | EOF 返回 `null` 且自身不修改 `running`；`IOException` 输出 `输入读取失败，程序退出。`、设置 `running = false` 并返回 `null`。 |
| `private record ParsedInput<T>(State state, T value)` | 私有嵌套类型 | 继续表达 `VALUE`、`EMPTY`、`INVALID`、`EOF` 四种字段解析状态；收支读取方法必须复用该类型，不新增并行状态类型。 |

**命令分发契约**：

| 命令 | 私有方法 | 服务调用 |
|------|----------|----------|
| `l` / `list` | `listTransactions()` | `FinanceService.listTransactions()` + `FinanceService.calculateStatistics()` |
| `i` / `income` | `recordIncome()` | `FinanceService.recordIncome(String, String, LocalDate, String)` |
| `e` / `expense` | `recordExpense()` | `FinanceService.recordExpense(String, String, LocalDate, String)` |
| `v` / `view` | `viewTransaction()` | `FinanceService.getTransaction(EntityId)` |
| `f` / `filter` | `filterTransactions()` | `FinanceService.listTransactions(TransactionQuery)` + `FinanceService.calculateStatistics(TransactionQuery)` |
| `u` / `update` | `updateTransaction()` | `FinanceService.updateTransaction(EntityId, TransactionType, String, String, LocalDate, String)` |
| `d` / `delete` | `deleteTransaction()` | `FinanceService.deleteTransaction(EntityId)` |
| `s` / `statistics` | `showFinanceStatistics()` | `FinanceService.calculateStatistics()` 或 `FinanceService.calculateStatistics(TransactionQuery)` |
| `b` / `back` | 子菜单返回 | 不调用服务 |
| `h` / `help` | `printFinanceMenu()` | 不调用服务 |

**字段读取顺序**：

| 操作 | 字段顺序 |
|------|----------|
| 记录收入 | 金额、类别、日期、备注 |
| 记录支出 | 金额、类别、日期、备注 |
| 查看记录 | 收支记录 id |
| 筛选记录 | 类型、类别、开始日期、结束日期 |
| 修改记录 | 收支记录 id、类型、金额、类别、日期、备注 |
| 删除记录 | 收支记录 id |
| 统计 | 类型、类别、开始日期、结束日期 |

**构造方式**：

- 仍由 `Main` 和测试通过 `new ConsoleApplication(services, input, output)` 构造。

**类型关系**：

- `ConsoleApplication` 依赖既有 `ApplicationServices.financeService()` 获取 `FinanceService`。
- `ConsoleApplication` 依赖 `DateRange` 构造收支日期范围，并捕获可预期的日期倒置错误。
- `ConsoleApplication` 依赖 `TransactionQuery.of(TransactionType, String, DateRange)` 表达类型、类别与日期范围组合筛选。
- `ConsoleApplication` 不依赖 `TransactionRepository`、`TransactionRecord`、`FinanceStatisticsService` 或任何仓储集合。

### `ConsoleApplication.ParsedInput<T>`

**形态**：`private record`

**包路径**：`assistant.app`，作为 `ConsoleApplication` 私有嵌套类型

**职责**：继续表达控制台字段解析结果，任务、日程、学习计划与收支子菜单共同复用，避免用 `null` 同时代表合法空筛选、非法输入和 EOF。

**状态定义保持不变**：

| 状态 | 含义 | 收支调用方行为 |
|------|------|----------------|
| `VALUE` | 字段存在且解析成功，`value()` 非空。 | 继续当前收支操作并使用值。 |
| `EMPTY` | 仅用于筛选与统计字段。 | 把对应 `TransactionQuery` 参数设为 `null`；若全部字段为空则统计命令调用全量统计。 |
| `INVALID` | 用户输入不合法，且已输出 `VALIDATION_ERROR`。 | 立即中止当前收支操作，不调用服务，保持在收支子菜单。 |
| `EOF` | 读取到 EOF，且字段读取方法已设置 `running = false`。 | 立即中止当前收支操作和收支子菜单，程序正常结束。 |

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

**职责**：通过完整控制台输入输出验证收支子菜单，不读取真实环境变量、不访问真实网络、不依赖真实系统时间。

**新增/调整导入**：

| 导入 | 用途 |
|------|------|
| `assistant.finance.TransactionQuery` | Mockito 验证筛选服务调用时按需使用。 |
| `assistant.finance.TransactionType` | 构造或断言筛选查询时按需使用。 |
| `assistant.common.DateRange` | 构造或验证日期范围筛选时按需使用。 |

既有 `FinanceService` mock 导入继续复用。

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
| `listCommandsDisplayEachCoreEntry()` | 输入改为 `2\nb\n3\nb\n4\nb\n5\nb\n6\n8\nq\n`；断言任务、日程、学习计划和收支均进入对应子菜单，其他主菜单入口仍可用。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 输入改为 `2\nl\nb\n3\nl\nb\n4\nl\nb\n5\nl\nb\n6\n8\nq\n`；断言收支空状态通过收支子菜单列表展示。 |
| `transactionCommandDisplaysStatisticsFailureCodeAndMessage()` | 输入改为 `5\nl\nb\nq\n`；mock `listTransactions()` 成功、`calculateStatistics()` 失败；断言输出服务失败错误码和消息。 |
| `financeMenuAddsIncomeExpenseAndSummaryReflectsBalance()` | 空数据服务；输入进入收支菜单，记录收入 `1000.00` 与支出 `120.50`，执行列表和统计，返回主菜单执行汇总；断言列表包含两条记录，统计展示收入 `1000.00`、支出 `120.50`、结余 `879.50`，汇总的本月收入/支出/结余同步更新。 |
| `financeMenuViewsUpdatesAndDeletesTransaction()` | 新增一条收入后查看、修改为支出、删除，再查看；断言查看含旧类型和金额，修改后详情含新类型/金额/类别/日期/备注，删除成功输出 `操作成功`，删除后查看输出 `NOT_FOUND`。 |
| `financeMenuFiltersByTypeCategoryAndDateRange()` | 新增一个匹配记录和一个非匹配记录；执行类型+类别+日期范围筛选；使用 `between(...)` 截取 `收支筛选结果` 到 `主菜单` 或下一段标题，断言匹配项出现且非匹配项不出现，并断言筛选统计只反映匹配记录。 |
| `financeMenuFilterEmptyFieldsListAllTransactions()` | 新增至少两条不同类型/类别/日期记录；筛选时类型、类别、开始日期和结束日期均输入空行；断言筛选结果包含全部记录。 |
| `financeMenuStatisticsSupportsEmptyAndFilteredQuery()` | 新增收入和支出；第一次统计四个筛选字段全空，断言全量统计；第二次统计输入 `expense`、类别和日期范围，断言只统计匹配支出，收入为 `0.00`、支出为匹配金额、结余为负值。 |
| `financeMenuAcceptsLongCommandAliasesAndCaseInsensitiveType()` | 输入 `income/view/update/filter/list/delete/statistics/back/quit` 等长命令；类型输入小写或混合大小写如 `expense`；断言各长命令生效、筛选结果包含匹配记录、删除成功。 |
| `financeMenuRejectsInvalidIdWithoutServiceCallOrWriteOperation()` | 使用真实空数据服务或 mock 服务；输入非法 id 执行查看、修改或删除之一；断言输出 `VALIDATION_ERROR` 和 `收支记录 id 必须是正整数`，并验证不调用对应 `FinanceService` 方法或列表仍为空。 |
| `financeMenuRejectsInvalidTypeBeforeCallingFinanceService()` | 使用 mock `FinanceService`；筛选或修改时输入非法类型；断言输出 `收支类型必须是 INCOME 或 EXPENSE`，并验证不调用 `financeService` 的筛选/修改方法。 |
| `financeMenuRejectsInvalidDateBeforeWriteOperation()` | 新增或修改时输入非法日期；断言输出 `收支日期格式必须是 yyyy-MM-dd`，随后列表不包含本次待新增记录或原记录未被修改。 |
| `financeMenuRejectsFilterSingleDateWithoutServiceCall()` | 使用 mock `FinanceService`；筛选时仅填写开始日期或仅填写结束日期；断言输出 `收支开始日期和结束日期必须同时填写或同时为空`，并验证不调用 `financeService`。 |
| `financeMenuRejectsFilterEndBeforeStartWithoutServiceCall()` | 使用 mock `FinanceService`；筛选时结束日期早于开始日期；断言输出 `收支结束日期不能早于开始日期`，并验证不调用 `financeService`。 |
| `financeMenuShowsAmountValidationFailuresFromService()` | 真实空数据服务；分别输入空金额、`0`、负数或超过小数位约束的金额之一；断言输出 `VALIDATION_ERROR` 和服务稳定消息，随后列表输出 `暂无收支记录`。 |
| `financeMenuShowsMissingTransactionFailures()` | 空数据服务；查看、修改、删除不存在 id；断言均输出 `NOT_FOUND`，删除不存在不输出 `操作成功`。 |
| `financeMenuDeleteRecomputesStatistics()` | 新增收入和支出，删除支出后执行统计；断言删除前后统计不同，删除后支出为 `0.00` 且结余等于收入。 |
| `financeMenuUnknownHelpBackAndMainMenuContinuation()` | 输入未知收支命令、帮助、返回主菜单、汇总、退出；断言未知命令提示、收支帮助、主菜单汇总均出现。 |
| `financeMenuBlankCommandPromptsAgainAndStaysInFinanceMenu()` | 输入空收支命令后执行列表；断言输出 `请输入收支命令。` 且随后仍展示 `收支记录列表`。 |
| `financeMenuExitsOnEofDuringCommandRead()` | 输入只包含 `5\n`；断言进入收支菜单后 EOF 正常结束，不抛异常。 |
| `financeMenuExitsOnEofDuringIncomeFields()` | 输入 `5\ni\n100.00\n工资\n` 等中途 EOF；断言程序正常结束，不抛异常，未输出 `收支记录详情`，未创建半成品收支记录。 |

**测试数据约束**：

- 固定当前时间由 `servicesWithoutDemoData()` 提供：`2026-01-15T09:00`，因此本月汇总应包含 `2026-01` 的收支记录。
- 收入/支出新增输入字段顺序必须与实现一致：金额、类别、日期、备注。
- 修改输入字段顺序必须与实现一致：收支记录 id、类型、金额、类别、日期、备注。
- 筛选与统计输入字段顺序必须与实现一致：类型、类别、开始日期、结束日期。
- 使用 ISO 日期输入，例如 `2026-01-10`、`2026-01-15`、`2026-01-20`、`2026-02-01`。
- 金额展示断言应使用两位小数格式，例如 `1000.00`、`120.50`、`0.00`。
- 所有测试通过 `ApplicationFactory.create(Map.of(), new FixedTimeProvider(...))` 或局部 Mockito mock 装配，不设置真实 API Key，不触发真实 AI 请求。

## 错误处理

- 服务层失败继续统一使用 `printResult(OperationResult<T>)` 输出：`失败: {ErrorCode} - {message}`。
- 控制台解析失败不构造业务服务调用，直接输出 `失败: VALIDATION_ERROR - {清晰提示}`，随后留在收支子菜单。
- 收支记录 id 非正整数、空值、非数字、小数或超出 `long` 范围统一输出 `收支记录 id 必须是正整数`。
- 收支类型为空或非 `INCOME` / `EXPENSE` 统一输出 `收支类型必须是 INCOME 或 EXPENSE`；大小写不敏感。
- 收支日期为空或格式错误统一输出 `收支日期格式必须是 yyyy-MM-dd`。新增、修改、筛选和统计日期均使用同一日期格式提示。
- 筛选/统计开始日期和结束日期均为空属于合法空周期筛选；仅填写一个日期输出 `收支开始日期和结束日期必须同时填写或同时为空`。
- `DateRange` 构造时若开始日期晚于结束日期，控制台层捕获 `IllegalArgumentException` 并统一输出 `收支结束日期不能早于开始日期`，不让运行时异常泄漏到 `run()` 调用方。
- 金额字段作为原始字符串传给 `FinanceService`，控制台层不拒绝空金额、零金额、负金额、小数位过多或超出金额值对象约束的输入；这些错误由服务返回 `OperationResult` 并通过 `printResult(...)` 稳定展示。
- 类别字段作为原始字符串传给 `FinanceService`。筛选类别为空表示不筛选类别；记录和修改时类别空白由服务返回业务失败结果。
- 备注字段作为原始字符串传给 `FinanceService`，空备注允许传递给服务。
- `readLine(...)` 读取到 EOF 返回 `null` 且不自行设置 `running = false`；主菜单 EOF 继续由 `dispatch(null)` 处理，收支字段读取方法负责将 EOF 转为 `running = false`。

## 行为契约

- 主菜单命令 `5` 进入收支子菜单后，除 `b/back` 和 EOF 外不返回主菜单；命令处理结束后仍等待下一条收支命令。
- `l/list` 必须同时展示全量统计和全部记录；空列表仍先展示统计，再输出 `暂无收支记录`。
- `f/filter` 必须展示筛选结果统计和全部筛选记录；空结果输出筛选统计和 `暂无收支记录`。
- `s/statistics` 支持全量或筛选统计；四个筛选字段全空时调用无参 `calculateStatistics()`，否则调用带 `TransactionQuery` 的重载。
- 所有列表输出不得使用 `limit(10)`。
- 新增、查看和修改成功时展示 `收支记录详情`；删除成功时只通过 `printResult(...)` 输出 `操作成功`。
- 任一控制台校验失败不得调用对应业务服务写入方法；若失败发生在构造筛选/统计查询前，也不得调用对应查询或统计服务。
- 删除后再次统计必须基于当前仓储状态重新计算，不缓存前一次统计结果。

## 依赖关系

- 依赖既有 `assistant.finance.FinanceService` 公开方法完成全部业务操作。
- 依赖既有 `assistant.finance.TransactionQuery.of(TransactionType, String, DateRange)` 表达组合筛选。
- 依赖既有 `assistant.finance.TransactionType` 枚举表达收入/支出类型。
- 依赖既有 `assistant.finance.TransactionView` 和 `assistant.finance.FinanceStatistics` 作为输出视图。
- 依赖既有 `assistant.common.DateRange`、`EntityId`、`OperationResult`、`ErrorCode`、`ParsedInput` 相关本地模式。
- 不向后续任务暴露新的 public API；所有新增逻辑均为 `ConsoleApplication` 私有实现细节和测试覆盖。
