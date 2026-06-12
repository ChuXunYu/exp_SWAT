# 测试报告（v19）

## 概述

本轮围绕 `assistant.ai` DeepSeek/OpenAI 兼容协议层补充并确认单元测试。测试基于详细设计中的公开行为契约编写，不访问真实 DeepSeek 网络、不读取真实 API Key。

## 测试文件

| 文件路径 | 覆盖范围 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/ai/DeepSeekAiClientTest.java` | 使用 fake transport 覆盖请求构造、成功响应解析、空响应、畸形 JSON、HTTP 状态映射、序列化失败、I/O/超时/中断异常处理。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiErrorMapperTest.java` | 覆盖 HTTP 状态码分类和异常分类分支。 |
| `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java` | 覆盖 JDK transport 构造校验、DTO 校验、POST 请求转换和 null body 归一化。 |

## 本轮补充

- 根据 `test_review_v19_r1.md` 反馈，在 `DeepSeekAiClientTest` 新增 `chatSerializesStreamingRequestWithAllMessagesInOrder`。
- 断言 `request.stream() == true` 时请求 JSON 序列化为 `stream: true`。
- 断言 `SYSTEM`、`USER`、`ASSISTANT` 三条消息全部按输入顺序转换为 OpenAI 兼容 `messages` 数组，且 `role` / `content` 完整保留。
- 根据 `test_review_v19_r1.md` 反馈，在 `JdkAiHttpTransportTest` 的 `AiHttpRequest` 校验中补充 absolute-but-no-host URI（`mailto:test@example.com`）应抛出 `IllegalArgumentException`。
- 采纳轻微建议，将 JDK POST 请求转换测试的 body 改为包含非 ASCII 内容，继续通过 `StandardCharsets.UTF_8` 读取 publisher 内容并断言完整保留。

## 设计契约覆盖

- `DeepSeekAiClient.chat(...)`：
  - `request == null` 返回 `VALIDATION_ERROR`，且不调用 transport。
  - 非法 endpoint 返回 `VALIDATION_ERROR`，且不调用 transport。
  - OpenAI 兼容请求 URI、headers、timeout、model、stream、messages 序列化结构正确。
  - 流式请求序列化 `stream: true`，多角色消息按输入顺序完整保留。
  - 序列化失败返回 `AI_BAD_REQUEST`，且不调用 transport。
  - `2xx` 成功响应解析 `choices[0].message.content`，并由 `AiResponse` 规范化内容。
  - 空 body、空白 body、空 choices、缺失 content、JSON null content、空白 content 映射 `AI_EMPTY_RESPONSE`。
  - 非法 JSON、非 object choice、非 object message、非文本 content 映射 `AI_MALFORMED_RESPONSE`。
  - `401`、`403`、`429`、`408`、`504`、`400`、`422`、未列明 `4xx`、`3xx`、`5xx`、`1xx` 状态码映射到设计规定错误码和消息。
  - `IOException`、`HttpTimeoutException`、`InterruptedException` 映射到设计规定错误码和消息，并验证中断标记恢复。
- `AiErrorMapper`：
  - 覆盖显式 HTTP 状态码、状态码类别、异常类别和 null 参数校验。
- `JdkAiHttpTransport` / DTO：
  - 覆盖构造器 null 校验、`create` timeout 校验、`AiHttpRequest` 校验和 headers 不可变快照、`AiHttpResponse` 校验。
  - 覆盖相对 URI 与 absolute 但无 host URI 均被 `AiHttpRequest` 拒绝。
  - 使用 Mockito mock `HttpClient` 捕获 `HttpRequest`，验证 POST、URI、timeout、headers、UTF-8 body 和响应 DTO 转换。
  - 验证 JDK 响应 body 为 null 时归一化为空字符串。

## 验证说明

按测试编写 Agent 指令，本轮职责是编写测试。为交付前自检，已在子项目目录执行：

```text
mvn test -Dtest=DeepSeekAiClientTest,JdkAiHttpTransportTest
```

结果：`BUILD SUCCESS`，共 `18` 个测试通过，`0` failures，`0` errors，`0` skipped。
