# 任务指令（v1）

## 动作
NEW

## 任务描述
建立 `java-ai-assistant/` Maven 单模块工程骨架，并实现首批通用结果与错误基础类型。

预期交付：
- 新增 `java-ai-assistant/pom.xml`，固定 Java release 为 17。
- 配置依赖：Jackson Databind `2.19.0`、JUnit Jupiter `5.14.4`、Mockito `5.18.0`。
- 配置插件：Maven Surefire `3.5.6`、Maven Failsafe `3.5.6`、JaCoCo Maven Plugin `0.8.13`；默认 `mvn clean test` 只运行单元测试，不访问真实 DeepSeek、网络或 API Key；集成测试通过 `integration` profile 单独运行。
- 建立 Maven 标准目录与基础包：`src/main/java/assistant/common`、`src/main/java/assistant/testability`、`src/test/java/assistant/common`。
- 在 `assistant.common` 实现 `ErrorCode`、`BusinessException`、`OperationResult<T>` 三个紧密相关的基础类型：
  - `ErrorCode` 至少包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT`、`SCHEDULE_CONFLICT`、`AI_NOT_CONFIGURED`、`AI_AUTH_FAILED`、`AI_RATE_LIMITED`、`AI_TIMEOUT`、`AI_BAD_REQUEST`、`AI_REMOTE_UNAVAILABLE`、`AI_NETWORK_ERROR`、`AI_EMPTY_RESPONSE`、`AI_MALFORMED_RESPONSE`、`SYSTEM_ERROR`。
  - `BusinessException` 持有 `ErrorCode` 和消息，拒绝空错误码，消息允许由调用方提供。
  - `OperationResult<T>` 表达成功/失败结果；成功结果可携带 payload，失败结果必须携带 `ErrorCode` 和简短消息；提供便于 JUnit 断言的只读访问方法。
- 增加针对上述基础类型的 JUnit 单元测试，覆盖成功结果、无载荷成功、失败结果、空错误码拒绝、业务异常携带错误码等基础分支。
- 新增 `java-ai-assistant/README.md` 和 `java-ai-assistant/docs/environment.md` 的最小运行说明，明确 `mvn clean test`、`mvn -Pintegration verify`、DeepSeek 配置变量名、无 API Key 时普通单元测试仍可运行。

## 选择理由
这是后续所有模块的构建和测试基础。任务、日程、学习计划、收支、笔记、汇总和 AI 服务都需要统一的错误分类与服务返回语义；先完成 Maven/JUnit/JaCoCo 基线和 `assistant.common` 基础类型，后续每轮才能在稳定命令下增量实现并测试。

## 任务上下文
必须依据以下输入：
- `/root/exp_SWAT/designs-tech/202606111332_java-ai-assistant-tech/tech_v2.md`
- `/root/exp_SWAT/docs/2 design-oo.md`
- `/root/exp_SWAT/docs/1 requirement.md`

关键约束：
- 项目工程目录固定为 `/root/exp_SWAT/java-ai-assistant/`。
- 使用 Java 17 LTS 与 Maven 单模块工程。
- 普通单元测试不得读取真实环境变量、不得访问网络、不得依赖真实 DeepSeek API Key。
- DeepSeek 默认配置后续应保留为 `https://api.deepseek.com`、`/chat/completions`、`deepseek-v4-flash`，但本轮只需在文档中说明配置入口，不实现真实客户端。
- API Key 不得写入源码、测试、README 示例明文或提交材料。
- 每个管线任务完成后由 Runner 执行测试、提交并尝试推送；commit message 必须为中文。若无远端或推送失败，需要在验证报告中说明。

## 已有代码上下文
当前项目根目录 `/root/exp_SWAT` 主要包含需求、OOD、技术方案、实验说明和实现管线目录；尚未存在 `java-ai-assistant/`、`pom.xml`、`src/main/java` 或 `src/test/java`。本轮应新增独立 Maven 工程，不修改既有需求和设计文档。
