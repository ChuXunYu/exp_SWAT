# 详细设计（v22）

## 概述

本轮设计目标是在 `assistant.app` 包中新增控制台应用装配、主入口与基础菜单骨架，使 `java-ai-assistant` 可以通过 `main` 启动为命令行程序，并通过服务层展示或触发 8 个核心功能入口。

本轮实现范围：

- `ApplicationServices`：只读应用服务装配结果，集中暴露任务、日程、学习计划、收支、笔记、汇总、AI 问答和 AI 草稿生命周期服务引用，以及当前 `TimeProvider`。
- `ApplicationFactory`：统一生产装配入口，创建内存仓储、递增编号生成器、系统时间提供者、业务服务、汇总服务、AI 配置、DeepSeek 客户端、草稿导入和草稿生命周期服务；生产配置从环境变量与 JVM 系统属性合并读取，系统属性覆盖同名环境变量；测试入口支持显式 `Map<String, String>` 与显式 `TimeProvider`。
- `DemoDataFactory`：通过公开服务写入一组可重复演示数据，覆盖任务、日程、学习计划、收支、笔记和汇总可见数据；日期只基于传入 `TimeProvider.today()` 相对生成。
- `ConsoleApplication`：负责主循环、菜单展示、输入读取、命令分发和结果展示；基础菜单提供汇总、任务、日程、学习计划、收支、笔记、AI 问答、AI 草稿、帮助和退出入口；控制台层只调用服务，不直接访问仓储或 HTTP。
- `Main`：只负责生产装配、可选加载演示数据并启动控制台应用。

本轮不实现：

- 完整增删改查交互子菜单。
- 文件、数据库或跨进程持久化。
- 测试中的真实环境变量读取、真实 DeepSeek 调用、真实网络、真实当前时间或外部文件访问。
- README 或运行文档修改。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/Main.java` | 新建 | 程序主入口，调用生产装配、按配置可选加载演示数据并启动 `ConsoleApplication`。 |
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 新建 | 控制台主循环、菜单展示、输入读取、命令分发、结果展示和最小可演示功能入口。 |
| `java-ai-assistant/src/main/java/assistant/app/ApplicationServices.java` | 新建 | 应用服务只读装配结果，集中持有顶层控制台需要调用的服务引用。 |
| `java-ai-assistant/src/main/java/assistant/app/ApplicationFactory.java` | 新建 | 生产与测试可控装配入口，构造内存仓储、编号生成器、时间提供者、业务服务、AI 服务和草稿服务。 |
| `java-ai-assistant/src/main/java/assistant/app/DemoDataFactory.java` | 新建 | 通过公开服务加载可重复演示数据，覆盖汇总可见的业务数据。 |
| `java-ai-assistant/src/test/java/assistant/app/ApplicationFactoryTest.java` | 新建 | 验证装配非空、配置合并、显式配置可测、AI 未配置失败和空依赖防御。 |
| `java-ai-assistant/src/test/java/assistant/app/DemoDataFactoryTest.java` | 新建 | 验证演示数据通过服务写入、固定时间下可重复、汇总可见且无直接环境依赖。 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 新建 | 验证菜单循环、命令分发、OperationResult 展示、AI 未配置提示、帮助和退出行为。 |

## 类型定义

### `ApplicationServices`

**形态**：`record`

**包路径**：`assistant.app`

**职责**：作为控制台层唯一装配结果，提供顶层应用服务引用，不暴露仓储、集合或 HTTP 传输对象。

**类型签名定义**：

```java
public record ApplicationServices(
        TaskService taskService,
        ScheduleService scheduleService,
        StudyPlanService studyPlanService,
        FinanceService financeService,
        NoteService noteService,
        SummaryService summaryService,
        AiAssistantService aiAssistantService,
        DraftLifecycleService draftLifecycleService,
        TimeProvider timeProvider)
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| canonical constructor | 构造器 | 对所有组件执行 `Objects.requireNonNull(component, "{componentName}")`；参数名分别为 `taskService`、`scheduleService`、`studyPlanService`、`financeService`、`noteService`、`summaryService`、`aiAssistantService`、`draftLifecycleService`、`timeProvider`。 |

