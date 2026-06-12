# 测试审查报告（v19 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/ai/DeepSeekAiClientTest.java` — `chatBuildsOpenAiCompatibleRequestAndParsesSuccessResponse` 只使用 `AiRequest.nonStreaming(...)` 和单条 `USER` 消息，未约束 `request.stream()` 为 `true` 时必须序列化为 `stream: true`，也未约束多条消息及 `SYSTEM` / `ASSISTANT` 角色必须全部按顺序映射到 OpenAI 兼容 `messages` 数组。详细设计要求请求 JSON 使用 `request.stream()`，并将 `request.messages()` 全量转换为 `{"role": message.role().wireValue(), "content": message.content()}`。当前测试无法发现实现硬编码 `stream=false`、只发送第一条消息、丢失后续消息或错误映射非 USER 角色的回归。

- **[一般]** `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java` — `aiHttpRequestValidatesAndCopiesHeaders` 只用相对 URI 覆盖非法 URI，未覆盖“absolute 但缺少 host”的 URI，例如 `mailto:test@example.com` 或其他 `uri.isAbsolute()==true` 但 `uri.getHost()==null` 的输入。详细设计明确要求 `AiHttpRequest.uri` 必须是 absolute URI，且 `uri.getScheme()`、`uri.getHost()` 均不得为空白。当前测试无法发现实现仅检查 `isAbsolute()`、放过无 host URI 的回归。

- **[轻微]** `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java` — `sendConvertsRequestToJdkPostRequest` 的请求 body 只包含 ASCII 字符，虽然辅助方法按 UTF-8 读取 publisher 内容，但测试数据无法有效验证设计要求的 UTF-8 body 转换。若后续误用非 UTF-8 charset，当前断言仍可能通过。

## 修改要求（仅 REJECTED 时）

1. 在 `java-ai-assistant/src/test/java/assistant/ai/DeepSeekAiClientTest.java` 的请求构造测试附近新增或扩展用例，构造 `new AiRequest("model-a", List.of(new AiMessage(AiRole.SYSTEM, "..."), new AiMessage(AiRole.USER, "..."), new AiMessage(AiRole.ASSISTANT, "...")), true)`，解析 fake transport 收到的 JSON，断言 `stream` 为 `true`，`messages` 数组长度为 3，三条消息的 `role` 分别为 `system`、`user`、`assistant`，并断言 content 按输入顺序完整保留。

2. 在 `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java` 的 `AiHttpRequest` 校验测试中补充 absolute-but-no-host URI 的断言，例如 `URI.create("mailto:test@example.com")`，期望构造 `AiHttpRequest` 抛出 `IllegalArgumentException`。该断言需要直接覆盖详细设计中的 host 非空契约，而不只是覆盖相对 URI。
