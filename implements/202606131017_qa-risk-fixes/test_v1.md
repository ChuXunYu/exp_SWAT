# 测试报告（v1）

## 编写范围
- `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionDraftServiceTest.java`
- `java-ai-assistant/src/test/java/assistant/app/ApplicationFactoryTest.java`
- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

## 覆盖说明
- 覆盖结构化任务草稿生成成功路径：AI 返回合法 `TASK_DRAFT` JSON 后分配固定草稿 id、解析任务字段、保存草稿并返回视图。
- 覆盖结构化学习计划草稿生成成功路径：AI 返回合法 `STUDY_PLAN_DRAFT` JSON 后保存草稿，并保留 `breakdown`。
- 覆盖任务草稿缺失 `dueDate` 的保存前校验：返回 `VALIDATION_ERROR`，不保存草稿。
- 覆盖生成服务错误路径：AI 未配置、空响应、 malformed JSON、非法结构化字段、入口类型不匹配、空白用户目标和构造器空依赖。
- 覆盖应用装配：`ApplicationFactory` 创建新增 `StructuredSuggestionDraftService`，`ApplicationServices` 对新增字段执行空值校验。
- 覆盖控制台 AI 草稿入口：任务草稿生成、学习计划草稿生成、生成后列表查看、详情查看、空白目标拦截、失败提示、确认导入、取消和重复确认保护。

## 说明
本步骤只负责编写和确认测试，不运行测试命令。

