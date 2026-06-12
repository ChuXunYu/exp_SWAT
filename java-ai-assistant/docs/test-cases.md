# 白盒测试用例

## 用例编号规则

用例编号采用 `{模块前缀}-{两位序号}`：`AI-01`、`DRAFT-01`、`TASK-01`、`SCHEDULE-01`、`STUDY-01`、`FINANCE-01`、`NOTE-01`、`SUMMARY-01`、`CONSOLE-01`。编号稳定用于与覆盖证据和缺陷回归记录互相引用。

## 测试方法说明

本项目白盒测试以服务层、AI 模块、查询条件和值对象为核心。语句覆盖用于触达公开方法主要执行语句；判定覆盖用于验证成功/失败、存在/不存在、状态合法/非法等分支；条件覆盖用于组合状态、日期、类型、标签和 AI 错误条件；基本路径用于分析复杂方法的独立执行路径。边界值覆盖进度、金额、日期和空集合；等价类划分有效/无效输入；错误推测覆盖空值、格式错误、状态冲突和外部依赖失败；状态迁移覆盖任务、草稿、日程和学习计划生命周期；场景链路覆盖模块变化后的摘要和 AI 本地上下文同步。

## 用例总览

### AI 问答与学习生活建议。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| AI-01 | 单元测试 | 基本路径 | `AiAssistantService.ask` | `AiAssistantServiceTest.askSendsBuiltRequestToClientAndReturnsContent` | fake 本地上下文、fake prompt、fake AI 返回内容 | 返回成功响应内容 | 通过 |
| AI-02 | 单元测试 | 判定覆盖 | `AiAssistantService.ask` | `AiAssistantServiceTest.askReturnsNotConfiguredWithoutCallingCollaborators` | AI 配置未启用 | 返回未配置失败且不调用协作者 | 通过 |
| AI-03 | 单元测试 | 边界值 | `PromptBuilder.build` | `PromptBuilderTest.buildReturnsValidationFailureForBlankQuestion` | 空白用户问题 | 返回校验失败 | 通过 |
| AI-04 | 单元测试 | 条件覆盖 | `PromptBuilder.build` | `PromptBuilderTest.buildIncludesOverviewAndAllContextSections` | 本地任务、日程、学习、收支、笔记上下文 | system prompt 包含本地上下文 | 通过 |
| AI-05 | 单元测试 | 等价类 | `AiConfigurationLoader.load` | `AiConfigurationLoaderTest.loadUsesDefaultsWhenMapIsEmpty`, `AiConfigurationLoaderTest.loadUsesProvidedValuesFromMap` | 空配置、base URL、model、timeout 覆盖 | 默认值或覆盖值正确 | 通过 |
| AI-06 | 单元测试 | 错误推测 | `DeepSeekAiClient.chat` | `DeepSeekAiClientTest.chatMapsEmptyResponseShapes`, `DeepSeekAiClientTest.chatMapsMalformedResponseShapes` | 空 choices、缺失 message、非 JSON 响应 | 映射为空响应或格式错误 | 通过 |
| AI-07 | 单元测试 | 条件覆盖 | `DeepSeekAiClient.chat`, `AiErrorMapper` | `DeepSeekAiClientTest.chatMapsHttpStatusFailuresWithoutParsingBody`, `AiErrorMapperTest.mapsExplicitHttpStatuses` | HTTP 401、429、5xx | 映射鉴权、限流、服务器错误 | 通过 |
| AI-08 | 单元测试 | 错误推测 | `DeepSeekAiClient.chat`, `AiErrorMapper` | `DeepSeekAiClientTest.chatMapsTransportExceptions`, `AiErrorMapperTest.mapsExceptions` | 超时、网络异常、运行时异常 | 映射外部依赖失败或系统错误 | 通过 |

