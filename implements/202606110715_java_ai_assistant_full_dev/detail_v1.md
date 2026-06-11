# 详细设计（v1）

## 概述

本轮设计目标是在项目根目录下新增 `java-ai-assistant/` Maven 单模块工程骨架，并实现后续所有业务模块共用的基础错误与结果语义。范围仅包含构建配置、标准源码/测试目录、`assistant.common` 下的 `ErrorCode`、`BusinessException`、`OperationResult<T>`、针对这些类型的单元测试，以及最小运行环境文档。

本轮不实现真实 DeepSeek 客户端、命令行菜单、业务模块、网络访问、API Key 读取或集成测试用例。普通 `mvn clean test` 必须只运行单元测试，不读取真实环境变量，不访问网络，不依赖真实 DeepSeek API Key。`integration` profile 只建立运行边界，供后续 `*IT` 集成测试使用。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/pom.xml` | 新建 | Maven 单模块构建文件，固定 Java 17、依赖版本、Surefire/Failsafe/JaCoCo 插件和 `integration` profile。 |
| `java-ai-assistant/README.md` | 新建 | 最小项目说明、单元测试命令、集成测试命令、DeepSeek 配置变量名和无 API Key 时的运行边界。 |
| `java-ai-assistant/docs/environment.md` | 新建 | 开发/测试环境、Maven 命令、DeepSeek 环境变量说明、普通单元测试隔离外部依赖的说明。 |
| `java-ai-assistant/src/main/java/assistant/common/ErrorCode.java` | 新建 | 统一错误分类枚举。 |
| `java-ai-assistant/src/main/java/assistant/common/BusinessException.java` | 新建 | 携带 `ErrorCode` 的业务异常。 |
| `java-ai-assistant/src/main/java/assistant/common/OperationResult.java` | 新建 | 表达服务成功/失败结果的不可变泛型结果类型。 |
| `java-ai-assistant/src/main/java/assistant/testability/.gitkeep` | 新建 | 建立 `assistant.testability` 包目录，后续可放时间、编号、测试友好抽象。 |
| `java-ai-assistant/src/test/java/assistant/common/ErrorCodeTest.java` | 新建 | 校验必需错误码存在且命名稳定。 |
| `java-ai-assistant/src/test/java/assistant/common/BusinessExceptionTest.java` | 新建 | 覆盖业务异常携带错误码、消息和空错误码拒绝。 |
| `java-ai-assistant/src/test/java/assistant/common/OperationResultTest.java` | 新建 | 覆盖成功结果、无载荷成功、失败结果、失败空错误码拒绝等基础分支。 |

## 构建配置规格

### Maven 工程

**文件**：`java-ai-assistant/pom.xml`

**坐标**：

| 字段 | 值 |
|------|----|
| `groupId` | `assistant` |
| `artifactId` | `java-ai-assistant` |
| `version` | `1.0.0-SNAPSHOT` |
| `packaging` | `jar` |
| `name` | `java-ai-assistant` |

**属性**：

| 属性名 | 值 |
|--------|----|
| `project.build.sourceEncoding` | `UTF-8` |
| `maven.compiler.release` | `17` |
| `jackson.version` | `2.19.0` |
| `junit.jupiter.version` | `5.14.4` |
| `mockito.version` | `5.18.0` |
| `maven.surefire.version` | `3.5.6` |
| `maven.failsafe.version` | `3.5.6` |
| `jacoco.version` | `0.8.13` |

**依赖**：

| groupId | artifactId | version | scope |
|---------|------------|---------|-------|
| `com.fasterxml.jackson.core` | `jackson-databind` | `${jackson.version}` | compile |
| `org.junit.jupiter` | `junit-jupiter` | `${junit.jupiter.version}` | test |
| `org.mockito` | `mockito-core` | `${mockito.version}` | test |
| `org.mockito` | `mockito-junit-jupiter` | `${mockito.version}` | test |

**插件**：

| 插件 | 版本 | 默认行为 |
|------|------|----------|
| `org.apache.maven.plugins:maven-compiler-plugin` | 固定为 Maven Central 稳定版本 `3.14.0` | 使用 `${maven.compiler.release}` 编译 Java 17。 |
| `org.apache.maven.plugins:maven-surefire-plugin` | `${maven.surefire.version}` | 默认运行 `**/*Test.java`，排除 `**/*IT.java`，禁用真实外部依赖访问约定由测试代码保证。 |
| `org.apache.maven.plugins:maven-failsafe-plugin` | `${maven.failsafe.version}` | 默认不绑定执行；仅在 `integration` profile 中绑定 `integration-test` 与 `verify`。 |
| `org.jacoco:jacoco-maven-plugin` | `${jacoco.version}` | 绑定 `prepare-agent` 到默认测试生命周期，并在 `verify` 阶段生成报告。 |

**`integration` profile**：

| 配置项 | 规格 |
|--------|------|
| profile id | `integration` |
| 激活方式 | 仅显式 `-Pintegration` 激活 |
| Failsafe includes | `**/*IT.java` |
| Failsafe goals | `integration-test`、`verify` |
| Surefire 行为 | 继续只运行普通单元测试，仍排除 `**/*IT.java` |

## 类型定义

### `ErrorCode`

**形态**：`enum`

**包路径**：`assistant.common`

**职责**：为应用服务、领域校验和 AI 外部依赖失败提供稳定、可断言的错误分类。

**类型签名定义**：`public enum ErrorCode`

**枚举值**：

| 枚举值 | 语义 |
|--------|------|
| `VALIDATION_ERROR` | 字段为空、格式非法、范围非法、日期非法等输入校验错误。 |
| `NOT_FOUND` | 修改、删除、查询、确认不存在的记录或草稿。 |
| `STATE_CONFLICT` | 重复完成、重复撤销、重复确认、取消后确认等状态冲突。 |
| `SCHEDULE_CONFLICT` | 日程时间段存在非空重叠。 |
| `AI_NOT_CONFIGURED` | DeepSeek API Key 或必要 AI 配置缺失。 |
| `AI_AUTH_FAILED` | DeepSeek 鉴权失败，例如 401 或 403。 |
| `AI_RATE_LIMITED` | DeepSeek 限流，例如 429。 |
| `AI_TIMEOUT` | AI 请求超时或网关超时。 |
| `AI_BAD_REQUEST` | AI 请求参数错误，例如 400 或 422。 |
| `AI_REMOTE_UNAVAILABLE` | DeepSeek 5xx 或远端服务不可用。 |
| `AI_NETWORK_ERROR` | 网络 I/O 失败。 |
| `AI_EMPTY_RESPONSE` | HTTP 成功但模型内容为空。 |
| `AI_MALFORMED_RESPONSE` | JSON 或业务结构不符合预期。 |
| `SYSTEM_ERROR` | 未预期运行时异常兜底。 |

**公开接口**：

| 方法签名 | 说明 |
|----------|------|
| `public String name()` | Java enum 默认方法，用于测试和序列化断言。 |
| `public static ErrorCode valueOf(String name)` | Java enum 默认方法，用于按名称解析。 |
| `public static ErrorCode[] values()` | Java enum 默认方法，用于枚举遍历。 |

**构造方式**：Java enum 常量创建，不开放构造。

**类型关系**：被 `BusinessException` 和 `OperationResult<T>` 组合持有。

### `BusinessException`

**形态**：`class`

**包路径**：`assistant.common`

**职责**：表示可预期业务错误，携带稳定 `ErrorCode` 供服务边界转换为 `OperationResult<T>` 或供 JUnit 断言。

**类型签名定义**：`public class BusinessException extends RuntimeException`

**字段**：

| 字段签名 | 约束 |
|----------|------|
| `private final ErrorCode errorCode` | 构造时必须非空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public BusinessException(ErrorCode errorCode, String message)` | 构造器 | `errorCode` 为 `null` 时抛出 `NullPointerException`；`message` 允许为 `null` 并传递给父类。 |
| `public BusinessException(ErrorCode errorCode, String message, Throwable cause)` | 构造器 | `errorCode` 为 `null` 时抛出 `NullPointerException`；`message` 和 `cause` 允许按 `RuntimeException` 语义传递。 |
| `public ErrorCode getErrorCode()` | `ErrorCode` | 返回构造时传入的错误码。 |

