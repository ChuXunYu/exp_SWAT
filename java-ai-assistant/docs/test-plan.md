# 实验 1 测试计划

## 测试目标

- 验证 8 个核心功能的业务正确性，包括正常创建、查询、修改、删除、统计和汇总。
- 验证异常输入处理，包括空标题、非法日期范围、非法金额、非法进度、无效草稿、生成任务草稿缺失截止日期、无效 AI 响应和缺失配置。
- 通过语句覆盖、判定覆盖、条件覆盖和基本路径测试覆盖服务层主要分支。
- 验证任务、草稿、日程、学习计划等对象的状态迁移，确保重复确认、取消、完成、撤销等非法迁移被拒绝。
- 验证跨模块同步，例如任务、日程、学习计划、收支和笔记变化后仪表盘摘要与 AI 本地上下文同步更新；摘要需覆盖今日任务、逾期未完成任务、未来 7 天高优先级任务，以及控制台生成的 AI 草稿能被列表、查看、确认和取消入口共享访问。
- 验证中文控制台枚举输入与展示，包括任务优先级和状态、日程状态、学习计划状态、收支类型、AI 草稿类型和草稿状态；同时保留英文枚举输入兼容。
- 隔离 AI 外部依赖，普通单元测试不访问真实 DeepSeek、真实网络、真实 API Key、真实用户文件或真实当前时间。
- 记录缺陷发现、修复方式、复现用例和回归验证结论。

## 测试范围

测试覆盖 8 个核心功能：

1. AI 问答与学习生活建议。
2. AI 结构化建议确认导入。
3. 任务待办管理。
4. 日程提醒管理。
5. 学习计划管理。
6. 收支记录管理。
7. 个人笔记或日记管理。
8. 数据查询与汇总统计。

不覆盖健康管理、联系人管理、真实数据持久化、文件导出和真实 DeepSeek 网络连通性。控制台层只作为交互入口补充测试，用于验证输入解析、菜单循环、中文枚举提示/错误提示和中文展示，不作为白盒覆盖核心。

## 测试环境

| 项 | 版本或说明 |
|----|------------|
| Java | 17 |
| Maven | 以开发机 Maven 命令为准 |
| JUnit Jupiter | 5.14.4 |
| Mockito | 5.18.0 |
| Maven Surefire Plugin | 3.5.6 |
| Maven Failsafe Plugin | 3.5.6 |
| JaCoCo Maven Plugin | 0.8.13 |
| 操作系统 | 开发机环境不限，命令以 Maven 为准 |

## 测试工具

- JUnit Jupiter：编写和执行服务层、值对象、策略、AI 模块和控制台交互单元测试。
- Mockito：替换控制台和部分依赖协作者，验证外部服务调用边界。
- Maven Surefire：执行默认单元测试，匹配普通 `*Test.java`。
- Maven Failsafe：作为可选集成测试入口，匹配 `*IT.java`。
- JaCoCo：收集测试覆盖证据并生成 HTML 报告。

## 测试分层

| 层级 | 范围 | 外部依赖边界 |
|------|------|--------------|
| 单元测试 | 服务、仓储、值对象、查询条件、AI 请求构造和错误映射 | 使用内存仓储、固定时间、fake/mock AI 依赖 |
| 控制台交互单元测试 | `ConsoleApplicationTest` 覆盖菜单输入解析、中文枚举提示/展示、错误分支和循环 | 使用内存服务或 mock 服务，不访问真实终端外部资源 |
| 可选集成测试 | 未来真实 DeepSeek 连通性或跨进程集成 | 当前仓库无 `*IT.java`；需要网络和 `DEEPSEEK_API_KEY` |

普通单元测试不得访问真实 DeepSeek、网络、API Key、用户文件或真实当前时间。

## 测试策略

- 语句覆盖：每个服务公开方法的成功路径和主要错误路径均由 JUnit 用例触达。
- 判定覆盖：对是否存在记录、是否为空、状态是否合法、日期范围是否合法、AI 响应是否有效等判定分别覆盖真/假结果。
- 条件覆盖：对查询组合、日期边界、状态过滤、类型过滤、AI HTTP 状态族和异常类型分别构造输入。
- 基本路径测试：对 `FinanceStatisticsService.calculate`、`DraftImportService.importDraft`、`SummaryService.getDashboardSummary` 等复杂方法列出独立路径并映射用例。
- 边界值：覆盖进度 0/100/-1/101、金额 0/负数/两位小数、首尾相接日程、空集合统计、空关键字等。
- 等价类：将任务优先级、任务状态、日程状态、学习计划状态、交易类型、笔记标签和 AI 场景划分为有效与无效等价类；控制台枚举输入额外区分中文别名、英文兼容输入和非法输入。
- 错误推测：针对空标题、缺失 dueDate、格式异常 JSON、HTTP 401/429/5xx、超时和不存在 id 构造测试。
- 状态迁移：验证待办完成/撤销、草稿确认/取消/重复确认、学习计划进度更新后的状态变化。
- 场景链路：验证 AI 草稿确认导入影响任务/学习计划，业务数据变化后摘要和 AI 本地上下文同步，尤其是逾期未完成任务和未来 7 天高优先级任务同步进入 prompt。

