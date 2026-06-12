# 详细设计（v13）

## 概述

本轮设计目标是在 `java-ai-assistant` Maven 工程中新增 `assistant.finance` 收支记录模块的领域基础类型，固定收入/支出方向、单条交易记录不变量，以及统计结果的收入总额、支出总额和结余语义。

本轮只实现以下范围：

- `TransactionType` 收支方向枚举。
- `TransactionRecord` 可修改收支记录领域实体。
- `FinanceStatistics` 不可变统计结果值对象。
- 上述 3 个类型的 JUnit Jupiter 单元测试。

本轮不实现 `TransactionQuery`、`TransactionRepository`、`FinanceService`、`FinanceStatisticsService`，后续轮次在本轮公开接口基础上补齐查询、仓储、服务和跨记录统计计算。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `java-ai-assistant/src/main/java/assistant/finance/TransactionType.java` | 新建 | 定义收入与支出枚举值，并提供方向判断方法。 |
| `java-ai-assistant/src/main/java/assistant/finance/TransactionRecord.java` | 新建 | 定义单条收支记录领域实体，保护编号、类型、金额、类别、日期和备注不变量。 |
| `java-ai-assistant/src/main/java/assistant/finance/FinanceStatistics.java` | 新建 | 定义不可变统计结果，统一计算收入总额、支出总额和结余。 |
| `java-ai-assistant/src/test/java/assistant/finance/TransactionTypeTest.java` | 新建 | 覆盖枚举取值、方向判断互斥性、稳定名称和未知名称拒绝。 |
| `java-ai-assistant/src/test/java/assistant/finance/TransactionRecordTest.java` | 新建 | 覆盖记录创建、文本规范化、必填校验、修改详情和失败后状态不变。 |
| `java-ai-assistant/src/test/java/assistant/finance/FinanceStatisticsTest.java` | 新建 | 覆盖零值统计、结余计算、负结余允许、空值和负总额拒绝。 |

## 类型定义

### `TransactionType`

**形态**：`enum`

**包路径**：`assistant.finance`

**职责**：限定一笔交易的收支方向，使统计逻辑能明确区分收入累计和支出累计。

**类型签名定义**：`public enum TransactionType`

**枚举值**：

| 枚举值 | 语义 |
|--------|------|
| `INCOME` | 收入。 |
| `EXPENSE` | 支出。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public boolean isIncome()` | `boolean` | 当且仅当当前值为 `INCOME` 时返回 `true`。 |
| `public boolean isExpense()` | `boolean` | 当且仅当当前值为 `EXPENSE` 时返回 `true`。 |

**构造方式**：

- Java enum 默认构造。
- 输入转换可使用标准 `TransactionType.valueOf(String name)`；未知名称由 enum 标准行为抛出 `IllegalArgumentException`。

**类型关系**：

- 被 `TransactionRecord` 用作必填字段。
- 后续 `TransactionQuery`、`FinanceStatisticsService` 按此枚举区分筛选与累计方向。

### `TransactionRecord`

**形态**：`class`

**包路径**：`assistant.finance`

**职责**：表示一笔可修改的收入或支出记录，集中维护单条记录的合法性和文本规范化规则，不负责跨记录统计。

**类型签名定义**：`public class TransactionRecord`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `private final EntityId id;` | 非空；创建后不可修改。 |
| `private TransactionType type;` | 非空；只能为 `INCOME` 或 `EXPENSE`。 |
| `private TransactionAmount amount;` | 非空；由 `TransactionAmount` 保证单笔金额大于 0 且最多两位小数。 |
| `private String category;` | 非空；保存 `strip()` 后的值，规范化后不得为空。 |
| `private LocalDate date;` | 非空；表示交易发生日期。 |
| `private String note;` | 非空；空备注和空白备注统一保存为 `""`，非空备注保存 `strip()` 后的值。 |

**构造器**：

| 方法签名 | 契约 |
|----------|------|
| `public TransactionRecord(EntityId id, TransactionType type, TransactionAmount amount, String category, LocalDate date, String note)` | 任一必填参数非法时抛出异常；成功时保存规范化后的类别和备注。 |

**静态工厂**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public static TransactionRecord create(EntityId id, TransactionType type, TransactionAmount amount, String category, LocalDate date, String note)` | 等价于调用公开构造器，便于服务层和测试保持与既有模块 `create(...)` 风格一致。 |

