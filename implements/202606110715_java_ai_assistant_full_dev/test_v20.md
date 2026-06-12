# 测试报告（v20）

## 概述

本轮围绕 `assistant.ai` 结构化建议草稿模型、只读视图、内存仓储和严格 JSON 解析器补充并确认单元测试。测试基于 `detail_v20.md` 的公开行为契约编写，不访问真实网络、不调用正式任务或学习计划导入服务。

## 测试文件

| 文件路径 | 覆盖范围 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTypeTest.java` | 覆盖结构化建议草稿类型枚举值、名称稳定性和类型判断辅助方法。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftStatusTest.java` | 覆盖草稿生命周期状态枚举值、名称稳定性、可确认状态和终态判断。 |
| `java-ai-assistant/src/test/java/assistant/ai/TaskDraftItemTest.java` | 覆盖任务草稿字段归一化、空描述默认值、可选截止日期和必填字段校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/StudyPlanDraftContentTest.java` | 覆盖学习计划草稿字段归一化、拆解说明过滤与不可变快照、日期/数值/null 校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTest.java` | 覆盖草稿工厂方法、任务列表快照、类型内容互斥、初始状态、取消/导入状态迁移和终态冲突。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftViewTest.java` | 覆盖从聚合根创建只读快照、状态快照不随聚合根后续变化、任务列表不可变和 null 参数校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/InMemorySuggestionDraftRepositoryTest.java` | 覆盖保存/替换、按 id 查询、插入顺序、返回聚合根引用、`findAll` 快照列表和删除行为。 |
| `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionParserTest.java` | 覆盖任务/学习计划 JSON、默认字段、fenced JSON、字段缺失/类型错误、枚举和日期错误、自然语言包裹、尾随 token、连续根 JSON 值和构造参数校验。 |

## 本轮补充

- 在 `SuggestionDraftTest` 新增 `terminalStatesRejectOppositeTransition`，补齐 `CANCELLED -> IMPORTED` 与 `IMPORTED -> CANCELLED` 的交叉冲突断言，确认终态保持不变且错误码为 `STATE_CONFLICT`。
- 在 `StructuredSuggestionParserTest` 补充字段类型与缺失边界：
  - 任务 `description` 非字符串失败。
  - 任务 `dueDate` 非字符串失败。
  - 学习计划缺失 `expectedHours` 失败。
  - 学习计划 `expectedHours` 非整数失败。
  - 学习计划 `startDate` 非字符串失败。
- 在 `StructuredSuggestionParserTest` 补充 `new StructuredSuggestionParser(null)` 的编程错误校验。

## 设计契约覆盖

- 新建 `SuggestionDraft` 状态固定为 `CONFIRMABLE`，任务草稿与学习计划草稿内容互斥。
- `TaskDraftItem`、`StudyPlanDraftContent`、`SuggestionDraft.getTasks()`、`SuggestionDraftView.tasks()` 和仓储 `findAll()` 的列表返回值均不可修改或为不可修改快照。
- `SuggestionDraftView.from(draft)` 复制创建时刻状态；聚合根后续 `cancel()` 不改变旧 view。
- `cancel()` 与 `markImported()` 只允许从 `CONFIRMABLE` 发起；重复迁移和终态交叉迁移均抛 `BusinessException(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable")`。
- `InMemorySuggestionDraftRepository` 保存同一聚合根引用，`findAll()` 保持插入顺序，同 id 替换不改变原 key 位置。
- `StructuredSuggestionParser` 成功解析任务草稿和学习计划草稿，并正确应用 `description`、`dueDate`、`initialProgress`、`breakdown` 默认值。
- `StructuredSuggestionParser` 对完整 JSON 和单个 fenced JSON block 使用严格单根值规则，拒绝自然语言包裹、根值后尾随文本、连续两个 JSON 根值、多个 fenced block 和 fenced 内容内尾随文本。
- `StructuredSuggestionParser` 对 AI 输出结构异常统一返回 `AI_MALFORMED_RESPONSE`，对调用方 `draftId == null` 抛 `NullPointerException`。

## 验证说明

按测试编写 Agent 指令，本轮职责是编写测试。为交付前自检，先在项目根目录尝试执行：

```text
mvn -pl java-ai-assistant -Dtest='assistant.ai.*Test' test
```

结果：命令未进入测试执行，原因是 `/root/exp_SWAT` 不是包含 `java-ai-assistant` module 的 Maven reactor，Maven 报告 `Could not find the selected project in the reactor: java-ai-assistant`。

随后在子项目目录 `/root/exp_SWAT/java-ai-assistant` 执行：

```text
mvn -Dtest='assistant.ai.*Test' test
```

结果：`BUILD SUCCESS`，共 `101` 个测试通过，`0` failures，`0` errors，`0` skipped。
