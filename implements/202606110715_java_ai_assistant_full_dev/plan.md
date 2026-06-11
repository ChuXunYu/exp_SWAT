# 实现计划

任务描述：依据技术方案、架构级 OOD 和需求文档，在项目根目录新增独立 Maven 工程 `java-ai-assistant/`，完成 Java AI 个人学习与生活助手的源码、单元测试、必要文档和构建配置；普通单元测试不得依赖真实 DeepSeek、网络、API Key 或真实当前时间；每个管线任务完成后由 Runner 执行测试、中文 commit 并尝试 push，若推送失败需在验证报告说明。
项目根目录：/root/exp_SWAT

---

## R1 NEW 建立 Maven 工程骨架与通用结果基础
任务：新增 `java-ai-assistant/` Maven 单模块工程，配置 Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe，并实现首批通用结果与错误基础类型，预期文件路径包括 `java-ai-assistant/pom.xml`、`java-ai-assistant/README.md`、`java-ai-assistant/docs/environment.md`、`java-ai-assistant/src/main/java/assistant/common/*`、`java-ai-assistant/src/test/java/assistant/common/*`。
选择理由：后续任务、日程、学习计划、收支、笔记、汇总和 AI 模块都依赖统一构建入口、测试执行入口、错误分类和服务返回语义；先建立可重复运行的工程和基础类型，可避免后续任务在无 Maven/JUnit 基线下分散配置。
上下文：当前仓库只有需求、OOD、技术方案和实验资料，尚无 Java 源码或构建文件。技术方案明确要求在根目录新增 `java-ai-assistant/`，使用 Maven 标准目录、Java 17、Jackson Databind 2.19.0、JUnit Jupiter 5.14.4、Mockito 5.18.0、JaCoCo 0.8.13、Surefire/Failsafe 3.5.6；错误分类至少覆盖校验、未找到、状态冲突、日程冲突、AI 配置/鉴权/限流/超时/远端不可用/空响应/响应格式异常和系统错误。

---

## R2 PASSED 建立 Maven 工程骨架与通用结果基础
结果：新增独立 Maven 单模块工程 `java-ai-assistant/`，实现 `assistant.common.ErrorCode`、`BusinessException`、`OperationResult<T>`，并补充 README、环境说明和基础单元测试。
测试：`mvn -f java-ai-assistant/pom.xml verify` 通过；验证报告记录通过 17 个测试，失败 0 个，跳过 0 个。

## R2 NEW 实现实体编号与递增编号生成基础
任务：新增跨业务唯一编号值对象与可替换编号生成基础，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/common/EntityId.java`、`java-ai-assistant/src/main/java/assistant/testability/IdGenerator.java`、`java-ai-assistant/src/main/java/assistant/testability/IncrementalIdGenerator.java`、`java-ai-assistant/src/test/java/assistant/common/EntityIdTest.java`、`java-ai-assistant/src/test/java/assistant/testability/IncrementalIdGeneratorTest.java`。
选择理由：任务、日程、学习计划、收支、笔记和 AI 草稿等所有可修改记录都需要稳定唯一标识；后续业务服务创建记录时依赖编号生成器，测试也需要可预测编号边界。先实现该底层依赖，可避免后续各业务模块重复使用裸 `long` 或分散实现编号逻辑。
上下文：v1 已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线与 `assistant.common` 错误/结果类型。技术方案要求 `EntityId` 使用正整数语义、底层可用 `long`，所有可修改记录持有 `EntityId`；默认编号由 `IncrementalIdGenerator` 生成；生产代码可依赖 `assistant.testability` 中的简单抽象及基础实现，测试可替换为固定序列生成器。

---

## R3 PASSED 实现实体编号与递增编号生成基础
结果：新增 `assistant.common.EntityId`、`assistant.testability.IdGenerator`、`assistant.testability.IncrementalIdGenerator`，补齐正整数编号、可替换编号生成和递增编号边界行为。
测试：`mvn clean verify` 通过；验证报告记录通过 34 个测试，失败 0 个。

## R3 NEW 实现可替换时间提供基础
任务：新增跨业务时间抽象和基础实现，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/testability/TimeProvider.java`、`java-ai-assistant/src/main/java/assistant/testability/SystemTimeProvider.java`、`java-ai-assistant/src/main/java/assistant/testability/FixedTimeProvider.java`、`java-ai-assistant/src/test/java/assistant/testability/FixedTimeProviderTest.java`、必要时补充 `SystemTimeProviderTest.java`。
选择理由：日程状态、学习计划逾期、本周统计、本月统计、今日摘要和 AI 本地上下文都依赖“当前日期/时间”；需求明确普通单元测试不得依赖真实当前时间。先实现可注入时间基础，可避免后续业务服务直接调用 `LocalDate.now()` 或 `LocalDateTime.now()`，并为边界状态测试提供稳定入口。
上下文：v1 已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线与通用结果类型，v2 已完成 `EntityId` 和 `IdGenerator`。技术方案要求 `assistant.testability.TimeProvider` 返回当前 `LocalDate` 与 `LocalDateTime`，生产实现读取系统时间，测试实现固定在指定日期时间；生产代码可依赖 `assistant.testability` 中的简单抽象及基础实现，JUnit 专用 fake/stub 不放入生产源码。

