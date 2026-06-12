# 任务指令（v20）

## 动作
NEW

## 任务描述
新增 AI 结构化建议的草稿类型、草稿状态、任务草稿项、学习计划草稿内容、草稿聚合根、草稿只读视图、草稿仓储契约、内存仓储和结构化建议解析器。

预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftType.java`
- `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftStatus.java`
- `java-ai-assistant/src/main/java/assistant/ai/TaskDraftItem.java`
- `java-ai-assistant/src/main/java/assistant/ai/StudyPlanDraftContent.java`
- `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraft.java`
- `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftView.java`
- `java-ai-assistant/src/main/java/assistant/ai/SuggestionDraftRepository.java`
- `java-ai-assistant/src/main/java/assistant/ai/InMemorySuggestionDraftRepository.java`
- `java-ai-assistant/src/main/java/assistant/ai/StructuredSuggestionParser.java`
- `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTypeTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftStatusTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/TaskDraftItemTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/StudyPlanDraftContentTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/SuggestionDraftViewTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/InMemorySuggestionDraftRepositoryTest.java`
- `java-ai-assistant/src/test/java/assistant/ai/StructuredSuggestionParserTest.java`

## 选择理由
v18-v19 已完成 AI 问答编排、提示词构造和真实 DeepSeek/OpenAI 兼容协议层；技术方案中下一条主线是“生成草稿、持有草稿、确认或取消、导入正式记录”。本轮先实现结构化建议解析与草稿持有基础，把 AI 原始 JSON 和正式任务/学习计划服务隔离开，并为后续 `DraftLifecycleService`、`DraftImportService` 和控制台确认流程提供稳定可测的中间表示。

本轮任务粒度限定在草稿模型、解析器和仓储基础，不实现确认导入正式记录，避免把解析、生命周期、跨服务事务和回滚一次性耦合到同一轮。

## 任务上下文
必须依据以下输入要求实现：

- 结构化建议采用“生成草稿、持有草稿、确认或取消、导入正式记录”的固定流程，AI 原始输出不得直接写入任务或学习计划仓储。
- `PromptBuilder` 对结构化场景要求模型返回单个 JSON 对象，业务类型只允许：
  - `TASK_DRAFT`：一个或多个待办任务草稿。
  - `STUDY_PLAN_DRAFT`：一个学习计划草稿，必要时附带拆解说明。
- `StructuredSuggestionParser` 使用 Jackson `JsonNode` 解析模型返回。
- 解析策略：
  - 优先解析完整 JSON 文本。
  - 如果模型返回单个 fenced JSON 代码块，可去除围栏后再解析。
  - 根节点、类型字段、必要业务字段缺失或类型错误时，返回 `OperationResult.failure(ErrorCode.AI_MALFORMED_RESPONSE, ...)`，不得创建草稿。
  - 不引入 JSON Schema 依赖，采用代码内必填字段和枚举校验。
- 解析成功后只生成受控的 `SuggestionDraft`，状态为 `CONFIRMABLE`。
- 草稿内容使用内部草稿 DTO 表达，不复用正式实体，避免绕过正式服务校验。
- 草稿状态固定为：
  - `CONFIRMABLE`：可确认或取消。
  - `CANCELLED`：用户已取消，不能再导入。
  - `IMPORTED`：已成功导入正式数据，不能重复导入。
- 取消草稿只改变草稿状态，不修改正式业务数据。
- 后续确认草稿时才委托 `DraftImportService` 调用 `TaskService` 或 `StudyPlanService`；本轮不得实现正式数据导入。

## 已有代码上下文
当前已有可复用能力：

- `assistant.ai.AiAssistantService` 已能通过 `ContextProvider`、`PromptBuilder` 和 `AiClient` 得到 AI 文本，并传播 AI 客户端错误。
- `assistant.ai.PromptBuilder` 已为结构化建议场景构造确定性提示词，要求模型返回 JSON，但尚不解析 JSON、尚不创建草稿。
- `assistant.ai.DeepSeekAiClient`、`AiHttpTransport`、`JdkAiHttpTransport`、`AiErrorMapper` 已完成真实 DeepSeek/OpenAI 兼容协议层，普通单元测试通过 fake transport 隔离网络。
- `assistant.common.EntityId` 与 `assistant.testability.IdGenerator` 可用于草稿编号。
- `assistant.common.OperationResult` 与 `assistant.common.ErrorCode` 已提供统一成功/失败返回；AI 结构异常使用 `AI_MALFORMED_RESPONSE`，草稿状态冲突使用 `STATE_CONFLICT`。
- `assistant.task.TaskService` 和 `assistant.study.StudyPlanService` 是后续正式导入必须调用的服务边界，本轮不得绕过它们写入正式仓储。

建议的公开语义需要稳定，便于后续轮次依赖：

- `SuggestionDraftType` 使用 enum，至少包含 `TASK_DRAFT`、`STUDY_PLAN_DRAFT`。
- `SuggestionDraftStatus` 使用 enum，包含 `CONFIRMABLE`、`CANCELLED`、`IMPORTED`，可提供状态判断辅助方法。
- `TaskDraftItem` 使用不可变 DTO/record，表达任务标题、描述、优先级文本或枚举、可选截止日期。字段校验必须足以拒绝空标题、空/未知优先级类型、非法日期类型；如直接使用 `TaskPriority`，解析器必须把未知文本映射为 `AI_MALFORMED_RESPONSE`。
- `StudyPlanDraftContent` 使用不可变 DTO/record，表达学习目标、开始日期、结束日期、预期投入小时、初始进度和可选拆解说明。字段校验必须足以拒绝空目标、空日期、结束日期早于开始日期、预期小时非正、进度越界。
- `SuggestionDraft` 使用领域聚合根 class，持有 `EntityId id`、`SuggestionDraftType type`、`SuggestionDraftStatus status` 以及任务草稿列表或学习计划草稿内容之一。构造时必须保证类型与内容匹配：
  - `TASK_DRAFT` 必须包含至少一个 `TaskDraftItem`，且不得包含学习计划内容。
  - `STUDY_PLAN_DRAFT` 必须包含一个 `StudyPlanDraftContent`，且不得包含任务草稿列表。
  - 对外返回任务草稿列表必须是不可修改快照。
  - `cancel()` 仅允许从 `CONFIRMABLE` 迁移到 `CANCELLED`。
  - `markImported()` 仅允许从 `CONFIRMABLE` 迁移到 `IMPORTED`。
  - 重复取消、取消后导入、导入后再次取消或导入必须以 `BusinessException(ErrorCode.STATE_CONFLICT, ...)` 表达。
- `SuggestionDraftView` 使用只读 DTO/record，从 `SuggestionDraft` 创建不可变快照；成功查询或后续服务返回不得暴露内部可变 `SuggestionDraft` 引用。
- `SuggestionDraftRepository` 提供保存、按 id 查询、查询全部、删除等最小仓储契约；`InMemorySuggestionDraftRepository` 使用 `LinkedHashMap<EntityId, SuggestionDraft>` 保持插入顺序，返回不可修改快照。
- `StructuredSuggestionParser` 负责 `String aiText` + `EntityId draftId` 到 `OperationResult<SuggestionDraft>` 的转换，或通过构造注入 `IdGenerator` 自行生成编号。选择一种接口即可，但必须让单元测试能使用固定编号并稳定断言。

建议固定 JSON 输入契约，避免实现者和测试分叉：

任务草稿 JSON：

```json
{
  "type": "TASK_DRAFT",
  "tasks": [
    {
      "title": "完成 Java 单元测试",
      "description": "覆盖任务服务状态流转",
      "priority": "HIGH",
      "dueDate": "2026-06-20"
    }
  ]
}
```

学习计划草稿 JSON：

```json
{
  "type": "STUDY_PLAN_DRAFT",
  "studyPlan": {
    "goalName": "复习软件测试",
    "startDate": "2026-06-15",
    "endDate": "2026-06-21",
    "expectedHours": 8,
    "initialProgress": 0,
    "breakdown": [
      "阅读第 1 章",
      "完成白盒测试练习"
    ]
  }
}
```

解析器必须接受上面字段名。`description`、`dueDate`、`breakdown` 可选；缺省描述按空字符串处理，缺省截止日期按 `null` 处理，缺省 `initialProgress` 按 `0` 处理，缺省 `breakdown` 按空列表处理。`dueDate`、`startDate`、`endDate` 只接受 ISO-8601 本地日期字符串。未知 `type`、大小写错误的 `type`、`tasks` 不是数组、`tasks` 为空、任务项不是对象、`studyPlan` 缺失或不是对象、日期格式非法、数值类型非法、额外业务类型均返回 `AI_MALFORMED_RESPONSE`。

fenced JSON 只支持“单个完整围栏包裹 JSON”的情况，例如：

````text
```json
{"type":"TASK_DRAFT","tasks":[{"title":"A","priority":"LOW"}]}
```
````

如果 JSON 前后还有自然语言、存在多个围栏、围栏内容不是单个 JSON 对象，均按 `AI_MALFORMED_RESPONSE` 处理。本轮不做从自然语言中猜测或截取 JSON 的宽松解析。

## RETRY 说明（仅 RETRY 时）
无。
