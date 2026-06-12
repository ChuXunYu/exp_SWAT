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

---

## R9 PASSED 实现任务待办查询、只读快照、仓储与服务闭环
结果：新增 `assistant.task.TaskQuery`、`TaskView`、`TaskRepository`、`InMemoryTaskRepository`、`TaskService`，实现任务创建、查看、列表、组合筛选、修改、删除、完成、撤销完成、错误转换和只读快照返回边界。
测试：`mvn verify` 通过；验证报告记录通过 264 个测试，失败 0 个。

## R9 NEW 实现日程领域模型与冲突策略基础
任务：新增日程提醒模块的核心领域实体、动态状态枚举和冲突判断策略，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/schedule/ScheduleStatus.java`、`java-ai-assistant/src/main/java/assistant/schedule/ScheduleItem.java`、`java-ai-assistant/src/main/java/assistant/schedule/ScheduleConflictPolicy.java`、`java-ai-assistant/src/test/java/assistant/schedule/ScheduleStatusTest.java`、`java-ai-assistant/src/test/java/assistant/schedule/ScheduleItemTest.java`、`java-ai-assistant/src/test/java/assistant/schedule/ScheduleConflictPolicyTest.java`。
选择理由：任务待办核心功能已经形成服务闭环，可以进入 8 个核心功能中的日程提醒管理。日程服务后续需要稳定的日程实体、基于 `TimeProvider` 的状态推导结果类型和可独立白盒测试的时间冲突规则；先实现领域模型与冲突策略，可避免服务层直接散落名称校验、时间区间重叠和首尾相接边界判断。
上下文：既有 `EntityId`、`DateTimeRange`、`BusinessException`、`ErrorCode.SCHEDULE_CONFLICT`、`TimeProvider` 已可支撑日程编号、左闭右开时间区间和可控当前时间。技术方案要求日程实体至少持有名称、日期时间范围、地点和备注；日程状态不持久化，由服务或领域方法基于当前时间动态推导为即将开始、进行中、已过期；冲突判断集中在 `ScheduleConflictPolicy`，同一时间段存在非空重叠时视为冲突，首尾相接不冲突；普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R10 PASSED 实现日程领域模型与冲突策略基础
结果：新增 `assistant.schedule.ScheduleStatus`、`ScheduleItem`、`ScheduleConflictPolicy`，实现日程动态状态、名称/地点/备注规范化、时间范围更新、自然日覆盖和左闭右开冲突判断规则。
测试：`mvn clean test` 通过；验证报告记录通过 316 个测试，失败 0 个。

## R10 NEW 实现日程提醒查询、只读视图、仓储与服务闭环
任务：新增日程提醒模块的查询条件、只读视图、仓储契约、内存仓储和应用服务，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/schedule/ScheduleQuery.java`、`java-ai-assistant/src/main/java/assistant/schedule/ScheduleView.java`、`java-ai-assistant/src/main/java/assistant/schedule/ScheduleRepository.java`、`java-ai-assistant/src/main/java/assistant/schedule/InMemoryScheduleRepository.java`、`java-ai-assistant/src/main/java/assistant/schedule/ScheduleService.java`、`java-ai-assistant/src/test/java/assistant/schedule/ScheduleQueryTest.java`、`java-ai-assistant/src/test/java/assistant/schedule/ScheduleViewTest.java`、`java-ai-assistant/src/test/java/assistant/schedule/InMemoryScheduleRepositoryTest.java`、`java-ai-assistant/src/test/java/assistant/schedule/ScheduleServiceTest.java`。
选择理由：v9 已完成日程实体、动态状态和冲突策略；日程提醒核心功能还缺少创建、查看、修改、删除、按日期查询和冲突拒绝的服务入口。先补齐日程服务闭环，可让后续汇总服务、AI 本地上下文和控制台菜单通过稳定公开 API 读取带动态状态的日程快照，而不直接暴露可变 `ScheduleItem` 或仓储集合。
上下文：既有 `ScheduleItem` 负责字段不变量和 `statusAt(...)` 动态状态推导，`ScheduleConflictPolicy` 负责非空时间重叠冲突判断，`DateTimeRange.coversDate(...)` 支持跨日期按自然日查询；既有 `EntityId`、`IdGenerator`、`TimeProvider`、`OperationResult`、`ErrorCode.SCHEDULE_CONFLICT` 可用于服务层生成编号、读取当前时间、返回只读视图和稳定错误分类。技术方案要求日程创建/修改时识别同一时间段冲突并拒绝保存，首尾相接不冲突，按日期查询返回该日期开始或覆盖该日期的日程快照，状态基于可注入当前时间动态计算；普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R11 PASSED 实现日程提醒查询、只读视图、仓储与服务闭环
结果：新增 `assistant.schedule.ScheduleQuery`、`ScheduleView`、`ScheduleRepository`、`InMemoryScheduleRepository`、`ScheduleService`，实现日程创建、查看、列表、筛选、按日期查询、修改、删除、冲突拒绝、错误分类和只读状态快照返回边界。
测试：`mvn test` 通过；验证报告记录通过 385 个测试，失败 0 个。

