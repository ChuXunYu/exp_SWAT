# 详细设计（v1）

## 概述
本轮目标是将 `/root/exp_SWAT/软件质量保证与测试实验指导书.md` 从通用白盒/黑盒实验模板改写为围绕当前真实 `java-ai-assistant` 项目的课程实验指导书。设计范围限定为 Markdown 文档交付和必要的一致性补充，不修改 Java 源码，不修改 `.doc`、`.docx` 等二进制文件。

指导书应直接服务《软件质量保证与测试》课程评审：读者能够依据文档了解项目被测对象、准备环境、执行 Maven 测试与 CI 复核、设计和填写实验报告、记录缺陷、计算质量度量并按 100 分制评分。所有类名、测试类、路径、命令和能力边界必须来自当前仓库真实内容；对历史验收数字必须注明来源或提示以当前执行结果为准。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `/root/exp_SWAT/软件质量保证与测试实验指导书.md` | 覆写 | 主交付物。改写为项目化、可执行、可评分的实验指导书，覆盖实验基础信息、项目简介、被测范围、测试策略、测试计划、实验步骤、CI、缺陷、质量度量、报告要求、评分标准和附录。 |
| `/root/exp_SWAT/java-ai-assistant/docs/test-plan.md` | 小幅更新 | 若实现时确认仍存在旧测试数量或与主指导书冲突的口径，将具体数量改为“以当前 Maven/Surefire 输出为准”，并可保留验收报告中的 952 个测试记录作为历史证据。不得重写整体测试计划。 |
| `/root/exp_SWAT/java-ai-assistant/docs/test-cases.md` | 小幅更新 | 若实现时确认仍存在旧测试数量或与主指导书冲突的口径，将执行结果摘要改为当前口径一致的说明。不得重写白盒用例主体。 |
| `/root/exp_SWAT/软件质量保证与测试实验指导书.doc` | 禁止修改 | 二进制参考材料，只可引用其课程背景，不可编辑。 |
| `/root/exp_SWAT/实验说明.docx` | 禁止修改 | 二进制参考材料，不可编辑。 |
| `/root/exp_SWAT/实验报告模板.docx` | 禁止修改 | 二进制参考材料，不可编辑。 |

## 类型定义

### 实验指导书文档
**形态**：Markdown document  
**包路径**：不适用  
**职责**：提供可直接使用的课程实验指导书，串联真实项目、真实测试资产、真实 CI 和课程评分要求。  

**结构签名定义**：
- 标题区：课程编号、课程名称、实验指导书标题、学院信息。
- `## 一、实验基本信息`
- `## 二、实验目的`
- `## 三、实验环境`
- `## 四、项目简介与被测对象`
- `## 五、被测范围与不测范围`
- `## 六、质量目标与质量属性`
- `## 七、测试策略`
- `## 八、测试计划`
- `## 九、测试用例设计方法`
- `## 十、单元测试实验`
- `## 十一、集成测试实验`
- `## 十二、系统测试与验收测试实验`
- `## 十三、自动化测试与 CI 实验`
- `## 十四、缺陷记录与分析`
- `## 十五、质量度量`
- `## 十六、实验报告提交要求`
- `## 十七、评分标准`
- `## 附录 A：推荐命令`
- `## 附录 B：关键目录结构`
- `## 附录 C：关键测试类索引`
- `## 附录 D：参考文档清单`

**公开接口**：不适用。文档对外暴露的是可执行章节、表格、命令、路径和评分标准。  
**构造方式**：以现有 `软件质量保证与测试实验指导书.md` 为目标文件整体覆写，保留课程编号、课程名称和学院信息，替换原泛化实验内容。  
**类型关系**：引用 `java-ai-assistant` README、测试计划、测试用例、验收报告、课程说明、实验报告模板和 CI 文件形成证据链。

### 项目证据清单
**形态**：Markdown table section  
**包路径**：不适用  
**职责**：在指导书中固化可核验项目事实，防止编造能力。  

