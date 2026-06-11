# Java AI 个人学习与生活助手技术方案 v2

## 目标与技术边界

本技术方案基于 `requirements/202606111104_java-assistant-requirement/req_v4.md` 和 `designs-oo/202606111206_java-ai-assistant-ood/design_v2.md`，将架构级 OOD 落到 Java 项目的可编码技术路径。实现目标是一个单用户、本地运行、命令行交互的 Java AI 学习与生活助手，覆盖 8 个核心功能，并为实验 1 白盒测试、JUnit 单元测试、覆盖证据和回归测试提供稳定技术基础。

本版不实现账号、多用户、数据库、文件导出、真实系统通知、健康管理、联系人管理和用户数据持久化。AI 调用按 DeepSeek OpenAI 兼容接口接入，真实联网验证只放入组件或集成测试，普通单元测试必须使用 fake、stub 或 mock 隔离外部依赖。

## 技术栈决策

| 技术事项 | 决策 |
|---------|------|
| Java 版本 | 使用 Java 17 LTS。核心理由是 `java.time`、`java.net.http.HttpClient`、record、不可变集合等能力足够覆盖本项目，同时课程环境较容易安装。 |
| 构建工具 | 使用 Maven 单模块工程。采用 Maven 标准目录布局，便于教师按统一命令运行源码和测试。 |
| 应用形态 | 命令行交互程序。控制台层只处理菜单、输入解析和展示，业务逻辑全部下沉到服务、领域对象和值对象。 |
| HTTP 客户端 | 使用 JDK `java.net.http.HttpClient`，不引入额外 HTTP 框架。它支持构建客户端、配置连接超时、同步和异步发送请求，满足 DeepSeek 调用需求。 |
| JSON 处理 | 使用 Jackson Databind `com.fasterxml.jackson.core:jackson-databind:2.19.0`。用于 DeepSeek 请求响应 JSON 序列化、结构化建议解析和测试断言。 |
| 测试框架 | 使用 JUnit Jupiter 5.14.4，保持需求文档中的 JUnit 5 口径。 |
| Mock 工具 | 使用 Mockito 5.18.0。业务服务优先使用手写 fake，只有验证协作者调用或异常传播时使用 Mockito。 |
| 覆盖率工具 | 使用 JaCoCo Maven Plugin 0.8.13 生成覆盖率证据。 |
| 测试运行 | 使用 Maven Surefire 3.5.6 运行单元测试；Maven Failsafe 3.5.6 运行集成测试。两个插件版本均固定为 Maven Central 已发布版本，避免课程环境构建时解析失败；集成测试使用 Maven profile 单独开启，避免默认测试依赖网络和 API Key。 |

不使用 Spring Boot、数据库 ORM、Lombok、复杂 CLI 框架或 OpenAI Java SDK。项目规模较小，直接使用 JDK、Jackson、JUnit、Mockito 和 JaCoCo 可以降低技术噪声，使测试重点集中在业务分支、状态流转、统计计算和 AI 失败处理。

## 工程目录与构建方案

源码实现时在当前仓库下新增独立 Maven 工程目录 `java-ai-assistant/`，设计文档、需求文档和实验资料继续保留在现有目录中。工程目录采用以下结构：

```text
java-ai-assistant/
  pom.xml
  README.md
  src/main/java/assistant/
    app/
    common/
    task/
    schedule/
    study/
    finance/
    note/
    summary/
    ai/
    testability/
  src/main/resources/
  src/test/java/assistant/
  src/test/resources/
  docs/
    environment.md
    test-plan.md
    test-cases.md
    defect-regression.md
    coverage/
```

`pom.xml` 固定 Java release 为 17。依赖只放置 Jackson、JUnit Jupiter、Mockito，Maven 插件配置 Surefire、Failsafe、JaCoCo 和可选的 Exec 插件。测试相关插件版本固定如下：

| 插件 | 版本 | 用途 |
|------|------|------|
| `maven-surefire-plugin` | `3.5.6` | 默认运行 JUnit Jupiter 单元测试。 |
| `maven-failsafe-plugin` | `3.5.6` | 在 `integration` profile 中运行 `*IT` 集成测试。 |
| `jacoco-maven-plugin` | `0.8.13` | 生成白盒覆盖率报告。 |