## R11 NEW 实现学习计划领域模型与状态分析基础
任务：新增学习计划模块的核心领域实体、状态枚举和状态分析组件，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/study/StudyPlanStatus.java`、`java-ai-assistant/src/main/java/assistant/study/StudyPlan.java`、`java-ai-assistant/src/main/java/assistant/study/StudyPlanAnalysisService.java`、`java-ai-assistant/src/test/java/assistant/study/StudyPlanStatusTest.java`、`java-ai-assistant/src/test/java/assistant/study/StudyPlanTest.java`、`java-ai-assistant/src/test/java/assistant/study/StudyPlanAnalysisServiceTest.java`。
选择理由：任务待办和日程提醒两个核心模块已形成服务闭环，可以进入 8 个核心功能中的学习计划管理。学习计划服务、汇总服务和 AI 拆解上下文后续都依赖稳定的计划实体、动态状态分类和统一分析规则；先实现领域模型与分析组件，可集中固定目标名称、日期周期、预期投入、进度更新、完成优先级和逾期判断等高分支规则。
上下文：既有 `EntityId` 可作为学习计划唯一编号，`DateRange` 可表达开始日期到截止日期的左右闭计划周期，`Progress` 已保证 0 到 100 进度边界，`TimeProvider.today()` 可为后续服务注入当前日期。技术方案要求学习计划实体持有目标名称、开始日期、截止日期、预期投入小时数和进度；开始日期晚于截止日期拒绝创建；计划状态由 `StudyPlanAnalysisService` 统一推导为未开始、进行中、已完成、逾期未完成，且进度 100 的已完成优先级高于日期状态；完成数量统计和本周统计后续也应复用该分析组件。普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R12 PASSED 实现学习计划领域模型与状态分析基础
结果：新增 `assistant.study.StudyPlanStatus`、`StudyPlan`、`StudyPlanAnalysisService`，实现学习计划目标名称、日期周期、预期投入小时数、进度更新、完成优先级和基于显式当前日期的动态状态分析。
测试：`mvn clean test` 通过；验证报告记录通过 427 个测试，失败 0 个，推送成功。

## R12 NEW 实现学习计划查询、只读视图、仓储与服务闭环
任务：新增学习计划模块的查询条件、只读视图、仓储契约、内存仓储和应用服务，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/study/StudyPlanQuery.java`、`java-ai-assistant/src/main/java/assistant/study/StudyPlanView.java`、`java-ai-assistant/src/main/java/assistant/study/StudyPlanRepository.java`、`java-ai-assistant/src/main/java/assistant/study/InMemoryStudyPlanRepository.java`、`java-ai-assistant/src/main/java/assistant/study/StudyPlanService.java`、`java-ai-assistant/src/test/java/assistant/study/StudyPlanQueryTest.java`、`java-ai-assistant/src/test/java/assistant/study/StudyPlanViewTest.java`、`java-ai-assistant/src/test/java/assistant/study/InMemoryStudyPlanRepositoryTest.java`、`java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`。
选择理由：v11 已完成学习计划实体、状态枚举和状态分析组件；学习计划核心功能还缺少创建、查看、修改、删除、更新进度、按状态或周期筛选、完成/未完成数量统计和只读服务入口。先补齐学习计划服务闭环，可让后续汇总服务、AI 本地上下文、草稿导入和控制台菜单通过稳定公开 API 读取带动态状态的学习计划快照，而不直接暴露可变 `StudyPlan` 或仓储集合。
上下文：既有 `StudyPlan` 负责目标名称、`DateRange` 周期、预期投入小时数和 `Progress` 不变量，`StudyPlanAnalysisService` 负责基于当前日期推导 `StudyPlanStatus`，`EntityId`、`IdGenerator`、`TimeProvider`、`OperationResult` 和 `ErrorCode` 已可支撑服务层生成编号、读取可控当前日期并返回稳定成功/失败结果。技术方案要求学习计划创建和更新进度拒绝非法输入，状态由统一分析组件动态推导，进度 100 优先识别为已完成，完成数量统计和本周统计后续复用该分析组件；普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R12 RETRY 实现学习计划查询、只读视图、仓储与服务闭环
原因：计划审查指出两个契约缺口：其一，学习计划动态状态筛选未像 `schedule` 模块那样显式传入当前日期与分析组件，容易导致 query、仓储或服务直接读取系统时间或复制状态推导逻辑；其二，创建学习计划时初始进度规则缺失，未覆盖需求中的创建边界和后续 AI 草稿导入入口。
修正：保持当前任务范围不变，补充唯一可执行的接口边界。要求 `StudyPlanQuery.matches(...)` 与 `StudyPlanRepository.findBy(...)` 显式接收 `StudyPlanAnalysisService` 和 `LocalDate currentDate`，由 `StudyPlanService` 统一通过 `timeProvider.today()` 提供动态状态上下文；同时明确创建接口既支持默认 `Progress.zero()`，也支持显式初始进度参数供 AI 草稿导入复用，并将创建时 `0`、`100`、`-1`、`101` 初始进度及失败后仓储不变性纳入本轮测试。