### AI 结构化建议确认导入。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| DRAFT-01 | 单元测试 | 基本路径 | `StructuredSuggestionParser.parse` | `StructuredSuggestionParserTest.parsesTaskDraftJson` | JSON 任务草稿含标题、描述、优先级、dueDate | 解析为任务草稿 | 通过 |
| DRAFT-02 | 单元测试 | 基本路径 | `StructuredSuggestionParser.parse` | `StructuredSuggestionParserTest.parsesStudyPlanDraftJson` | JSON 学习计划草稿 | 解析为学习计划草稿 | 通过 |
| DRAFT-03 | 单元测试 | 错误推测 | `StructuredSuggestionParser.parse` | `StructuredSuggestionParserTest.rejectsMalformedInputs` | 缺失字段、格式异常、未知类型 | 返回格式校验失败 | 通过 |
| DRAFT-04 | 单元测试 | 状态迁移 | `DraftLifecycleService.confirmDraft` | `DraftLifecycleServiceTest.confirmDraftImportsAndMarksDraftImported` | 可确认草稿、导入服务成功 | 草稿状态变为 `IMPORTED` | 通过 |
| DRAFT-05 | 单元测试 | 状态迁移 | `DraftLifecycleService.cancelDraft` | `DraftLifecycleServiceTest.cancelDraftMarksDraftCancelledAndSaves` | 可确认草稿 | 草稿状态变为 `CANCELLED`，不导入本地数据 | 通过 |
| DRAFT-06 | 单元测试 | 状态迁移 | `DraftLifecycleService.confirmDraft` | `DraftLifecycleServiceTest.confirmDraftRejectsTerminalDraftsWithoutImporting` | 已导入或已取消草稿重复确认 | 返回状态冲突且不调用导入服务 | 通过 |
| DRAFT-07 | 服务层场景测试 | 基本路径 | `DraftImportService.importDraft` | `DraftImportServiceTest.importsAllTaskDraftItems` | 多个任务草稿均含 dueDate | 批量创建任务成功 | 通过 |
| DRAFT-08 | 服务层场景测试 | 回滚路径 | `DraftImportService.importDraft` | `DraftImportServiceTest.rejectsTaskDraftMissingDueDateBeforeCreatingAnyTask`, `DraftImportServiceTest.rollsBackCreatedTasksWhenTaskCreationFails` | dueDate 缺失或第二个任务创建失败 | 不写入或回滚已写入任务 | 通过 |
| DRAFT-09 | 服务层场景测试 | 基本路径 | `DraftImportService.importDraft` | `DraftImportServiceTest.importsStudyPlanDraft`, `DraftImportServiceTest.propagatesStudyPlanCreationFailure` | 学习计划草稿成功或目标服务校验失败 | 成功导入或传播失败 | 通过 |
| DRAFT-10 | 控制台交互单元测试 | 边界值 | `ConsoleApplication` 草稿菜单 | `ConsoleApplicationTest.draftMenuRejectsInvalidIdBeforeCallingDraftLifecycleService` | 非数字、小数、非正整数、超出 `long` 的草稿 id | 不调用生命周期服务 | 通过 |

### 任务待办管理。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| TASK-01 | 单元测试 | 基本路径 | `TaskService.createTask` | `TaskServiceTest.createTaskStoresTodoTaskAndReturnsTaskView` | 标题、描述、优先级、dueDate 有效 | 创建 `TODO` 任务 | 通过 |
| TASK-02 | 单元测试 | 边界值 | `TaskService.createTask` | `TaskServiceTest.createTaskRejectsBlankTitleAndDoesNotStoreTask` | 标题为空白 | 返回校验失败且仓储不变 | 通过 |
| TASK-03 | 单元测试 | 条件覆盖 | `TaskService.listTasks` | `TaskServiceTest.listTasksWithStatusQueryFiltersTasks`, `TaskServiceTest.listTasksWithPriorityQueryFiltersTasks` | 按状态、优先级查询 | 只返回匹配任务 | 通过 |
| TASK-04 | 单元测试 | 条件覆盖 | `TaskService.listTasks` | `TaskServiceTest.listTasksWithCombinedQueryFiltersTasks` | 状态、优先级、dueDate 组合查询 | 同时满足条件才返回 | 通过 |
| TASK-05 | 单元测试 | 状态迁移 | `TaskService.markTaskCompleted` | `TaskServiceTest.markTaskCompletedChangesTodoTaskToCompleted` | `TODO` 任务 | 状态变为 `COMPLETED` | 通过 |
| TASK-06 | 单元测试 | 状态迁移 | `TaskService.markTaskCompleted`, `TaskService.reopenTask` | `TaskServiceTest.markTaskCompletedRejectsAlreadyCompletedTaskAndKeepsState`, `TaskServiceTest.reopenTaskChangesCompletedTaskToTodo` | 已完成任务重复完成，已完成任务撤销 | 重复完成失败，撤销后为 `TODO` | 通过 |
| TASK-07 | 单元测试 | 错误推测 | `TaskService.deleteTask` | `TaskServiceTest.deleteTaskReturnsNotFoundForMissingTask` | 不存在 id | 返回 `NOT_FOUND` | 通过 |
| TASK-08 | 控制台交互单元测试 | 场景链路 | `ConsoleApplication`, `SummaryService.getDashboardSummary` | `ConsoleApplicationTest.taskMenuAddsTaskAndSummaryReflectsTodayTaskCount`, `ConsoleApplicationTest.taskMenuViewsUpdatesDeletesTask`, `ConsoleApplicationTest.taskMenuCompletesReportsConflictAndReopensTask` | 菜单新增、完成、撤销、删除任务 | 摘要任务数量随状态变化同步 | 通过 |

