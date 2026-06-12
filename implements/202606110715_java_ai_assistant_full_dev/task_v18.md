# 任务指令（v18）

## 动作
NEW

## 任务描述
新增 AI 模块的配置值对象、配置加载器、客户端请求响应契约、提示词构造器、本地上下文提供接口和 AI 问答应用服务基础，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/ai/AiConfiguration.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiConfigurationLoader.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiRole.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiMessage.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiRequest.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiResponse.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiClient.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiScenario.java`
- `java-ai-assistant/src/main/java/assistant/ai/ContextProvider.java`
- `java-ai-assistant/src/main/java/assistant/ai/PromptBuilder.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiAssistantService.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiConfigurationTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiConfigurationLoaderTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiMessageTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiRequestTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiResponseTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/PromptBuilderTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiAssistantServiceTest.java`

本轮不实现 `DeepSeekAiClient`、真实 HTTP 调用、Jackson 响应解析、结构化建议解析、草稿生命周期、导入服务或控制台菜单。

## 选择理由
v17 已完成 `assistant.summary` 的跨模块摘要与 `LocalContext`，AI 问答、AI 笔记摘要、结构化建议、DeepSeek HTTP 客户端和后续控制台 AI 菜单都需要稳定的 AI 配置、内部消息 DTO、提示词构造、AI 客户端接口和服务层错误传播。本轮先完成不访问网络的 AI 基础编排，能在普通单元测试中用 fake/stub `AiClient` 和 `ContextProvider` 覆盖成功、配置缺失、上下文失败和客户端失败路径。

## 任务上下文
依据技术方案：

- 真实 AI 客户端后续实现为 `DeepSeekAiClient`，业务层只依赖 `AiClient` 接口。
- 默认配置：base URL `https://api.deepseek.com`，path `/chat/completions`，model `deepseek-v4-flash`，timeout 20 秒，stream 固定 `false`。
- 配置名固定为 `DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`、`DEEPSEEK_TIMEOUT_SECONDS`。
- API Key 不得写入源码、`pom.xml`、JUnit 测试、README 示例明文或提交材料；缺失时 AI 服务返回 `AI_NOT_CONFIGURED`。
- `PromptBuilder` 将场景、用户问题和本地摘要组装为 messages；结构化场景要求模型返回单个 JSON 对象，业务类型只允许 `TASK_DRAFT` 和 `STUDY_PLAN_DRAFT`，但本轮只构造提示词，不解析 JSON、不创建草稿。
- AI 失败不允许导致程序崩溃或污染本地业务数据。错误分类使用既有 `ErrorCode`：`AI_NOT_CONFIGURED`、`AI_AUTH_FAILED`、`AI_RATE_LIMITED`、`AI_TIMEOUT`、`AI_BAD_REQUEST`、`AI_REMOTE_UNAVAILABLE`、`AI_NETWORK_ERROR`、`AI_EMPTY_RESPONSE`、`AI_MALFORMED_RESPONSE`。
- 普通单元测试不得读取真实环境变量、不得访问网络、不得依赖真实当前时间或真实 API Key。

本轮建议固定以下公开契约，避免后续实现分叉：

- `AiConfiguration` 使用不可变 record 或 final class，字段至少包含 `baseUrl`、`chatCompletionsPath`、`model`、`apiKey`、`timeout`；构造时清理字符串，base URL、path、model 非空，timeout 必须为正；`apiKey` 允许空白并通过 `hasApiKey()` 判断配置是否可调用。
- `AiConfiguration.defaultWithoutApiKey()` 返回官方默认 base URL、path、model 和 20 秒超时，API Key 为空。
- `AiConfigurationLoader` 提供从 `Map<String, String>` 或等价可控配置源加载的方法；生产入口未来可传入环境变量/JVM 参数，但单元测试不能调用 `System.getenv()` 作为断言依赖。空白覆盖值应回退默认值，非法超时映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)` 或等价稳定失败，不抛未受控运行时异常给调用方。
- `AiRole` 至少包含 `SYSTEM`、`USER`、`ASSISTANT`，并提供 OpenAI 兼容 role 文本。
- `AiMessage` 不可变，包含 `AiRole role` 和非空白 `content`，构造时执行 `strip()`。
- `AiRequest` 不可变，包含 `model`、不可修改 `List<AiMessage> messages`、`boolean stream`；普通问答默认 `stream == false`，messages 非空且元素非空。
- `AiResponse` 不可变，包含非空白 `content`，构造时执行 `strip()`。
- `AiClient` 是接口，公开方法固定为 `OperationResult<AiResponse> chat(AiRequest request)` 或唯一等价签名；实现不得在本轮访问网络。
- `AiScenario` 至少覆盖通用问答、学习建议、笔记摘要、结构化任务建议、结构化学习计划建议，并为 `PromptBuilder` 提供稳定场景指令。
- `ContextProvider` 是接口，公开方法固定为 `OperationResult<LocalContext> getLocalContext()` 或唯一等价签名；测试中使用 fake/stub。
- `PromptBuilder` 接收 `AiConfiguration` 或模型名参数以构造 `AiRequest`；用户问题为空白返回 `VALIDATION_ERROR`；成功 messages 顺序稳定，至少包含 system message 和 user message。user message 必须包含 `LocalContext.overviewText()` 以及任务、日程、学习计划、收支、标签明细行；空明细列表使用稳定空数据表达，不得省略总览。
- 结构化建议场景的提示词必须明确要求“只返回单个 JSON 对象”，并包含目标类型 `TASK_DRAFT` 或 `STUDY_PLAN_DRAFT`；本轮不做 JSON 解析。
- `AiAssistantService` 构造依赖 `AiConfiguration`、`ContextProvider`、`PromptBuilder`、`AiClient`，依赖为空抛出带参数名的 `NullPointerException`。公开问答方法接收 `AiScenario` 和用户问题，成功返回 `OperationResult<String>` 或 `OperationResult<AiResponse>` 中的文本；API Key 缺失时直接返回 `AI_NOT_CONFIGURED` 且不得调用 `ContextProvider` 或 `AiClient`；上下文失败或客户端失败时传播同一 `ErrorCode` 和稳定消息。

## 已有代码上下文
已有 `assistant.common.ErrorCode` 包含全部 AI 相关错误分类；已有 `assistant.common.OperationResult` 作为服务层成功/失败返回；已有 `assistant.summary.LocalContext` 提供 `overviewText`、`todayTaskLines`、`todayScheduleLines`、`weekStudyPlanLines`、`monthTransactionLines`、`noteTagLines` 等确定性上下文字段；已有 `assistant.summary.SummaryService.buildLocalContext()` 可作为未来生产 `ContextProvider` 的委托来源。本轮只能通过这些只读上下文和接口协作，不得直接依赖任务、日程、学习、收支、笔记仓储或可变领域实体。
