# 测试报告（v9）

## 概述

已按详细设计 v9、实现报告 v9 和测试审查 v9 r1 的 REJECTED 意见修订单元测试。

测试继续基于公开行为契约编写，不断言私有辅助方法或内部实现细节；本轮重点补强 `ScheduleConflictPolicy` 不按 `EntityId` 特殊放行冲突，以及 `ScheduleItem.updateDetails(...)` 成功修改时间范围后派生行为必须读取当前 `timeRange` 的契约。

## 测试文件

| 文件路径 | 覆盖内容 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleStatusTest.java` | 覆盖状态枚举声明顺序、中文展示文本、语义判断方法、`valueOf(...)` 和稳定常量名。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleItemTest.java` | 覆盖日程实体构造、工厂方法、字段规范化、空参数和空白名称拒绝、修改失败不变性、成功修改后的派生状态和日期覆盖、动态状态边界、跨日期 `coversDate(...)` 行为和空日期拒绝。 |
| `java-ai-assistant/src/test/java/assistant/schedule/ScheduleConflictPolicyTest.java` | 覆盖日程冲突、包含关系冲突、跨日期冲突、相同 `EntityId` 仍按时间重叠判冲突、对称性、首尾相接不冲突、集合扫描、第一个冲突返回、空集合无冲突、空参数和空集合元素拒绝。 |

## 审查反馈处理

| 审查意见 | 处理结果 |
|---------|----------|
| `ScheduleConflictPolicy` 未覆盖相同 `EntityId` 的日程仍应按 `DateTimeRange.overlaps(...)` 判冲突。 | 已采纳。新增 `conflictsWhenSchedulesHaveSameEntityIdAndOverlappingRanges()`，验证两个不同 `ScheduleItem` 使用相同 `EntityId` 且时间重叠时 `conflicts(...)` 返回 `true`。 |
| `hasConflict(...)` / `findFirstConflict(...)` 未覆盖候选与集合中相同编号既有日程重叠时不能被特殊放行。 | 已采纳。新增 `collectionMethodsTreatSameEntityIdAsConflictWhenRangesOverlap()`，验证集合扫描仍返回冲突，并且 `findFirstConflict(...)` 返回相同编号的冲突日程。 |
| `ScheduleItem.updateDetails(...)` 后未验证 `statusAt(...)` 和 `coversDate(...)` 基于新 `timeRange` 动态计算。 | 已采纳。新增 `updateDetailsMakesStatusAndDateCoverageUseNewTimeRange()`，使用不同日期的新时间范围，验证新开始前为 `UPCOMING`、新开始边界为 `ONGOING`、新结束边界为 `EXPIRED`，并验证覆盖新日期且不再覆盖仅属于旧范围的日期。 |

## 本轮补充

| 测试方法 | 覆盖契约 |
|---------|----------|
| `ScheduleConflictPolicyTest.conflictsWhenSchedulesHaveSameEntityIdAndOverlappingRanges()` | `conflicts(...)` 只以时间范围重叠为准，不因相同 `EntityId` 排除候选自身或特殊放行。 |
| `ScheduleConflictPolicyTest.collectionMethodsTreatSameEntityIdAsConflictWhenRangesOverlap()` | `hasConflict(...)` 和 `findFirstConflict(...)` 对相同编号的既有日程仍按时间重叠判定冲突，并按迭代顺序返回冲突项。 |
| `ScheduleItemTest.updateDetailsMakesStatusAndDateCoverageUseNewTimeRange()` | `updateDetails(...)` 成功后，`statusAt(...)` 和 `coversDate(...)` 使用更新后的当前时间范围，而不是构造时旧范围或缓存值。 |

## 已覆盖设计用例

- `ScheduleStatusTest` 覆盖详细设计规划的全部状态枚举用例，包括固定顺序、展示文本、语义方法、枚举解析、未知名称拒绝和稳定常量名。
- `ScheduleItemTest` 覆盖详细设计规划的全部实体用例，包括构造字段保存、工厂方法、文本规范化、可选字段空值处理、内部空白保留、必填字段拒绝、空白名称拒绝、时间端点读取、成功修改、修改规范化、修改空可选字段、失败不变性、成功修改后的派生行为、动态状态左闭右开边界、跨日期自然日覆盖和空日期拒绝。
- `ScheduleConflictPolicyTest` 覆盖详细设计规划的全部冲突策略用例，包括内部重叠、候选包含既有、既有包含候选、跨日期重叠、相同编号不特殊放行、对称性、首尾相接、完全分离、不同日期不重叠、单项冲突空参数拒绝、集合任一冲突、空集合和无冲突集合、第一个冲突迭代顺序、无冲突返回空 Optional、集合方法空参数拒绝和集合内空元素拒绝。

## 验证说明

根据 verifier 指令，本环节只负责编写和修订测试，不负责运行测试；未执行 `mvn test`。
