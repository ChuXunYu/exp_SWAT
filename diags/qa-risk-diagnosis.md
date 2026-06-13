# QA 风险事实诊断报告

任务描述要求基于 `/root/exp_SWAT` 当前完整项目，对 8 个风险逐条判断真实性、描述精度、影响范围、严重程度，并给出代码、测试和文档证据。本报告仅定位问题，不提供代码修复方案。

## 总体结论

当前项目构建和测试交付状态较完整，验收报告显示 `mvn clean test` 通过 952 个测试、`mvn clean verify` 通过，CLI smoke 通过（`acceptance/20260613_full_acceptance.md:20-26`）。8 个风险中，1、2、5、6、7 为已确认或部分确认的真实风险；3 的现象真实但原描述需修正；4 是可维护性设计权衡；8 更偏产品范围取舍而非缺陷。

最需要优先处理的是 AI 结构化草稿端到端入口缺失，因为它直接影响“AI 生成草稿 -> 保存 -> 用户确认导入”的核心验收链路。其次是学习计划 breakdown 导入丢失、摘要紧急事项覆盖不足、中文 CLI 暴露英文枚举、以及草稿导入依赖手动补偿回滚的未来持久化风险。

## 风险 1：AI 草稿功能还像“后处理器”，不是完整用户流程

- 是否存在：CONFIRMED
- 严重程度：HIGH
- 建议处理方式：立即修复

### 事实诊断

风险真实存在。当前 CLI 主菜单有 `AI 问答` 和 `AI 草稿` 两个入口，但二者没有自然串联：

- `ConsoleApplication.printMainMenu()` 暴露 `7. AI 问答` 和 `8. AI 草稿`（`java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java:74-85`）。
- `dispatch()` 中 `7` 只调用 `askAi()`，`8` 只调用 `runDraftMenu()`（`ConsoleApplication.java:99-108`）。
- `askAi()` 固定以 `AiScenario.GENERAL_QA` 调用 `services.aiAssistantService().ask(...)`，成功后只输出文本回复，没有解析结构化建议、生成 `SuggestionDraft`、保存草稿的动作（`ConsoleApplication.java:1777-1792`）。
- `runDraftMenu()` 只提供列表、查看、确认导入、取消、返回和帮助，不提供“让 AI 生成任务草稿/学习计划草稿”的入口（`ConsoleApplication.java:1808-1839`）。
- 应用装配只创建 `AiAssistantService`、`InMemorySuggestionDraftRepository`、`DraftImportService`、`DraftLifecycleService`，没有可见的结构化建议生成应用服务来串联 AI 调用、解析器、草稿仓储和 ID 生成器（`java-ai-assistant/src/main/java/assistant/app/ApplicationFactory.java:79-89`；`ApplicationServices.java:14-23`）。

验收文档也明确记录该缺口：`acceptance/20260613_full_acceptance.md:40-42` 判定 AI 结构化建议确认导入“部分通过，有明显风险”；`acceptance/20260613_full_acceptance.md:71` 明确写到应用服务和 CLI 未发现“调用 AI 结构化场景 -> 解析响应 -> 保存草稿”的入口，草稿菜单只能管理既有草稿。

### 当前测试覆盖

已有测试覆盖的是“草稿已存在后的管理链路”，不是 AI 生成草稿的端到端链路：

