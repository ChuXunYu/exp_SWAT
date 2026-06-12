# 详细设计（v19）

## 概述

本轮设计目标是在 `assistant.ai` 包中新增真实 DeepSeek OpenAI 兼容协议层，同时保持业务服务只依赖 `AiClient` 抽象、普通单元测试不访问真实网络或真实 API Key。

本轮实现范围：

- `AiHttpRequest`：HTTP 发送边界请求 DTO，表达 URI、headers、body 和 timeout。
- `AiHttpResponse`：HTTP 发送边界响应 DTO，表达 status code 和响应 body。
- `AiHttpTransport`：可替换 HTTP 发送接口，供单元测试 fake/stub，生产实现使用 JDK `HttpClient`。
- `JdkAiHttpTransport`：基于 `java.net.http.HttpClient` 的同步发送实现，不引入额外 HTTP 框架。
- `AiErrorMapper`：集中映射 HTTP 状态码和外部异常到稳定 `ErrorCode`。
- `DeepSeekAiClient`：实现 `AiClient`，负责内部 `AiRequest` 到 OpenAI 兼容 JSON 的序列化、Bearer 鉴权、transport 调用、响应 JSON 解析和外部失败结果转换。
- 单元测试覆盖客户端协议构造、成功解析、错误映射、异常处理、线程中断恢复和 JDK transport 的可观察转换行为。

本轮不实现：

- `StructuredSuggestionParser`、`SuggestionDraft`、草稿生命周期或草稿导入服务。
- 控制台菜单、生产应用装配或环境变量读取入口。
- 真实 DeepSeek 集成测试、真实网络连通性测试或真实 API Key 读取。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/ai/AiHttpRequest.java` | 新建 | 定义 transport 边界请求 DTO，校验 URI、headers、body、timeout 并复制不可变快照。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiHttpResponse.java` | 新建 | 定义 transport 边界响应 DTO，保存 HTTP status code 和非空响应 body 字符串。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiHttpTransport.java` | 新建 | 定义可替换 HTTP 同步发送接口。 |
| `java-ai-assistant/src/main/java/assistant/ai/JdkAiHttpTransport.java` | 新建 | 使用 JDK `HttpClient` 将 `AiHttpRequest` 转换为 `HttpRequest` 并同步发送。 |
| `java-ai-assistant/src/main/java/assistant/ai/AiErrorMapper.java` | 新建 | 集中处理 HTTP 状态码和异常到 `ErrorCode` 的映射。 |
| `java-ai-assistant/src/main/java/assistant/ai/DeepSeekAiClient.java` | 新建 | 实现 `AiClient`，完成 DeepSeek/OpenAI 兼容请求构造、发送、响应解析和失败结果转换。 |
| `java-ai-assistant/src/test/java/assistant/ai/DeepSeekAiClientTest.java` | 新建 | 使用 fake transport 覆盖请求 URL/headers/body/timeout、成功解析、空响应、畸形 JSON、状态码映射和异常映射。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiErrorMapperTest.java` | 新建 | 覆盖全部 HTTP 状态码分类和异常分类分支。 |
| `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java` | 新建 | 不访问外网，覆盖构造器空依赖、边界 DTO 校验和 JDK 请求转换可观察行为。 |

## 类型定义

### `AiHttpRequest`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：作为 `AiHttpTransport` 的入参，隔离 AI 协议层与具体 JDK HTTP 类型。

**类型签名定义**：

```java
public record AiHttpRequest(
        URI uri,
        Map<String, String> headers,
        String body,
        Duration timeout)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `URI uri` | 非空；必须为 absolute URI；`uri.getScheme()` 不得为空白；`uri.getHost()` 不得为空白。 |
| `Map<String, String> headers` | 非空；key 和 value 均非空；key 执行 `strip()` 后不得为空白；value 保留原始字符串但不得为空；构造时复制为不可修改快照。 |
| `String body` | 非空；允许空字符串；本轮 `DeepSeekAiClient` 总是传入非空 JSON 字符串。 |
| `Duration timeout` | 非空；必须大于 `Duration.ZERO`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiHttpRequest` 规范构造器 | `AiHttpRequest` | 校验全部字段；空引用抛 `NullPointerException`，消息为参数名或 `headerName` / `headerValue`；非法 URI、header 名或 timeout 抛 `IllegalArgumentException`；`headers()` 返回不可修改快照。 |

