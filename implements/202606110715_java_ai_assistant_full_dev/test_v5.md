# 测试报告（v5）

## 修订背景

根据 `test_review_v5_r1.md` 的 REJECTED 意见，本轮修订测试覆盖缺口，不修改生产代码。

## 输入依据

| 文件路径 | 用途 |
|---------|------|
| `implements/202606110715_java_ai_assistant_full_dev/detail_v5.md` | 行为契约和测试规格依据。 |
| `implements/202606110715_java_ai_assistant_full_dev/code_v5.md` | 实现范围和偏差核对依据。 |
| `implements/202606110715_java_ai_assistant_full_dev/test_review_v5_r1.md` | 本轮 REJECTED 修订依据。 |

## 测试文件变更

| 文件路径 | 变更说明 |
|---------|----------|
| `java-ai-assistant/src/test/java/assistant/common/TransactionAmountTest.java` | 补充 `TransactionAmount.of(String)` 的金额约束错误路径断言，直接验证字符串工厂拒绝零值、负数、超过两位小数以及原始 scale 大于 2 的尾随零输入。 |
| `java-ai-assistant/src/test/java/assistant/common/MoneyValueTest.java` | 补充 `MoneyValue.of(String)` 拒绝需要舍入的数值文本断言，直接验证字符串工厂复用无舍入两位小数规范化规则。 |

## 审查反馈处理

| 审查要求 | 处理结果 |
|----------|----------|
| `TransactionAmount.of("0")` 应抛出 `IllegalArgumentException` | 已在 `stringFactoryRejectsAmountsThatViolateTransactionConstraints` 中覆盖。 |
| `TransactionAmount.of("-1.00")` 应抛出 `IllegalArgumentException` | 已在 `stringFactoryRejectsAmountsThatViolateTransactionConstraints` 中覆盖。 |
| `TransactionAmount.of("1.234")` 应抛出 `IllegalArgumentException` | 已在 `stringFactoryRejectsAmountsThatViolateTransactionConstraints` 中覆盖。 |
| `TransactionAmount.of("1.230")` 应抛出 `IllegalArgumentException` | 已在 `stringFactoryRejectsAmountsThatViolateTransactionConstraints` 中覆盖。 |
| `MoneyValue.of("1.234")` 应抛出 `IllegalArgumentException` | 已在 `stringFactoryRejectsValueThatRequiresRounding` 中覆盖。 |

## 覆盖说明

本轮补充的测试均面向公开字符串工厂接口，验证行为契约而非实现细节：

- `TransactionAmount.of(String)` 直接覆盖字符串入口对零值、负数、超过两位小数和尾随零但原始 scale 超限输入的拒绝行为。
- `MoneyValue.of(String)` 直接覆盖字符串入口对需要舍入数值文本的拒绝行为。

## 执行说明

本角色按 verifier 指令只负责编写和修订单元测试，未执行测试命令。
