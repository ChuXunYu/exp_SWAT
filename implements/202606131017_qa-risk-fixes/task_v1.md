# 任务指令（v1）

## 动作
NEW

## 任务描述
在 Java AI 助手中补齐 AI 结构化草稿端到端生成入口，并让新生成任务草稿的 dueDate 规则在保存前与确认导入保持一致。

预期实现范围：
- 在 `assistant.ai` 或合适应用层新增结构化草稿生成服务，负责调用 AI 结构化场景、解析返回内容、分配草稿 id、校验草稿内容、保存 `SuggestionDraft`。
- 支持生成任务草稿和学习计划草稿两类入口。
- 任务草稿保存前必须校验每个 `TaskDraftItem` 都有合法 `dueDate`；AI 返回缺失 dueDate 的任务草稿时返回清晰失败结果，不保存草稿，不创建正式任务。
- 学习计划草稿生成成功后保存 `StudyPlanDraftContent`，包括已有解析器支持的 breakdown 内容。
- 在 `ApplicationFactory` / `ApplicationServices` 完成装配，让 CLI 能访问该服务。
- 在 `ConsoleApplication` 的 AI 草稿菜单增加“生成任务草稿”和“生成学习计划草稿”入口；生成成功后用户能在草稿列表查看，并沿用现有查看、确认导入、取消和重复确认保护流程。
- 失败路径需要对 AI 未配置、调用失败、空响应、格式异常、字段非法、任务 dueDate 缺失给出稳定且清晰的控制台提示。

## 选择理由
这是本轮最高风险的用户主流程缺口。项目已有解析、草稿仓储、生命周期和导入能力，但用户无法从控制台自然完成“向 AI 提出结构化请求 -> 生成草稿 -> 查看草稿 -> 确认或取消 -> 导入正式数据”的链路。

任务草稿 dueDate 不一致会让系统继续保存“可查看但不可导入”的新草稿。由于正式任务模型和导入服务都要求 dueDate 非空，且当前草稿菜单没有编辑补全入口，本任务应在新生成草稿保存前拒绝缺 dueDate 的任务草稿。

## 任务上下文
来自 `/root/exp_SWAT/requirements/qa-risk-fix-requirement.md` 的约束：
- 必须补齐 AI 结构化草稿端到端生成入口。
- 用户可从控制台进入“生成任务草稿”和“生成学习计划草稿”一类结构化生成入口。
- AI 返回合法任务草稿时，系统保存一个可查看、可确认导入的任务草稿；确认后正式任务数据增加，取消后正式任务数据不变。
- AI 返回合法学习计划草稿时，系统保存一个可查看、可确认导入的学习计划草稿；确认后正式学习计划数据增加，取消后正式学习计划数据不变。
- AI 未配置、超时、鉴权失败、限流、空响应、格式异常或结构化校验失败时，应显示明确失败结果，不创建不可用草稿，不修改正式业务数据。
- 新生成或新解析并保存的任务草稿中，每个任务项都具有合法 dueDate。
- 缺少 dueDate 的 AI 任务草稿响应不会保存为可确认草稿，或按明确规则补全后保存；本任务采用“拒绝保存并提示缺少必需字段”的规则。
- 用户在草稿列表中看到的新生成可确认任务草稿，确认导入时不应再因为 dueDate 缺失失败。

必须补充或更新的测试：
- 服务层端到端单元测试：fake AI 返回任务草稿 JSON 后解析、分配草稿 id、保存草稿。
- 服务层端到端单元测试：fake AI 返回学习计划草稿 JSON 后解析、分配草稿 id、保存草稿。
- 失败路径测试：AI 未配置、空响应、格式异常、结构化字段非法、任务 dueDate 缺失时不保存草稿。
- CLI 测试：用户从结构化生成入口创建草稿后，草稿菜单能列出并查看该草稿。
- 回归测试：生成后的草稿确认导入、取消、重复确认仍符合既有生命周期规则。

## 已有代码上下文
- `assistant.ai.StructuredSuggestionParser` 已支持解析任务草稿 JSON 和学习计划草稿 JSON。
- `assistant.ai.TaskDraftItem` 当前允许 `dueDate == null`，但 `assistant.ai.DraftImportService` 在导入任务草稿前通过 `validateTaskDueDates()` 拒绝缺失 dueDate。
- `assistant.ai.StudyPlanDraftContent` 已保存 `breakdown`，解析器会清洗空 breakdown 项。
- `assistant.ai.SuggestionDraftRepository`、`InMemorySuggestionDraftRepository`、`SuggestionDraft`、`DraftLifecycleService` 和 `DraftImportService` 已提供草稿存储、查看、取消、确认导入和重复确认保护。
- `assistant.ai.AiAssistantService` 当前用于通用 AI 问答；`ConsoleApplication.askAi()` 只调用 `AiScenario.GENERAL_QA` 并输出自然语言，不解析或保存草稿。
- `assistant.app.ConsoleApplication.runDraftMenu()` 当前只管理已有草稿：列表、查看、确认导入、取消、返回和帮助。
- `assistant.app.ApplicationFactory` 当前装配 AI 问答服务、草稿仓储、导入服务和生命周期服务；`ApplicationServices` 需要扩展以暴露新增生成服务。

实现注意：
- 不要把结构化解析、校验和保存规则直接散落在 CLI 菜单中；CLI 应只负责读取用户目标、调用服务并展示结果。
- 不要重构整个 `ConsoleApplication`；使用小型私有方法即可。
- 不要依赖真实 DeepSeek、真实 API Key 或网络；测试使用 fake/stub AI。
- 本轮暂不处理学习计划 breakdown 导入落地、摘要紧急事项、中文枚举体验和文档最终同步，这些由后续任务继续推进。
