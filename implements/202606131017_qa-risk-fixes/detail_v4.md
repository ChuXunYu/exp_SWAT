# 详细设计（v4）

## 概述
本轮修复中文控制台暴露英文枚举的问题。设计范围限定在 `ConsoleApplication` 的输入解析、提示文案、列表/详情/摘要/草稿展示，以及对应控制台测试和测试文档。

目标行为：
- 任务优先级、任务状态、日程状态、学习计划状态、收支类型支持中文别名输入，同时保留现有英文枚举输入兼容。
- 控制台列表、详情、摘要紧急任务明细、AI 草稿列表/详情和非法输入错误提示优先显示中文枚举含义。
- 不修改服务层、领域枚举、查询对象、仓储或业务规则。
- 不引入国际化框架；在 `ConsoleApplication` 内使用小型私有 helper 集中映射。

本轮不改变 AI 结构化草稿 JSON 解析规则。AI 返回的结构化内容仍使用已有内部英文枚举值，由服务层和解析器维持类型安全；本轮只改善控制台对用户展示和用户手工输入体验。

## 文件规划
| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` | 修改 | 新增中文枚举解析和显示 helper，替换控制台枚举 prompt、错误提示、列表/详情/摘要/草稿输出 |
| `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` | 修改 | 更新英文枚举输出断言为中文显示，补充中文输入、英文兼容和非法输入中文错误提示测试 |
| `java-ai-assistant/docs/test-plan.md` | 修改 | 更新控制台/等价类测试计划，说明 CLI 中文枚举输入和中文显示覆盖 |
| `java-ai-assistant/docs/test-cases.md` | 修改 | 新增或更新控制台枚举相关用例，引用真实存在的 `ConsoleApplicationTest` 方法名 |
| `java-ai-assistant/README.md` | 可选修改 | 如 README 的控制台示例或枚举输入说明仍展示英文枚举，更新为中文优先并保留英文兼容说明 |

## 类型定义

### ConsoleApplication
**形态**：`final class`
**包路径**：`assistant.app`
**职责**：控制台交互入口，负责将用户输入转换为服务层类型，并将服务层视图转换为面向用户的中文文本。

**公开接口保持不变**：
```java
public ConsoleApplication(ApplicationServices services, Reader input, Writer output)

public void run()
```

**新增私有解析方法签名**：
```java
private static String normalizeEnumInput(String rawValue)

private TaskPriority parseTaskPriority(String rawValue)

private TaskStatus parseTaskStatus(String rawValue)

private ScheduleStatus parseScheduleStatus(String rawValue)

private StudyPlanStatus parseStudyPlanStatus(String rawValue)

private TransactionType parseTransactionType(String rawValue)
```

其中 `parseTaskPriority`、`parseTaskStatus`、`parseScheduleStatus`、`parseStudyPlanStatus`、`parseTransactionType` 为现有方法的行为扩展，签名保持不变。

**新增私有显示方法签名**：
```java
private static String displayTaskPriority(TaskPriority priority)

private static String displayTaskStatus(TaskStatus status)

private static String displayScheduleStatus(ScheduleStatus status)

private static String displayStudyPlanStatus(StudyPlanStatus status)

private static String displayTransactionType(TransactionType type)

private static String displayDraftType(SuggestionDraftType type)