**构造方式**：

- 由 `DeepSeekAiClient` 在每次 `chat(...)` 调用中创建。
- 单元测试可直接构造边界对象验证 `JdkAiHttpTransport`。

**类型关系**：

- 依赖 JDK `java.net.URI`、`java.time.Duration`、`java.util.Map`。
- 不依赖 Jackson、`HttpClient` 或业务服务。

### `AiHttpResponse`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：作为 `AiHttpTransport` 的出参，向协议层返回 HTTP 状态码和响应文本。

**类型签名定义**：

```java
public record AiHttpResponse(int statusCode, String body)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `int statusCode` | 必须在 `100..599` 范围内。 |
| `String body` | 非空；允许空字符串；不执行 `strip()`，由 `DeepSeekAiClient` 判断空白响应。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public AiHttpResponse` 规范构造器 | `AiHttpResponse` | `body == null` 抛 `NullPointerException("body")`；状态码越界抛 `IllegalArgumentException("statusCode must be between 100 and 599")`。 |

**构造方式**：

- `JdkAiHttpTransport` 根据 JDK `HttpResponse<String>` 构造。
- `DeepSeekAiClientTest` 的 fake transport 直接构造。

**类型关系**：

- 不依赖 JDK HTTP 类型或 Jackson。

### `AiHttpTransport`

**形态**：`interface`

**包路径**：`assistant.ai`

**职责**：定义 AI HTTP 同步发送边界，允许生产实现和单元测试替身互换。

**类型签名定义**：

```java
public interface AiHttpTransport
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `AiHttpResponse send(AiHttpRequest request) throws IOException, InterruptedException` | `AiHttpResponse` | `request == null` 由实现抛 `NullPointerException("request")`；网络 I/O、超时和中断向 `DeepSeekAiClient` 暴露，由客户端映射为 `OperationResult` 失败。 |

**构造方式**：

- 接口无构造。
- 生产使用 `JdkAiHttpTransport`。
- 单元测试使用 fake/stub transport，不访问真实 DeepSeek。

**类型关系**：

- 依赖 JDK `java.io.IOException`。
- 不依赖 `OperationResult`，避免 transport 层承载业务错误分类。

### `JdkAiHttpTransport`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：用 JDK `java.net.http.HttpClient` 同步发送 AI HTTP 请求。

**类型签名定义**：

```java
public final class JdkAiHttpTransport implements AiHttpTransport
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final HttpClient httpClient` | 非空；构造注入，便于测试 mock。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public JdkAiHttpTransport(HttpClient httpClient)` | `JdkAiHttpTransport` | `httpClient == null` 抛 `NullPointerException("httpClient")`。 |
| `public static JdkAiHttpTransport create(Duration connectTimeout)` | `JdkAiHttpTransport` | `connectTimeout == null` 抛 `NullPointerException("connectTimeout")`；必须大于 `Duration.ZERO`；使用 `HttpClient.newBuilder().connectTimeout(connectTimeout).build()` 创建。 |
| `@Override public AiHttpResponse send(AiHttpRequest request) throws IOException, InterruptedException` | `AiHttpResponse` | 校验 `request` 非空；构造 JDK `HttpRequest`，使用 POST、请求 URI、timeout、全部 headers 和 UTF-8 string body；调用 `httpClient.send(jdkRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))`；返回 `new AiHttpResponse(statusCode, body == null ? "" : body)`。 |

**JDK 请求转换规则**：

| 输入 | JDK `HttpRequest` 表达 |
|------|------------------------|
| `request.uri()` | `HttpRequest.Builder.uri(...)` |
| `request.timeout()` | `HttpRequest.Builder.timeout(...)` |
| `request.headers()` | 对每个 entry 调用 `builder.header(name, value)` |
| `request.body()` | `HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)` |
| HTTP 方法 | 固定 `POST` |

**构造方式**：

