# 测试报告（v1）

## 测试编写概述
基于详细设计 `detail_v1.md`、实现报告 `code_v1.md` 和测试审查意见 `test_review_v1_r1.md`，本轮修订 GitHub Actions CI / 发布门禁工作流交付测试。测试从纯文本行匹配改为受控 YAML 结构解析后断言关键配置层级，聚焦 `.github/workflows/ci.yml` 的公开配置契约，不验证 GitHub Actions 内部实现细节，不修改生产源码。

## 新增/修改测试文件
| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `java-ai-assistant/src/test/java/assistant/docs/CiWorkflowDeliveryTest.java` | 修改 | 读取仓库根目录 `.github/workflows/ci.yml`，解析 YAML 层级结构，验证 CI workflow 的触发、job、JDK、缓存、工作目录和 Maven 命令契约均位于设计要求的父子节点下。 |

## 测试用例覆盖
| 测试方法 | 覆盖契约 |
|---------|----------|
| `workflowFileExistsAtGithubActionsPathAndDeclaresCiWorkflow` | 验证工作流文件位于 `.github/workflows/ci.yml`，顶层 `name == CI`，顶层 `on` 包含 `push`，顶层 `jobs` 包含 `build`；同时约束本轮最小 CI 不包含 release 自动化或 artifact 上传。 |
| `workflowRunsForPushAndPullRequestEvents` | 通过结构化 `on` 节点验证工作流对 `push` 和 `pull_request` 事件生效。 |
| `buildJobUsesUbuntuAndRunsInsideJavaAiAssistantModule` | 通过 `jobs.build` 节点验证 job 名称、`ubuntu-latest` runner，以及 `jobs.build.defaults.run.working-directory == java-ai-assistant`。 |
| `workflowChecksOutCodeAndConfiguresTemurinJava17WithMavenCache` | 通过 `jobs.build.steps[0..1]` 验证 checkout 和 setup-java 两个 step 的顺序及各自配置，确认 Temurin、Java 17、Maven cache 属于 setup-java step。 |
| `workflowBuildsBeforeRunningDefaultUnitTests` | 通过 `jobs.build.steps[2..3]` 验证 Build step 先于 Test step，两个 Maven 命令各出现一次且属于对应 step，默认 CI 不启用 `-Pintegration`。 |

## 覆盖维度
- 正常路径：CI 文件存在，push / pull_request 触发，`jobs.build` 下 checkout、JDK 设置、构建、测试步骤完整。
- 边界条件：测试从 Maven 模块目录或仓库根目录运行时均可定位仓库根 workflow。
- 错误路径：缺失关键节点、错误缩进、关键配置放入错误父节点、step 顺序错误、未配置 Java 17 / Maven cache、错误启用 integration profile 时测试失败。
- 状态交互：验证 Build step 在 Test step 之前，并验证 setup-java 的 `with` 配置不会被其他 step 或错误层级冒充。

## 审查意见处理
- 已采纳 `test_review_v1_r1.md` 的修改要求：关键契约验证不再依赖任意位置子串匹配，而是先将本工作流使用到的 YAML 子集解析为 `Map` / `List` 结构，再从 `on`、`jobs.build`、`jobs.build.defaults.run`、`jobs.build.steps` 等结构节点读取并断言。
- 未新增外部测试依赖，避免为单个交付测试修改 Maven 依赖图；解析器限定于本工作流需要的 mapping、sequence-of-mapping、quoted scalar 子集，足以覆盖本轮设计契约。

## 执行说明
为验证本次修订没有编译或断言错误，已在 `/root/exp_SWAT/java-ai-assistant` 执行 `mvn -B -Dtest=assistant.docs.CiWorkflowDeliveryTest test`，结果通过：Tests run: 5, Failures: 0, Errors: 0, Skipped: 0。
