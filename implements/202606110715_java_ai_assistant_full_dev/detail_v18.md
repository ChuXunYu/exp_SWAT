# 详细设计（v18）

## 概述

本轮设计目标是在 `assistant.ai` 包中建立不访问网络的 AI 基础编排层，为后续 `DeepSeekAiClient`、结构化建议解析、草稿生命周期和控制台 AI 菜单提供稳定公开契约。

本轮实现范围：

- `AiConfiguration`：AI 调用配置值对象，保存 DeepSeek base URL、chat completions path、模型名、API Key 和超时时间。
- `AiConfigurationLoader`：从可控 `Map<String, String>` 配置源加载 `AiConfiguration`，不直接读取真实环境变量。
- `AiRole`、`AiMessage`、`AiRequest`、`AiResponse`：OpenAI 兼容聊天请求/响应的内部 DTO。
- `AiClient`：AI 客户端接口，本轮只定义契约，不实现真实 HTTP。
- `AiScenario`：提示词场景枚举，覆盖普通问答、学习建议、笔记摘要、结构化任务建议和结构化学习计划建议。
- `ContextProvider`：AI 模块获取 `LocalContext` 的接口，测试通过 fake/stub 隔离本地上下文。
- `PromptBuilder`：将场景、用户问题和 `LocalContext` 组装为非流式 `AiRequest`。
- `AiAssistantService`：AI 问答应用服务基础，负责配置短路、上下文获取、提示词构造、客户端调用和错误传播。

本轮不实现：

- `DeepSeekAiClient`、真实 HTTP 调用、Jackson 请求/响应序列化与解析。
- `StructuredSuggestionParser`、`SuggestionDraft`、草稿生命周期、草稿导入服务。
- 控制台菜单、README 或环境说明变更。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/ai/AiConfiguration.java` | 新建 | 定义 AI 配置不可变值对象、默认配置、API Key 可用性判断和安全字符串表示。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiConfigurationLoader.java` | 新建 | 从可控配置源加载 DeepSeek 配置，处理默认值、空白覆盖和非法超时。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiRole.java` | 新建 | 定义 OpenAI 兼容消息角色及 wire value。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiMessage.java` | 新建 | 定义不可变聊天消息。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiRequest.java` | 新建 | 定义不可变聊天请求，包含模型、消息列表和 stream 标记。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiResponse.java` | 新建 | 定义不可变聊天响应文本。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiClient.java` | 新建 | 定义 AI 客户端抽象接口。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiScenario.java` | 新建 | 定义提示词场景和结构化输出目标类型。 |
| `java-ai-assistant/src/main/java/assistant/ai/ContextProvider.java` | 新建 | 定义本地上下文提供接口。 |
| `java-ai-assistant/src/main/java/assistant/ai/PromptBuilder.java` | 新建 | 构造 AI 请求 messages，保证本地上下文和结构化指令稳定。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiAssistantService.java` | 新建 | 编排配置检查、上下文读取、提示词构造和 AI 客户端调用。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiConfigurationTest.java` | 新建 | 覆盖配置默认值、字段校验、字符串清理、API Key 判断和安全 `toString()`。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiConfigurationLoaderTest.java` | 新建 | 覆盖从 `Map` 加载、默认回退、空白覆盖、非法超时和不读取真实环境。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiMessageTest.java` | 新建 | 覆盖消息构造校验和 role wire value。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiRequestTest.java` | 新建 | 覆盖请求构造校验、消息列表快照和非流式标记。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiResponseTest.java` | 新建 | 覆盖响应文本构造校验和清理。 |
| `java-ai-assistant/src/test/java/assistant/ai/PromptBuilderTest.java` | 新建 | 覆盖普通/结构化场景提示词、空上下文表达、上下文明细包含和输入校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiAssistantServiceTest.java` | 新建 | 覆盖配置缺失短路、上下文失败、提示词失败、客户端失败、成功返回和调用顺序。 |

## 类型定义

### `AiConfiguration`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：保存一次 AI 调用所需的不可变配置，并避免日志或断言中泄露 API Key。

**类型签名定义**：

```java
public record AiConfiguration(
        String baseUrl,
        String chatCompletionsPath,
        String model,
        String apiKey,
        Duration timeout)