- `ConsoleApplicationTest` 覆盖草稿列表、查看、确认、取消、异常、非法 id、别名等菜单路径（从 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java:1454` 起的一组 draft menu 用例）。
- `StructuredSuggestionParserTest` 覆盖结构化文本解析（`java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionParserTest.java:23-222`）。
- `DraftLifecycleServiceTest` 覆盖 get/list/cancel/confirm/retry/重复确认等状态路径（`java-ai-assistant/src/test/java/assistant/ai/DraftLifecycleServiceTest.java:36-252`）。
- `DraftImportServiceTest` 覆盖导入任务和学习计划（`java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java:42-168`）。

测试缺口是：没有覆盖“AI 调用返回结构化 JSON -> 解析 -> 分配草稿 id -> 保存到仓储 -> CLI 可见 -> 用户确认导入”的完整用户流程。验收报告第 7 节已经将其列为风险项（`acceptance/20260613_full_acceptance.md:71-78`）。

### 描述修正

原描述准确。更精准表述为：当前 AI 草稿模块具备“解析、保存后的生命周期管理、确认导入”能力，但缺少从 AI 结构化响应生成并保存草稿的应用服务和 CLI 入口，因此用户无法从控制台完成完整的 AI 草稿生成流程。

## 风险 2：学习计划草稿的 breakdown 可能被浪费

- 是否存在：CONFIRMED
- 严重程度：MEDIUM
- 建议处理方式：立即修复或纳入下一轮 P1

### 事实诊断

风险真实存在。结构化解析器会解析学习计划草稿的 `breakdown`，展示层也能显示 breakdown，但导入服务导入学习计划时完全没有使用该字段。

证据链：

- `StructuredSuggestionParser.parseStudyPlan()` 从 JSON 的 `breakdown` 字段解析为 `StudyPlanDraftContent`（`java-ai-assistant/src/main/java/assistant/ai/StructuredSuggestionParser.java:122-134`）。
- `parseBreakdown()` 支持字符串数组，并过滤空项（`StructuredSuggestionParser.java:136-152`）。
- `StudyPlanDraftContent` 将 `breakdown` 保存为 `List<String>`，并提供 `hasBreakdown()`（`java-ai-assistant/src/main/java/assistant/ai/StudyPlanDraftContent.java:8-38`）。
- `ConsoleApplication.printStudyPlanDraftContent()` 会展示 `breakdown`（`ConsoleApplication.java:1931-1948`）。
- 但 `DraftImportService.importStudyPlan()` 只调用 `studyPlanService.createStudyPlan(content.goalName(), content.startDate(), content.endDate(), content.expectedHours(), content.initialProgress().value())`，没有读取或转化 `content.breakdown()`（`java-ai-assistant/src/main/java/assistant/ai/DraftImportService.java:77-90`）。

影响范围：确认导入学习计划草稿后，正式 `StudyPlan` 仅保留目标、日期、预期小时、初始进度；AI 生成的拆解步骤不会成为任务、笔记，也不会保存在学习计划模型中。

### 当前测试覆盖

- `StructuredSuggestionParserTest.parsesStudyPlanDraftJson` 验证 breakdown 可被解析并清洗为 `["Basics", "Practice"]`（`StructuredSuggestionParserTest.java:64-92`）。
- `DraftImportServiceTest.importsStudyPlanDraft` 使用带 `List.of("Syntax", "Testing")` breakdown 的 `StudyPlanDraftContent`，但断言只检查学习计划数量、目标名和进度，没有断言 breakdown 被保存或转化（`DraftImportServiceTest.java:124-139`, `184-191`）。

这说明测试已覆盖“breakdown 被解析”和“学习计划可导入”，但没有覆盖“breakdown 导入后应去向何处”。该风险不是测试盲猜，而是当前模型和导入规则确实没有保存位置。

### 描述修正

原描述准确。更精准表述为：breakdown 在草稿层可解析、可展示，但导入阶段没有落地到正式学习计划、任务或笔记中。

## 风险 3：草稿模型和导入规则不一致

- 是否存在：PARTIAL
- 严重程度：MEDIUM
- 建议处理方式：文档说明或立即修复，取决于产品规则

### 事实诊断

风险现象部分存在，但原描述“展示层允许任务没有 dueDate”不够精准。实际情况是：AI 任务草稿模型和解析器允许 `dueDate == null`，草稿展示层显示“未设置”；正式任务模型和导入规则要求 dueDate 非空。

证据链：

- `TaskDraftItem` 的 `dueDate` 字段可为 `null`，构造器没有 `Objects.requireNonNull(dueDate)`，并通过 `hasDueDate()` 判断是否设置（`java-ai-assistant/src/main/java/assistant/ai/TaskDraftItem.java:7-23`）。
- `StructuredSuggestionParser.optionalDate()` 在字段缺失或 JSON null 时返回 `null`（`StructuredSuggestionParser.java:204-213`）。
- `StructuredSuggestionParserTest.parsesTaskDraftDefaults` 明确断言缺省 `dueDate` 被解析为 `null`（`StructuredSuggestionParserTest.java:53-62`）。
- `ConsoleApplication.formatDraftDueDate()` 对无 dueDate 的草稿显示“未设置”（`ConsoleApplication.java:1975-1977`），`ConsoleApplicationTest.draftMenuDisplaysUnsetDueDateForTaskDraft` 覆盖该展示行为（`ConsoleApplicationTest.java:1532` 附近）。
- `DraftImportService.validateTaskDueDates()` 在导入任务草稿前逐项检查 dueDate，缺失即返回 `VALIDATION_ERROR - task draft dueDate must not be null`（`DraftImportService.java:40-56`）。
- `DraftImportServiceTest.rejectsTaskDraftMissingDueDateBeforeCreatingAnyTask` 明确断言缺 dueDate 的草稿导入失败且不会创建任何任务（`DraftImportServiceTest.java:61-78`）。
- 正式任务模型并不允许 dueDate 为空：`TaskItem` 构造器和 `TaskView` 构造器都要求 dueDate 非空（`java-ai-assistant/src/main/java/assistant/task/TaskItem.java:17-29`; `java-ai-assistant/src/main/java/assistant/task/TaskView.java:7-15`）。

影响范围：AI 可以生成并保存“可查看但不可导入”的任务草稿。用户在草稿详情页看到“截止日期：未设置”后，确认导入会失败；当前草稿菜单又没有编辑/补全入口，用户只能取消或保留失败草稿。

### 当前测试覆盖

当前测试实际上固定了这个不一致：

- 解析器测试确认草稿 dueDate 可缺省。
- 控制台测试确认展示“未设置”。
- 导入服务测试确认缺 dueDate 会失败且不会写入任务。

缺口是没有产品级测试声明“缺 dueDate 草稿是否允许被确认导入、补全、跳过或转为无截止任务”。

### 描述修正

应修正为：不是正式任务展示层允许无 dueDate，而是 AI 任务草稿模型/解析器/草稿详情允许无 dueDate；导入任务草稿时又强制所有草稿项必须有 dueDate，导致可确认草稿的可导入性不稳定。

## 风险 4：ConsoleApplication 过于庞大

- 是否存在：DESIGN_TRADEOFF
- 严重程度：MEDIUM
- 建议处理方式：重构规划

### 事实诊断

该风险真实反映维护压力，但不是当前功能缺陷。`ConsoleApplication` 是一个 2056 行的大类，集中了主菜单、子菜单循环、输入解析、校验错误提示、输出格式化、服务调用和跨模块流程。

证据：

- 文件行数：`java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 为 2056 行，测试类 `ConsoleApplicationTest.java` 为 1858 行。
- 同一类中包含：
  - 主菜单和分发（`ConsoleApplication.java:56-115`）
  - 任务菜单、输入解析、枚举解析、输出格式化（`ConsoleApplication.java:135-450`）
  - 日程菜单和解析（`ConsoleApplication.java:452-738`）
  - 学习计划菜单和解析（`ConsoleApplication.java:740-1132`）
  - 收支菜单和解析（`ConsoleApplication.java:1134-1508`）
  - 笔记菜单和解析（`ConsoleApplication.java:1510-1775`）
  - AI 问答、AI 草稿菜单、草稿展示和导入调用（`ConsoleApplication.java:1777-1980`）

