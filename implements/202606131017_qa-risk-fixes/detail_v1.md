# 详细设计（v1）

## 概述
本轮为 Java AI 助手补齐“控制台触发结构化 AI 建议 -> 解析 -> 保存草稿 -> 草稿列表查看 -> 确认导入或取消”的入口，并在新生成任务草稿保存前统一校验 `dueDate`。设计范围限定在 AI 草稿生成服务、应用装配、控制台草稿菜单和对应测试，不处理学习计划 breakdown 导入落地、摘要紧急事项、中文枚举体验和文档最终同步。

核心策略：
- 新增应用层服务 `StructuredSuggestionDraftService`，复用现有 `AiAssistantService.ask(...)`、`StructuredSuggestionParser`、`SuggestionDraftRepository` 和 `IdGenerator`。
- CLI 只读取用户目标、调用生成服务、打印 `OperationResult<SuggestionDraftView>`，不直接解析 JSON、不直接保存草稿。
- 任务草稿生成成功后、保存前校验所有 `TaskDraftItem.dueDate()` 非空；缺失时返回 `VALIDATION_ERROR`，不保存草稿，不创建正式任务。
- 学习计划草稿成功后保存完整 `StudyPlanDraftContent`，包括 parser 已支持并清洗后的 `breakdown`。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/ai/StructuredSuggestionDraftService.java` | 新建 | 结构化 AI 草稿生成、解析、保存前校验、保存并返回视图 |
| `java-ai-assistant/src/main/java/assistant/app/ApplicationServices.java` | 修改 | 新增 `StructuredSuggestionDraftService` 字段和空值校验 |
| `java-ai-assistant/src/main/java/assistant/app/ApplicationFactory.java` | 修改 | 装配 parser、生成服务并注入共享草稿仓储 |
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | AI 草稿菜单新增生成任务草稿和生成学习计划草稿入口 |
| `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionDraftServiceTest.java` | 新建 | 服务层端到端成功与失败路径单元测试 |
| `java-ai-assistant/src/test/java/assistant/app/ApplicationFactoryTest.java` | 修改 | 覆盖新增服务装配与 `ApplicationServices` 空值校验 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 覆盖控制台生成入口、列表查看、失败提示、确认/取消回归 |
| `java-ai-assistant/docs/test-plan.md` | 修改 | 补充结构化草稿生成服务和控制台入口测试范围 |
| `java-ai-assistant/docs/test-cases.md` | 修改 | 补充结构化草稿生成相关用例编号和链路说明 |

## 类型定义

### StructuredSuggestionDraftService
**形态**：`final class`
**包路径**：`assistant.ai`
**职责**：调用 AI 结构化场景，解析返回内容，分配草稿 id，校验生成草稿，保存 `SuggestionDraft`，返回 `SuggestionDraftView`。

**字段定义**：
```java
private final AiAssistantService aiAssistantService;
private final StructuredSuggestionParser parser;
private final SuggestionDraftRepository repository;
private final IdGenerator idGenerator;
```

**构造方式**：
```java
public StructuredSuggestionDraftService(
        AiAssistantService aiAssistantService,
        StructuredSuggestionParser parser,
        SuggestionDraftRepository repository,
        IdGenerator idGenerator)
```

**公开接口**：
```java
public OperationResult<SuggestionDraftView> generateTaskDraft(String userGoal)

public OperationResult<SuggestionDraftView> generateStudyPlanDraft(String userGoal)
```

**私有方法签名**：
```java
private OperationResult<SuggestionDraftView> generate(AiScenario scenario, String userGoal, SuggestionDraftType expectedType)

private OperationResult<SuggestionDraftView> validateGeneratedDraft(SuggestionDraft draft, SuggestionDraftType expectedType)

private OperationResult<SuggestionDraftView> validateTaskDueDates(SuggestionDraft draft)

private static OperationResult<SuggestionDraftView> toViewFailure(OperationResult<?> result)