**构造方式**：

- 仅由 `ApplicationFactory.create()`、`ApplicationFactory.create(Map<String, String>)`、`ApplicationFactory.create(Map<String, String>, TimeProvider)` 和测试夹具构造。

**类型关系**：

- 组合 `assistant.task.TaskService`、`assistant.schedule.ScheduleService`、`assistant.study.StudyPlanService`、`assistant.finance.FinanceService`、`assistant.note.NoteService`、`assistant.summary.SummaryService`、`assistant.ai.AiAssistantService`、`assistant.ai.DraftLifecycleService`、`assistant.testability.TimeProvider`。

### `ApplicationFactory`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：创建完整应用服务图，并隔离生产配置读取与测试可控装配。

**类型签名定义**：

```java
public final class ApplicationFactory
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final AiConfigurationLoader aiConfigurationLoader` | 构造时非空；AI 配置解析唯一入口。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ApplicationFactory()` | 构造器 | 委托 `this(new AiConfigurationLoader())`。 |
| `ApplicationFactory(AiConfigurationLoader aiConfigurationLoader)` | 包可见构造器 | 测试可注入 loader；`aiConfigurationLoader == null` 抛 `NullPointerException("aiConfigurationLoader")`。 |
| `public ApplicationServices create()` | `ApplicationServices` | 生产装配入口；创建 `new SystemTimeProvider()`；调用 `readMergedConfiguration()` 读取配置；不得读取用户文件；不得访问网络。 |
| `public ApplicationServices create(Map<String, String> configurationValues)` | `ApplicationServices` | 测试可控装配入口；创建 `new SystemTimeProvider()`；只使用传入配置 map，不读取真实环境或系统属性。 |
| `public ApplicationServices create(Map<String, String> configurationValues, TimeProvider timeProvider)` | `ApplicationServices` | 测试完全可控装配入口；`configurationValues == null` 抛 `NullPointerException("configurationValues")`；`timeProvider == null` 抛 `NullPointerException("timeProvider")`；只使用传入配置与时间提供者。 |

**私有辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private ApplicationServices createWith(Map<String, String> configurationValues, TimeProvider timeProvider)` | `ApplicationServices` | 统一装配流程；配置解析失败时使用 `AiConfiguration.defaultWithoutApiKey()` 保证本地功能可启动；不得抛出配置校验异常到 `Main`。 |
| `private Map<String, String> readMergedConfiguration()` | `Map<String, String>` | 复制 `System.getenv()` 中所有值到可变 `LinkedHashMap`，再遍历 `System.getProperties().stringPropertyNames()` 覆盖同名键；返回不可修改 map 或新 map；系统属性可覆盖同名环境变量。 |
| `private AiConfiguration loadAiConfiguration(Map<String, String> values)` | `AiConfiguration` | 调用 `aiConfigurationLoader.load(values)`；成功返回载荷；失败返回 `AiConfiguration.defaultWithoutApiKey()`。 |
| `private ContextProvider contextProvider(SummaryService summaryService)` | `ContextProvider` | 返回 lambda 或私有实现，调用 `summaryService.buildLocalContext()`。 |

**装配流程契约**：