- 生产装配可调用 `JdkAiHttpTransport.create(configuration.timeout())` 或直接注入预配置 `HttpClient`。
- 单元测试使用 Mockito mock `HttpClient` 捕获传入的 JDK `HttpRequest`，不得连接外网。

**类型关系**：

- 实现 `AiHttpTransport`。
- 依赖 JDK `java.net.http.HttpClient`、`HttpRequest`、`HttpResponse`、`StandardCharsets`。
- 不依赖 Jackson 或 `OperationResult`。

### `AiErrorMapper`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：将 AI 外部协议错误统一映射为应用层 `ErrorCode`。

**类型签名定义**：

```java
public final class AiErrorMapper
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public ErrorCode mapHttpStatus(int statusCode)` | `ErrorCode` | 仅用于非 2xx 状态；按固定状态码策略返回错误分类；若误传 2xx，返回 `AI_REMOTE_UNAVAILABLE`。 |
| `public ErrorCode mapException(Exception exception)` | `ErrorCode` | `exception == null` 抛 `NullPointerException("exception")`；`HttpTimeoutException` 映射 `AI_TIMEOUT`；其他 `IOException` 映射 `AI_NETWORK_ERROR`；`InterruptedException` 映射 `AI_NETWORK_ERROR`；其他异常映射 `AI_REMOTE_UNAVAILABLE`。 |

**HTTP 状态映射规则**：

| 状态码 | ErrorCode |
|--------|-----------|
| `401` 或 `403` | `AI_AUTH_FAILED` |
| `429` | `AI_RATE_LIMITED` |
| `408` 或 `504` | `AI_TIMEOUT` |
| `400` 或 `422` | `AI_BAD_REQUEST` |
| 未列明的其他 `4xx` | `AI_BAD_REQUEST` |
| 未列明的 `3xx` | `AI_REMOTE_UNAVAILABLE` |
| 任意 `5xx` | `AI_REMOTE_UNAVAILABLE` |
| 其他未列明的非 `2xx` HTTP 状态 | `AI_REMOTE_UNAVAILABLE` |
| 误传 `2xx` | `AI_REMOTE_UNAVAILABLE` |

**构造方式**：

- 无状态组件，可直接 `new AiErrorMapper()`。

**类型关系**：

- 依赖 `assistant.common.ErrorCode`。
- 被 `DeepSeekAiClient` 使用；测试可独立白盒覆盖。

### `DeepSeekAiClient`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：真实 AI 客户端基础设施实现，将内部聊天请求转换为 DeepSeek/OpenAI 兼容 HTTP 调用，并将所有可预期外部失败转换为稳定 `OperationResult`。

**类型签名定义**：

```java
public final class DeepSeekAiClient implements AiClient
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final AiConfiguration configuration` | 非空；提供 base URL、path、API Key、默认模型和 timeout。 |
| `private final AiHttpTransport transport` | 非空；可替换发送边界。 |
| `private final ObjectMapper objectMapper` | 非空；用于请求 JSON 序列化和响应 JSON 解析。 |
| `private final AiErrorMapper errorMapper` | 非空；集中错误分类。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public DeepSeekAiClient(AiConfiguration configuration, AiHttpTransport transport, ObjectMapper objectMapper, AiErrorMapper errorMapper)` | `DeepSeekAiClient` | 任一参数为空抛 `NullPointerException`，消息分别为 `configuration`、`transport`、`objectMapper`、`errorMapper`。 |
| `public DeepSeekAiClient(AiConfiguration configuration, AiHttpTransport transport)` | `DeepSeekAiClient` | 便捷构造器；内部创建 `new ObjectMapper()` 和 `new AiErrorMapper()`。 |
| `@Override public OperationResult<AiResponse> chat(AiRequest request)` | `OperationResult<AiResponse>` | `request == null` 返回 `VALIDATION_ERROR`，消息 `AI request is required`；请求构造、发送、解析任一可预期失败均返回失败结果，不向业务服务泄露 HTTP/Jackson/I/O 异常。 |

**请求 URI 规则**：

- URI 字符串为 `configuration.baseUrl() + configuration.chatCompletionsPath()`。
- 使用 `URI.create(...)` 创建 URI。
- 若 URI 创建失败或不是合法 absolute URI，返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "invalid DeepSeek endpoint")`。
- `AiConfiguration` 已移除 base URL 尾部 `/` 并保证 path 以 `/` 开头，因此正常配置会得到单斜杠拼接。

