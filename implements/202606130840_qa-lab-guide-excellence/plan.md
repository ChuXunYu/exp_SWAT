# 实现计划

任务描述：基于当前真实 `java-ai-assistant` 项目，系统修订 `/root/exp_SWAT/软件质量保证与测试实验指导书.md`，使其成为可直接用于《软件质量保证与测试》课程实验、可复现实验步骤、可支撑评分的 Markdown 指导书；必要时小幅同步补充 `java-ai-assistant/docs/test-plan.md` 或 `java-ai-assistant/docs/test-cases.md`，不修改 `.doc`/`.docx` 二进制文件。
项目根目录：/root/exp_SWAT

---

## R1 NEW 修订课程实验指导书主体
任务：文档修订，预期修改 `/root/exp_SWAT/软件质量保证与测试实验指导书.md`，必要时小幅补充 `/root/exp_SWAT/java-ai-assistant/docs/test-plan.md` 或 `/root/exp_SWAT/java-ai-assistant/docs/test-cases.md`。
选择理由：当前指导书仍是泛化的实验 1/实验 2 说明，未围绕已完成的 `java-ai-assistant` 项目、真实测试资产、CI、缺陷分析、验收评价和评分标准展开；主交付物是课程指导书，应先完成主体结构和真实项目映射。
上下文：项目是 Java 17 + Maven 单模块控制台应用，入口为 `assistant.app.Main`，核心模块包括 AI、任务、日程、学习计划、收支、笔记、汇总统计；已有测试计划、白盒测试用例、README、CI 工作流、验收报告和大量 JUnit 测试类可作为真实依据。

---

## R2 PASSED 修订课程实验指导书主体
结果：已将 `/root/exp_SWAT/软件质量保证与测试实验指导书.md` 修订为围绕真实 `java-ai-assistant` 项目的课程实验指导书，覆盖实验基本信息、实验环境、项目简介、被测/不测范围、质量目标、测试策略、测试计划、用例设计、单元/集成/系统验收测试、CI、缺陷分析、质量度量、报告要求、100 分评分标准和附录；同步小幅修订 `/root/exp_SWAT/java-ai-assistant/docs/test-plan.md` 与 `/root/exp_SWAT/java-ai-assistant/docs/test-cases.md` 的测试数量口径，并更新 `/root/exp_SWAT/java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java`。
测试：`cd /root/exp_SWAT/java-ai-assistant && mvn test` 通过，验证报告记录 964 个测试通过、0 失败。
