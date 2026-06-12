# 任务指令（v23）

## 动作
NEW

## 任务描述
扩展 `assistant.app.ConsoleApplication` 的任务待办入口，使主菜单命令 `2` 进入可循环的任务子菜单，支持通过控制台完成任务新增、查看、列表、筛选、修改、删除、标记完成和撤销完成，并补充对应 JUnit 测试。

预期主要文件路径：

- `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java`
- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

必要时可在 `java-ai-assistant/src/main/java/assistant/app/` 下新增小型包可见输入解析辅助类型或方法，但不得新增独立 CLI 框架，不得修改 `TaskService`、`TaskRepository`、`TaskItem`、`TaskView`、`TaskQuery` 的公开契约。

任务子菜单固定命令：

| 命令 | 行为 |
|------|------|
| `l` / `list` | 展示全部任务，复用现有任务列表展示格式。 |
| `a` / `add` | 依次读取标题、描述、优先级、截止日期，调用 `TaskService.createTask(...)`。 |
| `v` / `view` | 读取任务 id，调用 `TaskService.getTask(...)` 展示单条任务详情。 |
| `f` / `filter` | 依次读取可选状态、可选优先级、可选截止日期，构造 `TaskQuery.of(...)` 并调用 `TaskService.listTasks(query)`。 |
| `u` / `update` | 读取任务 id、标题、描述、优先级、截止日期，调用 `TaskService.updateTask(...)`。 |
| `c` / `complete` | 读取任务 id，调用 `TaskService.markTaskCompleted(...)`。 |
| `r` / `reopen` | 读取任务 id，调用 `TaskService.reopenTask(...)`。 |
| `d` / `delete` | 读取任务 id，调用 `TaskService.deleteTask(...)`。 |
| `b` / `back` | 返回主菜单。 |
| `h` / `help` | 展示任务子菜单帮助。 |

输入解析契约：

- 任务 id 输入使用正整数，解析失败或小于等于 0 时不调用服务，输出包含 `VALIDATION_ERROR` 和清晰提示，随后留在任务子菜单。
- 截止日期输入格式固定为 ISO `yyyy-MM-dd`，解析失败时不调用服务，输出包含 `VALIDATION_ERROR` 和清晰提示。
- 优先级输入接受 `LOW`、`MEDIUM`、`HIGH`，大小写不敏感，前后空白忽略；非法值不调用服务，输出包含 `VALIDATION_ERROR` 和清晰提示。
- 筛选状态输入接受空值、`TODO`、`COMPLETED`，大小写不敏感，前后空白忽略；空值表示不按状态筛选，非法值不调用服务。
- 筛选优先级和截止日期均允许空值，空值表示不按该字段筛选。
- 新增和修改中的标题、描述等原始文本不要在控制台层自行做业务裁剪或校验，除输入解析必须完成的 enum/date/id 外，其余交给 `TaskService` 返回稳定 `OperationResult`。
- EOF 出现在任务子菜单任意读取步骤时，应与主菜单 EOF 一致，正常结束程序，不抛出异常。

展示契约：

- 进入任务子菜单时先展示任务子菜单命令说明。
- 新增、查看、修改、完成、撤销完成成功时展示单条任务详情，至少包含 id、标题、优先级、状态、截止日期和描述。
- 删除成功时展示“操作成功”或等价成功提示。
- 列表和筛选无结果时展示“暂无任务”。
- 服务失败统一复用当前 `printResult(...)` 风格，输出错误码和消息。
- 任务子菜单返回主菜单后，主菜单仍可继续执行汇总、AI 问答和退出等既有命令。

测试要求：

- 在 `ConsoleApplicationTest` 中补充基于 `StringReader` / `StringWriter` 的控制台交互测试，不读取真实环境变量、不访问真实网络、不依赖真实 API Key。
- 覆盖新增任务后列表可见，并可通过汇总命令看到今日任务数量随新增变化。
- 覆盖查看、修改、删除成功路径，以及删除后列表或查看反映当前状态。
- 覆盖标记完成、重复完成返回 `STATE_CONFLICT`、撤销完成成功路径。
- 覆盖按状态、优先级、截止日期筛选的组合路径，至少断言匹配项出现且非匹配项不出现在筛选输出段中。
- 覆盖 id、日期、优先级、状态解析失败时不调用对应服务写操作；可以通过输出和后续列表状态断言。
- 覆盖任务子菜单未知命令、帮助、返回主菜单和 EOF 退出。
- 保留并调整既有 `listCommandsDisplayEachCoreEntry()` 等测试，使主菜单命令 `2` 的行为变化后测试仍断言任务入口可用。

## 选择理由
8 个核心功能的领域、服务、汇总、AI 问答、DeepSeek 客户端、AI 草稿导入和基础控制台装配已经完成并通过 v22 验证。当前剩余主要风险在交互验收：控制台入口还不能对本地模块执行真实输入型操作。任务待办模块依赖最少、状态链路清晰，先完成它的完整控制台闭环，可以验证输入解析、服务调用、错误展示、子菜单循环和主菜单返回模式，为后续日程、学习计划、收支、笔记交互细化提供可复用样式。

## 任务上下文
需求要求程序提供清晰菜单或操作入口，使用户能选择不同功能；每个操作应有明确提示，输入成功后反馈操作结果，输入失败时说明字段为空、格式错误、范围非法、记录不存在或状态冲突等原因。任务待办核心功能要求新增、查看、修改、删除待办任务，字段至少包含标题、描述、优先级、截止日期和完成状态，支持标记完成/未完成，并按状态或优先级筛选，同时处理标题为空、日期格式错误、优先级非法、删除不存在任务、重复标记完成、撤销完成等情况。

本轮只实现任务待办控制台交互，不实现日程、学习计划、收支、笔记的完整增删改查子菜单，不实现持久化，不修改 AI 请求或草稿导入服务。控制台层仍不得直接访问仓储集合、不得构造或修改 `TaskItem` 实体、不得承载业务判断。

## 已有代码上下文
`assistant.app.ConsoleApplication` 当前包含主菜单、汇总展示、各模块列表展示、AI 问答和 AI 草稿列表。主菜单命令 `2` 当前直接调用 `showTasks()`，仅展示 `TaskService.listTasks()` 的结果。`printResult(OperationResult<T>)` 已能统一展示失败错误码和消息，成功空载荷会输出“操作成功”。`readLine(String prompt)` 已处理 `IOException` 并在读取失败时结束程序。

`assistant.task.TaskService` 已提供完整任务服务闭环：创建、查看、列表、按 `TaskQuery` 筛选、修改、删除、标记完成、撤销完成，并把业务校验、未找到和状态冲突映射为 `OperationResult`。`TaskPriority`、`TaskStatus`、`TaskQuery`、`TaskView`、`EntityId` 均已存在并有单元测试。`ConsoleApplicationTest` 已使用 `ApplicationFactory.create(Map.of(), FixedTimeProvider)`、`StringReader` 和 `StringWriter` 验证菜单、汇总、列表入口、AI 未配置和错误展示，可在同一测试类中扩展任务子菜单测试。
