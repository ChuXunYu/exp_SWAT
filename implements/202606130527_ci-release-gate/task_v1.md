# 任务指令（v1）

## 动作
NEW

## 任务描述
新增 `.github/workflows/ci.yml`，为 `java-ai-assistant` 配置基础 GitHub Actions CI / 发布门禁。工作流需要在后续提交和拉取请求时自动运行，至少包含 checkout、JDK 设置、依赖缓存、build、test。

预期文件路径：
- `/root/exp_SWAT/.github/workflows/ci.yml`

## 选择理由
这是任务要求的核心交付物。项目是单模块 Maven Java 17 应用，当前没有 GitHub Actions 工作流；先建立最小可用 CI，能让后续提交自动执行构建和测试，避免引入复杂发布流程。

## 任务上下文
需求要求：
- 识别项目构建工具和现有测试命令。
- 如果项目适合 GitHub Actions，则新增或更新 `.github/workflows/ci.yml`。
- CI 至少包含 checkout、JDK 设置、dependency cache 如适用、build、test。
- 不引入复杂发布流程，保持最小可用。
- 本地验证 CI 中使用的命令可以运行。
- 每完成一个轮次都要提交并推送 GitHub。

本轮实现建议：
- 使用 `actions/checkout`。
- 使用 `actions/setup-java` 设置 Java 17，并启用 Maven cache。
- 将工作目录设置为 `java-ai-assistant`，因为 `pom.xml` 位于该目录下。
- 使用 Maven 系统安装命令，不假设存在 `mvnw`。
- 明确拆分 build 和 test 步骤，例如 `mvn -B -DskipTests package` 和 `mvn -B test`，满足 CI 中独立 build/test 的可读性要求。
- 不加入发布、制品上传、部署、tag release 等复杂流程。

## 已有代码上下文
项目根目录 `/root/exp_SWAT` 下的应用目录为 `/root/exp_SWAT/java-ai-assistant`。

相关构建信息：
- `/root/exp_SWAT/java-ai-assistant/pom.xml` 是 Maven 单模块项目。
- `maven.compiler.release` 为 `17`。
- 测试依赖包括 JUnit Jupiter、Mockito。
- Surefire 配置运行 `**/*Test.java`，排除 `**/*IT.java`。
- Failsafe integration profile 是可选入口，当前默认 CI 不需要引入。
- README 记录：
  - build: `mvn clean package`
  - unit tests: `mvn clean test`
  - verification: `mvn clean verify`

本轮本地验证应至少运行 CI 中实际使用的 Maven 命令，优先在 `/root/exp_SWAT/java-ai-assistant` 下执行。

## 修订说明（v1 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| 任务未把“每完成一个轮次都要提交并推送 GitHub”作为本轮完成条件或验证要求，后续环节可能漏掉推送远端。 | 将本轮完成条件明确扩展为：实现并验证通过后，Runner 或等价收尾步骤需要提交本轮改动并推送到 GitHub 远端；若当前环境无法推送，必须在验证报告中记录推送失败原因、当前分支、远端配置和未完成状态，避免静默遗漏该需求。 |

## 轮次完成要求
本轮除新增 CI 工作流和本地验证 CI 命令外，还必须在验证/收尾阶段执行提交并推送到 GitHub。若推送因认证、网络、远端配置或权限问题失败，不应视为已完整满足该需求；验证报告需要明确记录失败原因、当前分支、远端状态，以及是否仍有未推送提交。