**字段定义**：
| 字段 | 类型 | 约束 |
|------|------|------|
| `证据项` | 文本 | 例如“项目入口”“CI 文件”“测试计划”“验收记录”。 |
| `真实路径或类名` | 文本 | 必须来自当前仓库，例如 `/root/exp_SWAT/java-ai-assistant/src/main/java/assistant/app/Main.java`、`.github/workflows/ci.yml`。 |
| `用途` | 文本 | 说明在实验中的检查或引用方式。 |
| `边界说明` | 文本 | 标明离线测试、不访问真实 DeepSeek、无 `*IT.java` 等限制。 |

**公开接口**：不适用。  
**构造方式**：在主指导书“项目简介与被测对象”“附录”章节中以表格体现。  
**类型关系**：由当前文件系统中的真实文件、README、验收报告和任务文件共同约束。

### 测试类索引
**形态**：Markdown table section  
**包路径**：不适用  
**职责**：把课程测试层级和真实 JUnit 测试类映射起来，便于学生和评审复核。  

**字段定义**：
| 字段 | 类型 | 约束 |
|------|------|------|
| `模块` | 文本 | AI、应用入口、任务、日程、学习计划、收支、笔记、汇总、文档与 CI。 |
| `被测对象` | 文本 | 真实生产类名，使用反引号包裹。 |
| `代表性测试类` | 文本 | 真实 `src/test/java` 下存在的测试类名。 |
| `观察点` | 文本 | 对应测试目标、边界、异常或状态迁移。 |

**公开接口**：不适用。  
**构造方式**：在主指导书附录 C 和各实验步骤中引用。  
**类型关系**：必须与 `/root/exp_SWAT/java-ai-assistant/src/test/java` 文件列表一致。

### 评分标准
**形态**：Markdown table section  
**包路径**：不适用  
**职责**：提供 100 分制、可执行、可检查的课程评分规则。  

**字段定义**：
| 字段 | 类型 | 约束 |
|------|------|------|
| `评分项` | 文本 | 覆盖项目理解、测试计划、用例设计、自动化执行、CI、缺陷分析、质量度量、报告规范等。 |
| `分值` | 整数 | 总和必须为 100。 |
| `高分要求` | 文本 | 明确如何拿满或接近满分。 |
| `扣分点` | 文本 | 明确常见缺失、泛化表述、路径不存在、未复现测试等扣分原因。 |

**公开接口**：不适用。  
**构造方式**：替换原实验考核标准，保留课程对出勤态度、测试计划、测试用例、结果分析、程序和报告规范的关注，并扩展为项目化评分。  
**类型关系**：与 `/root/exp_SWAT/实验报告模板.md` 的报告结构和 `/root/exp_SWAT/实验说明.md` 的提交材料要求一致。

### 文档一致性修订
**形态**：Markdown line-level update  
**包路径**：不适用  
**职责**：消除 `test-plan.md` 和 `test-cases.md` 中与主指导书、验收报告不一致的历史测试数量口径。  

**字段定义**：
| 字段 | 类型 | 约束 |
|------|------|------|
| `目标文件` | 路径 | 仅限 `java-ai-assistant/docs/test-plan.md` 和 `java-ai-assistant/docs/test-cases.md`。 |
| `旧口径` | 文本 | 例如 v28、944 个测试。 |
| `新口径` | 文本 | “以当前 Maven/Surefire 输出为准”；可补充“`acceptance/20260613_full_acceptance.md` 记录 952 个测试通过”。 |
| `修改范围` | 文本 | 只改相关句子，不重排章节、不新增虚构测试。 |

**公开接口**：不适用。  
**构造方式**：编码阶段根据实际文件内容进行最小补丁。  
**类型关系**：与主指导书中的质量度量和验证说明保持一致。

## 错误处理
本轮为文档设计，无运行时异常类型。实现阶段应按以下错误处理策略约束文档内容：

1. 路径或类名无法在当前仓库中核实时，不写入主指导书；若确需表达能力边界，写为“不属于当前版本范围”。
2. 历史测试数量、覆盖率或验收结果只能引用已存在验收记录，并标明来源；新执行结果必须来自后续真实验证命令。
3. `.doc`、`.docx` 二进制文件即使内容与 Markdown 不一致，也不在本轮修订；主交付以 Markdown 为准。
4. 当前仓库无真实 DeepSeek `*IT.java`，指导书不得声称已有真实网络集成测试；`mvn -Pintegration verify` 只能描述为保留的可选集成测试入口。
5. 若文档一致性测试或 Markdown 路径检查在后续实现中新增，必须检查真实路径或关键内容，不允许只做恒真断言。

