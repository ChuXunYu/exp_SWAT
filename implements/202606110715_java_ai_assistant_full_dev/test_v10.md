# 测试报告（v10）

## 概述
基于 `detail_v10.md` 的行为契约，为 `assistant.schedule` 日程应用服务闭环补充单元测试。测试覆盖 `ScheduleQuery`、`ScheduleView`、`InMemoryScheduleRepository` 和 `ScheduleService` 的公开行为，不验证实现细节，不依赖真实系统时间、网络、外部文件或执行顺序。

## 新增测试文件
| 文件路径 | 覆盖内容 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleQueryTest.java` | 覆盖全量查询、按自然日覆盖筛选、跨日中间日、结束日零点排除、动态状态筛选、组合 AND 筛选、通配组件、筛选标记和空参数拒绝。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleViewTest.java` | 覆盖 record 构造校验、文本保留、时间端点一致性、实体字段投影、三种状态快照、状态语义方法、空参数拒绝和实体后续修改不影响既有视图。 |
| `java-ai-assistant/src/test/java/assistant/schedule/InMemoryScheduleRepositoryTest.java` | 覆盖保存/查找、同编号覆盖、覆盖后保持原插入位置、全量快照顺序和不可修改性、条件筛选、删除语义和空参数快速失败。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleServiceTest.java` | 覆盖服务依赖校验、创建/查看/列表/筛选/按日期/修改/删除、字段校验错误分类、冲突拒绝、首尾相接允许、自身冲突排除、跨日和零点边界、注入时间使用、不可修改快照和返回 DTO 不暴露实体。 |

## 关键覆盖点
- 查询与列表状态计算均使用测试注入的固定 `LocalDateTime`，新增 `CountingTimeProvider` 验证列表类方法单次调用内只读取一次 `now()`。
- 日期筛选覆盖普通日期、跨日期日程，以及结束时间恰好位于查询日 `00:00` 的排除边界。
- 创建和修改冲突测试覆盖非空时间重叠拒绝，以及首尾相接时间范围允许。
- 修改测试覆盖字段非法和冲突失败时既有日程保持不变，以及修改自身原时间范围时不被自身误判为冲突。
- 快照测试覆盖仓储返回不可修改列表、服务返回不可修改 `ScheduleView` 列表，以及后续实体更新/新增不改变既有视图和列表快照。
- 使用 `CopyingScheduleRepository` 覆盖仓储返回分离副本时 `updateSchedule(...)` 仍调用 `save(...)` 持久化修改。

## 编译验证
在 `java-ai-assistant` 目录执行：

```bash
mvn -DskipTests test
```

结果：构建成功，主代码和测试代码编译通过；Surefire 测试执行阶段按参数跳过。

## 说明
- 按 verifier 指令，本轮职责为编写测试；未执行完整 `mvn test`。
- 未修改编码 agent 新增的生产源码文件。
