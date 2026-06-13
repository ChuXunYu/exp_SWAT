# 测试报告（v1）

## 概述
根据详细设计和实现报告，本轮补充文档交付的单元测试。测试基于行为契约验证公开交付物内容，不验证实现细节，不修改生产源码。

## 测试文件
| 文件路径 | 说明 |
|---------|------|
| `/root/exp_SWAT/java-ai-assistant/src/test/java/assistant/docs/DocumentationDeliveryTest.java` | 新增主实验指导书契约测试，复用项目既有文档交付测试风格。 |

## 新增用例
| 测试方法 | 覆盖契约 |
|---------|----------|
| `labGuideDocumentsRequiredCourseStructureAndProjectScope` | 验证主指导书包含课程标题、17 个主章节、4 个附录，以及 Java 17、Maven 单模块、控制台入口、内存仓储、外部依赖隔离和不测范围。 |
| `labGuideMapsRealModulesCommandsCiAndIntegrationBoundaries` | 验证主指导书列出真实生产类、推荐 Maven/Java 命令、CI 触发与命令，以及当前无 `*IT.java`、Failsafe 空跑不等于真实 DeepSeek 连通性的边界。 |
| `labGuideDocumentsHistoricalAcceptanceAsEvidenceAndRequiresCurrentExecutionResults` | 验证 2026-06-13 验收记录中的 952 个测试、verify、integration、JaCoCo 覆盖率数字均作为历史证据出现，并要求课程复核以当前输出为准。 |
| `labGuideDefinesDefectMetricsReportRequirementsAndOneHundredPointRubric` | 验证缺陷模板、严重级别、质量度量、报告提交材料，并用解析评分表的方式断言分值合计为 100。 |

## 覆盖维度
- 正常路径：主指导书结构、项目事实、模块索引、命令和 CI 信息完整。
- 边界条件：集成测试入口存在但当前无 `*IT.java`，不得误判为真实 DeepSeek 连通性验证。
- 错误路径：历史测试数量和覆盖率不得伪造成当前执行结果。
- 状态交互：评分标准通过总分计算验证，避免仅检查“100”文本存在。

## 执行说明
按测试编写角色要求，本轮只负责编写单元测试，未运行 Maven 或 JUnit 命令。

## 设计偏差
无。