同时需要指出：核心业务规则大多仍在服务和实体层，控制台层并没有完全承载业务逻辑。例如任务创建和状态变化由 `TaskService`/`TaskItem` 负责，草稿导入由 `DraftImportService` 负责，摘要由 `SummaryService` 负责。因此这是可维护性和可演进性风险，不是立即导致错误行为的根因。

### 当前测试覆盖

`ConsoleApplicationTest` 覆盖大量菜单交互和错误分支，包括任务、日程、学习计划、收支、笔记、AI 问答和 AI 草稿。测试数量多能降低回归风险，但也说明控制台职责膨胀导致测试类同步膨胀。

### 描述修正

原描述准确。更精准表述为：`ConsoleApplication` 过大导致 UI 层改动成本和回归面偏高，但核心业务仍较好地分布在服务层，因此严重程度不应定为 HIGH。

## 风险 5：摘要页可能漏掉真正紧急的事

- 是否存在：CONFIRMED
- 严重程度：MEDIUM
- 建议处理方式：立即修复或下一轮 P1

### 事实诊断

风险真实存在。当前摘要只查询“今天 dueDate 的任务”，不会突出逾期未完成任务，也不会突出未来 7 天高优先级任务。

证据链：

- `SummaryService.getDashboardSummary()` 对任务只调用 `taskService.listTasks(TaskQuery.byDueDate(today))`（`java-ai-assistant/src/main/java/assistant/summary/SummaryService.java:53-64`）。
- `DashboardSummary` 只有 `todayTasks`，没有 overdueTasks、upcomingHighPriorityTasks 或类似字段（`java-ai-assistant/src/main/java/assistant/summary/DashboardSummary.java:17-31`）。
- `ConsoleApplication.showSummary()` 只输出今日任务数、今日日程数、本周学习计划数、本月收支、笔记和标签数量，不展示逾期或未来高优先级提示（`ConsoleApplication.java:117-133`）。
- README 对 Summary 的功能描述也是 today tasks/today schedules/week plans/month finance/note stats，没有紧急任务维度（`java-ai-assistant/README.md:9-18`）。

