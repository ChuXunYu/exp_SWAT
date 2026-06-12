# 测试审查报告（v22 r2）

## 审查结果
REJECTED

## 发现
- **[一般]** `java-ai-assistant/src/main/java/assistant/app/Main.java` / `java-ai-assistant/src/test/java/assistant/app` — 详细设计为 `Main.main(...)` 和 `isDemoDataEnabled()` 定义了入口装配与演示数据开关契约，但当前测试集中没有任何 `Main` 相关测试。`ASSISTANT_DEMO_DATA` 缺失时默认启用，值为 `false`、`0`、`no` 时禁用，且系统属性优先于环境变量，这些启动期行为未被测试锁定。现有 `ConsoleApplicationTest` 和 `DemoDataFactoryTest` 只能分别验证控制台和演示数据工厂，不能证明 `Main` 会按开关正确连接两者。

## 修改要求
- 在 `java-ai-assistant/src/test/java/assistant/app` 增加入口层测试。应覆盖 `ASSISTANT_DEMO_DATA` 缺失默认启用、系统属性为 `false` / `0` / `no` 时禁用、其他值启用，以及系统属性优先于环境变量的可测路径。由于 `main` 直接绑定 `System.in/out` 与生产装配，可选择将开关解析提取为包可见纯函数，或通过反射调用私有方法并在 `finally` 中恢复系统属性；测试必须避免真实网络、避免污染全局系统属性，并使用 EOF 输入保证控制台立即退出。
