# 任务指令（v24）

## 动作
NEW

## 任务描述
扩展 `java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java` 的日程提醒控制台入口：主菜单命令 `3` 不再只执行一次日程列表，而是进入可循环日程子菜单，支持列表、新增、查看、按日期/状态筛选、修改、删除、帮助、返回主菜单和 EOF 稳定退出。

同步补充 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 的交互测试，覆盖日程子菜单成功路径、验证失败、冲突拒绝、筛选、服务失败、未知命令、帮助、返回主菜单和 EOF 场景。

本轮不得修改 `ScheduleService`、`ScheduleQuery`、`ScheduleView` 的公开契约；必要时可在 `assistant.app` 包内抽取或复用小型输入解析辅助方法。

## 选择理由
v23 已完成任务待办控制台完整交互入口，验证了子菜单循环、字段读取、错误展示、返回主菜单和 EOF 处理模式。当前日程提醒模块的领域、查询、仓储和服务闭环已经完成，但控制台层仍只有一次性列表展示，无法通过程序入口演示新增、修改、删除、按日期/状态筛选、动态状态和时间冲突拒绝。日程提醒是 8 个核心功能之一，下一步应按同等粒度补齐日程菜单，继续提高“每个核心功能可演示、可输入、可产生明确结果”的验收覆盖。

## 任务上下文
需求要求日程提醒管理支持记录课程、会议、考试、个人安排等日程事项；每条日程包含名称、日期时间、地点和备注；程序应支持按日期查看日程，并能识别同一时间段的重复或冲突安排；查询时显示即将开始、进行中或已过期等状态。输入失败时应说明字段为空、格式错误、范围非法、记录不存在或日程冲突等原因。

技术方案要求控制台层只处理菜单、输入解析和展示，不直接访问集合或 HTTP；业务逻辑下沉到服务、领域对象和值对象。日程实体使用 `DateTimeRange` 表达左闭右开时间区间，冲突由 `ScheduleConflictPolicy` 处理，日程状态由 `ScheduleService` 基于可注入 `TimeProvider` 动态推导。

## 已有代码上下文
`ConsoleApplication` 当前主菜单命令 `3` 调用 `showSchedules()`，该方法只调用 `services.scheduleService().listSchedules()`，输出 `日程列表`，空结果输出 `暂无日程`，非空时最多展示 10 条，格式包含 id、名称、状态、开始结束时间和地点。

`ScheduleService` 已提供以下公开接口：
- `OperationResult<ScheduleView> createSchedule(String name, DateTimeRange timeRange, String location, String note)`
- `OperationResult<ScheduleView> getSchedule(EntityId id)`
- `OperationResult<List<ScheduleView>> listSchedules()`
- `OperationResult<List<ScheduleView>> listSchedules(ScheduleQuery query)`
- `OperationResult<List<ScheduleView>> listSchedulesByDate(LocalDate date)`
- `OperationResult<ScheduleView> updateSchedule(EntityId id, String name, DateTimeRange timeRange, String location, String note)`
- `OperationResult<Void> deleteSchedule(EntityId id)`

`ScheduleQuery` 支持 `ScheduleQuery.of(LocalDate date, ScheduleStatus status)`，空日期或空状态表示对应条件不筛选。`ScheduleStatus` 枚举为 `UPCOMING`、`ONGOING`、`EXPIRED`。`ScheduleView` 提供 `id()`、`name()`、`startDateTime()`、`endDateTime()`、`location()`、`note()`、`status()`。

`ConsoleApplication` 已有任务菜单可作为交互风格参考：`runTaskMenu()`、`printTaskMenu()`、`dispatchTaskCommand(...)`、`printTaskList(...)`、`printTaskDetail(...)`、`printValidationError(...)`、`ParsedInput<T>`，以及任务 id、枚举和日期解析的 EOF/INVALID/EMPTY/VALUE 处理模式。日程菜单应沿用一致的输出和错误风格，但日程字段解析需覆盖 `LocalDateTime` 和可选状态/日期筛选。

