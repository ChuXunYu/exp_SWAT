# 任务指令（v1）

## 动作
NEW

## 任务描述
修订 `/root/exp_SWAT/软件质量保证与测试实验指导书.md`，将其从泛化课程实验说明改写为围绕当前真实 `java-ai-assistant` 项目的《软件质量保证与测试》课程实验指导书。指导书必须能够直接指导学生或评审老师完成实验、复现实验步骤、检查交付材料、填写实验报告并按评分标准评价。

允许在发现明显缺口时小幅补充 `/root/exp_SWAT/java-ai-assistant/docs/test-plan.md` 或 `/root/exp_SWAT/java-ai-assistant/docs/test-cases.md`，但本轮核心交付是主指导书。禁止修改 `/root/exp_SWAT/软件质量保证与测试实验指导书.doc`、`/root/exp_SWAT/实验说明.docx`、`/root/exp_SWAT/实验报告模板.docx` 等二进制文件，不要大范围重写源码。

## 选择理由
当前 `/root/exp_SWAT/软件质量保证与测试实验指导书.md` 仍是通用模板，只说明白盒测试和黑盒测试的基础要求，缺少与 `java-ai-assistant` 真实功能、真实路径、真实测试类、Maven 命令、CI 文件、缺陷回归、质量度量、验收与评分的映射。任务目标是“课程项目最高分交付”，因此第一轮应优先完成主交付文档的完整结构和真实项目证据链。

## 任务上下文
指导书必须至少覆盖以下内容，并且所有功能、命令、路径、类名、测试类、CI 文件、文档引用都必须来自真实项目：

- 实验名称、适用课程、实验性质、建议学时、前置知识。
- 实验目的：质量保证意识、测试设计方法、自动化测试、CI、缺陷分析、质量评价。
- 实验环境：JDK、Maven、Git、GitHub Actions、项目目录、运行/测试命令。
- 项目简介：`java-ai-assistant` 的真实功能模块和被测对象。
- 被测范围和不测范围：明确当前版本功能边界。
- 质量目标和质量属性：正确性、健壮性、可维护性、可测试性、可追踪性等。
- 测试策略：测试层级、测试类型、测试数据、测试环境、准入/准出标准。
- 测试计划：任务分解、角色、进度、风险、交付物。
- 测试用例设计方法：等价类、边界值、判定表或状态迁移、异常场景，并映射到当前项目功能。
- 单元测试实验：目标、步骤、关键测试类索引、运行命令、观察点。
- 集成测试实验：服务、仓储、AI 配置、导入导出边界或当前可选集成入口的组合验证；不得声称当前已有真实 DeepSeek `*IT.java`。
- 系统测试与验收测试实验：从用户工作流验证任务、日程、学习计划、财务、笔记、AI 建议草稿等场景。
- 自动化测试与 CI 实验：说明 `.github/workflows/ci.yml`、触发条件、执行命令、如何解读结果。
- 缺陷记录与分析：缺陷模板、严重级别、复现步骤、根因分析、回归测试要求。
- 质量度量：测试通过率、用例覆盖范围、缺陷密度或严重度分布、自动化执行结果、风险残留。
- 实验报告提交要求：结合 `/root/exp_SWAT/实验报告模板.md` 和 `/root/exp_SWAT/实验说明.md`。
- 评分标准：建议以 100 分制给出清晰可执行的评分项，突出高分要求。
- 附录：推荐命令、关键目录结构、关键测试类索引、参考文档清单。

验证要求应在后续实现完成后至少包括：

- `git status`
- 检查 Markdown 中引用的关键路径是否存在。
- 运行项目测试或与文档相关的验证测试。
- 本轮完成后应按 implementation harness 管线规则提交，并确保推送到当前工作分支对应的远程分支。
- 若当前执行环境无法完成推送，应在验证报告中明确记录失败原因、当前分支、提交哈希和远程状态，避免静默遗漏。
- 管线全部完成后的最终收尾要求仍由主 Agent 处理：将成果 fast-forward 合回 `main`，删除临时远程分支，并保持远程只剩 `main`。

## 已有代码上下文
真实项目与文档资料如下：

- 主交付待修订文件：`/root/exp_SWAT/软件质量保证与测试实验指导书.md`。
- 课程提交说明：`/root/exp_SWAT/实验说明.md`。
- 实验报告模板：`/root/exp_SWAT/实验报告模板.md`。
- 项目 README：`/root/exp_SWAT/java-ai-assistant/README.md`。
- 测试计划：`/root/exp_SWAT/java-ai-assistant/docs/test-plan.md`。
- 白盒测试用例：`/root/exp_SWAT/java-ai-assistant/docs/test-cases.md`。
- 需求、设计和验收资料：`/root/exp_SWAT/docs/1 requirement.md`、`/root/exp_SWAT/docs/2 design-oo.md`、`/root/exp_SWAT/docs/3 acceptance-and-next-iteration.md`、`/root/exp_SWAT/acceptance/20260613_full_acceptance.md`。
- CI 文件：`/root/exp_SWAT/.github/workflows/ci.yml`，当前在 push 和 pull_request 时于 `java-ai-assistant` 工作目录执行 `mvn -B -DskipTests package` 和 `mvn -B test`。
- Maven 项目目录：`/root/exp_SWAT/java-ai-assistant`，Java 17 单模块控制台应用，运行入口 `assistant.app.Main`。