```

**常量定义**：

| 常量签名 | 值 | 说明 |
|----------|----|------|
| `public static final String DEFAULT_BASE_URL` | `https://api.deepseek.com` | DeepSeek OpenAI 兼容 base URL。 |
| `public static final String DEFAULT_CHAT_COMPLETIONS_PATH` | `/chat/completions` | Chat completions path。 |
| `public static final String DEFAULT_MODEL` | `deepseek-v4-flash` | 默认模型。 |
| `public static final Duration DEFAULT_TIMEOUT` | `Duration.ofSeconds(20)` | 默认连接和请求超时。 |

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `String baseUrl` | 非空；执行 `strip()`；清理后不得为空白；不得以 `/` 结尾，构造时移除末尾连续 `/`；移除后仍不得为空。 |
| `String chatCompletionsPath` | 非空；执行 `strip()`；清理后不得为空白；必须以 `/` 开头。 |
| `String model` | 非空；执行 `strip()`；清理后不得为空白。 |
| `String apiKey` | 非空；执行 `strip()`；允许为空字符串；不得在 `toString()` 中输出原值。 |
| `Duration timeout` | 非空；必须大于 `Duration.ZERO`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiConfiguration` 规范构造器 | `AiConfiguration` | 校验并规范化全部字段；空引用抛 `NullPointerException`，消息为参数名；非法值抛 `IllegalArgumentException` 和稳定英文消息。 |
| `public static AiConfiguration defaultWithoutApiKey()` | `AiConfiguration` | 返回默认 base URL、path、model、20 秒超时和空 API Key。 |
| `public boolean hasApiKey()` | `boolean` | 当且仅当 `apiKey` 清理后非空白时返回 `true`。 |
| `public String toString()` | `String` | 返回不包含真实 `apiKey` 内容的字符串；只允许展示 `apiKeyConfigured=true/false`。 |

**构造方式**：

- 生产装配由 `AiConfigurationLoader` 构造。
- 测试可直接构造，API Key 只能使用非真实占位字符串，不使用真实 token 格式或真实密钥。

**类型关系**：

- 依赖 JDK `java.time.Duration`。
- 不依赖 `AiClient`、HTTP、Jackson 或真实环境变量。

### `AiConfigurationLoader`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：从可控配置源加载 AI 配置，统一处理默认值、空白覆盖和非法配置失败。

**类型签名定义**：`public final class AiConfigurationLoader`

**常量定义**：

| 常量签名 | 值 |
|----------|----|
| `public static final String API_KEY_NAME` | `DEEPSEEK_API_KEY` |
| `public static final String BASE_URL_NAME` | `DEEPSEEK_BASE_URL` |
| `public static final String MODEL_NAME` | `DEEPSEEK_MODEL` |
| `public static final String TIMEOUT_SECONDS_NAME` | `DEEPSEEK_TIMEOUT_SECONDS` |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<AiConfiguration> load(Map<String, String> values)` | `OperationResult<AiConfiguration>` | `values == null` 抛 `NullPointerException("values")`；只读取四个固定配置名；成功返回配置；非法超时或非法字符串配置返回 `VALIDATION_ERROR`，不向调用方抛未受控运行时异常。 |

**加载规则**：

| 配置名 | 规则 |
|--------|------|
| `DEEPSEEK_API_KEY` | 缺失或空白时使用空字符串；存在时执行 `strip()`。 |
| `DEEPSEEK_BASE_URL` | 缺失或空白时使用 `AiConfiguration.DEFAULT_BASE_URL`；存在时执行 `strip()` 并交由 `AiConfiguration` 规范化。 |
| `DEEPSEEK_MODEL` | 缺失或空白时使用 `AiConfiguration.DEFAULT_MODEL`；存在时执行 `strip()`。 |
| `DEEPSEEK_TIMEOUT_SECONDS` | 缺失或空白时使用 `AiConfiguration.DEFAULT_TIMEOUT`；存在时必须能解析为正整数秒。 |
| chat completions path | 本轮无外部覆盖项，固定使用 `AiConfiguration.DEFAULT_CHAT_COMPLETIONS_PATH`。 |

**失败结果**：

| 场景 | ErrorCode | 消息 |
|------|-----------|------|
| 超时值不是整数 | `VALIDATION_ERROR` | `invalid DeepSeek timeout seconds` |
| 超时值小于等于 0 | `VALIDATION_ERROR` | `invalid DeepSeek timeout seconds` |
| 构造 `AiConfiguration` 发现非法 base URL、path、model 或 timeout | `VALIDATION_ERROR` | `invalid DeepSeek configuration` |

