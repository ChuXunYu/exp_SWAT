# 测试报告（v26）

## 角色与范围

- 角色：测试编写 Agent。
- 权威依据：`implements/202606110715_java_ai_assistant_full_dev/detail_v26.md`。
- 实现参考：`implements/202606110715_java_ai_assistant_full_dev/code_v26.md`。
- 项目根目录：`/root/exp_SWAT`。
- 本步骤只编写测试，不运行测试。

## 测试文件

- `java-ai-assistant/src/test/java/assistant/app/ConsoleApplicationTest.java`

## 编写内容

在既有 `ConsoleApplicationTest` 收支子菜单覆盖基础上补强以下行为契约测试：

- `financeMenuRejectsInvalidIdBeforeCallingFinanceService()`：验证查看、修改、删除命令输入非法 id 时输出校验错误，且不调用 `FinanceService`。
- `financeMenuRejectsInvalidUpdateTypeBeforeCallingWriteService()`：验证修改命令输入非法收支类型时不调用 `updateTransaction(...)`。
- `financeMenuRejectsInvalidDateBeforeCallingFinanceService()`：验证新增收入时日期格式非法不调用 `FinanceService`。
- `financeMenuListsMoreThanTenTransactionsWithoutTruncation()`：验证收支列表输出超过 10 条记录时不截断，覆盖设计中的“列表不得限制为前 10 条”契约。

同时补充测试辅助构造：

- `transactionView(long id, String note)`：构造用于列表展示的 `TransactionView`。
- `statistics(String income, String expense)`：构造用于列表统计展示的 `FinanceStatistics`。

## 覆盖映射

- 正常路径：收支列表展示完整记录与统计。
- 边界条件：超过 10 条记录完整输出。
- 错误路径：非法 id、非法类型、非法日期。
- 状态交互：控制台校验失败前置拦截，不触发业务服务调用。

## 验证说明

按 verifier 指令，本轮只负责编写测试，未执行 `mvn test` 或其他测试命令。

