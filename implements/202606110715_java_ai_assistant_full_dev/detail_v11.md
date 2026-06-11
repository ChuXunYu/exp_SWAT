# 详细设计（v11）

## 概述

本轮设计目标是在 `java-ai-assistant` Maven 工程中新增 `assistant.study` 学习计划模块的核心领域模型与状态分析基础：`StudyPlanStatus`、`StudyPlan` 和 `StudyPlanAnalysisService`。

本轮只固定学习计划的实体不变量、进度更新入口和动态状态推导规则，不实现仓储、只读视图、应用服务、统计服务或 AI 拆解导入闭环。学习计划实体负责保护目标名称、计划周期、预期投入小时数和进度值；动态状态统一由 `StudyPlanAnalysisService` 基于调用方显式传入的 `LocalDate` 推导，生产代码和测试代码均不得在状态分析中读取真实系统日期。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/study/StudyPlanStatus.java` | 新建 | 定义学习计划动态状态枚举，覆盖未开始、进行中、已完成和逾期未完成，并提供稳定展示名与语义判断方法。 |
| `java-ai-assistant/src/main/java/assistant/study/StudyPlan.java` | 新建 | 定义学习计划领域实体，持有编号、目标名称、日期周期、预期投入小时数和进度，并提供详情更新与进度更新入口。 |
| `java-ai-assistant/src/main/java/assistant/study/StudyPlanAnalysisService.java` | 新建 | 定义无状态分析组件，集中推导学习计划在指定当前日期下的动态状态。 |
| `java-ai-assistant/src/test/java/assistant/study/StudyPlanStatusTest.java` | 新建 | 覆盖四种状态、展示名、稳定枚举名和语义方法。 |
| `java-ai-assistant/src/test/java/assistant/study/StudyPlanTest.java` | 新建 | 覆盖实体构造、文本规范化、日期周期、预期投入小时数、默认/指定进度、详情更新、进度更新和非法参数快速失败。 |
| `java-ai-assistant/src/test/java/assistant/study/StudyPlanAnalysisServiceTest.java` | 新建 | 覆盖 0%、100%、未开始、进行中、截止日当天、逾期、完成优先级和空参数快速失败。 |

## 类型定义

### `StudyPlanStatus`

**形态**：`enum`

**包路径**：`assistant.study`

**职责**：表达学习计划基于进度和当前日期推导出的动态状态。状态不持久化在 `StudyPlan` 中，统一由 `StudyPlanAnalysisService` 计算。

**枚举常量**：

| 常量 | 展示名 | 语义 |
|------|--------|------|
| `NOT_STARTED` | `未开始` | 当前日期早于计划开始日期，且进度未达到 100。 |
| `IN_PROGRESS` | `进行中` | 当前日期位于计划周期左右闭区间内，且进度未达到 100。 |
| `COMPLETED` | `已完成` | 进度达到 100，优先级高于所有日期状态。 |
| `OVERDUE_INCOMPLETE` | `逾期未完成` | 当前日期晚于截止日期，且进度未达到 100。 |

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final String displayName` | 构造后不可变 | 枚举内部固定非空中文展示名。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public String displayName()` | `String` | 返回稳定中文展示名。 |
| `public boolean isNotStarted()` | `boolean` | 当且仅当当前实例为 `NOT_STARTED` 时返回 `true`。 |
| `public boolean isInProgress()` | `boolean` | 当且仅当当前实例为 `IN_PROGRESS` 时返回 `true`。 |
| `public boolean isCompleted()` | `boolean` | 当且仅当当前实例为 `COMPLETED` 时返回 `true`。 |
| `public boolean isOverdueIncomplete()` | `boolean` | 当且仅当当前实例为 `OVERDUE_INCOMPLETE` 时返回 `true`。 |

**构造方式**：

- 仅通过枚举常量使用。
- 私有枚举构造器签名为 `StudyPlanStatus(String displayName)`。

**类型关系**：

- 被 `StudyPlanAnalysisService.analyzeStatus(...)` 返回。
- 后续会被学习计划查询、视图、统计汇总和 AI 本地上下文复用。

### `StudyPlan`

**形态**：`class`

**包路径**：`assistant.study`

**职责**：表示一个正式学习计划领域实体，集中保护目标名称、计划周期、预期投入小时数和进度的不变量。实体不提供 `statusAt(...)` 一类动态状态方法，避免状态推导散落；只暴露进度是否完成的基础语义。

**类型签名定义**：`public class StudyPlan`

**字段定义**：

| 字段签名 | 可变性 | 约束 |
|----------|--------|------|
| `private final EntityId id` | 构造后不可变 | 必须非空；复用 `assistant.common.EntityId`。 |
| `private String goalName` | 可通过详情更新修改 | 必须非空；保存 `strip()` 后文本；去除首尾空白后不得为空；保留内部空白。 |
| `private DateRange period` | 可通过详情更新修改 | 必须非空；复用 `assistant.common.DateRange` 左右闭日期区间；开始日期晚于截止日期由 `DateRange` 拒绝。 |
| `private int expectedHours` | 可通过详情更新修改 | 必须大于 0；表示预期投入小时数。 |
| `private Progress progress` | 可通过进度更新修改 | 必须非空；复用 `assistant.common.Progress`，范围 0 到 100。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public StudyPlan(EntityId id, String goalName, DateRange period, int expectedHours, Progress progress)` | 构造器 | 校验并保存全部字段；空编号、空目标名称、空周期或空进度抛出 `NullPointerException`；空白目标名称或 `expectedHours <= 0` 抛出 `IllegalArgumentException`。 |
| `public static StudyPlan create(EntityId id, String goalName, DateRange period, int expectedHours)` | `StudyPlan` | 创建默认进度为 `Progress.zero()` 的学习计划。 |
| `public static StudyPlan create(EntityId id, String goalName, DateRange period, int expectedHours, Progress progress)` | `StudyPlan` | 创建指定初始进度的学习计划，等价于调用公开构造器。 |
| `public EntityId getId()` | `EntityId` | 返回计划编号。 |
| `public String getGoalName()` | `String` | 返回规范化后的目标名称。 |
| `public DateRange getPeriod()` | `DateRange` | 返回计划日期周期值对象。 |
| `public LocalDate getStartDate()` | `LocalDate` | 返回 `period.startDate()`。 |
| `public LocalDate getEndDate()` | `LocalDate` | 返回 `period.endDate()`。 |
| `public int getExpectedHours()` | `int` | 返回预期投入小时数。 |
| `public Progress getProgress()` | `Progress` | 返回当前进度值对象。 |
| `public boolean isCompleted()` | `boolean` | 返回 `progress.isComplete()`；仅表达进度完成语义，不替代动态状态分析。 |
| `public void updateDetails(String goalName, DateRange period, int expectedHours)` | `void` | 更新目标名称、日期周期和预期投入小时数；先完成全部校验，再一次性写入字段；非法输入时实体保持原状态。 |
| `public void updateProgress(Progress progress)` | `void` | 更新进度；`progress == null` 时抛出 `NullPointerException`；进度范围由 `Progress` 保证。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String normalizeGoalName(String goalName)` | `String` | `goalName == null` 时抛出 `NullPointerException`；返回 `goalName.strip()`；结果为空时抛出 `IllegalArgumentException`。 |
| `private static int requirePositiveExpectedHours(int expectedHours)` | `int` | `expectedHours <= 0` 时抛出 `IllegalArgumentException`；否则返回原值。 |

**构造方式**：

- 服务层和后续草稿导入优先使用 `create(...)` 工厂方法。
- 测试可直接调用公开构造器覆盖指定进度场景。
- 调用方需要传入 `DateRange`，不在本实体中新增起止日期裸参数重载，以保持计划周期语义集中在既有值对象中。

**类型关系**：

- 组合 `assistant.common.EntityId`、`assistant.common.DateRange` 和 `assistant.common.Progress`。
- 被 `StudyPlanAnalysisService` 读取并分析。
- 后续会被学习计划仓储、服务、视图、汇总统计和 AI 上下文模块复用。

### `StudyPlanAnalysisService`

**形态**：`class`

**包路径**：`assistant.study`

**职责**：集中推导学习计划动态状态，避免完成优先级、逾期判断和日期边界判断散落到实体、服务、汇总或 AI 上下文模块。

**类型签名定义**：`public final class StudyPlanAnalysisService`

**字段定义**：无实例字段；该组件无状态。

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public StudyPlanStatus analyzeStatus(StudyPlan plan, LocalDate currentDate)` | `StudyPlanStatus` | `plan == null` 或 `currentDate == null` 时抛出 `NullPointerException`；按统一优先级返回动态状态。 |
| `public boolean isCompleted(StudyPlan plan)` | `boolean` | `plan == null` 时抛出 `NullPointerException`；返回 `plan.getProgress().isComplete()`，供后续完成数量统计复用。 |
| `public boolean isOverdueIncomplete(StudyPlan plan, LocalDate currentDate)` | `boolean` | `plan == null` 或 `currentDate == null` 时抛出 `NullPointerException`；返回 `analyzeStatus(plan, currentDate).isOverdueIncomplete()`。 |