**构造方式**：

- 无状态组件，可直接 `new AiConfigurationLoader()`。
- 本轮不提供调用 `System.getenv()` 或 `System.getProperties()` 的生产入口；后续控制台装配可把环境变量/JVM 参数合并为 `Map` 后传入。

**类型关系**：

- 依赖 `assistant.common.OperationResult` 和 `assistant.common.ErrorCode`。
- 不依赖真实环境、文件系统、网络、HTTP 或 Jackson。

### `AiRole`

**形态**：`enum`

**包路径**：`assistant.ai`

**职责**：定义内部消息角色并提供 OpenAI 兼容字符串。

**类型签名定义**：`public enum AiRole`

**枚举值**：

| 枚举值 | wire value |
|--------|------------|
| `SYSTEM` | `system` |
| `USER` | `user` |
| `ASSISTANT` | `assistant` |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public String wireValue()` | `String` | 返回 OpenAI 兼容 role 文本。 |

### `AiMessage`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：保存一条不可变聊天消息。

**类型签名定义**：

```java
public record AiMessage(AiRole role, String content)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `AiRole role` | 非空。 |
| `String content` | 非空；执行 `strip()`；清理后不得为空白。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiMessage` 规范构造器 | `AiMessage` | `role == null` 抛 `NullPointerException("role")`；`content == null` 抛 `NullPointerException("content")`；内容空白抛 `IllegalArgumentException("content must not be blank")`。 |

### `AiRequest`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：保存一次 AI 聊天请求的内部表示，供后续 `DeepSeekAiClient` 转换为 JSON。

**类型签名定义**：

```java
public record AiRequest(String model, List<AiMessage> messages, boolean stream)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `String model` | 非空；执行 `strip()`；清理后不得为空白。 |
| `List<AiMessage> messages` | 非空；元素非空；构造时复制为不可修改快照；列表不得为空。 |
| `boolean stream` | 本轮由 `PromptBuilder` 固定为 `false`；DTO 仍保留字段供后续协议层序列化。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiRequest` 规范构造器 | `AiRequest` | 校验模型和消息列表；空引用抛参数名 `NullPointerException`；空模型或空消息列表抛 `IllegalArgumentException`；访问器返回不可修改列表。 |
| `public static AiRequest nonStreaming(String model, List<AiMessage> messages)` | `AiRequest` | 等价于 `new AiRequest(model, messages, false)`。 |

### `AiResponse`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：保存 AI 返回的非空自然语言或结构化文本。

**类型签名定义**：

```java
public record AiResponse(String content)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `String content` | 非空；执行 `strip()`；清理后不得为空白。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiResponse` 规范构造器 | `AiResponse` | `content == null` 抛 `NullPointerException("content")`；内容空白抛 `IllegalArgumentException("content must not be blank")`。 |

### `AiClient`

**形态**：`interface`

**包路径**：`assistant.ai`

**职责**：隔离真实大模型调用，业务层只依赖该接口。

**类型签名定义**：

```java
public interface AiClient
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `OperationResult<AiResponse> chat(AiRequest request)` | `OperationResult<AiResponse>` | `request` 必须非空；实现负责把外部失败映射为 AI 相关 `ErrorCode`；本轮不提供真实实现，不访问网络。 |

**类型关系**：

- 后续 `DeepSeekAiClient` 实现该接口。
- 测试使用 fake/stub 实现，不读取网络或真实 API Key。

### `AiScenario`

**形态**：`enum`

**包路径**：`assistant.ai`

**职责**：为 `PromptBuilder` 提供稳定场景指令和结构化输出目标。

**类型签名定义**：`public enum AiScenario`

**枚举值**：

