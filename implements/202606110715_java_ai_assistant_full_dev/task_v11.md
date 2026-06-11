# 任务指令（v11）

## 动作
NEW

## 任务描述
新增学习计划模块的核心领域实体、状态枚举和状态分析组件，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/study/StudyPlanStatus.java`
- `java-ai-assistant/src/main/java/assistant/study/StudyPlan.java`
- `java-ai-assistant/src/main/java/assistant/study/StudyPlanAnalysisService.java`
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanStatusTest.java`
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanTest.java`
- `java-ai-assistant/src/test/java/assistant/study/StudyPlanAnalysisServiceTest.java`

本轮只实现学习计划领域模型与状态分析基础，不实现仓储、只读视图和应用服务闭环；这些留给后续轮次。

## 选择理由
任务待办和日程提醒两个核心模块已经形成服务闭环，可以进入 8 个核心功能中的学习计划管理。学习计划服务、汇总服务和 AI 拆解上下文后续都依赖稳定的计划实体、动态状态分类和统一分析规则；先实现领域模型与分析组件，可集中固定目标名称、日期周期、预期投入、进度更新、完成优先级和逾期判断等高分支规则，避免后续服务层重复判断。

## 任务上下文
必须依据以下要求实现：

- 学习计划实体持有目标名称、开始日期、截止日期、预期投入小时数和进度。
- 记录类数据必须持有唯一标识，学习计划使用既有 `EntityId`。
- 计划周期使用既有 `DateRange` 表达，开始日期晚于截止日期应拒绝创建。
- 进度使用既有 `Progress` 值对象，范围为 0 到 100；进度达到 100 视为已完成。
- 计划状态由 `StudyPlanAnalysisService` 统一推导，至少覆盖：未开始、进行中、已完成、逾期未完成。
- 状态推导中，进度 100 的已完成优先级高于日期状态；未完成计划在当前日期晚于截止日期时为逾期未完成；当前日期早于开始日期时为未开始；当前日期位于计划周期内且未完成时为进行中。
- 分析组件后续会被完成数量统计、本周统计、汇总服务和 AI 本地上下文复用，因此状态判断不得散落在实体或服务外部。
- 普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件；状态分析测试必须显式传入固定 `LocalDate`。

## 已有代码上下文
当前项目位于 `java-ai-assistant/`，已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线与以下相关类型：

- `assistant.common.EntityId`：正整数唯一编号值对象，任务、日程和后续学习计划等可修改记录应复用。
- `assistant.common.DateRange`：左右闭日期区间，构造时拒绝开始日期晚于结束日期，提供 `contains(...)` 和 `overlaps(...)`。
- `assistant.common.Progress`：0 到 100 的进度值对象，提供 `zero()`、`complete()`、`of(...)`、`isComplete()` 和百分比展示。
- `assistant.testability.TimeProvider`：提供可注入当前日期和当前时间；本轮分析组件可以直接接收 `LocalDate`，后续服务再负责从 `TimeProvider.today()` 传入。
- `assistant.task.TaskItem` 和 `assistant.schedule.ScheduleItem` 已体现当前项目领域实体风格：字段在构造和更新入口规范化，必填文本 `strip()` 后不能为空，可选文本 `null` 转为空字符串。
- `assistant.schedule.ScheduleStatus` 已体现动态状态 enum 风格，可参考其 `isUpcoming()` 等语义方法命名方式。

测试要求：

- `StudyPlanStatusTest` 覆盖四种状态及语义方法。
- `StudyPlanTest` 覆盖目标名称去空白、空目标拒绝、日期区间合法性、预期投入小时数校验、默认/指定进度、更新目标信息、更新进度为 0 和 100、非法空依赖快速失败。
- `StudyPlanAnalysisServiceTest` 覆盖 0%、100%、未开始、进行中、截止日当天仍进行中、当前日期晚于截止日期时逾期未完成、已完成优先级高于未开始和逾期、空参数快速失败。

## RETRY 说明（仅 RETRY 时）
无。
