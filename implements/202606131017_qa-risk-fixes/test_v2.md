# 测试报告（v2）

## 编写范围
- `java-ai-assistant/src/test/java/assistant/ai/DraftImportServiceTest.java`
- `java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java`
- `java-ai-assistant/docs/test-cases.md`

## 覆盖说明
- 覆盖学习计划草稿含 breakdown 的确认导入成功路径：创建 1 个正式学习计划，并按 breakdown 顺序创建对应正式 TODO 任务。
- 覆盖 breakdown 任务字段映射：标题使用 breakdown 项原文，描述为 `来自学习计划：Learn Java`，优先级为 `TaskPriority.MEDIUM`，dueDate 为学习计划 endDate，状态为 TODO。
- 覆盖学习计划草稿无 breakdown 的边界路径：仅创建学习计划，不创建任务。
- 覆盖 `StudyPlanDraftContent` 对 breakdown 空白项的清洗结果：导入时只创建非空且已 trim 的 breakdown 任务。
- 覆盖学习计划创建失败路径：传播原错误，且不创建 breakdown 任务。
- 覆盖 breakdown 任务创建返回业务失败路径：回滚本次已创建 breakdown 任务，补偿删除本次学习计划，保留导入前既有任务。
- 覆盖 breakdown 任务创建抛出运行时异常路径：回滚本次已创建 breakdown 任务，补偿删除本次学习计划，并映射为 `SYSTEM_ERROR / "failed to import suggestion draft"`。
- 根据 v2 r2 审查意见修订两个 breakdown 补偿失败用例：导入前预置既有学习计划，并断言失败后学习计划列表仅保留该预置计划的 id、目标名和进度，防止误删非本次导入数据的实现通过测试。
- 修订 `DocumentationDeliveryTest.documentedWhiteBoxAndCoverageTestReferencesPointToExistingTestAssets` 的文档引用一致性验证为通用扫描：校验 `docs/test-cases.md` 与 `docs/coverage/README.md` 中所有 `*Test.methodName` 引用均对应真实测试类与真实 `@Test` 方法。
- 通用扫描覆盖本轮新增/调整的 `DraftImportServiceTest` 方法引用，包括 breakdown 成功导入、无 breakdown、学习计划创建失败、breakdown 任务创建失败和运行时异常补偿路径。
- 通用扫描发现 `docs/test-cases.md` 中既有 `FinanceServiceTest.addTransactionRejectsInvalidAmountAndDoesNotStore` 引用不存在，已修正为真实存在的 `FinanceServiceTest.recordTransactionRejectsInvalidAmountCategoryAndDateAndKeepsRepositoryUnchanged`，避免增强后的文档一致性测试被历史拼写错误阻断。

## 验证
- 已执行 `mvn test -Dtest=DocumentationDeliveryTest,DraftImportServiceTest`。
- 首次执行失败，原因是增强后的通用扫描发现 `docs/test-cases.md` 的既有 Finance 测试方法引用不存在。
- 修正文档引用后已重跑同一命令，结果通过：`DraftImportServiceTest` 12 个测试通过，`DocumentationDeliveryTest` 15 个测试通过，共 27 个测试通过，失败 0 个、错误 0 个。
- v2 r2 修订后已重跑 `mvn test -Dtest=DraftImportServiceTest,DocumentationDeliveryTest`，结果通过：`DraftImportServiceTest` 12 个测试通过，`DocumentationDeliveryTest` 15 个测试通过，共 27 个测试通过，失败 0 个、错误 0 个。