| 枚举值 | 职责 | 结构化目标 |
|--------|------|------------|
| `GENERAL_QA` | 通用学习与生活问答。 | 无 |
| `STUDY_ADVICE` | 基于本地学习计划、任务和日程给出学习建议。 | 无 |
| `NOTE_SUMMARY` | 基于本地笔记标签和摘要上下文给出笔记总结建议。 | 无 |
| `STRUCTURED_TASK_SUGGESTION` | 生成待办任务草稿建议。 | `TASK_DRAFT` |
| `STRUCTURED_STUDY_PLAN_SUGGESTION` | 生成学习计划草稿建议。 | `STUDY_PLAN_DRAFT` |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public String systemInstruction()` | `String` | 返回非空白中文场景指令；不得包含真实 API Key 或运行环境信息。 |
| `public boolean requiresStructuredJson()` | `boolean` | 结构化任务建议和结构化学习计划建议返回 `true`，其余返回 `false`。 |
| `public Optional<String> targetType()` | `Optional<String>` | 结构化场景返回目标类型；非结构化场景返回 `Optional.empty()`。 |

**结构化指令契约**：

- `STRUCTURED_TASK_SUGGESTION` 的提示词必须包含 `只返回单个 JSON 对象` 和 `TASK_DRAFT`。
- `STRUCTURED_STUDY_PLAN_SUGGESTION` 的提示词必须包含 `只返回单个 JSON 对象` 和 `STUDY_PLAN_DRAFT`。
- 本轮只构造提示词，不解析 JSON，不创建草稿。

### `ContextProvider`

**形态**：`interface`

**包路径**：`assistant.ai`

**职责**：为 AI 服务提供本地上下文快照，隔离 `SummaryService` 和测试替身。

**类型签名定义**：

```java
public interface ContextProvider
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `OperationResult<LocalContext> getLocalContext()` | `OperationResult<LocalContext>` | 成功返回非空 `LocalContext`；失败返回既有业务错误或 AI 可用性错误；不得抛出可预期业务失败。 |

**类型关系**：

- 依赖 `assistant.summary.LocalContext`。
- 后续生产实现可委托 `SummaryService.buildLocalContext()`。
- 本轮不新增默认生产实现，单元测试使用 fake/stub。

### `PromptBuilder`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：把场景、用户问题、模型配置和本地上下文组装为稳定的非流式聊天请求。

