# 设计审查报告（v5 r1）

## 审查结果
APPROVED

## 发现

- **[轻微]** — `TransactionAmount.of(String)` 和 `MoneyValue.of(String)` 均允许将 `BigDecimal(String)` 抛出的 `NumberFormatException` 作为 `IllegalArgumentException` 子类向外传播。该契约可编码、可测试，也与任务中“标准异常表达输入错误”的风格一致；若后续控制台层需要更统一的提示，可在服务或输入解析边界统一转换，不影响本轮设计可用性。
- **[轻微]** — 设计要求 `TransactionAmount` 对 `1.230` 直接拒绝，而 `MoneyValue` 对 `1.2000` 规范化为 `1.20`。两者语义差异已经在文档中说明，能够支撑单笔交易输入校验与统计金额表达分离；后续实现和测试需要严格按该差异落地，避免为了复用规范化逻辑而误放宽单笔交易输入。

