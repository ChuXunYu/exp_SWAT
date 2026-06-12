# 详细设计（v20）

## 概述

本轮设计目标是在 `assistant.ai` 包中新增 AI 结构化建议草稿的领域模型、只读快照、内存仓储和 JSON 解析器，为后续 `DraftLifecycleService`、`DraftImportService` 与控制台确认流程提供稳定中间表示。

本轮实现范围：

- `SuggestionDraftType`：限定结构化建议业务类型为任务草稿或学习计划草稿。
- `SuggestionDraftStatus`：限定草稿生命周期状态，并提供状态判断辅助方法。
- `TaskDraftItem`：不可变任务草稿 DTO，表达标题、描述、优先级和可选截止日期。
- `StudyPlanDraftContent`：不可变学习计划草稿 DTO，表达目标、周期、投入小时、初始进度和拆解说明。
- `SuggestionDraft`：草稿聚合根，保护类型与内容匹配、状态迁移和内部集合不可变。
- `SuggestionDraftView`：只读 DTO，从聚合根创建不可变快照，供查询或后续服务返回。
- `SuggestionDraftRepository` / `InMemorySuggestionDraftRepository`：草稿持有边界和默认内存实现。
- `StructuredSuggestionParser`：使用 Jackson `JsonNode` 将 AI 文本解析为 `OperationResult<SuggestionDraft>`。
- 对应单元测试覆盖枚举、DTO 校验、聚合根状态流转、视图快照、仓储顺序和解析器成功/失败路径。

本轮不实现：

- `DraftLifecycleService`、`DraftImportService`、正式任务或学习计划导入、导入回滚。
- `AiAssistantService` 自动创建草稿、控制台确认流程或真实网络集成。
- JSON Schema、从自然语言中猜测 JSON、多个 fenced code block 解析。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftType.java` | 新建 | 定义结构化建议草稿类型枚举。 |
| `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftStatus.java` | 新建 | 定义草稿状态枚举和可确认状态判断。 |
| `java-ai-assistant/src/main/java/assistant/ai/TaskDraftItem.java` | 新建 | 定义不可变任务草稿项 DTO，校验标题和优先级，允许截止日期为空。 |
| `java-ai-assistant/src/main/java/assistant/ai/StudyPlanDraftContent.java` | 新建 | 定义不可变学习计划草稿内容 DTO，校验目标、日期范围、投入小时、进度和拆解说明列表。 |
| `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraft.java` | 新建 | 定义草稿聚合根，维护 id、类型、状态、内容匹配和状态迁移。 |
| `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftView.java` | 新建 | 定义草稿只读快照 DTO，避免服务返回内部可变聚合根。 |
| `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftRepository.java` | 新建 | 定义保存、按 id 查询、查询全部、删除的最小仓储契约。 |
| `java-ai-assistant/src/main/java/assistant/ai/InMemorySuggestionDraftRepository.java` | 新建 | 使用 `LinkedHashMap<EntityId, SuggestionDraft>` 保存草稿并保持插入顺序。 |
| `java-ai-assistant/src/main/java/assistant/ai/StructuredSuggestionParser.java` | 新建 | 使用 Jackson `JsonNode` 解析完整 JSON 或单个 fenced JSON 代码块为可确认草稿。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTypeTest.java` | 新建 | 覆盖草稿类型枚举值和名称稳定性。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftStatusTest.java` | 新建 | 覆盖状态枚举值、名称稳定性和辅助判断方法。 |
| `java-ai-assistant/src/test/java/assistant/ai/TaskDraftItemTest.java` | 新建 | 覆盖任务草稿字段归一化、可选截止日期和非法字段校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/StudyPlanDraftContentTest.java` | 新建 | 覆盖学习计划草稿字段归一化、拆解说明快照和日期/数值校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTest.java` | 新建 | 覆盖工厂方法、类型内容匹配、任务列表快照和状态冲突。 |
| `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftViewTest.java` | 新建 | 覆盖从聚合根映射字段、快照不可变和 null 参数校验。 |
| `java-ai-assistant/src/test/java/assistant/ai/InMemorySuggestionDraftRepositoryTest.java` | 新建 | 覆盖保存替换、按 id 查询、插入顺序、快照列表和删除行为。 |
| `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionParserTest.java` | 新建 | 覆盖任务/学习计划 JSON、fenced JSON、默认字段、未知类型、字段缺失、类型错误、非法日期、混入自然语言、尾随 token 和多个根 JSON 值等解析路径。 |

## 类型定义

### `SuggestionDraftType`

**形态**：`enum`

**包路径**：`assistant.ai`

**职责**：限定 AI 结构化建议的业务类型，避免解析器或后续导入服务接收额外业务类型。

**类型签名定义**：

```java
public enum SuggestionDraftType
```

**枚举值**：

| 值 | 语义 |
|----|------|
| `TASK_DRAFT` | 一个或多个待办任务草稿。 |
| `STUDY_PLAN_DRAFT` | 一个学习计划草稿，可包含拆解说明。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public boolean isTaskDraft()` | `boolean` | 当前值为 `TASK_DRAFT` 时返回 `true`。 |
| `public boolean isStudyPlanDraft()` | `boolean` | 当前值为 `STUDY_PLAN_DRAFT` 时返回 `true`。 |