**类型签名定义**：`public final class PromptBuilder`

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public OperationResult<AiRequest> build(AiScenario scenario, String userQuestion, AiConfiguration configuration, LocalContext localContext)` | `OperationResult<AiRequest>` | `scenario`、`configuration`、`localContext` 为空抛参数名 `NullPointerException`；`userQuestion == null || userQuestion.isBlank()` 返回 `VALIDATION_ERROR`；成功返回 `stream == false` 且 model 来自 `configuration.model()` 的 `AiRequest`。 |

**成功请求契约**：

| 项目 | 契约 |
|------|------|
| messages 顺序 | 固定为第 1 条 `SYSTEM`，第 2 条 `USER`。 |
| system message | 必须包含助手身份约束、不得编造本地数据的约束、`scenario.systemInstruction()`；结构化场景还必须包含 `只返回单个 JSON 对象` 和目标类型。 |
| user message | 必须包含清理后的用户问题、`localContext.overviewText()` 和五类明细区块。 |
| 今日任务区块 | 标题固定为 `今日任务：`；每条使用 `- {line}`；列表为空时使用 `（无）`。 |
| 今日日程区块 | 标题固定为 `今日日程：`；每条使用 `- {line}`；列表为空时使用 `（无）`。 |
| 本周学习计划区块 | 标题固定为 `本周学习计划：`；每条使用 `- {line}`；列表为空时使用 `（无）`。 |
| 本月收支区块 | 标题固定为 `本月收支：`；每条使用 `- {line}`；列表为空时使用 `（无）`。 |
| 笔记标签区块 | 标题固定为 `笔记标签：`；每条使用 `- {line}`；列表为空时使用 `（无）`。 |

**失败结果**：

| 场景 | ErrorCode | 消息 |
|------|-----------|------|
| 用户问题为空引用或空白 | `VALIDATION_ERROR` | `user question must not be blank` |

**构造方式**：

- 无状态组件，可直接 `new PromptBuilder()`。
- 不读取真实时间、环境变量、文件系统或网络。

**类型关系**：

- 依赖 `AiScenario`、`AiConfiguration`、`AiRequest`、`AiMessage`、`AiRole` 和 `assistant.summary.LocalContext`。
- 不依赖业务仓储、业务服务、HTTP、Jackson 或真实 DeepSeek 配置。

### `AiAssistantService`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：作为 AI 问答应用服务基础，统一短路配置缺失、获取本地上下文、构造请求、调用客户端并传播稳定错误。

**类型签名定义**：`public final class AiAssistantService`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final AiConfiguration configuration;` | 构造时非空。 |
| `private final ContextProvider contextProvider;` | 构造时非空。 |
| `private final PromptBuilder promptBuilder;` | 构造时非空。 |
| `private final AiClient aiClient;` | 构造时非空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiAssistantService(AiConfiguration configuration, ContextProvider contextProvider, PromptBuilder promptBuilder, AiClient aiClient)` | `AiAssistantService` | 任一依赖为空抛 `NullPointerException`，消息为参数名。 |
| `public OperationResult<String> ask(AiScenario scenario, String userQuestion)` | `OperationResult<String>` | 根据指定场景处理一次 AI 问答或建议请求；成功返回 `AiResponse.content()`；失败返回稳定 `OperationResult`，不得修改任何本地业务数据。 |

**`ask` 行为顺序契约**：

1. `scenario == null` 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "ai scenario is required")`。
2. `!configuration.hasApiKey()` 返回 `OperationResult.failure(ErrorCode.AI_NOT_CONFIGURED, "DeepSeek API key is not configured")`，且不得调用 `ContextProvider`、`PromptBuilder` 或 `AiClient`。
3. `userQuestion == null || userQuestion.isBlank()` 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "user question must not be blank")`，且不得调用 `ContextProvider` 或 `AiClient`。
4. 调用 `contextProvider.getLocalContext()`；失败时传播同一 `ErrorCode` 和稳定消息，且不得调用 `AiClient`。
5. 调用 `promptBuilder.build(scenario, userQuestion, configuration, localContext)`；失败时传播同一 `ErrorCode` 和稳定消息，且不得调用 `AiClient`。
6. 调用 `aiClient.chat(request)`；失败时传播同一 `ErrorCode` 和稳定消息。
7. 客户端成功但 payload 为 `null` 时返回 `OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty")`。
8. 客户端成功且 payload 非空时返回 `OperationResult.success(response.content())`。

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static <T> OperationResult<T> propagateFailure(OperationResult<?> failure, String fallbackMessage)` | `OperationResult<T>` | `failure == null` 抛 `NullPointerException("failure")`；要求 `failure.isFailure()`；错误码原样传播；消息为空或空白时使用 `fallbackMessage`。 |
| `private static String stableMessage(String message, String fallbackMessage)` | `String` | 原消息非空白时返回原消息；否则返回非空白 fallback。 |

**错误传播 fallback**：

| 来源 | fallback message |
|------|------------------|
| `ContextProvider` 失败但消息异常为空 | `AI local context unavailable` |
| `PromptBuilder` 失败但消息异常为空 | `AI prompt build failed` |
| `AiClient` 失败但消息异常为空 | `AI client failed` |

**类型关系**：

- 依赖 `assistant.common.OperationResult` 和 `assistant.common.ErrorCode`。
- 依赖 `ContextProvider`、`PromptBuilder`、`AiClient` 和 AI DTO。
- 不依赖 `SummaryService`、业务仓储、HTTP、Jackson 或草稿类型。

## 错误处理

- 值对象/DTO 构造器使用空引用 `NullPointerException`、非法字段 `IllegalArgumentException`，与现有 `assistant.summary`、`assistant.common` 风格一致。
- 应用服务和加载器使用 `OperationResult` 表达可预期失败，不把配置缺失、用户输入为空、上下文失败或 AI 客户端失败作为系统崩溃。
- `OperationResult.failure` 要求消息非空白，因此所有失败路径必须提供稳定消息；传播外部失败时若消息异常为空，必须使用本设计定义的 fallback。
- API Key 缺失属于可预期外部配置状态，固定返回 `AI_NOT_CONFIGURED`；不得尝试构造提示词或调用 AI 客户端。
- 本轮不捕获真实网络异常，因为本轮没有网络实现；后续 `DeepSeekAiClient` 负责把 HTTP、超时、鉴权、限流、空响应和 JSON 异常映射到既有 AI `ErrorCode`。
- AI 基础服务不得写入任务、日程、学习计划、收支、笔记或未来草稿仓储；失败不得污染本地业务数据。

## 行为契约

- `assistant.ai` 本轮只能依赖 `assistant.common` 和 `assistant.summary.LocalContext`，不得直接依赖任务、日程、学习、收支、笔记仓储或可变领域实体。
- `AiConfiguration.defaultWithoutApiKey()` 必须使用默认值：base URL `https://api.deepseek.com`、path `/chat/completions`、model `deepseek-v4-flash`、timeout `20` 秒、API Key 空字符串。
- `AiConfigurationLoader.load(...)` 不得调用 `System.getenv()`、`System.getProperty()`、读取文件或访问网络；普通单元测试只传入显式 `Map`。
- API Key 不得写入源码、`pom.xml`、JUnit 测试、README 示例明文或提交材料；测试只能使用非真实占位字符串验证“已配置/未配置”分支。
- `AiConfiguration.toString()` 不得包含 `apiKey()` 的原始内容，避免后续日志或断言泄露。
- `AiRequest.messages()` 返回的列表必须不可修改，且构造后修改输入列表不得影响请求。
- `PromptBuilder` 生成的 `AiRequest.stream()` 必须恒为 `false`。
- `PromptBuilder` 的 user message 必须始终包含 `LocalContext.overviewText()`；即使五类明细均为空，也不得省略总览。
- 五类明细空列表必须用 `（无）` 表达，不得省略对应区块。
- 结构化场景只负责提示模型返回 JSON；本轮不验证返回 JSON、不解析 JSON、不创建草稿。
- `AiAssistantService.ask(...)` 的配置缺失、输入验证、上下文失败、提示词失败、客户端失败和成功路径必须可通过 fake/stub 单元测试精确断言调用顺序。

