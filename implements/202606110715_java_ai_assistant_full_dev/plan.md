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
