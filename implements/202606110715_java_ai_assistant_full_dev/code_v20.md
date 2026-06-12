# 实现报告（v20）

## 概述

按 `detail_v20.md` 在 `assistant.ai` 包中实现了 AI 结构化建议草稿的领域模型、只读视图、仓储接口与内存实现，以及严格 JSON 解析器。解析器使用 Jackson `JsonParser` 读取单个 `JsonNode` 后检查 EOF，确保完整 JSON 文本和 fenced JSON 内容都拒绝根值后的自然语言、第二个根 JSON 值或其他尾随 token。

同时新增对应单元测试，覆盖枚举辅助方法、DTO 归一化与校验、聚合根状态迁移、视图快照、仓储插入顺序与引用语义，以及解析器成功/失败路径和严格 JSON 完整性场景。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftType.java` | 定义结构化建议草稿类型枚举与类型判断方法。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftStatus.java` | 定义草稿生命周期状态枚举与状态判断方法。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/TaskDraftItem.java` | 实现不可变任务草稿项 DTO，包含标题、描述、优先级和可选截止日期校验归一化。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/StudyPlanDraftContent.java` | 实现不可变学习计划草稿内容 DTO，包含目标、日期范围、投入小时、初始进度和拆解说明快照。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraft.java` | 实现草稿聚合根，保证类型与内容匹配、初始可确认状态和状态迁移冲突处理。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftView.java` | 实现草稿只读快照 DTO，从聚合根复制当前状态和任务列表。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftRepository.java` | 定义草稿保存、按 id 查询、查询全部和删除的仓储契约。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/InMemorySuggestionDraftRepository.java` | 实现基于 `LinkedHashMap` 的内存草稿仓储，保持插入顺序并返回聚合根引用。 |
| 新建 | `java-ai-assistant/src/main/java/assistant/ai/StructuredSuggestionParser.java` | 实现严格结构化建议 JSON/fenced JSON 解析并统一返回 `AI_MALFORMED_RESPONSE`。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTypeTest.java` | 覆盖草稿类型枚举名称和辅助方法。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftStatusTest.java` | 覆盖草稿状态枚举名称和辅助方法。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/TaskDraftItemTest.java` | 覆盖任务草稿 DTO 归一化、可选截止日期和非法字段校验。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/StudyPlanDraftContentTest.java` | 覆盖学习计划草稿 DTO 归一化、拆解说明快照和字段校验。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTest.java` | 覆盖聚合根工厂、内容匹配、不可变任务列表和状态冲突。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftViewTest.java` | 覆盖视图字段映射、快照不可变和 null 参数校验。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/InMemorySuggestionDraftRepositoryTest.java` | 覆盖保存替换、按 id 查询、插入顺序、快照列表和删除行为。 |
| 新建 | `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionParserTest.java` | 覆盖任务/学习计划 JSON、fenced JSON、默认字段、类型/字段错误、非法日期/数值、自然语言包裹、尾随 token 和多个根 JSON 值。 |

## 编译验证

已执行：

```bash
cd /root/exp_SWAT/java-ai-assistant && mvn test
```

结果：`BUILD SUCCESS`，共 `791` 个测试通过，`0` 失败，`0` 错误，`0` 跳过。

## 设计偏差说明

无偏差。
