# 测试报告（v28）

## 概述

已根据 `detail_v28.md` 的行为契约和 `test_review_v28_r1.md` 的审查意见，为 `assistant.app.ConsoleApplication` 的 AI 草稿循环子菜单补充并核对单元测试。测试集中放置在项目既有约定文件：

- `/root/exp_SWAT/java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

本轮测试基于公开控制台输入输出与 `DraftLifecycleService` 公开接口交互编写，不依赖实现细节，不访问真实网络、环境变量或系统时间。

## 覆盖内容

| 测试方法 | 覆盖行为 |
|----------|----------|
| `listCommandsDisplayEachCoreEntry()` | 主菜单命令 `8` 进入 `AI 草稿菜单`，不再是一次性列表入口。 |
| `listCommandsDisplayEmptyStateWithoutDemoData()` | 空数据场景下通过 AI 草稿子菜单 `l` 展示 `暂无 AI 草稿`。 |
| `draftMenuListsAllDraftsWithoutTruncation()` | `l/list` 调用 `listDrafts()` 并展示全部 11 条草稿，验证未截断前 10 条。 |
| `draftMenuDisplaysListFailureWithoutListContent()` | `listDrafts()` 失败时只展示固定失败格式，不继续打印 `AI 草稿列表`、空列表或列表条目。 |
| `draftMenuViewsTaskDraftDetail()` | `v/view` 成功展示任务草稿详情、状态、标题、优先级、截止日期、描述，并断言缺失学习计划区块稳定输出 `学习计划草稿: 无`。 |
| `draftMenuViewsStudyPlanDraftDetail()` | `v/view` 成功展示学习计划草稿目标、日期、预期小时、初始进度和拆解条目，并断言缺失任务区块稳定输出 `任务草稿: 无`。 |
| `draftMenuDisplaysUnsetDueDateForTaskDraft()` | 任务草稿缺失截止日期时稳定展示 `截止日期: 未设置`。 |
| `draftMenuConfirmsDraftAndDisplaysImportedStatus()` | `c/confirm` 调用确认服务，并展示返回视图中的 `IMPORTED` 状态。 |
| `draftMenuCancelsDraftAndDisplaysCancelledStatus()` | `x/cancel` 调用取消服务，并展示返回视图中的 `CANCELLED` 状态。 |
| `draftMenuDisplaysNotFoundAndStateConflictFailuresWithoutDetail()` | `NOT_FOUND` 与 `STATE_CONFLICT` 失败只展示错误，不展示草稿详情。 |
| `draftMenuDisplaysImportFailureWithoutOldDetail()` | 导入失败只展示失败信息，不展示旧详情或旧状态。 |
| `draftMenuRejectsInvalidIdBeforeCallingDraftLifecycleService()` | 空值、非数字、小数、非正数、超出 `long` 范围的 id 均被控制台层拒绝，且不调用草稿生命周期服务。 |
| `draftMenuHandlesUnknownBlankHelpBackAndEof()` | 未知命令、空命令、帮助、返回主菜单和进入子菜单后 EOF 的稳定行为。 |
| `draftMenuAcceptsLongCommandAliases()` | `list/view/confirm/cancel/back` 长命令别名分发到对应服务方法。 |

## 测试辅助

新增或复用的测试辅助包括：

- `taskDraftView(...)`
- `taskDraftViewWithoutDueDate(...)`
- `studyPlanDraftView(...)`
- `taskDraftItem(...)`
- `studyPlanDraftContent(...)`
- `withDraftLifecycleService(...)`

这些辅助仅构造不可变测试视图和替换 mock `DraftLifecycleService`，其余应用服务沿用既有 `ApplicationServices` 装配。

## 审查修订

已采纳 `test_review_v28_r1.md` 的两项审查要求：

- 在任务草稿详情测试中补充 `学习计划草稿: 无` 断言。
- 在学习计划草稿详情测试中补充 `任务草稿: 无` 断言。
- 新增 `DraftLifecycleService.listDrafts()` 失败路径测试，验证失败输出复用 `printResult(...)` 格式且不会打印列表标题、空列表提示或草稿条目。

## 执行说明

按 Verifier 角色约束，本步骤只负责编写和核对测试，不执行测试命令。实现报告中记录的测试执行由编码阶段完成。