private static OperationResult<SuggestionDraftView> failure(ErrorCode errorCode, String message)
```

**类型关系**：
- 组合 `AiAssistantService`：用于调用 `STRUCTURED_TASK_SUGGESTION` 或 `STRUCTURED_STUDY_PLAN_SUGGESTION`。
- 组合 `StructuredSuggestionParser`：用于把 AI 文本解析为 `SuggestionDraft`。
- 组合 `SuggestionDraftRepository`：保存通过校验的新草稿。
- 组合 `IdGenerator`：在解析前生成草稿 `EntityId`。
- 返回现有 `SuggestionDraftView.from(draft)`，不新增 DTO。

### ApplicationServices
**形态**：`record`
**包路径**：`assistant.app`
**职责**：应用服务容器。

**修改后的记录签名**：
```java
public record ApplicationServices(
        TaskService taskService,
        ScheduleService scheduleService,
        StudyPlanService studyPlanService,
        FinanceService financeService,
        NoteService noteService,
        SummaryService summaryService,
        AiAssistantService aiAssistantService,
        StructuredSuggestionDraftService structuredSuggestionDraftService,
        DraftLifecycleService draftLifecycleService,
        TimeProvider timeProvider)
```

**构造约束**：
- 紧跟 `aiAssistantService` 后校验 `Objects.requireNonNull(structuredSuggestionDraftService, "structuredSuggestionDraftService")`。
- 现有字段校验保持不变。

### ApplicationFactory
**形态**：`final class`
**包路径**：`assistant.app`
**职责**：生产环境和测试环境服务装配。

**修改点**：
- 在 `createWith(...)` 中复用既有 `IdGenerator idGenerator`。
- 保持单个 `SuggestionDraftRepository draftRepository = new InMemorySuggestionDraftRepository();` 同时注入生成服务和生命周期服务。
- 新增局部变量：
```java
StructuredSuggestionParser structuredSuggestionParser = new StructuredSuggestionParser();
StructuredSuggestionDraftService structuredSuggestionDraftService =
        new StructuredSuggestionDraftService(
                aiAssistantService,
                structuredSuggestionParser,
                draftRepository,
                idGenerator);
```
- `new ApplicationServices(...)` 参数顺序按新增记录签名传入 `structuredSuggestionDraftService`。

### ConsoleApplication
**形态**：`final class`
**包路径**：`assistant.app`
**职责**：控制台交互入口。

**菜单修改**：
`printDraftMenu()` 增加：
```text
g/task. 生成任务草稿
p/plan. 生成学习计划草稿
```

**命令分发修改**：
`dispatchDraftCommand(String rawCommand)` 增加：
```java
case "g", "task" -> generateTaskDraft();
case "p", "plan" -> generateStudyPlanDraft();
```

**新增私有方法签名**：
```java
private void generateTaskDraft()

private void generateStudyPlanDraft()

private void generateStructuredDraft(
        String prompt,
        Function<String, OperationResult<SuggestionDraftView>> generator)
```

**新增 import**：
```java
import java.util.function.Function;
```

**行为规则**：
- `generateTaskDraft()` 调用 `generateStructuredDraft("请输入任务草稿目标: ", services.structuredSuggestionDraftService()::generateTaskDraft)`。
- `generateStudyPlanDraft()` 调用 `generateStructuredDraft("请输入学习计划草稿目标: ", services.structuredSuggestionDraftService()::generateStudyPlanDraft)`。
- 读取到 EOF 时设置 `running = false` 并返回。
- 用户目标为空白时打印 `目标不能为空。`，不调用生成服务。
- 服务返回成功时复用 `printDraftResult(...)`，因此生成成功后立即打印 `AI 草稿详情`，用户随后可通过 `l/list`、`v/view` 查看并通过既有 `c/confirm`、`x/cancel` 操作。
- 服务返回失败时复用 `printResult(...)`，稳定显示 `失败: {ErrorCode} - {message}`。

## 错误处理
- `StructuredSuggestionDraftService` 公开方法对空白 `userGoal` 返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "user goal must not be blank")`。
- AI 未配置、调用失败、超时、鉴权失败、限流、空响应等由 `AiAssistantService.ask(...)` 返回原 `ErrorCode` 和 message，生成服务用 `toViewFailure(...)` 原样传播。
- 解析失败由 `StructuredSuggestionParser.parse(...)` 返回 `AI_MALFORMED_RESPONSE`，生成服务原样传播。
- AI 返回类型与入口不匹配时返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "AI structured suggestion type does not match requested draft type")`，不保存。
- 任务草稿任一 `TaskDraftItem` 缺少 `dueDate` 时返回 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, "task draft dueDate is required before saving")`，不保存。
- `SuggestionDraft` 或内容构造中的 `IllegalArgumentException` 已由 parser 捕获为 `AI_MALFORMED_RESPONSE`；生成服务不再捕获仓储内存保存异常，保持现有服务风格。
- 控制台所有失败统一通过现有 `printResult(...)` 输出。