影响范围：如果用户有昨天或更早到期且未完成的任务，或者未来 7 天高优先级任务，它们不会在摘要页作为紧急事项出现，只有在任务列表或筛选中才能发现。对“个人学习与生活助手”的今日安排建议和 AI 本地上下文也会产生间接影响，因为 `SummaryService.buildLocalContext()` 依赖同一个 dashboard summary（`SummaryService.java:113-119`）。

### 当前测试覆盖

现有测试覆盖的是当前行为：

- `SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries` 验证任务查询参数正是 `TaskQuery.byDueDate(TODAY)`（`java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java:103-125`）。
- `SummaryServiceTest.buildLocalContextReturnsLocalContextFromSuccessfulSummary` 的 AI 上下文行只包含今日任务行（`SummaryServiceTest.java:247-280`）。
- 文档测试计划也把 `SummaryService.getDashboardSummary` 的重点放在今日任务/日程、本周计划、本月收支、笔记标签和依赖失败（`java-ai-assistant/docs/test-cases.md:106-111`）。

没有测试覆盖“逾期未完成任务必须出现在摘要”或“未来 7 天高优先级任务必须突出”。

### 描述修正

原描述准确。建议将“可能没有突出”改为“当前没有数据结构、查询逻辑和 CLI 输出来突出”。

## 风险 6：中文交互里混用英文枚举

- 是否存在：CONFIRMED
- 严重程度：LOW
- 建议处理方式：立即修复或文档说明

### 事实诊断

风险真实存在，主要影响 CLI 可用性和中文一致性，不影响核心业务正确性。控制台中文提示中大量要求输入英文枚举，输出详情也直接打印 enum 名称。

证据：

- 任务新增/修改/筛选要求 `LOW/MEDIUM/HIGH`、`TODO/COMPLETED`，错误提示也直接显示英文枚举（`ConsoleApplication.java:199-216`, `227-244`, `421-435`）。
- 日程筛选要求 `UPCOMING/ONGOING/EXPIRED`（`ConsoleApplication.java:542-548`, `731-736`）。
- 学习计划筛选要求 `NOT_STARTED/IN_PROGRESS/COMPLETED/OVERDUE_INCOMPLETE`（`ConsoleApplication.java:844-846`, `1125-1130`）。
- 收支修改/筛选要求 `INCOME/EXPENSE`（`ConsoleApplication.java:1270-1278`, `1453-1455`, `1488-1493`）。新增收入/支出已通过菜单 `i/income` 和 `e/expense` 分开，不要求用户手输类型，但详情和列表仍直接输出 `INCOME`/`EXPENSE`（`ConsoleApplication.java:1330-1348`）。
- `ConsoleApplicationTest` 断言了这些英文提示和输出，例如“优先级必须是 LOW、MEDIUM 或 HIGH”、“收支类型必须是 INCOME 或 EXPENSE”、输出“类型: INCOME”（`java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java:491`, `900`, `1025`, `1048`）。

### 当前测试覆盖