## 行为契约

### 主指导书内容契约
1. 必须围绕 `/root/exp_SWAT/java-ai-assistant`，不得写成通用软件测试教材。
2. 必须明确项目是 Java 17、Maven 单模块、控制台应用，运行入口为 `assistant.app.Main`。
3. 必须说明默认测试不访问真实 DeepSeek、真实网络或真实 API Key。
4. 必须说明当前项目使用内存仓储，不包含数据库、文件导出、系统通知、账号、多用户或图形界面。
5. 必须列出真实业务模块和代表性类：
   - AI 与草稿：`AiAssistantService`、`PromptBuilder`、`DeepSeekAiClient`、`AiConfigurationLoader`、`StructuredSuggestionParser`、`DraftLifecycleService`、`DraftImportService`。
   - 应用入口与控制台：`Main`、`ApplicationFactory`、`ConsoleApplication`、`DemoDataFactory`。
   - 任务：`TaskService`、`TaskItem`、`TaskQuery`、`TaskStatus`、`TaskPriority`。
   - 日程：`ScheduleService`、`ScheduleItem`、`ScheduleConflictPolicy`、`ScheduleStatus`、`ScheduleQuery`。
   - 学习计划：`StudyPlanService`、`StudyPlan`、`StudyPlanAnalysisService`、`StudyPlanStatus`、`StudyPlanQuery`。
   - 收支：`FinanceService`、`FinanceStatisticsService`、`TransactionRecord`、`TransactionQuery`、`TransactionType`。
   - 笔记：`NoteService`、`Note`、`NoteQuery`、`NoteSearchPolicy`。
   - 汇总：`SummaryService`、`DashboardSummary`、`LocalContext`。
   - 可测试性：`TimeProvider`、`FixedTimeProvider`、`IdGenerator`、`IncrementalIdGenerator`。
6. 必须列出真实测试类索引，至少覆盖任务文件给出的代表性测试类，并可补充当前文件列表中已存在的值对象和仓储测试类。
7. 必须给出可执行命令，命令工作目录必须标注为 `/root/exp_SWAT/java-ai-assistant`：
   - `mvn clean test`
   - `mvn clean verify`
   - `mvn jacoco:report`
   - `mvn -Pintegration verify`
   - `mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`
   - `mvn -q -DskipTests compile`
   - `java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main`
   - `printf 'q\n' | java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main`
8. 必须说明 CI 文件 `.github/workflows/ci.yml` 在 `push` 和 `pull_request` 时进入 `java-ai-assistant` 工作目录执行 `mvn -B -DskipTests package` 与 `mvn -B test`。
9. 必须提供实验报告填写要求，并引用 `/root/exp_SWAT/实验报告模板.md` 与 `/root/exp_SWAT/实验说明.md`。
10. 必须提供 100 分制评分表，分值合计为 100。

### 测试策略章节契约
1. 测试层级必须至少包含单元测试、集成测试入口、系统/验收测试、CI 自动化验证。
2. 测试类型必须覆盖白盒测试、黑盒/系统场景、边界值、等价类、判定表或状态迁移、异常场景、回归测试。
3. 测试数据必须与真实模块映射，例如任务优先级、日程时间冲突、学习计划进度、收支金额和日期范围、笔记关键字和标签、AI 未配置和错误响应。
4. 准入准出标准必须可执行，例如依赖安装完成、默认测试全部通过、关键路径存在、缺陷记录闭环、报告材料齐全。

### 单元测试实验契约
1. 每个模块应给出目标、步骤、运行命令和观察点。
2. 观察点应包括测试是否隔离外部依赖、是否覆盖成功/失败/边界/状态迁移、是否能从 Surefire 或 Maven 输出复核结果。
3. 不得要求学生直接修改生产源码才能完成实验；本轮指导书用于当前已完成项目的测试复核和报告整理。