private static String displayDraftStatus(SuggestionDraftStatus status)
```

**新增 import**：
```java
import assistant.ai.SuggestionDraftStatus;
import assistant.ai.SuggestionDraftType;
```

如 `SuggestionDraftView.type()` 和 `SuggestionDraftView.status()` 已能通过返回类型推断调用 helper，则只新增上述 import；不新增其它外部依赖。

**中文显示映射**：
| 内部类型 | 内部值 | 中文显示 |
|---------|--------|----------|
| `TaskPriority` | `LOW` | `低` |
| `TaskPriority` | `MEDIUM` | `中` |
| `TaskPriority` | `HIGH` | `高` |
| `TaskStatus` | `TODO` | `未完成` |
| `TaskStatus` | `COMPLETED` | `已完成` |
| `ScheduleStatus` | `UPCOMING` | `即将开始` |
| `ScheduleStatus` | `ONGOING` | `进行中` |
| `ScheduleStatus` | `EXPIRED` | `已过期` |
| `StudyPlanStatus` | `NOT_STARTED` | `未开始` |
| `StudyPlanStatus` | `IN_PROGRESS` | `进行中` |
| `StudyPlanStatus` | `COMPLETED` | `已完成` |
| `StudyPlanStatus` | `OVERDUE_INCOMPLETE` | `逾期未完成` |
| `TransactionType` | `INCOME` | `收入` |
| `TransactionType` | `EXPENSE` | `支出` |
| `SuggestionDraftType` | `TASK_DRAFT` | `任务草稿` |
| `SuggestionDraftType` | `STUDY_PLAN_DRAFT` | `学习计划草稿` |
| `SuggestionDraftStatus` | `CONFIRMABLE` | `待确认` |
| `SuggestionDraftStatus` | `CANCELLED` | `已取消` |
| `SuggestionDraftStatus` | `IMPORTED` | `已导入` |

**中文输入别名映射**：
| 解析方法 | 支持中文输入 | 兼容英文输入 |
|---------|--------------|--------------|
| `parseTaskPriority` | `低`、`中`、`高` | `LOW`、`MEDIUM`、`HIGH`，大小写不敏感，允许首尾空白 |
| `parseTaskStatus` | `未完成`、`已完成` | `TODO`、`COMPLETED`，大小写不敏感，允许首尾空白 |
| `parseScheduleStatus` | `即将开始`、`进行中`、`已过期` | `UPCOMING`、`ONGOING`、`EXPIRED`，大小写不敏感，允许首尾空白 |
| `parseStudyPlanStatus` | `未开始`、`进行中`、`已完成`、`逾期未完成` | `NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`OVERDUE_INCOMPLETE`，大小写不敏感，允许首尾空白 |
| `parseTransactionType` | `收入`、`支出` | `INCOME`、`EXPENSE`，大小写不敏感，允许首尾空白 |

**normalizeEnumInput 行为**：
- 参数为 `rawValue`。
- 返回 `rawValue.strip()`。
- 英文匹配继续在各解析方法内对 strip 后结果调用 `toUpperCase(Locale.ROOT)`。
- 不做全角/半角、拼音、数字编号或同义词扩展；本轮只覆盖任务文件列明的中文别名与英文兼容。

**parseTaskPriority 行为**：
- `normalizeEnumInput(rawValue)` 结果为 `低` 返回 `TaskPriority.LOW`。
- 结果为 `中` 返回 `TaskPriority.MEDIUM`。
- 结果为 `高` 返回 `TaskPriority.HIGH`。
- 否则按现有 `TaskPriority.valueOf(normalized.toUpperCase(Locale.ROOT))` 解析英文。
- 解析失败时调用 `printValidationError("优先级必须是 低、中、高（也可输入 LOW、MEDIUM、HIGH）")` 并返回 `null`。

**parseTaskStatus 行为**：
- `未完成` 返回 `TaskStatus.TODO`。
- `已完成` 返回 `TaskStatus.COMPLETED`。
- 否则按英文枚举解析。
- 解析失败时调用 `printValidationError("状态必须是 未完成 或 已完成（也可输入 TODO 或 COMPLETED）")` 并返回 `null`。

**parseScheduleStatus 行为**：
- `即将开始` 返回 `ScheduleStatus.UPCOMING`。
- `进行中` 返回 `ScheduleStatus.ONGOING`。
- `已过期` 返回 `ScheduleStatus.EXPIRED`。
- 否则按英文枚举解析。
- 解析失败时调用 `printValidationError("日程状态必须是 即将开始、进行中 或 已过期（也可输入 UPCOMING、ONGOING、EXPIRED）")` 并返回 `null`。

**parseStudyPlanStatus 行为**：
- `未开始` 返回 `StudyPlanStatus.NOT_STARTED`。
- `进行中` 返回 `StudyPlanStatus.IN_PROGRESS`。
- `已完成` 返回 `StudyPlanStatus.COMPLETED`。
- `逾期未完成` 返回 `StudyPlanStatus.OVERDUE_INCOMPLETE`。
- 否则按英文枚举解析。
- 解析失败时调用 `printValidationError("学习计划状态必须是 未开始、进行中、已完成 或 逾期未完成（也可输入 NOT_STARTED、IN_PROGRESS、COMPLETED、OVERDUE_INCOMPLETE）")` 并返回 `null`。

**parseTransactionType 行为**：
- `收入` 返回 `TransactionType.INCOME`。
- `支出` 返回 `TransactionType.EXPENSE`。
- 否则按英文枚举解析。
- 解析失败时调用 `printValidationError("收支类型必须是 收入 或 支出（也可输入 INCOME 或 EXPENSE）")` 并返回 `null`。