**状态推导优先级**：

1. 若 `plan.getProgress().isComplete()` 为 `true`，返回 `StudyPlanStatus.COMPLETED`。
2. 否则若 `currentDate.isAfter(plan.getEndDate())` 为 `true`，返回 `StudyPlanStatus.OVERDUE_INCOMPLETE`。
3. 否则若 `currentDate.isBefore(plan.getStartDate())` 为 `true`，返回 `StudyPlanStatus.NOT_STARTED`。
4. 否则返回 `StudyPlanStatus.IN_PROGRESS`。

**构造方式**：

- 使用默认无参构造器创建。
- 不持有 `TimeProvider`；本轮状态分析必须由调用方显式传入固定 `LocalDate`。后续学习计划服务再负责从 `TimeProvider.today()` 取得日期并传入。

**类型关系**：

- 依赖 `java.time.LocalDate`、`java.util.Objects`。
- 依赖 `assistant.study.StudyPlan` 和 `assistant.study.StudyPlanStatus`。
- 后续会被 `StudyPlanService`、本周学习统计、完成数量统计、汇总服务和 AI 本地上下文复用。

## 错误处理

- `StudyPlanStatus` 不处理外部输入解析；未知枚举名由 Java `Enum.valueOf(...)` 自然抛出 `IllegalArgumentException`。
- `StudyPlan` 对装配或调用方编程错误快速失败：空编号、空目标名称、空周期、空进度抛出 `NullPointerException`；空白目标名称和非正预期投入小时数抛出 `IllegalArgumentException`。
- `DateRange` 负责拒绝开始日期晚于截止日期；`StudyPlan` 不复制该校验逻辑，但必须对空 `DateRange` 快速失败。
- `Progress` 负责拒绝小于 0 或大于 100 的进度；`StudyPlan` 不复制该校验逻辑，但必须对空 `Progress` 快速失败。
- `updateDetails(...)` 必须先完成目标名称、周期和预期投入小时数校验，再修改实体字段；任何校验异常都不得造成部分字段更新。
- `StudyPlanAnalysisService` 对空计划或空当前日期抛出 `NullPointerException`。本轮没有应用服务边界，不将异常转换为 `OperationResult`。