**请求 header 规则**：

| Header | 值 |
|--------|----|
| `Authorization` | `Bearer ` + `configuration.apiKey()` |
| `Content-Type` | `application/json` |
| `Accept` | `application/json` |

`DeepSeekAiClient` 不负责 API Key 缺失短路；现有 `AiAssistantService` 已在配置缺失时返回 `AI_NOT_CONFIGURED`。若直接使用未配置 key 的客户端，仍发送 `Bearer `，远端状态由 HTTP 映射处理。

**请求 JSON 结构**：

```text
{
  "model": request.model(),
  "messages": [
    {"role": message.role().wireValue(), "content": message.content()}
  ],
  "stream": request.stream()
}
```

设计约束：

- 使用 Jackson `ObjectMapper` 序列化结构化对象，不使用手写 JSON 拼接。
- 可用私有静态 record 或 `Map<String, Object>` 表达序列化模型；若使用私有 record，名称不作为公开 API。
- `AiRequest` 已保证 model、messages 和 message content 非空非空白；`DeepSeekAiClient` 不重复修改业务含义。
- 序列化失败返回 `OperationResult.failure(ErrorCode.AI_BAD_REQUEST, "AI request could not be serialized")`。

**响应 JSON 解析规则**：

- 仅当 HTTP 状态码为 `200..299` 时解析响应体。
- 响应体为 `null`、空字符串或 `isBlank()` 为 true 时返回 `AI_EMPTY_RESPONSE`，消息 `AI response is empty`。
- 使用 Jackson 读取 `JsonNode root = objectMapper.readTree(body)`。
- 成功响应内容路径固定为 `root.path("choices").get(0).path("message").path("content")` 的文本值。
- 以下场景返回 `OperationResult.failure(ErrorCode.AI_EMPTY_RESPONSE, "AI response is empty")`：
  - `choices` 缺失。
  - `choices` 不是数组。
  - `choices` 为空数组。
  - `choices[0].message` 缺失。
  - `choices[0].message.content` 缺失。
  - `content` 为 JSON null。
  - `content` 是文本但 `strip()` 后为空白。
- 以下场景返回 `OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI response format is invalid")`：
  - 响应体不是合法 JSON。
  - `choices[0]` 不是 object。
  - `message` 存在但不是 object。
  - `content` 存在但不是 textual node。
- 成功时返回 `OperationResult.success(new AiResponse(content.asText()))`；`AiResponse` 负责 `strip()`。

**HTTP 状态处理规则**：

- `statusCode` 在 `200..299`：按响应 JSON 解析规则处理。
- 非 `2xx`：调用 `errorMapper.mapHttpStatus(statusCode)`，返回 `OperationResult.failure(mappedCode, messageFor(mappedCode))`。
- 失败消息固定如下：

| ErrorCode | 消息 |
|-----------|------|
| `AI_AUTH_FAILED` | `DeepSeek authentication failed` |
| `AI_RATE_LIMITED` | `DeepSeek rate limit exceeded` |
| `AI_TIMEOUT` | `AI request timed out` |
| `AI_BAD_REQUEST` | `AI request was rejected` |
| `AI_REMOTE_UNAVAILABLE` | `DeepSeek service is unavailable` |
| `AI_NETWORK_ERROR` | `AI network request failed` |
| `AI_EMPTY_RESPONSE` | `AI response is empty` |
| `AI_MALFORMED_RESPONSE` | `AI response format is invalid` |

**异常处理规则**：

| 异常 | ErrorCode | 消息 | 附加动作 |
|------|-----------|------|----------|
| `HttpTimeoutException` | `AI_TIMEOUT` | `AI request timed out` | 无。 |
| 其他 `IOException` | `AI_NETWORK_ERROR` | `AI network request failed` | 无。 |
| `InterruptedException` | `AI_NETWORK_ERROR` | `AI request was interrupted` | 必须调用 `Thread.currentThread().interrupt()` 恢复中断标记。 |
| Jackson `JsonProcessingException` 或其他 JSON 解析异常 | `AI_MALFORMED_RESPONSE` | `AI response format is invalid` | 仅限解析成功 HTTP 响应时。 |
| URI 构造相关运行时异常 | `VALIDATION_ERROR` | `invalid DeepSeek endpoint` | 不调用 transport。 |