Surefire 与 Failsafe 不使用 `3.6.0`，因为 Maven Central 当前无该正式版本；本方案统一采用已发布的 `3.5.6`，降低课程环境中 `mvn clean test` 和 `mvn -Pintegration verify` 的插件解析风险。普通命令为：

| 命令 | 用途 |
|------|------|
| `mvn clean test` | 编译并运行全部单元测试，不访问真实 DeepSeek。 |
| `mvn clean verify` | 运行单元测试并完成默认验证。 |
| `mvn jacoco:report` | 在测试后生成 `target/site/jacoco/index.html` 覆盖率报告。 |
| `mvn -Pintegration verify` | 运行需要网络或 API Key 的集成测试。默认不启用。 |

生产代码不依赖测试工具包。`assistant.testability` 中只放可被生产和测试共用的简单抽象及基础实现，例如 `TimeProvider`、`IdGenerator`、固定时间实现和测试数据工厂；JUnit 专用 fake、stub 和断言辅助放在 `src/test/java`。

## 包与模块落地

包结构直接承接 OOD 的业务分包。每个业务包内部按“领域对象、查询条件、服务、仓储接口、内存仓储实现”组织，不再额外拆 Maven 子模块。

| 包 | 技术职责 |
|----|----------|
| `assistant.app` | `Main`、控制台菜单、输入解析、结果展示、应用装配。不得直接访问集合或 HTTP。 |
| `assistant.common` | `EntityId`、`OperationResult`、`ErrorCode`、`BusinessException`、`DateRange`、`DateTimeRange`、金额、进度、标签等通用值对象。 |
| `assistant.task` | 任务实体、优先级和状态枚举、任务查询、任务服务、任务仓储和内存实现。 |
| `assistant.schedule` | 日程实体、状态推导、冲突策略、日程服务、日程仓储和内存实现。 |
| `assistant.study` | 学习计划实体、进度值对象、计划状态分析、计划服务、计划仓储和内存实现。 |
| `assistant.finance` | 收支记录、收支类型、查询条件、收支服务、统计服务、统计结果和内存仓储。 |
| `assistant.note` | 笔记实体、标签、查询条件、搜索策略、笔记服务和内存仓储。 |
| `assistant.summary` | 今日摘要、本周学习统计、本月收支统计、笔记标签分布、AI 本地上下文生成。 |
| `assistant.ai` | AI 配置、DeepSeek 客户端、请求响应 DTO、提示词构造、AI 问答服务、结构化建议解析、草稿生命周期和导入服务。 |
| `assistant.testability` | 可替换时间、编号、演示数据装配和测试友好装配入口。 |

领域实体使用普通 Java class，以便封装状态变化；不可变值对象和只读 DTO 可使用 Java record。实体集合对外返回不可修改快照，避免控制台层或汇总层绕过服务修改内部状态。

## 核心基础类型技术方案

`EntityId` 使用正整数语义，底层可用 `long`。所有可修改记录都必须持有 `EntityId`，包括任务、日程、学习计划、收支、笔记和 AI 草稿。默认编号由 `IncrementalIdGenerator` 生成，测试使用固定序列生成器保证断言稳定。

`TimeProvider` 返回当前 `LocalDate` 和 `LocalDateTime`。生产实现读取系统时间，测试实现固定在指定日期时间。日程状态、学习计划逾期、本周统计、本月统计和今日摘要都必须通过该接口读取时间，不直接调用 `LocalDate.now()` 或 `LocalDateTime.now()`。

`DateRange` 使用左右闭区间语义，适用于收支日期筛选、学习计划周期、本周和本月统计。开始日期晚于结束日期直接作为输入校验错误。

`DateTimeRange` 使用左闭右开区间语义，适用于日程。当前时间等于开始时间视为进行中，等于结束时间视为已过期；两个日程首尾相接不冲突，存在非空重叠才冲突。

金额使用两个值对象分离语义：

