# 任务指令（v10）

## 动作
NEW

## 任务描述
实现日程提醒模块的查询、只读视图、仓储与应用服务闭环。

预期新增生产文件：
- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleQuery.java`
- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleView.java`
- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleRepository.java`
- `java-ai-assistant/src/main/java/assistant/schedule/InMemoryScheduleRepository.java`
- `java-ai-assistant/src/main/java/assistant/schedule/ScheduleService.java`

预期新增测试文件：
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleQueryTest.java`
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleViewTest.java`
- `java-ai-assistant/src/test/java/assistant/schedule/InMemoryScheduleRepositoryTest.java`
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleServiceTest.java`

本轮应提供以下能力：
- `ScheduleQuery` 表达日程筛选条件，至少支持按 `LocalDate` 查询覆盖该自然日的日程，并可按 `ScheduleStatus` 组合筛选；状态匹配必须基于调用方传入的 `LocalDateTime` 或服务注入的 `TimeProvider.now()`，不得读取真实系统时间。
- `ScheduleView` 作为只读 DTO/record，包含 `EntityId id`、`String name`、`DateTimeRange timeRange`、`LocalDateTime startDateTime`、`LocalDateTime endDateTime`、`String location`、`String note`、`ScheduleStatus status`，并提供 `from(ScheduleItem item, LocalDateTime currentDateTime)` 工厂方法。
- `ScheduleRepository` 提供 `save`、`findById`、`findAll`、`findBy(ScheduleQuery query, LocalDateTime currentDateTime)` 和 `deleteById` 等日程仓储契约。
- `InMemoryScheduleRepository` 使用 `LinkedHashMap<EntityId, ScheduleItem>` 保存数据，保持插入顺序，返回不可修改快照；空参数应快速失败。
- `ScheduleService` 依赖 `ScheduleRepository`、`IdGenerator`、`TimeProvider` 和 `ScheduleConflictPolicy`，提供创建、查看、列表、按条件筛选、按日期查询、修改和删除日程的服务入口。
- `ScheduleService` 成功载荷只能返回 `ScheduleView` 或不可修改的 `List<ScheduleView>`，不得向外暴露内部可变 `ScheduleItem` 引用。
- 创建和修改日程时必须调用 `ScheduleConflictPolicy` 识别与既有日程的非空时间重叠；冲突时返回 `OperationResult.failure(ErrorCode.SCHEDULE_CONFLICT, ...)` 并保持仓储状态不变。
- 修改日程时必须排除同一 `EntityId` 的当前日程后再判断冲突，否则修改自身不变时间范围会被误判为冲突；但与其他同编号污染数据是否冲突不需要在本轮额外处理。
- 删除、查看和修改不存在日程返回 `NOT_FOUND`；空编号、空查询、非法字段、空名称或空时间范围等输入返回 `VALIDATION_ERROR`；所有可预期业务错误通过 `OperationResult` 表达。
- 按日期查询必须使用 `DateTimeRange.coversDate(...)` 语义，覆盖跨日期日程，并排除结束时间正好位于查询日零点且没有实际覆盖该日的日程。

## 选择理由
v9 已完成日程领域实体、动态状态枚举和冲突策略，日程提醒核心功能仍缺少应用服务层闭环。补齐查询、只读视图、仓储和服务后，日程模块即可支持需求中的新增、查看、修改、删除、按日期查看、冲突识别和动态状态展示，也能为后续数据汇总、AI 本地上下文和控制台菜单提供稳定 API。

本轮继续沿用 v8-v9 的实现策略：底层领域对象保护字段不变量，仓储只负责内存保存和快照，服务层负责编号生成、时间注入、错误转换、冲突拒绝和只读结果投影。这样可以避免控制台层、汇总层或后续 AI 模块绕过服务直接修改日程实体。

## 任务上下文
需求文档要求日程提醒管理支持记录课程、会议、考试和个人安排；每条日程包含名称、日期时间、地点和备注；程序支持按日期查看日程，并能识别同一时间段的重复或冲突安排；查询时显示即将开始、进行中或已过期等状态；单元测试必须通过可控当前时间避免测试结果随真实日期变化。

技术方案要求：
- 日程实体持有名称、`DateTimeRange`、地点和备注。
- 创建日程时结束时间必须晚于开始时间，该规则已经由 `DateTimeRange` 承担。
- 冲突判断由 `ScheduleConflictPolicy` 处理，同一天存在非空时间重叠则拒绝保存并返回 `SCHEDULE_CONFLICT`，首尾相接不冲突。
- 日程状态不持久化，由 `ScheduleService` 基于 `TimeProvider` 动态推导。
- 按日期查询返回该日期开始或覆盖该日期的日程快照。
- 实体集合对外返回不可修改快照，避免外部模块绕过服务修改内部状态。

本轮普通单元测试不得依赖真实当前时间、网络、API Key 或外部文件。

## 已有代码上下文
已完成的相关类型：
- `assistant.common.EntityId`：正整数编号值对象。
- `assistant.common.DateTimeRange`：左闭右开日期时间区间，提供 `overlaps(...)` 和 `coversDate(...)`。
- `assistant.common.OperationResult<T>`、`ErrorCode`、`BusinessException`：应用服务返回成功/失败和稳定错误分类。
- `assistant.testability.IdGenerator`、`IncrementalIdGenerator`：可替换编号生成。
- `assistant.testability.TimeProvider`、`FixedTimeProvider`、`SystemTimeProvider`：可替换当前日期和当前时间。
- `assistant.schedule.ScheduleStatus`：`UPCOMING`、`ONGOING`、`EXPIRED` 动态展示状态。
- `assistant.schedule.ScheduleItem`：日程领域实体，支持创建、字段规范化、`updateDetails(...)`、`statusAt(...)` 和 `coversDate(...)`。
- `assistant.schedule.ScheduleConflictPolicy`：无状态冲突策略，按 `DateTimeRange.overlaps(...)` 判断冲突，首尾相接不冲突。

可参考的既有模式：
- `assistant.task.TaskQuery`：用 record 表达可组合筛选条件。
- `assistant.task.TaskView`：服务层返回的只读 DTO，避免暴露可变实体。
- `assistant.task.TaskRepository` 与 `InMemoryTaskRepository`：使用 `LinkedHashMap`、`Optional` 和不可修改快照。
- `assistant.task.TaskService`：在服务边界生成编号、捕获实体校验异常并转换为 `OperationResult.failure(ErrorCode.VALIDATION_ERROR, ...)`，不存在记录返回 `NOT_FOUND`，成功查询返回 `TaskView` 或 `List<TaskView>`。

测试应覆盖：
- `ScheduleQuery` 的空条件、日期筛选、状态筛选、日期+状态组合筛选、跨日期覆盖和空参数拒绝。
- `ScheduleView` 的字段投影、动态状态计算、空参数拒绝和不会暴露可变实体引用的边界。
- `InMemoryScheduleRepository` 的保存覆盖、按编号查找、插入顺序、不可修改快照、查询筛选、删除和空参数拒绝。
- `ScheduleService` 的创建成功、查看成功、列表成功、按日期查询成功、修改成功、删除成功、冲突创建拒绝、冲突修改拒绝、首尾相接允许、修改自身排除冲突、非法输入错误分类、不存在记录错误分类、删除后查询为空、只读结果列表不可修改、返回 `ScheduleView` 后外部无法改变仓储内部状态。