## 依赖关系

- `assistant.ai` 可依赖：
  - `assistant.common.ErrorCode`
  - `assistant.common.OperationResult`
  - `assistant.summary.LocalContext`
  - JDK `Duration`、`List`、`Map`、`Optional`、`Objects`
- `assistant.ai` 本轮不得依赖：
  - `java.net.http.HttpClient`
  - Jackson `ObjectMapper` 或 `JsonNode`
  - 任何业务仓储或领域实体
  - `SummaryService` 具体类
  - 控制台输入输出类
- 后续任务可基于本轮契约新增：
  - `DeepSeekAiClient implements AiClient`
  - `SummaryContextProvider implements ContextProvider`
  - `StructuredSuggestionParser`
  - 草稿生命周期和导入服务

## 测试设计

### `AiConfigurationTest`

| 用例 | 断言重点 |
|------|----------|
| `defaultWithoutApiKeyUsesDeepSeekDefaults()` | 默认 base URL、path、model、20 秒 timeout、空 API Key、`hasApiKey() == false`。 |
| `constructorNormalizesStringsAndDetectsApiKey()` | base URL 去除末尾 `/`，path/model/apiKey 执行 `strip()`，占位 API Key 非空时 `hasApiKey() == true`。 |
| `constructorRejectsNullRequiredFields()` | base URL、path、model、apiKey、timeout 为空引用时抛参数名 NPE。 |
| `constructorRejectsBlankOrInvalidValues()` | base URL、path、model 空白，path 不以 `/` 开头，timeout 非正数时抛 `IllegalArgumentException`。 |
| `toStringDoesNotExposeApiKeyValue()` | `toString()` 包含配置是否存在，不包含占位 API Key 原文。 |

### `AiConfigurationLoaderTest`

| 用例 | 断言重点 |
|------|----------|
| `loadUsesDefaultsWhenMapIsEmpty()` | 空 map 返回默认配置且 API Key 未配置。 |
| `loadUsesProvidedValuesFromMap()` | 四个固定配置名被读取、清理并进入配置对象。 |
| `loadTreatsBlankOverridesAsDefaultsExceptApiKey()` | blank base URL/model/timeout 回退默认值，blank API Key 变为空字符串。 |
| `loadReturnsValidationFailureForInvalidTimeout()` | 非数字、`0`、负数都返回 `VALIDATION_ERROR` 和 `invalid DeepSeek timeout seconds`。 |
| `loadReturnsValidationFailureForInvalidConfiguration()` | 非法 base URL/path/model 造成构造失败时返回 `VALIDATION_ERROR` 和 `invalid DeepSeek configuration`。 |
| `loadRejectsNullMap()` | `values == null` 抛 `NullPointerException("values")`。 |

### `AiMessageTest`

| 用例 | 断言重点 |
|------|----------|
| `roleWireValuesAreOpenAiCompatible()` | `SYSTEM/USER/ASSISTANT` 分别返回 `system/user/assistant`。 |
| `constructorNormalizesContent()` | 内容执行 `strip()`。 |
| `constructorRejectsNullRoleOrContent()` | role/content 为空引用时抛参数名 NPE。 |
| `constructorRejectsBlankContent()` | 空白内容抛 `IllegalArgumentException("content must not be blank")`。 |

### `AiRequestTest`

