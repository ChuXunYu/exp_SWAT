# 代码审查报告（v22 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/app/DemoDataFactoryTest.java` — 详细设计要求实现 `loadPropagatesServiceFailureAsIllegalStateException()`，但当前测试类只有 3 个用例，缺少服务写入失败时 `DemoDataFactory.load(...)` 转换为 `IllegalStateException` 且消息包含 `failed to load demo data` 和错误码的覆盖。

## 修改要求（仅 REJECTED 时）
- 在 `java-ai-assistant/src/test/java/assistant/app/DemoDataFactoryTest.java` 增加 `loadPropagatesServiceFailureAsIllegalStateException()`。该用例应按 `detail_v22.md` 第 337 行的契约，使用 mock 或手工构造的 `ApplicationServices`，让某个公开写入服务方法返回失败，断言 `DemoDataFactory.load(...)` 抛出 `IllegalStateException`，并且异常消息包含 `failed to load demo data` 和对应错误码。
