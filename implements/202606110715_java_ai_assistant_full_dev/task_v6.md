# 任务指令（v6）

## 动作
NEW

## 任务描述
新增学习进度值对象和笔记标签值对象，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/common/Progress.java`
- `java-ai-assistant/src/main/java/assistant/common/Tag.java`
- `java-ai-assistant/src/test/java/assistant/common/ProgressTest.java`
- `java-ai-assistant/src/test/java/assistant/common/TagTest.java`

`Progress` 用于表达学习计划完成进度，必须集中封装 0 到 100 的合法边界，并提供后续学习计划状态分析可直接使用的完成判断语义。`Tag` 用于表达笔记标签，必须集中封装去除首尾空白、拒绝空标签以及大小写归一后的相等性语义，后续笔记服务、标签查询、标签分布统计和 AI 本地上下文均应复用该值对象。

本轮只实现 `assistant.common` 中的两个通用值对象及其单元测试，不实现 `study`、`note`、`summary` 或 `ai` 业务包，不实现学习计划服务、笔记服务、标签统计服务、控制台交互或 DeepSeek 接入。

## 选择理由
学习计划管理依赖稳定的进度边界：需求明确要求覆盖 0%、100%、小于 0、大于 100 等路径，技术方案要求进度达到 100 时视为已完成。提前实现 `Progress` 可避免后续学习计划实体和分析服务重复判断进度范围。

笔记管理依赖稳定的标签语义：需求要求按标签查询笔记并统计标签分布，技术方案要求标签通过 `Tag` 值对象统一清理和校验。提前实现 `Tag` 可避免后续笔记服务、搜索策略和汇总服务分别处理空标签、大小写和首尾空白。

## 任务上下文
完整需求和设计输入中的相关约束包括：

- `assistant.common` 应包含进度、标签等通用值对象。
- `Progress` 值对象范围为 0 到 100。
- 学习计划实体持有进度；进度达到 100 视为已完成，优先级高于日期状态。
- 学习计划测试需要覆盖进度为 0%、100%、小于 0、大于 100 等边界。
- `Tag` 值对象负责去除首尾空白，空标签拒绝；是否大小写归一统一在 `Tag` 中决定，服务层不重复处理。
- 笔记实体持有标签集合；标签查询和标签分布统计按 `Tag` 的统一语义比较。
- 普通单元测试不得依赖真实 DeepSeek、网络、API Key、真实当前时间或外部文件。

## 已有代码上下文
当前项目已经在 `java-ai-assistant/` 下建立 Maven 单模块工程，Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe 基线可用。

已完成的通用基础包括：

- `assistant.common.ErrorCode`、`BusinessException`、`OperationResult<T>`
- `assistant.common.EntityId`
- `assistant.common.DateRange`、`DateTimeRange`
- `assistant.common.TransactionAmount`、`MoneyValue`
- `assistant.testability.IdGenerator`、`IncrementalIdGenerator`
- `assistant.testability.TimeProvider`、`SystemTimeProvider`、`FixedTimeProvider`

现有 `assistant.common` 值对象主要采用 Java `record`、构造阶段标准异常校验、不可变语义和集中边界测试。`Progress` 与 `Tag` 应优先延续这一风格；如需公开工厂、展示文本、完成判断或归一化方法，由详细设计根据后续 `study` 与 `note` 模块的使用方式给出明确契约。