## R12 RETRY 实现学习计划查询、只读快照仓储与统计契约服务闭环
原因：二次计划审查指出两个剩余分叉：其一，仓储层虽然要求返回“不可修改快照”，但没有明确是否允许把内部可变 `StudyPlan` 实体引用直接暴露给调用方；其二，完成/未完成数量统计的服务接口签名与返回形态未固定，仍可能导致 coder 自行扩展或遗漏。
修正：继续保持当前任务范围不变，并把接口边界收束为唯一实现路径。要求 `StudyPlanRepository.findById(...)`、`findAll()`、`findBy(...)` 均返回脱离内部存储状态的 `StudyPlan` 快照，`save(...)` 也必须保存调用参数的副本或等价隔离结果，禁止外部通过仓储返回值或保存后仍持有的对象引用绕过 `StudyPlanService` 修改内部状态；同时明确统计接口仅做“全量计划”的动态状态统计，本轮固定公开方法为 `OperationResult<Integer> countCompletedPlans()` 与 `OperationResult<Integer> countIncompletePlans()`，二者统一基于 `timeProvider.today()` 和 `StudyPlanAnalysisService` 计算，不新增聚合 DTO，也不提供按查询条件过滤的统计重载。测试需补充仓储读写快照隔离和两个统计方法的成功/失败边界。