## 行为契约
- `generateTaskDraft(userGoal)` 的调用顺序：
  1. 校验 `userGoal` 非空白。
  2. 生成新 `EntityId draftId = idGenerator.nextId()`。
  3. 调用 `aiAssistantService.ask(AiScenario.STRUCTURED_TASK_SUGGESTION, userGoal)`。
  4. AI 成功后调用 `parser.parse(aiText, draftId)`。
  5. 校验草稿类型为 `SuggestionDraftType.TASK_DRAFT`。
  6. 校验每个 `TaskDraftItem.hasDueDate()` 为 `true`。
  7. 调用 `repository.save(draft)`。
  8. 返回 `OperationResult.success(SuggestionDraftView.from(draft))`。
- `generateStudyPlanDraft(userGoal)` 与任务入口相同，但场景为 `STRUCTURED_STUDY_PLAN_SUGGESTION`，期望类型为 `STUDY_PLAN_DRAFT`，不执行任务 dueDate 校验。
- 任一失败路径不得调用 `repository.save(...)`，不得调用 `DraftImportService`，不得修改正式任务或学习计划数据。
- 学习计划生成成功后保存 parser 返回的 `StudyPlanDraftContent` 原内容；`breakdown` 使用 `StudyPlanDraftContent` 构造器清洗后的列表。
- 生成服务只负责创建 `CONFIRMABLE` 草稿；确认、取消、重复确认保护继续由 `DraftLifecycleService` 负责。
- `ApplicationFactory` 必须让生成服务和生命周期服务共享同一个 `SuggestionDraftRepository`，否则 CLI 生成后列表和查看不可见。
- 控制台生成成功后不自动确认导入；用户必须显式执行 `c/confirm`。

## 依赖关系
- 依赖已有类型：
  - `assistant.ai.AiAssistantService`
  - `assistant.ai.AiScenario`
  - `assistant.ai.StructuredSuggestionParser`
  - `assistant.ai.SuggestionDraft`
  - `assistant.ai.SuggestionDraftType`
  - `assistant.ai.SuggestionDraftView`
  - `assistant.ai.SuggestionDraftRepository`
  - `assistant.ai.TaskDraftItem`
  - `assistant.common.EntityId`
  - `assistant.common.ErrorCode`
  - `assistant.common.OperationResult`
  - `assistant.testability.IdGenerator`
- 暴露给后续任务的公开接口：
  - `ApplicationServices.structuredSuggestionDraftService()`
  - `StructuredSuggestionDraftService.generateTaskDraft(String userGoal)`
  - `StructuredSuggestionDraftService.generateStudyPlanDraft(String userGoal)`
- 不修改 `TaskDraftItem` 允许 `dueDate == null` 的既有模型，以保留历史草稿查看和导入服务回归测试；只对“新生成草稿保存前”增加限制。
- 不修改 `DraftImportService.importStudyPlan(...)` 的 breakdown 导入策略，本轮只保证生成草稿保存时 breakdown 不丢失。

## 测试设计

### StructuredSuggestionDraftServiceTest
**新建测试类**：`assistant.ai.StructuredSuggestionDraftServiceTest`

**测试支撑类型**：
```java
private static final class FakeAiAssistantService extends AiAssistantService
```
不可行，因为 `AiAssistantService` 为 `final`。因此测试应使用真实 `AiAssistantService`，并注入 fake `ContextProvider`、mock/自定义 `PromptBuilder` 和 fake `AiClient`；或直接用 Mockito mock `AiAssistantService` 不可行，因为该类为 `final` 且当前未启用 inline mock。推荐构造真实 `AiAssistantService`。

**推荐 fake 类型签名**：
```java
private static final class FakeContextProvider implements ContextProvider {
    @Override
    public OperationResult<LocalContext> getLocalContext()
}

private static final class FakeAiClient implements AiClient {
    private OperationResult<AiResponse> result;
    private AiRequest receivedRequest;
    private int calls;
    @Override
    public OperationResult<AiResponse> chat(AiRequest request)
}
```