测试覆盖的是英文枚举输入和错误提示，而不是中文别名或中文输出。因此当前行为被测试固定，改动时需要同步更新控制台测试。

### 描述修正

原描述基本准确，但“收入支出类型等可能要求用户输入 INCOME/EXPENSE”应限定为收支修改和筛选路径；新增收入/支出路径已通过独立菜单避免手输类型。

## 风险 7：草稿导入缺少真正事务边界

- 是否存在：PARTIAL
- 严重程度：MEDIUM
- 建议处理方式：重构规划；若引入持久化则立即修复

### 事实诊断

当前内存实现下已有补偿式回滚，单元测试覆盖了导入失败后的清理，因此“当前会产生部分导入”不是事实；但“缺少真正事务边界、未来持久化后有部分导入或重复导入风险”成立。

证据：

- `DraftImportService.createTasks()` 循环逐个调用 `taskService.createTask(...)`，记录已创建 id；任一结果失败或运行时异常时调用 `rollbackCreatedTasks(createdIds)`（`java-ai-assistant/src/main/java/assistant/ai/DraftImportService.java:58-75`）。
- `rollbackCreatedTasks()` 逐个调用 `taskService.deleteTask(id)`，并吞掉回滚期间的运行时异常，注释明确这是 best-effort rollback（`DraftImportService.java:92-99`）。
- `DraftLifecycleService.confirmDraft()` 是先 `importService.importDraft(draft)`，导入成功后再 `draft.markImported()` 并保存草稿状态（`java-ai-assistant/src/main/java/assistant/ai/DraftLifecycleService.java:52-72`）。业务数据导入和草稿状态更新不在同一个事务边界内。
- 当前仓储是内存仓储，`ApplicationFactory` 默认装配 `InMemoryTaskRepository`、`InMemoryStudyPlanRepository`、`InMemorySuggestionDraftRepository`（`java-ai-assistant/src/main/java/assistant/app/ApplicationFactory.java:65-88`）。
- README 明确当前数据通过内存服务和仓储保存，进程退出不持久化（`java-ai-assistant/README.md:5-8`）。

影响范围：

- 当前内存版本：失败路径基本受控，已创建任务会被 best-effort 删除；学习计划导入是单个 create，没有批量 partial 问题。
- 未来持久化版本：如果保存任务、删除任务、更新草稿状态跨多个仓储或数据库连接，现有服务层无法保证原子性；若导入成功后 `draft.markImported()` 或 `repository.save(draft)` 失败，可能出现业务数据已写入但草稿仍可确认，导致重复导入风险。

### 当前测试覆盖

- `DraftImportServiceTest.rollsBackCreatedTasksWhenTaskCreationFails` 和 `rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException` 覆盖任务导入失败后的回滚（`java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java:80-122`）。
- `DraftLifecycleServiceTest.confirmDraftKeepsDraftConfirmableWhenImportFails` 覆盖导入失败后草稿保持可确认并可重试（`java-ai-assistant/src/test/java/assistant/ai/DraftLifecycleServiceTest.java:202-222`）。
- 缺陷回归文档把“任务草稿批量导入中途失败后可能留下部分任务”记录为已修复，修复方式是记录已创建 id 并 best-effort 删除（`java-ai-assistant/docs/defect-regression.md:15`）。

缺口是没有测试覆盖“导入成功但草稿状态保存失败”的跨仓储一致性场景，也没有真正事务机制可供测试。

### 描述修正

应修正为：当前内存版本已有补偿式回滚并通过测试，不应描述为当前必然部分导入；但该补偿不是事务边界，未来持久化后仍存在原子性和重复导入风险。

## 风险 8：部分功能有轻微冗余或低耦合

- 是否存在：DESIGN_TRADEOFF
- 严重程度：LOW
- 建议处理方式：暂不处理或文档说明

### 事实诊断

该风险更偏产品范围取舍，不是代码缺陷。当前需求文档明确将 8 个核心功能定位为 AI 问答与建议、AI 结构化建议确认导入、任务、日程、学习计划、收支、笔记、汇总统计（`docs/1 requirement.md:202`）。验收文档也按这 8 个核心功能验收，并判定收支、笔记、汇总统计均通过（`acceptance/20260613_full_acceptance.md:38-47`）。

