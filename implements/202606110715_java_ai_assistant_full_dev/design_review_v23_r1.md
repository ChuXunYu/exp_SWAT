# 设计审查报告（v23 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** 可选筛选字段的读取/解析接口无法区分“空输入表示不筛选”和“非法输入导致解析失败”。`readTaskStatus(String prompt)`、`readTaskPriority(String prompt, boolean optional)`、`readTaskDueDate(String prompt, boolean optional)` 在筛选场景下均可能用 `null` 表示合法空值，同时又用 `null` 表示非法输入或 EOF。按设计中的 `filterTasks()` 契约，调用方需要在“任一非空字段解析失败时不调用服务”和“空值表示不按该字段筛选并继续构造 TaskQuery”之间做出不同处理，但现有返回类型无法表达这个差异。尤其是非法状态、非法优先级、非法日期在输出 `VALIDATION_ERROR` 后仍可能被当作合法空筛选条件，继续调用 `TaskService.listTasks(TaskQuery.of(null, ...))`，违反任务文件要求。

- **[一般]** EOF 处理契约在字段读取阶段不一致。设计一处说明 `readLine(...)` 读取到 EOF 返回 `null`，任务子菜单中任意读取步骤收到 `null` 后以 `running = false` 正常结束；但 `readTaskId(...)` 又写成“依赖 `readLine` 设置 `running = false`”。当前既有 `readLine` 对 EOF 只返回 `null`，不会设置 `running = false`。如果后续实现按该详细设计编码，子菜单字段读取中途 EOF 可能只中止当前操作而不结束程序，随后继续任务菜单循环并反复读到 EOF，偏离“EOF 任意读取步骤正常结束程序”的要求。

## 修改要求

1. 为筛选输入解析引入能区分三态/四态结果的设计，例如包可见小型结果类型、局部枚举状态、或让 `filterTasks()` 先读取原始字符串后分别处理空值、非法值和 EOF。必须明确：合法空值继续构造 `TaskQuery`；非法值输出 `VALIDATION_ERROR` 后不调用服务并留在子菜单；EOF 设置 `running = false` 并结束程序。

2. 统一字段读取 EOF 契约。要么修改 `readLine(...)` 使 EOF 明确设置 `running = false`，并评估主菜单 EOF 行为；要么要求所有字段读取调用方在收到 EOF `null` 时显式设置 `running = false`。详细设计中不能再写“依赖 `readLine` 设置 running=false”而与既有实现相矛盾。
