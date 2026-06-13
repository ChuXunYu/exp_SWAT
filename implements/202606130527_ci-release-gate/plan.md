# 实现计划

任务描述：为 java-ai-assistant 增加基础 CI / 发布门禁，确保后续提交至少自动执行构建和测试。
项目根目录：/root/exp_SWAT

---

## R1 NEW 新增 GitHub Actions Maven CI 工作流
任务：新增 `.github/workflows/ci.yml`，为 `java-ai-assistant` 配置最小可用 GitHub Actions CI，包含 checkout、JDK 17 设置、Maven 依赖缓存、构建和测试步骤，并使用本地可运行的 Maven 命令验证。
选择理由：需求核心是建立提交门禁；项目已明确为单模块 Maven Java 17 应用，且没有现有 CI 工作流，因此首轮应直接补齐最小 CI 入口。
上下文：`java-ai-assistant/pom.xml` 使用 Maven、Java 17、JUnit Jupiter、Mockito、Surefire/Failsafe、JaCoCo；`java-ai-assistant/README.md` 记录构建命令 `mvn clean package`、测试命令 `mvn clean test`、验证命令 `mvn clean verify`。仓库当前未发现 `.github/workflows` 或 Maven wrapper。

## R1 RETRY 新增 GitHub Actions Maven CI 工作流
原因：计划审查指出本轮任务未把“每完成一个轮次都要提交并推送 GitHub”落实为完成条件或验证要求。
修正方向：保留新增最小 GitHub Actions Maven CI 的任务范围，同时要求 Runner 或等价收尾步骤在验证通过后提交并推送到 GitHub；若无法推送，必须在验证报告中记录失败原因、当前分支、远端配置和未推送状态。

---

## R2 PASSED 新增 GitHub Actions Maven CI 工作流
结果：新增 `.github/workflows/ci.yml`，为 `java-ai-assistant` 配置 GitHub Actions 基础 CI；工作流包含 checkout、Temurin JDK 17 设置、Maven 依赖缓存、构建步骤和测试步骤。
测试：本地执行 `/root/exp_SWAT/java-ai-assistant` 下的 `mvn clean verify` 通过，Tests run: 960, Failures: 0, Errors: 0, Skipped: 0。
收尾：已提交并推送到 `origin/202606130527_ci-release-gate`，提交为 `e4446d5 v1 done`。