**公开读取接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public EntityId getId()` | `EntityId` | 返回记录编号。 |
| `public TransactionType getType()` | `TransactionType` | 返回交易类型。 |
| `public TransactionAmount getAmount()` | `TransactionAmount` | 返回单笔金额。 |
| `public String getCategory()` | `String` | 返回规范化后的类别。 |
| `public LocalDate getDate()` | `LocalDate` | 返回交易日期。 |
| `public String getNote()` | `String` | 返回规范化后的备注，永不为 `null`。 |

**公开变更接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public void updateDetails(TransactionType type, TransactionAmount amount, String category, LocalDate date, String note)` | `void` | 更新类型、金额、类别、日期和备注；必须先完成全部输入校验与规范化，再改变对象字段，保证任一输入非法时对象保持原状态。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static String normalizeCategory(String category)` | `String` | `category == null` 时抛出 `NullPointerException`；`category.strip().isEmpty()` 时抛出 `IllegalArgumentException("category must not be blank")`；否则返回 `strip()` 后的类别。 |
| `private static String normalizeNote(String note)` | `String` | `note == null` 时返回 `""`；否则返回 `note.strip()`，允许结果为空字符串。 |

**构造方式**：

- 推荐服务层使用 `TransactionRecord.create(...)`。
- 测试可直接使用构造器验证构造契约。
- `EntityId`、`TransactionAmount` 均为不可变值对象，可安全保存引用。

**类型关系**：

- 依赖 `assistant.common.EntityId`、`assistant.common.TransactionAmount`。
- 依赖 `java.time.LocalDate`、`java.util.Objects`。
- 后续仓储复制实体时可通过公开读取接口和构造器重建副本。

### `FinanceStatistics`

**形态**：`record`

**包路径**：`assistant.finance`

**职责**：表示收入总额、支出总额和结余的不可变统计结果。结余统一由工厂按 `totalIncome.subtract(totalExpense)` 计算，避免调用方传入不一致结果。

**类型签名定义**：`public record FinanceStatistics(MoneyValue totalIncome, MoneyValue totalExpense, MoneyValue balance)`

**字段定义**：

| 字段签名 | 约束 |
|----------|------|
| `MoneyValue totalIncome` | 非空；不得为负数。 |
| `MoneyValue totalExpense` | 非空；不得为负数。 |
| `MoneyValue balance` | 非空；允许为负数；应等于 `totalIncome.subtract(totalExpense)`。 |

**公开接口**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `public FinanceStatistics` 紧凑构造器 | `FinanceStatistics` | 校验三个字段非空；校验收入总额和支出总额非负；校验 `balance.equals(totalIncome.subtract(totalExpense))`，不一致时抛出 `IllegalArgumentException("balance must equal totalIncome minus totalExpense")`。 |
| `public static FinanceStatistics zero()` | `FinanceStatistics` | 返回收入、支出、结余均为 `MoneyValue.zero()` 的统计结果。 |
| `public static FinanceStatistics of(MoneyValue totalIncome, MoneyValue totalExpense)` | `FinanceStatistics` | `totalIncome` 或 `totalExpense` 为空时抛出 `NullPointerException`；收入或支出为负时抛出 `IllegalArgumentException`；返回 `new FinanceStatistics(totalIncome, totalExpense, totalIncome.subtract(totalExpense))`。 |

**私有辅助方法设计**：

| 方法签名 | 返回类型 | 契约 |
|----------|----------|------|
| `private static MoneyValue requireNonNegative(MoneyValue value, String name)` | `MoneyValue` | `value == null` 时抛出 `NullPointerException(name)`；`value.value().compareTo(BigDecimal.ZERO) < 0` 时抛出 `IllegalArgumentException(name + " must not be negative")`；否则返回原值。 |

**构造方式**：

- 业务代码优先使用 `FinanceStatistics.zero()` 和 `FinanceStatistics.of(...)`。
- 公开 record 构造器保留 Java record 语义，但必须保护字段不变量和三字段一致性。

**类型关系**：

- 依赖 `assistant.common.MoneyValue`。
- 依赖 `java.math.BigDecimal`、`java.util.Objects`。
- 后续 `FinanceStatisticsService` 负责跨记录累计收入与支出后调用 `of(...)` 生成结果。

## 错误处理

- 领域基础类型沿用既有 `task`、`schedule`、`study` 模块风格：必填引用参数为空时抛出 `NullPointerException`，字段值语义非法时抛出 `IllegalArgumentException`。
- 本轮不引入 `OperationResult`，因为服务层尚未实现；后续 `FinanceService` 应捕获领域对象和值对象异常并映射为 `OperationResult`。
- `TransactionRecord` 的必填字段包括 `id`、`type`、`amount`、`category`、`date`；`note` 为可选字段，允许 `null` 并规范化为 `""`。
- `TransactionRecord.updateDetails(...)` 必须先在局部变量中完成 `type`、`amount`、`category`、`date`、`note` 的校验和规范化，再写入字段，保证异常路径无部分更新。
- `FinanceStatistics` 明确允许负结余，但拒绝负收入总额和负支出总额；该规则通过 `MoneyValue.value().compareTo(BigDecimal.ZERO)` 判断，不使用 `double`。

## 行为契约

### `TransactionType`

1. `TransactionType.values()` 的声明顺序固定为 `INCOME`、`EXPENSE`。
2. `INCOME.isIncome()` 返回 `true`，`INCOME.isExpense()` 返回 `false`。
3. `EXPENSE.isExpense()` 返回 `true`，`EXPENSE.isIncome()` 返回 `false`。
4. 任一枚举值的收入和支出判断必须互斥。

### `TransactionRecord`

1. 创建记录成功后：
   - `getId()` 返回构造参数 `id`。
   - `getType()` 返回构造参数 `type`。
   - `getAmount()` 返回构造参数 `amount`。
   - `getCategory()` 返回 `category.strip()`。
   - `getDate()` 返回构造参数 `date`。
   - `getNote()` 在 `note == null` 时返回 `""`，否则返回 `note.strip()`。
2. 类别 `""`、只含空白字符或 Unicode 空白字符规范化后为空时，创建和修改均抛出 `IllegalArgumentException`。
3. 备注允许为空、空字符串或只含空白字符；这些情况统一保存为 `""`。
4. `updateDetails(...)` 成功时只修改 `type`、`amount`、`category`、`date`、`note`，不修改 `id`。
5. `updateDetails(...)` 失败时，所有字段必须保持调用前状态。
6. `TransactionRecord` 不读取系统时间，不校验日期是否在过去或未来；只要求 `date != null`。

### `FinanceStatistics`

1. `zero()` 返回的三个字段均等于 `MoneyValue.zero()`。
2. `of(totalIncome, totalExpense)` 统一计算 `balance = totalIncome.subtract(totalExpense)`。
3. 收入大于支出时结余为正；收入等于支出时结余为零；收入小于支出时结余为负。
4. 负结余是合法状态，不得被构造器或工厂拒绝。
5. 负收入总额和负支出总额均非法，即使传入的 `balance` 与二者数学关系一致也必须拒绝。
6. record 三参数构造器必须拒绝与 `totalIncome - totalExpense` 不一致的 `balance`，防止绕过 `of(...)` 生成错误统计。

## 依赖关系

- 新增 `assistant.finance` 包只依赖 `assistant.common` 和 JDK 标准库。
- `TransactionRecord` 依赖：
  - `assistant.common.EntityId`
  - `assistant.common.TransactionAmount`
  - `java.time.LocalDate`
  - `java.util.Objects`
- `FinanceStatistics` 依赖：
  - `assistant.common.MoneyValue`
  - `java.math.BigDecimal`
  - `java.util.Objects`
- 本轮不修改既有 `assistant.common`、`assistant.task`、`assistant.schedule`、`assistant.study` 类型。
- 本轮对后续任务暴露的稳定接口：
  - `TransactionType.isIncome()`
  - `TransactionType.isExpense()`
  - `TransactionRecord.create(...)`
  - `TransactionRecord` 全量 getter
  - `TransactionRecord.updateDetails(...)`
  - `FinanceStatistics.zero()`
  - `FinanceStatistics.of(...)`

## 测试规格

### `TransactionTypeTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `exposesFixedTypeValuesInDeclaredOrder()` | 断言 `values()` 为 `{INCOME, EXPENSE}`。 |
| `incomeAndExpensePredicatesAreMutuallyExclusive()` | 断言收入/支出方向判断互斥。 |
| `valueOfParsesDeclaredTypeName()` | 断言标准 `valueOf("INCOME")` 和 `valueOf("EXPENSE")` 可用。 |
| `valueOfRejectsUnknownTypeName()` | 断言未知名称抛出 `IllegalArgumentException`。 |
| `nameUsesStableEnumConstantName()` | 断言 `INCOME.name()` 和 `EXPENSE.name()` 稳定。 |