## R12 RETRY 实现学习计划查询、只读快照仓储与统计契约服务闭环
原因：三次计划审查指出两个最终契约冲突：其一，`StudyPlanService` 的“成功查询和写操作统一返回 `StudyPlanView` / `List<StudyPlanView>`”与固定统计接口 `OperationResult<Integer>` 同时存在，导致统计是否属于该统一规则不明确；其二，创建接口一面要求接收 `Progress initialProgress`，一面又要求对 `-1`、`101` 这类原始非法数值在服务层映射为 `VALIDATION_ERROR`，仍未收束到唯一公开 API 形态。
修正：继续保持当前任务范围不变，并把服务接口进一步收束为唯一实现路径。要求 `StudyPlanService` 只有创建、查看、列表、组合筛选、修改详情、更新进度这类返回计划快照的方法成功时返回 `StudyPlanView` 或不可修改的 `List<StudyPlanView>`；删除固定返回 `OperationResult<Void>`，全量统计固定返回 `OperationResult<Integer>`，不再纳入快照返回规则。学习计划创建服务公开接口固定接收原始整数初始进度参数（`int`/`Integer` 或仅参数命名等价的同义签名），并保留一个不传初始进度、默认 `0` 的便捷重载；服务内部负责把原始数值转换为 `Progress.zero()` / `Progress.of(...)`，从而将 `0`、`100` 映射为成功，将 `-1`、`101` 和空必填字段稳定映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`。领域实体、仓储快照和视图仍统一使用 `Progress` 值对象表示合法进度。测试需补充服务创建重载、非法原始进度映射和统计接口不受快照返回规则约束的断言。

## R12 RETRY 实现学习计划查询、只读快照仓储与统计契约服务闭环
原因：四次计划审查指出仍有两个未收束的公开接口分叉：其一，创建/修改详情一侧要求服务边界统一把“开始日期晚于截止日期”映射为 `VALIDATION_ERROR`，但任务没有固定服务是接收原始开始日期与截止日期还是直接接收 `DateRange`；其二，更新进度仍未固定为接收原始整数，导致 `-1`、`101` 一类非法输入可能在服务调用前就无法表示，无法形成稳定错误映射与白盒测试契约。
修正：继续保持当前任务范围不变，并把创建、修改详情、更新进度三个写接口全部收束到原始输入层。要求 `StudyPlanService` 的创建和修改详情公开接口必须接收原始 `LocalDate startDate`、`LocalDate endDate`（或唯一等价的原始请求对象），由服务内部构造 `DateRange` 并统一把开始日期晚于截止日期、空日期、空目标名称、非正预期小时数映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`；同时要求 `updateProgress(...)` 与创建初始进度规则一致，公开接口固定接收原始整数进度值，由服务内部转换为 `Progress.of(...)`。测试需补充创建/修改详情在非法日期范围下的失败路径与失败后仓储状态不变，以及更新进度对 `0`、`100`、`-1`、`101` 的成功/失败和失败后仓储不变性断言。

---

## R13 PASSED 实现学习计划查询、只读快照仓储与统计契约服务闭环
结果：新增 `assistant.study.StudyPlanQuery`、`StudyPlanView`、`StudyPlanRepository`、`InMemoryStudyPlanRepository`、`StudyPlanService`，实现学习计划创建、查看、列表、组合筛选、修改详情、更新进度、删除、完成/未完成数量统计、动态状态投影、输入错误映射和仓储快照隔离。
测试：`mvn test` 通过；验证报告记录通过 497 个测试，失败 0 个，推送成功。

