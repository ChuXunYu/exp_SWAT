# 测试审查报告（v22 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/app/ApplicationFactoryTest.java` — 未覆盖 `ApplicationFactory.create()` 的生产配置合并契约。详细设计明确要求生产入口读取 `System.getenv()` 与 JVM 系统属性并由系统属性覆盖同名环境变量，但现有测试只覆盖 `create(Map, TimeProvider)` 显式配置路径和注入 loader 的显式配置路径。若 `create()` 不读取系统属性、覆盖顺序错误，或错误地读取外部来源，当前测试无法发现。

- **[一般]** `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` — 未验证 `ConsoleApplication` 对服务层失败 `OperationResult` 的统一展示契约。详细设计要求所有失败展示必须包含 `ErrorCode` 名称和简短消息，且 `showTransactions()` 任一服务调用失败都应按失败展示。现有控制台测试基本使用真实成功服务和 AI 未配置路径，未构造任务、日程、学习计划、收支、笔记、草稿等入口的失败结果；如果这些入口漏调 `printResult`、吞掉错误码或遗漏消息，测试无法发现。

## 修改要求（仅 REJECTED 时）

- 在 `ApplicationFactoryTest.java` 增加针对 `create()` 的生产配置合并测试。建议通过临时设置同名 JVM 系统属性并注入 mock `AiConfigurationLoader` 捕获传入 map，断言系统属性值进入 loader 且覆盖同名配置来源；测试结束必须清理系统属性，避免污染其他测试。若无法稳定模拟环境变量，也至少要覆盖系统属性读取和传入 loader 的行为，因为这是生产入口与显式配置入口的关键差异。

- 在 `ConsoleApplicationTest.java` 增加失败 `OperationResult` 展示测试。可用 mock 服务组装 `ApplicationServices`，分别让关键入口返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "expected message")`，至少覆盖任务列表入口和收支统计的第二阶段失败；断言输出同时包含 `VALIDATION_ERROR` 与对应 message。覆盖目标是锁定控制台层对服务失败的统一处理，而不是服务本身逻辑。