**构造方式**：

- Java 枚举常量。
- `StructuredSuggestionParser` 只允许通过精确大小写的 `SuggestionDraftType.valueOf(typeText)` 解析。

**类型关系**：

- 被 `SuggestionDraft`、`SuggestionDraftView` 和 `StructuredSuggestionParser` 使用。

### `SuggestionDraftStatus`

**形态**：`enum`

**包路径**：`assistant.ai`

**职责**：表达草稿生命周期状态，并提供后续服务判断可确认性的稳定语义。

**类型签名定义**：

```java
public enum SuggestionDraftStatus
```

**枚举值**：

| 值 | 语义 |
|----|------|
| `CONFIRMABLE` | 草稿可确认或取消。 |
| `CANCELLED` | 用户已取消，不能再导入。 |
| `IMPORTED` | 已成功导入正式数据，不能重复导入。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public boolean isConfirmable()` | `boolean` | 当前值为 `CONFIRMABLE` 时返回 `true`。 |
| `public boolean isTerminal()` | `boolean` | 当前值为 `CANCELLED` 或 `IMPORTED` 时返回 `true`。 |

**构造方式**：

- Java 枚举常量。
- 新建 `SuggestionDraft` 时初始状态固定为 `CONFIRMABLE`。

**类型关系**：

- 被 `SuggestionDraft` 和 `SuggestionDraftView` 使用。

### `TaskDraftItem`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：表达 AI 生成的待办任务草稿项，不复用正式 `TaskItem`，避免绕过 `TaskService` 的正式校验。

**类型签名定义**：

```java
public record TaskDraftItem(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `String title` | 非空；执行 `strip()`；归一化后不得为空白；保存归一化值。 |
| `String description` | 可空；`null` 归一化为 `""`；非空时执行 `strip()`；保存归一化值。 |
| `TaskPriority priority` | 非空；解析器只接受 `LOW`、`MEDIUM`、`HIGH` 精确大小写文本。 |
| `LocalDate dueDate` | 可空；解析器只接受 ISO-8601 本地日期字符串；缺省保存为 `null`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public TaskDraftItem` 规范构造器 | `TaskDraftItem` | 校验并归一化字段；`title == null` 抛 `NullPointerException("title")`；`priority == null` 抛 `NullPointerException("priority")`；标题空白抛 `IllegalArgumentException("title must not be blank")`。 |
| `public boolean hasDueDate()` | `boolean` | `dueDate != null` 时返回 `true`。 |

**构造方式**：

- `StructuredSuggestionParser` 从任务 JSON 项创建。
- 单元测试可直接构造验证 DTO 约束。

**类型关系**：

- 依赖 `assistant.task.TaskPriority` 和 JDK `java.time.LocalDate`。
- 被 `SuggestionDraft` 和 `SuggestionDraftView` 组合。

### `StudyPlanDraftContent`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：表达 AI 生成的学习计划草稿内容，不复用正式 `StudyPlan`，后续导入仍需调用 `StudyPlanService`。

**类型签名定义**：

```java
public record StudyPlanDraftContent(
        String goalName,
        LocalDate startDate,
        LocalDate endDate,
        int expectedHours,
        Progress initialProgress,
        List<String> breakdown)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `String goalName` | 非空；执行 `strip()`；归一化后不得为空白；保存归一化值。 |
| `LocalDate startDate` | 非空；解析器只接受 ISO-8601 本地日期字符串。 |
| `LocalDate endDate` | 非空；不得早于 `startDate`。 |
| `int expectedHours` | 必须大于 `0`。 |
| `Progress initialProgress` | 非空；取值约束由 `Progress` 保证为 `0..100`；解析器缺省使用 `Progress.zero()`。 |
| `List<String> breakdown` | 非空；元素非空；每个元素执行 `strip()`；空白元素丢弃；保存为不可修改快照；缺省为空列表。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public StudyPlanDraftContent` 规范构造器 | `StudyPlanDraftContent` | 校验并归一化字段；空引用抛对应参数名的 `NullPointerException`；空目标、结束日期早于开始日期、非正投入小时抛 `IllegalArgumentException`。 |
| `public boolean hasBreakdown()` | `boolean` | `breakdown` 非空时返回 `true`。 |

**构造方式**：

- `StructuredSuggestionParser` 从 `studyPlan` JSON 对象创建。
- 单元测试可直接构造验证 DTO 约束。

**类型关系**：

- 依赖 `assistant.common.Progress` 和 JDK `LocalDate`、`List`。
- 被 `SuggestionDraft` 和 `SuggestionDraftView` 组合。

### `SuggestionDraft`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：作为 AI 结构化建议草稿聚合根，保证草稿 id、类型、内容和状态迁移一致。

**类型签名定义**：

```java
public final class SuggestionDraft
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final EntityId id` | 非空。 |
| `private final SuggestionDraftType type` | 非空。 |
| `private SuggestionDraftStatus status` | 非空；新草稿固定为 `CONFIRMABLE`。 |
| `private final List<TaskDraftItem> tasks` | 非空不可修改快照；`TASK_DRAFT` 时至少一个；`STUDY_PLAN_DRAFT` 时为空列表。 |
| `private final StudyPlanDraftContent studyPlan` | `STUDY_PLAN_DRAFT` 时非空；`TASK_DRAFT` 时为空。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static SuggestionDraft forTasks(EntityId id, List<TaskDraftItem> tasks)` | `SuggestionDraft` | 创建 `TASK_DRAFT`，状态为 `CONFIRMABLE`；`id` 和 `tasks` 非空；任务列表至少一个且元素非空；保存不可修改快照。 |
| `public static SuggestionDraft forStudyPlan(EntityId id, StudyPlanDraftContent studyPlan)` | `SuggestionDraft` | 创建 `STUDY_PLAN_DRAFT`，状态为 `CONFIRMABLE`；`id` 和 `studyPlan` 非空；任务列表为空。 |
| `public EntityId getId()` | `EntityId` | 返回草稿 id。 |
| `public SuggestionDraftType getType()` | `SuggestionDraftType` | 返回草稿类型。 |
| `public SuggestionDraftStatus getStatus()` | `SuggestionDraftStatus` | 返回当前状态。 |
| `public List<TaskDraftItem> getTasks()` | `List<TaskDraftItem>` | 返回不可修改任务草稿快照；学习计划草稿返回空列表。 |
| `public Optional<StudyPlanDraftContent> getStudyPlan()` | `Optional<StudyPlanDraftContent>` | 学习计划草稿返回内容；任务草稿返回 `Optional.empty()`。 |
| `public boolean isConfirmable()` | `boolean` | 委托 `status.isConfirmable()`。 |
| `public void cancel()` | `void` | 仅允许 `CONFIRMABLE -> CANCELLED`；否则抛 `BusinessException(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable")`。 |
| `public void markImported()` | `void` | 仅允许 `CONFIRMABLE -> IMPORTED`；否则抛 `BusinessException(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable")`。 |

**构造方式**：

- 外部只能通过 `forTasks(...)` 或 `forStudyPlan(...)` 创建。
- `StructuredSuggestionParser` 成功解析时调用工厂方法并产生 `CONFIRMABLE` 草稿。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.BusinessException`、`assistant.common.ErrorCode`。
- 组合 `TaskDraftItem` 或 `StudyPlanDraftContent`，二者互斥。
- 被 `SuggestionDraftRepository` 保存，被 `SuggestionDraftView.from(...)` 快照化。

### `SuggestionDraftView`

**形态**：`record`

**包路径**：`assistant.ai`

**职责**：提供草稿只读快照，避免查询或后续服务向外暴露内部可变 `SuggestionDraft` 引用。

**类型签名定义**：

```java
public record SuggestionDraftView(
        EntityId id,
        SuggestionDraftType type,
        SuggestionDraftStatus status,
        List<TaskDraftItem> tasks,
        Optional<StudyPlanDraftContent> studyPlan)
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `EntityId id` | 非空。 |
| `SuggestionDraftType type` | 非空。 |
| `SuggestionDraftStatus status` | 非空。 |
| `List<TaskDraftItem> tasks` | 非空；元素非空；保存为不可修改快照。 |
| `Optional<StudyPlanDraftContent> studyPlan` | 非空；由聚合根内容映射而来。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public SuggestionDraftView` 规范构造器 | `SuggestionDraftView` | 校验字段并复制 `tasks` 为不可修改快照；空引用抛对应参数名的 `NullPointerException`；任务元素为空抛 `NullPointerException("task")`。 |
| `public static SuggestionDraftView from(SuggestionDraft draft)` | `SuggestionDraftView` | `draft == null` 抛 `NullPointerException("draft")`；复制当前 id、type、status、tasks 和 studyPlan。 |
| `public boolean isConfirmable()` | `boolean` | 委托 `status.isConfirmable()`。 |

**构造方式**：

- 后续查询服务或单元测试通过 `SuggestionDraftView.from(draft)` 创建。

**类型关系**：

- 依赖 `SuggestionDraft`、`SuggestionDraftType`、`SuggestionDraftStatus`、`TaskDraftItem`、`StudyPlanDraftContent` 和 `EntityId`。

### `SuggestionDraftRepository`

**形态**：`interface`

**包路径**：`assistant.ai`

**职责**：定义 AI 草稿的运行期持有边界。

**类型签名定义**：

```java
public interface SuggestionDraftRepository
```

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `void save(SuggestionDraft draft)` | `void` | 保存或替换同 id 草稿；`draft == null` 由实现抛 `NullPointerException("draft")`。 |
| `Optional<SuggestionDraft> findById(EntityId id)` | `Optional<SuggestionDraft>` | `id == null` 由实现抛 `NullPointerException("id")`；不存在返回空。 |
| `List<SuggestionDraft> findAll()` | `List<SuggestionDraft>` | 返回按插入顺序排列的不可修改快照列表。 |
| `boolean deleteById(EntityId id)` | `boolean` | `id == null` 由实现抛 `NullPointerException("id")`；存在并删除返回 `true`。 |

**构造方式**：

- 接口无构造。
- 默认实现为 `InMemorySuggestionDraftRepository`。

**类型关系**：

- 依赖 `assistant.common.EntityId`。
- 后续 `DraftLifecycleService` 将依赖该接口，而非具体内存实现。

### `InMemorySuggestionDraftRepository`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：使用内存 `LinkedHashMap` 保存草稿，保持插入顺序，作为本版默认仓储。

**类型签名定义**：

```java
public final class InMemorySuggestionDraftRepository implements SuggestionDraftRepository
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final Map<EntityId, SuggestionDraft> drafts` | 初始化为 `new LinkedHashMap<>()`；key 为草稿 id，value 为聚合根引用。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `@Override public void save(SuggestionDraft draft)` | `void` | `draft == null` 抛 `NullPointerException("draft")`；按 `draft.getId()` 保存；同 id 替换 value，不改变既有 key 的插入位置。 |
| `@Override public Optional<SuggestionDraft> findById(EntityId id)` | `Optional<SuggestionDraft>` | `id == null` 抛 `NullPointerException("id")`；返回内部聚合根引用，供后续生命周期服务修改状态。 |
| `@Override public List<SuggestionDraft> findAll()` | `List<SuggestionDraft>` | 返回 `List.copyOf(drafts.values())`，保持插入顺序且列表不可修改；列表是快照，后续保存不改变旧列表大小。 |
| `@Override public boolean deleteById(EntityId id)` | `boolean` | `id == null` 抛 `NullPointerException("id")`；删除成功返回 `true`。 |

**构造方式**：

- 直接 `new InMemorySuggestionDraftRepository()`。

**类型关系**：

- 实现 `SuggestionDraftRepository`。
- 不依赖 `TaskService`、`StudyPlanService` 或 AI 客户端。

### `StructuredSuggestionParser`

**形态**：`final class`

**包路径**：`assistant.ai`

**职责**：将 AI 返回文本解析为受控 `SuggestionDraft`，对所有 AI JSON 结构异常返回 `AI_MALFORMED_RESPONSE`，不得写入正式业务仓储。

**类型签名定义**：

```java
public final class StructuredSuggestionParser
```

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final ObjectMapper objectMapper` | 非空；用于配合 `JsonParser` 读取 `JsonNode`；不得直接用 `readTree(String)` 完成解析，因为必须显式拒绝根 JSON 后的尾随 token。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public StructuredSuggestionParser()` | `StructuredSuggestionParser` | 使用 `new ObjectMapper()`。 |
| `public StructuredSuggestionParser(ObjectMapper objectMapper)` | `StructuredSuggestionParser` | `objectMapper == null` 抛 `NullPointerException("objectMapper")`。 |
| `public OperationResult<SuggestionDraft> parse(String aiText, EntityId draftId)` | `OperationResult<SuggestionDraft>` | `draftId == null` 抛 `NullPointerException("draftId")`；`aiText` 为空、空白、非单个 JSON 对象、字段缺失、字段类型错误、枚举未知、日期非法或 DTO 校验失败时返回 `OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI structured suggestion format is invalid")`；成功返回 `SuggestionDraft` 且状态为 `CONFIRMABLE`。 |

**内部解析辅助方法**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private JsonNode parseSingleJsonValue(String text) throws IOException` | `JsonNode` | 使用 `objectMapper.getFactory().createParser(text)` 创建 `JsonParser`，再调用 `objectMapper.readTree(parser)` 读取首个 JSON 值；读取后必须调用 `parser.nextToken()` 检查后续 token，只有返回 `null` 才表示 EOF；若存在任何非空尾随 token、连续第二个 JSON 根值、JSON 后自然语言或 tokenization 失败，向上抛出 Jackson/I/O 异常并由 `parse(...)` 统一转换为 `AI_MALFORMED_RESPONSE`。 |

**JSON 输入契约**：

任务草稿根对象：

```text
type: string, 必须精确为 "TASK_DRAFT"
tasks: array, 必须存在、非空，元素必须为 object
tasks[].title: string, 必须存在且归一化后非空
tasks[].description: string, 可选；缺省为 ""
tasks[].priority: string, 必须存在，精确为 "LOW" / "MEDIUM" / "HIGH"
tasks[].dueDate: string, 可选；存在时必须为 ISO-8601 LocalDate
```

学习计划草稿根对象：

```text
type: string, 必须精确为 "STUDY_PLAN_DRAFT"
studyPlan: object, 必须存在
studyPlan.goalName: string, 必须存在且归一化后非空
studyPlan.startDate: string, 必须存在且为 ISO-8601 LocalDate
studyPlan.endDate: string, 必须存在且为 ISO-8601 LocalDate，且不得早于 startDate
studyPlan.expectedHours: integer, 必须存在且 > 0
studyPlan.initialProgress: integer, 可选；缺省为 0；必须在 0..100
studyPlan.breakdown: array<string>, 可选；缺省为空列表；存在时必须为数组且元素必须为 string
```

**fenced JSON 处理契约**：

- 优先按 `aiText` 完整文本解析 JSON。
- 完整文本解析失败后，仅当 `aiText.strip()` 是单个完整 fenced code block 时尝试去围栏后解析。
- 支持开头围栏为 ```` ```json ```` 或 ```` ``` ````，结尾围栏必须是最后一行的 ```` ``` ````。
- 完整文本解析和去围栏后的内容解析都必须通过 `parseSingleJsonValue(...)` 的 EOF 检查，拒绝根 JSON 对象后的任何非空尾随 token。
- fenced 内容解析后根节点仍必须是单个 JSON object；如果 fenced 内容为 `{"type":"TASK_DRAFT",...} text`、两个连续 JSON 对象或 JSON 数组等，均失败。
- JSON 前后存在自然语言、存在多个围栏、围栏外存在非空文本、围栏内容不是单个 JSON object，均返回 `AI_MALFORMED_RESPONSE`。

**构造方式**：

- 生产或单元测试可直接 `new StructuredSuggestionParser()`。
- 需要控制 Jackson 行为的测试可注入 `ObjectMapper`。
- 草稿编号由调用方传入 `draftId`，便于单元测试固定断言；本轮不在解析器中注入 `IdGenerator`。

**类型关系**：

- 依赖 Jackson `com.fasterxml.jackson.databind.ObjectMapper`、`JsonNode`。
- 依赖 `assistant.common.OperationResult`、`ErrorCode`、`EntityId`、`Progress`。
- 依赖 `assistant.task.TaskPriority`。
- 只创建 `SuggestionDraft`，不依赖或调用 `TaskService`、`StudyPlanService`、正式仓储或 `AiAssistantService`。

## 错误处理

- DTO 和聚合根构造的编程错误使用现有项目风格：空引用抛 `NullPointerException`，业务字段非法抛 `IllegalArgumentException`。
- `SuggestionDraft.cancel()` 和 `SuggestionDraft.markImported()` 的状态冲突统一抛 `BusinessException(ErrorCode.STATE_CONFLICT, "suggestion draft is not confirmable")`。
- `StructuredSuggestionParser.parse(...)` 面向 AI 输出，所有 AI 响应结构问题统一返回 `OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, "AI structured suggestion format is invalid")`，包括：
  - `aiText == null`、空白、JSON 解析失败。
  - 首个合法 JSON 根值之后存在任何非空尾随 token，例如自然语言文本或第二个 JSON 对象。
  - 根节点不是 object。
  - `type` 缺失、不是 string、未知、大小写不匹配。
  - 业务字段缺失、类型错误、数组为空、数组元素不是 object/string。
  - 日期不是 ISO-8601 本地日期、数值不是整数、进度越界、结束日期早于开始日期。
  - `TaskPriority` 文本未知或大小写不匹配。
  - DTO 或聚合根校验抛出的 `IllegalArgumentException`。
- `StructuredSuggestionParser.parse(...)` 对 `draftId == null` 抛 `NullPointerException("draftId")`，因为这是调用方编程错误，不属于 AI 格式异常。
- 解析失败不得创建或保存草稿；本轮解析器不持有仓储引用，因此不存在半写入。

## 行为契约

- 新建 `SuggestionDraft` 的状态总是 `CONFIRMABLE`。
- `TASK_DRAFT` 草稿必须包含至少一个 `TaskDraftItem`，且 `getStudyPlan()` 必须为空。
- `STUDY_PLAN_DRAFT` 草稿必须包含一个 `StudyPlanDraftContent`，且 `getTasks()` 必须为空列表。
- `SuggestionDraft.getTasks()`、`SuggestionDraftView.tasks()`、`StudyPlanDraftContent.breakdown()` 和仓储 `findAll()` 返回的列表均不可修改。
- `SuggestionDraftView.from(draft)` 必须复制创建时刻的状态和列表快照；之后聚合根状态变化不得改变旧 view 的 `status`。
- `cancel()` 只允许从 `CONFIRMABLE` 到 `CANCELLED`；重复取消、导入后取消均抛 `STATE_CONFLICT`。
- `markImported()` 只允许从 `CONFIRMABLE` 到 `IMPORTED`；取消后导入、重复导入均抛 `STATE_CONFLICT`。
- `InMemorySuggestionDraftRepository` 保存聚合根引用，后续生命周期服务可通过 `findById` 取得同一对象并迁移状态；查询展示时必须使用 `SuggestionDraftView` 快照，避免暴露内部可变对象。
- `InMemorySuggestionDraftRepository.findAll()` 保持插入顺序；同 id 替换不改变原 key 位置。
- `StructuredSuggestionParser` 不接受自然语言包裹 JSON，不从文本中截取 JSON，不接受多个 fenced block。
- `StructuredSuggestionParser` 对完整 JSON 文本和 fenced 内容使用同一套严格完整性规则：只能存在一个 JSON 根值，根值后除空白外必须到 EOF；`{"type":"TASK_DRAFT",...} text`、`{"type":"TASK_DRAFT",...}{"type":"TASK_DRAFT",...}` 和 fenced 内容内 JSON 后追加文本都必须返回 `AI_MALFORMED_RESPONSE`。
- `StructuredSuggestionParser` 对额外 JSON 字段保持忽略；只校验本轮固定契约中的 `type` 与必要业务字段。
- `description` 缺省为 `""`；`dueDate` 缺省为 `null`；`initialProgress` 缺省为 `Progress.zero()`；`breakdown` 缺省为空列表。

## 测试规格补充

`StructuredSuggestionParserTest` 必须新增以下严格 JSON 完整性用例，均调用 `parse(...)` 并断言 `isFailure()` 且 `getErrorCode() == ErrorCode.AI_MALFORMED_RESPONSE`：

| 用例 | 输入形态 | 预期 |
|------|----------|------|
| JSON 后尾随自然语言 | `{"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]} extra text` | 失败，不创建草稿。 |
| 连续两个 JSON 根对象 | `{"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}{"type":"TASK_DRAFT","tasks":[{"title":"B","priority":"LOW"}]}` | 失败，不只取第一个对象。 |
| fenced 内容内 JSON 后尾随文本 | 单个 fenced block，围栏内部为 `{"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]} extra text` | 失败，去围栏后仍执行 EOF 检查。 |

## 依赖关系

- 复用现有公共类型：
  - `assistant.common.EntityId`
  - `assistant.common.OperationResult`
  - `assistant.common.ErrorCode`
  - `assistant.common.BusinessException`
  - `assistant.common.Progress`
- 复用现有任务类型：
  - `assistant.task.TaskPriority`
- 复用既有依赖：
  - Maven 已包含 `com.fasterxml.jackson.core:jackson-databind`，本轮无需新增依赖。
- 暴露给后续任务的公开接口：
  - `SuggestionDraftType`
  - `SuggestionDraftStatus`
  - `TaskDraftItem`
  - `StudyPlanDraftContent`
  - `SuggestionDraft.forTasks(...)`
  - `SuggestionDraft.forStudyPlan(...)`
  - `SuggestionDraft.cancel()`
  - `SuggestionDraft.markImported()`
  - `SuggestionDraftView.from(...)`
  - `SuggestionDraftRepository`
  - `InMemorySuggestionDraftRepository`
  - `StructuredSuggestionParser.parse(String aiText, EntityId draftId)`
- 本轮明确不依赖：
  - `TaskService`
  - `StudyPlanService`
  - `AiClient`
  - `AiAssistantService`
  - `IdGenerator`
  - 真实网络、真实 DeepSeek、真实 API Key、系统当前时间或外部文件。

## 修订说明（v20 r1)

| 审查意见 | 修改措施 |
|---------|---------|
| `StructuredSuggestionParser` 未明确拒绝根 JSON 对象后的尾随 token，编码者若直接使用 `ObjectMapper.readTree(String)` 可能接受 JSON 后自然语言或连续两个 JSON 根值。 | 在 `StructuredSuggestionParser` 类型定义中补充 `parseSingleJsonValue(String)` 内部方法契约，要求使用 `JsonParser` 读取首个 `JsonNode` 后调用 `parser.nextToken()` 检查 EOF；完整 JSON 文本和去围栏后的内容都必须经过该检查，任何非空尾随 token、第二个根 JSON 值或 tokenization 失败均转换为 `AI_MALFORMED_RESPONSE`。 |
| 缺少覆盖尾随 token 的解析器测试要求。 | 在文件规划、行为契约和新增“测试规格补充”中要求 `StructuredSuggestionParserTest` 覆盖 JSON 后尾随自然语言、连续两个 JSON 根对象、fenced 内容内 JSON 后尾随文本，均断言 `AI_MALFORMED_RESPONSE` 且不创建草稿。 |