## R13 NEW 实现收支记录领域模型与统计结果基础
任务：新增收支记录模块的核心领域实体、类型枚举和统计结果值对象，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/finance/TransactionType.java`、`java-ai-assistant/src/main/java/assistant/finance/TransactionRecord.java`、`java-ai-assistant/src/main/java/assistant/finance/FinanceStatistics.java`、`java-ai-assistant/src/test/java/assistant/finance/TransactionTypeTest.java`、`java-ai-assistant/src/test/java/assistant/finance/TransactionRecordTest.java`、`java-ai-assistant/src/test/java/assistant/finance/FinanceStatisticsTest.java`。
选择理由：任务待办、日程提醒和学习计划三个核心模块已形成服务闭环，可以进入 8 个核心功能中的收支记录管理。既有金额值对象已完成，下一步应先固定收支方向、单条交易记录不变量和统计结果的收入/支出/结余语义，再在后续轮次接入查询、仓储、服务和统计计算组件，避免服务层直接散落类别清理、日期校验和结余计算规则。
上下文：既有 `EntityId` 可作为收支记录唯一编号，`TransactionAmount` 已保证单笔金额大于 0 且最多两位小数，`MoneyValue` 已支持统计金额零值、负值、加减和两位小数展示，`DateRange` 可用于后续日期筛选。技术方案要求收支记录持有类型、金额、类别、日期和备注；收入与支出使用 `TransactionType` enum；收入总额、支出总额和结余由 `FinanceStatistics` 表示，空记录统计后续应返回三个零值，只有支出无收入时结余允许为负，统计计算禁止使用 `double`。本轮不实现 `TransactionQuery`、仓储、`FinanceService` 或 `FinanceStatisticsService`，但领域模型和统计结果必须为后续服务闭环提供稳定公开接口；普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

---

## R14 PASSED 实现收支记录领域模型与统计结果基础
结果：新增 `assistant.finance.TransactionType`、`TransactionRecord`、`FinanceStatistics`，实现收入/支出方向、单条收支记录字段校验与文本规范化、详情修改原子性、收入总额/支出总额/结余一致性和负结余允许规则。
测试：`mvn test` 通过；验证报告记录通过 526 个测试，失败 0 个，推送成功。

## R14 NEW 实现收支记录查询、只读视图、仓储、服务与统计闭环
任务：新增收支记录模块的查询条件、只读视图、仓储契约、内存仓储、应用服务和统计服务，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/finance/TransactionQuery.java`、`TransactionView.java`、`TransactionRepository.java`、`InMemoryTransactionRepository.java`、`FinanceService.java`、`FinanceStatisticsService.java`，以及对应测试 `TransactionQueryTest.java`、`TransactionViewTest.java`、`InMemoryTransactionRepositoryTest.java`、`FinanceServiceTest.java`、`FinanceStatisticsServiceTest.java`。
选择理由：v13 已完成收支方向、单条记录不变量和统计结果值对象；收支记录核心功能还缺少记录收入、记录支出、查看、组合筛选、修改、删除和即时统计入口。先补齐收支服务闭环，可让后续汇总服务、AI 本地上下文和控制台菜单通过稳定公开 API 读取收支快照和统计结果，而不直接暴露可变 `TransactionRecord` 或仓储集合。
上下文：既有 `TransactionRecord` 负责编号、类型、金额、类别、日期和备注不变量，`FinanceStatistics` 负责收入总额、支出总额和结余一致性，`TransactionAmount` 和 `MoneyValue` 负责金额精度，`DateRange` 负责左右闭日期筛选。技术方案要求查询条件组合支持类型、类别和日期范围，开始日期晚于结束日期作为输入错误，空记录统计返回三个零值，统计计算使用 `BigDecimal`/金额值对象禁止 `double`，删除记录后统计必须基于当前仓储状态重新计算。普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## R14 RETRY 实现收支记录查询、只读视图、仓储、服务与统计闭环
原因：计划审查指出 `updateTransaction(EntityId id, TransactionType type, String amountText, String category, LocalDate date, String note)` 的未知收支类型错误映射未收束；在 enum 接口形态下，未知或空类型最可能表现为 `type == null`，若不明确服务层契约，后续实现可能让实体层 `NullPointerException` 或 `IllegalArgumentException` 泄漏给调用方。
修正：保持当前任务范围不变，在 `task_v14.md` 中明确 `type == null` 必须由 `FinanceService` 映射为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不得向调用方泄漏运行时异常；同时要求 `FinanceServiceTest` 覆盖该失败路径，并验证失败后仓储状态不变。

---

## R15 PASSED 实现收支记录查询、只读视图、仓储、服务与统计闭环
结果：新增 `assistant.finance.TransactionQuery`、`TransactionView`、`TransactionRepository`、`InMemoryTransactionRepository`、`FinanceService`、`FinanceStatisticsService`，实现收支记录创建、查看、列表、组合筛选、修改、删除、即时统计、错误映射和仓储快照隔离。
测试：`mvn test` 通过；验证报告记录通过 589 个测试，失败 0 个，推送成功。