**构造方式**：调用公开构造器直接创建。

**类型关系**：继承 `RuntimeException`；组合 `ErrorCode`。

### `OperationResult<T>`

**形态**：`final class`

**包路径**：`assistant.common`

**职责**：作为应用服务面向控制台层和测试层的统一返回语义，表达成功结果或失败结果。

**类型签名定义**：`public final class OperationResult<T>`

**字段**：

| 字段签名 | 约束 |
|----------|------|
| `private final boolean success` | `true` 表示成功，`false` 表示失败。 |
| `private final T payload` | 成功时允许为 `null`；失败时必须为 `null`。 |
| `private final ErrorCode errorCode` | 成功时必须为 `null`；失败时必须非空。 |
| `private final String message` | 成功时允许为 `null`；失败时必须非空且去除首尾空白后非空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static <T> OperationResult<T> success(T payload)` | `OperationResult<T>` | 创建成功结果，允许携带 `null` payload。 |
| `public static OperationResult<Void> success()` | `OperationResult<Void>` | 创建无载荷成功结果。 |
| `public static <T> OperationResult<T> failure(ErrorCode errorCode, String message)` | `OperationResult<T>` | 创建失败结果；`errorCode` 为 `null` 时抛出 `NullPointerException`；`message` 为 `null` 或 blank 时抛出 `IllegalArgumentException`。 |
| `public boolean isSuccess()` | `boolean` | 成功返回 `true`。 |
| `public boolean isFailure()` | `boolean` | 失败返回 `true`。 |
| `public T getPayload()` | `T` | 返回 payload；失败结果返回 `null`。 |
| `public ErrorCode getErrorCode()` | `ErrorCode` | 返回失败错误码；成功结果返回 `null`。 |
| `public String getMessage()` | `String` | 返回失败消息；成功结果返回 `null`。 |

**构造方式**：私有构造器，调用静态工厂方法创建。

**类型关系**：组合 `ErrorCode`；不继承业务类型；不依赖测试框架。

## 单元测试规格

### `ErrorCodeTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void containsRequiredBusinessAndAiErrorCodes()` | 断言任务要求的 14 个错误码均存在，防止后续误删或重命名。 |
| `void valueOfReturnsStableErrorCodeByName()` | 断言 `ErrorCode.valueOf("VALIDATION_ERROR")` 返回对应枚举。 |

### `BusinessExceptionTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void carriesErrorCodeAndMessage()` | 构造 `BusinessException(ErrorCode.VALIDATION_ERROR, "invalid")` 后断言错误码和消息。 |
| `void carriesCauseWhenProvided()` | 构造带 cause 的异常后断言 `getCause()` 和错误码。 |
| `void rejectsNullErrorCode()` | 传入空错误码时断言抛出 `NullPointerException`。 |

### `OperationResultTest`

**包路径**：`assistant.common`

**测试框架**：JUnit Jupiter 5.14.4

**测试方法规划**：

| 方法签名 | 覆盖目标 |
|----------|----------|
| `void successResultCarriesPayload()` | `OperationResult.success("ok")` 应成功、携带 payload、无错误码和失败消息。 |
| `void successWithoutPayloadIsSuccessfulVoidResult()` | `OperationResult.success()` 应成功、payload 为 `null`、无错误码。 |
| `void failureResultCarriesErrorCodeAndMessage()` | `OperationResult.failure(ErrorCode.NOT_FOUND, "missing")` 应失败、携带错误码和消息、payload 为 `null`。 |
| `void failureRejectsNullErrorCode()` | 失败结果空错误码应抛出 `NullPointerException`。 |
| `void failureRejectsBlankMessage()` | 失败结果空白消息应抛出 `IllegalArgumentException`。 |
| `void failureRejectsNullMessage()` | 失败结果 `null` 消息应抛出 `IllegalArgumentException`。 |

## 错误处理

`ErrorCode` 是本轮唯一错误分类来源。`BusinessException` 用于领域和值对象或服务内部表达可预期业务错误，本轮只定义异常类型，不实现服务边界转换。

`BusinessException` 对空 `ErrorCode` 使用 `java.util.Objects.requireNonNull(errorCode, "errorCode")` 语义，抛出 `NullPointerException`。消息允许调用方提供，允许为 `null`，保持与 `RuntimeException` 构造器一致。

`OperationResult.failure(ErrorCode, String)` 对空错误码使用 `Objects.requireNonNull(errorCode, "errorCode")` 语义。失败消息必须是简短可展示文本，`null` 或 `blank` 使用 `IllegalArgumentException` 拒绝。成功结果不携带错误码和失败消息。

构建与测试阶段不得因缺少 `DEEPSEEK_API_KEY` 失败。本轮不读取环境变量，因此不存在 AI 配置失败路径。

## 行为契约

1. `mvn clean test` 在 `java-ai-assistant/` 目录下执行时，只运行 `*Test` 单元测试，不运行 `*IT` 集成测试。
2. `mvn -Pintegration verify` 在 `java-ai-assistant/` 目录下执行时，允许运行后续新增的 `*IT` 集成测试；本轮未定义真实集成测试类。
3. `OperationResult<T>` 实例创建后不可变；调用只读访问方法不得改变内部状态。
4. `OperationResult.success(T)` 允许 `payload == null`，用于“操作成功但没有返回实体”的场景。
5. `OperationResult.success()` 固定返回语义为成功、无 payload、无错误码、无失败消息。
6. `OperationResult.failure(ErrorCode, String)` 必须满足失败结果包含非空错误码和非 blank 消息，且不携带 payload。
7. `BusinessException` 必须始终携带非空 `ErrorCode`，便于后续服务边界稳定转换和测试断言。
8. `ErrorCode` 枚举名是跨模块契约，后续模块只能增量补充，不能重命名或删除本轮列出的必需错误码。
9. README 和 `docs/environment.md` 不得包含真实 API Key，也不得放入看起来像真实密钥的示例值。

## 依赖关系

本轮生产代码依赖关系如下：

| 类型 | 依赖 |
|------|------|
| `assistant.common.ErrorCode` | 无项目内依赖。 |
| `assistant.common.BusinessException` | `assistant.common.ErrorCode`、JDK `RuntimeException`、JDK `Objects`。 |
| `assistant.common.OperationResult<T>` | `assistant.common.ErrorCode`、JDK `Objects`。 |

本轮测试代码依赖关系如下：

| 测试类 | 被测类型 | 外部依赖 |
|--------|----------|----------|
| `ErrorCodeTest` | `ErrorCode` | JUnit Jupiter Assertions。 |
| `BusinessExceptionTest` | `BusinessException` | JUnit Jupiter Assertions。 |
| `OperationResultTest` | `OperationResult<T>` | JUnit Jupiter Assertions。 |

对后续任务暴露的公开接口为：

| 公开类型 | 后续用途 |
|----------|----------|
| `ErrorCode` | 所有服务和值对象统一错误分类。 |
| `BusinessException` | 领域校验和服务内部业务异常。 |
| `OperationResult<T>` | 应用服务返回成功/失败结果，便于控制台展示和 JUnit 断言。 |

## 文档规格

### `README.md`

必须包含以下信息：

| 小节 | 内容 |
|------|------|
| 项目定位 | Java AI 个人学习与生活助手，当前工程为 Maven 单模块。 |
| 环境要求 | Java 17、Maven。 |
| 单元测试命令 | `mvn clean test`，说明不会访问真实 DeepSeek、网络或 API Key。 |
| 集成测试命令 | `mvn -Pintegration verify`，说明后续真实 AI 集成测试应放在该 profile 下。 |
| DeepSeek 配置变量 | `DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`、`DEEPSEEK_TIMEOUT_SECONDS`。 |
| 默认 AI 配置 | base URL 为 `https://api.deepseek.com`，path 为 `/chat/completions`，model 为 `deepseek-v4-flash`。 |
| API Key 边界 | API Key 不写入源码、测试和文档示例；无 API Key 时普通单元测试仍可运行。 |

### `docs/environment.md`

必须包含以下信息：

| 小节 | 内容 |
|------|------|
| 开发环境 | Java 17 LTS、Maven、JUnit Jupiter 5.14.4、Mockito 5.18.0。 |
| 构建与测试 | `mvn clean test`、`mvn clean verify`、`mvn -Pintegration verify`、JaCoCo 报告位置。 |
| DeepSeek 环境变量 | 四个变量名与用途，不给出真实密钥。 |
| 测试隔离说明 | 普通单元测试不读取真实环境变量、不访问网络、不依赖 API Key；真实调用后续归入集成测试。 |