当前真实业务模块与代表性类包括：

- AI 与草稿：`assistant.ai.AiAssistantService`、`PromptBuilder`、`DeepSeekAiClient`、`AiConfigurationLoader`、`StructuredSuggestionParser`、`DraftLifecycleService`、`DraftImportService`。
- 应用入口与控制台：`assistant.app.Main`、`ApplicationFactory`、`ConsoleApplication`、`DemoDataFactory`。
- 任务：`assistant.task.TaskService`、`TaskItem`、`TaskQuery`、`TaskStatus`、`TaskPriority`。
- 日程：`assistant.schedule.ScheduleService`、`ScheduleItem`、`ScheduleConflictPolicy`、`ScheduleStatus`、`ScheduleQuery`。
- 学习计划：`assistant.study.StudyPlanService`、`StudyPlan`、`StudyPlanAnalysisService`、`StudyPlanStatus`、`StudyPlanQuery`。
- 收支：`assistant.finance.FinanceService`、`FinanceStatisticsService`、`TransactionRecord`、`TransactionQuery`、`TransactionType`。
- 笔记：`assistant.note.NoteService`、`Note`、`NoteQuery`、`NoteSearchPolicy`。
- 汇总：`assistant.summary.SummaryService`、`DashboardSummary`、`LocalContext`。
- 可测试性：`assistant.testability.TimeProvider`、`FixedTimeProvider`、`IdGenerator`、`IncrementalIdGenerator`。

代表性测试类包括：

- AI：`AiAssistantServiceTest`、`PromptBuilderTest`、`DeepSeekAiClientTest`、`AiErrorMapperTest`、`AiConfigurationLoaderTest`、`StructuredSuggestionParserTest`、`DraftLifecycleServiceTest`、`DraftImportServiceTest`。
- 控制台和装配：`ConsoleApplicationTest`、`ApplicationFactoryTest`、`DemoDataFactoryTest`、`MainTest`。
- 业务模块：`TaskServiceTest`、`ScheduleServiceTest`、`StudyPlanServiceTest`、`StudyPlanAnalysisServiceTest`、`FinanceServiceTest`、`FinanceStatisticsServiceTest`、`NoteServiceTest`、`SummaryServiceTest`。
- 文档与 CI 交付：`assistant.docs.DocumentationDeliveryTest`、`assistant.docs.CiWorkflowDeliveryTest`。

README 中记录的常用命令包括：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn clean test
mvn clean verify
mvn jacoco:report
mvn -Pintegration verify
mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
mvn -q -DskipTests compile
java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
printf 'q\n' | java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

重要事实边界：

- 默认测试不访问真实 DeepSeek、真实网络或真实 API Key。
- 当前仓库没有 `*IT.java` 真实 DeepSeek 连通性测试类，`mvn -Pintegration verify` 是保留的可选集成测试入口。
- 项目使用内存仓储，运行时数据不持久化；没有数据库、文件导出、真实系统通知、账号、多用户或图形界面。
- `acceptance/20260613_full_acceptance.md` 记录过 `mvn clean test` 952 个测试通过、`mvn clean verify` 通过、`mvn -Pintegration verify` 通过但无集成测试可运行、JaCoCo 指令覆盖 96.78%、分支覆盖 86.65%、行覆盖 94.55%；如指导书引用具体数字，应标明来自该验收记录或提示以当前执行结果为准，避免伪造新执行结果。
- `java-ai-assistant/docs/test-plan.md` 与 `docs/test-cases.md` 中有历史口径提到 v28 的 944 个测试；如主指导书需要引用测试数量，应优先使用“以当前 Maven/Surefire 输出为准”或引用验收报告中的 952 个测试记录，并注意不要制造文档互相矛盾。

## 修订说明（v1 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| 任务指令遗漏原始需求中“每完成一个轮次都要提交并推送 GitHub”的交付约束，且未传递最终 fast-forward 合回 main、删除临时远程分支、保持远程只剩 main 的收尾要求。 | 在验证要求中补充本轮提交并推送当前远程分支的要求；补充无法推送时需在验证报告记录失败原因、当前分支、提交哈希和远程状态；补充管线完成后由主 Agent 处理 fast-forward 合回 main、删除临时远程分支并保持远程只剩 main。 |
