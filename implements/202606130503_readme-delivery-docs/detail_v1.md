# 详细设计（v1）

## 概述
本轮只完善 `java-ai-assistant` 的交付入口文档，使新用户能够基于真实项目结构完成理解、构建、测试、运行和配置。实现范围限定为 `java-ai-assistant/README.md` 的内容重写或扩写；如现有文档交付测试因新增 README 章节需要同步断言，只允许在 `DocumentationDeliveryTest` 中最小增补 README 相关断言，不修改业务代码、构建插件、命令入口或测试隔离边界。

README 必须保持与当前代码一致：单模块 Maven 工程，`pom.xml` 声明 Java release 17；运行入口为 `assistant.app.Main`；默认启动加载演示数据；`ASSISTANT_DEMO_DATA=false`、`ASSISTANT_DEMO_DATA=0` 或 `ASSISTANT_DEMO_DATA=no` 关闭演示数据；控制台主菜单包含汇总、任务、日程、学习计划、收支、笔记、AI 问答、AI 草稿、帮助和退出；AI 配置变量为 `DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`、`DEEPSEEK_TIMEOUT_SECONDS`；默认 DeepSeek base URL 为 `https://api.deepseek.com`，path 为 `/chat/completions`，默认模型为 `deepseek-v4-flash`；默认单元测试不访问真实 DeepSeek、网络或 API Key；当前 `src/test/java` 下没有 `*IT.java`。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/README.md` | 修改 | 作为交付入口文档，补齐项目简介、功能清单、环境要求、构建/测试/运行/配置说明、常见工作流和已知限制。 |
| `java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java` | 可选修改 | 仅在需要保护新增 README 交付章节时，最小范围增补 README 内容断言；不得删除或放宽现有测试文档、覆盖率命令、集成测试边界和 944 tests 基线断言。 |

## 类型定义

本任务不新增 Java 类型、接口、枚举、异常、配置类或数据结构。

### README 文档结构
**形态**：Markdown document
**包路径**：不适用
**职责**：面向新用户的项目交付入口。

**章节签名定义**：

- `# Java AI Assistant`
- `## Project Overview`
- `## Features`
- `## Requirements`
- `## Build`
- `## Tests`
- `## Unit Tests`
- `## Integration Tests`
- `## Coverage`
- `## Run`
- `## Configuration`
- `## Common Workflows`
- `## Known Limitations`
- `## Test Documentation`

**公开接口**：不适用。
**构造方式**：由编码阶段直接编辑 Markdown 文件。
**类型关系**：README 引用现有 `docs/test-plan.md`、`docs/test-cases.md`、`docs/defect-regression.md`、`docs/coverage/README.md`、`docs/environment.md`。

### DocumentationDeliveryTest README 断言
**形态**：JUnit Jupiter test method assertions
**包路径**：`assistant.docs`
**职责**：保护 README 中真实交付命令、测试边界和配置说明。

**现有方法签名**：

- `void readmeDocumentsTestDeliverablesCoverageCommandIntegrationBoundaryAndBaseline()`

**允许的断言增补项**：

- README 必须包含 `## Project Overview`。
- README 必须包含 `## Features`。
- README 必须包含 `## Requirements`。
- README 必须包含 `## Build`。
- README 必须包含 `## Run`。
- README 必须包含 `## Configuration`。
- README 必须包含 `## Common Workflows`。
- README 必须包含 `## Known Limitations`。
- README 必须包含 `assistant.app.Main`。
- README 必须包含 `ASSISTANT_DEMO_DATA`。

**禁止的断言变更**：

- 不得移除对 `## Unit Tests` 或默认测试命令的保护。
- 不得移除或放宽 `mvn clean test`、`mvn clean verify`、`mvn jacoco:report`、`mvn -Pintegration verify`、`target/site/jacoco/index.html` 的断言。
- 不得移除或放宽 `passed 944 tests with 0 failures`、`does not contain \`*IT.java\` classes`、`does not mean that a real DeepSeek connectivity test already exists`、`DEEPSEEK_API_KEY` 的断言。

**公开接口**：无新增公开接口。
**构造方式**：在现有测试方法内追加 `assertContainsAll(readme, List.of(...))` 或扩展现有列表。
**类型关系**：依赖现有 `assertContainsAll(String text, List<String> expectedValues)` helper。

