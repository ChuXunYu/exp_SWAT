# 缺陷修复与回归测试记录

## 记录口径

本记录来自开发迭代中的可测缺陷和 JUnit 回归结果归纳，最终以 v28 `mvn clean test` 通过 944 个测试、失败 0 个为回归基准。普通回归测试使用固定时间、内存仓储和 fake/mock AI 依赖，不访问真实 DeepSeek、真实网络、真实 API Key、真实用户文件或真实当前时间。

## 缺陷记录

| 编号 | 缺陷表现 | 触发输入/前置状态 | 预期结果 | 实际结果 | 可能原因 | 修复方式 | 复现/回归用例 | 重跑范围 | 结论 |
|------|----------|-------------------|----------|----------|----------|----------|----------------|----------|------|
| BUG-01 | DeepSeek HTTP 失败未稳定映射到业务错误 | HTTP 401、429、5xx 或传输异常 | 返回鉴权、限流、外部服务或系统错误 | 修复前边界映射不完整 | 状态码和异常分类分支不足 | 补齐 `AiErrorMapper` 和 `DeepSeekAiClient` 错误映射 | AI-07, AI-08；`DeepSeekAiClientTest.chatMapsHttpStatusFailuresWithoutParsingBody`, `AiErrorMapperTest.mapsExceptions` | `assistant.ai` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-02 | AI 空响应或格式异常可能被当作正常回答 | 空 choices、缺失 message、非 JSON 响应 | 返回空响应或格式错误失败 | 修复前异常响应边界不完整 | 响应结构校验不足 | 增加空响应、畸形响应解析校验 | AI-06；`DeepSeekAiClientTest.chatMapsEmptyResponseShapes`, `DeepSeekAiClientTest.chatMapsMalformedResponseShapes` | `assistant.ai` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-03 | 结构化建议字段缺失或格式异常未被明确拒绝 | 缺少必填字段、非法 JSON、正文夹带多余文本 | 返回格式校验失败，不生成草稿 | 修复前解析边界可能放过异常输入 | JSON 抽取和必填字段校验不足 | 补齐 `StructuredSuggestionParser` 的严格解析和异常输入拒绝 | DRAFT-03；`StructuredSuggestionParserTest.rejectsMalformedInputs`, `StructuredSuggestionParserTest.rejectsTextAroundJsonAndTrailingTokens` | `assistant.ai` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-04 | 草稿重复确认或终态草稿仍可能调用导入服务 | 已导入或已取消草稿再次确认 | 返回状态冲突且不写入本地数据 | 修复前生命周期状态保护不足 | 状态迁移前置校验缺失 | 在确认和取消入口统一检查终态 | DRAFT-06；`DraftLifecycleServiceTest.confirmDraftRejectsTerminalDraftsWithoutImporting`, `DraftLifecycleServiceTest.cancelDraftRejectsTerminalDrafts` | `assistant.ai` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-05 | 任务草稿批量导入中途失败后可能留下部分任务 | 多任务草稿，第二个任务创建失败或仓储抛异常 | 已创建任务回滚，返回失败 | 修复前缺少批量导入事务语义 | 先写入后失败未清理 | `DraftImportService` 记录已创建 id 并 best-effort 删除 | DRAFT-08；`DraftImportServiceTest.rollsBackCreatedTasksWhenTaskCreationFails`, `DraftImportServiceTest.rollsBackCreatedTasksWhenTaskCreationThrowsRuntimeException` | `assistant.ai`, `assistant.task` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-06 | 控制台草稿列表或业务列表输出被截断 | 草稿、交易或笔记超过 10 条 | 列出全部记录 | 修复前列表展示可能只显示前 10 条 | 控制台展示循环限制 | 改为遍历完整列表 | DRAFT-10, NOTE-08；`ConsoleApplicationTest.draftMenuListsAllDraftsWithoutTruncation`, `ConsoleApplicationTest.financeMenuListsMoreThanTenTransactionsWithoutTruncation`, `ConsoleApplicationTest.noteMenuListsMoreThanTenNotesWithoutTruncation` | `assistant.app` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-07 | 草稿 id 非法输入可能进入生命周期服务 | 非数字、小数、非正整数、超出 `long` 范围 | 控制台拦截并提示，不调用服务 | 修复前输入解析边界不足 | id 解析未覆盖所有非法等价类 | 增加统一 id 解析和调用前校验 | DRAFT-10；`ConsoleApplicationTest.draftMenuRejectsInvalidIdBeforeCallingDraftLifecycleService` | `assistant.app` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-08 | 日程首尾相接被误判为冲突 | 一个日程结束时间等于另一个开始时间 | 允许创建或更新 | 修复前重叠判定边界不清 | 时间区间闭开边界处理错误 | 冲突策略采用结束点不包含的区间语义 | SCHEDULE-04；`ScheduleServiceTest.createScheduleAllowsTouchingTimeRanges`, `ScheduleServiceTest.updateScheduleAllowsTouchingOtherScheduleTimeRange` | `assistant.schedule` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-09 | 收支删除后统计可能未重新反映当前数据 | 删除一笔收入或支出后再查看统计 | 统计基于删除后的交易集合 | 修复前统计入口可能依赖旧集合 | 查询和统计链路同步不足 | 删除后通过服务重新查询并计算统计 | FINANCE-07；`ConsoleApplicationTest.financeMenuDeleteRecomputesStatistics` | `assistant.finance`, `assistant.app` 单元测试，全量 `mvn clean test` | 已修复 |
| BUG-10 | 摘要依赖失败时错误消息不稳定 | 任一依赖服务返回失败且消息为空 | 返回稳定失败消息 | 修复前可能暴露空消息 | 依赖错误传播缺少消息兜底 | `SummaryService` 使用稳定错误消息 | SUMMARY-06；`SummaryServiceTest.getDashboardSummaryPropagatesFirstDependencyFailure`, `SummaryServiceTest.getDashboardSummaryUsesStableFallbackWhenDependencyFailureMessageIsBlank` | `assistant.summary` 单元测试，全量 `mvn clean test` | 已修复 |

