# 实现报告（v19）

## 概述
在 `assistant.ai` 包中新增真实 DeepSeek/OpenAI 兼容协议层，包括可替换 HTTP transport 边界、JDK `HttpClient` transport、HTTP/异常错误映射器，以及基于 `AiClient` 的 `DeepSeekAiClient`。新增单元测试覆盖协议请求构造、成功响应解析、错误响应分类、异常转换、中断恢复、DTO 校验和 JDK 请求转换行为；普通单元测试不访问真实网络、不读取真实 API Key。

## 文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/AiHttpRequest.java` | 定义 transport 请求 DTO，校验 URI、headers、body、timeout，并复制不可修改 headers 快照。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/AiHttpResponse.java` | 定义 transport 响应 DTO，校验 HTTP status code 和非空 body。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/AiHttpTransport.java` | 定义可替换同步 HTTP 发送接口。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/JdkAiHttpTransport.java` | 使用 JDK `HttpClient` 将 `AiHttpRequest` 转换为 POST `HttpRequest` 并同步发送。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/AiErrorMapper.java` | 集中映射 HTTP 状态码和外部异常到稳定 `ErrorCode`。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/DeepSeekAiClient.java` | 实现 `AiClient`，完成请求 JSON 序列化、Bearer header 构造、transport 调用、响应 JSON 解析和失败结果转换。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/DeepSeekAiClientTest.java` | 使用 fake transport 覆盖请求构造、成功解析、空/畸形响应、HTTP 状态映射、I/O/超时/中断异常处理。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/AiErrorMapperTest.java` | 覆盖 HTTP 状态码分类和异常分类分支。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/JdkAiHttpTransportTest.java` | 覆盖 JDK transport 构造、DTO 校验、POST 请求转换和 null body 归一化。 |

## 编译验证
已执行：

```text
mvn test
```

结果：`BUILD SUCCESS`，共 `755` 个测试通过，`0` failures，`0` errors，`0` skipped。

## 设计偏差说明
无偏差。
