项目根目录：/root/exp_SWAT

参考文档：
- /root/exp_SWAT/designs-tech/202606111332_java-ai-assistant-tech/tech_v2.md
- /root/exp_SWAT/docs/2 design-oo.md
- /root/exp_SWAT/docs/1 requirement.md
- /root/exp_SWAT/implements/202606110715_java_ai_assistant_full_dev/plan.md

任务：
对 java-ai-assistant 项目做一次完整交付验收，不新增功能，重点确认实现是否满足需求、OO 设计和技术设计。

验收范围：
1. 构建是否通过。
2. 全量测试是否通过。
3. CLI/应用入口是否可运行。
4. 配置加载、会话管理、草稿导入、草稿生命周期、AI 调用封装、异常处理等主流程是否符合文档。
5. 检查是否存在明显遗漏、坏味道、未覆盖风险或交付阻塞问题。

要求：
1. 可以运行测试和必要的本地命令。
2. 不要修改源码，除非发现阻塞性小问题且修复风险很低。
3. 如果修改了任何文件，必须说明原因并提交推送。
4. 输出验收报告到 /root/exp_SWAT/acceptance/20260613_full_acceptance.md。
5. 最终返回验收结论：PASS / PASS_WITH_RISKS / FAIL，并列出阻塞项、风险项、建议后续项。
