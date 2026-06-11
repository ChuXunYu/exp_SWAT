# 测试审查报告（v6 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/common/TagTest.java:21` — `trimsLeadingAndTrailingWhitespace` 只使用普通 ASCII 空格，未覆盖详细设计要求的 `strip()` Unicode 空白语义。若生产实现错误地改为 `trim()`，当前测试仍可能通过，无法保护“仅包含可被 `strip()` 去除的空白文本被拒绝”和“首尾 Unicode 空白被去除”的契约。
- **[一般]** `java-ai-assistant/src/test/java/assistant/common/TagTest.java:28` — 大小写归一测试未证明实现使用 `Locale.ROOT`，也未验证在非默认英语 Locale 下的稳定结果。若生产实现错误地使用 `value.toLowerCase()` 默认 Locale，当前 `Java`、`JAVA`、`java` 等样例仍可能在当前环境通过，但会偏离详细设计中按 `Locale.ROOT` 小写归一的契约。
- **[一般]** `java-ai-assistant/src/test/java/assistant/common/TagTest.java:47` — `factoryCreatesNormalizedTag` 只覆盖 `Tag.of` 成功路径，`rejectsNullValue` 只覆盖 `Tag.of(null)`；未覆盖 `Tag.of("")` 和 `Tag.of("   ")` 的空标签拒绝。详细设计明确要求 `Tag.of(String)` 等价于构造器校验和规范化，并在错误处理表中列出 `Tag.of(blank)` 应抛出 `IllegalArgumentException`，当前测试无法捕获工厂方法绕过空白校验的实现缺陷。

## 修改要求

- 在 `java-ai-assistant/src/test/java/assistant/common/TagTest.java` 的空白处理测试附近补充 Unicode 空白用例：使用可被 `String.strip()` 去除但普通 `trim()` 不会去除的首尾空白字符，断言合法文本被规范化为无首尾空白；同时补充仅由该类 Unicode 空白构成的输入会抛出 `IllegalArgumentException`。
- 在 `java-ai-assistant/src/test/java/assistant/common/TagTest.java` 的大小写归一测试附近补充 `Locale.ROOT` 稳定性用例：临时切换默认 Locale 为土耳其语等会影响默认小写规则的 Locale，在 `finally` 中恢复原 Locale，并断言 `Tag.of("AI").value()` 仍为 `ai`，从而防止实现使用默认 Locale。
- 在 `java-ai-assistant/src/test/java/assistant/common/TagTest.java` 的 `Tag.of` 测试附近补充失败路径断言：`Tag.of("")` 和 `Tag.of("   ")` 均抛出 `IllegalArgumentException`，证明工厂方法与构造器使用同一空标签校验。
