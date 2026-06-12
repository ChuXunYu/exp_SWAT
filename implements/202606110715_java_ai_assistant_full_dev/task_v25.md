# 任务指令（v25）

## 动作
NEW

## 任务描述
扩展 `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 的学习计划菜单交互，并补充 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 覆盖；必要时可在 `assistant.app` 包内复用或抽取小型输入解析辅助方法，但不得修改 `StudyPlanService`、`StudyPlanQuery`、`StudyPlanView`、`StudyPlanStatus` 的公开契约。

主菜单命令 `4` 不再只执行一次 `showStudyPlans()`，而应进入可循环学习计划子菜单。子菜单至少支持：

- `l` / `list`：列表全部学习计划。
- `a` / `add`：新增学习计划。
- `v` / `view`：按 id 查看学习计划详情。
- `f` / `filter`：按可选状态和可选周期筛选。
- `u` / `update`：修改目标名称、开始日期、截止日期和预期投入小时数。
- `p` / `progress`：更新进度。
- `d` / `delete`：删除学习计划。
- `b` / `back`：返回主菜单。
- `h` / `help`：显示学习计划子菜单帮助。

字段读取顺序固定为：

- 新增学习计划：目标名称、开始日期、截止日期、预期投入小时数、初始进度。初始进度允许用户输入空行，空行表示使用 `StudyPlanService.createStudyPlan(goalName, startDate, endDate, expectedHours)` 默认 0；非空时调用带 `initialProgress` 的重载。
- 查看学习计划：学习计划 id。
- 筛选学习计划：状态、开始日期、截止日期。状态为空表示不按状态筛选；开始日期和截止日期必须同时为空或同时合法填写，同时为空表示不按周期筛选；只填一个日期应由控制台层拒绝为 `VALIDATION_ERROR` 且不调用服务。
- 修改学习计划：学习计划 id、目标名称、开始日期、截止日期、预期投入小时数。
- 更新进度：学习计划 id、进度。
- 删除学习计划：学习计划 id。

输入解析与展示要求：

- 学习计划 id 必须为正整数；非法时输出 `失败: VALIDATION_ERROR - 学习计划 id 必须是正整数`，不调用服务。
- 日期使用 `yyyy-MM-dd`；格式非法时输出 `失败: VALIDATION_ERROR - 学习计划日期格式必须是 yyyy-MM-dd`，不调用服务。
- 预期投入小时数必须能解析为正整数；非法时输出 `失败: VALIDATION_ERROR - 预期投入小时数必须是正整数`，不调用服务。
- 进度必须能解析为 0 到 100 的整数；非整数或越界时输出 `失败: VALIDATION_ERROR - 进度必须是 0 到 100 的整数`，不调用服务。
- 状态大小写不敏感匹配 `NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`OVERDUE_INCOMPLETE`；非法时输出 `失败: VALIDATION_ERROR - 状态必须是 NOT_STARTED、IN_PROGRESS、COMPLETED 或 OVERDUE_INCOMPLETE`，不调用服务。
- 筛选周期结束日期早于开始日期时输出 `失败: VALIDATION_ERROR - 学习计划结束日期不能早于开始日期`，不调用服务。
- 服务失败统一复用既有 `printResult(...)` 格式展示错误码和消息；成功删除复用空载荷成功输出 `操作成功`。
- 列表输出标题为 `学习计划列表`，筛选输出标题为 `学习计划筛选结果`；空列表输出 `暂无学习计划`；非空列表逐行输出全部学习计划，不限制 10 条，格式至少包含 `id | goalName | status | 进度 {progress}% | {startDate} ~ {endDate} | 预期 {expectedHours} 小时`。
- 详情输出标题为 `学习计划详情`，至少包含 `ID`、`目标`、`状态`、`进度`、`开始日期`、`截止日期`、`预期投入小时数`。
- EOF 或读取失败时按既有控制台约定结束程序；空命令输出 `请输入学习计划命令。`；未知命令输出 `未知学习计划命令，请输入 h 查看帮助。` 后展示帮助。

