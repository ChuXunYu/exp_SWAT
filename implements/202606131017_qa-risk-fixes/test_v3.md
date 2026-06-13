# 测试报告（v3）

## 角色与范围
本步骤按测试编写角色完成。依据 `detail_v3.md` 的行为契约，结合 `code_v3.md` 的实现说明，检查并确认本轮摘要页紧急任务视图相关单元测试已落入项目约定测试目录。

本角色只负责编写/确认测试，不负责运行测试。

## 测试文件
| 测试文件 | 覆盖目标 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java` | 全量任务查询、今日任务过滤、逾期未完成任务过滤、未来 7 天高优先级任务过滤、依赖失败短路、本地上下文生成 |
| `java-ai-assistant/src/test/java/assistant/summary/DashboardSummaryTest.java` | 新增任务列表字段的构造校验、元素 null 校验、不可变快照、空摘要 |
| `java-ai-assistant/src/test/java/assistant/summary/LocalContextTest.java` | overview 新增计数、新增任务行生成、空列表、构造校验、不可变快照 |
| `java-ai-assistant/src/test/java/assistant/ai/PromptBuilderTest.java` | AI user message 中新增“逾期未完成任务”和“未来7天高优先级任务”段落，覆盖非空和空列表输出 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 控制台汇总页新增计数和任务标题明细输出 |
| `java-ai-assistant/src/test/java/assistant/ai/AiAssistantServiceTest.java` | 同步新增 `DashboardSummary` 构造签名后的既有 AI 服务测试 |
| `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionDraftServiceTest.java` | 同步新增 `DashboardSummary` 构造签名后的既有结构化草稿服务测试 |

第一个测试文件路径：

`/root/exp_SWAT/java-ai-assistant/src/test/java/assistant/summary/SummaryServiceTest.java`

## 行为契约覆盖
- `SummaryService.getDashboardSummary()` 使用 `TaskQuery.all()` 进行一次任务快照查询，并继续按既有服务顺序查询日程、学习计划、收支和笔记。
- 任务查询失败时立即返回失败，不调用后续依赖。
- `todayTasks` 保持仅按 `dueDate == today` 过滤，不排除已完成任务。
- `overdueTasks` 覆盖 `dueDate < today && status != COMPLETED`，并排除已完成逾期任务。
- `upcomingHighPriorityTasks` 覆盖 `today <= dueDate <= today.plusDays(7) && priority == HIGH && status != COMPLETED`。
- 未来 7 天窗口覆盖今天和第 7 天边界，排除第 8 天、低优先级和已完成任务。
- 同一高优先级未完成今日任务可同时出现在今日任务和未来 7 天高优先级任务中。
- `LocalContext` 从摘要生成新增任务明细行，overview 文本包含两个新增计数。
- `PromptBuilder` 对新增列表输出非空条目；空列表时输出 `（无）`。
- `ConsoleApplication` 汇总页输出新增计数，并在非空时按源顺序展示任务标题、截止日期、优先级和状态。
- `DashboardSummary` 和 `LocalContext` 对新增字段维持 null 拒绝、元素 null/blank 拒绝和不可变快照语义。

## 边界与错误路径
- 空摘要下新增任务列表为空，overview 和 prompt 仍稳定输出。
- 逾期已完成任务不进入逾期未完成任务列表。
- 今日已完成任务仍进入今日任务列表，但不进入未来 7 天高优先级任务列表。
- 第 7 天高优先级未完成任务进入未来 7 天列表，第 8 天不进入。
- 任务依赖失败消息为空白时，摘要服务仍映射为稳定兜底消息。

## 执行说明
本步骤未运行测试。实现报告记录编码阶段已执行：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn test -DskipTests=false
```

结果：`BUILD SUCCESS`，`Tests run: 983, Failures: 0, Errors: 0, Skipped: 0`。

实现报告还记录已执行针对性测试：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn test -Dtest=SummaryServiceTest,DashboardSummaryTest,LocalContextTest,PromptBuilderTest,ConsoleApplicationTest,DocumentationDeliveryTest
```

结果：`BUILD SUCCESS`，`Tests run: 161, Failures: 0, Errors: 0, Skipped: 0`。

## 结论
本轮测试覆盖详细设计要求的新增摘要紧急事项视图、AI 本地上下文、AI prompt 出口和控制台汇总展示。未发现需要额外新增测试文件的缺口。
