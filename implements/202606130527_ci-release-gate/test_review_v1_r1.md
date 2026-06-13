# 测试审查报告（v1 r1）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/test/java/assistant/docs/CiWorkflowDeliveryTest.java` — 测试仅用文本行和子串匹配验证 `.github/workflows/ci.yml`，没有解析 YAML 结构，因此不能证明 `on`、`jobs.build`、`defaults.run.working-directory`、`steps[].uses/run/with` 等配置处在有效的 GitHub Actions 层级中。错误缩进、重复键、把关键行放到错误 job/step、或把配置写入无效位置时，测试仍可能通过，导致 CI 门禁配置无效但测试放行。

## 修改要求（仅 REJECTED 时）
- `java-ai-assistant/src/test/java/assistant/docs/CiWorkflowDeliveryTest.java`：将关键契约验证从纯文本行匹配改为结构化 YAML 验证，或至少补充能绑定父子层级的解析逻辑。需要明确断言 `name == CI`、触发事件包含 `push` 和 `pull_request`、`jobs.build` 存在且其 `runs-on/defaults/steps` 位于该 job 下，并验证 checkout、setup-java、build、test 四个 step 的顺序和各自配置属于对应 step。这样才能保证测试覆盖的是实际 GitHub Actions workflow 语义，而不是文件中任意位置出现了相同字符串。