---

## R4 PASSED 实现可替换时间提供基础
结果：新增 `assistant.testability.TimeProvider`、`SystemTimeProvider`、`FixedTimeProvider`，实现可注入当前日期和当前时间能力，并补齐固定时间与系统时间基础测试。
测试：`mvn clean test` 通过；验证报告记录通过 43 个测试，失败 0 个，推送成功。

## R4 NEW 实现日期区间与日期时间区间值对象
任务：新增跨业务日期区间和日期时间区间值对象，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/common/DateRange.java`、`java-ai-assistant/src/main/java/assistant/common/DateTimeRange.java`、`java-ai-assistant/src/test/java/assistant/common/DateRangeTest.java`、`java-ai-assistant/src/test/java/assistant/common/DateTimeRangeTest.java`。
选择理由：收支日期筛选、学习计划周期、本周和本月统计依赖左右闭区间日期语义；日程时间校验、冲突识别和按日期查询依赖左闭右开日期时间语义。先实现这两个通用值对象，可为后续日程、学习、收支和汇总模块提供稳定可测的边界判断，避免各业务服务重复实现日期比较逻辑。
上下文：v1 已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线与通用错误/结果类型，v2 已完成 `EntityId` 与可替换编号生成，v3 已完成 `TimeProvider`、系统时间和固定时间实现。技术方案要求 `DateRange` 使用左右闭区间，开始日期晚于结束日期时作为输入校验错误；`DateTimeRange` 使用左闭右开区间，结束时间必须晚于开始时间，当前时间等于开始时间视为包含、等于结束时间视为不包含，两个区间首尾相接不冲突，只有非空重叠才冲突。普通单元测试不得依赖真实当前时间、网络或 API Key。

---

## R5 PASSED 实现日期区间与日期时间区间值对象
结果：新增 `assistant.common.DateRange`、`assistant.common.DateTimeRange`，实现日期闭区间、日期时间左闭右开区间、包含判断、重叠判断和自然日覆盖判断，并补齐边界行为单元测试。
测试：`mvn test` 通过；验证报告记录通过 90 个测试，失败 0 个，推送成功。

## R5 NEW 实现金额值对象基础
任务：新增单笔收支金额值对象和统计金额值对象，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/common/TransactionAmount.java`、`java-ai-assistant/src/main/java/assistant/common/MoneyValue.java`、`java-ai-assistant/src/test/java/assistant/common/TransactionAmountTest.java`、`java-ai-assistant/src/test/java/assistant/common/MoneyValueTest.java`。
选择理由：收支记录管理与收支统计是需求中的白盒测试重点，后续 `finance.TransactionRecord`、`FinanceService`、`FinanceStatisticsService` 都依赖稳定的金额合法性、精度和加减语义。先实现两个底层金额值对象，可避免后续 finance 模块直接使用裸 `BigDecimal` 或 `double`，并集中覆盖零值、负数、小数位和统计结余等边界。
上下文：v1 已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线与通用错误/结果类型，v2 已完成 `EntityId` 与可替换编号生成，v3 已完成 `TimeProvider`，v4 已完成 `DateRange` 与 `DateTimeRange`。技术方案要求 `TransactionAmount` 使用 `BigDecimal` 表示单笔收入或支出金额，必须大于 0 且最多两位小数；`MoneyValue` 使用 `BigDecimal` 表示统计金额，允许 0 和负数，统一两位小数展示，统计计算禁止使用 `double`。普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R6 PASSED 实现金额值对象基础
结果：新增 `assistant.common.TransactionAmount`、`assistant.common.MoneyValue`，实现单笔正金额校验、统计金额规范化、字符串工厂、金额转换、加减运算和两位小数展示，并补齐公开接口边界测试。
测试：`mvn test` 通过；验证报告记录通过 128 个测试，失败 0 个。