### `TransactionRecordTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `constructorStoresProvidedFields()` | 成功创建并读取全部字段。 |
| `createFactoryCreatesEquivalentRecord()` | 静态工厂与构造器行为一致。 |
| `constructorNormalizesCategoryAndNote()` | 类别和备注首尾空白清理。 |
| `constructorConvertsNullNoteToEmptyString()` | 空备注规范化为 `""`。 |
| `constructorAllowsBlankNoteAsEmptyString()` | 空白备注规范化为 `""`。 |
| `keepsInternalWhitespaceInTextFields()` | 文本内部空白不被破坏。 |
| `rejectsNullRequiredFields()` | 空编号、空类型、空金额、空类别、空日期均抛出 `NullPointerException`。 |
| `rejectsBlankCategory()` | 空字符串、普通空白和 Unicode 空白类别均抛出 `IllegalArgumentException`。 |
| `updateDetailsChangesEditableFieldsOnly()` | 修改成功后更新所有可变字段且保留 `id`。 |
| `updateDetailsNormalizesCategoryAndNote()` | 修改路径执行同样文本规范化。 |
| `updateDetailsConvertsNullNoteToEmptyString()` | 修改路径空备注转为空字符串。 |
| `updateDetailsRejectsInvalidRequiredInputAndKeepsFieldsUnchanged()` | 任一必填字段非法时对象保持原状态。 |
| `updateDetailsRejectsBlankCategoryAndKeepsFieldsUnchanged()` | 类别为空白时对象保持原状态。 |

