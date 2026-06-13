# 实现计划

任务描述：基于 qa-risk-fix-requirement.md 修复本轮确认的 QA 风险，补齐 AI 结构化草稿入口、学习计划 breakdown 落地、任务草稿 dueDate 一致性、摘要紧急事项、中文 CLI 枚举体验，并同步测试、文档、验证、提交和推送。
项目根目录：/root/exp_SWAT

---

## R1 NEW AI 结构化草稿生成入口与 dueDate 保存前一致性
任务：新增或补齐应用层结构化建议生成服务，串联 AI 调用、结构化解析、草稿 id 分配、dueDate 校验、草稿保存；在 ConsoleApplication 的 AI 草稿菜单增加生成任务草稿和生成学习计划草稿入口；补充服务层、CLI 和生命周期回归测试，预期涉及 /root/exp_SWAT/java-ai-assistant/src/main/java/assistant/ai、/root/exp_SWAT/java-ai-assistant/src/main/java/assistant/app 及对应测试。
选择理由：AI 结构化草稿端到端入口是当前最高风险主流程缺口；任务草稿 dueDate 不一致会直接影响新生成草稿的可确认性，应在生成保存入口同步约束，避免继续产生可查看但不可导入的新草稿。
上下文：项目已有 StructuredSuggestionParser、SuggestionDraft、SuggestionDraftRepository、DraftLifecycleService、DraftImportService、AiAssistantService、AiScenario、IdGenerator 和草稿菜单管理能力，但缺少“AI 结构化响应 -> 解析 -> 保存草稿 -> CLI 可见”的应用服务和入口。TaskDraftItem 允许 dueDate 为空，而 DraftImportService 要求任务草稿导入时 dueDate 非空；本轮新生成草稿必须保存前拒绝缺 dueDate 的任务项并给出清晰错误。

---

## R2 PASSED AI 结构化草稿生成入口与 dueDate 保存前一致性
结果：新增 StructuredSuggestionDraftService，串联 AI 结构化场景、解析、草稿 id 分配、保存前类型校验、任务 dueDate 校验、草稿保存和视图返回；ApplicationFactory/ApplicationServices 已装配该服务；ConsoleApplication 的 AI 草稿菜单已支持生成任务草稿和学习计划草稿，生成后可列表、查看、确认、取消。
测试：新增和更新 StructuredSuggestionDraftServiceTest、ApplicationFactoryTest、ConsoleApplicationTest、DemoDataFactoryTest 及测试文档；验证报告显示 mvn test 通过 979 个测试。

## R2 NEW 学习计划草稿 breakdown 导入落地
任务：在 DraftImportService 导入学习计划草稿时，为非空 breakdown 定义并实现正式数据落地策略；建议将每个 breakdown 项转换为与学习计划目标相关的 TODO 任务，并在确认导入后可通过任务服务查询。补充导入服务、失败一致性和必要 CLI/服务查询测试，同步测试计划、测试用例和受影响文档。
选择理由：v1 已补齐 AI 结构化生成入口并保留学习计划草稿 breakdown，但确认导入仍只创建正式学习计划，breakdown 会被静默丢弃；这是本轮必须修复的直接数据一致性缺口，且依赖 v1 的草稿生成链路。
上下文：StudyPlanDraftContent 已清洗并保存 breakdown；StructuredSuggestionDraftService 生成学习计划草稿时保留 breakdown；ConsoleApplication 可展示草稿 breakdown；DraftImportService.importStudyPlan() 当前只调用 StudyPlanService.createStudyPlan(...)，没有读取 content.breakdown()。正式 StudyPlan 模型没有 breakdown 字段，任务模块可承载可执行步骤。若采用“breakdown 转任务”策略，任务 dueDate 应使用稳定规则，例如学习计划 endDate；优先级使用固定默认值，例如 MEDIUM；描述应包含来源学习计划目标，便于用户识别。导入必须避免“学习计划已创建但 breakdown 部分丢失且用户无感知”的情况，至少应先校验 breakdown 可生成任务，再在任务创建失败时返回可解释错误并补偿已创建任务/学习计划，或选择先创建任务再创建学习计划并处理失败回滚。

---

## R3 PASSED 学习计划草稿 breakdown 导入落地
结果：DraftImportService 已将学习计划草稿 breakdown 按顺序转换为正式 TODO 任务，字段映射稳定为标题=breakdown 项、描述=来源学习计划、优先级=MEDIUM、dueDate=学习计划 endDate；学习计划创建失败不会创建任务，breakdown 任务创建失败或异常会回滚本次已创建任务并补偿删除本次学习计划。
测试：新增和更新 DraftImportServiceTest、DocumentationDeliveryTest 及测试文档；验证报告显示 mvn test 通过 983 个测试。

## R3 NEW 摘要页紧急任务视图
任务：扩展摘要数据结构、摘要服务、AI 本地上下文和控制台汇总页，使其在保留今日任务的基础上展示逾期未完成任务、未来 7 天 HIGH 且未完成任务；补充 SummaryService、DashboardSummary/LocalContext、ConsoleApplication 和文档一致性相关测试，同步 README、测试计划或测试用例中受影响说明。
选择理由：AI 草稿主链路、任务 dueDate 一致性和学习计划 breakdown 落地已经通过验证；摘要漏掉逾期未完成任务和未来高优先级任务仍是本轮必须修复的真实用户主流程风险，且独立于中文枚举体验，适合下一轮单独处理。
上下文：SummaryService.getDashboardSummary() 当前只用 TaskQuery.byDueDate(today) 查询今日任务；DashboardSummary 只有 todayTasks 字段，没有 overdueTasks 或 upcomingHighPriorityTasks；LocalContext.from(...) 只生成 todayTaskLines，overviewText 也只包含今日任务数；ConsoleApplication.showSummary() 只输出今日任务数。TaskQuery 当前支持 status/priority/dueDate 单日匹配，不支持日期范围；可在 SummaryService 内用现有 TaskService.listTasks(TaskQuery.all()) 或组合现有查询后过滤，避免为本轮小修扩大仓储接口。规则建议固定为：逾期未完成任务 = dueDate 早于 today 且 status != COMPLETED；未来 7 天高优先级未完成任务 = dueDate 在 today 到 today.plusDays(7) 闭区间内、priority == HIGH、status != COMPLETED；文档和测试必须与该边界一致。