## 核心功能覆盖计划

| 核心功能 | 主要被测类 | 代表性测试类 | 覆盖重点 |
|----------|------------|--------------|----------|
| AI 问答与学习生活建议。 | `AiAssistantService`, `PromptBuilder`, `DeepSeekAiClient`, `AiErrorMapper`, `AiConfigurationLoader` | `AiAssistantServiceTest`, `PromptBuilderTest`, `DeepSeekAiClientTest`, `AiErrorMapperTest`, `AiConfigurationLoaderTest` | 请求构造、上下文注入、逾期和未来高优先级任务段落、配置默认值、空响应、HTTP/网络/格式异常映射 |
| AI 结构化建议确认导入。 | `StructuredSuggestionDraftService`, `StructuredSuggestionParser`, `DraftLifecycleService`, `DraftImportService`, `SuggestionDraft` | `StructuredSuggestionDraftServiceTest`, `StructuredSuggestionParserTest`, `DraftLifecycleServiceTest`, `DraftImportServiceTest`, `SuggestionDraftTest`, `ConsoleApplicationTest` | 结构化生成、解析、保存前 dueDate 校验、breakdown 保留并确认导入后转正式任务、字段缺失拒绝、确认、取消、重复确认冲突、导入失败回滚 |
| 任务待办管理。 | `TaskService`, `TaskItem`, `TaskQuery`, `TaskStatus` | `TaskServiceTest`, `TaskItemTest`, `TaskQueryTest`, `ConsoleApplicationTest` | 新增、空标题、筛选、完成/重复完成/撤销、删除不存在、摘要同步、控制台中文优先级/状态输入和显示 |
| 日程提醒管理。 | `ScheduleService`, `ScheduleItem`, `ScheduleConflictPolicy`, `ScheduleStatus`, `ScheduleQuery` | `ScheduleServiceTest`, `ScheduleItemTest`, `ScheduleConflictPolicyTest`, `ScheduleStatusTest`, `ConsoleApplicationTest` | 时间范围、冲突识别、首尾相接、日期查询、状态计算、控制台中文状态筛选和显示 |
| 学习计划管理。 | `StudyPlanService`, `StudyPlan`, `Progress`, `StudyPlanAnalysisService`, `StudyPlanQuery` | `StudyPlanServiceTest`, `StudyPlanTest`, `ProgressTest`, `StudyPlanAnalysisServiceTest`, `ConsoleApplicationTest` | 创建、日期范围、进度边界、状态、统计、控制台中文状态筛选和显示 |
| 收支记录管理。 | `FinanceService`, `FinanceStatisticsService`, `TransactionRecord`, `TransactionQuery`, `MoneyValue` | `FinanceServiceTest`, `FinanceStatisticsServiceTest`, `TransactionRecordTest`, `TransactionQueryTest`, `MoneyValueTest`, `ConsoleApplicationTest` | 收入/支出、金额边界、类别/日期筛选、空集合和多笔统计、删除后统计、控制台中文收支类型筛选和显示 |
| 个人笔记或日记管理。 | `NoteService`, `Note`, `NoteQuery`, `NoteSearchPolicy`, `Tag` | `NoteServiceTest`, `NoteTest`, `NoteQueryTest`, `NoteSearchPolicyTest`, `TagTest` | 标题/内容校验、关键字、标签、无匹配、修改/删除不存在 |
| 数据查询与汇总统计。 | `SummaryService`, `DashboardSummary`, `LocalContext` | `SummaryServiceTest`, `DashboardSummaryTest`, `LocalContextTest`, `ConsoleApplicationTest` | 空数据、单模块、多模块组合、本周/本月范围、逾期未完成任务、未来 7 天高优先级任务、笔记标签、AI 本地上下文 |

## 运行命令

以下命令均从 `java-ai-assistant/` 目录执行：

```bash
mvn clean test
mvn clean verify
mvn jacoco:report
mvn -Pintegration verify
```

具体测试数量以当前 Maven/Surefire 输出为准。本轮文档不伪造新的执行时间、覆盖率百分比或集成测试通过结果；`/root/exp_SWAT/acceptance/20260613_full_acceptance.md` 记录 2026-06-13 执行 `mvn clean test` 时 952 个测试通过、失败 0 个。

## 集成测试边界

当前 `src/test/java` 下不存在 `*IT.java` 集成测试类。`mvn -Pintegration verify` 是保留的可选集成测试入口，不代表当前已经验证真实 DeepSeek 连通性。真实 DeepSeek 连通性只作为未来可选集成测试，需要网络环境和 `DEEPSEEK_API_KEY`。

## 通过准则

- 默认单元测试全部通过，失败数为 0。
- 普通单元测试不访问真实外部依赖、真实网络、真实 API Key、用户文件或真实当前时间。
- 覆盖证据可以从 JaCoCo 报告、路径分析和白盒用例映射追溯。
- 每个核心功能至少具备成功路径、失败路径、边界输入和状态变化用例。
- 缺陷修复记录包含复现用例、重跑范围和明确回归结论。

相关文档：[`test-cases.md`](test-cases.md)、[`coverage/README.md`](coverage/README.md)、[`defect-regression.md`](defect-regression.md)。
