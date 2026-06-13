项目根目录：/root/exp_SWAT

任务：
请基于当前完整项目，对以下 8 个风险逐条进行事实诊断，判断问题是否真实存在、描述是否精准、影响范围和严重程度，并给出代码/测试/文档证据。不要直接修改代码。

待诊断风险：
1. AI 草稿功能还像“后处理器”，不是完整用户流程：控制台可能缺少“让 AI 生成结构化草稿并保存”的入口，AI 问答和 AI 草稿管理没有自然串联。
2. 学习计划草稿的 breakdown 可能被浪费：DraftImportService 导入学习计划时可能只创建学习计划本体，AI 生成的分解步骤没有保存、转任务或转笔记。
3. 草稿模型和导入规则不一致：展示层允许任务没有 dueDate，但导入任务草稿时强制每个任务都有截止日期。
4. ConsoleApplication 过于庞大：菜单、输入解析、格式化输出、业务调用、错误提示混在一起，维护风险高。
5. 摘要页可能漏掉真正紧急的事：摘要偏今天到期任务，可能没有突出逾期未完成任务和未来 7 天高优先级任务。
6. 中文交互里混用英文枚举：优先级、状态、收入支出类型等可能要求用户输入 LOW/MEDIUM/HIGH、INCOME/EXPENSE。
7. 草稿导入缺少真正事务边界：多任务导入失败时靠手动删除回滚，未来持久化后可能有部分导入和重复导入风险。
8. 部分功能有轻微冗余或低耦合：笔记搜索/筛选和财务模块相对核心 AI 学习助手可能外扩。

诊断要求：
1. 逐条检查真实代码、测试和文档，不凭印象判断。
2. 每条风险输出：
   - 是否存在：CONFIRMED / PARTIAL / NOT_FOUND / DESIGN_TRADEOFF
   - 严重程度：BLOCKER / HIGH / MEDIUM / LOW
   - 证据文件和关键类/方法
   - 当前已有测试覆盖
   - 问题描述是否需要修正
   - 建议处理方式：立即修复 / 重构规划 / 文档说明 / 暂不处理
3. 特别关注：
   - /root/exp_SWAT/java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java
   - /root/exp_SWAT/java-ai-assistant/src/main/java/assistant/ai
   - /root/exp_SWAT/java-ai-assistant/src/main/java/assistant/summary
   - /root/exp_SWAT/java-ai-assistant/src/test/java
   - /root/exp_SWAT/docs/3 acceptance-and-next-iteration.md
   - /root/exp_SWAT/acceptance/20260613_full_acceptance.md
4. 输出诊断报告到：
   /root/exp_SWAT/diags/qa-risk-diagnosis.md