笔记搜索/筛选和财务模块确实与“AI 学习助手”的核心学习流耦合较低，但它们已成为当前需求范围的一部分：

- 笔记模块支持列表、新增、查看、关键字搜索、标签搜索、组合筛选、修改、删除（`ConsoleApplication.java:1510-1690`）。
- 收支模块支持收入、支出、列表、查看、筛选、修改、删除、统计（`ConsoleApplication.java:1134-1321`）。
- 摘要和 AI 本地上下文会读取收支和笔记数据，例如 `SummaryService` 汇总本月收支和笔记标签（`SummaryService.java:77-110`），`LocalContext` 测试也验证本月收支和标签行进入 AI 上下文（`SummaryServiceTest.java:247-280`）。

因此“冗余或低耦合”不是未实现或错误行为，而是产品边界和课程功能数量之间的取舍。它可能增加控制台和测试规模，但当前没有证据表明这些模块破坏核心功能。

### 当前测试覆盖

收支和笔记测试覆盖较充分：

- 收支服务、统计、查询、仓储、控制台相关测试存在于 `src/test/java/assistant/finance` 和 `ConsoleApplicationTest`。
- 笔记服务、查询、搜索策略、仓储、控制台相关测试存在于 `src/test/java/assistant/note` 和 `ConsoleApplicationTest`。
- 文档测试计划将收支、笔记列入核心测试范围（`java-ai-assistant/docs/test-plan.md:76-82`）。

### 描述修正

原描述应改为产品范围判断：收支和笔记搜索相对 AI 学习助手主线低耦合，但它们是当前需求为了满足课程“8 个核心能力”而保留的正式范围，不宜作为缺陷处理。

## 优先级建议汇总

| 风险 | 是否存在 | 严重程度 | 建议处理 |
|------|----------|----------|----------|
| 1. AI 草稿缺少完整生成入口 | CONFIRMED | HIGH | 立即修复 |
| 2. 学习计划 breakdown 导入丢失 | CONFIRMED | MEDIUM | 立即修复或 P1 |
| 3. 草稿 dueDate 规则不一致 | PARTIAL | MEDIUM | 明确产品规则后修复或文档说明 |
| 4. ConsoleApplication 过大 | DESIGN_TRADEOFF | MEDIUM | 重构规划 |
| 5. 摘要漏紧急事项 | CONFIRMED | MEDIUM | 立即修复或 P1 |
| 6. 中文交互暴露英文枚举 | CONFIRMED | LOW | 可用性修复或文档说明 |
| 7. 缺少真正事务边界 | PARTIAL | MEDIUM | 持久化前纳入重构规划 |
| 8. 低耦合功能范围 | DESIGN_TRADEOFF | LOW | 暂不处理，保持文档说明 |

## 修复者需要重点查看的位置

- AI 结构化草稿端到端入口：`assistant.app.ConsoleApplication`、`assistant.app.ApplicationFactory`、`assistant.app.ApplicationServices`、`assistant.ai.AiAssistantService`、`assistant.ai.StructuredSuggestionParser`、`assistant.ai.SuggestionDraftRepository`
- 学习计划 breakdown 导入：`assistant.ai.DraftImportService.importStudyPlan()`、`assistant.ai.StudyPlanDraftContent`
- 任务草稿 dueDate 规则：`assistant.ai.TaskDraftItem`、`assistant.ai.StructuredSuggestionParser.optionalDate()`、`assistant.ai.DraftImportService.validateTaskDueDates()`、`assistant.task.TaskItem`
- 摘要紧急事项：`assistant.summary.SummaryService.getDashboardSummary()`、`assistant.summary.DashboardSummary`、`assistant.summary.LocalContext`、`ConsoleApplication.showSummary()`
- CLI 枚举交互：`ConsoleApplication` 中 `parseTaskPriority`、`parseTaskStatus`、`parseScheduleStatus`、`parseStudyPlanStatus`、`parseTransactionType`
- 导入一致性边界：`assistant.ai.DraftImportService.createTasks()`、`rollbackCreatedTasks()`、`assistant.ai.DraftLifecycleService.confirmDraft()`