| 类型 | 用途 | 约束 |
|------|------|------|
| `TransactionAmount` | 单笔收入或支出金额 | 使用 `BigDecimal`，必须大于 0，最多两位小数。 |
| `MoneyValue` | 统计金额 | 使用 `BigDecimal`，允许 0 和负数，统一按两位小数展示。 |

进度使用 `Progress` 值对象，范围为 0 到 100。标签使用 `Tag` 值对象，去除首尾空白，空标签拒绝；是否大小写归一统一在 `Tag` 中决定，服务层不重复处理。

`OperationResult` 是应用服务面向控制台层和测试层的统一返回语义。成功结果包含必要载荷，失败结果包含 `ErrorCode` 和简短消息。领域值对象可以抛出 `BusinessException`，但应用服务边界负责捕获并转换成 `OperationResult`，使 JUnit 能稳定断言错误分类和状态不变。

## 内存数据与仓储方案

本版需求已删除数据持久化与导出，因此用户业务数据只保存在进程内存中。各仓储接口按业务语义设计，不使用一个过宽的通用仓储强行覆盖所有场景。默认内存实现使用 `LinkedHashMap<EntityId, T>`：

- `LinkedHashMap` 保持插入顺序，便于控制台展示和测试断言稳定。
- 保存、查找、删除按 `EntityId` 执行，避免同名记录歧义。
- 列表查询返回不可修改快照，防止外部修改内部集合。
- 删除不存在记录由服务层转换为 `NOT_FOUND` 错误。
- 每个 JUnit 测试重新创建仓储实例，保证测试隔离。

AI 草稿也使用独立内存仓储 `SuggestionDraftRepository`。它是运行期状态持有边界，不属于用户数据持久化。后续如果自愿扩展文件持久化，只新增仓储实现和组件测试，不改变服务契约，也不纳入本版 8 个核心功能验收。

## DeepSeek API 接入方案

### 接口与配置

真实 AI 客户端实现为 `DeepSeekAiClient`，只负责协议、鉴权、超时、HTTP 状态和响应解析，不理解任务、计划、笔记等业务规则。业务层只依赖 `AiClient` 接口。

按需求和 DeepSeek 官方文档，默认配置为：

| 配置项 | 默认或来源 |
|--------|------------|
| base URL | `https://api.deepseek.com` |
| path | `/chat/completions` |
| model | `deepseek-v4-flash` |
| API Key | 环境变量 `DEEPSEEK_API_KEY` 或 JVM 参数注入，不写入源码、测试数据和文档示例明文。 |
| timeout | 默认连接超时和请求超时 20 秒，可通过配置覆盖。 |
| stream | 默认 `false`，命令行应用先采用非流式响应，降低交互和测试复杂度。 |

可选配置名固定为：

| 配置名 | 用途 |
|--------|------|
| `DEEPSEEK_API_KEY` | API Key。缺失时 AI 服务返回 `AI_NOT_CONFIGURED`。 |
| `DEEPSEEK_BASE_URL` | 覆盖 base URL，测试或代理环境可用。默认仍是官方地址。 |
| `DEEPSEEK_MODEL` | 覆盖模型名。默认仍是 `deepseek-v4-flash`。 |
| `DEEPSEEK_TIMEOUT_SECONDS` | 覆盖超时时间。 |

配置加载由 `AiConfigurationLoader` 完成。生产入口从环境变量和 JVM 参数读取；单元测试直接构造 `AiConfiguration`，不读取真实环境。

### 请求与响应数据流

AI 问答数据流为：

1. 控制台层收集用户问题和场景类型。
2. `AiAssistantService` 通过 `ContextProvider` 获取 `LocalContext`。
3. `PromptBuilder` 将场景、用户问题和本地摘要组装为 messages。
4. `AiClient` 接收内部请求对象，并由 `DeepSeekAiClient` 转换为 JSON。
5. `DeepSeekAiClient` 使用 JDK `HttpClient` POST 到 `/chat/completions`。
6. Jackson 解析响应 JSON，提取 `choices[0].message.content`。
7. `AiErrorMapper` 将异常、HTTP 状态和空响应转换为稳定错误分类。

