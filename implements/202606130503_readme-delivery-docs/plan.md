# 实现计划

任务描述：整理 `java-ai-assistant` 的交付文档，使新用户可以基于真实代码和可运行命令理解、构建、测试、运行和配置项目；重点更新或新增 README，覆盖项目简介、功能清单、环境要求、构建/测试/运行命令、配置说明、常见工作流示例和已知限制；完成后运行必要验证命令，并由后续 Runner 提交并推送 GitHub。
项目根目录：/root/exp_SWAT

---

## R1 NEW 审计并完善 java-ai-assistant README 交付文档
任务：基于真实项目结构、`pom.xml`、控制台入口、AI 配置加载器、现有测试文档和当前 README，更新 `java-ai-assistant/README.md` 为新用户可直接使用的交付文档；必要时同步最小范围文档测试期望，预期文件路径包括 `java-ai-assistant/README.md`，如现有文档交付测试对 README 章节有硬性断言，可同步更新 `java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java` 中与 README 文案相关的断言。
选择理由：当前 README 已包含测试、覆盖率和 DeepSeek 配置的部分信息，但缺少完整的新用户交付路径，尤其是项目简介、8 个功能清单、构建命令、可执行运行命令、演示数据开关、常见工作流和已知限制。该任务是文档交付需求的核心，可一次性把交付入口整理到 README，并用现有文档测试和 Maven 命令验证文档没有脱离真实代码。
上下文：项目主体位于 `/root/exp_SWAT/java-ai-assistant`，是单模块 Maven Java 17 工程，`pom.xml` 配置 Jackson 2.19.0、JUnit Jupiter 5.14.4、Mockito 5.18.0、Surefire/Failsafe 3.5.6 和 JaCoCo 0.8.13；主类为 `assistant.app.Main`，默认加载演示数据，`ASSISTANT_DEMO_DATA=false|0|no` 可关闭演示数据；控制台主菜单包含汇总、任务、日程、学习计划、收支、笔记、AI 问答和 AI 草稿；AI 配置通过 `DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`、`DEEPSEEK_TIMEOUT_SECONDS`，默认 base URL 为 `https://api.deepseek.com`、path 为 `/chat/completions`、模型为 `deepseek-v4-flash`；普通单元测试不得访问真实 DeepSeek、网络、API Key 或真实当前时间；现有 README 和 `DocumentationDeliveryTest` 已断言测试文档、覆盖率命令、集成测试边界和 944 个测试基线等交付内容。

## R1 RETRY 审计并完善 java-ai-assistant README 交付文档
原因：计划审查指出原计划把“每完成一个轮次都要提交并推送 GitHub”表述为由后续 Runner 完成，但当前 Runner 角色只负责 `git add -A && git commit -m "v{N} done"`，不会执行 `git push`，存在交付责任缺口。
修正方向：本轮任务继续聚焦 README 交付文档，但明确 GitHub 推送不是 Runner 自动动作；Coder/Verifier/Runner 不得声称已完成推送。Runner 完成提交后，管线外的编排收尾必须在项目根目录检查远端与当前分支，并执行 `git push -u origin 202606130503_readme-delivery-docs`，当前已确认远端为 `origin https://github.com/ChuXunYu/exp_SWAT.git`，当前分支为 `202606130503_readme-delivery-docs`。若推送因认证或网络失败，应在最终交付中明确报告失败原因和待执行命令，而不是把该需求标记为已完成。