### 集成测试实验契约
1. 当前可用的集成入口是 `mvn -Pintegration verify`。
2. 必须明确当前无 `*IT.java`，执行通过且 Failsafe `No tests to run` 不等价于真实 DeepSeek 连通性已验证。
3. 集成实验可围绕服务组合、应用装配、AI 配置加载边界、草稿导入与摘要同步说明；真实 DeepSeek 连通性只能作为未来可选扩展。

### 系统与验收实验契约
1. 应从用户工作流描述验收路径，覆盖任务、日程、学习计划、财务、笔记、AI 建议草稿和汇总。
2. 应引用 `/root/exp_SWAT/acceptance/20260613_full_acceptance.md` 中的历史验收记录：`mvn clean test` 952 个测试通过、`mvn clean verify` 通过、`mvn -Pintegration verify` 通过但无集成测试可运行、JaCoCo 指令覆盖 96.78%、分支覆盖 86.65%、行覆盖 94.55%。
3. 若引用上述数字，必须标明“来自 2026-06-13 验收记录”，并提示课程复核时以当前执行输出为准。

### 缺陷与质量度量契约
1. 缺陷模板必须包含缺陷编号、模块、严重级别、环境、复现步骤、预期结果、实际结果、根因分析、修复措施、回归测试、状态。
2. 严重级别至少包含阻塞、严重、一般、轻微，并给出项目化示例。
3. 质量度量至少包含测试通过率、模块用例覆盖范围、缺陷密度或严重度分布、自动化执行结果、覆盖率证据、风险残留。
4. 必须明确文档不能伪造新执行结果；后续编码和验证阶段应运行真实命令并记录。

## 依赖关系
主指导书依赖以下真实项目资料：

| 依赖 | 用途 |
|------|------|
| `/root/exp_SWAT/实验说明.md` | 提交材料、命名要求、答辩视频和其它文档要求。 |
| `/root/exp_SWAT/实验报告模板.md` | 报告章节、测试计划、测试用例、结果分析和评分表参考。 |
| `/root/exp_SWAT/java-ai-assistant/README.md` | 项目功能、环境、命令、配置和边界说明。 |
| `/root/exp_SWAT/java-ai-assistant/docs/test-plan.md` | 测试目标、范围、策略、测试分层、核心功能覆盖计划。 |
| `/root/exp_SWAT/java-ai-assistant/docs/test-cases.md` | 白盒测试用例编号、方法、模块映射和执行结果摘要。 |
| `/root/exp_SWAT/docs/1 requirement.md` | 原始需求、8 个核心功能、AI 和测试分层要求。 |
| `/root/exp_SWAT/docs/2 design-oo.md` | OO 设计和模块职责参考。 |
| `/root/exp_SWAT/docs/3 acceptance-and-next-iteration.md` | 验收清单和下一阶段边界参考。 |
| `/root/exp_SWAT/acceptance/20260613_full_acceptance.md` | 历史测试、覆盖率、风险和验收结论证据。 |
| `/root/exp_SWAT/.github/workflows/ci.yml` | CI 触发条件、JDK 17、Maven package/test 命令。 |
| `/root/exp_SWAT/java-ai-assistant/src/main/java` | 生产类、模块边界和被测对象来源。 |
| `/root/exp_SWAT/java-ai-assistant/src/test/java` | 真实 JUnit 测试类索引来源。 |

## 编码阶段建议顺序
1. 覆写 `/root/exp_SWAT/软件质量保证与测试实验指导书.md`，先完成所有必需章节，再补齐表格、命令和附录。
2. 检查主指导书中引用的关键路径、类名、测试类和 CI 文件是否存在。
3. 检查 `java-ai-assistant/docs/test-plan.md`、`java-ai-assistant/docs/test-cases.md` 是否仍存在 944/v28 等旧口径；若存在，最小化改为当前一致口径。
4. 运行必要验证命令并在后续验证报告中记录，包括 `git status`、Markdown 关键路径检查、项目测试或文档一致性测试。
5. 后续 Runner 阶段按管线要求提交并推送当前工作分支；如无法推送，验证报告必须记录失败原因、当前分支、提交哈希和远程状态。
