# 用户原始需求

$cangjie-requirement-design-harness 请你阅读这个仓库的三个md，然后给我设计一个需求文档，适用Java，功能不少于8个，尤其着重测试部分的内容。

# 仓库参考材料

请阅读并参考当前仓库中的以下三个 Markdown 文件：

- `/Users/chuyuxun/Documents/workspace/软测/experiments/实验报告模板.md`
- `/Users/chuyuxun/Documents/workspace/软测/experiments/实验说明.md`
- `/Users/chuyuxun/Documents/workspace/软测/experiments/软件质量保证与测试实验指导书.md`

# 从参考材料中明确得到的约束

- 这是“软件质量保证与测试”课程实验相关需求。
- 实验 1 要完成一个简单的个人助手程序，目标用户群自定，功能数不少于小组人数；本次用户进一步要求功能不少于 8 个。
- 用户明确要求适用 Java，因此需求文档应面向 Java 实现，不要设计成 Python 或其他语言优先的方案。
- 待测程序应体现较好的编码规范，包括类名、方法名、变量名命名恰当，高内聚、低耦合，层级清晰，注释合理。
- 实验 1 需要制定测试计划、设计白盒测试用例，使用 JUnit 等单元测试工具运行白盒测试用例，记录测试结果，并基于结果分析缺陷或可测性问题。
- 实验 2 需要针对实验 1 完成的应用程序制定测试计划、设计黑盒测试用例，使用手工测试方式运行黑盒测试并记录测试结果，进行结果分析。
- 实验报告需要覆盖实验 1 和实验 2 的测试计划、测试用例、测试结果分析、实验总结。
- 评分特别关注测试计划、测试用例设计、测试结果分析、待测应用程序编码规范和实验报告规范；本次需求文档应尤其强化测试相关内容。
- 提交材料包括实验报告、答辩视频、其它文档；其它文档中需要包含待测程序源代码、开发和运行环境说明、单元测试脚本。

# 迭代需求（202606111114）

用户要求使用以下 todo 文件迭代上一版需求文档：

- todo 文件：`/Users/chuyuxun/Documents/workspace/软测/experiments/requirements/202606111104_java-assistant-requirement/todo.md`
- 基线需求文档：`/Users/chuyuxun/Documents/workspace/软测/experiments/requirements/202606111104_java-assistant-requirement/req_v1.md`

本轮目标是在保留原有需求设计意图的基础上，根据 todo 文件中的待办事项修订需求文档，输出下一版完整需求文档。修订时应继续满足原始要求：适用 Java，功能不少于 8 个，尤其着重测试部分内容，并与仓库中的三个 Markdown 实验材料保持一致。

# 迭代需求（202606111139）

用户要求继续使用以下 todo 文件迭代上一版需求文档：

- todo 文件：`/Users/chuyuxun/Documents/workspace/软测/experiments/requirements/202606111104_java-assistant-requirement/todo.md`
- 基线需求文档：`/Users/chuyuxun/Documents/workspace/软测/experiments/requirements/202606111104_java-assistant-requirement/req_v2.md`

本轮目标是在 `req_v2.md` 的基础上，按照当前 `todo.md` 的最新内容修订为下一版完整需求文档。当前 todo 明确提出以下方向：

- 实验一的助手应定位为 AI 助手，需要接入 DeepSeek Flash 大模型；接口兼容 OpenAI，`base_url` 为 `https://api.deepseek.com`，模型为 `deepseek-v4-flash`。
- 需要澄清测试分层：JUnit 单元测试应保持在单元级别，通过 mock/stub 隔离外部资源和文件系统依赖；真实文件 I/O 应放入组件测试或集成测试中验证，避免把单元测试和真实文件 I/O 验证混在一起。
- 需要把“修复并复测”扩展为明确的回归测试要求：每次缺陷修复必须新增复现用例，并重跑相关模块测试及核心回归测试集。
- 白盒测试不能只列测试方法名称，还需要可证明的覆盖证据，例如核心复杂方法的控制流图、圈复杂度、独立路径设计、branch/condition coverage 结果或方法-用例映射表。
- 需要系统强化状态变化和跨模块交互测试，明确状态迁移、基本流/备选流和场景链路测试，例如任务完成/撤销、提醒状态变化、记录增删后统计同步等。

修订时仍需保留原始要求：适用 Java，功能不少于 8 个，当前范围只做实验 1，并尤其着重测试部分内容。

# 迭代需求（202606111156）

用户要求继续使用以下 todo 文件迭代上一版需求文档：

- todo 文件：`/Users/chuyuxun/Documents/workspace/软测/experiments/requirements/202606111104_java-assistant-requirement/todo.md`
- 基线需求文档：`/Users/chuyuxun/Documents/workspace/软测/experiments/requirements/202606111104_java-assistant-requirement/req_v3.md`

本轮目标是在 `req_v3.md` 的基础上，按照当前 `todo.md` 的最新内容修订为下一版完整需求文档。当前 todo 明确提出：目前功能模块太多，需要删除 3 个模块，并确保删除后的需求文档自洽且符合 `/Users/chuyuxun/Documents/workspace/软测/experiments/软件质量保证与测试实验指导书.md` 的要求。

修订时需要保留原始硬性约束：适用 Java，当前范围只做实验 1，功能不少于 8 个，尤其着重测试部分内容；如果删除 3 个模块后仍需满足“功能不少于 8 个”，应保留至少 8 个核心功能并同步调整测试计划、测试范围、用例设计、状态/场景链路和交付说明，避免文档中残留已删除模块造成不自洽。
