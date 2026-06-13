# 实现计划

任务描述：基于 qa-risk-fix-requirement.md 修复本轮确认的 QA 风险，补齐 AI 结构化草稿入口、学习计划 breakdown 落地、任务草稿 dueDate 一致性、摘要紧急事项、中文 CLI 枚举体验，并同步测试、文档、验证、提交和推送。
项目根目录：/root/exp_SWAT

---

## R1 NEW AI 结构化草稿生成入口与 dueDate 保存前一致性
任务：新增或补齐应用层结构化建议生成服务，串联 AI 调用、结构化解析、草稿 id 分配、dueDate 校验、草稿保存；在 ConsoleApplication 的 AI 草稿菜单增加生成任务草稿和生成学习计划草稿入口；补充服务层、CLI 和生命周期回归测试，预期涉及 /root/exp_SWAT/java-ai-assistant/src/main/java/assistant/ai、/root/exp_SWAT/java-ai-assistant/src/main/java/assistant/app 及对应测试。
选择理由：AI 结构化草稿端到端入口是当前最高风险主流程缺口；任务草稿 dueDate 不一致会直接影响新生成草稿的可确认性，应在生成保存入口同步约束，避免继续产生可查看但不可导入的新草稿。
上下文：项目已有 StructuredSuggestionParser、SuggestionDraft、SuggestionDraftRepository、DraftLifecycleService、DraftImportService、AiAssistantService、AiScenario、IdGenerator 和草稿菜单管理能力，但缺少“AI 结构化响应 -> 解析 -> 保存草稿 -> CLI 可见”的应用服务和入口。TaskDraftItem 允许 dueDate 为空，而 DraftImportService 要求任务草稿导入时 dueDate 非空；本轮新生成草稿必须保存前拒绝缺 dueDate 的任务项并给出清晰错误。
