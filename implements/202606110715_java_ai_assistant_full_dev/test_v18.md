# 测试报告（v18）

## 概述
根据 `detail_v18.md` 的行为契约，为 `assistant.ai` 离线 AI 基础编排层补充和增强单元测试。测试仅验证公开接口行为，不依赖真实 API Key、环境变量、文件系统、网络或真实 DeepSeek HTTP 调用。

## 测试文件
| 文件路径 | 覆盖重点 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/ai/AiConfigurationTest.java` | 默认 DeepSeek 配置、字符串规范化、API Key 判断、非法字段、空引用参数名和安全 `toString()`。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiConfigurationLoaderTest.java` | 显式 `Map` 配置加载、默认回退、空白覆盖、非法超时、非法配置和空 `Map` 参数名。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiMessageTest.java` | OpenAI 兼容 role wire value、内容清理、空引用参数名和空白内容错误消息。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiRequestTest.java` | 非流式工厂、model 清理、消息列表不可变快照、空引用参数名、空消息列表错误消息。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiResponseTest.java` | 响应内容清理、空引用参数名和空白内容错误消息。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiScenarioTest.java` | 非结构化场景指令、结构化 JSON 标记和目标类型 `TASK_DRAFT` / `STUDY_PLAN_DRAFT`。 |
| `java-ai-assistant/src/test/java/assistant/ai/PromptBuilderTest.java` | 非流式请求、固定消息顺序、用户问题清理、上下文总览和五类明细区块、空列表标记、结构化提示词、输入校验和空依赖参数名。 |
| `java-ai-assistant/src/test/java/assistant/ai/AiAssistantServiceTest.java` | 构造器空依赖、配置缺失短路、空场景/空问题校验、上下文失败、提示词失败、客户端失败、空响应、成功路径请求传递和只读交互。 |

## 覆盖说明
- 每个本轮新增公开类型均有对应单元测试文件。
- 正常路径覆盖默认配置、显式配置加载、DTO 规范化、场景指令、提示词构造和服务成功返回。
- 边界条件覆盖空白配置、空白用户问题、空上下文明细、不可修改消息列表和构造后输入列表变更。
- 错误路径覆盖空引用参数名、非法字段、非法超时、配置未设置、上下文失败、提示词失败、AI 客户端错误和客户端空 payload。
- 状态交互覆盖 `AiAssistantService.ask(...)` 的短路顺序，验证未配置或输入非法时不会调用上下文、提示词构造或 AI 客户端。

## 验证状态
本轮遵循 `verifier.md` 指令，只负责编写单元测试，不运行测试命令。建议后续 Runner 执行：

```bash
mvn -q -Dtest='assistant.ai.*Test' test
mvn -q test
```
