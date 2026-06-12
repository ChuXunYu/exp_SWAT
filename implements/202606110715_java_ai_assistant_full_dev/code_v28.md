# 实现报告（v28）

## 概述
实现了 `ConsoleApplication` 的 AI 草稿循环子菜单，将主菜单命令 `8` 从一次性列表改为进入子菜单；新增草稿列表、查看详情、确认导入、取消、帮助、返回、EOF 退出、草稿 id 校验及任务/学习计划草稿详情输出。

同步更新 `ConsoleApplicationTest`，覆盖 AI 草稿子菜单的列表不截断、任务详情、学习计划详情、无截止日期、确认/取消终态展示、失败不展示旧详情、非法 id 不调用服务、帮助/返回/EOF 和长命令别名。

## 文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | java-ai-assistant/src/main/java/assistant/app/ConsoleApplication.java | 主菜单 `8` 接入 AI 草稿子菜单，新增草稿命令分发、id 解析、列表和详情输出。 |
| 修改 | java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java | 更新主菜单草稿入口断言，新增 AI 草稿子菜单行为测试与测试视图构造辅助。 |

## 编译验证
已执行：

```bash
mvn -q -Dtest=assistant.app.ConsoleApplicationTest test
mvn -q test
```

结果：均通过。

## 设计偏差说明
无偏差。
