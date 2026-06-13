# 代码审查报告（v1 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/README.md` — Features 中的 Schedules 条目声称支持 `complete` 和 `cancel` 日程操作，但当前 `ConsoleApplication` 的日程菜单只提供 list/add/view/filter/update/delete/back/help，`ScheduleService` 也没有完成或取消日程的行为接口。该 README 声明与实际交付能力不一致，会误导新用户。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/README.md` 的 Features / Schedules 条目应按当前代码真实能力修正，移除 `complete` 和 `cancel` 日程操作声明，或改为当前支持的状态筛选/时间派生状态说明。不得通过新增未经设计授权的业务代码来补齐该文档声明。