1. 创建单个 `IdGenerator idGenerator = new IncrementalIdGenerator()`，供任务、日程、学习计划、收支、笔记和草稿共享递增编号。
2. 创建 `TaskService(new InMemoryTaskRepository(), idGenerator)`。
3. 创建 `ScheduleService(new InMemoryScheduleRepository(), idGenerator, timeProvider, new ScheduleConflictPolicy())`。
4. 创建 `StudyPlanService(new InMemoryStudyPlanRepository(), idGenerator, timeProvider, new StudyPlanAnalysisService())`。
5. 创建 `FinanceService(new InMemoryTransactionRepository(), idGenerator, new FinanceStatisticsService())`。
6. 创建 `NoteService(new InMemoryNoteRepository(), idGenerator, timeProvider, new NoteSearchPolicy())`。
7. 创建 `SummaryService(taskService, scheduleService, studyPlanService, financeService, noteService, timeProvider)`。
8. 创建 `AiConfiguration aiConfiguration = loadAiConfiguration(configurationValues)`。
9. 创建 `PromptBuilder promptBuilder = new PromptBuilder()`。
10. 创建 `AiClient aiClient = new DeepSeekAiClient(aiConfiguration, JdkAiHttpTransport.create(aiConfiguration.timeout()))`。
11. 创建 `AiAssistantService(aiConfiguration, contextProvider(summaryService), promptBuilder, aiClient)`。
12. 创建 `SuggestionDraftRepository draftRepository = new InMemorySuggestionDraftRepository()`。
13. 创建 `DraftImportService(taskService, studyPlanService)`。
14. 创建 `DraftLifecycleService(draftRepository, draftImportService)`。
15. 返回 `new ApplicationServices(...)`。

**构造方式**：

- `Main` 使用 `new ApplicationFactory().create()`。
- 单元测试使用 `create(Map.of(...), new FixedTimeProvider(...))`，禁止依赖真实环境。

**类型关系**：

- 依赖 `assistant.ai`、`assistant.common`、`assistant.finance`、`assistant.note`、`assistant.schedule`、`assistant.study`、`assistant.summary`、`assistant.task`、`assistant.testability` 包中的已有类型。
- 不向 `ApplicationServices` 暴露 `InMemory*Repository`、`AiHttpTransport`、`AiConfiguration`、`PromptBuilder`、`ContextProvider` 或集合对象。

### `DemoDataFactory`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：通过公开服务写入一组固定语义、相对日期的演示数据，便于控制台首次启动后看到非空列表与汇总。

**类型签名定义**：

```java
public final class DemoDataFactory
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DemoDataFactory()` | 构造器 | 无状态构造器。 |
| `public void load(ApplicationServices services)` | `void` | `services == null` 抛 `NullPointerException("services")`；通过 `services.*Service()` 公开方法写入数据；任一写入返回失败时抛 `IllegalStateException("failed to load demo data: " + message)`。 |

**私有辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void requireSuccess(OperationResult<?> result)` | `void` | `result == null` 抛 `NullPointerException("result")`；失败时抛 `IllegalStateException`，消息包含失败 `ErrorCode` 和 `message`。 |
| `private Set<String> tags(String... values)` | `Set<String>` | 返回 `LinkedHashSet`，保持标签顺序。 |

**演示数据契约**：

- 以 `LocalDate today = services.timeProvider().today()` 和 `LocalDateTime start = today.atTime(...)` 生成相对日期，不读取真实日期。
- 至少创建：
  - 2 条任务：一条今日高优先级任务，一条明日普通任务。
  - 2 条日程：一条今日上午日程，一条今日下午日程；时间段互不重叠。
  - 2 条学习计划：一条覆盖本周且进度非 100，一条已经完成或接近完成。
  - 2 条收支：本月一条收入、一条支出。
  - 2 篇笔记：含至少两个标签，使汇总 `noteTagDistribution` 非空。
- 不创建 AI 草稿；AI 草稿由 AI 建议解析或后续菜单细化负责。
- 不直接调用任何 `InMemory*Repository` 或实体构造器。

**类型关系**：

- 依赖 `ApplicationServices`、`DateTimeRange`、`OperationResult`、`TaskPriority` 和各公开服务。

### `ConsoleApplication`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：实现可测试的控制台主循环、基础菜单、命令分发和稳定结果展示。

**类型签名定义**：

```java
public final class ConsoleApplication
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final ApplicationServices services` | 构造时非空。 |
| `private final BufferedReader input` | 构造时非空；测试可传入 `StringReader` 包装。 |
| `private final PrintWriter output` | 构造时非空；测试可传入 `StringWriter` 包装。 |
| `private boolean running` | `run()` 内控制主循环；初始 `false`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ConsoleApplication(ApplicationServices services, Reader input, Writer output)` | 构造器 | `services == null` 抛 `NullPointerException("services")`；`input == null` 抛 `NullPointerException("input")`；`output == null` 抛 `NullPointerException("output")`；内部创建 `BufferedReader` 与 `PrintWriter(output, true)`。 |
| `public void run()` | `void` | 打印欢迎语和主菜单；循环读取命令；EOF 时正常退出；每次处理后刷新输出；不得抛出 `IOException`，读取失败时显示错误并退出。 |