## 核心回归测试集

- 任务状态：新增、筛选、完成、重复完成、撤销完成、删除不存在和摘要同步。
- 日程状态：创建、非法时间范围、冲突、首尾相接、按日期查询、即将开始、进行中和已过期。
- 学习计划进度：进度 0、100、-1、101，未开始、进行中、完成、逾期和统计。
- 收支统计：收入、支出、金额边界、类别和日期过滤、空集合统计、多笔统计、删除后统计。
- 汇总：空数据、单模块数据、多模块组合、本周学习、本月收支、笔记标签分布。
- AI 失败降级：未配置、空响应、格式异常、HTTP 401/429/5xx、超时和网络异常。
- AI 草稿确认/取消：任务草稿、学习计划草稿、字段缺失、确认导入、取消不写入、重复确认冲突、导入失败回滚。
- 控制台交互：非法 id、非法日期、非法枚举、循环子菜单、帮助、返回、EOF、列表不截断。

## 回归执行结论

v28 验证报告记录默认单元测试 `mvn clean test` 通过 944 个测试，失败 0 个。真实 DeepSeek 集成测试未作为默认回归执行，当前回归结论仅覆盖隔离的 JUnit 单元测试和控制台交互单元测试。

## 残余风险

- 真实 DeepSeek 连通性依赖外部网络、服务可用性和 `DEEPSEEK_API_KEY`，当前未记录真实网络复测结果。
- 当前 `src/test/java` 下没有 `*IT.java` 集成测试类，`mvn -Pintegration verify` 只是可选集成测试入口。
- 覆盖率百分比需要由本地 `mvn clean verify` 或 `mvn jacoco:report` 生成，本记录不伪造覆盖率数字。
- 若课程要求提交覆盖截图，应基于本地生成的 `target/site/jacoco/index.html` 另行引用。

相关用例见 [`test-cases.md`](test-cases.md)，覆盖路径说明见 [`coverage/README.md`](coverage/README.md)。
