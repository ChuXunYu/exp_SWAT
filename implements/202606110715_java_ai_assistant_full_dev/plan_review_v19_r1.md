# 计划审查报告（v19 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** — `AiErrorMapperTest` 被要求覆盖“未列明 4xx/3xx 的默认外部失败分类”，但 `task_v19.md` 没有明确这些默认状态码必须映射到哪个具体 `ErrorCode`。当前可选错误码里没有通用 `AI_EXTERNAL_ERROR`，可被实现者理解为 `AI_BAD_REQUEST`、`AI_REMOTE_UNAVAILABLE`、`AI_NETWORK_ERROR` 或 `SYSTEM_ERROR`，测试也无法形成唯一断言。

## 修改要求（仅 REJECTED 时）

- 固定未列明 HTTP 状态码的映射契约。需要在 `task_v19.md` 中明确：未列明 4xx、3xx，以及必要时其他未列明非 2xx 状态分别映射到哪个具体 `ErrorCode`，并要求 `AiErrorMapperTest` 与 `DeepSeekAiClientTest` 按该唯一口径断言。修正后，后续设计和实现不得自行选择默认分类。