| 用例 | 断言重点 |
|------|----------|
| `nonStreamingCreatesRequestWithStreamFalse()` | 静态工厂返回 `stream == false`。 |
| `constructorNormalizesModelAndCopiesMessages()` | model 清理；输入列表后续修改不影响请求；访问器列表不可修改。 |
| `constructorRejectsNullsAndBlankModel()` | model/messages/消息元素为空引用和 model 空白被拒绝。 |
| `constructorRejectsEmptyMessages()` | 空消息列表抛 `IllegalArgumentException("messages must not be empty")`。 |

### `AiResponseTest`

| 用例 | 断言重点 |
|------|----------|
| `constructorNormalizesContent()` | 响应内容执行 `strip()`。 |
| `constructorRejectsNullOrBlankContent()` | content 为空引用或空白被拒绝。 |

### `PromptBuilderTest`

| 用例 | 断言重点 |
|------|----------|
| `buildCreatesNonStreamingRequestWithConfiguredModel()` | model 来自配置，stream 为 false，messages 顺序为 system/user。 |
| `buildIncludesOverviewAndAllContextSections()` | user message 包含 overview、今日任务、今日日程、本周学习计划、本月收支、笔记标签五类明细。 |
| `buildUsesStableEmptyMarkerForEmptyDetailLists()` | 空明细列表对应区块包含 `（无）`，不省略总览。 |
| `buildIncludesScenarioInstructionForGeneralStudyAndNoteScenarios()` | 普通问答、学习建议、笔记摘要分别包含对应场景指令。 |
| `buildAddsStructuredJsonInstructionForTaskSuggestion()` | 结构化任务场景包含 `只返回单个 JSON 对象` 和 `TASK_DRAFT`。 |
| `buildAddsStructuredJsonInstructionForStudyPlanSuggestion()` | 结构化学习计划场景包含 `只返回单个 JSON 对象` 和 `STUDY_PLAN_DRAFT`。 |
| `buildReturnsValidationFailureForBlankQuestion()` | null、空字符串、空白字符串返回 `VALIDATION_ERROR` 和稳定消息。 |
| `buildRejectsNullDependencies()` | scenario/configuration/localContext 为空引用时抛参数名 NPE。 |

### `AiAssistantServiceTest`

| 用例 | 断言重点 |
|------|----------|
| `constructorRejectsNullDependencies()` | configuration/contextProvider/promptBuilder/aiClient 为空引用时抛参数名 NPE。 |
| `askReturnsNotConfiguredWithoutCallingCollaborators()` | API Key 缺失时返回 `AI_NOT_CONFIGURED`；不调用 `ContextProvider`、`PromptBuilder`、`AiClient`。 |
| `askReturnsValidationFailureForNullScenario()` | scenario 为空返回 `VALIDATION_ERROR`，不调用上下文或客户端。 |
| `askReturnsValidationFailureForBlankQuestionWithoutCallingContextOrClient()` | 配置存在但问题为空白时返回 `VALIDATION_ERROR`，不调用上下文或客户端。 |
| `askPropagatesContextProviderFailure()` | 上下文失败时错误码和消息原样传播，不调用 prompt builder 或客户端。 |
| `askPropagatesPromptBuilderFailure()` | 提示词构造失败时错误码和消息原样传播，不调用客户端。 |
| `askSendsBuiltRequestToClientAndReturnsContent()` | 成功路径中客户端收到 `PromptBuilder` 返回的同一请求，返回响应文本。 |
| `askPropagatesAiClientFailures()` | `AI_AUTH_FAILED`、`AI_RATE_LIMITED`、`AI_TIMEOUT`、`AI_BAD_REQUEST`、`AI_REMOTE_UNAVAILABLE`、`AI_NETWORK_ERROR`、`AI_EMPTY_RESPONSE`、`AI_MALFORMED_RESPONSE` 均可原样传播。 |
| `askReturnsEmptyResponseFailureWhenClientPayloadIsNull()` | 客户端成功但 payload 为 null 时返回 `AI_EMPTY_RESPONSE` 和稳定消息。 |
| `askDoesNotModifyLocalBusinessData()` | 使用 fake `ContextProvider` 和 fake `AiClient` 验证服务只读，不接触任何业务仓储或实体。 |

## 验证建议

- 定向运行：`mvn -q -Dtest='assistant.ai.*Test' test`
- 全量运行：`mvn -q test`
- 全部测试必须在无真实 API Key、无网络、无真实环境变量依赖的环境下可重复通过。