**构造方式**：

- 后续生产装配使用 `new DeepSeekAiClient(configuration, JdkAiHttpTransport.create(configuration.timeout()))`。
- 单元测试使用 `new DeepSeekAiClient(configuration, fakeTransport, objectMapper, new AiErrorMapper())`。

**类型关系**：

- 实现 `AiClient`。
- 依赖 `AiConfiguration`、`AiRequest`、`AiMessage`、`AiResponse`、`AiHttpTransport`、`AiHttpRequest`、`AiHttpResponse`、`AiErrorMapper`。
- 依赖 `assistant.common.OperationResult`、`assistant.common.ErrorCode`。
- 依赖 Jackson `ObjectMapper` / `JsonNode`。
- 不依赖控制台、仓储、`AiAssistantService`、环境变量或真实网络。

## 错误处理

本轮所有 DeepSeek 外部依赖失败均通过 `OperationResult.failure(...)` 返回，禁止向 `AiAssistantService` 或控制台层传播底层 HTTP、I/O、Jackson 异常。

| 场景 | 处理方式 |
|------|----------|
| `DeepSeekAiClient.chat(null)` | 返回 `VALIDATION_ERROR`，消息 `AI request is required`。 |
| 非法 endpoint 配置导致 URI 无法创建 | 返回 `VALIDATION_ERROR`，消息 `invalid DeepSeek endpoint`，不调用 transport。 |
| 请求 JSON 序列化失败 | 返回 `AI_BAD_REQUEST`，消息 `AI request could not be serialized`，不调用 transport 或不继续发送。 |
| 非 2xx HTTP 状态 | 由 `AiErrorMapper.mapHttpStatus(...)` 分类并返回固定消息。 |
| HTTP 2xx 但空白 body | 返回 `AI_EMPTY_RESPONSE`。 |
| HTTP 2xx 且 JSON 结构缺失可用文本内容 | 缺失或空白内容按 `AI_EMPTY_RESPONSE`；字段类型错误或非法 JSON 按 `AI_MALFORMED_RESPONSE`。 |
| JDK 超时异常 | 返回 `AI_TIMEOUT`。 |
| 网络 I/O 异常 | 返回 `AI_NETWORK_ERROR`。 |
| `InterruptedException` | 恢复线程中断标记，返回 `AI_NETWORK_ERROR`，消息表明请求被中断。 |

`JdkAiHttpTransport` 不捕获 `IOException` 或 `InterruptedException`，只负责转换和发送；分类由 `DeepSeekAiClient` 和 `AiErrorMapper` 完成。

## 行为契约

### `DeepSeekAiClient.chat(...)` 调用顺序

1. 若 `request == null`，立即返回 `VALIDATION_ERROR`。
2. 构造 endpoint URI；失败则返回 `VALIDATION_ERROR`，不得调用 transport。
3. 用 Jackson 序列化 OpenAI 兼容 JSON；失败则返回 `AI_BAD_REQUEST`。
4. 创建 `AiHttpRequest`，包含 URI、三类 headers、JSON body 和 `configuration.timeout()`。
5. 调用 `transport.send(...)`。
6. 若 transport 抛异常，按异常处理规则返回失败。
7. 若 status code 非 2xx，按 HTTP 状态处理规则返回失败，不解析 body。
8. 若 status code 为 2xx，解析 body 中 `choices[0].message.content`。
9. 内容有效时返回 `AiResponse` 成功结果。

### 不变式

- 普通单元测试不读取 `System.getenv()`、不访问 `https://api.deepseek.com`、不依赖真实 API Key。
- `DeepSeekAiClient` 不修改本地业务数据，不依赖任务、日程、学习计划、收支、笔记或 summary 包。
- `DeepSeekAiClient` 不把 API Key 写入失败消息。
- `AiHttpRequest.headers()` 返回不可修改快照；调用方后续修改原始 map 不影响请求。
- `JdkAiHttpTransport.send(...)` 固定使用 POST；不实现 GET、流式响应或异步发送。
- 对 `InterruptedException` 的测试必须在断言后清理当前线程中断状态，避免污染其他测试。

