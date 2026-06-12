# 测试报告（v22）

## 概述

本轮根据 `test_review_v22_r2.md` 的审查意见，修订 `assistant.app` 包入口层测试，补齐 `Main` 的演示数据开关契约。测试仍基于公开启动行为和可注入纯函数路径编写，不访问真实网络；涉及全局 `System.in`、`System.out` 与 JVM 系统属性的用例均在 `finally` 中恢复，避免污染其他测试。

## 修订文件

| 文件路径 | 修订内容 |
|---------|----------|
| `java-ai-assistant/src/main/java/assistant/app/Main.java` | 将演示数据开关解析提取为包可见纯函数 `isDemoDataEnabled(String systemPropertyValue, String environmentValue)`；私有无参方法仍读取 `System.getProperty(...)` 和 `System.getenv(...)`，对外启动行为不变。 |
| `java-ai-assistant/src/test/java/assistant/app/MainTest.java` | 新增入口层测试，覆盖缺失配置默认启用、系统属性为 `false` / `0` / `no` 时禁用、其他值启用、系统属性优先于环境变量，以及 `Main.main(...)` 在 EOF 输入下能完成启动并退出。 |

## 行为覆盖

### Main

- `ASSISTANT_DEMO_DATA` 缺失时默认启用演示数据。
- 系统属性值为 `false`、`0`、`no` 时禁用演示数据。
- 禁用值解析会执行 `strip()` 与大小写归一化，覆盖带空白和大写的输入。
- 系统属性为其他值时启用演示数据。
- 当系统属性缺失时使用环境变量值。
- 当系统属性与环境变量同时存在时，系统属性优先于环境变量。
- `Main.main(...)` 使用 EOF 输入可立即启动控制台并退出，不阻塞测试；测试过程中临时设置 `ASSISTANT_DEMO_DATA=0` 避免加载演示数据，并在 `finally` 中恢复系统属性、标准输入和标准输出。

### ApplicationFactory / ApplicationServices

- `create(Map, TimeProvider)` 能构造任务、日程、学习计划、收支、笔记、汇总、AI 问答、AI 草稿生命周期服务和时间提供者。
- 显式空配置不会读取真实环境，AI 问答返回 `AI_NOT_CONFIGURED`。
- 注入的 `AiConfigurationLoader` 会被用于显式配置装配。
- `create()` 生产入口会读取合并配置并传入 `AiConfigurationLoader`。
- JVM 系统属性会参与生产配置，且同名系统属性覆盖环境变量来源。
- `configurationValues`、`timeProvider`、`aiConfigurationLoader` 为空时抛出带约定参数名的 `NullPointerException`。
- `ApplicationServices` record 对所有组件执行空引用防御，并返回约定参数名。

### DemoDataFactory

- `load(ApplicationServices)` 通过服务写入任务、日程、学习计划、收支和笔记，且写入后汇总可见。
- 演示数据日期基于固定 `TimeProvider`，任务、日程和收支日期均可在固定时间下稳定断言。
- `services == null` 时抛出 `NullPointerException("services")`。
- 当底层服务写入失败时，`load(...)` 转换为 `IllegalStateException`，消息包含 `failed to load demo data` 与失败错误码 `VALIDATION_ERROR`。

### ConsoleApplication

- `run()` 打印应用名称、主菜单，并在 `q` 命令下正常退出。
- EOF 输入可正常结束，不抛出异常。
- 汇总命令展示今日、本月收入和本月结余。
- 任务、日程、学习计划、收支、笔记和 AI 草稿命令能展示对应入口。
- 无演示数据时各列表展示空状态。
- 任务列表服务失败时，控制台输出失败 `ErrorCode` 名称和失败消息。
- 收支统计第二阶段 `calculateStatistics()` 失败时，控制台输出失败 `ErrorCode` 名称和失败消息。
- AI 问答在未配置时显示 `AI_NOT_CONFIGURED` 并继续返回菜单。
- 空问题、空命令、未知命令和帮助命令均有稳定提示。
- 构造器拒绝空 `services`、`input`、`output`。

## 审查意见处理

- 已处理 `Main` 入口层测试缺口：新增 `MainTest` 覆盖 `ASSISTANT_DEMO_DATA` 默认启用、显式禁用值、其他值启用和系统属性优先于环境变量。
- 已处理 `main` 直接绑定 `System.in/out` 的可测性要求：新增 EOF 输入启动测试，并在 `finally` 中恢复全局输入、输出和系统属性。
- 为避免依赖真实环境变量，开关解析逻辑提取为包可见纯函数；私有生产路径仍按设计从系统属性和环境变量读取。
- 未回退他人的无关改动。

## 验证结果

已在 `java-ai-assistant` 目录执行：

```text
mvn test
```

结果：

```text
BUILD SUCCESS
Tests run: 844, Failures: 0, Errors: 0, Skipped: 0
```