## 行为契约

- 学习计划编号创建后不可修改；目标名称、日期周期、预期投入小时数和进度可通过实体方法修改。
- 目标名称规范化使用 `String.strip()`，去除首尾 Unicode 空白；空白目标拒绝；内部空格、制表符或换行保持原样。
- `expectedHours` 必须大于 0；0 和负数均为非法输入。
- 默认创建的学习计划进度为 `Progress.zero()`；指定进度创建时保存调用方传入的 `Progress`。
- `updateProgress(Progress.zero())` 和 `updateProgress(Progress.complete())` 均为合法操作。
- `StudyPlan.isCompleted()` 只代表进度达到 100，不代表根据日期推导出的完整状态。动态状态必须通过 `StudyPlanAnalysisService` 获得。
- `DateRange` 为左右闭日期区间，因此未完成计划在开始日当天和截止日当天均为 `IN_PROGRESS`。
- 当前日期晚于截止日期才进入 `OVERDUE_INCOMPLETE`；截止日当天未完成仍是 `IN_PROGRESS`。
- 进度 100 的完成优先级高于日期状态：即使当前日期早于开始日期或晚于截止日期，也返回 `COMPLETED`。
- 状态分析不得调用 `LocalDate.now()`、`LocalDateTime.now()`、`System.currentTimeMillis()` 或 `TimeProvider`；普通单元测试必须显式传入固定 `LocalDate`。

