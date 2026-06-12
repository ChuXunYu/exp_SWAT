# 设计审查报告（v20 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** — `StructuredSuggestionParser` 的“完整 JSON 文本”解析契约没有指定如何拒绝尾随 token，容易实现出违反任务要求的解析器。设计仅说明使用 `ObjectMapper.readTree(...)` 得到 `JsonNode`，并要求自然语言包裹 JSON、多个 JSON/fenced 内容返回 `AI_MALFORMED_RESPONSE`；但 Jackson 默认 `ObjectMapper.readTree(String)` 可接受合法根对象后的尾随内容，例如 `{"type":"TASK_DRAFT",...} text` 或连续两个 JSON 对象时会返回第一个根节点而不报错。若编码者按当前设计直接实现，`JSON 前后还有自然语言`、`围栏内容不是单个 JSON 对象`、或多个根值等输入可能被误判为成功，破坏本轮任务中“优先解析完整 JSON 文本”“不从自然语言中猜测或截取 JSON”的硬约束。

## 修改要求（仅 REJECTED 时）

- 对 `StructuredSuggestionParser` 补充严格 JSON 完整性设计：明确必须拒绝根 JSON 对象后的任何非空尾随 token。可选修正方向包括在内部 `ObjectMapper` 启用 `DeserializationFeature.FAIL_ON_TRAILING_TOKENS` 后再 `readTree`，或使用 `JsonParser` 读取首个 `JsonNode` 后显式检查下一个 token 必须为 EOF。该规则也必须用于去围栏后的内容，并在 `StructuredSuggestionParserTest` 中加入尾随自然语言、连续两个 JSON 对象、fenced 内容内包含 JSON 后尾随文本等失败用例，均断言 `AI_MALFORMED_RESPONSE`。