### 日程提醒管理。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| SCHEDULE-01 | 单元测试 | 基本路径 | `ScheduleService.createSchedule` | `ScheduleServiceTest.createScheduleStoresScheduleAndReturnsScheduleView` | 名称、开始、结束有效 | 创建日程并返回视图 | 通过 |
| SCHEDULE-02 | 单元测试 | 边界值 | `ScheduleService.createSchedule` | `ScheduleServiceTest.createScheduleReturnsValidationErrorForInvalidFieldsAndDoesNotStore` | 名称为空或时间范围非法 | 返回校验失败且不存储 | 通过 |
| SCHEDULE-03 | 单元测试 | 判定覆盖 | `ScheduleConflictPolicy` | `ScheduleServiceTest.createScheduleRejectsOverlappingScheduleAndKeepsRepositoryUnchanged` | 已有日程与新日程重叠 | 拒绝创建 | 通过 |
| SCHEDULE-04 | 单元测试 | 边界值 | `ScheduleConflictPolicy` | `ScheduleServiceTest.createScheduleAllowsTouchingTimeRanges` | 前一日程结束等于后一日程开始 | 不视为冲突 | 通过 |
| SCHEDULE-05 | 单元测试 | 条件覆盖 | `ScheduleService.listSchedulesByDate` | `ScheduleServiceTest.listSchedulesByDateReturnsSchedulesCoveringDate`, `ScheduleServiceTest.listSchedulesByDateIncludesCrossDateSchedule` | 指定日期、跨日程 | 返回覆盖该日期的日程 | 通过 |
| SCHEDULE-06 | 单元测试 | 边界值 | `ScheduleService.listSchedulesByDate` | `ScheduleServiceTest.listSchedulesByDateExcludesExclusiveMidnightEndBoundary` | 结束时间为次日 00:00 | 结束日不包含该日程 | 通过 |
| SCHEDULE-07 | 单元测试 | 状态迁移 | `ScheduleService.listSchedules` | `ScheduleServiceTest.listSchedulesComputesStatusesWithInjectedTimeProvider` | 固定当前时间位于日程前、中、后 | 返回即将开始、进行中、已过期状态 | 通过 |

### 学习计划管理。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| STUDY-01 | 单元测试 | 基本路径 | `StudyPlanService.createStudyPlan` | `StudyPlanServiceTest.createStudyPlanWithoutInitialProgressDefaultsToZeroAndReturnsView` | 有效目标、日期、预期小时，未传进度 | 创建进度 0 的计划 | 通过 |
| STUDY-02 | 单元测试 | 边界值 | `StudyPlanService.createStudyPlan` | `StudyPlanServiceTest.createStudyPlanRejectsInvalidDateRangeAndKeepsRepositoryUnchanged` | 结束日期早于开始日期 | 返回校验失败且仓储不变 | 通过 |
| STUDY-03 | 单元测试 | 边界值 | `StudyPlanService.createStudyPlan` | `StudyPlanServiceTest.createStudyPlanAcceptsExplicitZeroInitialProgress`, `StudyPlanServiceTest.createStudyPlanAcceptsExplicitCompleteInitialProgress` | 初始进度 0 或 100 | 接受并计算正确状态 | 通过 |
| STUDY-04 | 单元测试 | 边界值 | `StudyPlanService.createStudyPlan` | `StudyPlanServiceTest.createStudyPlanRejectsNegativeInitialProgressAndKeepsRepositoryUnchanged`, `StudyPlanServiceTest.createStudyPlanRejectsProgressGreaterThanHundredAndKeepsRepositoryUnchanged` | 初始进度 -1 或 101 | 返回校验失败 | 通过 |
| STUDY-05 | 单元测试 | 状态迁移 | `StudyPlanService.listStudyPlans` | `StudyPlanServiceTest.listStudyPlansComputesStatusesWithInjectedCurrentDate` | 固定当前日期覆盖未开始、进行中、完成、逾期 | 状态计算正确 | 通过 |
| STUDY-06 | 单元测试 | 统计路径 | `StudyPlanAnalysisService.analyze` | `StudyPlanAnalysisServiceTest` 覆盖统计场景 | 多个计划和进度 | 返回计划数量和完成情况统计 | 通过 |

