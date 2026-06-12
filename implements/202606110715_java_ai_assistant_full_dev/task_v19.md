# 任务指令（v19）

## 动作
NEW

## 任务描述
新增真实 DeepSeek OpenAI 兼容 HTTP 客户端、可替换 HTTP 发送边界和协议错误映射，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/ai/DeepSeekAiClient.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiHttpTransport.java`
- `java-ai-assistant/src/main/java/assistant/ai/JdkAiHttpTransport.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiHttpRequest.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiHttpResponse.java`
- `java-ai-assistant/src/main/java/assistant/ai/AiErrorMapper.java`
- `java-ai-assistant/src/test/java/assistant/ai/DeepSeekAiClientTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/AiErrorMapperTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java`

本轮实现 `DeepSeekAiClient implements AiClient`，负责将内部 `AiRequest` 序列化为 DeepSeek/OpenAI 兼容 JSON，通过可替换 HTTP transport 发送请求，解析响应文本，并将 HTTP 状态、超时、网络异常、空响应和 JSON 格式异常映射为稳定 `OperationResult`。普通单元测试不得访问真实网络、真实 DeepSeek 或真实 API Key。

## 选择理由
v18 已完成 AI 配置、消息契约、提示词构造、上下文提供接口、`AiClient` 抽象和 `AiAssistantService` 编排。AI 问答功能还缺少真实协议层：POST 到 DeepSeek chat completions、Bearer 鉴权、Jackson JSON 序列化/解析、HTTP 状态和外部依赖异常映射。先实现 `DeepSeekAiClient` 与可替换发送边界，可让后续结构化建议解析、草稿生成服务、控制台 AI 菜单和可选集成测试共享同一底层客户端，不把 HTTP/Jackson 细节泄漏到应用服务或控制台层。

## 任务上下文
完整需求与技术方案要求：

- 应用需具备按 OpenAI 兼容接口接入 DeepSeek Flash 的能力，默认 `base_url` 为 `https://api.deepseek.com`，模型为 `deepseek-v4-flash`。
- HTTP 客户端使用 JDK `java.net.http.HttpClient`，不引入额外 HTTP 框架。
- JSON 处理使用 Jackson Databind。
- 请求 POST 到 `/chat/completions`，使用 Bearer API Key，messages 和非流式请求格式与 OpenAI 兼容。
- AI 失败属于常见外部状态，不允许导致程序崩溃或污染本地业务数据。
- 错误映射固定为：
  - 401 或 403 → `AI_AUTH_FAILED`
  - 429 → `AI_RATE_LIMITED`
  - JDK 超时异常、408 或 504 → `AI_TIMEOUT`
  - 400 或 422 → `AI_BAD_REQUEST`
  - 未列明的其他 4xx → `AI_BAD_REQUEST`
  - 未列明的 3xx → `AI_REMOTE_UNAVAILABLE`
  - 5xx → `AI_REMOTE_UNAVAILABLE`
  - 其他未列明的非 2xx HTTP 状态 → `AI_REMOTE_UNAVAILABLE`
  - 网络 I/O 异常 → `AI_NETWORK_ERROR`
  - HTTP 2xx 但内容为空 → `AI_EMPTY_RESPONSE`
  - JSON 结构不符合预期 → `AI_MALFORMED_RESPONSE`
- 捕获 `InterruptedException` 时必须恢复线程中断标记，并返回外部依赖失败分类，优先映射为 `AI_NETWORK_ERROR`，消息需表明 AI 请求被中断。
- 普通单元测试必须使用 fake/stub/mock 隔离外部依赖，不读取真实环境变量、不访问真实 DeepSeek、不依赖真实 API Key。真实 DeepSeek 连通性后续如需验证，应放入 `*IT` 或 integration profile。

本轮不实现：

- `StructuredSuggestionParser`、`SuggestionDraft`、草稿生命周期或草稿导入服务。
- 控制台菜单和应用装配。
- 真实 DeepSeek 集成测试。

## 已有代码上下文
既有 `assistant.ai` 包在 v18 已提供：