**私有辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private void printWelcome()` | `void` | 输出应用名称。 |
| `private void printMainMenu()` | `void` | 输出命令列表，至少包含 `1` 汇总、`2` 任务、`3` 日程、`4` 学习计划、`5` 收支、`6` 笔记、`7` AI 问答、`8` AI 草稿、`h` 帮助、`q` 退出。 |
| `private void dispatch(String rawCommand)` | `void` | 对 `rawCommand == null` 视为退出；对空白命令提示重新输入；大小写不敏感处理 `h`、`help`、`q`、`quit`、`exit`；未知命令输出帮助提示。 |
| `private void showSummary()` | `void` | 调用 `services.summaryService().getDashboardSummary()` 并通过 `printResult` 展示；成功时输出今日、今日任务数、今日日程数、本周学习计划数、本月收入、支出、结余、笔记数和标签数。 |
| `private void showTasks()` | `void` | 调用 `services.taskService().listTasks()`；成功时列出最多 10 条任务的 id、标题、优先级、状态和截止日期；空列表显示“暂无任务”。 |
| `private void showSchedules()` | `void` | 调用 `services.scheduleService().listSchedules()`；成功时列出最多 10 条日程的 id、名称、状态、起止时间和地点；空列表显示“暂无日程”。 |
| `private void showStudyPlans()` | `void` | 调用 `services.studyPlanService().listStudyPlans()`；成功时列出最多 10 条学习计划的 id、目标、状态、进度和周期；空列表显示“暂无学习计划”。 |
| `private void showTransactions()` | `void` | 调用 `services.financeService().listTransactions()` 和 `services.financeService().calculateStatistics()`；任一失败按失败展示；成功时输出统计和最多 10 条收支记录。 |
| `private void showNotes()` | `void` | 调用 `services.noteService().listNotes()`；成功时列出最多 10 篇笔记的 id、标题、日期和标签；空列表显示“暂无笔记”。 |
| `private void askAi()` | `void` | 提示用户输入问题；空白问题显示校验提示并返回主菜单；调用 `services.aiAssistantService().ask(AiScenario.GENERAL_QA, question)`；未配置 API Key 时展示 `AI_NOT_CONFIGURED` 且继续主循环。 |
| `private void showDrafts()` | `void` | 调用 `services.draftLifecycleService().listDrafts()`；成功时列出草稿 id、类型、状态和摘要；空列表显示“暂无 AI 草稿”。 |
| `private void printHelp()` | `void` | 输出命令说明，不访问服务。 |
| `private void stop()` | `void` | 设置 `running = false` 并输出退出提示。 |
| `private <T> boolean printResult(OperationResult<T> result)` | `boolean` | `result == null` 抛 `NullPointerException("result")`；失败输出 `"失败: " + result.getErrorCode() + " - " + result.getMessage()` 并返回 `false`；成功无载荷输出“操作成功”并返回 `true`；有载荷由调用方继续展示并返回 `true`。 |
| `private String readLine(String prompt)` | `String` | 输出 prompt 并读取一行；EOF 返回 `null`；`IOException` 转为显示错误并返回 `null`。 |

**最小菜单行为契约**：

- 控制台层不得创建业务实体、不得访问仓储、不得创建 `DeepSeekAiClient` 或 `JdkAiHttpTransport`。
- 任务、日程、学习计划、收支、笔记、草稿入口本轮只需列表或摘要展示，并自动返回主菜单。
- AI 问答入口只读取一行问题；调用 `AiAssistantService.ask(...)`；API Key 缺失时显示 `AI_NOT_CONFIGURED`，之后仍可输入本地菜单命令。
- 所有 `OperationResult` 失败展示必须包含 `ErrorCode` 名称和简短消息。
- 主循环在 `q`、`quit`、`exit` 或 EOF 下退出。

**构造方式**：

- `Main` 使用 `new ConsoleApplication(services, new InputStreamReader(System.in, StandardCharsets.UTF_8), new OutputStreamWriter(System.out, StandardCharsets.UTF_8))`。
- 单元测试使用 `StringReader` 和 `StringWriter`。

**类型关系**：

- 依赖 `ApplicationServices` 和各服务返回的 view/summary DTO。
- 只使用 `AiScenario.GENERAL_QA` 触发本轮 AI 问答入口。

### `Main`

**形态**：`final class`

**包路径**：`assistant.app`

**职责**：Java 程序主入口，连接生产装配、可选演示数据和控制台应用。

**类型签名定义**：

```java
public final class Main
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static void main(String[] args)` | `void` | 调用 `new ApplicationFactory().create()`；若 `isDemoDataEnabled()` 为 `true`，调用 `new DemoDataFactory().load(services)`；创建并运行 `ConsoleApplication`。 |

**私有辅助接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private Main()` | 构造器 | 抛 `UnsupportedOperationException("utility class")` 或空私有构造器；禁止实例化。 |
| `private static boolean isDemoDataEnabled()` | `boolean` | 读取 JVM 系统属性 `ASSISTANT_DEMO_DATA`，若不存在再读取环境变量 `ASSISTANT_DEMO_DATA`；值为 `false`、`0`、`no` 时禁用；其他值或缺失时启用。 |