## 依赖关系

- 新增生产包为 `assistant.study`，不新增 Maven 依赖。
- 复用已有通用类型：`assistant.common.EntityId`、`assistant.common.DateRange`、`assistant.common.Progress`。
- 复用 Java 标准库：`java.time.LocalDate`、`java.util.Objects`。
- 测试继续使用 JUnit Jupiter，不访问网络、真实 API Key、真实系统时间或外部文件。
- 本轮不依赖 `assistant.testability.TimeProvider`；后续学习计划应用服务再注入 `TimeProvider` 并调用 `today()`。

## 单元测试规格

### `StudyPlanStatusTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void exposesFixedStatusValuesInDeclaredOrder()` | 断言枚举顺序为 `NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`OVERDUE_INCOMPLETE`。 |
| `void displayNameReturnsStableChineseText()` | 断言四种状态展示名分别为 `未开始`、`进行中`、`已完成`、`逾期未完成`。 |
| `void notStartedSemanticFlagsMatchOnlyNotStarted()` | 覆盖 `isNotStarted()` 只对 `NOT_STARTED` 返回 `true`。 |
| `void inProgressSemanticFlagsMatchOnlyInProgress()` | 覆盖 `isInProgress()` 只对 `IN_PROGRESS` 返回 `true`。 |
| `void completedSemanticFlagsMatchOnlyCompleted()` | 覆盖 `isCompleted()` 只对 `COMPLETED` 返回 `true`。 |
| `void overdueIncompleteSemanticFlagsMatchOnlyOverdueIncomplete()` | 覆盖 `isOverdueIncomplete()` 只对 `OVERDUE_INCOMPLETE` 返回 `true`。 |
| `void valueOfParsesDeclaredStatusName()` | 覆盖 Java 枚举名解析。 |
| `void valueOfRejectsUnknownStatusName()` | 未知状态名抛出 `IllegalArgumentException`。 |
| `void nameUsesStableEnumConstantName()` | 断言至少一个常量的 `name()` 稳定，例如 `NOT_STARTED`。 |

### `StudyPlanTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试数据约定**：