### 单元测试设计契约

`DeepSeekAiClientTest`：

- 定义私有 fake transport，记录最近一次 `AiHttpRequest`、调用次数，并可返回预置 `AiHttpResponse` 或抛出预置异常。
- 成功路径断言：
  - URI 为 `{baseUrl}{chatCompletionsPath}`。
  - headers 包含 `Authorization: Bearer placeholder-key`、`Content-Type: application/json`、`Accept: application/json`。
  - timeout 等于配置 timeout。
  - 请求 body JSON 可由 Jackson 解析，包含 `model`、`stream`、`messages[0].role`、`messages[0].content`。
  - 返回 payload content 为响应 JSON 中 `choices[0].message.content`。
- 失败路径至少覆盖：
  - `request == null`。
  - 空 body、空白 body、空 choices、缺失 content、空白 content。
  - 非法 JSON、`choices` 类型错误、`message` 类型错误、`content` 非文本。
  - `401`、`403`、`429`、`408`、`504`、`400`、`422`、至少一个未列明 `4xx`、至少一个 `3xx`、至少一个 `5xx`、至少一个其他未列明非 `2xx` 状态。
  - `IOException`、`HttpTimeoutException`、`InterruptedException` 并断言中断标记恢复。

`AiErrorMapperTest`：

- 覆盖 `401`、`403`、`429`、`408`、`504`、`400`、`422`。
- 覆盖未列明 `4xx` 映射 `AI_BAD_REQUEST`。
- 覆盖未列明 `3xx`、`5xx`、`1xx` 和误传 `2xx` 映射 `AI_REMOTE_UNAVAILABLE`。
- 覆盖 `HttpTimeoutException`、普通 `IOException`、`InterruptedException`、其他 `RuntimeException` 和 `null` 参数。

`JdkAiHttpTransportTest`：

- 覆盖 `new JdkAiHttpTransport(null)` 抛 `NullPointerException("httpClient")`。
- 覆盖 `create(null)` 和非正 connect timeout。
- 覆盖 `AiHttpRequest` 和 `AiHttpResponse` record 校验。
- 使用 Mockito mock `HttpClient`，捕获传入 `HttpRequest`，返回 mock `HttpResponse<String>`，断言 method、uri、timeout、headers 和返回 DTO status/body。
- 不对真实域名发起请求，不读取环境变量。

## 依赖关系

新增生产代码依赖：

- JDK 17：
  - `java.net.URI`
  - `java.net.http.HttpClient`
  - `java.net.http.HttpRequest`
  - `java.net.http.HttpResponse`
  - `java.net.http.HttpTimeoutException`
  - `java.time.Duration`
  - `java.io.IOException`
  - `java.nio.charset.StandardCharsets`
- Jackson Databind：
  - `com.fasterxml.jackson.databind.ObjectMapper`
  - `com.fasterxml.jackson.databind.JsonNode`
  - `com.fasterxml.jackson.core.JsonProcessingException`
- 项目已有类型：
  - `assistant.ai.AiClient`
  - `assistant.ai.AiConfiguration`
  - `assistant.ai.AiRequest`
  - `assistant.ai.AiMessage`
  - `assistant.ai.AiResponse`
  - `assistant.common.OperationResult`
  - `assistant.common.ErrorCode`

新增测试代码依赖：

- JUnit Jupiter。
- Mockito core / Mockito JUnit Jupiter，用于 mock `HttpClient` 和 `HttpResponse<String>`。
- Jackson `ObjectMapper`，用于断言请求 body JSON。

对后续任务暴露的公开接口：

- `AiHttpTransport` 可被后续集成测试、生产装配或代理 transport 复用。
- `JdkAiHttpTransport.create(Duration)` 可被后续应用装配使用。
- `DeepSeekAiClient` 可作为 `AiAssistantService` 的真实 `AiClient` 实现。
- `AiErrorMapper` 可被后续结构化建议解析或集成测试复用同一错误分类口径。
