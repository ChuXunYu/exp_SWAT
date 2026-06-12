# 任务指令（v22）

## 动作
NEW

## 任务描述
新增 `assistant.app` 控制台应用装配、主入口与基础菜单骨架，使 `java-ai-assistant` 成为可通过 `main` 启动的命令行程序，并通过服务层展示/触发 8 个核心功能入口。

预期生产文件：
- `java-ai-assistant/src/main/java/assistant/app/Main.java`
- `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java`
- `java-ai-assistant/src/main/java/assistant/app/ApplicationServices.java`
- `java-ai-assistant/src/main/java/assistant/app/ApplicationFactory.java`
- `java-ai-assistant/src/main/java/assistant/app/DemoDataFactory.java`

预期测试文件：
- `java-ai-assistant/src/test/java/assistant/app/ApplicationFactoryTest.java`
- `java-ai-assistant/src/test/java/assistant/app/DemoDataFactoryTest.java`
- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

本轮范围要求：
- `ApplicationServices` 作为只读装配结果，集中持有任务、日程、学习计划、收支、笔记、汇总、AI 问答和 AI 草稿生命周期等应用服务引用，不暴露底层仓储集合或 HTTP 传输对象。
- `ApplicationFactory` 负责生产装配：创建内存仓储、递增编号生成器、系统时间提供者、各业务服务、`SummaryService`、`ContextProvider`、`PromptBuilder`、`AiConfiguration`、`DeepSeekAiClient`、`DraftImportService` 和 `DraftLifecycleService`。配置读取只发生在生产装配入口，并支持从环境变量与 JVM 系统属性合并读取，系统属性可覆盖同名环境变量；测试必须能通过显式 `Map<String, String>` 或等价重载注入配置，避免读取真实环境。
- `DemoDataFactory` 负责可选初始化一组可重复演示数据，至少覆盖任务、日程、学习计划、收支、笔记和汇总可见数据；演示数据必须通过公开服务写入，不得直接操作仓储；不得创建依赖真实当前日期的不可预测数据，生产装配可基于 `TimeProvider.today()` 生成相对日期，测试用固定时间断言。
- `ConsoleApplication` 负责主循环、菜单展示、输入读取、命令分发和结果展示。基础菜单至少包含：查看汇总、任务、日程、学习计划、收支、笔记、AI 问答、AI 草稿、帮助、退出等入口。对于本轮未细化的子菜单操作，可以先提供“入口可达/列表或摘要展示/返回主菜单”的最小可演示行为，但不得把业务规则写入控制台层。
- `Main` 只负责调用生产装配、可选加载演示数据并启动 `ConsoleApplication`；不得在 `main` 方法中直接创建业务记录、直接读取仓储或直接调用 DeepSeek HTTP。
- 控制台层对 `OperationResult` 的展示应稳定：成功显示载荷摘要或操作成功，失败显示 `ErrorCode` 与简短消息；AI API Key 缺失时应显示 `AI_NOT_CONFIGURED`，本地菜单仍能继续使用。
- 默认单元测试不得读取真实环境变量、访问真实 DeepSeek、真实网络、真实 API Key、真实当前时间或外部文件。

## 选择理由
v1-v21 已完成 Maven/JUnit 基线、通用值对象、任务、日程、学习计划、收支、笔记、汇总统计、AI 配置与协议层、结构化建议解析、草稿生命周期和正式导入服务闭环。需求和技术方案仍明确要求命令行交互程序、清晰菜单或操作入口、应用装配和运行说明。本轮补齐 `assistant.app` 后，项目才具备可启动、可演示的顶层应用形态。

控制台入口应放在业务核心稳定之后实现，避免把输入输出细节混入服务层或影响已通过的 815 个单元测试。通过 `ApplicationServices` 和 `ApplicationFactory` 固定装配边界，也能让后续菜单细化、集成测试和文档补全复用同一启动路径。

## 任务上下文
需求要求：
- 应用默认可实现为命令行交互程序，核心业务逻辑与界面层分离。
- 程序应提供清晰菜单或操作入口，使用户能够选择不同功能。
- DeepSeek API Key 不应硬编码，网络不可用或 API Key 未配置时应给出明确提示，本地非 AI 功能仍可继续使用。
- 应用数据可以存放在内存集合中，建议提供一组可重复初始化的示例数据。

技术方案要求：
- `assistant.app` 包包含 `Main`、控制台菜单、输入解析、结果展示、应用装配。
- 控制台层只处理菜单、输入解析和展示，不直接访问集合或 HTTP。
- 启动层从环境变量和 JVM 系统属性读取配置，应用装配层将配置转换为 `AiConfiguration`、`TimeProvider`、`IdGenerator` 和服务实例。
- 示例数据由 `DemoDataFactory` 在启动时可选装配，不从真实用户文件读取。

## 已有代码上下文
当前工程已有：
- 任务服务：`assistant.task.TaskService`、`TaskQuery`、`TaskView`、内存仓储。
- 日程服务：`assistant.schedule.ScheduleService`、`ScheduleQuery`、`ScheduleView`、冲突策略、内存仓储。
- 学习计划服务：`assistant.study.StudyPlanService`、`StudyPlanQuery`、`StudyPlanView`、状态分析、内存仓储。
- 收支服务：`assistant.finance.FinanceService`、`FinanceStatisticsService`、`TransactionQuery`、内存仓储。
- 笔记服务：`assistant.note.NoteService`、`NoteQuery`、`NoteView`、搜索策略、内存仓储。
- 汇总服务：`assistant.summary.SummaryService` 可生成 `DashboardSummary` 和 `LocalContext`。
- AI 服务：`AiConfigurationLoader`、`AiConfiguration`、`PromptBuilder`、`AiAssistantService`、`DeepSeekAiClient`、`JdkAiHttpTransport`、`StructuredSuggestionParser`、`DraftImportService`、`DraftLifecycleService`。
- 可测试基础：`IncrementalIdGenerator`、`SystemTimeProvider`、`FixedTimeProvider`。

当前缺口：
- 没有 `assistant.app` 包和 `Main` 入口。
- README 中仍以“Future AI client code”描述 AI 配置，后续文档任务可修正；本轮可只在代码测试范围内完成入口和装配。
- 没有统一生产装配路径，也没有演示数据初始化入口。
