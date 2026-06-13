# 任务指令（v1）

## 动作
NEW

## 任务描述
审计并完善 `java-ai-assistant/README.md`，使新用户可以直接理解、构建、测试、运行和配置项目。README 至少必须包含：

- 项目简介。
- 功能清单，覆盖当前控制台可用的汇总、任务、日程、学习计划、收支、笔记、AI 问答、AI 草稿等能力。
- 环境要求，基于 `pom.xml` 和现有文档写明 Java 17、Maven 以及测试/覆盖率相关工具口径。
- 构建命令。
- 测试命令，包含默认单元测试、默认验证、覆盖率报告和可选 integration profile，并明确默认测试不访问真实 DeepSeek、网络或 API Key。
- 运行命令，必须基于真实可执行入口 `assistant.app.Main` 给出可运行的 Maven 或 Java 命令；不要编造不存在的 CLI 参数。
- 配置说明，覆盖 `DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`、`DEEPSEEK_TIMEOUT_SECONDS` 和 `ASSISTANT_DEMO_DATA`。
- 常见工作流示例，例如首次运行、运行测试、生成覆盖率、配置真实 AI、关闭演示数据运行。
- 已知限制，例如单用户、内存数据、无账号/数据库/真实系统通知、默认没有 `*IT.java` 真实 DeepSeek 连通性测试等。

如现有 `DocumentationDeliveryTest` 对 README 内容存在硬性断言且需要适配新增交付章节，可在最小范围内同步更新该测试中 README 相关断言；不得放宽测试对真实命令、测试文档链接、覆盖率命令和集成测试边界的约束。

## 选择理由
本任务直接覆盖交付文档整理需求。当前 README 已有测试、覆盖率和 DeepSeek 配置说明，但尚不足以让新用户从零开始完整理解“项目是什么、能做什么、怎样构建、怎样运行、怎样配置、有哪些限制”。先完成 README 交付入口，可在不改业务代码的前提下补齐项目可交付性，并通过现有文档测试和 Maven 命令验证文档基于真实项目。

## 任务上下文
完整需求要求：

- 检查现有 README、项目结构和实际命令。
- 更新或新增 README 文档，至少包含项目简介、功能清单、环境要求、构建命令、测试命令、运行命令、配置说明、常见工作流示例、已知限制。
- 文档必须基于真实代码和可运行命令，不要编造不存在的参数或功能。
- 完成后运行必要的验证命令。
- 每完成一个轮次都要提交并推送 GitHub，提交与推送由后续 Runner 执行。

## 已有代码上下文
项目主体为 `/root/exp_SWAT/java-ai-assistant`：

- `pom.xml` 是单模块 Maven 工程，Java release 为 17，依赖 Jackson Databind 2.19.0、JUnit Jupiter 5.14.4、Mockito 5.18.0，插件包括 Maven Surefire 3.5.6、Failsafe 3.5.6、JaCoCo 0.8.13。
- 主类是 `assistant.app.Main`，启动后创建 `ApplicationServices` 并运行 `ConsoleApplication`。
- `Main` 默认加载演示数据；`ASSISTANT_DEMO_DATA=false`、`0` 或 `no` 可以关闭演示数据。该开关不是命令行参数。
- 控制台主菜单包含：`1. 汇总`、`2. 任务`、`3. 日程`、`4. 学习计划`、`5. 收支`、`6. 笔记`、`7. AI 问答`、`8. AI 草稿`、`h. 帮助`、`q. 退出`。
- AI 配置加载器使用 `DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`、`DEEPSEEK_TIMEOUT_SECONDS`；默认 base URL 为 `https://api.deepseek.com`，path 为 `/chat/completions`，默认模型为 `deepseek-v4-flash`。
- 当前 README 已包含 `mvn clean test`、`mvn -Pintegration verify`、`mvn clean verify`、`mvn jacoco:report`、测试文档链接、覆盖率报告路径和“944 tests with 0 failures”的历史验证记录。
- `src/test/java/assistant/docs/DocumentationDeliveryTest.java` 已检查 README 中的测试文档、覆盖率命令、integration profile 边界、`DEEPSEEK_API_KEY` 和 944 个测试基线等内容。

实现时请优先阅读：

- `java-ai-assistant/README.md`
- `java-ai-assistant/pom.xml`
- `java-ai-assistant/src/main/java/assistant/app/Main.java`
- `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiConfigurationLoader.java`
- `java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java`
- `java-ai-assistant/docs/environment.md`
- `java-ai-assistant/docs/test-plan.md`

建议验证命令：

- 在 `java-ai-assistant/` 目录运行 `mvn test`，确保文档测试仍通过。
- 如 README 中新增或确认运行命令，至少用非交互输入 smoke 验证启动入口，例如向程序输入 `q` 后退出，避免文档给出不可执行命令。

## 修订说明（v1 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| 计划和任务文件把“每完成一个轮次都要提交并推送 GitHub”交给后续 Runner，但 Runner 指令只提交、不推送，导致需求无可执行责任人。 | 保留 README 文档任务本身，同时明确提交由 Runner 按既有管线完成；GitHub 推送不是 Runner 自动能力，必须作为管线外编排收尾动作执行。当前项目远端为 `origin https://github.com/ChuXunYu/exp_SWAT.git`，当前分支为 `202606130503_readme-delivery-docs`；Runner 提交后应在项目根目录执行 `git push -u origin 202606130503_readme-delivery-docs`。若认证或网络导致失败，应在最终交付中报告失败原因和待执行命令，不得声称已推送。 |

## 交付与推送约束
本任务的实现、设计和验证环节只需围绕 README 交付文档和必要验证命令展开，不要在业务代码中制造推送逻辑，也不要修改 Runner 角色指令。后续 Runner 仍只负责按管线提交本轮变更；提交之后，编排收尾必须显式执行并记录：

- `git remote -v` 确认 `origin` 指向 `https://github.com/ChuXunYu/exp_SWAT.git`。
- `git branch --show-current` 确认当前分支为 `202606130503_readme-delivery-docs`。
- `git push -u origin 202606130503_readme-delivery-docs` 推送本轮提交到 GitHub。

若推送不可执行或失败，最终交付状态必须写明失败原因，并给出上述待执行命令；不得使用“Runner 会推送”或“已自动推送”这类与当前管线能力不一致的表述。