## 选择理由
任务待办和日程提醒两个控制台入口已经形成完整 CRUD、筛选、验证失败、服务失败、帮助、返回主菜单和 EOF 交互模式；学习计划当前仍是一次性列表入口，不能满足需求中“学习计划管理可输入、可演示、可产生明确结果”的验收口径。学习计划还直接影响汇总统计和 AI 草稿导入结果，补齐其控制台闭环后，可通过手工演示和自动化交互测试验证计划新增、进度更新、状态筛选、日期边界和汇总同步。

## 任务上下文
需求要求学习计划管理支持创建学习目标、记录开始日期、截止日期、预期投入时间和完成进度，支持更新进度、判断是否逾期、统计已完成与未完成计划数量，并明确处理进度为 `0`、`100`、小于 `0`、大于 `100`、截止日期早于开始日期等情况。交互层应提供清晰菜单或操作入口，输入成功后反馈结果，输入失败时说明失败原因。单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

本轮只实现控制台学习计划入口，不修改学习计划领域、仓储、服务、汇总或 AI 草稿导入契约。控制台层只负责菜单、输入解析、服务调用和结果展示；业务校验仍以 `StudyPlanService`、`StudyPlanQuery`、`DateRange`、`StudyPlanAnalysisService` 为准。

## 已有代码上下文
`ConsoleApplication` 当前主菜单命令 `4` 调用 `showStudyPlans()`，该方法只调用 `services.studyPlanService().listStudyPlans()`，输出 `学习计划列表`，空列表输出 `暂无学习计划`，非空时只展示前 10 条。命令 `2` 和 `3` 已分别接入任务、日程子菜单，可复用其命令循环、`ParsedInput<T>`、`readLine(...)`、`printValidationError(...)`、`printResult(...)`、id/日期解析和列表/详情展示模式。

`StudyPlanService` 已提供学习计划创建、查看、列表、组合筛选、修改详情、更新进度、删除、完成/未完成统计接口，并通过 `OperationResult` 返回成功载荷或稳定错误分类。`StudyPlanQuery.of(StudyPlanStatus status, DateRange period)` 可表达状态与周期组合筛选；`DateRange` 构造时开始日期晚于结束日期会抛出 `IllegalArgumentException`；`StudyPlanView` 暴露 `id()`、`goalName()`、`startDate()`、`endDate()`、`expectedHours()`、`progress()`、`status()`。

`ConsoleApplicationTest` 已用 `StringReader` / `StringWriter` 覆盖主菜单、任务子菜单、日程子菜单、服务失败、输入验证失败、长命令别名、大小写不敏感筛选、空命令、未知命令、帮助、返回主菜单和 EOF 场景。本轮应在同一测试类中补充学习计划子菜单测试，至少覆盖：

- 主菜单 `4` 进入学习计划子菜单并可返回主菜单。
- 空数据列表输出 `暂无学习计划`。
- 新增学习计划后列表和详情可见，汇总中的 `本周学习计划数` 能反映新增计划。
- 查看、修改详情、更新进度、删除和删除后查看 `NOT_FOUND`。
- 按状态和周期筛选，空筛选字段列出全部学习计划。
- 初始进度空行默认 0，显式 `100` 可创建完成计划。
- 非法 id、非法日期、只填一个筛选日期、结束日期早于开始日期、非法小时数、非法进度、非法状态均由控制台层输出指定 `VALIDATION_ERROR`，且不产生对应写入或不调用 mock 服务。
- 服务失败路径：mock `StudyPlanService.listStudyPlans()` 或其他学习计划服务方法返回失败时，控制台输出错误码和消息。
- 长命令别名、状态大小写不敏感、未知命令、帮助、空命令、返回主菜单和 EOF。