**行为契约**：

- `main` 方法不得直接创建业务记录、不得直接读取仓储、不得直接调用 DeepSeek HTTP。
- `main` 可以读取演示数据开关，但 AI 配置读取只通过 `ApplicationFactory.create()` 完成。

### `ApplicationFactoryTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.app`

**职责**：验证应用装配边界、配置合并和测试可控性。

**类型签名定义**：

```java
class ApplicationFactoryTest
```

**测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `createWithExplicitConfigurationBuildsAllServices()` | 使用 `Map.of()` 与 `FixedTimeProvider` 调用 `create(Map, TimeProvider)`；断言 `ApplicationServices` 中所有服务非空；调用 `summaryService.getDashboardSummary()` 成功。 |
| `createWithExplicitConfigurationDoesNotReadRealEnvironment()` | 使用空配置和固定时间装配；调用 `aiAssistantService.ask(AiScenario.GENERAL_QA, "hello")` 返回 `AI_NOT_CONFIGURED`；不访问网络。 |
| `createUsesProvidedAiConfiguration()` | 传入 `DEEPSEEK_API_KEY` 等配置后装配；使用 `aiAssistantService.ask(...)` 不应返回 `AI_NOT_CONFIGURED`；测试通过包可见构造器注入 fake loader 或 mock `AiConfigurationLoader`，避免真实网络调用路径被触发。 |
| `createRejectsNullConfigurationMap()` | `create(null, fixedTimeProvider)` 抛 `NullPointerException("configurationValues")`。 |
| `createRejectsNullTimeProvider()` | `create(Map.of(), null)` 抛 `NullPointerException("timeProvider")`。 |
| `constructorRejectsNullLoader()` | `new ApplicationFactory(null)` 抛 `NullPointerException("aiConfigurationLoader")`。 |
| `applicationServicesRejectsNullComponents()` | 对 `ApplicationServices` 任一组件传 null 时抛对应参数名的 `NullPointerException`。 |

**测试夹具约束**：

- 默认测试不得调用 `ApplicationFactory.create()` 生产入口，避免真实环境读取。
- 如需验证 `readMergedConfiguration()` 的系统属性覆盖规则，应通过包可见辅助或临时设置后恢复系统属性，且不得依赖真实环境变量具体值；优先使用显式配置入口覆盖配置解析行为。

