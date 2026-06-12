# 测试报告（v12）

## 概述

已先阅读 `test_review_v12_r7.md`，并针对其中 `REJECTED` 原因修订测试。修订范围限定在：

- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`

未修改生产代码，未创建新版本号文件，未回退他人改动。

## 本轮修订内容

### 补强更新进度成功路径的持久化断言

调整以下用例：

- `updateStudyPlanProgressAcceptsZeroProgressAndRefreshesViewStatus()`
- `updateStudyPlanProgressAcceptsCompleteProgressAndRefreshesViewStatus()`

修订后两条成功路径除校验返回的 `StudyPlanView.progress()` 和 `StudyPlanView.status()` 外，还会重新通过 `repository.findById(new EntityId(100))` 读取仓储最终状态，并分别断言持久化后的进度为 `0` 与 `100`。

这样如果服务实现退化为只修改仓储返回的实体副本并返回视图，但遗漏 `repository.save(plan)`，测试会失败。

### 让时间替身独立区分 `today()` 与 `now()`

重做 `StudyPlanServiceTest` 内部的 `CountingTimeProvider`：

- `today()` 只增加 `todayCalls` 并按测试配置返回 `LocalDate`
- `now()` 只增加 `nowCalls`，返回一个与测试配置日期可区分的固定 `LocalDateTime`
- `now()` 不再调用 `today()`

随后对所有显式依赖“服务只读取一次当前日期”的用例补充 `assertEquals(0, timeProvider.nowCalls())`，覆盖创建、查看、列表、组合筛选、修改详情、更新进度和统计路径。

这样如果实现改为通过 `timeProvider.now().toLocalDate()` 获取日期，相关用例会因为 `todayCalls` 不符合预期或 `nowCalls` 非零而失败，能够真正锁住设计要求的 `timeProvider.today()` 使用约束。

## 测试文件

- `java-ai-assistant/src/test/java/assistant/study/StudyPlanServiceTest.java`

## 执行结果

已执行：

```bash
mvn -q -Dtest='assistant.study.StudyPlanServiceTest' test
mvn -q -Dtest='assistant.study.*Test' test
```

结果：通过。