**用例要求**：
- `generateTaskDraftParsesAssignsIdAndSavesDraft`：fake AI 返回合法 `TASK_DRAFT` JSON，断言返回成功、id 为固定生成值、仓储有同 id 草稿、任务 dueDate 为返回值。
- `generateStudyPlanDraftParsesAssignsIdSavesBreakdown`：fake AI 返回合法 `STUDY_PLAN_DRAFT` JSON 且含 `breakdown`，断言仓储保存、view 中 breakdown 完整。
- `generateTaskDraftRejectsMissingDueDateWithoutSaving`：fake AI 返回任务缺 `dueDate`，断言 `VALIDATION_ERROR`、消息为 `task draft dueDate is required before saving`、仓储为空。
- `generateDraftPropagatesNotConfiguredWithoutSaving`：使用 `AiConfiguration.defaultWithoutApiKey()`，断言 `AI_NOT_CONFIGURED`、fake client 未调用、仓储为空。
- `generateDraftRejectsBlankGoalWithoutCallingAi`：空白目标返回 `VALIDATION_ERROR`，fake client 未调用。
- `generateDraftPropagatesEmptyResponseWithoutSaving`：fake client 返回 payload 为 `null` 或内容为空导致 `AI_EMPTY_RESPONSE`/`AI_MALFORMED_RESPONSE`，仓储为空。
- `generateDraftRejectsMalformedJsonWithoutSaving`：fake AI 返回非 JSON，断言 `AI_MALFORMED_RESPONSE`。
- `generateDraftRejectsInvalidStructuredFieldsWithoutSaving`：fake AI 返回非法 priority、非法日期或空任务数组，断言 `AI_MALFORMED_RESPONSE`。
- `generateTaskDraftRejectsMismatchedStudyPlanTypeWithoutSaving`：任务入口返回学习计划草稿，断言 `VALIDATION_ERROR`。
- `constructorRejectsNullDependencies`：四个依赖分别为空时抛 `NullPointerException`，消息为字段名。

### ConsoleApplicationTest
**现有构造器同步**：
- 所有 `new ApplicationServices(...)` 增加 `structuredSuggestionDraftService` 参数。
- 新增 helper：
```java
private static ApplicationServices withStructuredSuggestionDraftService(
        ApplicationServices baseServices,
        StructuredSuggestionDraftService structuredSuggestionDraftService)
```
若 `StructuredSuggestionDraftService` 为 `final` 且不能 mock，CLI 测试可使用真实生成服务配合真实 `AiAssistantService` fake client；或者新增测试专用 `ApplicationServices` helper 从 `ApplicationFactory` 创建真实共享仓储链路后覆盖 AI 配置不可行。推荐服务层充分测生成逻辑，CLI 层通过真实服务 + fake AI 构造完整 `ApplicationServices`。

**CLI 用例要求**：
- `draftMenuGeneratesTaskDraftAndListsAndViewsIt`：输入 `8\ng\n整理明天任务\nl\nv\n{生成id}\nb\nq\n`，断言输出含生成草稿详情、列表、标题、dueDate。
- `draftMenuGeneratesStudyPlanDraftAndDisplaysBreakdown`：输入 `8\np\n准备考试\nv\n{生成id}\nb\nq\n`，断言输出含学习计划目标和 breakdown。
- `draftMenuGenerateRejectsBlankGoalWithoutCallingService`：输入生成命令后空白目标，断言 `目标不能为空。`。
- `draftMenuGenerateFailureDisplaysStableMessage`：服务返回 AI 未配置或格式错误时，断言输出 `失败: AI_NOT_CONFIGURED - ...` 或 `失败: AI_MALFORMED_RESPONSE - ...`。
- `generatedTaskDraftCanConfirmCancelAndRejectRepeatConfirm`：使用真实仓储链路生成任务草稿后确认导入，断言任务列表出现正式任务；另一路生成后取消，断言任务列表不出现；对已导入草稿再次确认输出 `STATE_CONFLICT`。

### ApplicationFactoryTest
- `createWithExplicitConfigurationBuildsAllServices` 增加断言 `services.structuredSuggestionDraftService()` 非空。
- `applicationServicesRejectsNullComponents` 增加 `structuredSuggestionDraftService` 为空的断言。
- 如需证明共享仓储，可新增 `createWiresGeneratedDraftsIntoLifecycleService`：在可控 AI 依赖不可注入的当前 factory 下不强制测试真实生成；共享仓储由服务层和 CLI 链路测试覆盖即可。

### 文档一致性测试
- 若 `DocumentationDeliveryTest` 检查文档内容或关键词，补充 `test-plan.md` 和 `test-cases.md` 后运行现有文档测试。
- 文档更新不写入虚假的测试数量、覆盖率或执行时间。

## 验证要求
- 编码完成后从 `java-ai-assistant/` 执行：
```bash
mvn clean test
```
- 文档一致性测试包含在 Maven 默认测试中；如失败需按测试提示补齐文档引用。