## README 内容规格

### Project Overview
必须说明 `Java AI Assistant` 是个人学习与生活助手控制台应用；项目是单模块 Maven Java 17 工程；数据当前通过内存服务和仓储管理，适合本地演示、开发和测试；README 中命令均从 `java-ai-assistant/` 目录执行。

### Features
必须列出当前控制台可达能力，至少包含：

- 汇总：展示今日任务、今日日程、本周学习计划、本月收入/支出/结余、笔记和标签统计。
- 任务：列表、新增、查看、筛选、修改、完成、撤销完成、删除。
- 日程：列表、新增、查看、筛选、修改、完成、取消、删除。
- 学习计划：列表、新增、查看、筛选、修改、进度更新、删除。
- 收支：列表、新增、查看、筛选、修改、删除、统计。
- 笔记：列表、新增、查看、筛选、修改、删除。
- AI 问答：基于本地摘要上下文调用 DeepSeek 客户端；未配置 API Key 时返回未配置错误而不是访问真实服务。
- AI 草稿：查看、确认、取消结构化建议草稿，并支持导入任务或学习计划。

功能清单不得声称存在账号、数据库、文件导出、系统通知、真实提醒推送、健康管理或联系人管理。

### Requirements
必须写明 Java 17、Maven、JUnit Jupiter 5.14.4、Mockito 5.18.0、Maven Surefire Plugin 3.5.6、Maven Failsafe Plugin 3.5.6、JaCoCo Maven Plugin 0.8.13。Maven 以开发机安装版本为准；无需固定 Maven 具体版本。

### Build
必须给出真实构建命令：

```bash
mvn clean package
```

可补充说明该命令会执行默认测试生命周期。不得加入项目不存在的 Maven plugin goal 或 CLI 参数。

### Tests
必须包含 `## Unit Tests`、`## Integration Tests`、`## Coverage` 标题或兼容标题文本。

必须列出命令：

```bash
mvn clean test
mvn clean verify
mvn jacoco:report
mvn -Pintegration verify
```

必须说明默认单元测试不访问真实 DeepSeek、网络资源或 API Key；v28 历史验证记录为 `mvn clean test` passed 944 tests with 0 failures；`mvn -Pintegration verify` 是可选入口；当前仓库 does not contain `*IT.java` classes；该命令 does not mean that a real DeepSeek connectivity test already exists；未来真实 DeepSeek 连通性测试应使用 `integration` profile、`*IT.java` 命名，需要网络和 `DEEPSEEK_API_KEY`；JaCoCo HTML 报告路径为 `target/site/jacoco/index.html`；`target/site/jacoco/` 生成文件不提交。

### Run
必须基于真实入口 `assistant.app.Main` 给出至少一个可运行命令。

首选 Maven 运行命令必须不依赖新增插件，可使用 Maven dependency plugin 在运行时构造 classpath：

```bash
mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
mvn -q -DskipTests compile
java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

还必须给出非交互 smoke 形式，便于验证启动入口后立即退出：

```bash
printf 'q\n' | java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

如 README 额外提供关闭演示数据启动命令，只能使用环境变量或 system property，例如：

```bash
ASSISTANT_DEMO_DATA=false java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
java -DASSISTANT_DEMO_DATA=false -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

不得编造 `--demo-data`、`--api-key`、`--model`、`--help` 等不存在的 CLI 参数。不得声称 jar 已配置 `Main-Class`，因为当前 `pom.xml` 没有 jar manifest 主类配置。

### Configuration
必须覆盖：

| 配置名 | 来源 | 默认值 | 行为 |
|-------|------|--------|------|
| `DEEPSEEK_API_KEY` | 环境变量或 Java system property | 空字符串 | 为空时 AI 问答返回未配置错误；默认测试不需要。 |
| `DEEPSEEK_BASE_URL` | 环境变量或 Java system property | `https://api.deepseek.com` | DeepSeek base URL。 |
| `DEEPSEEK_MODEL` | 环境变量或 Java system property | `deepseek-v4-flash` | DeepSeek 模型名。 |
| `DEEPSEEK_TIMEOUT_SECONDS` | 环境变量或 Java system property | 代码默认 timeout | 必须是正整数秒；非法时应用回退到无 API Key 默认配置。 |
| `ASSISTANT_DEMO_DATA` | 环境变量或 Java system property | 启用 | `false`、`0`、`no` 关闭演示数据，其他值或未设置启用。 |

