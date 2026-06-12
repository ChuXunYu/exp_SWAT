# 测试审查报告（v25 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java:670` — `studyPlanMenuRejectsEndBeforeStartWithoutWriteOperation()` 未能有效验证“新增学习计划日期倒置时不写入”。`secondList` 从 `output.indexOf("目标名称:")` 截取，该位置是第一次新增“保留学习”的提示，不是倒置新增之后的提示；随后 `between(..., "学习计划列表", "主菜单")` 实际可能截到倒置新增之前的列表。因此即使倒置新增后错误写入 `倒置学习`，该断言也可能仍然通过。

## 修改要求

- 修正 `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java` 中 `studyPlanMenuRejectsEndBeforeStartWithoutWriteOperation()` 的输出截取方式，确保用于断言 `倒置学习` 不存在的片段来自倒置新增之后执行的 `l` 列表输出。建议拆成两个独立测试，分别覆盖修改详情日期倒置不写入、新增日期倒置不写入；或使用唯一锚点定位第二次新增之后的列表区间。
