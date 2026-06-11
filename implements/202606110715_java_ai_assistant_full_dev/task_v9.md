# 任务指令（v9）

## 动作
NEW

## 任务描述
新增日程提醒模块的核心领域实体、动态状态枚举和冲突判断策略，预期文件路径包括：

- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleStatus.java`
- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleItem.java`
- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleConflictPolicy.java`
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleStatusTest.java`
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleItemTest.java`
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleConflictPolicyTest.java`

本轮仅实现日程领域模型和冲突策略基础，不实现 `ScheduleService`、日程仓储、日程查询 DTO、控制台菜单、汇总统计或 AI 日程建议。

## 选择理由
v8 已完成任务待办模块的查询、只读快照、仓储和服务闭环，并通过 `mvn verify`。项目当前已具备跨业务通用编号、时间抽象、日期时间区间和统一错误分类，可以开始实现 8 个核心功能中的日程提醒管理。

日程服务后续依赖稳定的 `ScheduleItem`、`ScheduleStatus` 和 `ScheduleConflictPolicy`：实体集中保护名称、时间范围、地点和备注等字段不变量；状态枚举承载即将开始、进行中、已过期的展示语义；冲突策略独立覆盖时间重叠、首尾相接和跨日期覆盖等白盒测试重点。先完成这三个底层类型，可让下一轮日程仓储与服务直接复用，不在服务层重复散落冲突判断。

## 任务上下文
需求与技术方案中的日程提醒管理要求：

- 用户可以记录课程、会议、考试、个人安排等日程事项。
- 每条日程应包含名称、日期时间、地点和备注。
- 程序应支持按日期查看日程，并能识别同一时间段的重复或冲突安排。
- 不要求真正调用系统通知，但应能在查询时显示即将开始、正常进行或已过期等状态。
- 创建日程时结束时间必须晚于开始时间。
- 冲突判断由 `ScheduleConflictPolicy` 处理，同一天存在非空时间重叠则拒绝保存并返回 `SCHEDULE_CONFLICT`；首尾相接不冲突。
- 日程状态不持久化，应基于 `TimeProvider` 或传入的当前时间动态推导：早于开始为即将开始，大于等于开始且小于结束为进行中，大于等于结束为已过期。
- 普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

本轮建议的公开行为边界：

- `ScheduleStatus` 使用 enum，至少包含 `UPCOMING`、`ONGOING`、`EXPIRED`，并提供必要的语义方法或展示文本方法。
- `ScheduleItem` 持有 `EntityId id`、`String name`、`DateTimeRange timeRange`、`String location`、`String note`。
- `ScheduleItem` 构造或工厂方法必须拒绝空编号、空时间范围、空名称和空白名称；名称使用 `strip()` 后保存；地点和备注允许 `null` 并规范化为空字符串，非空时可 `strip()`。
- `ScheduleItem` 应支持修改基础信息，但修改失败时不得改变既有字段。
- `ScheduleItem` 应提供 `statusAt(LocalDateTime currentDateTime)` 或等价方法，禁止读取真实系统时间；当前时间等于开始时间时为 `ONGOING`，等于结束时间时为 `EXPIRED`。
- `ScheduleItem` 应提供 `coversDate(LocalDate date)` 或等价方法，复用 `DateTimeRange.coversDate(...)` 支撑后续按日期查询。
- `ScheduleConflictPolicy` 应集中判断两个日程或一个候选日程与既有集合是否冲突；冲突语义必须复用左闭右开区间重叠规则，首尾相接返回不冲突，任一入参为 `null` 时抛出 `NullPointerException`。

## 已有代码上下文
当前项目根目录 `/root/exp_SWAT` 下已有独立 Maven 工程 `java-ai-assistant/`。

已完成的通用基础：

- `assistant.common.EntityId`：正整数编号值对象。
- `assistant.common.DateTimeRange`：左闭右开日期时间区间，提供 `contains(LocalDateTime)`、`overlaps(DateTimeRange)`、`coversDate(LocalDate)`；结束时间必须晚于开始时间；首尾相接不重叠。
- `assistant.common.ErrorCode`：已包含 `VALIDATION_ERROR`、`NOT_FOUND`、`STATE_CONFLICT`、`SCHEDULE_CONFLICT` 等错误分类。
- `assistant.common.BusinessException`：可用于领域层表达可预期业务错误。
- `assistant.testability.TimeProvider`、`FixedTimeProvider`、`SystemTimeProvider`：用于后续服务层可控获取当前日期和时间。

已完成的任务模块可作为风格参考：

- `assistant.task.TaskItem` 使用普通 class 封装可变实体状态，构造器集中校验字段，修改方法先计算规范化值再赋值以保持失败不变性。
- `assistant.task.TaskService` 在应用服务边界将 `NullPointerException`、`IllegalArgumentException`、`BusinessException` 转换为 `OperationResult`；本轮日程服务尚不实现，但领域类型应保持同样清晰的不变量边界。
- 现有测试均使用 JUnit Jupiter，测试数据使用固定编号、固定日期时间，不依赖真实当前时间。