## R15 NEW 实现个人笔记领域模型与搜索策略基础
任务：新增个人笔记模块的核心领域实体和关键字搜索策略，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/note/Note.java`、`java-ai-assistant/src/main/java/assistant/note/NoteSearchPolicy.java`、`java-ai-assistant/src/test/java/assistant/note/NoteTest.java`、`java-ai-assistant/src/test/java/assistant/note/NoteSearchPolicyTest.java`。
选择理由：任务、日程、学习计划和收支四个本地核心模块已形成服务闭环，可以进入 8 个核心功能中的个人笔记或日记管理。既有 `Tag`、`EntityId` 和可注入时间基础已完成，下一步应先固定笔记实体的标题、内容、创建日期、标签集合不变量，以及关键字匹配标题/内容/标签的统一策略，再在后续轮次补齐查询、仓储、只读视图和服务闭环，避免服务层直接散落文本清理、标签处理和关键字匹配规则。
上下文：既有 `EntityId` 可作为笔记唯一编号，`Tag` 已负责标签首尾空白清理、空标签拒绝和 `Locale.ROOT` 小写归一，`TimeProvider.today()` 后续可用于服务创建日期。技术方案要求笔记实体持有标题、内容、创建日期和标签集合，标题和内容不能为空，标签通过 `Tag` 值对象统一处理；关键字查询由 `NoteSearchPolicy` 处理，关键字为空属于输入错误，后续服务应返回 `VALIDATION_ERROR`，无匹配返回空集合；关键字匹配标题和内容，标签查询按 `Tag` 的统一语义比较。普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## R15 RETRY 实现个人笔记领域模型与搜索策略基础
原因：计划审查指出两个契约缺口：其一，`NoteSearchPolicy` 的标题和内容关键字匹配只写为“建议采用大小写不敏感”，与测试覆盖大小写行为之间存在实现分叉；其二，替换全部标签时没有明确失败原子性，输入集合包含 `null` 或非法标签时可能污染原有标签集合。
修正：保持当前任务范围不变，固定唯一可执行语义。要求标题和内容关键字匹配必须使用 `Locale.ROOT` 大小写不敏感包含匹配，标签匹配必须通过 `Tag.of(keyword)` 与标签集合按归一语义精确比较；同时要求替换全部标签必须先完整校验输入集合引用、元素非空和所有标签有效性，再替换内部状态，任何非法输入都必须保持原标签集合不变。测试需补充大小写不敏感断言和标签替换失败后状态不变性。

---

## R16 PASSED 实现个人笔记领域模型与搜索策略基础
结果：新增 `assistant.note.Note`、`NoteSearchPolicy`，实现个人笔记标题/内容/创建日期/标签集合不变量、标签快照隔离、内容更新与标签替换原子性、关键字大小写不敏感文本匹配和标签语义精确匹配。
测试：`mvn test` 通过；验证报告记录通过 619 个测试，失败 0 个，推送成功。

## R16 NEW 实现个人笔记查询、只读视图、仓储与服务闭环
任务：新增个人笔记模块的查询条件、只读视图、仓储契约、内存仓储和应用服务，预期文件路径包括 `java-ai-assistant/src/main/java/assistant/note/NoteQuery.java`、`NoteView.java`、`NoteRepository.java`、`InMemoryNoteRepository.java`、`NoteService.java`，以及对应测试 `NoteQueryTest.java`、`NoteViewTest.java`、`InMemoryNoteRepositoryTest.java`、`NoteServiceTest.java`。
选择理由：v15 已完成个人笔记领域实体和关键字搜索策略；笔记核心功能还缺少新增、查看、修改、删除、按关键字查询、按标签查询、组合筛选和面向汇总/AI 的只读服务入口。先补齐笔记服务闭环，可让后续汇总服务、AI 摘要上下文和控制台菜单通过稳定公开 API 读取笔记快照，而不直接暴露可变 `Note` 或仓储集合。
上下文：既有 `Note` 负责标题、内容、创建日期和标签集合不变量，`NoteSearchPolicy` 负责关键字非空校验、标题/内容大小写不敏感匹配和标签语义精确匹配，`Tag` 负责标签原始文本清理与小写归一，`EntityId`、`IdGenerator`、`TimeProvider`、`OperationResult` 和 `ErrorCode` 已可支撑服务层生成编号、读取可控创建日期并返回稳定成功/失败结果。技术方案要求关键字为空返回 `VALIDATION_ERROR`，无匹配返回空集合，按标签查询复用 `Tag` 语义，修改或删除不存在笔记返回 `NOT_FOUND`，成功查询和写操作不得返回内部可变实体引用。普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。
