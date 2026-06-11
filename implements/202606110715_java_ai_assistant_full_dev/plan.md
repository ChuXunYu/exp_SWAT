# 实现计划

任务描述：依据技术方案、架构级 OOD 和需求文档，在项目根目录新增独立 Maven 工程 `java-ai-assistant/`，完成 Java AI 个人学习与生活助手的源码、单元测试、必要文档和构建配置；普通单元测试不得依赖真实 DeepSeek、网络、API Key 或真实当前时间；每个管线任务完成后由 Runner 执行测试、中文 commit 并尝试 push，若推送失败需在验证报告说明。
项目根目录：/root/exp_SWAT

---

## R1 NEW 建立 Maven 工程骨架与通用结果基础
任务：新增 `java-ai-assistant/` Maven 单模块工程，配置 Java 17、JUnit Jupiter、Mockito、Jackson、JaCoCo、Surefire/Failsafe，并实现首批通用结果与错误基础类型，预期文件路径包括 `java-ai-assistant/pom.xml`、`java-ai-assistant/README.md`、`java-ai-assistant/docs/environment.md`、`java-ai-assistant/src/main/java/assistant/common/*`、`java-ai-assistant/src/test/java/assistant/common/*`。
选择理由：后续任务、日程、学习计划、收支、笔记、汇总和 AI 模块都依赖统一构建入口、测试执行入口、错误分类和服务返回语义；先建立可重复运行的工程和基础类型，可避免后续任务在无 Maven/JUnit 基线下分散配置。
上下文：当前仓库只有需求、OOD、技术方案和实验资料，尚无 Java 源码或构建文件。技术方案明确要求在根目录新增 `java-ai-assistant/`，使用 Maven 标准目录、Java 17、Jackson Databind 2.19.0、JUnit Jupiter 5.14.4、Mockito 5.18.0、JaCoCo 0.8.13、Surefire/Failsafe 3.5.6；错误分类至少覆盖校验、未找到、状态冲突、日程冲突、AI 配置/鉴权/限流/超时/远端不可用/空响应/响应格式异常和系统错误。
