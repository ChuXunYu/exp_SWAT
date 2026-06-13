项目根目录：/root/exp_SWAT

任务：
调用审议式执行流程，基于当前完整项目和课程材料，生成一份可拆分给 5 个人分别提交的《软件质量保证与测试》完整实验报告。报告必须参考 /root/exp_SWAT/实验报告模板.md 的结构，综合考虑实验指导书、测试计划、测试用例、验收报告、CI、覆盖率、缺陷与回归等所有相关要求。最终报告要生成所有模块的内容，且内容量和分工足够 5 人分别提交。

最终交付文件：
- /root/exp_SWAT/deliverables/五人拆分版-软件质量保证与测试实验报告.md

可参考已有草稿：
- /root/exp_SWAT/deliverables/五人拆分版-软件质量保证与测试实验报告.md

必须参考材料：
- /root/exp_SWAT/实验报告模板.md
- /root/exp_SWAT/软件质量保证与测试实验指导书.md
- /root/exp_SWAT/实验说明.md
- /root/exp_SWAT/实验报告模板.docx
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

报告内容要求：
1. 按实验报告模板组织完整内容，删除模板中的说明性占位话术。
2. 包含课程封面信息表，可使用“成员A-E/姓名学号待替换”占位。
3. 包含五人小组分工表，且每个人负责的模块、测试计划、测试用例、结果分析和个人总结要明显不同。
4. 包含实验目的。
5. 包含实验1测试计划：系统功能概述、测试目标、测试范围、测试环境、测试工具、测试策略。
6. 包含实验2测试计划：测试目标、测试范围、测试环境、测试工具、测试策略。
7. 包含实验1白盒测试用例及结果分析，覆盖 AI、AI 草稿、任务、日程、学习计划、收支、笔记、汇总、控制台、文档与 CI。
8. 包含实验2黑盒/系统/验收测试用例及结果分析，覆盖用户工作流、异常输入、自动化测试、CI 和风险边界。
9. 包含缺陷记录与回归分析，至少给出可直接使用的缺陷记录样例和回归测试范围。
10. 包含质量度量，引用真实验收记录：2026-06-13 `mvn clean test` 952 个测试通过，失败 0；JaCoCo 指令覆盖 96.78%、分支覆盖 86.65%、行覆盖 94.55%。必须标注这些数字来自历史验收记录，现场复核以当前命令输出为准。
11. 包含五人拆分提交建议。
12. 包含五个人不同侧重点的实验总结。
13. 包含成绩评定表和自评说明。
14. 不要声称真实 DeepSeek 网络集成测试已经通过；必须说明当前 `mvn -Pintegration verify` 通过但无 `*IT.java`，真实 DeepSeek 连通性是风险边界。
15. 不要编造不存在的功能、路径、类名或测试结果。
16. 不修改 .doc/.docx 二进制文件。

执行要求：
1. 可以修订已有草稿或重写最终 Markdown。
2. 产出后检查最终文件存在、章节完整、五人分工和五份总结存在。
3. 输出执行摘要到本审议流程的 output_vN.md。
4. 如修改了最终交付文件，可保持未提交；本任务重点是生成报告，不要求 git commit。