### `DemoDataFactoryTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.app`

**职责**：验证演示数据固定、可重复、通过公开服务写入且能被汇总看见。

**类型签名定义**：

```java
class DemoDataFactoryTest
```

**测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `loadCreatesVisibleDemoDataThroughServices()` | 使用 `ApplicationFactory.create(Map.of(), new FixedTimeProvider(LocalDateTime.of(2026, 1, 15, 9, 0)))` 装配；调用 `load`；断言任务、日程、学习计划、收支、笔记列表均非空；`summaryService.getDashboardSummary()` 成功且今日/本周/本月相关计数非空。 |
| `loadUsesFixedTimeProviderForRelativeDates()` | 固定日期装配后加载；断言今日任务 dueDate 等于固定 today，日程覆盖固定 today，本月收支落在固定月份。 |
| `loadRejectsNullServices()` | `load(null)` 抛 `NullPointerException("services")`。 |
| `loadPropagatesServiceFailureAsIllegalStateException()` | 使用 mock 或手工构造 `ApplicationServices`，使其中一个服务公开写入方法返回失败；断言 `IllegalStateException` 消息包含 `failed to load demo data` 和错误码。 |

**测试夹具约束**：

- 不读取真实时间；只使用 `FixedTimeProvider`。
- 不断言具体 id 起始值以外的内部仓储状态；通过公开服务列表和汇总断言。

### `ConsoleApplicationTest`

**形态**：JUnit 5 测试类

**包路径**：`assistant.app`

**职责**：验证控制台菜单主循环、命令分发和结果展示的稳定行为。

**类型签名定义**：

```java
class ConsoleApplicationTest
```

**测试夹具规划**：

| 辅助成员 | 形态 | 职责 |
|----------|------|------|
| `private ApplicationServices servicesWithDemoData()` | 辅助方法 | 使用显式配置和 `FixedTimeProvider` 装配，并调用 `DemoDataFactory.load(...)`。 |
| `private String runWithInput(ApplicationServices services, String input)` | 辅助方法 | 使用 `StringReader` / `StringWriter` 运行 `ConsoleApplication` 并返回输出文本。 |

**测试用例**：

| 测试方法名 | 覆盖契约 |
|------------|----------|
| `runPrintsMenuAndExitsOnQuit()` | 输入 `"q\n"`；输出包含应用名称、汇总入口、退出提示。 |
| `runExitsOnEndOfInput()` | 输入空字符串；不抛异常，输出包含菜单。 |
| `summaryCommandDisplaysDashboardSummary()` | 输入 `"1\nq\n"`；输出包含今日、本月收入或结余等汇总文本。 |
| `listCommandsDisplayEachCoreEntry()` | 输入 `"2\n3\n4\n5\n6\n8\nq\n"`；输出分别包含任务、日程、学习计划、收支、笔记、AI 草稿入口结果。 |
| `aiCommandShowsNotConfiguredAndContinues()` | 空配置装配；输入 `"7\n今天有什么安排？\n1\nq\n"`；输出包含 `AI_NOT_CONFIGURED`，且后续汇总命令仍被执行。 |
| `helpAndUnknownCommandDisplayHelp()` | 输入 `"x\nh\nq\n"`；输出包含未知命令提示和帮助说明。 |
| `constructorRejectsNullDependencies()` | 构造器三个参数 null 分别抛对应参数名 `NullPointerException`。 |

**测试夹具约束**：

- 不真实访问 DeepSeek；AI 未配置路径必须在 `AiAssistantService` 内返回 `AI_NOT_CONFIGURED` 后短路。
- 不使用 `System.in` 或 `System.out`。

## 错误处理