请求头固定包含 `Content-Type: application/json` 和 `Authorization: Bearer {API_KEY}`。请求体包含 `model`、`messages` 和 `stream:false`。本项目不启用流式输出、不启用工具调用、不实现复杂 agent 编排。

### AI 错误映射

AI 失败属于常见外部状态，不允许导致程序崩溃或污染本地业务数据。错误映射策略如下：

| 场景 | 错误分类 |
|------|----------|
| API Key 缺失 | `AI_NOT_CONFIGURED` |
| 401 或 403 | `AI_AUTH_FAILED` |
| 429 | `AI_RATE_LIMITED` |
| JDK 超时异常、408 或 504 | `AI_TIMEOUT` |
| 400 或 422 | `AI_BAD_REQUEST` |
| 5xx | `AI_REMOTE_UNAVAILABLE` |
| 网络 I/O 异常 | `AI_NETWORK_ERROR` |
| HTTP 2xx 但内容为空 | `AI_EMPTY_RESPONSE` |
| JSON 结构不符合预期 | `AI_MALFORMED_RESPONSE` |

如果捕获 `InterruptedException`，客户端必须恢复线程中断标记并返回外部依赖失败分类。控制台层只展示简短错误说明，本地任务、日程、学习计划、收支和笔记服务继续可用。

## AI 结构化建议方案

结构化建议采用“生成草稿、持有草稿、确认或取消、导入正式记录”的固定流程。AI 原始输出不得直接写入任务或学习计划仓储。

### 输出约束与解析

`PromptBuilder` 对结构化场景要求模型返回单个 JSON 对象，业务类型只允许：

| 类型 | 目标 |
|------|------|
| `TASK_DRAFT` | 一个或多个待办任务草稿。 |
| `STUDY_PLAN_DRAFT` | 一个学习计划草稿，必要时附带拆解说明。 |

`StructuredSuggestionParser` 使用 Jackson `JsonNode` 解析模型返回。技术处理策略为：

- 优先解析完整 JSON 文本。
- 如果模型返回单个 fenced JSON 代码块，可去除围栏后再解析。
- 根节点、类型字段、必要业务字段缺失或类型错误时，返回 `AI_MALFORMED_RESPONSE`，不创建草稿。
- 不引入 JSON Schema 依赖，采用代码内必填字段和枚举校验，便于白盒测试覆盖每个错误分支。

解析成功后只生成受控的 `SuggestionDraft`，状态为 `CONFIRMABLE`。草稿内容使用内部草稿 DTO 表达，不复用正式实体，避免绕过正式服务校验。

### 草稿生命周期

`DraftLifecycleService` 负责创建、查询、取消和确认草稿。状态固定为：

| 状态 | 含义 |
|------|------|
| `CONFIRMABLE` | 可确认或取消。 |
| `CANCELLED` | 用户已取消，不能再导入。 |
| `IMPORTED` | 已成功导入正式数据，不能重复导入。 |

取消草稿只改变草稿状态，不修改正式业务数据。确认草稿时，生命周期服务先检查草稿存在且状态为 `CONFIRMABLE`，再委托 `DraftImportService`。

### 导入与回滚

`DraftImportService` 根据草稿类型调用 `TaskService` 或 `StudyPlanService`。目标服务仍执行完整输入校验，AI 草稿不能绕过标题、日期、优先级、进度等业务规则。

批量任务草稿采用“先校验全部，再写入全部”的方案。若任何草稿项校验失败，正式仓储不写入任何记录，草稿保持 `CONFIRMABLE`，返回目标服务的错误分类。若导入过程中出现不可预期异常，导入服务删除本次已创建的记录并返回系统错误，保证正式数据不半写入。导入成功后草稿状态改为 `IMPORTED`；重复确认返回 `STATE_CONFLICT`，不得重复新增正式记录。

## 各业务功能技术路径

### 任务待办

