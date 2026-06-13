# 测试报告（v1）

## 测试编写概述
基于详细设计 `detail_v1.md` 和实现报告 `code_v1.md`，本轮补充 README 交付文档的单元测试断言。测试聚焦公开文档行为契约，不验证实现细节，不修改业务源码。

## 新增/修改测试文件
| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java` | 修改 | 在现有文档交付测试中补充 README 行为契约断言。 |

## 测试用例覆盖
| 测试方法 | 覆盖契约 |
|---------|----------|
| `readmeDocumentsRunnableEntryPointCommandsWithoutUnsupportedCliOrJarClaims` | 验证 README 提供真实 Maven classpath 构造、编译、交互启动、非交互 smoke、关闭演示数据启动命令；同时防止文档声明不存在的 CLI 参数或可执行 jar `Main-Class`。 |
| `readmeDocumentsAiAndDemoDataConfigurationContracts` | 验证 README 记录 DeepSeek 与演示数据配置变量、默认 base URL、默认模型、chat completions path、无 API Key 隔离行为，以及 API Key 不得提交。 |
| `readmeDocumentsCurrentFeatureSurfaceAndKnownLimitationsOnly` | 验证 README 描述当前控制台功能面、主菜单、内存数据和已知限制；防止文档声称账号、联系人或健康管理等不存在能力。 |

## 已保留测试
保留并扩展既有 `readmeDocumentsTestDeliverablesCoverageCommandIntegrationBoundaryAndBaseline` 断言，继续保护：

- README 新增章节标题。
- `mvn clean test`、`mvn clean verify`、`mvn jacoco:report`、`mvn -Pintegration verify`。
- `passed 944 tests with 0 failures`、`does not contain \`*IT.java\` classes`、`does not mean that a real DeepSeek connectivity test already exists`。
- 测试文档链接和 JaCoCo HTML 路径。

## 执行说明
按测试编写 Agent 指令，本步骤只负责编写单元测试，不负责运行测试。未执行 Maven 测试命令。
