# 任务指令（v29）

## 动作
NEW

## 任务描述
新增并完善实验 1 测试交付文档，预期文件路径包括：

- `java-ai-assistant/docs/test-plan.md`
- `java-ai-assistant/docs/test-cases.md`
- `java-ai-assistant/docs/defect-regression.md`
- `java-ai-assistant/docs/coverage/README.md`

必要时同步更新：

- `java-ai-assistant/README.md`
- `java-ai-assistant/docs/environment.md`

本轮只补齐测试计划、白盒测试用例、覆盖证据说明、缺陷修复与回归记录、测试运行与集成测试边界说明，不新增业务功能，不修改服务公开契约。

## 选择理由
源码、单元测试、Maven 构建、AI 配置说明和 8 个核心功能控制台入口已经基本完成；v28 验证报告记录 `mvn clean test` 通过 944 个测试。完整需求仍要求实验 1 交付测试计划、白盒测试用例、JUnit 执行结果、覆盖证据、缺陷修复与回归测试记录和结果分析。当前工程只存在 `README.md` 与 `docs/environment.md`，缺少技术方案目录中规划的 `test-plan.md`、`test-cases.md`、`defect-regression.md` 和 `docs/coverage/` 说明，因此本轮应完成课程验收文档收尾。

## 任务上下文
完整需求要求：

- 测试计划说明测试目标、测试范围、测试环境、测试工具、测试分层和测试策略。
- 白盒测试方法至少体现语句覆盖、判定覆盖、条件覆盖、基本路径测试中的多种方法，并结合边界值、等价类、错误推测、状态迁移和场景链路。
- 每个核心功能至少覆盖成功路径、典型失败路径、边界输入和状态变化。
- 覆盖证据至少选择 2 到 3 个复杂核心方法，给出控制流或文字化控制流、圈复杂度、独立路径和测试用例映射；同时说明 JaCoCo 覆盖率报告生成方式。
- 缺陷修复与回归记录应说明缺陷表现、触发输入、预期结果、实际结果、修复方式、新增或保留的复现用例、重跑范围和回归结论。
- 普通 JUnit 单元测试不得访问真实 DeepSeek、真实网络、真实 API Key、真实用户文件或真实当前时间；真实 DeepSeek 连通性只能作为可选组件/集成测试说明。

8 个核心功能为：

1. AI 问答与学习生活建议。
2. AI 结构化建议确认导入。
3. 任务待办管理。
4. 日程提醒管理。
5. 学习计划管理。
6. 收支记录管理。
7. 个人笔记或日记管理。
8. 数据查询与汇总统计。

## 已有代码上下文
项目根目录下已存在独立 Maven 工程 `java-ai-assistant/`：

- `pom.xml` 已配置 Java 17、JUnit Jupiter、Mockito、Surefire/Failsafe 和 JaCoCo。
- `src/main/java/assistant/` 已包含 `common`、`task`、`schedule`、`study`、`finance`、`note`、`summary`、`ai`、`app`、`testability` 包。
- `src/test/java/assistant/` 已包含各业务包和控制台层单元测试。
- v28 验证报告记录 `mvn clean test` 通过 944 个测试，失败 0 个。
- 现有文档只有 `java-ai-assistant/README.md` 与 `java-ai-assistant/docs/environment.md`，尚缺测试计划、测试用例、缺陷回归和覆盖证据说明。

本轮文档应基于已有实际类名、测试类名和运行命令编写，不虚构尚不存在的测试脚本或真实 DeepSeek 集成测试结果。可将真实 DeepSeek 验证描述为可选 `mvn -Pintegration verify` 边界和前置条件，除非项目中已存在实际 `*IT` 测试。