## R6 NEW 实现进度与标签值对象基础
任务：新增学习进度值对象和笔记标签值对象，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/common/Progress.java`、`java-ai-assistant/src/main/java/assistant/common/Tag.java`、`java-ai-assistant/src/test/java/assistant/common/ProgressTest.java`、`java-ai-assistant/src/test/java/assistant/common/TagTest.java`。
选择理由：学习计划管理依赖 0 到 100 的进度边界和 100% 完成语义；笔记管理、标签查询、标签分布统计和 AI 本地上下文依赖稳定的标签清理与比较语义。先实现这两个通用值对象，可避免后续 `study` 和 `note` 模块重复散落进度越界、空标签和标签归一规则。
上下文：v1 已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线与通用错误/结果类型，v2 已完成 `EntityId` 与可替换编号生成，v3 已完成 `TimeProvider`，v4 已完成 `DateRange` 与 `DateTimeRange`，v5 已完成金额基础。技术方案要求 `Progress` 值对象范围为 0 到 100；标签使用 `Tag` 值对象，去除首尾空白，空标签拒绝，大小写归一规则由 `Tag` 集中决定，服务层不重复处理。普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R7 PASSED 实现进度与标签值对象基础
结果：新增 `assistant.common.Progress`、`assistant.common.Tag`，实现学习进度 0 到 100 边界、完成判断、百分比展示、标签首尾空白清理、空标签拒绝、`Locale.ROOT` 小写归一、展示文本和集合/映射键语义。
测试：`mvn test` 通过；验证报告记录通过 153 个测试，失败 0 个。

## R7 NEW 实现任务待办领域模型基础
任务：新增任务待办模块的核心领域实体与枚举，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/task/TaskPriority.java`、`java-ai-assistant/src/main/java/assistant/task/TaskStatus.java`、`java-ai-assistant/src/main/java/assistant/task/TaskItem.java`、`java-ai-assistant/src/test/java/assistant/task/TaskPriorityTest.java`、`java-ai-assistant/src/test/java/assistant/task/TaskStatusTest.java`、`java-ai-assistant/src/test/java/assistant/task/TaskItemTest.java`。
选择理由：通用编号、日期、时间、金额、进度和标签基础已经完成，可以开始进入 8 个核心功能中的任务待办管理。任务实体、优先级和状态是后续 `TaskService`、`TaskQuery`、任务仓储、汇总服务和 AI 任务草稿导入的直接依赖；先集中实现领域模型，可把标题校验、截止日期、状态流转和重复完成冲突等规则固定在可测对象中。
上下文：v1-v6 已完成 Maven/JUnit/Mockito/Jackson/JaCoCo 基线、`ErrorCode`/`BusinessException`/`OperationResult`、`EntityId`/`IdGenerator`、`TimeProvider`、`DateRange`/`DateTimeRange`、金额、进度与标签基础。技术方案与 OOD 要求任务实体至少持有标题、描述、优先级、截止日期、完成状态和编号；优先级与状态使用 enum；任务应支持完成、撤销完成和修改基础信息；重复完成、重复撤销属于状态冲突；普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R8 PASSED 实现任务待办领域模型基础
结果：新增 `assistant.task.TaskPriority`、`assistant.task.TaskStatus`、`assistant.task.TaskItem`，实现任务优先级、完成状态、标题和描述规范化、基础信息修改、完成/撤销完成状态迁移，以及重复状态迁移的 `STATE_CONFLICT` 业务冲突。
测试：`mvn test` 通过；验证报告记录通过 193 个测试，失败 0 个。

## R8 NEW 实现任务待办查询、仓储与服务闭环
任务：新增任务待办模块的查询条件、仓储契约、内存仓储和应用服务，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/task/TaskQuery.java`、`java-ai-assistant/src/main/java/assistant/task/TaskRepository.java`、`java-ai-assistant/src/main/java/assistant/task/InMemoryTaskRepository.java`、`java-ai-assistant/src/main/java/assistant/task/TaskService.java`、`java-ai-assistant/src/test/java/assistant/task/TaskQueryTest.java`、`java-ai-assistant/src/test/java/assistant/task/InMemoryTaskRepositoryTest.java`、`java-ai-assistant/src/test/java/assistant/task/TaskServiceTest.java`。
选择理由：v7 已完成任务实体与状态流转基础；任务待办核心功能还缺少创建、查看、修改、删除、按状态/优先级筛选、完成和撤销完成的服务入口。先完成任务模块服务闭环，可让后续汇总服务、AI 草稿导入和控制台菜单通过稳定公开 API 读写任务数据，而不直接操作实体或集合。
上下文：既有 `TaskItem` 已负责字段不变量和重复完成/重复撤销的 `BusinessException(ErrorCode.STATE_CONFLICT, ...)`；既有 `EntityId`、`IdGenerator`、`OperationResult`、`ErrorCode` 可用于服务层生成编号和返回稳定成功/失败结果。技术方案要求任务查询支持按状态、优先级和截止日期等条件筛选；仓储默认使用内存 `LinkedHashMap<EntityId, TaskItem>` 保持插入顺序并返回不可修改快照；删除不存在任务、修改不存在任务、完成/撤销不存在任务应返回 `NOT_FOUND`，非法输入应返回 `VALIDATION_ERROR` 或由实体校验后在服务边界转换，本地单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## R8 RETRY 实现任务待办查询、只读快照、仓储与服务闭环
原因：计划审查指出原任务只要求列表集合不可修改，但未明确 `TaskService` 查询结果不得直接暴露仓储内部可变 `TaskItem`，可能导致外部调用方绕过服务修改任务状态或基础信息。
修正：修订 `task_v8.md`，本轮新增 `TaskView` 只读 DTO/record，要求 `TaskService` 创建、查看、列表和组合筛选成功载荷返回 `TaskView` 或不可修改的 `List<TaskView>`，不得返回内部 `TaskItem` 引用；同时补充查询只读边界测试要求，验证载荷类型、列表不可修改，以及外部拿到查询结果后不能影响仓储内部状态。
