# Java AI Assistant

Java AI Assistant 是一个面向大学生日常学习与生活管理的控制台个人助手。项目采用 Java 17 和 Maven 构建，业务数据默认保存在内存中，适合作为软件质量保证与测试课程实验的待测程序。

## 开发与运行环境

- JDK: Java 17
- 构建工具: Maven 3.9+
- 主要依赖: Jackson Databind 2.19.0
- 测试工具: JUnit Jupiter 5.14.4, Mockito 5.18.0, Maven Surefire, JaCoCo
- 运行入口: `assistant.app.Main`

## 运行方式

在 `java-ai-assistant` 目录执行：

```powershell
mvn clean package
java -jar target\java-ai-assistant-1.0.0-SNAPSHOT.jar
```

程序默认加载演示数据，便于打开后直接查看汇总、任务、日程、学习计划、收支和笔记。关闭演示数据：

```powershell
java -DASSISTANT_DEMO_DATA=false -jar target\java-ai-assistant-1.0.0-SNAPSHOT.jar
```

配置 DeepSeek 后可使用 AI 问答和 AI 草稿功能：

```powershell
$env:DEEPSEEK_API_KEY="your-api-key"
$env:DEEPSEEK_MODEL="deepseek-v4-flash"
java -jar target\java-ai-assistant-1.0.0-SNAPSHOT.jar
```

可选配置项：

- `DEEPSEEK_API_KEY`: DeepSeek API Key
- `DEEPSEEK_BASE_URL`: 默认 `https://api.deepseek.com`
- `DEEPSEEK_MODEL`: 默认 `deepseek-v4-flash`
- `DEEPSEEK_TIMEOUT_SECONDS`: 默认 `20`
- `ASSISTANT_DEMO_DATA`: `false`、`0` 或 `no` 表示不加载演示数据

## 功能模块

| 模块 | 主要类 | 功能说明 |
| --- | --- | --- |
| 应用入口与控制台交互 | `assistant.app` | 启动程序、加载演示数据、菜单导航、输入解析和结果展示 |
| 任务管理 | `assistant.task` | 新增、查看、筛选、修改、完成、撤销完成、删除任务 |
| 日程管理 | `assistant.schedule` | 新增、查看、筛选、修改、删除日程，检测时间冲突并计算状态 |
| 学习计划 | `assistant.study` | 创建学习目标、维护周期和投入小时、更新进度、计算计划状态 |
| 收支记录 | `assistant.finance` | 记录收入和支出、查询筛选、修改删除、统计收入支出和结余 |
| 笔记管理 | `assistant.note` | 新增、查看、搜索、筛选、修改、删除笔记和标签 |
| 汇总统计 | `assistant.summary` | 聚合今日任务、逾期任务、日程、学习计划、收支、笔记标签，生成 AI 本地上下文 |
| AI 问答与草稿 | `assistant.ai` | DeepSeek 兼容请求、错误映射、本地上下文 prompt、结构化 JSON 草稿、确认导入和取消 |
| 通用模型与可测试性 | `assistant.common`, `assistant.testability` | ID、日期范围、金额、进度、错误码、时间和 ID 生成抽象 |

## 适合的小组分工

| 组员 | 建议负责模块 |
| --- | --- |
| 成员 A | AI 问答、AI 结构化草稿、DeepSeek 配置与错误处理 |
| 成员 B | 任务管理、日程管理、控制台交互 |
| 成员 C | 学习计划、收支记录、笔记管理 |
| 成员 D | 汇总统计、应用入口、通用模型、构建运行 |

## 当前边界

当前版本是课程实验用控制台程序，不包含数据库持久化、图形界面、多用户账号、系统级通知或后台常驻提醒。未配置 `DEEPSEEK_API_KEY` 时，AI 功能会给出未配置提示，其它本地功能仍可正常使用。