**prompt 文案调整**：
```java
readRequiredTaskPriority("优先级(低/中/高，可输入 LOW/MEDIUM/HIGH): ")
readOptionalTaskStatus("状态(未完成/已完成，可输入 TODO/COMPLETED，可空): ")
readOptionalTaskPriority("优先级(低/中/高，可输入 LOW/MEDIUM/HIGH，可空): ")
readOptionalScheduleStatus("状态(即将开始/进行中/已过期，可输入 UPCOMING/ONGOING/EXPIRED，可空): ")
readOptionalStudyPlanStatus("状态(未开始/进行中/已完成/逾期未完成，可输入 NOT_STARTED/IN_PROGRESS/COMPLETED/OVERDUE_INCOMPLETE，可空): ")
readRequiredTransactionType("类型(收入/支出，可输入 INCOME/EXPENSE): ")
readOptionalTransactionType("类型(收入/支出，可输入 INCOME/EXPENSE，可空): ")
```

所有调用 `readRequiredTaskPriority` 的新增和修改任务流程都使用同一中文优先 prompt。所有调用 `readRequiredTransactionType` 的新增收入、支出和修改收支流程都使用同一中文优先 prompt。

**输出替换点**：
- `printTaskTitles(...)`：将 `task.priority()` 替换为 `displayTaskPriority(task.priority())`，将 `task.status()` 替换为 `displayTaskStatus(task.status())`。
- `printTaskList(...)`：列表行输出中文优先级和中文状态。
- `printTaskDetail(...)`：详情的 `优先级:` 和 `状态:` 输出中文。
- `printScheduleList(...)`：列表行输出中文日程状态。
- `printScheduleDetail(...)`：详情的 `状态:` 输出中文日程状态。
- `printStudyPlanList(...)`：列表行输出中文学习计划状态。
- `printStudyPlanDetail(...)`：详情的 `状态:` 输出中文学习计划状态。
- `printTransactionList(...)`：列表行输出中文收支类型。
- `printTransactionDetail(...)`：详情的 `类型:` 输出中文收支类型。
- `printDraftList(...)`：列表行输出中文草稿类型和中文草稿状态。
- `printDraftDetail(...)`：详情的 `类型:` 和 `状态:` 输出中文草稿类型和中文草稿状态。
- `printTaskDraftItem(...)`：任务草稿项的 `优先级:` 输出中文优先级。

**类型关系**：
- 继续消费现有 `TaskPriority`、`TaskStatus`、`ScheduleStatus`、`StudyPlanStatus`、`TransactionType`、`SuggestionDraftType`、`SuggestionDraftStatus`。
- 不新增 enum、record、service、repository 或 mapper 类。
- 不改变 `TaskQuery`、`ScheduleQuery`、`StudyPlanQuery`、`TransactionQuery` 构造方式；解析结果仍传入原有强类型枚举。

### ConsoleApplicationTest
**形态**：`final class`
**包路径**：`assistant.app`
**职责**：覆盖控制台交互输出和输入解析行为。

**现有断言更新范围**：
- 原断言中用户可见输出含 `HIGH | TODO`、`MEDIUM | TODO`、`状态: TODO`、`INCOME`、`类型: INCOME`、草稿 `TASK_DRAFT`/`CONFIRMABLE` 等内容的地方，按新中文显示更新。
- 只更新控制台输出断言；测试内部直接构造领域对象时仍使用英文 enum 常量。
- 如测试输入用于证明英文兼容，可继续输入英文枚举值。

**新增或调整测试方法建议签名**：
```java
@Test
void taskMenuAcceptsChinesePriorityAndStatusFiltersAndDisplaysChineseEnums()

@Test
void taskMenuKeepsEnglishPriorityAndStatusInputCompatible()

@Test
void taskMenuRejectsInvalidEnumInputWithChineseOptions()

@Test
void scheduleMenuAcceptsChineseStatusFilterAndDisplaysChineseStatus()

@Test
void studyPlanMenuAcceptsChineseStatusFilterAndDisplaysChineseStatus()

@Test
void financeMenuAcceptsChineseTypeFilterAndDisplaysChineseType()

@Test
void financeMenuKeepsEnglishTypeInputCompatible()

@Test
void draftMenuDisplaysChineseDraftTypeStatusAndTaskPriority()
```

如已有测试方法能自然承载上述行为，可以在原方法内扩展断言；但至少要有独立方法覆盖中文输入、英文兼容和非法输入中文错误提示三类风险。