任务实体持有标题、描述、优先级、截止日期、完成状态和编号。优先级、状态使用 enum。新增和修改任务时由服务集中校验标题非空、优先级合法、日期合法；标记完成和撤销完成先查找记录，再检查状态迁移。

重复完成、重复撤销统一返回 `STATE_CONFLICT`，任务状态不变。查询条件由 `TaskQuery` 表达，支持按状态、优先级和截止日期筛选。汇总服务只读调用任务查询，不接收任务仓储引用。

### 日程提醒

日程实体持有名称、`DateTimeRange`、地点和备注。创建日程时结束时间必须晚于开始时间。冲突判断由 `ScheduleConflictPolicy` 处理，同一天存在非空时间重叠则拒绝保存并返回 `SCHEDULE_CONFLICT`，首尾相接不冲突。

日程状态不持久化，由 `ScheduleService` 基于 `TimeProvider` 动态推导：早于开始为即将开始，大于等于开始且小于结束为进行中，大于等于结束为已过期。按日期查询返回该日期开始或覆盖该日期的日程快照。

### 学习计划

学习计划实体持有目标名称、开始日期、截止日期、预期投入小时数和进度。进度通过 `Progress` 值对象保证范围 0 到 100。开始日期晚于截止日期时拒绝创建。

计划状态由 `StudyPlanAnalysisService` 统一推导：未开始、进行中、已完成、逾期未完成。进度达到 100 视为已完成，优先级高于日期状态。完成数量统计和本周统计都通过该分析组件计算，避免多个服务产生不一致状态。

### 收支记录

收支记录持有类型、`TransactionAmount`、类别、日期和备注。金额从字符串或 `BigDecimal` 构造，拒绝 0、负数和超过两位小数的输入。收入与支出使用 `TransactionType` enum。

查询条件由 `TransactionQuery` 表达，组合支持类型、类别和 `DateRange`。`FinanceStatisticsService` 从查询结果计算收入总额、支出总额和结余，空集合返回三个零值；只有支出无收入时结余允许为负。统计计算使用 `BigDecimal.add`，禁止使用 `double`。

### 笔记管理

笔记实体持有标题、内容、创建日期和标签集合。标题和内容不能为空。标签通过 `Tag` 值对象统一清理和校验。

关键字查询由 `NoteSearchPolicy` 处理。关键字为空属于输入错误，返回 `VALIDATION_ERROR`；无匹配返回空集合，不作为错误。关键字匹配标题和内容，标签查询按 `Tag` 的统一语义比较。AI 摘要由 AI 服务编排，笔记服务只提供上下文，不直接调用外部 API。

### 汇总统计

`SummaryService` 每次即时读取任务、日程、学习计划、收支和笔记服务，不维护冗余缓存。今日摘要基于 `TimeProvider.today()`；本周统计采用 ISO 周语义，周一到周日；本月收支统计采用当前月第一天到最后一天。

`LocalContext` 是 AI 提示词输入的摘要对象，不暴露领域实体内部可变结构。空数据、单模块数据和多模块组合数据都有稳定表达，便于单元测试断言提示词包含必要上下文。

## 配置管理方案

项目不引入 Spring 配置体系。配置分三层：

1. 生产启动层从环境变量和 JVM 系统属性读取。
2. 应用装配层将配置转换为 `AiConfiguration`、`TimeProvider`、`IdGenerator` 和服务实例。
3. 单元测试直接构造配置对象和 fake 依赖，不读取真实环境。

DeepSeek API Key 不得写入源码、`pom.xml`、JUnit 测试、README 示例明文或提交材料。`README.md` 和 `docs/environment.md` 只说明变量名和配置方式。若 API Key 未配置，AI 菜单可以进入，但调用返回明确配置错误；本地功能不受影响。

示例数据由 `DemoDataFactory` 在启动时可选装配，不从真实用户文件读取。这样演示数据可重复，单元测试也不依赖外部路径。

## 错误处理方案

错误分类采用 `ErrorCode` enum，至少覆盖：