### 收支记录管理。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| FINANCE-01 | 单元测试 | 基本路径 | `FinanceService.recordIncome`, `FinanceService.recordExpense` | `FinanceServiceTest.recordIncomeCreatesRecordAndReturnsView`, `FinanceServiceTest.recordExpenseCreatesRecordAndReturnsView`, `ConsoleApplicationTest.financeMenuAddsIncomeExpenseAndSummaryReflectsBalance` | 收入、支出记录 | 创建交易并保留类型 | 通过 |
| FINANCE-02 | 单元测试 | 边界值 | `FinanceService.addTransaction`, `TransactionAmount` | `FinanceServiceTest.addTransactionRejectsInvalidAmountAndDoesNotStore`, `TransactionAmountTest` | 金额 0、负金额 | 返回校验失败 | 通过 |
| FINANCE-03 | 单元测试 | 边界值 | `MoneyValue`, `TransactionAmount` | `MoneyValueTest`, `TransactionAmountTest` | 两位小数金额 | 金额精度按业务值对象校验 | 通过 |
| FINANCE-04 | 单元测试 | 条件覆盖 | `FinanceService.listTransactions` | `FinanceServiceTest.listTransactionsWithQueryFiltersByTypeCategoryDateRangeAndCombination` | 类型、类别、日期范围查询 | 返回匹配交易 | 通过 |
| FINANCE-05 | 单元测试 | 边界值 | `TransactionQuery.byDateRange` | `TransactionQueryTest` 日期范围场景 | 开始日期晚于结束日期 | 拒绝非法日期范围 | 通过 |
| FINANCE-06 | 单元测试 | 基本路径 | `FinanceStatisticsService.calculate` | `FinanceStatisticsServiceTest.calculateReturnsZeroForEmptyRecords`, `FinanceStatisticsServiceTest.calculateAccumulatesIncomeAndExpenseSeparately`, `FinanceStatisticsServiceTest.calculateAllowsNegativeBalanceWhenExpenseExceedsIncome` | 空集合、多笔收入支出 | 返回 0 统计或分别累计 | 通过 |
| FINANCE-07 | 控制台交互单元测试 | 场景链路 | `FinanceService.deleteTransaction`, `SummaryService.getDashboardSummary` | `ConsoleApplicationTest.financeMenuDeleteRecomputesStatistics` | 删除一笔交易 | 统计重新计算 | 通过 |

### 个人笔记或日记管理。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| NOTE-01 | 单元测试 | 基本路径 | `NoteService.createNote` | `NoteServiceTest.createNoteUsesGeneratedIdAndCurrentDate` | 标题、内容、标签有效 | 创建笔记 | 通过 |
| NOTE-02 | 单元测试 | 边界值 | `NoteService.createNote` | `NoteServiceTest.createNoteReturnsValidationErrorForInvalidInputsAndDoesNotSave` | 标题为空白 | 返回校验失败且不存储 | 通过 |
| NOTE-03 | 单元测试 | 边界值 | `NoteService.createNote` | `NoteServiceTest.createNoteReturnsValidationErrorForInvalidInputsAndDoesNotSave` | 内容为空白 | 返回校验失败且不存储 | 通过 |
| NOTE-04 | 单元测试 | 条件覆盖 | `NoteService.searchByKeyword` | `NoteServiceTest.searchByKeywordReturnsMatchesAndEmptyListWhenNoMatch`, `NoteSearchPolicyTest` | 标题或正文包含关键字 | 返回匹配笔记 | 通过 |
| NOTE-05 | 单元测试 | 边界值 | `NoteService.searchByKeyword` | `NoteServiceTest.searchByKeywordRejectsNullOrBlankKeyword` | 空关键字 | 返回校验失败 | 通过 |
| NOTE-06 | 单元测试 | 条件覆盖 | `NoteService.searchByTag`, `NoteService.listNotes` | `NoteServiceTest.searchByTagUsesTagSemantics`, `NoteServiceTest.listNotesWithQueryFiltersUsingSearchPolicy` | 标签查询 | 返回匹配标签笔记 | 通过 |
| NOTE-07 | 单元测试 | 错误推测 | `NoteService.updateNote`, `NoteService.deleteNote` | `NoteServiceTest.updateNoteReturnsNotFoundForMissingId`, `NoteServiceTest.deleteNoteReturnsNotFoundForMissingId` | 不存在 id | 返回 `NOT_FOUND` | 通过 |
| NOTE-08 | 控制台交互单元测试 | 边界值 | `ConsoleApplication` 笔记菜单 | `ConsoleApplicationTest.noteMenuListsMoreThanTenNotesWithoutTruncation` | 超过 10 条笔记 | 列表不截断 | 通过 |

