# 详细设计（v1）

## 概述
本轮设计目标是在项目根目录新增 GitHub Actions 工作流 `.github/workflows/ci.yml`，为 `java-ai-assistant` 子项目建立最小可用 CI / 发布门禁。工作流在后续提交和拉取请求时自动运行，使用 Java 17 和 Maven 缓存，并在 `java-ai-assistant` 目录内分别执行构建与测试命令。

设计范围仅包含 CI 工作流文件；不新增 Java 源码、测试源码、发布、部署、制品上传、tag release 或复杂矩阵构建。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `.github/workflows/ci.yml` | 新建 | 定义 GitHub Actions CI，触发 push / pull_request，设置 JDK 17 和 Maven 缓存，执行 Maven build 和 test。 |

## 类型定义

### CI Workflow
**形态**：GitHub Actions workflow YAML
**包路径**：不适用
**职责**：为 `java-ai-assistant` 提供基础提交门禁，自动验证 Maven 构建与单元测试。

**顶层配置**：
- `name`：`CI`
- `on`：
  - `push`
  - `pull_request`
- `jobs`：
  - `build`

**job 定义**：
- `jobs.build.name`：`Build and test`
- `jobs.build.runs-on`：`ubuntu-latest`
- `jobs.build.defaults.run.working-directory`：`java-ai-assistant`

**step 定义及依赖动作**：
- `Checkout`：
  - `uses`：`actions/checkout@v4`
  - 职责：检出当前提交源码。
- `Set up JDK 17`：
  - `uses`：`actions/setup-java@v4`
  - `with.distribution`：`temurin`
  - `with.java-version`：`17`
  - `with.cache`：`maven`
  - 职责：安装 Java 17，并启用 Maven 依赖缓存。
- `Build`：
  - `run`：`mvn -B -DskipTests package`
  - 工作目录：继承 `java-ai-assistant`
  - 职责：编译并打包项目，跳过测试以与后续 Test 步骤形成清晰分工。
- `Test`：
  - `run`：`mvn -B test`
  - 工作目录：继承 `java-ai-assistant`
  - 职责：执行默认 Maven 测试生命周期，由现有 Surefire 配置运行 `**/*Test.java` 并排除 `**/*IT.java`。

**公开接口**：
- GitHub Actions 事件入口：
  - `push`
  - `pull_request`
- CI 命令入口：
  - `mvn -B -DskipTests package`
  - `mvn -B test`

**构造方式**：
- 在项目根目录创建 `.github/workflows/ci.yml`。
- GitHub 在检测到工作流文件后自动注册该 CI workflow。

**类型关系**：
- `.github/workflows/ci.yml` 依赖仓库中的 `java-ai-assistant/pom.xml`。
- `Build` 和 `Test` steps 依赖 `Set up JDK 17` 完成。
- Maven 测试行为由 `java-ai-assistant/pom.xml` 中的 Surefire 配置决定。

## 错误处理
- 工作流不定义自定义错误类型；GitHub Actions 以非零退出码标记 step 和 job 失败。
- `actions/checkout@v4` 失败时，后续步骤不会执行，job 失败。
- `actions/setup-java@v4` 失败时，后续 Maven 命令不会执行，job 失败。
- `mvn -B -DskipTests package` 失败时，job 失败，不继续视为通过。
- `mvn -B test` 失败时，job 失败，阻断对应提交或 PR 的 CI 状态。
- 推送 GitHub 不属于本 YAML 的运行逻辑；本轮收尾阶段必须由 Runner 或等价收尾步骤在本地提交并推送。本环境无法推送时，验证报告必须记录失败原因、当前分支、远端配置和未推送状态。

## 行为契约
- 前置条件：
  - 仓库托管于 GitHub 或兼容 GitHub Actions 的环境。
  - `java-ai-assistant/pom.xml` 存在。
  - 项目使用 Java 17；`pom.xml` 中 `maven.compiler.release` 为 `17`。
  - CI runner 可访问 Maven Central 下载依赖。
- 触发规则：
  - 任意分支发生 `push` 时运行。
  - 任意目标分支收到或更新 `pull_request` 时运行。
- 执行顺序：
  1. checkout 当前提交。
  2. 设置 Temurin JDK 17，并启用 Maven cache。
  3. 在 `java-ai-assistant` 工作目录执行 `mvn -B -DskipTests package`。
  4. 在 `java-ai-assistant` 工作目录执行 `mvn -B test`。
- 后置条件：
  - 所有步骤成功时，GitHub Actions job 状态为成功。
  - 任一步骤失败时，GitHub Actions job 状态为失败。
  - 默认 CI 不运行 integration profile，不要求 `DEEPSEEK_API_KEY`，不访问真实 DeepSeek 服务。
- 状态变化规则：
  - 本轮仅新增 `.github/workflows/ci.yml`。
  - 不修改 `java-ai-assistant/pom.xml`、README、Java 源码或测试源码。
  - 不生成或提交 `java-ai-assistant/target/`。

## 依赖关系
- 依赖现有项目结构：
  - `/root/exp_SWAT/java-ai-assistant/pom.xml`
  - Maven 单模块项目。
  - Java release 17。
- 依赖现有测试配置：
  - `maven-surefire-plugin` 运行 `**/*Test.java`。
  - `maven-surefire-plugin` 排除 `**/*IT.java`。
  - `integration` profile 通过 Failsafe 提供可选集成测试入口，但本轮 CI 不启用。
- 依赖 GitHub Actions 官方动作：
  - `actions/checkout@v4`
  - `actions/setup-java@v4`
- 暴露给后续任务的公开接口：
  - 工作流文件路径：`.github/workflows/ci.yml`
  - 基础 CI 名称：`CI`
  - CI job 名称：`Build and test`
  - 本地验证命令：
    - 在 `/root/exp_SWAT/java-ai-assistant` 执行 `mvn -B -DskipTests package`
    - 在 `/root/exp_SWAT/java-ai-assistant` 执行 `mvn -B test`
