项目根目录：/root/exp_SWAT

任务：
为 java-ai-assistant 增加基础 CI / 发布门禁，确保后续提交至少自动执行构建和测试。

要求：
1. 识别项目构建工具和现有测试命令。
2. 如果项目适合 GitHub Actions，则新增或更新 .github/workflows/ci.yml。
3. CI 至少包含：
   - checkout
   - JDK 设置
   - dependency cache 如适用
   - build
   - test
4. 不引入复杂发布流程，先保持最小可用。
5. 本地验证 CI 中使用的命令可以运行。
6. 每完成一个轮次都要提交并推送 GitHub。
