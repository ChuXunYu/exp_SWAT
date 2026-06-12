# 测试报告（v25）

## 测试文件

- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

## 覆盖范围

- 学习计划主菜单入口进入子菜单，并支持返回主菜单。
- 学习计划子菜单列表、新增、查看、筛选、修改详情、更新进度、删除、帮助、未知命令、空命令和 EOF 退出。
- 学习计划服务失败时展示错误码和错误消息。
- 长命令别名与状态筛选大小写不敏感。
- 创建学习计划时空初始进度使用默认 `0%`。
- 显式初始进度 `100` 时展示 `COMPLETED`。
- 无效 id、无效日期、日期倒置、筛选单边日期、无效小时数、无效进度、无效状态的校验失败路径。
- 校验失败路径保持在学习计划子菜单，并避免写入或调用学习计划服务。
- 学习计划新增后汇总中的本周学习计划数联动更新。

## 本轮新增/强化

- 保留 `studyPlanMenuCreatesDefaultInitialProgressWhenBlank()`，直接验证新增时初始进度空输入走默认进度契约。
- 根据 `test_review_v25_r1.md` 审查意见，拆分原 `studyPlanMenuRejectsEndBeforeStartWithoutWriteOperation()`：
  - `studyPlanMenuRejectsUpdateEndBeforeStartWithoutWriteOperation()` 独立覆盖修改详情日期倒置时不写入，断言列表仍保留原目标且不包含待修改目标。
  - `studyPlanMenuRejectsAddEndBeforeStartWithoutWriteOperation()` 独立覆盖新增学习计划日期倒置时不写入，使用该用例中唯一的列表输出断言不包含待新增目标，避免截取到倒置新增之前的列表。

## 验证

- 已执行：`mvn -q -Dtest=ConsoleApplicationTest test`
- 结果：通过。
- 已执行：`mvn -q test`
- 结果：`Tests run: 896, Failures: 0, Errors: 0, Skipped: 0`
