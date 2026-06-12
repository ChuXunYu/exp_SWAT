# 测试审查报告（v28 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `/root/exp_SWAT/java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` — 草稿详情缺失区块的稳定输出未被断言。详细设计要求草稿详情同时打印任务草稿区和学习计划草稿区，某一区块不存在时输出稳定 `无` 文本；当前 `draftMenuViewsTaskDraftDetail()` 只断言任务字段，未断言 `学习计划草稿: 无`，`draftMenuViewsStudyPlanDraftDetail()` 只断言学习计划字段，未断言 `任务草稿: 无`。如果实现遗漏缺失区块提示，现有测试仍会通过。
- **[一般]** `/root/exp_SWAT/java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` — `DraftLifecycleService.listDrafts()` 失败路径未覆盖。详细设计要求所有服务失败都复用 `printResult(...)` 输出 `失败: {ErrorCode} - {message}`，列表入口是本轮新增入口之一；当前仅覆盖列表成功和空列表，未验证列表服务失败时不会打印 `AI 草稿列表` 或旧/空列表内容。若 `listDrafts()` 失败后仍继续打印列表，现有测试无法发现。

## 修改要求（仅 REJECTED 时）

- 在 `ConsoleApplicationTest` 的草稿详情测试中补充缺失区块断言：任务草稿详情应断言 `学习计划草稿: 无`；学习计划草稿详情应断言 `任务草稿: 无`。如要覆盖拆解空列表，也应新增一个空 breakdown 的学习计划视图断言 `拆解: 无`。
- 新增 `DraftLifecycleService.listDrafts()` 失败用例：mock 返回 `OperationResult.failure(...)`，输入 `8\nl\nb\nq\n`，断言输出固定失败格式，并断言失败段不包含 `AI 草稿列表` 或任何列表条目。
