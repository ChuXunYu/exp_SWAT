# 测试审查报告（v5 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `java-ai-assistant/src/test/java/assistant/common/TransactionAmountTest.java` — `TransactionAmount.of(String)` 是独立公开工厂方法，详细设计明确要求字符串工厂同样禁止零值、负数、超过两位小数以及原始 scale 大于 2 的尾随零输入，但当前测试只覆盖了合法解析、`null`、空白和非数字文本，未直接验证这些金额约束是否通过字符串入口生效。
- **[一般]** `java-ai-assistant/src/test/java/assistant/common/MoneyValueTest.java` — `MoneyValue.of(String)` 是独立公开工厂方法，详细设计明确要求字符串工厂复用构造器的无舍入两位小数约束，但当前测试未直接验证字符串入口拒绝需要舍入的数值文本，例如 `"1.234"`。

## 修改要求（仅 REJECTED 时）

- `java-ai-assistant/src/test/java/assistant/common/TransactionAmountTest.java`：在现有字符串工厂测试附近补充针对 `TransactionAmount.of("0")`、`TransactionAmount.of("-1.00")`、`TransactionAmount.of("1.234")`、`TransactionAmount.of("1.230")` 的异常断言。问题在于当前测试只证明构造器拒绝这些输入，不能证明公开字符串入口也满足详细设计中“构造器和字符串工厂都禁止零值、负数和超过两位小数”的契约；期望通过直接断言字符串入口的错误路径防止未来工厂方法实现偏离。
- `java-ai-assistant/src/test/java/assistant/common/MoneyValueTest.java`：在现有 `MoneyValue.of` 错误路径测试附近补充 `MoneyValue.of("1.234")` 抛出 `IllegalArgumentException` 的断言。问题在于当前测试只证明构造器拒绝需要舍入的 `BigDecimal`，不能证明公开字符串入口也复用同一金额规范化规则；期望通过直接断言字符串入口拒绝需要舍入的文本输入覆盖该公共契约。