| 分类 | 典型场景 |
|------|----------|
| `VALIDATION_ERROR` | 字段为空、格式非法、金额非法、进度越界、日期范围非法。 |
| `NOT_FOUND` | 修改、删除或确认不存在的记录或草稿。 |
| `STATE_CONFLICT` | 重复完成、重复撤销、重复确认、取消后确认、导入后确认。 |
| `SCHEDULE_CONFLICT` | 日程时间段存在非空重叠。 |
| `AI_NOT_CONFIGURED` | DeepSeek API Key 未配置。 |
| `AI_AUTH_FAILED` | DeepSeek 鉴权失败。 |
| `AI_RATE_LIMITED` | DeepSeek 限流。 |
| `AI_TIMEOUT` | AI 请求超时。 |
| `AI_REMOTE_UNAVAILABLE` | DeepSeek 服务端错误或不可用。 |
| `AI_EMPTY_RESPONSE` | AI 返回空内容。 |
| `AI_MALFORMED_RESPONSE` | AI 返回 JSON 或业务结构异常。 |
| `SYSTEM_ERROR` | 未预期运行时异常兜底。 |

领域和值对象可以抛出 `BusinessException` 表达非法状态；应用服务边界统一转换为 `OperationResult`。控制台层不得直接捕获底层 HTTP、Jackson 或集合异常来判断业务错误。单元测试主要断言 `ErrorCode`、返回载荷和仓储状态不变，不断言完整控制台文本。

## 测试技术路线

### 单元测试

单元测试位于 `src/test/java`，使用 JUnit Jupiter。每个测试通过 `@BeforeEach` 或测试工厂重新创建内存仓储、固定时间、固定编号和 fake AI 客户端。普通单元测试不读取真实环境变量、不访问网络、不依赖真实日期。

重点测试对象包括：

| 对象 | 覆盖重点 |
|------|----------|
| `FinanceStatisticsService` | 空集合、收入支出分类、日期边界、负结余、金额精度。 |
| `ScheduleConflictPolicy` 和 `ScheduleService` | 首尾相接、非空重叠、状态边界、冲突拒绝保存。 |
| `StudyPlanAnalysisService` | 0、100、越界、未开始、进行中、完成、逾期。 |
| `TaskService` | 创建、修改、删除、重复完成、撤销、筛选和状态不变。 |
| `NoteSearchPolicy` 和 `NoteService` | 空关键字、无匹配、多字段匹配、标签查询。 |
| `SummaryService` | 无数据、单模块、多模块、数据变化后即时统计。 |
| `PromptBuilder` 和 `AiAssistantService` | 请求参数、模型名、本地上下文、空响应、超时、鉴权、限流。 |
| `StructuredSuggestionParser`、`DraftLifecycleService`、`DraftImportService` | 草稿字段缺失、取消、确认、重复确认、导入失败回滚。 |

Mock 使用原则：值对象和领域对象不 mock；外部接口如 `AiClient`、`ContextProvider` 可用 fake 或 Mockito；需要验证调用次数时才用 Mockito `verify`。

### 集成测试

真实 DeepSeek 调用放入 `src/test/java` 的 `*IT` 类或单独 profile，使用 `@Tag("integration")` 标记，默认 Surefire 排除。只有执行 `mvn -Pintegration verify` 且存在 `DEEPSEEK_API_KEY` 时才运行。集成测试只断言 HTTP 调用成功、模型配置正确、返回内容非空和错误映射可用，不断言自然语言完整文本。

若实现者后续自愿增加文件仓储或配置文件读取，相应测试也归入组件或集成测试，不混入默认单元测试。

### 覆盖率证据

JaCoCo 报告输出到 `target/site/jacoco/index.html`，并将关键截图或摘要复制到实验报告资料目录。覆盖率重点放在 `assistant.task`、`assistant.schedule`、`assistant.study`、`assistant.finance`、`assistant.note`、`assistant.summary` 和 `assistant.ai` 的服务、策略和值对象。控制台层不作为覆盖率硬指标核心。

建议覆盖目标为核心业务包行覆盖率不低于 80%，分支覆盖率不低于 70%。实验报告仍需补充方法到用例、独立路径到用例、关键分支到用例的映射表，因为覆盖率数字不能替代白盒测试设计说明。