必须说明 API Key 不得提交到源码、测试或文档示例。

### Common Workflows
必须至少包含：首次运行；运行默认测试；默认验证；生成覆盖率报告；配置真实 AI；关闭演示数据运行。

### Known Limitations
必须至少包含：单用户控制台应用；当前业务数据为内存数据，进程退出后不持久化；没有数据库、文件导出、真实系统通知或后台提醒服务；默认测试和默认验证不访问真实 DeepSeek；当前没有 `*IT.java` 真实 DeepSeek 连通性测试；AI 真实调用依赖外部网络、DeepSeek 服务可用性和有效 API Key；README 不声明具体覆盖率百分比。

### Test Documentation
必须保留现有链接：

- `[Test plan](docs/test-plan.md)`
- `[White-box test cases](docs/test-cases.md)`
- `[Defect and regression record](docs/defect-regression.md)`
- `[Coverage evidence notes](docs/coverage/README.md)`
- `[Environment](docs/environment.md)`

## 错误处理
本任务不引入运行时错误类型。文档层面的错误处理策略如下：

- 若发现 README 中已有命令与当前 `pom.xml` 或入口类不一致，应以真实代码为准修正文档。
- 若新增 README 章节导致 `DocumentationDeliveryTest` 失败，应优先调整 README 保留现有硬性文本；只有在确需保护新增交付章节时才最小增补测试断言。
- 不得通过删除测试、放宽断言或降低命令真实性要求来规避失败。
- 不得伪造本轮未执行的测试数量、覆盖率百分比、DeepSeek 连通性结果或 GitHub 推送结果。

## 行为契约

### 编码阶段契约
- 所有命令说明均假设当前目录为 `java-ai-assistant/`。
- README 提供的运行命令必须能在当前 Maven 工程中运行，不依赖未配置的 exec plugin 或 jar manifest。
- README 不能新增项目实际不支持的 CLI 参数。
- README 中对 AI 的描述必须区分“默认测试隔离”和“用户设置真实 API Key 后可发起真实调用”。
- README 必须保留现有文档交付测试需要的英文短语，尤其是 `passed 944 tests with 0 failures`、`does not contain \`*IT.java\` classes` 和 `does not mean that a real DeepSeek connectivity test already exists`。

### 验证阶段契约
后续 Runner 至少执行：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn test
mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
mvn -q -DskipTests compile
printf 'q\n' | java -cp "target/classes:$(cat target/classpath.txt)" assistant.app.Main
```

若 README 修改了覆盖率或 verify 相关说明，建议额外执行：

```bash
cd /root/exp_SWAT/java-ai-assistant
mvn clean verify
mvn jacoco:report
```

### 提交与推送收尾契约
后续 Runner 完成提交后，编排收尾必须在项目根目录执行并记录：

```bash
cd /root/exp_SWAT
git remote -v
git branch --show-current
git push -u origin 202606130503_readme-delivery-docs
```

预期远端为 `origin https://github.com/ChuXunYu/exp_SWAT.git`，预期分支为 `202606130503_readme-delivery-docs`。若认证、权限或网络导致推送失败，最终交付必须报告失败原因和待执行命令，不得声称已推送。

## 依赖关系
- `java-ai-assistant/pom.xml`：Java release、依赖版本、Surefire/Failsafe/JaCoCo 配置、integration profile。
- `assistant.app.Main`：真实启动入口、`ASSISTANT_DEMO_DATA` 行为。
- `assistant.app.ConsoleApplication`：主菜单和控制台功能清单。
- `assistant.app.ApplicationFactory`：读取环境变量和 system properties，创建 DeepSeek 客户端及内存服务。
- `assistant.ai.AiConfigurationLoader`：DeepSeek 配置变量、默认值、timeout 校验。
- `assistant.ai.AiConfiguration`：默认 base URL、path、model、timeout。
- `assistant.ai.AiAssistantService`：缺少 API Key 时返回 `AI_NOT_CONFIGURED`。
- `java-ai-assistant/docs/environment.md`：测试工具、隔离边界和环境说明口径。
- `java-ai-assistant/docs/test-plan.md`：核心功能、运行命令、集成测试边界。
- `java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java`：README 交付断言边界。