- `ApplicationServices` 对所有组件执行空引用防御，构造失败使用参数名明确的 `NullPointerException`。
- `ApplicationFactory.create(Map, TimeProvider)` 对显式配置和时间提供者执行空引用防御；生产 `create()` 读取配置后不得因 AI 配置错误导致本地应用无法启动，配置解析失败时降级为 `AiConfiguration.defaultWithoutApiKey()`。
- `DemoDataFactory.load(...)` 中服务写入失败统一转为 `IllegalStateException("failed to load demo data: " + errorCode + " - " + message)`；这是启动期演示数据初始化错误，不返回 `OperationResult`。
- `ConsoleApplication` 不向外传播服务失败；所有 `OperationResult.failure` 均以稳定文本输出，必须包含 `ErrorCode` 与简短消息。
- `ConsoleApplication` 读取输入发生 `IOException` 时输出 `"输入读取失败，程序退出。"` 并结束主循环。
- AI API Key 缺失由 `AiAssistantService.ask(...)` 返回 `OperationResult.failure(ErrorCode.AI_NOT_CONFIGURED, "DeepSeek API key is not configured")`；控制台展示该错误后继续可用。

## 行为契约

- 控制台层只调用 `ApplicationServices` 暴露的公开服务，不直接访问仓储、实体集合、`AiHttpTransport` 或 DeepSeek HTTP。
- `ApplicationFactory` 是生产装配唯一入口；测试必须优先使用显式 `Map<String, String>` 和 `FixedTimeProvider` 入口。
- 生产配置合并规则：环境变量作为基础，JVM 系统属性中同名 key 覆盖环境变量；配置 key 沿用 `AiConfigurationLoader.API_KEY_NAME`、`BASE_URL_NAME`、`MODEL_NAME`、`TIMEOUT_SECONDS_NAME`。
- `ApplicationServices` 不暴露 `AiConfiguration`，避免控制台直接依赖 HTTP 配置对象。
- `DemoDataFactory` 演示数据必须通过公开服务写入；不得直接实例化仓储或业务实体；不得依赖真实当前日期。
- `DemoDataFactory` 可重复指的是固定时间与空内存装配下生成同样语义和相对日期的数据；不要求对同一 `ApplicationServices` 多次调用去重。
- `ConsoleApplication.run()` 每处理一个命令后回到主菜单，除非收到退出命令或 EOF。
- 列表入口最多展示 10 条，避免控制台输出过长；统计或空列表提示必须稳定。
- `Main.main(...)` 不解析业务命令，不创建业务记录，不调用 AI HTTP；只组织装配、演示数据和控制台运行。

## 依赖关系

- 依赖 Java 17 标准库：`java.io.BufferedReader`、`Reader`、`Writer`、`PrintWriter`、`InputStreamReader`、`OutputStreamWriter`、`StandardCharsets`、`LocalDate`、`LocalDateTime`、`Map`、`LinkedHashMap`、`Set`、`LinkedHashSet`、`Objects`。
- 依赖已有服务层：
  - `assistant.task.TaskService`、`TaskPriority`、`TaskView`
  - `assistant.schedule.ScheduleService`、`ScheduleConflictPolicy`、`ScheduleView`
  - `assistant.study.StudyPlanService`、`StudyPlanAnalysisService`、`StudyPlanView`
  - `assistant.finance.FinanceService`、`FinanceStatisticsService`、`FinanceStatistics`、`TransactionView`
  - `assistant.note.NoteService`、`NoteSearchPolicy`、`NoteView`
  - `assistant.summary.SummaryService`、`DashboardSummary`
  - `assistant.ai.AiAssistantService`、`AiScenario`、`AiConfigurationLoader`、`AiConfiguration`、`DeepSeekAiClient`、`JdkAiHttpTransport`、`PromptBuilder`、`ContextProvider`、`InMemorySuggestionDraftRepository`、`DraftImportService`、`DraftLifecycleService`、`SuggestionDraftView`
  - `assistant.common.OperationResult`、`ErrorCode`、`DateTimeRange`
  - `assistant.testability.IdGenerator`、`IncrementalIdGenerator`、`SystemTimeProvider`、`TimeProvider`
- 对后续任务暴露的稳定接口：`ApplicationFactory.create(...)`、`ApplicationServices` record accessors、`DemoDataFactory.load(...)`、`ConsoleApplication.run()` 和 `Main.main(...)`。
