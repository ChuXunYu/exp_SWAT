项目根目录：/root/exp_SWAT

任务目标：
以“课程项目最高分交付”为目标，基于当前完整 java-ai-assistant 项目，系统修订并完善 /root/exp_SWAT/软件质量保证与测试实验指导书.md，使其可以作为《软件质量保证与测试》课程实验指导书直接使用，并能体现项目的软件质量保证、测试设计、自动化测试、CI、缺陷分析、验收评价等完整能力。

最高分质量标准：
1. 指导书必须围绕当前 java-ai-assistant 项目展开，避免泛泛的软件测试教材内容。
2. 所有功能、命令、路径、类名、测试类、CI 文件、文档引用必须来自真实项目，不允许编造不存在的能力。
3. 指导书要让学生或评审老师可以按步骤完成实验、复现实验结果、填写实验报告、检查评分标准。
4. 内容要覆盖软件质量保证与测试课程的关键评价点：测试计划、测试策略、测试用例设计、单元测试、集成测试、系统/验收测试、自动化测试、CI、缺陷管理、质量度量、实验报告与评分标准。
5. 结构清晰，语言正式，适合作为课程提交材料；保留 Markdown 作为主交付物，不修改 .doc/.docx 二进制文件。

参考材料：
- /root/exp_SWAT/软件质量保证与测试实验指导书.md
- /root/exp_SWAT/软件质量保证与测试实验指导书.doc
- /root/exp_SWAT/实验说明.md
- /root/exp_SWAT/实验报告模板.md
- /root/exp_SWAT/java-ai-assistant/docs/test-plan.md
- /root/exp_SWAT/java-ai-assistant/docs/test-cases.md
- /root/exp_SWAT/java-ai-assistant/README.md
- /root/exp_SWAT/docs/1 requirement.md
- /root/exp_SWAT/docs/2 design-oo.md
- /root/exp_SWAT/docs/3 acceptance-and-next-iteration.md
- /root/exp_SWAT/acceptance/20260613_full_acceptance.md
- /root/exp_SWAT/.github/workflows/ci.yml
- /root/exp_SWAT/java-ai-assistant/src/main/java
- /root/exp_SWAT/java-ai-assistant/src/test/java

必须交付：
1. 修订 /root/exp_SWAT/软件质量保证与测试实验指导书.md。
2. 如发现现有 /root/exp_SWAT/java-ai-assistant/docs/test-plan.md 或 /root/exp_SWAT/java-ai-assistant/docs/test-cases.md 与指导书存在明显缺口，可以同步小幅补充，但不要大范围重写源码。
3. 不修改 .doc/.docx 二进制文件。

指导书内容必须至少包括：
1. 实验名称、适用课程、实验性质、建议学时、前置知识。
2. 实验目的：覆盖质量保证意识、测试设计方法、自动化测试、CI、缺陷分析、质量评价。
3. 实验环境：JDK、Maven、Git、GitHub Actions、项目目录、运行/测试命令。
4. 项目简介：java-ai-assistant 的真实功能模块和被测对象说明。
5. 被测范围和不测范围：明确当前版本功能边界。
6. 质量目标和质量属性：正确性、健壮性、可维护性、可测试性、可追踪性等。
7. 测试策略：测试层级、测试类型、测试数据、测试环境、准入/准出标准。
8. 测试计划：任务分解、角色、进度、风险、交付物。
9. 测试用例设计方法：等价类、边界值、判定表/状态迁移、异常场景，并映射到当前项目功能。
10. 单元测试实验：目标、步骤、关键测试类索引、运行命令、观察点。
11. 集成测试实验：服务/仓储/AI 配置/导入导出等组合验证。
12. 系统测试与验收测试实验：从用户工作流角度验证任务、日程、学习计划、财务、笔记、AI 建议草稿等场景。
13. 自动化测试与 CI 实验：说明 .github/workflows/ci.yml、触发条件、执行命令、如何解读结果。
14. 缺陷记录与分析：缺陷模板、严重级别、复现步骤、根因分析、回归测试要求。
15. 质量度量：测试通过率、用例覆盖范围、缺陷密度/严重度分布、自动化执行结果、风险残留。
16. 实验报告提交要求：结合 /root/exp_SWAT/实验报告模板.md。
17. 评分标准：建议以 100 分制给出清晰可执行的评分项，突出高分要求。
18. 附录：推荐命令、关键目录结构、关键测试类索引、参考文档清单。

验证要求：
1. 完成后运行必要验证命令，至少包括：
   - git status
   - 检查 Markdown 中引用的关键路径存在
   - 运行项目测试或与文档相关的验证测试
2. 如果新增/更新文档一致性测试，测试必须真实检查关键路径或指导书内容，不要只做无意义断言。
3. 每完成一个轮次都要提交并推送 GitHub。

分支与收尾要求：
1. 按 implementation harness 创建独立分支运行。
2. 管线完成后，最终可由主 Agent 将成果 fast-forward 合回 main，并删除临时远程分支，保持远程只剩 main。