## 交付物组织

最终交付物按以下方式组织：

| 交付物 | 路径 |
|--------|------|
| Java 源码 | `java-ai-assistant/src/main/java` |
| 单元测试脚本 | `java-ai-assistant/src/test/java` |
| 构建配置 | `java-ai-assistant/pom.xml` |
| 运行说明 | `java-ai-assistant/README.md` 和 `java-ai-assistant/docs/environment.md` |
| 测试计划 | `java-ai-assistant/docs/test-plan.md` |
| 测试用例表 | `java-ai-assistant/docs/test-cases.md` |
| 缺陷与回归记录 | `java-ai-assistant/docs/defect-regression.md` |
| 覆盖率证据 | `java-ai-assistant/docs/coverage/` 或实验报告附件 |
| 实验报告 | 按课程模板生成，引用源码、测试、覆盖率和缺陷回归材料。 |

提交前必须保证 `mvn clean test` 在无 API Key、无网络的环境下可重复运行。`README.md` 需要明确真实 AI 集成测试的额外条件：网络可用、配置 `DEEPSEEK_API_KEY`、执行集成测试 profile。

## 实施顺序

1. 建立 Maven 工程、基础包、`pom.xml`、JUnit 和 JaCoCo 配置。
2. 实现 `assistant.common` 基础值对象、错误分类、结果对象、时间和编号抽象。
3. 依次实现任务、日程、学习计划、收支、笔记的领域对象、内存仓储和服务。
4. 实现汇总服务和本地上下文生成，补齐跨模块统计。
5. 实现 AI 配置、提示词构造、`AiClient` 接口、fake client 和 AI 服务单元测试。
6. 实现 `DeepSeekAiClient`，并将真实 API 测试放入集成测试 profile。
7. 实现结构化建议解析、草稿生命周期和导入服务。
8. 实现控制台菜单和应用装配。
9. 完成白盒测试用例、覆盖率报告、缺陷修复记录和回归测试记录。

该顺序先稳定可测试业务核心，再接入控制台和真实外部 API，可避免界面或网络问题阻塞单元测试。

## 已核验技术事实

设计过程中核验了以下技术事实：

- DeepSeek 官方文档说明 API 支持 OpenAI 兼容格式，OpenAI base URL 为 `https://api.deepseek.com`，模型列表包含 `deepseek-v4-flash`，Chat API 示例使用 `/chat/completions`、Bearer API Key、`messages` 和非流式请求。
- Oracle Java 17 API 文档确认 `java.net.http.HttpClient` 可构建客户端、设置连接超时，并支持 `send` 和 `sendAsync`。
- Maven 官方文档确认标准目录包括 `src/main/java`、`src/main/resources`、`src/test/java`、`src/test/resources`、`pom.xml` 和 `target`。
- Maven Surefire 官方文档确认 JUnit Platform 运行方式，并说明 Jupiter 测试需要相应 TestEngine。
- Maven Central 元数据确认 `maven-surefire-plugin` 和 `maven-failsafe-plugin` 均存在已发布版本 `3.5.6`，且没有正式 `3.6.0` 版本；因此本方案固定使用 `3.5.6`。
- Jackson Databind 官方仓库说明其提供通用数据绑定能力，`ObjectMapper` 可读写 JSON。
- Mockito 官方网站说明其是面向 Java 单元测试的 mock 框架，支持 Maven 依赖和 mock、verify、when 等测试能力。
- JaCoCo Maven 插件官方文档说明插件可为测试提供 runtime agent 并生成覆盖率报告。

## 修订说明（v2）

| 审查意见 | 修改措施 |
|---------|---------|
| Maven Surefire `3.6.0` 在 Maven Central 当前元数据中不存在，若实现者照此配置会导致 `mvn clean test` 插件解析失败；Failsafe 若沿用同版本也有同样风险。 | 将 Surefire 与 Failsafe 插件版本统一固定为 Maven Central 已发布的 `3.5.6`，并在技术栈决策、工程构建方案和已核验技术事实中明确不采用正式版本不存在的 `3.6.0`。 |
