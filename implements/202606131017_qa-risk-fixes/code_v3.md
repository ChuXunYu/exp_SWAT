# 实现报告（v3）

## 概述
按 `detail_v3.md` 完成摘要页紧急任务视图扩展：

- `DashboardSummary` 新增逾期未完成任务、未来 7 天高优先级任务两个不可变列表字段。
- `SummaryService.getDashboardSummary()` 改为一次 `TaskQuery.all()` 获取任务快照，并在内存中过滤今日任务、逾期未完成任务、未来 7 天高优先级未完成任务。
- `LocalContext` 新增两个任务明细行列表，overview 文本加入两个新增计数。
- `PromptBuilder` 在 user message 中输出两个新增任务段落，沿用空列表“（无）”语义。
- `ConsoleApplication.showSummary()` 输出新增计数和非空任务标题明细。
- 同步更新摘要、AI prompt、控制台、文档相关测试和测试文档。

## 文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `java-ai-assistant/src/main/java/assistant/summary/DashboardSummary.java` | 新增 `overdueTasks`、`upcomingHighPriorityTasks` 字段及构造校验 |
| 修改 | `java-ai-assistant/src/main/java/assistant/summary/SummaryService.java` | 改用 `TaskQuery.all()` 并新增三类任务过滤方法 |
| 修改 | `java-ai-assistant/src/main/java/assistant/summary/LocalContext.java` | 新增两类任务行、overview 计数和 `from` 映射 |
| 修改 | `java-ai-assistant/src/main/java/assistant/ai/PromptBuilder.java` | 输出逾期未完成任务、未来 7 天高优先级任务段落 |
| 修改 | `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 汇总页输出新增计数和任务标题明细 |
| 修改 | `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java` | 覆盖全量任务查询、过滤规则、失败短路和本地上下文输出 |
| 修改 | `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java` | 覆盖新增列表快照、空摘要、null 和元素 null 校验 |
| 修改 | `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java` | 覆盖新增 overview 计数、任务行、空列表、构造校验和不可变快照 |
| 修改 | `java-ai-assistant/src/test/java/assistant/ai/PromptBuilderTest.java` | 覆盖新增 prompt 段落的非空和空列表输出 |
| 修改 | `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 覆盖汇总输出新增计数和任务标题 |
| 修改 | `java-ai-assistant/src/test/java/assistant/ai/AiAssistantServiceTest.java` | 同步 `DashboardSummary` 构造签名 |
| 修改 | `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionDraftServiceTest.java` | 同步 `DashboardSummary` 构造签名 |
| 修改 | `java-ai-assistant/docs/test-plan.md` | 更新汇总统计和 AI 上下文测试重点 |
| 修改 | `java-ai-assistant/docs/test-cases.md` | 新增 SUMMARY-07 并更新 AI/SUMMARY 用例映射 |
| 修改 | `java-ai-assistant/docs/coverage/README.md` | 更新 `SummaryService.getDashboardSummary()` 控制流、路径和复杂度说明 |

## 编译验证
已执行：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn test -DskipTests=false
```

结果：`BUILD SUCCESS`，`Tests run: 983, Failures: 0, Errors: 0, Skipped: 0`。

已执行针对性验证：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn test -Dtest=SummaryServiceTest,DashboardSummaryTest,LocalContextTest,PromptBuilderTest,ConsoleApplicationTest,DocumentationDeliveryTest
```

结果：`BUILD SUCCESS`，`Tests run: 161, Failures: 0, Errors: 0, Skipped: 0`。

## 设计偏差说明
无偏差。