- `AiConfiguration`：保存 `baseUrl`、`chatCompletionsPath`、`model`、`apiKey`、`timeout`，默认 base URL/path/model/20 秒超时，并提供 `hasApiKey()`。
- `AiRequest`：保存 `model`、`List<AiMessage>`、`stream`，并提供 `nonStreaming(...)` 工厂。
- `AiMessage` 与 `AiRole`：保存 role/content，`AiRole.wireValue()` 返回 `system`、`user`、`assistant`。
- `AiResponse`：保存 AI 返回文本。
- `AiClient`：`OperationResult<AiResponse> chat(AiRequest request)`。
- `AiAssistantService`：在配置缺失时返回 `AI_NOT_CONFIGURED`，配置可用时调用 `ContextProvider`、`PromptBuilder` 和 `AiClient`，并传播客户端失败。
- `ErrorCode` 已包含 `AI_AUTH_FAILED`、`AI_RATE_LIMITED`、`AI_TIMEOUT`、`AI_BAD_REQUEST`、`AI_REMOTE_UNAVAILABLE`、`AI_NETWORK_ERROR`、`AI_EMPTY_RESPONSE`、`AI_MALFORMED_RESPONSE`。
- `pom.xml` 已包含 Jackson Databind 依赖，可直接用于 JSON 序列化与解析。

实现建议：

- `AiHttpRequest`/`AiHttpResponse` 作为 transport 边界 DTO，至少表达 URI、headers、body、timeout 和 HTTP status/body。
- `AiHttpTransport` 负责发送 `AiHttpRequest` 并返回 `AiHttpResponse`，可声明抛出 `IOException`、`InterruptedException`、`java.net.http.HttpTimeoutException` 或等价 JDK 异常。
- `JdkAiHttpTransport` 使用 `HttpClient` 将边界请求转换为 JDK `HttpRequest` 并同步发送，构造器允许注入 `HttpClient` 以便测试；不要在单元测试中访问外网。
- `DeepSeekAiClient` 注入 `AiConfiguration`、`AiHttpTransport` 和 Jackson `ObjectMapper`（或内部创建默认 mapper，但需可测试），负责：
  - 校验 `request == null` 时返回 `VALIDATION_ERROR` 或抛参数空指针需在设计中固定；
  - 使用 `{baseUrl}{chatCompletionsPath}` 组装 URI；
  - 添加 `Authorization: Bearer {apiKey}` 与 `Content-Type: application/json`；
  - 序列化 JSON 字段 `model`、`messages[{role, content}]`、`stream`；
  - 解析成功响应的 `choices[0].message.content`；
  - 空响应体、空 choices、缺失 message/content 或 content 空白返回 `AI_EMPTY_RESPONSE`；
  - JSON 无法解析或字段类型不符合预期返回 `AI_MALFORMED_RESPONSE`。
- `AiErrorMapper` 集中处理 HTTP 状态码与异常到 `ErrorCode` 的映射，便于白盒测试覆盖所有分支。

测试要求：

- `DeepSeekAiClientTest` 使用 fake `AiHttpTransport` 捕获请求并返回构造好的 HTTP 响应或抛出异常，覆盖成功请求 URL/headers/body/timeout、成功解析、空 body、空 content、畸形 JSON、401/403/429/408/504/400/422/5xx、I/O 异常、超时异常、InterruptedException 恢复中断标记。
- `AiErrorMapperTest` 覆盖全部状态码和异常映射分支，包括未列明 4xx 映射为 `AI_BAD_REQUEST`，未列明 3xx 映射为 `AI_REMOTE_UNAVAILABLE`，其他未列明非 2xx HTTP 状态映射为 `AI_REMOTE_UNAVAILABLE`。
- `DeepSeekAiClientTest` 除已列明状态码外，还必须覆盖至少一个未列明 4xx、一个 3xx 和一个其他未列明非 2xx HTTP 状态，并按上述唯一口径断言失败 `ErrorCode`。
- `JdkAiHttpTransportTest` 在不连接外网的前提下验证构造器空依赖、请求字段校验和 JDK 请求转换可观察行为；如无法无网络断言发送，可将发送测试限定在 mockable/fake `HttpClient` 或仅覆盖边界对象校验。

## 修订说明（v19 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| `AiErrorMapperTest` 要求覆盖未列明 4xx/3xx 默认外部失败分类，但任务未明确这些状态码映射到哪个具体 `ErrorCode`，导致实现和测试无法形成唯一断言。 | 固定未列明 HTTP 状态码映射契约：未列明 4xx 映射为 `AI_BAD_REQUEST`，未列明 3xx 映射为 `AI_REMOTE_UNAVAILABLE`，其他未列明非 2xx HTTP 状态映射为 `AI_REMOTE_UNAVAILABLE`。 |
| `DeepSeekAiClientTest` 可能只覆盖已列明状态码，无法验证客户端实际按默认状态码口径传播错误。 | 在测试要求中补充必须覆盖至少一个未列明 4xx、一个 3xx 和一个其他未列明非 2xx HTTP 状态，并按固定映射口径断言失败 `ErrorCode`。 |
