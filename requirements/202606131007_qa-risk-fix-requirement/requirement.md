项目根目录：/root/exp_SWAT

输入材料：
- /root/exp_SWAT/diags/202606131007_qa-risk-diagnosis/diag_v1.md
- /root/exp_SWAT/diags/202606131007_qa-risk-diagnosis/challenge_v1.md
- /root/exp_SWAT/docs/1 requirement.md
- /root/exp_SWAT/docs/2 design-oo.md
- /root/exp_SWAT/docs/3 acceptance-and-next-iteration.md
- /root/exp_SWAT/java-ai-assistant/README.md
- /root/exp_SWAT/acceptance/20260613_full_acceptance.md

任务：
基于诊断报告，把确认存在且值得处理的问题优化为清晰、可实现、可测试的修复需求。不要直接修改代码。

要求：
1. 区分必须本轮修复、建议后续修复、只需文档说明、暂不处理的问题。
2. 不要把所有风险都强行变成本轮开发任务，优先保证项目质量和交付稳定性。
3. 对每个本轮修复项写清楚：
   - 用户问题
   - 当前行为
   - 目标行为
   - 验收标准
   - 影响模块
   - 必须补充/更新的测试
4. 建议优先考虑：
   - AI 结构化草稿端到端生成入口
   - 学习计划 breakdown 不丢失
   - 草稿 dueDate 规则一致性
   - 摘要增加逾期和未来高优先级任务
   - 中文控制台输入体验
5. 对 ConsoleApplication 大规模拆分要谨慎：如果风险较高，可以先定义为后续重构任务，而不是本轮大改。
6. 输出需求文档到：
   /root/exp_SWAT/requirements/qa-risk-fix-requirement.md