### 数据查询与汇总统计。

| 编号 | 测试层级 | 测试方法 | 被测类/方法 | JUnit 测试类/方法 | 输入或前置条件 | 预期结果 | 实际结果 |
|------|----------|----------|-------------|-------------------|----------------|----------|----------|
| SUMMARY-01 | 单元测试 | 基本路径 | `SummaryService.getDashboardSummary` | `SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries` | 空内存仓储、固定日期 | 返回空任务、空日程、空计划、0 收支、0 笔记 | 通过 |
| SUMMARY-02 | 单元测试 | 场景链路 | `SummaryService.getDashboardSummary` | `SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries`, `ConsoleApplicationTest.summaryCommandDisplaysDashboardSummary` | 同日任务/日程、本周计划、本月收支、带标签笔记 | 返回多模块组合摘要 | 通过 |
| SUMMARY-03 | 单元测试 | 条件覆盖 | `SummaryService.getDashboardSummary` | `SummaryServiceTest.getDashboardSummaryQueriesServicesWithExpectedDateBoundaries`, `SummaryServiceTest.getDashboardSummaryUsesSingleStableTodaySnapshotForAllDateBoundariesAndQueries` | 固定日期位于周/月中间 | 本周学习、本月收支边界正确 | 通过 |
| SUMMARY-04 | 单元测试 | 条件覆盖 | `SummaryService.getDashboardSummary` | `SummaryServiceTest.getDashboardSummaryAggregatesNoteTagsInFirstSeenOrder` | 多条笔记共享标签 | 标签分布计数正确 | 通过 |
| SUMMARY-05 | 单元测试 | 基本路径 | `SummaryService.buildLocalContext` | `SummaryServiceTest.buildLocalContextReturnsLocalContextFromSuccessfulSummary`, `LocalContextTest.fromBuildsStableOverviewAndEmptyLinesForEmptySummary`, `LocalContextTest.fromBuildsLinesInSourceOrderForMultiModuleData` | 已有仪表盘摘要 | 生成 AI 本地上下文 | 通过 |
| SUMMARY-06 | 单元测试 | 错误推测 | `SummaryService.getDashboardSummary` | `SummaryServiceTest.getDashboardSummaryPropagatesFirstDependencyFailure`, `SummaryServiceTest.getDashboardSummaryUsesStableFallbackWhenDependencyFailureMessageIsBlank` | 依赖服务返回失败 | 摘要返回同一错误码和稳定消息 | 通过 |

## 跨模块场景链路

| 链路 | 覆盖用例 | 预期同步关系 | 实际结果 |
|------|----------|--------------|----------|
| AI 任务草稿确认导入到任务服务 | DRAFT-04, DRAFT-07, TASK-08 | 导入成功后任务列表和摘要可见新任务 | 通过 |
| AI 学习计划草稿确认导入到学习计划服务 | DRAFT-09, STUDY-01, SUMMARY-02 | 导入成功后本周学习计划摘要可见 | 通过 |
| AI 草稿取消不写入本地业务数据 | DRAFT-05 | 取消后任务和学习计划数据不变化 | 通过 |
| AI 草稿导入失败回滚 | DRAFT-08 | 任务批量导入中途失败时已创建任务被删除 | 通过 |
| 任务变化后汇总同步 | TASK-08, SUMMARY-02 | 今日任务数量和完成状态在仪表盘中同步 | 通过 |
| 日程变化后汇总同步 | SCHEDULE-05, SUMMARY-02 | 今日日程在仪表盘中同步 | 通过 |
| 学习计划变化后汇总同步 | STUDY-05, SUMMARY-02 | 本周计划数量和完成/未完成计数同步 | 通过 |
| 收支变化后汇总同步 | FINANCE-07, SUMMARY-02 | 本月收入、支出和结余同步 | 通过 |
| 笔记变化后汇总和 AI 本地上下文同步 | NOTE-06, SUMMARY-04, SUMMARY-05 | 笔记数量、标签分布和本地上下文同步 | 通过 |

## 执行结果摘要

执行结果基于 v28 验证报告：`mvn clean test` 通过 944 个测试，失败 0 个。以上实际结果列统一记录为“通过”。未执行也未记录真实 DeepSeek 网络集成测试结果。
