# 任务指令（v2）

## 动作
NEW

## 任务描述
实现跨业务实体编号与递增编号生成基础，为后续任务、日程、学习计划、收支记录、笔记和 AI 草稿提供统一标识语义。

预期生产文件：

| 文件路径 | 目标 |
|---------|------|
| `java-ai-assistant/src/main/java/assistant/common/EntityId.java` | 新增不可变编号值对象，封装正整数 `long` 编号语义。 |
| `java-ai-assistant/src/main/java/assistant/testability/IdGenerator.java` | 新增编号生成接口，供业务服务通过依赖注入获取新 `EntityId`。 |
| `java-ai-assistant/src/main/java/assistant/testability/IncrementalIdGenerator.java` | 新增默认递增编号生成器，从指定起点或默认起点生成稳定递增编号。 |

预期测试文件：

| 文件路径 | 目标 |
|---------|------|
| `java-ai-assistant/src/test/java/assistant/common/EntityIdTest.java` | 覆盖正整数校验、访问器、相等性、hashCode、toString 和排序语义。 |
| `java-ai-assistant/src/test/java/assistant/testability/IncrementalIdGeneratorTest.java` | 覆盖默认起点、指定起点、连续递增、非法起点拒绝和生成结果类型。 |

## 选择理由
`EntityId` 是所有可修改记录的底层依赖。需求和技术方案均要求记录类数据具有唯一标识，避免同名记录无法定位；后续任务、日程、学习计划、收支、笔记和 AI 草稿服务的新增、修改、删除、确认导入和回滚逻辑都会依赖该标识。

本任务先完成编号值对象和递增生成器，可为后续业务服务提供稳定契约，并为单元测试使用固定或可控编号生成器打下基础。任务粒度控制在 3 个紧密相关生产类型内，不引入业务仓储或具体功能实现。

## 任务上下文
来自需求、OOD 和技术方案的直接约束：

- 记录类数据应具有唯一标识，便于修改和删除，避免同名记录导致无法定位。
- 推荐由程序为任务、日程、学习计划、笔记和收支记录生成递增编号，并在测试中允许通过可控方式初始化编号或查询生成结果。
- `EntityId` 使用正整数语义，底层可用 `long`。
- 所有可修改记录都必须持有 `EntityId`，包括任务、日程、学习计划、收支、笔记和 AI 草稿。
- 默认编号由 `IncrementalIdGenerator` 生成。
- 生产代码只依赖 `assistant.testability` 中的接口或简单基础实现；测试代码可使用专门实现控制外部状态。

建议行为契约：

- `EntityId` 位于 `assistant.common` 包，可实现为不可变 `record` 或 `final class`。
- `EntityId` 只接受大于 0 的 `long` 值；0 或负数应抛出 `IllegalArgumentException`，错误语义属于输入校验。
- `EntityId` 应提供清晰访问方式，例如 `value()`；若使用 record 则保留默认访问器。
- `EntityId` 应具备稳定的相等性和哈希语义，可安全作为 Map key。
- `EntityId` 建议实现 `Comparable<EntityId>`，按底层 `long` 升序比较，便于后续排序展示和测试断言。
- `EntityId.toString()` 应返回可读且稳定的字符串，便于调试和控制台展示；测试只断言稳定格式，不依赖对象地址。
- `IdGenerator` 位于 `assistant.testability` 包，接口方法建议为 `EntityId nextId()`。
- `IncrementalIdGenerator` 位于 `assistant.testability` 包，默认从 1 开始生成；也应支持传入自定义起始值以便测试。
- `IncrementalIdGenerator` 自定义起始值必须大于 0；非法起点抛出 `IllegalArgumentException`。
- 每次调用 `nextId()` 返回新的 `EntityId`，值按 1 递增。
- 本轮不要求实现线程安全并发语义；应用默认单用户命令行顺序执行。

## 已有代码上下文
当前项目已存在 Maven 单模块工程 `java-ai-assistant/`：

- `java-ai-assistant/pom.xml` 已配置 Java 17、JUnit Jupiter、Mockito、Jackson、Surefire、Failsafe 和 JaCoCo。
- `assistant.common.ErrorCode` 已包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT`、`SCHEDULE_CONFLICT`、AI 相关错误和 `SYSTEM_ERROR`。
- `assistant.common.BusinessException` 已支持携带非空 `ErrorCode`。
- `assistant.common.OperationResult<T>` 已支持成功/失败返回语义。
- `assistant.testability` 包目录已通过 `.gitkeep` 建立，但尚无生产类型。
- 普通单元测试不得读取真实环境变量、访问网络、依赖真实 DeepSeek API Key 或真实当前时间。

## RETRY 说明（仅 RETRY 时）
不适用。
