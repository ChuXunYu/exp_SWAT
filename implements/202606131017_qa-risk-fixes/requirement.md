项目根目录：/root/exp_SWAT

任务：
基于 /root/exp_SWAT/requirements/qa-risk-fix-requirement.md，对已确认且列入本轮的质量风险进行修复、补充测试、更新文档，并提交推送。

参考材料：
- /root/exp_SWAT/diags/qa-risk-diagnosis.md
- /root/exp_SWAT/requirements/qa-risk-fix-requirement.md
- /root/exp_SWAT/docs/1 requirement.md
- /root/exp_SWAT/docs/2 design-oo.md
- /root/exp_SWAT/docs/3 acceptance-and-next-iteration.md
- /root/exp_SWAT/java-ai-assistant/README.md
- /root/exp_SWAT/java-ai-assistant/docs/test-plan.md
- /root/exp_SWAT/java-ai-assistant/docs/test-cases.md
- /root/exp_SWAT/acceptance/20260613_full_acceptance.md

实现要求：
1. 严格按需求文档列入“本轮修复”的问题执行，不擅自扩大范围。
2. 每个修复必须有对应测试。
3. 优先保持现有架构稳定，避免一次性大规模重构 ConsoleApplication。
4. 如果实现 AI 结构化草稿生成入口，必须覆盖：
   - 用户从控制台触发结构化建议生成
   - AI 返回结构化内容
   - 解析并保存草稿
   - 用户查看草稿
   - 用户确认或取消
   - 导入任务或学习计划
   - 失败时错误提示清晰
5. 如果处理学习计划 breakdown，必须明确保存策略，并有测试证明确认导入后不会丢失。
6. 如果处理 dueDate 规则，必须让草稿展示、解析、确认导入和测试保持一致。
7. 如果处理摘要紧急事项，必须补充逾期未完成任务和未来 7 天高优先级任务的测试。
8. 如果处理中文输入体验，必须支持中文别名或数字选项，并保持原英文输入兼容。
9. 更新 README、测试计划、测试用例或验收文档中受影响的说明。
10. 运行必要验证，至少包括：
    - mvn clean test
    - 文档一致性测试
11. 每完成一个轮次都要提交并推送 GitHub。
12. 管线结束后如果创建了临时分支，最终合回 main，并清理远程临时分支，只保留 main。