**测试输入契约**：
- 中文任务新增路径输入 `高`，创建后列表或详情显示 `高 | 未完成`。
- 中文任务筛选路径输入 `未完成` 和 `高`，只返回匹配任务。
- 英文任务新增或筛选路径继续输入 `LOW`、`MEDIUM`、`HIGH`、`TODO`、`COMPLETED`，结果成功且显示中文。
- 非法任务优先级输入触发 `失败: VALIDATION_ERROR - 优先级必须是 低、中、高（也可输入 LOW、MEDIUM、HIGH）`。
- 非法任务状态输入触发 `失败: VALIDATION_ERROR - 状态必须是 未完成 或 已完成（也可输入 TODO 或 COMPLETED）`。
- 中文日程状态筛选输入 `即将开始`、`进行中` 或 `已过期`，结果成功且列表/详情显示中文状态。
- 中文学习计划状态筛选输入 `未开始`、`进行中`、`已完成` 或 `逾期未完成`，结果成功且列表/详情显示中文状态。
- 中文收支类型新增、修改、筛选或统计输入 `收入`、`支出`，结果成功且列表/详情显示中文类型。
- 英文收支类型输入 `INCOME`、`EXPENSE` 继续成功且显示中文。
- 非法收支类型输入触发 `失败: VALIDATION_ERROR - 收支类型必须是 收入 或 支出（也可输入 INCOME 或 EXPENSE）`。
- 草稿查看输出 `类型: 任务草稿` 或 `类型: 学习计划草稿`、`状态: 待确认/已取消/已导入`，任务草稿优先级显示 `低/中/高`。

**文档一致性测试关系**：
- 若 `DocumentationDeliveryTest` 对 `test-cases.md` 中测试类/方法名有固定引用检查，新增用例必须引用真实存在的方法名。
- 不要求新增 `DocumentationDeliveryTest` 断言，除非当前文档一致性测试对新增 CONSOLE 用例编号或章节有硬性清单。

## 错误处理
- 所有非法枚举输入继续通过 `printValidationError(...)` 输出，错误码前缀保持 `失败: VALIDATION_ERROR - `。
- 新错误消息中文可选值优先，括号中保留英文别名，避免用户失去旧输入路径。
- `readRequired...` 和 `readOptional...` 方法的 `ParsedInput.invalid()`、`ParsedInput.empty()`、`ParsedInput.eof()` 语义不变。
- 英文枚举解析失败仍捕获 `IllegalArgumentException`；中文匹配未命中再尝试英文解析。
- `display...` 方法只接收非空 enum；当前服务视图和值对象已保证枚举字段非空。本轮不新增 null 容错或自定义异常。
- 新增 `displayDraftType`、`displayDraftStatus` 只用于控制台展示，不影响草稿生命周期状态冲突、确认、取消或导入失败处理。

## 行为契约
- 用户输入中文别名和对应英文枚举值应得到完全相同的服务层请求参数。
- 英文兼容大小写不敏感，保持现有 `toUpperCase(Locale.ROOT)` 行为；例如 `high`、`High`、`HIGH` 都解析为 `TaskPriority.HIGH`。
- 中文别名首尾空白可被忽略；中文值本身必须精确匹配本设计列出的别名。
- 控制台所有用户可见枚举值优先显示中文含义，不显示内部英文枚举名，除非是 prompt 或错误提示中明确说明“也可输入”的兼容别名。
- 摘要紧急任务明细延续 v3 的输出结构和任务源顺序，只将优先级、状态替换为中文显示。
- 任务草稿缺失 dueDate 的展示、AI 草稿生成、确认、取消、导入流程不变；只调整草稿类型、草稿状态和任务草稿优先级的展示文本。
- 服务层类型安全枚举和业务规则不变；本轮不新增领域层测试。
- 文档必须与真实测试方法名一致，不记录未执行的测试数量或虚构覆盖率。

## 依赖关系
- 依赖现有领域枚举：
  - `assistant.task.TaskPriority`
  - `assistant.task.TaskStatus`
  - `assistant.schedule.ScheduleStatus`
  - `assistant.study.StudyPlanStatus`
  - `assistant.finance.TransactionType`
  - `assistant.ai.SuggestionDraftType`
  - `assistant.ai.SuggestionDraftStatus`
- 暴露给后续任务的公开接口无变化。
- 后续编码任务只需修改 `assistant.app.ConsoleApplication`、`assistant.app.ConsoleApplicationTest` 和受影响文档。
- 验证阶段至少执行：
  - `cd /root/exp_SWAT/java-ai-assistant && mvn clean test`
  - 包含 `DocumentationDeliveryTest` 的文档一致性测试，若已由 `mvn clean test` 覆盖则无需单独重复，但测试报告需说明。