本轮建议固定日程子菜单命令：
- `l/list`：调用 `ScheduleService.listSchedules()`，输出 `日程列表`。
- `a/add`：依次读取名称、开始时间、结束时间、地点、备注；开始和结束时间使用 ISO 本地日期时间格式 `yyyy-MM-ddTHH:mm`，构造 `DateTimeRange`，调用 `createSchedule(...)`。
- `v/view`：读取日程 id，调用 `getSchedule(...)`。
- `f/filter`：依次读取可选日期 `yyyy-MM-dd` 和可选状态 `UPCOMING/ONGOING/EXPIRED`，构造 `ScheduleQuery.of(dateOrNull, statusOrNull)`，调用 `listSchedules(query)`，输出 `日程筛选结果`。
- `u/update`：依次读取日程 id、名称、开始时间、结束时间、地点、备注，调用 `updateSchedule(...)`。
- `d/delete`：读取日程 id，调用 `deleteSchedule(...)`。
- `b/back`：返回主菜单。
- `h/help`：展示日程菜单帮助。

输入解析要求：
- 日程 id 必须是正整数，非法时输出 `失败: VALIDATION_ERROR - 日程 id 必须是正整数`，不调用服务。
- 必填开始/结束时间为空或格式错误时输出 `失败: VALIDATION_ERROR - 日程时间格式必须是 yyyy-MM-ddTHH:mm`，不调用服务。
- 结束时间不晚于开始时间时，应在控制台层构造 `DateTimeRange` 时捕获异常，输出服务风格的 `VALIDATION_ERROR`，不让运行时异常泄漏；消息可使用 `结束时间必须晚于开始时间` 或 `DateTimeRange` 的明确异常消息，但测试需固定断言。
- 筛选日期为空表示不按日期筛选；非空格式错误输出 `失败: VALIDATION_ERROR - 日程日期格式必须是 yyyy-MM-dd`。
- 筛选状态为空表示不按状态筛选；非空大小写不敏感匹配 `UPCOMING`、`ONGOING`、`EXPIRED`，非法时输出 `失败: VALIDATION_ERROR - 状态必须是 UPCOMING、ONGOING 或 EXPIRED`。
- 名称、地点、备注作为原始字段传给服务，不在控制台层做业务校验；服务失败由 `printResult(...)` 输出错误码和消息。
- 任一字段读取 EOF 时设置 `running = false`，中止当前操作，不创建半成品日程。

输出展示要求：
- 新增、查看、修改成功后输出 `日程详情`，至少包含 `ID: {id}`、`名称: {name}`、`状态: {status}`、`开始时间: {startDateTime}`、`结束时间: {endDateTime}`、`地点: {location}`、`备注: {note}`。
- 列表和筛选空结果输出 `暂无日程`；非空逐行输出全部日程，不再限制为 10 条，格式保持可读并包含 id、名称、状态、开始结束时间和地点。
- 删除成功复用 `OperationResult<Void>` 空载荷展示，输出 `操作成功`。
- 日程创建或修改与已有日程时间重叠时，应展示服务返回的 `SCHEDULE_CONFLICT`，并保证原有数据不被错误修改。

测试至少覆盖：
- 主菜单命令 `3` 进入日程子菜单，并能 `b/back` 返回主菜单继续执行汇总或退出。
- 空数据列表输出 `暂无日程`。
- 新增日程后列表可见，查看详情展示名称、时间、地点、备注和动态状态。
- 修改日程后详情显示新内容，删除后查看返回 `NOT_FOUND` 或列表不再包含该日程。
- 新增相互重叠日程返回 `SCHEDULE_CONFLICT`，首尾相接的日程可成功创建。
- 按日期和状态组合筛选时，匹配项出现且非匹配项不出现在筛选输出段中；筛选字段全空时列出全部日程。
- 非法 id、非法日期、非法日期时间、非法状态、结束时间早于或等于开始时间均输出 `VALIDATION_ERROR`，且不写入或不修改已有日程。
- 未知日程命令展示提示和帮助，空日程命令提示后仍留在日程菜单。
- 日程命令读取 EOF 和新增字段中途 EOF 均正常结束，不抛异常，不创建半成品日程。
- 通过 mock `ScheduleService` 覆盖列表服务失败展示错误码和消息。
