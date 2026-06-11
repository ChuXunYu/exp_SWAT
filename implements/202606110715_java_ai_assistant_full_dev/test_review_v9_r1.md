# 测试审查报告（v9 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/schedule/ScheduleConflictPolicyTest.java` — 未覆盖 `ScheduleConflictPolicy` 不按 `EntityId` 排除候选自身的契约。详细设计明确要求 `conflicts(...)` 只以 `DateTimeRange.overlaps(...)` 为准，且“不按 `EntityId` 排除候选自身”；当前测试只使用不同编号日程。如果实现错误地在编号相同或同一对象时直接返回不冲突，现有测试不会失败。
- **[一般]** `java-ai-assistant/src/test/java/assistant/schedule/ScheduleItemTest.java` — 未验证 `updateDetails(...)` 成功修改时间范围后，`statusAt(...)` 和 `coversDate(...)` 基于新的 `timeRange` 动态计算。详细设计要求状态不持久化、每次基于当前 `timeRange` 和传入时间推导，`coversDate(...)` 也委托当前 `timeRange`。当前测试只断言 getter 返回新范围，无法防止实现缓存旧状态、旧日期覆盖结果，或派生方法继续读取旧时间范围。

## 修改要求（仅 REJECTED 时）

- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleConflictPolicyTest.java`：新增用例覆盖相同 `EntityId` 的两个不同 `ScheduleItem` 在时间重叠时 `conflicts(...)` 返回 `true`，并覆盖候选与集合中相同编号的既有日程重叠时 `hasConflict(...)` / `findFirstConflict(...)` 不被特殊放行。期望测试能杀死“按编号相同跳过冲突”的错误实现。
- `java-ai-assistant/src/test/java/assistant/schedule/ScheduleItemTest.java`：新增或增强成功 `updateDetails(...)` 后的派生行为断言，使用与原范围不同日期和时间的新 `DateTimeRange`，验证 `statusAt(...)` 对新开始/结束边界返回正确状态，`coversDate(...)` 覆盖新日期且不再覆盖仅属于旧范围的日期。期望测试能证明派生行为读取的是更新后的当前时间范围。