### `FinanceStatisticsTest`

| 测试方法 | 覆盖点 |
|----------|--------|
| `zeroReturnsAllZeroValues()` | `zero()` 三个字段均为零。 |
| `ofCalculatesPositiveBalance()` | 收入大于支出时结余正确。 |
| `ofCalculatesZeroBalance()` | 收入等于支出时结余为零。 |
| `ofAllowsNegativeBalance()` | 支出大于收入时结余为负。 |
| `ofRejectsNullTotals()` | 空收入或空支出抛出 `NullPointerException`。 |
| `ofRejectsNegativeIncomeTotal()` | 负收入总额抛出 `IllegalArgumentException`。 |
| `ofRejectsNegativeExpenseTotal()` | 负支出总额抛出 `IllegalArgumentException`。 |
| `canonicalConstructorRejectsNullFields()` | record 构造器拒绝空收入、空支出或空结余。 |
| `canonicalConstructorRejectsNegativeTotals()` | record 构造器拒绝负收入总额和负支出总额。 |
| `canonicalConstructorRejectsInconsistentBalance()` | record 构造器拒绝与收入减支出不一致的结余。 |
| `canonicalConstructorAcceptsConsistentNegativeBalance()` | record 构造器允许一致的负结余。 |

## 编码注意事项

- 所有新增文件使用 UTF-8 和项目既有 Java 代码风格。
- 生产代码不得使用 `double`、`float` 或当前系统时间。
- 金额计算统一通过 `MoneyValue.subtract(...)` 完成。
- 不新增外部依赖，不修改 Maven 构建配置。
- 单元测试只依赖 JUnit Jupiter 和本地值对象，不依赖网络、API Key、真实当前时间或外部文件。