- 固定编号使用 `new EntityId(1)`。
- 固定周期使用 `new DateRange(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14))`。
- 不调用真实当前日期。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void constructorStoresProvidedFields()` | 构造器保存编号、目标名称、日期周期、预期投入小时数和指定进度。 |
| `void createFactoryDefaultsProgressToZero()` | `create(id, goalName, period, expectedHours)` 默认进度为 `Progress.zero()`。 |
| `void createFactoryStoresSpecifiedProgress()` | 指定进度工厂保存调用方传入的进度。 |
| `void constructorNormalizesGoalName()` | 目标名称首尾空白被 `strip()` 去除。 |
| `void keepsInternalWhitespaceInGoalName()` | 目标名称内部空白保持不变。 |
| `void rejectsNullRequiredFields()` | 空编号、空目标名称、空周期、空进度在构造和工厂入口快速失败。 |
| `void rejectsBlankGoalName()` | `""`、普通空白和 Unicode 空白目标名称抛出 `IllegalArgumentException`。 |
| `void rejectsInvalidDateRange()` | 使用开始日期晚于截止日期构造 `DateRange` 时抛出 `IllegalArgumentException`，计划无法创建。 |
| `void rejectsNonPositiveExpectedHours()` | 预期投入小时数为 0 或负数时构造和工厂入口抛出 `IllegalArgumentException`。 |
| `void exposesStartAndEndDatesFromPeriod()` | `getStartDate()` 和 `getEndDate()` 分别来自 `DateRange`。 |
| `void isCompletedReflectsProgressOnly()` | `Progress.complete()` 时 `isCompleted()` 为 `true`，其他进度为 `false`。 |
| `void updateDetailsChangesEditableFieldsOnly()` | 更新目标名称、周期和预期投入小时数，不改变编号和进度。 |
| `void updateDetailsNormalizesNewGoalName()` | 更新目标名称时执行同样的 `strip()` 规范化。 |
| `void updateDetailsRejectsInvalidGoalNameAndKeepsFieldsUnchanged()` | 空或空白目标名称更新失败后原字段保持不变。 |
| `void updateDetailsRejectsNullPeriodAndKeepsFieldsUnchanged()` | 空周期更新失败后原字段保持不变。 |
| `void updateDetailsRejectsNonPositiveExpectedHoursAndKeepsFieldsUnchanged()` | 非正预期投入小时数更新失败后原字段保持不变。 |
| `void updateProgressAcceptsZeroProgress()` | 更新进度为 `Progress.zero()` 成功。 |
| `void updateProgressAcceptsCompleteProgress()` | 更新进度为 `Progress.complete()` 成功且 `isCompleted()` 为 `true`。 |
| `void updateProgressStoresIntermediateProgress()` | 更新进度为中间值成功。 |
| `void updateProgressRejectsNullProgressAndKeepsFieldsUnchanged()` | 空进度更新失败后原进度保持不变。 |
| `void progressValueObjectRejectsOutOfRangeProgress()` | `Progress.of(-1)` 和 `Progress.of(101)` 抛出 `IllegalArgumentException`，计划无法接收越界进度。 |

### `StudyPlanAnalysisServiceTest`

**包路径**：`assistant.study`

**测试框架**：JUnit Jupiter

**测试数据约定**：

- 固定周期使用 `2026-06-08` 到 `2026-06-14`。
- 所有状态分析显式传入固定 `LocalDate`，不得调用真实当前日期。

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void analyzeStatusReturnsInProgressForZeroProgressInsidePeriod()` | 进度 0 且当前日期位于周期内时返回 `IN_PROGRESS`。 |
| `void analyzeStatusReturnsCompletedForCompleteProgress()` | 进度 100 时返回 `COMPLETED`。 |
| `void analyzeStatusReturnsNotStartedBeforeStartDate()` | 未完成计划在当前日期早于开始日期时返回 `NOT_STARTED`。 |
| `void analyzeStatusReturnsInProgressAtStartDate()` | 未完成计划在开始日当天返回 `IN_PROGRESS`。 |
| `void analyzeStatusReturnsInProgressInsidePeriod()` | 未完成计划在周期内部返回 `IN_PROGRESS`。 |
| `void analyzeStatusReturnsInProgressOnEndDate()` | 未完成计划在截止日当天仍返回 `IN_PROGRESS`。 |
| `void analyzeStatusReturnsOverdueIncompleteAfterEndDate()` | 未完成计划在当前日期晚于截止日期时返回 `OVERDUE_INCOMPLETE`。 |
| `void completedProgressHasPriorityOverNotStartedDate()` | 进度 100 且当前日期早于开始日期时仍返回 `COMPLETED`。 |
| `void completedProgressHasPriorityOverOverdueDate()` | 进度 100 且当前日期晚于截止日期时仍返回 `COMPLETED`。 |
| `void isCompletedReflectsProgressValue()` | `isCompleted(...)` 对 0%、中间值和 100% 返回准确布尔值。 |
| `void isOverdueIncompleteReflectsAnalyzedStatus()` | `isOverdueIncomplete(...)` 委托统一状态分析，不自行产生不同规则。 |
| `void methodsRejectNullArguments()` | `analyzeStatus(null, date)`、`analyzeStatus(plan, null)`、`isCompleted(null)`、`isOverdueIncomplete(null, date)` 和 `isOverdueIncomplete(plan, null)` 抛出 `NullPointerException`。 |

## 实现注意事项

- 新增 `assistant.study` 目录时保持 Maven 标准目录布局。
- 生产代码保持 Java 17 兼容，不使用 Lombok 或额外依赖。
- `StudyPlan` 的字段更新方法必须使用“先校验临时变量，再赋值”的顺序，确保非法更新不会产生部分状态变化。
- `StudyPlanAnalysisService` 是本轮动态状态判断唯一入口；后续代码如需判断未开始、进行中、完成或逾期，应复用该组件。
- 单元测试断言应聚焦公开行为，不通过反射访问私有字段。
